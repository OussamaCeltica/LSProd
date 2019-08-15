package com.leadersoft.celtica.lsprod.Productions;

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
import com.leadersoft.celtica.lsprod.Preparations.FairePrepConfig;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;

import java.util.ArrayList;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class FaireProdConfig extends AppCompatActivity {
    TextView code_chef,code_ligne;
    private int demandeScann=1;
    boolean ligneSelected=false;
    private int backPressedNbr=0;

    MySpinnerSearchable spinnerChefs,spinnerLigne;
    //SpinnerDialog spinnerDialog;
    //ArrayList<String> codes_chef_items=new ArrayList<String>();

    SpinnerDialog spinnerDialog2;
    ArrayList<String> codes_ligne_items=new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_prod_config);

        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            //region configuration de code chef de ligne ..
            ArrayList<SpinnerItem> codes_chef_items= new ArrayList<SpinnerItem>();
            Cursor r1 = Accueil.bd.read("select * from chef_ligne");
            while (r1.moveToNext()) {
                codes_chef_items.add(new SpinnerItem(r1.getString(r1.getColumnIndex("code_chef")),r1.getString(r1.getColumnIndex("nom"))));
            }

            spinnerChefs=new MySpinnerSearchable(this, codes_chef_items, getResources().getString(R.string.code_chef_search_hint), new MySpinnerSearchable.SpinnerConfig() {
                @Override
                public void onChooseItem(int pos, SpinnerItem item) {
                    //pos not fonctional yet ..
                    code_chef.setText(item.key);
                    spinnerChefs.closeSpinner();

                }
            }, new MySpinnerSearchable.ButtonSpinnerOnClick() {
                @Override
                public void onClick() {
                    demandeScann=1;
                    DeviceConfig.session.openScannerCodeBarre(FaireProdConfig.this, new Session.OnScanListener() {
                        @Override
                        public void OnScan(String code, LinearLayout root) {
                            TestChefExiste(code);
                        }
                    });
                }
            });

            code_chef=(TextView)findViewById(R.id.prod_config_code_chef);
            code_chef.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spinnerChefs.openSpinner();
                }
            });
            //endregion

            //region configuration de code de ligne ..
            ArrayList<SpinnerItem> codes_ligneProd= new ArrayList<SpinnerItem>();
            r1 = Accueil.bd.read("select * from ligne_production");
            while (r1.moveToNext()) {
                codes_ligneProd.add(new SpinnerItem(r1.getString(r1.getColumnIndex("code_ligne")),r1.getString(r1.getColumnIndex("designiation"))));
            }

            spinnerLigne=new MySpinnerSearchable(this, codes_ligneProd, getResources().getString(R.string.add_pr_selectLigne), new MySpinnerSearchable.SpinnerConfig() {
                @Override
                public void onChooseItem(int pos, SpinnerItem item) {
                    //pos not fonctional yet ..
                    code_ligne.setText(item.key);
                    spinnerLigne.closeSpinner();

                }
            }, new MySpinnerSearchable.ButtonSpinnerOnClick() {
                @Override
                public void onClick() {
                    demandeScann=2;
                    DeviceConfig.session.openScannerCodeBarre(FaireProdConfig.this, new Session.OnScanListener() {
                        @Override
                        public void OnScan(String code,LinearLayout root) {
                            TestLigneExiste(code);
                        }
                    });
                }
            });

            code_ligne = (TextView)findViewById(R.id.prod_config_code_ligne);
            code_ligne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spinnerLigne.openSpinner();
                    Log.e("ddd"," ok ");
                }
            });

            //endregion

            //region valider ..
            ((TextView) findViewById(R.id.prod_config_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!ligneSelected){
                        if(code_ligne.getText().toString().equals("")){
                            //  Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_noUser),Toast.LENGTH_SHORT).show();
                        }else {
                            backPressedNbr=0;
                            ligneSelected=true;
                            code_ligne.setVisibility(View.GONE);
                            Intent i = new Intent(FaireProdConfig.this, FaireProdMode.class);
                            Cursor r = Accueil.bd.read("select pd.*, pr.nom_pr from production pd , produit pr where pd.code_pr=pr.codebar and  date_fin is null and code_ligne='"+code_ligne.getText().toString()+"' order by id_prod desc limit 1");
                            if (r.moveToNext()) {
                                i.putExtra("request", "second-scan");
                                i.putExtra("produit", r.getString(r.getColumnIndex("nom_pr")));
                                i.putExtra("id_prod", r.getString(r.getColumnIndex("id_prod")));
                                if (r.getString(r.getColumnIndex("isPrep")).equals("1")){
                                    ProductionEnCours p=new ProductionEnCours(r.getString(r.getColumnIndex("id_prod")));
                                    p.isPrep=true;
                                    ProductionAdapter.Productions.add(p);
                                    ProductionAdapter.selectedItem=0;

                                }else {
                                    ProductionAdapter.selectedItem=-1;
                                }
                                startActivity(i);
                            } else {
                                code_chef.setVisibility(View.VISIBLE);
                            }

                        }


                    }else {
                        if(code_chef.getText().toString().equals("")){

                        }else {

                            Intent i = new Intent(FaireProdConfig.this, FaireProdMode.class);
                            DeviceConfig.session = new Session(code_chef.getText().toString(), code_ligne.getText().toString(),"");
                            i.putExtra("request", "first-scan");
                            startActivity(i);
                        }
                    }


                }
            });
            //endregion

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            String code=data.getExtras().getString("code");
            if (demandeScann==1){
                TestChefExiste(code);
            }else {
                TestLigneExiste(code);
            }


        }

    }

    private void TestLigneExiste(String code){
        Cursor r=Accueil.bd.read("select * from ligne_production where codebar='"+code+"'");
        if (r.moveToNext()){
            code_ligne.setText(r.getString(r.getColumnIndex("code_ligne")));
            spinnerLigne.closeSpinner();
        }else {
            Toast.makeText(getApplicationContext(),"Ce code n'existe pas !",Toast.LENGTH_SHORT).show();
        }

    }

    private void TestChefExiste(String code){
        Cursor r=Accueil.bd.read("select * from chef_ligne where codebar='"+code+"'");
        if (r.moveToNext()){
            code_chef.setText(r.getString(r.getColumnIndex("code_chef")));
            spinnerChefs.closeSpinner();
        }else {
            Toast.makeText(getApplicationContext(),"Ce code n'existe pas !",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        if(backPressedNbr==0){
            backPressedNbr++;
            ligneSelected=false;
            code_ligne.setVisibility(View.VISIBLE);
            code_chef.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }
}
