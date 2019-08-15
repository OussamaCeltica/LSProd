package com.leadersoft.celtica.lsprod;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by celtica on 02/05/19.
 */

public class UpdaterBD {
    public static void update(AppCompatActivity c) throws PackageManager.NameNotFoundException {
        PackageManager manager = c.getPackageManager();
        PackageInfo info = manager.getPackageInfo(c.getPackageName(), PackageManager.GET_ACTIVITIES);
        int appVersion=info.versionCode;
        Log.e("vvv",appVersion+"/" );

        try {
            Cursor r=Accueil.bd.read("select code_version,device_name from admin");
            if(r.moveToNext()){
                Log.e("vvv",r.getInt(r.getColumnIndex("code_version"))+"/ my code / device "+r.getString(r.getColumnIndex("device_name")));
                if(r.getInt(r.getColumnIndex("code_version")) == 0 && r.getString(r.getColumnIndex("device_name")) != null){
                    Accueil.bd.write("update admin set code_version='14'");
                }
                 r=Accueil.bd.read("select code_version,device_name from admin");
                r.moveToNext();
                if(r.getString(r.getColumnIndex("code_version")) != null){

                    int myVersion=r.getInt(r.getColumnIndex("code_version"));
                    while (myVersion != appVersion){
                        Log.e("vvv",appVersion+"/"+myVersion);
                        switch (myVersion){
                            case 14:{
                                Accueil.bd.write("CREATE TABLE bon_preparation (\n" +
                                        "    id_bon          VARCHAR (60)  NOT NULL\n" +
                                        "                                  PRIMARY KEY,\n" +
                                        "    code_ligne      VARCHAR (100),\n" +
                                        "    nom_ligne       VARCHAR (100),\n" +
                                        "    code_pr         VARCHAR (200),\n" +
                                        "    nom_pr          VARCHAR (200),\n" +
                                        "    date_bon        DATETIME,\n" +
                                        "    sync            VARCHAR (2),\n" +
                                        "    etat            VARCHAR (30) \n" +
                                        ")\n");

                                Accueil.bd.write("CREATE TABLE produit_preparer (\n" +
                                        "    id_bon      VARCHAR (60)  REFERENCES bon_preparation (id_bon) ON DELETE CASCADE\n" +
                                        "                                                                 ON UPDATE CASCADE,\n" +
                                        "    codebar_pr  VARCHAR (100),\n" +
                                        "    nom_pr      VARCHAR (200),\n" +
                                        "    quantité    DOUBLE\n" +
                                        ")\n");

                                Accueil.bd.write("CREATE TABLE produit_preparer_production (\n" +
                                        "    id_prod    INTEGER (60)  REFERENCES production (id_prod) ON DELETE CASCADE\n" +
                                        "                                                             ON UPDATE CASCADE,\n" +
                                        "    codebar_pr VARCHAR (100),\n" +
                                        "    nom_pr     VARCHAR (200),\n" +
                                        "    quantité   DOUBLE,\n" +
                                        "    code_depot VARCHAR (100),\n" +
                                        "    nom_depot  VARCHAR (100),\n" +
                                        "    num_lot    VARCHAR (100) \n" +
                                        ")\n");


                                Accueil.bd.write("Alter table derniere_export add id_bon_preparation varchar(60)");

                                Accueil.bd.write("update   derniere_export set id_bon_preparation='0'");

                                Accueil.bd.write("Alter table production add isPrep varchar(2) ");





                            }
                            break;
                        }
                        myVersion++;
                    }

                }
                Accueil.bd.write("update admin set code_version='"+appVersion+"'");

            }
        }
        catch(SQLiteException e) {


            Accueil.bd.write("alter table admin add isLot varchar(2) ");
            Accueil.bd.write("alter table admin add device_name varchar(100)  ");
            Accueil.bd.write("update admin set isLot='0' ");
            Accueil.bd.write("alter table admin add prodIsPackaging varchar(2) ");
            Accueil.bd.write("update admin set prodIsPackaging='1' ");
            Accueil.bd.write("alter table admin add code_version varchar(30) ");
            Accueil.bd.write("update admin set code_version='"+info.versionCode+"' ");
            Accueil.bd.write("Alter table admin add defaultDepot VARCHAR (100)");
            Accueil.bd.write("Alter table production add code_depot VARCHAR (100)");
            Accueil.bd.write("CREATE TABLE depot (\n" +
                    "    codebar VARCHAR (100) NOT NULL,\n" +
                    "    nom_dep VARCHAR (100),\n" +
                    "    PRIMARY KEY (\n" +
                    "        codebar\n" +
                    "    )\n" +
                    ")");
            Accueil.bd.write("Alter table production add num_lot    VARCHAR (100)");
            Accueil.bd.write("Alter table production add isPackaging    VARCHAR (2)");
            Accueil.bd.write("update production set isPackaging='1',code_depot=''");


            try {
                Accueil.bd.write("DROP TABLE  production_arret ");
            }catch (SQLiteException e2){

            }

            Accueil.bd.write("CREATE TABLE production_arret (\n" +
                    "    id_arret   INTEGER,\n" +
                    "    id_prod    INTEGER       NOT NULL\n" +
                    "                             REFERENCES production (id_prod) ON DELETE CASCADE\n" +
                    "                                                             ON UPDATE CASCADE,\n" +
                    "    motive     VARCHAR (100),\n" +
                    "    date_debut DATETIME,\n" +
                    "    date_fin   DATETIME,\n" +
                    "    CONSTRAINT fk_prod FOREIGN KEY (\n" +
                    "        id_prod\n" +
                    "    )\n" +
                    "    REFERENCES production (id_prod) \n" +
                    ")");
        }



    }
}
