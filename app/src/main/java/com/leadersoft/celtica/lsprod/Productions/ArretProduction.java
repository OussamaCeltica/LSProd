package com.leadersoft.celtica.lsprod.Productions;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.MyBD;

/**
 * Created by celtica on 08/05/19.
 */

public class ArretProduction {

    public ArretProduction(String id_arret, String date_deb, String date_fin, String motive_arret) {
        this.id_arret = id_arret;
        this.date_deb = date_deb;
        this.date_fin = date_fin;
        this.motive_arret = motive_arret;
    }

    String id_arret,date_deb,date_fin,motive_arret;

    public ArretProduction(String motive_arret){
        this.motive_arret=motive_arret;
        int i=0;
        Cursor r= Accueil.bd.read("select id_arret from production_arret order by id_arret desc limit 1 ");
        if (r.moveToNext()){
            i=r.getInt(r.getColumnIndex("id_arret"))+1;
        }
        id_arret=i+"";
    }

    public void addToBD(String id_prod){
        Accueil.bd.write2("insert into production_arret(id_arret,id_prod,motive,date_debut) values('"+id_arret+"','" + id_prod + "',?,strftime('%Y-%m-%d %H:%M','now','localtime'))", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,motive_arret+"");
                stmt.execute();
            }
        });
    }

    public String getTempsArret(){
        Cursor r= Accueil.bd.read("select ((julianday(date_fin) - julianday(date_debut))*24*60) as tmp_arret from production_arret where id_arret='"+id_arret+"' ");
        if (r.moveToNext()){
           return r.getString(r.getColumnIndex("tmp_arret"));
        }
        return "0";
    }
}
