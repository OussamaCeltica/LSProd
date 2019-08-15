package com.leadersoft.celtica.lsprod.Maintenances;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem3;
import com.leadersoft.celtica.lsprod.Productions.AddStock;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;

import java.sql.SQLException;
import java.util.ArrayList;

public class AddMaintenance extends AppCompatActivity {

    MySpinnerSearchable code_employes,type_maints,spinnerLigne;
    TextView code_emp,type_maint,code_ligne;
    String id_maint="",REQUEST_SCANNER="";
    SpinnerItem3 itemLigne=null,item_Emp=null,itemMotive=null;
    boolean firstScan=true,maitenanceTerminé=false;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);
        if (savedInstanceState != null) {
            //region Revenir a au Deviceconfig ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {
            Synchronisation.ExportationErr.equals("");
            code_emp=(TextView)findViewById(R.id.add_mantain_emp);
            type_maint=(TextView)findViewById(R.id.add_mantain_type);
            code_ligne=(TextView)findViewById(R.id.add_mantain_ligne);


            //region request from Accueil ..
            if(getIntent().getExtras().getString("request").equals("first-scan")){

                //region select une ligne ..
                final ArrayList<SpinnerItem> codeLigne=new ArrayList<SpinnerItem>();
                Cursor r2= Accueil.bd.read("select * from ligne_production");
                while (r2.moveToNext()){
                    codeLigne.add(new SpinnerItem3(r2.getString(r2.getColumnIndex("code_ligne")),r2.getString(r2.getColumnIndex("designiation")),r2.getString(r2.getColumnIndex("Oid"))));
                }
                spinnerLigne=new MySpinnerSearchable(AddMaintenance.this, codeLigne, "Séléctionner un employé ..", new MySpinnerSearchable.SpinnerConfig() {
                    @Override
                    public void onChooseItem(int pos, SpinnerItem item) {
                        code_ligne.setText(item.key);
                        itemLigne = ((SpinnerItem3)item);
                        spinnerLigne.closeSpinner();
                    }
                }, new MySpinnerSearchable.ButtonSpinnerOnClick() {
                    @Override
                    public void onClick() {
                        REQUEST_SCANNER="ligne";
                        DeviceConfig.session.openScannerCodeBarre(AddMaintenance.this, new Session.OnScanListener() {
                            @Override
                            public void OnScan(String code, LinearLayout root) {
                                TestLigneExiste(code);

                            }
                        });
                    }
                });

                code_ligne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        spinnerLigne.openSpinner();
                    }
                });
                //endregion

                //region testé si en cours ou nouvelle maintenance ..
                ((TextView)findViewById(R.id.add_maint_ligne_valid)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(itemLigne != null){
                            ((TextView)findViewById(R.id.add_mantain_valider)).setVisibility(View.VISIBLE);
                            ((TextView)findViewById(R.id.add_maint_ligne_valid)).setVisibility(View.GONE);
                            code_ligne.setVisibility(View.GONE);
                            final Cursor r2=Accueil.bd.read("select * from maintenance where code_ligne='"+itemLigne.key2+"' and date_fin is null");
                            if (r2.moveToNext()){
                                id_maint=r2.getString(r2.getColumnIndex("id"));
                                firstScan=false;
                                code_emp.setVisibility(View.GONE);
                                type_maint.setVisibility(View.GONE);
                                code_ligne.setVisibility(View.GONE);
                                ((TextView)findViewById(R.id.maint_label)).setVisibility(View.VISIBLE);
                                ((TextView)findViewById(R.id.maint_nomType)).setVisibility(View.VISIBLE);
                                ((TextView)findViewById(R.id.maint_nomType)).setText(""+r2.getString(r2.getColumnIndex("type_mantain")));
                                ((TextView)findViewById(R.id.add_mantain_valider)).setText("Terminer");

                            }else {

                                //region select un employé ..
                                code_emp.setVisibility(View.VISIBLE);
                                final ArrayList<SpinnerItem> codeEmp=new ArrayList<SpinnerItem>();
                                Cursor r=Accueil.bd.read("select * from employe");
                                while (r.moveToNext()){
                                    codeEmp.add(new SpinnerItem3(r.getString(r.getColumnIndex("code_emp")),r.getString(r.getColumnIndex("nom_emp")),r.getString(r.getColumnIndex("Oid"))));
                                }
                                code_employes=new MySpinnerSearchable(AddMaintenance.this, codeEmp, "Séléctionner un employé ..", new MySpinnerSearchable.SpinnerConfig() {
                                    @Override
                                    public void onChooseItem(int pos, SpinnerItem item) {
                                        code_emp.setText(item.value);
                                        item_Emp=((SpinnerItem3)item);
                                        code_employes.closeSpinner();
                                    }
                                }, new MySpinnerSearchable.ButtonSpinnerOnClick() {
                                    @Override
                                    public void onClick() {
                                        REQUEST_SCANNER="emp";
                                        DeviceConfig.session.openScannerCodeBarre(AddMaintenance.this, new Session.OnScanListener() {
                                            @Override
                                            public void OnScan(String code,LinearLayout root) {
                                                TestEmployéExiste(code);

                                            }
                                        });
                                    }
                                });

                                code_emp.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        code_employes.openSpinner();
                                    }
                                });
                                //endregion

                                //region select un type ..
                                type_maint.setVisibility(View.VISIBLE);
                                final ArrayList<SpinnerItem> type=new ArrayList<SpinnerItem>();
                                r=Accueil.bd.read("select * from type_maintenance");
                                while (r.moveToNext()){
                                    type.add(new SpinnerItem3("",r.getString(r.getColumnIndex("type")),r.getString(r.getColumnIndex("Oid"))));
                                }
                                type_maints=new MySpinnerSearchable(AddMaintenance.this, type,"Rechercher un type ..",new MySpinnerSearchable.SpinnerConfig() {
                                    @Override
                                    public void onChooseItem(int pos, SpinnerItem item) {
                                        type_maint.setText(item.value);
                                        itemMotive=((SpinnerItem3)item);
                                        type_maints.closeSpinner();
                                    }
                                });

                                type_maint.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        type_maints.openSpinner();
                                    }
                                });
                                //endregion
                            }
                        }
                    }
                });
                //endregion

            }
            //endregion

            //region request from MaintenanceAdapter pour second_scan ..
            else {

                firstScan = false;
                id_maint=getIntent().getExtras().getString("id");
                secondScan(getIntent().getExtras().getString("type_maint"));
            }
            //endregion

            //region valider maintenance ..
            ((TextView)findViewById(R.id.add_mantain_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(firstScan) {
                        if (code_emp.getText().toString().equals("") || type_maint.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "Séléctionner un employé et type de maintenance ", Toast.LENGTH_SHORT).show();
                        } else {
                            Maintenance m = new Maintenance(itemLigne.key2, item_Emp.key2, itemMotive.key2);
                            m.addToBD("first-scan");
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();

                            revenirAfficherManit();
                        }
                    }else {
                        //Log.e("ddd", r2.getString(r2.getColumnIndex("id"))+"");
                        Maintenance m=new Maintenance(id_maint);
                        m.addToBD("second-scan");
                        maitenanceTerminé=true;

                        try {
                            Accueil.BDsql=new SqlServerBD(DeviceConfig.session.serveur.ip, DeviceConfig.session.serveur.port, DeviceConfig.session.serveur.bdName, DeviceConfig.session.serveur.user, DeviceConfig.session.serveur.mdp, "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                                @Override
                                public void echec() {
                                    progress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_maint_cnct_err),Toast.LENGTH_SHORT).show();
                                            revenirAuAccueil();
                                        }
                                    });

                                }

                                @Override
                                public void before() {
                                    progress = new ProgressDialog(AddMaintenance.this); // activité non context ..

                                    progress.setTitle("Connexion");
                                    progress.setMessage("attendez SVP ...");
                                    progress.show();
                                }

                                @Override
                                public void After() throws SQLException {

                                    if (!Synchronisation.isOnExport) {
                                        Synchronisation.isOnExport=true;
                                        Synchronisation.exportéMaintenance();
                                    }

                                    //region afficher msg d erreur ..
                                    Accueil.BDsql.es.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Accueil.BDsql.es.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Synchronisation.isOnExport=false;
                                                    if(!Synchronisation.ExportationErr.equals("")){
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                progress.dismiss();
                                                                AlertDialog.Builder mb = new AlertDialog.Builder(AddMaintenance.this); //c est l activity non le context ..

                                                                View v= getLayoutInflater().inflate(R.layout.div_aff_msg_err,null);
                                                                TextView msg=(TextView) v.findViewById(R.id.err_msg);
                                                                final TextView ok=(TextView) v.findViewById(R.id.ok);

                                                                msg.setText(""+Synchronisation.ExportationErr);
                                                                Synchronisation.ExportationErr="";

                                                                mb.setView(v);
                                                                final AlertDialog ad=mb.create();
                                                                ad.show();
                                                                ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                                                ad.setCancelable(false); //désactiver le button de retour ..

                                                                ok.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        revenirAuAccueil();
                                                                    }
                                                                });

                                                            }
                                                        });
                                                    }else {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                                                                revenirAuAccueil();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    //endregion
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
                        //revenirAuAccueil();
                    }
                }
            });
            //endregion

        }
    }

    private void secondScan(String typeMaint ){
        ((TextView)findViewById(R.id.add_mantain_valider)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.add_maint_ligne_valid)).setVisibility(View.GONE);
        code_emp.setVisibility(View.GONE);
        type_maint.setVisibility(View.GONE);
        code_ligne.setVisibility(View.GONE);
        ((TextView)findViewById(R.id.maint_label)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.maint_nomType)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.maint_nomType)).setText(""+typeMaint);
        ((TextView)findViewById(R.id.add_mantain_valider)).setText("Terminer");

    }

    private void TestEmployéExiste(String code){
        Cursor r=Accueil.bd.read("select * from employe where codebar='"+code+"'");
        if (r.moveToNext()){
            code_emp.setText(r.getString(r.getColumnIndex("nom_emp")));
            item_Emp=new SpinnerItem3(r.getString(r.getColumnIndex("code_emp")),r.getString(r.getColumnIndex("nom_emp")),r.getString(r.getColumnIndex("Oid")));
            code_employes.closeSpinner();
        }else {
            Toast.makeText(getApplicationContext(),"Ce code n'existe pas !",Toast.LENGTH_SHORT).show();
        }

    }

    private void TestLigneExiste(String code){
        Cursor r=Accueil.bd.read("select * from ligne_production where codebar='"+code+"'");
        if (r.moveToNext()){
            code_ligne.setText(r.getString(r.getColumnIndex("designiation")));
            itemLigne=new SpinnerItem3(r.getString(r.getColumnIndex("code_ligne")),r.getString(r.getColumnIndex("designiation")),r.getString(r.getColumnIndex("Oid")));
            spinnerLigne.closeSpinner();
        }else {
            Toast.makeText(getApplicationContext(),"Ce code n'existe pas !",Toast.LENGTH_SHORT).show();
        }

    }

    public void revenirAuAccueil(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Accueil.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //endregion
    }

    public void revenirAfficherManit(){
        Intent intent = new Intent(getApplicationContext(), Accueil.class);
        Intent i = new Intent(getApplicationContext(), AfficherMaintenance.class);
        i.putExtra("request","first-scan");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivities(new Intent[]{intent,i});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            String code=data.getExtras().getString("code");
            if(REQUEST_SCANNER.equals("emp")) {
                TestEmployéExiste(code);
            }else {
                TestLigneExiste(code);
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (!maitenanceTerminé)
            super.onBackPressed();
        else{
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_conect_err),Toast.LENGTH_SHORT).show();
            revenirAuAccueil();
        }

    }
}
