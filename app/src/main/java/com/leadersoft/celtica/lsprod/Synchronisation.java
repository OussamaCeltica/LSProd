package com.leadersoft.celtica.lsprod;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Maintenances.Maintenance;
import com.leadersoft.celtica.lsprod.Preparations.BonPreparation;
import com.leadersoft.celtica.lsprod.Preparations.ProduitPréparé;
import com.leadersoft.celtica.lsprod.Productions.Production;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Synchronisation extends AppCompatActivity {
    EditText ip,port,bd,user,mdp;
    ProgressDialog progess;
    public static int code_prod=0;
    public static int code_prep=0;
    public static int code_prepPr=0;
    private LinearLayout div_admin;

    public String ImportationErr="";
    public static String ExportationErr="";

    public static boolean isOnExport=false;//pour tester si on exportation est en cours ou nn pour eviter le sync en doublant

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronisation);

        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {
            progess=new ProgressDialog(this);

            ExportationErr="";

            //region Conexion d admin ..
            div_admin = (LinearLayout) findViewById(R.id.sync_div_admin);

            final EditText nom_admin,mdp_admin;
            nom_admin = (EditText) findViewById(R.id.sync_admin_nom);
            mdp_admin = (EditText) findViewById(R.id.sync_admin_mdp);
            ((TextView) findViewById(R.id.sync_admin_conn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nom_admin.getText().toString().equals("") || mdp_admin.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.remplissage_err), Toast.LENGTH_SHORT).show();
                    } else {
                        Cursor r = Accueil.bd.read("select * from admin where pseudo='" + nom_admin.getText().toString().replaceAll("'", "") + "'");
                        if (r.moveToNext()) {
                            if (r.getString(r.getColumnIndex("mdp")).equals(mdp_admin.getText().toString().replaceAll("'", ""))) {
                                div_admin.setVisibility(View.GONE);
                                ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.VISIBLE);

                                //region connecter au sql server ..
                                ip=(EditText)findViewById(R.id.synch_ip);
                                port=(EditText)findViewById(R.id.synch_port);
                                user=(EditText)findViewById(R.id.synch__user);
                                bd=(EditText)findViewById(R.id.synch_bd);
                                mdp=(EditText)findViewById(R.id.synch_mdp);

                                Cursor r5= Accueil.bd.read("select * from sqlconnect");
                                while(r5.moveToNext()) {
                                    if(r5.getString(r5.getColumnIndex("ip")) != null) {
                                        ip.setText(r5.getString(r5.getColumnIndex("ip")));
                                        port.setText(r5.getString(r5.getColumnIndex("port")));
                                        user.setText(r5.getString(r5.getColumnIndex("user")));
                                        bd.setText(r5.getString(r5.getColumnIndex("bd_name")));
                                        mdp.setText(r5.getString(r5.getColumnIndex("mdp")));
                                    }
                                }

                                //region afficher le div de connexion SQL
                                try {
                                    Accueil.BDsql = new SqlServerBD(ip.getText().toString(),port.getText().toString(),bd.getText().toString(),user.getText().toString() ,mdp.getText().toString(),"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                                        @Override
                                        public void echec() {
                                            Log.e("connnect", " Echoue");
                                            progess.dismiss();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_err),Toast.LENGTH_SHORT).show();
                                                    ((TextView)findViewById(R.id.synch_Butt)).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            //region afficher le div de connexion SQL
                                                            try {
                                                                Accueil.BDsql = new SqlServerBD(ip.getText().toString(),port.getText().toString(),bd.getText().toString(),user.getText().toString() ,mdp.getText().toString(),"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                                                                    @Override
                                                                    public void echec() {
                                                                        Log.e("connnect", " Echoue");
                                                                        progess.dismiss();
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_err),Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });

                                                                    }

                                                                    @Override
                                                                    public void before() {
                                                                        progess.setTitle(getResources().getString(R.string.sync_conect));
                                                                        progess.setMessage(getResources().getString(R.string.sync_wait));
                                                                        progess.show();


                                                                    }

                                                                    @Override
                                                                    public void After() throws SQLException {
                                                                        Log.e("connnect", " Reussite");
                                                                        progess.dismiss();
                                                                        ((LinearLayout)findViewById(R.id.div_options)).setVisibility(View.VISIBLE);
                                                                        ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.GONE);
                                                                        Accueil.bd.write("update sqlconnect set ip='"+ip.getText()+"',port='"+port.getText()+"',bd_name='"+bd.getText()+"',mdp='"+mdp.getText()+"',user='"+user.getText()+"' where id='1'");

                                                                    }
                                                                });
                                                            } catch (ClassNotFoundException e) {
                                                                e.printStackTrace();
                                                            } catch (IllegalAccessException e) {
                                                                e.printStackTrace();
                                                            } catch (InstantiationException e) {
                                                                e.printStackTrace();
                                                            } catch (SQLException e) {
                                                                e.printStackTrace();
                                                            }
                                                            //endregion
                                                        }
                                                    });

                                                }
                                            });




                                        }

                                        @Override
                                        public void before() {
                                            progess.setTitle(getResources().getString(R.string.sync_conect));
                                            progess.setMessage(getResources().getString(R.string.sync_wait));
                                            progess.show();


                                        }

                                        @Override
                                        public void After() throws SQLException {
                                            Log.e("connnect", " Reussite");
                                            progess.dismiss();
                                            ((LinearLayout)findViewById(R.id.div_options)).setVisibility(View.VISIBLE);
                                            ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.GONE);
                                            Accueil.bd.write("update sqlconnect set ip='"+ip.getText()+"',port='"+port.getText()+"',bd_name='"+bd.getText()+"',mdp='"+mdp.getText()+"',user='"+user.getText()+"' where id='1'");

                                        }
                                    });
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                //endregion

                                //endregion

                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_mdp_err), Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_user_err), Toast.LENGTH_SHORT).show();

                        }

                    }

                }
            });
            //endregion

            //region connecter au sql server ..
            try {
                Accueil.BDsql = new SqlServerBD(DeviceConfig.session.serveur.ip,DeviceConfig.session.serveur.port,DeviceConfig.session.serveur.bdName,DeviceConfig.session.serveur.user,DeviceConfig.session.serveur.mdp,"net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                    @Override
                    public void echec() {
                        Log.e("connnect", " Echoue");
                        progess.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_sql_err),Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void before() {
                        progess.setTitle(getResources().getString(R.string.sync_conect));
                        progess.setMessage(getResources().getString(R.string.sync_wait));
                        progess.show();


                    }

                    @Override
                    public void After() throws SQLException {
                        Log.e("connnect", " Reussite");
                        progess.dismiss();
                        ((LinearLayout)findViewById(R.id.div_options)).setVisibility(View.VISIBLE);
                        ((LinearLayout)findViewById(R.id.DivSqlConnect)).setVisibility(View.GONE);
                       // Accueil.bd.write("update sqlconnect set ip='"+ip.getText()+"',port='"+port.getText()+"',bd_name='"+bd.getText()+"',mdp='"+mdp.getText()+"',user='"+user.getText()+"' where id='1'");

                    }
                });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //endregion

            //region importation ..
            ((TextView)findViewById(R.id.sync_import_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //region progressbar ..
                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progess.setTitle(getResources().getString(R.string.sync_conect));
                                    progess.setMessage(getResources().getString(R.string.sync_wait));
                                    progess.show();
                                }
                            });
                        }
                    });
                    //endregion

                    //region importation des lignes de production ..
                            Accueil.BDsql.read("select Oid,name,BARCODE from ProductionLine ", new SqlServerBD.doAfterBeforeGettingData() {
                                @Override
                                public void echec(final SQLException e) {
                                    ImportationErr=ImportationErr+"Erreur de récupération des Lignes de production: \n"+e.getMessage()+" \n";
                                }

                                @Override
                                public void before() {

                                }

                                @Override
                                public void After()   {
                                    boolean SqliteErr=false;
                                    final ResultSet r = Accueil.BDsql.r;
                                    Accueil.bd.write("delete from ligne_production where '1'='1' ");
                                    try {
                                        while (r.next()) {
                                            Log.e("lll", r.getString("name"));
                                            try {
                                                Accueil.bd.write2("insert into ligne_production (Oid,code_ligne,designiation) values('" + r.getString("Oid") + "',?,'')", new MyBD.SqlPrepState() {
                                                    @Override
                                                    public void putValue(SQLiteStatement stmt) {
                                                        try {
                                                            stmt.bindString(1, r.getString("name"));
                                                            stmt.execute();
                                                        } catch (SQLException e) {
                                                            e.printStackTrace();

                                                        }
                                                    }
                                                });
                                            } catch (android.database.SQLException e) {
                                                if (!SqliteErr) {
                                                    SqliteErr = true;
                                                    ImportationErr = ImportationErr + "-Erreur d'insertion des Lignes de production: \n" + e.getMessage() + "\n";

                                                } else {
                                                    ImportationErr = ImportationErr + e.getMessage() + " \n";
                                                }

                                            }

                                        }
                                    }catch (SQLException e){

                                    }

                                }
                            });

                        //endregion

                    //region importation des chefs de ligne ..

                            Accueil.BDsql.read("select emp.CODEBARRE,FirstName,LastName, matricule_employé from ProductionLine pl  inner join Person on pl.productionLineLeader=Person.Oid inner join Employé emp  on pl.productionLineLeader=emp.Oid where emp.matricule_employé is not null and pl.GCRecord is null  ", new SqlServerBD.doAfterBeforeGettingData() {
                                @Override
                                public void echec(final SQLException e) {
                                    ImportationErr=ImportationErr+"Erreur de récupération des chefs des lignes: \n"+e.getMessage()+" \n";

                                }

                                @Override
                                public void before() {

                                }

                                @Override
                                public void After()  {

                                    boolean SqliteErr=false;
                                    ResultSet r = Accueil.BDsql.r;
                                    Accueil.bd.write("delete from chef_ligne where '1'='1' ");
                                    try {
                                        while (r.next()) {
                                            Log.e("empp", r.getString("matricule_employé") + r.getString("FirstName"));
                                            try {
                                                Accueil.bd.write("insert into chef_ligne (code_chef,nom,codebar) values('" + r.getString("matricule_employé") + "','" + r.getString("LastName").replaceAll("'", "") + " " + r.getString("FirstName").replaceAll("'", "") + "','" + r.getString("CODEBARRE") + "')");

                                            } catch (android.database.SQLException e) {
                                                if (!SqliteErr) {
                                                    SqliteErr = true;
                                                    ImportationErr = ImportationErr + "-Erreur d'insertion des chefs des Lignes: \n" + e.getMessage() + "\n";

                                                } else {
                                                    ImportationErr = ImportationErr + e.getMessage() + " \n";
                                                }
                                            }
                                        }
                                    }catch(SQLException e2){
                                        ImportationErr = ImportationErr + "-Erreur de syntaxe , table chefs des Lignes: \n" + e2.getMessage() + "\n";

                                    }
                                }
                            });

                        //endregion

                    //region importation des produit ..
                            Accueil.BDsql.read("select name,EAN13Code as codebar from Product where EAN13Code is not null and EAN13Code !='' and GCRecord is null ", new SqlServerBD.doAfterBeforeGettingData() {
                                @Override
                                public void echec(final SQLException e) {
                                    ImportationErr=ImportationErr+"Erreur de récupération des produits: \n"+e.getMessage()+" \n";
                                }

                                @Override
                                public void before() {
                                    progess.setTitle(getResources().getString(R.string.sync_titre));
                                    progess.setMessage(getResources().getString(R.string.sync_wait));
                                    progess.show();
                                }

                                @Override
                                public void After() {
                                    boolean SqliteErr=false;
                                    ResultSet r = Accueil.BDsql.r;
                                    Accueil.bd.write("delete from produit where '1'='1'");
                                    try {
                                        while (r.next()) {
                                            Log.e("prr", r.getString("codebar") + " / " + r.getString("name"));
                                            try {
                                                Accueil.bd.write("insert into produit(codebar,nom_pr) values('" + r.getString("codebar") + "','" + r.getString("name").replaceAll("'", "`") + "')");
                                            } catch (android.database.SQLException e) {
                                                if (!SqliteErr) {
                                                    SqliteErr = true;
                                                    ImportationErr = ImportationErr + "-Erreur d'insertion des produits: \n code produit:" + r.getString("codebar") + " \n " + e.getMessage() + "\n";

                                                } else {
                                                    ImportationErr = ImportationErr + "code produit:" + r.getString("codebar") + "\n" + e.getMessage() + " \n";
                                                }
                                            }
                                        }
                                    }catch (SQLException e){

                                    }
                                }
                            });

                        //endregion

                    //region importation des motive de maintenance ..
                        Accueil.BDsql.read("select * from MaintenanceMotive where GCRecord is null", new SqlServerBD.doAfterBeforeGettingData() {
                            @Override
                            public void echec(SQLException e) {
                                ImportationErr=ImportationErr+"Erreur d importation de Motive de maintenance: "+e.getMessage()+"\n";
                            }

                            @Override
                            public void before() {

                            }

                            @Override
                            public void After()  {
                                final ResultSet r=Accueil.BDsql.r;
                                Accueil.bd.write("delete from type_maintenance");
                                try {
                                    while (r.next()){
                                        Log.e("mmm",r.getString("designation"));
                                        Accueil.bd.write2("insert into type_maintenance (Oid,type) values('"+r.getString("Oid")+"',?)", new MyBD.SqlPrepState() {
                                            @Override
                                            public void putValue(SQLiteStatement stmt) {
                                                try {
                                                    stmt.bindString(1,r.getString("designation"));
                                                    stmt.execute();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }

                                }catch (SQLException e){

                                }
                            }
                        });
                    //endregion

                    //region importation des employé ..
                    Accueil.BDsql.read("select CODEBARRE,matricule_employé, e.Oid, e.maintenanceAgent, p.FirstName, p.LastName\n" +
                            "\n" +
                            "from employé e inner join person p on e.Oid = p.Oid where e.maintenanceAgent='1' ", new SqlServerBD.doAfterBeforeGettingData() {
                        @Override
                        public void echec(SQLException e) {
                            ImportationErr=ImportationErr+"-Erreur d importation des employé: \n"+e.getMessage();
                        }

                        @Override
                        public void before() {

                        }

                        @Override
                        public void After() {
                            final ResultSet r=Accueil.BDsql.r;
                            Accueil.bd.write("delete from employe");
                            try {
                                while (r.next()){
                                    Log.e("emm",""+r.getString("matricule_employé"));
                                    Accueil.bd.write2("insert into employe (code_emp,nom_emp,codebar,Oid) values('"+r.getString("matricule_employé")+"',?,'"+r.getString("CODEBARRE")+"','"+r.getString("Oid")+"')", new MyBD.SqlPrepState() {
                                        @Override
                                        public void putValue(SQLiteStatement stmt) {
                                            try{
                                                stmt.bindString(1,r.getString("FirstName")+" "+r.getString("LastName"));
                                                stmt.execute();
                                            }catch (android.database.SQLException e){
                                                ImportationErr=ImportationErr+"-Erreur d insertion des employé: \n"+e.getMessage();

                                            } catch (SQLException e) {
                                                ImportationErr=ImportationErr+"-Erreur d importation des employés: \n"+e.getMessage();

                                            }
                                        }
                                    });
                                }
                            }catch (SQLException e){

                            }


                        }
                    });
                    //endregion

                    //region importation des depot ..
                    Accueil.BDsql.read("SELECT  Oid , name , barCode  FROM  Warehouse  where GCRecord is null and isActif =1 and barCode is not null and barCode !=''  ", new SqlServerBD.doAfterBeforeGettingData() {
                        @Override
                        public void echec(SQLException e) {
                            ImportationErr=ImportationErr+"-Erreur d importation des dépôts: \n"+e.getMessage();
                        }

                        @Override
                        public void before() {

                        }

                        @Override
                        public void After() {
                            final ResultSet r=Accueil.BDsql.r;
                            Accueil.bd.write("delete from depot");
                            try {
                                while (r.next()){
                                    Log.e("depp",""+r.getString("barcode"));
                                    Accueil.bd.write2("insert into depot (codebar,nom_dep) values('"+r.getString("barCode")+"',?)", new MyBD.SqlPrepState() {
                                        @Override
                                        public void putValue(SQLiteStatement stmt) {
                                            try{
                                                stmt.bindString(1,r.getString("name")+"");
                                                stmt.execute();
                                            }catch (android.database.SQLException e){
                                                ImportationErr=ImportationErr+"-Erreur d insertion des dépôts: \n"+e.getMessage();

                                            } catch (SQLException e) {
                                                ImportationErr=ImportationErr+"-Erreur d importation des dépôts: \n"+e.getMessage();
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }catch (SQLException e){

                            }


                        }
                    });
                    //endregion

                    //region importation default depot ..
                    Accueil.BDsql.read("select w.oid, w.barCode, w.name\n" +
                            "from Warehouse w inner join parameters p on w.oid = p.defaultManifacturingWarehouse\n" +
                            "where w.GCRecord is null and p.GCRecord is null and w.isActif = 1", new SqlServerBD.doAfterBeforeGettingData() {
                        @Override
                        public void echec(SQLException e) {
                            ImportationErr=ImportationErr+"-Erreur d importation de  dépôt par default: \n"+e.getMessage();
                        }

                        @Override
                        public void before() {

                        }

                        @Override
                        public void After() {
                            final ResultSet r=Accueil.BDsql.r;

                            try {
                                if (r.next()){
                                    Log.e("depp","depot default : "+r.getString("barcode"));
                                    Accueil.bd.write("update admin set defaultDepot='"+r.getString("barCode")+"'");
                                }
                            }catch (SQLException e){

                            }


                        }
                    });
                    //endregion

                    afficherMsgErr();

                }
            });

            //endregion

            //region exportation ..
            ((TextView)findViewById(R.id.sync_export_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                if (!isOnExport) {

                    isOnExport=true;

                    //region progressbar ..
                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progess.setTitle(getResources().getString(R.string.sync_conect));
                                    progess.setMessage(getResources().getString(R.string.sync_wait));
                                    progess.show();
                                }
                            });
                        }
                    });
                    //endregion

                    exportéProd();

                    exportéMaintenance();

                    afficherMsgErr();

                    //region supprimé l archive ..
                    Accueil.bd.write("delete  from production where julianday('now') - julianday('date_fin') > 60 and sync='1'  ");
                    Accueil.bd.write("delete  from maintenance where julianday('now') - julianday('date_fin') > 60 and sync='1'  ");
                    Accueil.bd.write("delete  from bon_preparation where julianday('now') - julianday('date_bon') > 60 and sync='1'  ");
                    //endregion
                }

                }
            });
            //endregion

        }
    }

    //region testé si on a pas de production n est pa synchroniser ..
    private boolean testIfProdonSyncExiste(){

        if(Accueil.bd.read("select * from production desc limit 1 ").moveToNext()){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_BonExiste),Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;

    }
    //endregion

    public static void exportéProd(){

        //region exportation des productions ..
        Accueil.BDsql.read("select top 1 RECORDID from BonProductionMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(final SQLException e) {
                ExportationErr="Erreur de Récupération de RECORDID de BonProductionMobile \n ";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r = Accueil.BDsql.r;
                try {
                    if (r.next()) {
                        code_prod = Integer.parseInt(r.getString("RECORDID")) + 1;
                    }
                    Log.e("recc", "" + code_prod);
                }catch (SQLException e){

                }

            }
        });

        Accueil.BDsql.read("select top 1 RECORDID from bonProductionMobileItem order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(final SQLException e) {
                ExportationErr="Erreur de Récupération de RECORDID de bonProductionMobileItem \n ";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r = Accueil.BDsql.r;
                try {
                    if (r.next()) {
                        code_prepPr = Integer.parseInt(r.getString("RECORDID")) + 1;
                    }
                    Log.e("reccPr", "" + code_prep);
                }catch (SQLException e){

                }

            }
        });

        //region envoyé les données aux tables intermédiaire ..
        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                final Cursor r2 = Accueil.bd.read("select p.*,cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_debut)) as double) / (3600 * 24) as date_deb" +
                        " , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_fin)) as double) / (3600 * 24) as date_f" +
                        " from production p where sync='0' and date_fin is not null");

                while (r2.moveToNext()) {
                    Accueil.BDsql.beginTRansact();
                    final Production p2=new Production(r2.getString(r2.getColumnIndex("id_prod")),r2.getString(r2.getColumnIndex("code_pr")),r2.getString(r2.getColumnIndex("code_chef")),r2.getString(r2.getColumnIndex("code_ligne")),r2.getDouble(r2.getColumnIndex("date_deb"))+"",r2.getDouble(r2.getColumnIndex("date_f"))+"",r2.getDouble(r2.getColumnIndex("quantité")));
                    p2.exportéProd(code_prod);

                    ArrayList<ProduitPréparé> préparés=p2.getProduitPreparer();
                    for (ProduitPréparé p:préparés) {
                        p.exportéProduit(code_prepPr+"",p2.id_prod);
                        code_prepPr++;
                    }

                    Accueil.BDsql.commitTRansact();

                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (Accueil.BDsql.transactErr == false){
                                p2.changeState("exporté");
                            }
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

                    code_prod++;
                }
                code_prod=0;
                code_prepPr=0;
            }
        });
        //endregion
        //endregion
    }

    public static void exportéMaintenance(){

        //region exportation des maintenance ..
        Cursor r=Accueil.bd.read("select m.*,cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_debut)) as double) / (3600 * 24) as date_deb," +
                " cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_fin)) as double) / (3600 * 24) as date_f from maintenance m where sync='0' and date_fin is not null");
        while (r.moveToNext()){

            Maintenance maint=new Maintenance(r.getString(r.getColumnIndex("id")),r.getDouble(r.getColumnIndex("date_deb"))+"",r.getDouble(r.getColumnIndex("date_f"))+"",r.getString(r.getColumnIndex("code_ligne")),r.getString(r.getColumnIndex("code_emp")),r.getString(r.getColumnIndex("type_mantain")));
            maint.exportéMaintenance();


                        /*
                        maintenances.add(r.getString(r.getColumnIndex("id")));
                        errSql=false;
                        HashMap<Integer,String> datas=new HashMap<Integer,String> ();
                        datas.put(1,r.getString(r.getColumnIndex("code_ligne")));
                            Accueil.BDsql.write2("insert into Maintenance (Oid,productionLine,employee,start,[end],motive) values(NEWID(),?,'"+r.getString(r.getColumnIndex("code_emp"))+"',CAST("+r.getDouble(r.getColumnIndex("date_deb"))+" as datetime),CAST("+r.getDouble(r.getColumnIndex("date_f"))+" as datetime),'"+r.getString(r.getColumnIndex("oid_type_maint"))+"')",datas, new SqlServerBD.doAfterBeforeGettingData() {
                                @Override
                                public void echec(SQLException e) {
                                    if (!errSql){
                                        errSql=true;
                                        ExportationErr =ExportationErr+ "Erreur d exportation  de Maintenance: \n "+e.getMessage();
                                    }else{
                                        ExportationErr=ExportationErr+e.getMessage()+" \n";
                                    }
                                }

                                @Override
                                public void before() {

                                }

                                @Override
                                public void After()  {
                                    Maintenance m=new Maintenance(maintenances.get(indexMaint));
                                    m.changeState("exporté");
                                    indexMaint++;

                                }
                            });

*/

        }

        //endregion

    }

    public static void exportéPréparation(){

        //region exportation des productions ..
        Accueil.BDsql.read("select top 1 RECORDID from BonProductionMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(final SQLException e) {
                ExportationErr="Erreur de Récupération de RECORDID de BonProductionMobile \n ";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r = Accueil.BDsql.r;
                try {
                    if (r.next()) {
                        code_prep = Integer.parseInt(r.getString("RECORDID")) + 1;
                    }
                    Log.e("recc", "" + code_prep);
                }catch (SQLException e){

                }

            }
        });

        Accueil.BDsql.read("select top 1 RECORDID from bonProductionMobileItem order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(final SQLException e) {
                ExportationErr="Erreur de Récupération de RECORDID de bonProductionMobileItem \n ";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {
                ResultSet r = Accueil.BDsql.r;
                try {
                    if (r.next()) {
                        code_prepPr = Integer.parseInt(r.getString("RECORDID")) + 1;
                    }
                    Log.e("reccPr", "" + code_prep);
                }catch (SQLException e){

                }

            }
        });

        //region envoyé les données aux tables intermédiaire ..
        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                final Cursor r= Accueil.bd.read("select p.*,cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_bon)) as double) / (3600 * 24) as date_b" +
                        " from bon_preparation p where sync='0' and etat='validé'");

                while (r.moveToNext()) {
                    Accueil.BDsql.beginTRansact();
                    final BonPreparation bon=new BonPreparation(r.getString(r.getColumnIndex("id_bon")),r.getString(r.getColumnIndex("code_pr"))+"",r.getString(r.getColumnIndex("nom_pr"))+"",r.getString(r.getColumnIndex("code_ligne"))+"",r.getString(r.getColumnIndex("nom_ligne"))+"",r.getString(r.getColumnIndex("date_bon"))+"", ETAT.EN_COURS);
                    bon.exportéBon();

                    //region exporté les arrets productions
                    ArrayList<ProduitPréparé> préparés=bon.getProduitPreparer();
                    for (ProduitPréparé p:préparés) {
                        p.exportéProduit(code_prepPr+"",bon.id_bon);
                        code_prepPr++;
                    }
                   //endregion

                    Accueil.BDsql.commitTRansact();

                    Accueil.BDsql.es.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!Accueil.BDsql.transactErr){
                                bon.changeState("exporté");
                            }
                        }
                    });
                    code_prep++;
                }
                code_prep=0;
                code_prepPr=0;
            }
        });
        //endregion
        //endregion
    }

    public void afficherMsgErr(){
        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Accueil.BDsql.es.execute(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isOnExport=false;
                                Log.e("errr"," MSG :"+ExportationErr);
                                progess.dismiss();
                                if(!ImportationErr.equals("") || !ExportationErr.equals("")){
                                    AlertDialog.Builder mb = new AlertDialog.Builder(Synchronisation.this); //c est l activity non le context ..

                                    View v = getLayoutInflater().inflate(R.layout.div_aff_msg_err, null);
                                    TextView msg = (TextView) v.findViewById(R.id.err_msg);
                                    Button ok = (Button) v.findViewById(R.id.ok);

                                    mb.setView(v);
                                    final AlertDialog add = mb.create();

                                    try {
                                        add.show();
                                        add.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                        add.setCancelable(false); //désactiver le button de retour ..
                                    }catch (Exception e){

                                    }


                                    msg.setText(ImportationErr+" \n "+ExportationErr);

                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            add.dismiss();
                                        }
                                    });
                                    ImportationErr="";
                                    ExportationErr="";

                                }else {
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_succ),Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                });
            }
        });
    }
}
