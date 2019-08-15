package com.leadersoft.celtica.lsprod.Productions;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.Preparations.ProduitPréparé;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by celtica on 14/11/18.
 */

public class Production extends PublicationProduction {
    public String id_prod,codebar,heure,heure_fin,code_chef,code_ligne,nomChef,nomPr,code_depot,num_lot;
    public boolean isPackaging,isPrep=false;
    public double qt;
    Produit pr;

    public ETAT etat=ETAT.VALIDÉ;


    public Production(String id,String codebar,String code_chef,String code_ligne,String heure,String heure_fin, double qt) {
        super("production");
        this.codebar = codebar;
        this.id_prod=id;
        this.heure = heure;
        this.qt = qt;
        this.code_chef=code_chef;
        this.code_ligne=code_ligne;
        this.heure_fin=heure_fin;
        getProdInfo();
        //getNomChef();
    }

    public Production(String id){
        super();
        this.id_prod=id;

    }

    public Production() {

    }


    public void changeState(String etat){
        //etat=supprimé ou exporté ..
        Accueil.bd.write("update production set sync='1',etat='"+etat+"' where id_prod='"+id_prod+"'");
    }

    public void getProdInfo(){
        Cursor r=Accueil.bd.read("select * from production where id_prod='"+id_prod+"'");

        if(r.moveToNext()){

            nomPr=r.getString(r.getColumnIndex("nom_pr"));
            nomChef=r.getString(r.getColumnIndex("nom_chef"));
            code_depot=r.getString(r.getColumnIndex("code_depot"));
            num_lot=r.getString(r.getColumnIndex("num_lot"));
            isPackaging=(r.getString(r.getColumnIndex("isPackaging"))+"").equals("1");
        }
    }

    public void getNomChef(){
        Cursor r=Accueil.bd.read("select * from chef_ligne where code_chef='"+code_chef+"'");
        if(r.moveToNext()){
            nomChef=r.getString(r.getColumnIndex("nom"));
        }
    }

    public String getProduit(){

        Cursor r=Accueil.bd.read("select * from produit where codebar='"+codebar+"'");

        if(r.moveToNext()){
            Log.e("ppp",r.getString(r.getColumnIndex("nom_pr"))+"");
        return r.getString(r.getColumnIndex("nom_pr"));
        }

        return "";


    }

    public ArrayList<ProduitPréparé> getProduitPreparer(){
        ArrayList<ProduitPréparé> produits=new ArrayList<>();
        Cursor r= Accueil.bd.read("select * from produit_preparer_production where id_prod='"+id_prod+"' ");
        while (r.moveToNext()){
            Log.e("ddd","Kayen / "+r.getString(r.getColumnIndex("num_lot")));
            String lot="";
            if (!r.getString(r.getColumnIndex("num_lot")).equals("")) lot=r.getString(r.getColumnIndex("num_lot"));
             produits.add(new ProduitPréparé(r.getString(r.getColumnIndex("codebar_pr")),r.getString(r.getColumnIndex("nom_pr")),r.getString(r.getColumnIndex("code_depot"))+"",r.getString(r.getColumnIndex("nom_depot"))+"",lot+"",r.getDouble(r.getColumnIndex("quantité"))));
        }

        return produits;
    }

    public void suppPrPréparé(){
        Accueil.bd.write("delete from produit_preparer_production where id_prod='"+id_prod+"'");
    }

    public void suppProd(){
        Accueil.bd.write("delete from production where id_prod='"+id_prod+"'");
    }

    public boolean produitExisteInProd(String code){
        Cursor r2=Accueil.bd.read("select code_pr from production where id_prod='"+id_prod+"'");
        if(r2.moveToNext()){
            if (r2.getString(r2.getColumnIndex("code_pr")).equals(code)){
               return true;
            }else {
                return false;
            }
        }

        return false;
    }

    public ArrayList<ArretProduction> getArretProd(){
        ArrayList<ArretProduction> arrets=new ArrayList<ArretProduction>();
        Cursor r=Accueil.bd.read("select * from production_arret where id_prod='"+id_prod+"'");
        while (r.moveToNext()){
            arrets.add(new ArretProduction(""+r.getString(r.getColumnIndex("id_arret")),""+r.getString(r.getColumnIndex("date_debut")),""+r.getString(r.getColumnIndex("date_fin")),""+r.getString(r.getColumnIndex("motive"))));
        }

        return arrets;
    }

    public void exportéProd(final int record_id){

        //region envoyé les données aux tables intermédiaire ..
               // Accueil.BDsql.beginTRansact();
                // Log.e("prodd", String.format("%5f",r2.getDouble(r2.getColumnIndex("date_deb")))+" / "+r2.getFloat(r2.getColumnIndex("date_f")));
                HashMap<Integer,String> datas=new HashMap<Integer,String> ();
                datas.put(1,code_ligne);
                datas.put(2,num_lot);

                String packaging="F";
                if (isPackaging) packaging="T";

                Accueil.BDsql.write2("insert into BonProductionMobile (RECORDID,NUM_BON,CODE_BARRE,LIGNE_PRODUCTION,CHEF_LIGNE_PRODUCTION,QTE,DATE_DEBUT,DATE_FIN,BLOCAGE,TEMPS_ARRET,CODEBARRE_DEPOT_PF,NUM_LOT,QTE_PAR_CARTON) values ('" + record_id + "','"+ DeviceConfig.session.deviceId +"_"+id_prod+"','" + codebar + "',?,'" +code_chef + "','" +qt+ "',CAST(" +heure+ " as datetime),CAST(" + heure_fin + " as datetime),'F','0','"+code_depot+"',?,'"+packaging+"') ",datas
                        , new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(SQLException e) {
                                Log.e("sqll","export prod: "+e.getMessage());
                                Accueil.BDsql.transactErr=true;
                                Synchronisation.ExportationErr=Synchronisation.ExportationErr+"Erreur d insertion dans BonProductionMobile: "+e.getMessage()+" \n ";
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After() {

                            }

                        });
/*
                                   //region exporté les arrets productions
                                   Cursor r3=Accueil.bd.read("select * from production_arret");
                                   while (r3.moveToNext()){
                                       Accueil.BDsql.write("insert", new SqlServerBD.doAfterBeforeGettingData() {
                                           @Override
                                           public void echec(SQLException e) {
                                               Accueil.BDsql.transactErr=true;
                                               ExportationErr=ExportationErr+"Erreur d insertion dans arretProduction: \n "+e.getMessage()+" \n ";

                                           }

                                           @Override
                                           public void before() {

                                           }

                                           @Override
                                           public void After()  {

                                           }
                                       });
                                   }

                                   //endregion
                               */





        //endregion

    }


}
