package com.leadersoft.celtica.lsprod;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.leadersoft.celtica.lsprod.Maintenances.AddMaintenance;
import com.leadersoft.celtica.lsprod.Maintenances.AfficherMaintenance;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsprod.Preparations.FairePrepConfig;
import com.leadersoft.celtica.lsprod.Productions.AfficherStock;
import com.leadersoft.celtica.lsprod.Productions.FaireProdConfig;
import com.leadersoft.celtica.lsprod.Productions.Production;

import java.util.ArrayList;


public class Accueil extends AppCompatActivity {

    public static MyBD bd;
    public static SqlServerBD BDsql;

    DrawerLayout mDrawerLayout;
    MySpinnerSearchable spinn;
    ArrayList<SpinnerItem> items=new ArrayList<SpinnerItem>();
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_accueil);
            bd=new MyBD("lsprod.db",Accueil.this);



            Cursor r2 = Accueil.bd.read("select * from  produit limit 5");
            while (r2.moveToNext()) {
                Log.e("mmm", " "+r2.getString(r2.getColumnIndex("codebar")));
            }



            //bd.write("insert into produit (codebar,nom_pr) values('6136552001333','PRODUCT COTEX') ");

            //Accueil.bd.write("update  production set sync='0',date_fin=NULL");

            //Accueil.bd.write("insert into depot(codebar,nom_dep) values('6130552001225','DEPOT 0001')");

            //bd.write("update maintenance set nom_emp='Mohamed Amine ',type_mantain='CHANGEMENT DES JOINTS',nom_ligne='LIGNE N=1' ");

            //bd.write("insert into employe (code_emp,nom_emp,codebar,Oid) values('EMP0002','KARIM OUKIL','6130552001225','6262rf2f6r') ");

            //bd.write("insert into type_maintenance (type,Oid) values('CHANGEMENT DES JOINTS','0556oid') ");
            //bd.write("insert into produit (codebar,nom_pr) values('6130552001225','MAUCHOIR COTEX') ");
            //bd.write("insert into type_arret (type) values('Coupure de courant') ");

            //bd.write("update maintenance set date_debut='2019-12-31 15:00:00',date_fin='2019-08-15 08:08:08',sync='0' ");
            //bd.write("delete from maintenance where '1'='1' ");
            //bd.write("insert into chef_ligne (code_chef, nom,mdp_chef,codebar) values('61350552001225','SALIM','123','6130552001225')");
            //bd.write("insert into ligne_production (code_ligne, designiation,codebar,Oid) values('LIGNE_PROD','Ligne pour LIMONADE','6130552001225','586oid566')");

            //des test de bd


            //Accueil.bd.write("update maintenance set etat='supprimé'");




            //region tester si il ya une session ouverte ou nn ..
            /*
            Cursor r=Accueil.bd.read("select * from session");
            if(!r.moveToNext()){
                startActivity(new Intent(Accueil.this,DeviceConfig2.class));
                finish();
            }else {
                 DeviceConfig.session=new Session(r.getString(r.getColumnIndex("code_chef")),r.getString(r.getColumnIndex("code_ligne")),r.getString(r.getColumnIndex("type_device")));
            }
            */

            /*------------------------------- OFFICIEL -----------------------------*/

            //region UPDATER ..
            try {
                UpdaterBD.update(this);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //endregion

            //region test si le mobile has ID
             r2 = Accueil.bd.read("select device_name from  admin");
            if (r2.moveToNext()) {
                if(r2.getString(r2.getColumnIndex("device_name")) == null) {
                    Intent intent = new Intent(getApplicationContext(), SetDeviceId.class);
                    startActivity(intent);
                    finish();
                }

            }
            //endregion

            //region check camera permission ..
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(Accueil.this, new String[]{Manifest.permission.CAMERA}, 8);

            }
            //endregion

            //region set up type_device ..
            Cursor r=Accueil.bd.read("select * from admin");
            if (r.moveToNext()){
                if (r.getString(r.getColumnIndex("type_device")) == null){
                    Accueil.bd.write("update admin set type_device='"+getResources().getString(R.string.config_type_sansscanner)+"' ");
                }
            }
            //endregion

            //region open session
             r=Accueil.bd.read("select * from admin");
            if(r.moveToNext()){
                DeviceConfig.session=new Session(null,null,r.getString(r.getColumnIndex("type_device")));
            }
            //endregion

            //region configuration drawer layout ..
            mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);

            ((ImageView)findViewById(R.id.drawer)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                }
            });


            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                            // set item as selected to persist highlight
                            //menuItem.setChecked(true);

                        if(menuItem.getItemId()== R.id.menu_parametrage){
                            startActivity(new Intent(Accueil.this,Parametrage.class));

                        }
                        else if(menuItem.getItemId()== R.id.menu_sync) {
                             startActivity(new Intent(Accueil.this,Synchronisation.class));

                        }else if (menuItem.getItemId() == R.id.menu_archive){
                            Intent i=new Intent(Accueil.this,AfficherStock.class);
                            i.putExtra("request","archive");
                            startActivity(i);
                        }


                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();

                            // Add code here to update the UI based on the item selected
                            // For example, swap UI fragments here

                            return true;
                        }
                    });

            //endregion

            //region faire prod ..
            ((LinearLayout)findViewById(R.id.add_stock_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Intent i=new Intent(Accueil.this,DeviceConfig2.class);
                    //startActivity(i);
                    startActivity(new Intent(Accueil.this,FaireProdConfig.class));
                }
            });
            //endregion

            //region afficher le stock ..
            ((LinearLayout)findViewById(R.id.afficher_stock_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(Accueil.this, AfficherStock.class);
                    i.putExtra("request","productions");
                    startActivity(i);
                }
            });
            //endregion

            //region afficher Maintenance ..
            ((LinearLayout)findViewById(R.id.mantain_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(Accueil.this, AfficherMaintenance.class);
                    Cursor r=Accueil.bd.read("select * from maintenance where date_fin is null order by id desc limit 1");
                    if (r.moveToNext()){
                        i.putExtra("request","second-scan");
                        i.putExtra("id",r.getString(r.getColumnIndex("id")));
                        i.putExtra("type_maint",r.getString(r.getColumnIndex("type_mantain")));
                    }else {
                        i.putExtra("request","first-scan");
                    }
                   startActivity(i);
                }
            });
            //endregion

            //region faire maintenance ..
            ((LinearLayout)findViewById(R.id.add_mantain_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(Accueil.this, AddMaintenance.class);
                    i.putExtra("request","first-scan");
                    startActivity(i);
                }
            });

            //endregion

            //region Afficher preparation ..
            ((LinearLayout)findViewById(R.id.prep_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Accueil.this, AfficherPreparations.class));
                }
            });
            //endregion

            //region faire preparation
            ((LinearLayout)findViewById(R.id.add_prep_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Accueil.this, FairePrepConfig.class));
                }
            });
            //endregion

        }

}
