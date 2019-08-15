package com.leadersoft.celtica.lsprod.Preparations;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.DeviceConfig2;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.Productions.FaireProdConfig;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;

import java.util.ArrayList;

public class FairePrepConfig extends AppCompatActivity {

    String codebar_pr="";
    String codebar_ligne="";
     MySpinnerSearchable spinnerLigne;
     int demandeScann;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_prep_config);

        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            //region select produit ..
            ((TextView)findViewById(R.id.prep_config_pr)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    demandeScann=1;
                    DeviceConfig.session.openScannerCodeBarre(FairePrepConfig.this, new Session.OnScanListener() {
                        @Override
                        public void OnScan(String code, LinearLayout body) {
                            checkProduitExiste(code);
                        }
                    });
                }
            });
            //endregion

            //region configuration de code de ligne ..
            ArrayList<SpinnerItem> codes_ligneProd= new ArrayList<SpinnerItem>();
            Cursor r1 = Accueil.bd.read("select * from ligne_production");
            while (r1.moveToNext()) {
                codes_ligneProd.add(new SpinnerItem(r1.getString(r1.getColumnIndex("code_ligne")),r1.getString(r1.getColumnIndex("designiation"))));
            }

            spinnerLigne=new MySpinnerSearchable(this, codes_ligneProd, getResources().getString(R.string.add_pr_selectLigne), new MySpinnerSearchable.SpinnerConfig() {
                @Override
                public void onChooseItem(int pos, SpinnerItem item) {
                    //pos not fonctional yet ..
                    ((TextView)findViewById(R.id.prep_config_ligne)).setText(item.value);
                    codebar_ligne=item.key;
                    spinnerLigne.closeSpinner();

                }
            }, new MySpinnerSearchable.ButtonSpinnerOnClick() {
                @Override
                public void onClick() {
                    demandeScann=2;
                    spinnerLigne.closeSpinner();
                    DeviceConfig.session.openScannerCodeBarre(FairePrepConfig.this, new Session.OnScanListener() {
                        @Override
                        public void OnScan(String code,LinearLayout root) {
                           checkLigneExiste(code);
                        }
                    });
                }
            });


            ((TextView)findViewById(R.id.prep_config_ligne)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spinnerLigne.openSpinner();

                }
            });
            //endregion

            //region valider le produit
            ((TextView)findViewById(R.id.prep_config_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(codebar_pr.equals("") || codebar_ligne.equals("")){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.fairePrep_noPr),Toast.LENGTH_SHORT).show();
                    }else {

                        Log.e("ttt1",codebar_pr+"");
                        Intent i=new Intent(FairePrepConfig.this,FairePrep.class);
                        i.putExtra("request","new_bon");
                        i.putExtra("nom_pr",""+((TextView)findViewById(R.id.prep_config_pr)).getText());
                        i.putExtra("nom_ligne",""+((TextView)findViewById(R.id.prep_config_ligne)).getText());
                        i.putExtra("code_ligne",""+codebar_ligne);
                        i.putExtra("codebar_pr",""+codebar_pr);
                        startActivity(i);
                    }

                }
            });
            //endregion
        }
    }

    public void checkProduitExiste(String pr){
        Cursor r= Accueil.bd.read("select * from produit where codebar='"+pr+"'");
        if (r.moveToNext()){
            codebar_pr=pr;
            ((TextView)findViewById(R.id.prep_config_pr)).setText(r.getString(r.getColumnIndex("nom_pr")));
        }else {
            codebar_pr="";
            ((TextView)findViewById(R.id.prep_config_pr)).setText("");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.fairePrep_noCodebar),Toast.LENGTH_SHORT).show();
        }
    }

    public void checkLigneExiste(String codebar){
        Cursor r= Accueil.bd.read("select * from ligne_production where codebar='"+codebar+"'");
        if (r.moveToNext()){
            codebar_ligne=codebar;
            ((TextView)findViewById(R.id.prep_config_ligne)).setText(r.getString(r.getColumnIndex("designiation")));
        }else {
            codebar_ligne="";
            ((TextView)findViewById(R.id.prep_config_ligne)).setText("");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.fairePrep_noCodebar),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if (demandeScann==1)
                checkProduitExiste(data.getExtras().getString("code"));
            else
                checkLigneExiste(data.getExtras().getString("code"));
        }
    }
}
