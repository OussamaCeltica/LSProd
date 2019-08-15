package com.leadersoft.celtica.lsprod;

import android.database.Cursor;

/**
 * Created by celtica on 02/05/19.
 */

public class Depot {
    public  String code_dep,nom_dep;

    public Depot(String code_dep){
        this.code_dep=code_dep;
    }

    public Depot(String code_dep, String nom_dep) {
        this.code_dep=code_dep;
        this.nom_dep=nom_dep;
    }

    public boolean existe(){
        Cursor r=Accueil.bd.read("select * from depot where codebar='"+code_dep+"'");
        if (r.moveToNext()){
            nom_dep=r.getString(r.getColumnIndex("nom_dep"));
            return true;
        }
        else{
            return false;
        }
    }
}
