package com.leadersoft.celtica.lsprod.Productions;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.Depot;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.MyBD;
import com.leadersoft.celtica.lsprod.Preparations.ProduitPréparé;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by celtica on 17/02/19.
 */

public class ProductionEnCours extends Production {

    public ProductionEnCours(String id,String codebar, String code_chef, String code_ligne, String heure, String heure_fin, double qt) {
        super(id,codebar, code_chef, code_ligne, heure, heure_fin, qt);
        super.type="en_cours";
        etat= ETAT.EN_COURS;
    }

    public ProductionEnCours(String codebar, String code_chef, String code_ligne) {
        super();
        this.codebar=codebar;
        this.code_chef=code_chef;
        this.code_ligne=code_ligne;
        super.type="en_cours";
        getNomChef();
        this.nomPr=getProduit();

        int id=0;
        Cursor r=Accueil.bd.read("select id_prod from derniere_export order by id_prod desc limit 1");
        if(r.moveToNext()){
            id=Integer.parseInt(r.getString(r.getColumnIndex("id_prod")))+1;
        }
        this.id_prod=String.valueOf(id);

    }

    public ProductionEnCours(String id){
        super(id);
        this.id_prod=id;
        Cursor r=Accueil.bd.read("select * from production where id_prod='"+id_prod+"'");
        while (r.moveToNext()){
            nomPr=r.getString(r.getColumnIndex("nom_pr"));
            nomChef=r.getString(r.getColumnIndex("nom_chef"));
            codebar=r.getString(r.getColumnIndex("code_pr"));
            code_ligne=r.getString(r.getColumnIndex("code_ligne"));
            code_chef=r.getString(r.getColumnIndex("code_chef"));
            heure=r.getString(r.getColumnIndex("date_debut"));
        }

    }

    public void arreterProd(final String motive){

        Accueil.bd.write2("insert into production_arret(id_prod,motive,date_debut) values('" + id_prod + "',?,'"+heure+"','"+heure_fin+"')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,motive+"");
                stmt.execute();
            }
        });
    }

    public void terminerProd(double qt, String code_depot, final String numLot){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        if (code_depot.equals(""))
        code_depot=DeviceConfig.session.getDefaultDepot();

        int i=0;
        if (DeviceConfig.session.prodIsPackaging) i=1;

        Accueil.bd.write2("update production set date_fin=strftime('%Y-%m-%d %H:%M','now','localtime'),quantité=quantité+" + qt + ", code_depot='"+code_depot+"' , num_lot=?,isPackaging='"+i+"' where id_prod='" + id_prod + "'", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,numLot+"");
                stmt.execute();
            }
        });
        heure_fin=date;
        this.qt=qt;
    }

    public boolean hasProduit(String code){
        Cursor r=Accueil.bd.read("select code_pr from production where id_prod='"+id_prod+"' ");
        if (r.moveToNext()){
            if (r.getString(r.getColumnIndex("code_pr")).equals(code)){
                return true;
            }
            return false;
        }
        return false;
    }


    public Depot getLastDepotPréparé(){
        Cursor r=Accueil.bd.read("select * from produit_preparer_production where id_prod='"+id_prod+"' ORDER BY rowid DESC LIMIT 1");
        if(r.moveToNext()){
            Depot d=new Depot(r.getString(r.getColumnIndex("code_depot")),r.getString(r.getColumnIndex("nom_depot")));
            return d;
        }
        return null;
    }



    public void addToBD(){
        int isprep=0;
        if (isPrep) isprep=1;

        Accueil.bd.write2("insert into production (id_prod,code_chef,nom_chef,code_ligne,code_pr,nom_pr,date_debut,sync,quantité,isPrep) values('" + id_prod + "','" + DeviceConfig.session.code_chef + "',?,?,'" + codebar + "',?,strftime('%Y-%m-%d %H:%M','now','localtime'),0,0,'"+isprep+"')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,nomChef);
                stmt.bindString(2,code_ligne);
                stmt.bindString(3,nomPr);

                stmt.execute();

            }
        });
        //Accueil.bd.write("insert into production (id_prod,code_chef,nom_chef,code_ligne,code_pr,nom_pr,date_debut,sync,quantité) values('"+id_prod+"','"+ DeviceConfig.session.code_chef+"','"+nomChef+"','"+DeviceConfig.session.code_ligne+"','"+pr+"','"+nom_pr+"',strftime('%Y-%m-%d %H:%M','now','localtime'),0,0)");
        Accueil.bd.write("update derniere_export set id_prod='"+id_prod+"' ");
    }

}
