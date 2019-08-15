package com.leadersoft.celtica.lsprod.Productions;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.MyBD;

/**
 * Created by celtica on 30/03/19.
 */

public class ProductionEnArret extends Production {
    String motive_arret;
    public ProductionEnArret(String id,String codebar, String code_chef, String code_ligne, String heure, String heure_fin, int qt) {
        super(id,codebar, code_chef, code_ligne, heure, heure_fin, qt);
        super.type="en_arret";
    }

    public ProductionEnArret(String id_prod){
        super(id_prod);
    }
    public ProductionEnArret(String id_prod,String motive_arret){
        super(id_prod);

    }


    public void relancerProduction(String id_arret){
        Accueil.bd.write("update production_arret set date_fin=strftime('%Y-%m-%d %H:%M','now','localtime')   where id_arret='"+id_arret+"'");
    }

    public String getLastArretId(){
        Cursor r=Accueil.bd.read("select id_arret from production_arret where id_prod='"+id_prod+"' order by id_arret desc limit 1 ");
        if (r.moveToNext())
            return r.getString(r.getColumnIndex("id_arret"));
        return "";
    }
}
