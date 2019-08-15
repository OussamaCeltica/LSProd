package com.leadersoft.celtica.lsprod.Maintenances;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.MyBD;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by celtica on 16/02/19.
 */

public class Maintenance {
    String id,date_debut,date_fin,code_ligne,code_employé,code_maint,nom_emp,nom_ligne,motive_maint;
    private int id2;

    public ETAT etat;


    public Maintenance(String id, String date_debut, String date_fin, String code_ligne, String code_employé, String type_maintenance) {
        this.id = id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.code_ligne = code_ligne;
        this.code_employé = code_employé;
        this.motive_maint = type_maintenance;
        getMotiveFromMaint();
    }

    public Maintenance(String id, String date_debut, String date_fin, String code_ligne, String code_employé, String type_maintenance,ETAT e) {
        this.id = id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.code_ligne = code_ligne;
        this.code_employé = code_employé;
        this.motive_maint = type_maintenance;
        etat=e;
        nom_ligne=getNomLigneFromMaint();
        getMotiveFromMaint();
    }

    public Maintenance(String code_ligne, String code_employé, String code_maint) {

        this.code_ligne = code_ligne;
        this.code_employé = code_employé;
        this.code_maint = code_maint;
        nom_ligne=getNomLigne();
        nom_emp=getNomEmploye();
    }

    public Maintenance(String id) {
        this.id = id;

        //Récupération des info selon id_maintain
    }



    public String getNomEmploye(){
        Cursor r= Accueil.bd.read("select nom_emp from employe where Oid='"+code_employé+"'");
        if(r.moveToNext())
            return r.getString(r.getColumnIndex("nom_emp"));
        return "";
    }

    public String getNomEmployeFromMaint(){
        Cursor r=Accueil.bd.read("select nom_emp from maintenance where id='"+id+"'");
        if(r.moveToNext())
            return r.getString(r.getColumnIndex("nom_emp"));
        return "";
    }
    public String getNomLigneFromMaint(){
        Cursor r=Accueil.bd.read("select nom_ligne from maintenance where id='"+id+"'");
        if(r.moveToNext())
            return r.getString(r.getColumnIndex("nom_ligne"));
        return "";
    }

    public String getMotiveFromMaint(){
        Cursor r=Accueil.bd.read("select type_mantain,oid_type_maint from maintenance where id='"+id+"'");
        if(r.moveToNext()) {
            code_maint = r.getString(r.getColumnIndex("oid_type_maint"));
            return r.getString(r.getColumnIndex("type_mantain"));
        }
        return "";
    }

    public String getNomLigne(){
        Cursor r=Accueil.bd.read("select designiation from ligne_production where Oid='"+code_ligne+"'");
        if(r.moveToNext())
            return r.getString(r.getColumnIndex("designiation"));
        return "";
    }

    public String getMotiveMaint(){
        Cursor r=Accueil.bd.read("select type from type_maintenance where Oid='"+code_maint+"'");
        if(r.moveToNext())
            return r.getString(r.getColumnIndex("type"));
        return "";
    }

    public void addToBD(String scan){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        if(scan.equals("first-scan")){
             id2=0;
            Cursor r=Accueil.bd.read("select * from derniere_export order by id_maint desc limit 1");
            if (r.moveToNext()){
                id2=Integer.parseInt(r.getString(r.getColumnIndex("id_maint")))+1;
            }
            Log.e("rrr",nom_emp+" / "+nom_ligne);
            try {
                Accueil.bd.write2("insert into maintenance(id,code_emp,nom_emp,code_ligne,nom_ligne,oid_type_maint,type_mantain,date_debut,sync) values('" + id2 + "','"+code_employé+"',?,'"+code_ligne+"',?,'"+code_maint+"',?,'" + date + "','0')", new MyBD.SqlPrepState() {
                    @Override
                    public void putValue(SQLiteStatement stmt) {
                        try{
                            stmt.bindString(1,nom_emp+"");
                            stmt.bindString(2,nom_ligne+"");
                            stmt.bindString(3,getMotiveMaint()+"");
                            stmt.execute();
                            Accueil.bd.write("update derniere_export set id_maint='"+id2+"' ");
                        }catch (SQLException e){
                            e.printStackTrace();
                            Toast.makeText(DeviceConfig.me,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }catch (SQLException e){
                e.printStackTrace();
                Toast.makeText(DeviceConfig.me,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        }else {
            Accueil.bd.write("update maintenance set date_fin='"+date+"' where id='"+id+"' ");
            Log.e("idd",""+id);
        }

    }

    public void changeState(String etat){
        //etat=supprimé ou exporté ..
        Accueil.bd.write("update maintenance set sync='1',etat='"+etat+"' where id='"+id+"'");
    }

    public void exportéMaintenance(){

        HashMap<Integer,String> datas=new HashMap<Integer,String> ();
        datas.put(1,code_ligne);
        Log.e("reqq","insert into Maintenance (Oid,productionLine,employee,start,[end],motive) values(NEWID(),?,'"+code_employé+"',CAST("+date_debut+" as datetime),CAST("+date_fin+" as datetime),'"+code_maint+"')");
        Accueil.BDsql.write2("insert into Maintenance (Oid,productionLine,employee,start,[end],motive) values(NEWID(),?,'"+code_employé+"',CAST("+date_debut+" as datetime),CAST("+date_fin+" as datetime),'"+code_maint+"')",datas, new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(java.sql.SQLException e) {
                Synchronisation.ExportationErr =Synchronisation.ExportationErr+ "-Erreur d exportation  de Maintenance: \n "+e.getMessage()+" \n";

            }

            @Override
            public void before() {

            }

            @Override
            public void After()  {
                changeState("exporté");
            }
        });
    }


}
