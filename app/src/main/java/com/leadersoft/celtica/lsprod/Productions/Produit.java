package com.leadersoft.celtica.lsprod.Productions;

import android.database.Cursor;

import com.leadersoft.celtica.lsprod.Accueil;

/**
 * Created by celtica on 14/11/18.
 */

public class Produit {
    public String codebar,designiation,condition;
    int qt_condition;

    public Produit(String codebar, String designiation, String condition, int qt_condition) {
        this.codebar = codebar;
        this.designiation = designiation;
        this.condition = condition;
        this.qt_condition = qt_condition;
    }

    public Produit(String codebar) {
        this.codebar = codebar;

    }

    public Produit() {

    }

    public boolean existe(){
        Cursor r=Accueil.bd.read("select * from produit where codebar='"+codebar+"' ");
        if (r.moveToNext()){
            designiation=r.getString(r.getColumnIndex("nom_pr"));
            return true;
        }
        return false;
    }
}
