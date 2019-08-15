package com.leadersoft.celtica.lsprod.Preparations;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.MyBD;
import com.leadersoft.celtica.lsprod.Productions.Produit;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;

import java.sql.SQLException;
import java.util.ArrayList;

public class BonPreparation {
    public String id_bon,codebar_pr,nom_pr,code_ligne,nom_ligne,date;
    ETAT etat;

    public BonPreparation() {

    }

    public BonPreparation(String codebar_pr,String nom_pr, String code_ligne,String nom_ligne) {
        int id=0;
        Cursor r= Accueil.bd.read("select id_bon_preparation from derniere_export ");
        if (r.moveToNext()){
            id=r.getInt(r.getColumnIndex("id_bon_preparation"))+1;
        }
        this.id_bon=id+"";
        this.codebar_pr = codebar_pr;
        this.code_ligne = code_ligne;
        this.nom_pr=nom_pr;
        this.nom_ligne=nom_ligne;

    }

    public BonPreparation(String codebar_pr) {
        this.codebar_pr = codebar_pr;
    }

    public BonPreparation(String id_bon, String codebar_pr, String nom_pr, String code_ligne, String nom_ligne, String date, ETAT etat) {
        this.id_bon = id_bon;
        this.codebar_pr = codebar_pr;
        this.nom_pr = nom_pr;
        this.code_ligne = code_ligne;
        this.nom_ligne = nom_ligne;
        this.date = date;
        this.etat = etat;
    }

    public ArrayList<ProduitPréparé> getProduitPreparer(){
        ArrayList<ProduitPréparé> produits=new ArrayList<>();
        Cursor r= Accueil.bd.read("select * from produit_preparer where id_bon='"+id_bon+"' ");
        while (r.moveToNext()){
            Log.e("ddd","Kayen");
            //produits.add(new ProduitPréparé(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getDouble(r.getColumnIndex("quantité"))));
        }

        return produits;
    }

    public void addToBD(){

        Accueil.bd.write2("insert into bon_preparation (id_bon,code_ligne,nom_ligne,code_pr,nom_pr,date_bon,sync,etat) values('" + id_bon + "',?,?,'" + codebar_pr + "',?,strftime('%Y-%m-%d %H:%M','now','localtime'),'0','en cours')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,code_ligne+"");
                stmt.bindString(2,nom_ligne+"");
                stmt.bindString(3,nom_pr+"");
                stmt.execute();
            }
        });

        Accueil.bd.write("update derniere_export set id_bon_preparation='"+id_bon+"' ");
    }

    public void exportéBon(){
        Accueil.BDsql.write("insert", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr = true;
                Synchronisation.ExportationErr = Synchronisation.ExportationErr + "Erreur d insertion dans bonPrepMobile le bon: " + id_bon + " \n " + e.getMessage() + " \n ";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {

            }
        });
    }

    public void changeState(String state) {
        // state= en cours /validé/exporté/supprimé
        Accueil.bd.write("update bon_preparation set etat='"+state+"' where id_bon='"+id_bon+"' ");
    }

    public void suppPrPréparé(){
        Accueil.bd.write("delete from produit_preparer where id_bon='"+id_bon+"'");
    }

    public void validerBon() {
        changeState("validé");
    }
}
