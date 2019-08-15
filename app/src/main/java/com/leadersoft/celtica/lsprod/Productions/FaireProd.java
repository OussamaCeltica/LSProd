package com.leadersoft.celtica.lsprod.Productions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.Depot;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.Preparations.FairePrep;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FaireProd extends AppCompatActivity {

    TextView codebarPr,depotDest,NumLot;
    CheckBox isPackaging;
    EditText qt;
    String demandeScan="produit",code_depot="";
    boolean prodTerminé=false;
    ProgressDialog progress;
    int code_prod=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_prod);
        if (savedInstanceState != null) {
            //region Revenir a au Au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {

            final String request=getIntent().getExtras().getString("request");

            depotDest=(TextView)findViewById(R.id.faire_prod_depot);
            NumLot=(TextView)findViewById(R.id.faire_prod_lot);
            qt=(EditText)findViewById(R.id.faire_prod_qt);
            isPackaging=(CheckBox)findViewById(R.id.faire_prod_isPackaging);

            //region select produit ..
            final Session.OnScanListener scanListener=new Session.OnScanListener() {
                @Override
                public void OnScan(String code, LinearLayout parent) {
                    checkProduitExiste(code);

                }
            };

            codebarPr = (TextView) findViewById(R.id.faire_prod_codebar);
            DeviceConfig.session.openScannerCodeBarre(this, scanListener);
            codebarPr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeviceConfig.session.openScannerCodeBarre(FaireProd.this, scanListener);
                }
            });
            //endregion

            //region valider production ..
            ((TextView)findViewById(R.id.add_pr_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (request.equals("first-scan")){
                        if(codebarPr.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_valider_pr_vide),Toast.LENGTH_SHORT).show();
                        }else {


                            AlertDialog.Builder mb = new AlertDialog.Builder(FaireProd.this); //c est l activity non le context ..

                            View v= getLayoutInflater().inflate(R.layout.div_type_prod,null);
                            TextView prod=(TextView) v.findViewById(R.id.divTypeProd_prod);
                            TextView prep=(TextView) v.findViewById(R.id.divTypeProd_prep);

                            mb.setView(v);
                            final AlertDialog ad=mb.create();
                            ad.show();
                            ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

                            prep.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    ProductionAdapter.selectedItem=-1;
                                    Intent i=new Intent(FaireProd.this, FairePrep.class);
                                    i.putExtra("request","new_bon");
                                    i.putExtra("codebar_pr",codebarPr.getText().toString()+"");
                                    startActivity(i);

                                }
                            });

                            prod.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ProductionEnCours p=new ProductionEnCours(codebarPr.getText().toString(),DeviceConfig.session.code_chef,DeviceConfig.session.code_ligne);
                                    p.addToBD();
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                                    revenirAfficherProd();
                                }
                            });


                        }
                    }else {
                        if(codebarPr.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_valider_pr_vide),Toast.LENGTH_SHORT).show();
                        }else {


                            if (qt.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_qt_hint), Toast.LENGTH_SHORT).show();
                            } else if (Double.parseDouble(qt.getText().toString()) <= 0) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_valider_qt_infZero), Toast.LENGTH_SHORT).show();
                            } else {
                                if (DeviceConfig.session.isLot) {
                                    if (NumLot.getText().toString().equals("")) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_noLot), Toast.LENGTH_SHORT).show();

                                    } else {
                                        ProductionEnCours prod = new ProductionEnCours(getIntent().getExtras().getString("id_prod"));
                                        prod.terminerProd(Double.parseDouble(qt.getText().toString()), code_depot + "", NumLot.getText().toString() + "");
                                        ((TextView) findViewById(R.id.add_pr_valider)).setVisibility(View.GONE);
                                        prodTerminé = true;

                                        //region connecter SQLSERVER et exporté prod ..
                                        try {
                                            Accueil.BDsql = new SqlServerBD(DeviceConfig.session.serveur.ip, DeviceConfig.session.serveur.port, DeviceConfig.session.serveur.bdName, DeviceConfig.session.serveur.user, DeviceConfig.session.serveur.mdp, "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {

                                                @Override
                                                public void echec() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_conect_err), Toast.LENGTH_SHORT).show();
                                                            revenirAccueil();
                                                        }
                                                    });

                                                }

                                                @Override
                                                public void before() {
                                                    progress = new ProgressDialog(FaireProd.this); // activité non context ..

                                                    progress.setTitle("Connexion");
                                                    progress.setMessage("attendez SVP ...");
                                                    progress.show();
                                                }

                                                @Override
                                                public void After() throws SQLException {

                                                    //region exportation des productions ..

                                                    if (!Synchronisation.isOnExport){
                                                        Synchronisation.isOnExport=true;
                                                        Synchronisation.exportéProd();
                                                    }

                                                    afficherMsgErr();

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
                                        //endregion
                                    }
                                } else {
                                    ProductionEnCours prod = new ProductionEnCours(getIntent().getExtras().getString("id_prod"));
                                    prod.terminerProd(Double.parseDouble(qt.getText().toString()), code_depot + "", "");
                                    ((TextView) findViewById(R.id.add_pr_valider)).setVisibility(View.GONE);

                                    prodTerminé = true;

                                    //region connecter SQLSERVER et exporté prod ..
                                    try {
                                        Accueil.BDsql = new SqlServerBD(DeviceConfig.session.serveur.ip, DeviceConfig.session.serveur.port, DeviceConfig.session.serveur.bdName, DeviceConfig.session.serveur.user, DeviceConfig.session.serveur.mdp, "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {

                                            @Override
                                            public void echec() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_conect_err), Toast.LENGTH_SHORT).show();
                                                        revenirAccueil();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void before() {
                                                progress = new ProgressDialog(FaireProd.this); // activité non context ..

                                                progress.setTitle("Connexion");
                                                progress.setMessage("attendez SVP ...");
                                                progress.show();
                                            }

                                            @Override
                                            public void After() throws SQLException {

                                                if (!Synchronisation.isOnExport){
                                                    Synchronisation.isOnExport=true;
                                                    Synchronisation.exportéProd();
                                                }
                                                afficherMsgErr();

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
                                    //endregion
                                }

                            }
                        }
                    }

                }
            });
            //endregion
        }

    }

    public void afficherMsgErr(){
        //region afficher msg d err
        Accueil.BDsql.es.execute(new Runnable() {
            @Override
            public void run() {
                Accueil.BDsql.es.execute(new Runnable() {
                    @Override
                    public void run() {
                        Synchronisation.isOnExport=false;
                        Log.e("sqll", "ih");
                        if (!Synchronisation.ExportationErr.equals("")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    AlertDialog.Builder mb = new AlertDialog.Builder(FaireProd.this); //c est l activity non le context ..

                                    View v = getLayoutInflater().inflate(R.layout.div_aff_msg_err, null);
                                    TextView msg = (TextView) v.findViewById(R.id.err_msg);
                                    final TextView ok = (TextView) v.findViewById(R.id.ok);

                                    msg.setText("" + Synchronisation.ExportationErr);
                                    Synchronisation.ExportationErr = "";

                                    mb.setView(v);
                                    final AlertDialog ad = mb.create();

                                    try {
                                        ad.show();
                                        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                        ad.setCancelable(false); //désactiver le button de retour ..

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            revenirAccueil();
                                        }
                                    });

                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                                    revenirAccueil();
                                }
                            });
                        }


                    }
                });
            }
        });
        //endregion
    }

    public void checkProduitExiste(String code){

        Produit p=new Produit(code);
        if (p.existe()){
            ((TextView)findViewById(R.id.add_pr_nom)).setText(p.designiation+"");
            codebarPr.setText(code);
            if (getIntent().getExtras().getString("request").equals("second-scan")){

                ProductionEnCours prod=new ProductionEnCours(getIntent().getExtras().getString("id_prod"));
                if(prod.hasProduit(code)){

                    //region afficher la select de dépot
                    depotDest.setVisibility(View.VISIBLE);
                    depotDest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            demandeScan="depot";
                            DeviceConfig.session.openScannerCodeBarre(FaireProd.this, new Session.OnScanListener() {
                                @Override
                                public void OnScan(String code,LinearLayout parent) {
                                    checkDepotExiste(code);

                                }
                            });
                        }
                    });
                    //endregion

                    //region afficher le num de lot ..
                    if (DeviceConfig.session.isLot){
                        NumLot.setVisibility(View.VISIBLE);
                    }
                    //endregion

                    //region afficher checkbox packaging et configurer
                    isPackaging.setVisibility(View.VISIBLE);
                    if (DeviceConfig.session.prodIsPackaging){
                        isPackaging.setChecked(true);
                    }
                    isPackaging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            DeviceConfig.session.changeProdPackgingPermission(isChecked);
                        }
                    });
                    //endregion

                    qt.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_noPrInProd),Toast.LENGTH_SHORT).show();
                    ((TextView)findViewById(R.id.add_pr_nom)).setText("");
                    codebarPr.setText("");
                    depotDest.setVisibility(View.GONE);
                    NumLot.setVisibility(View.GONE);
                    qt.setVisibility(View.GONE);
                    isPackaging.setVisibility(View.GONE);
                }

            }
        }else {
            ((TextView)findViewById(R.id.add_pr_nom)).setText("");
            codebarPr.setText("");
            depotDest.setVisibility(View.GONE);
            NumLot.setVisibility(View.GONE);
            qt.setVisibility(View.GONE);
            isPackaging.setVisibility(View.GONE);

            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_pr_err),Toast.LENGTH_SHORT).show();
        }

    }

    public void checkDepotExiste(String code){
        Depot d=new Depot(code);
        if (d.existe()){
            depotDest.setText(d.nom_dep);
            code_depot=d.code_dep;
        }else {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_noDepot),Toast.LENGTH_SHORT).show();
        }

    }

    public void revenirAccueil(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Accueil.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //endregion
    }

    public void revenirAfficherProd(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Accueil.class);
        Intent i=new Intent(getApplicationContext(),AfficherStock.class);
        i.putExtra("request","productions");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivities(new Intent[]{intent,i});
        //endregion
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK){
            if (!demandeScan.equals("depot")) {
                checkProduitExiste(data.getExtras().getString("code"));
            }else {
                checkDepotExiste(data.getExtras().getString("code"));
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (!prodTerminé)
            super.onBackPressed();
        else{
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_conect_err),Toast.LENGTH_SHORT).show();
            revenirAccueil();
        }

    }
}
