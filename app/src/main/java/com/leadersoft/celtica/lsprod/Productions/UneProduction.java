package com.leadersoft.celtica.lsprod.Productions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.Preparations.FairePrep;
import com.leadersoft.celtica.lsprod.Preparations.PanierPreparationAdapter;
import com.leadersoft.celtica.lsprod.R;

import java.util.ArrayList;

public class UneProduction extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_une_production);

        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {



            ((TextView) findViewById(R.id.unePr_codebar)).setText(((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).codebar);

            ((TextView) findViewById(R.id.unePr_nomPr)).setText(((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).nomPr);

            ((TextView) findViewById(R.id.unePr_conditionPr)).setText("");

            ((TextView) findViewById(R.id.unePr_qtConditionPr)).setText("");

            ((TextView) findViewById(R.id.unePr_codeChef)).setText(((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).code_chef);

            ((TextView) findViewById(R.id.unePr_codeLigne)).setText(((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).code_ligne);

            ((TextView) findViewById(R.id.unePr_nomChef)).setText(((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).nomChef);

            if(((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).isPrep){
                ((TextView)findViewById(R.id.prodPrep_affPr)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.prodPrep_affPr)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PanierPreparationAdapter.produits=((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).getProduitPreparer();
                        Intent i=new Intent(UneProduction.this, FairePrep.class);
                        i.putExtra("request","bon_validé");
                        startActivity(i);
                    }
                });
            }
            //((TextView)findViewById(R.id.unePr_tmpArret)).setText(((Production)ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).tmp_arret+" Min");


            //region afficher les arret ..
            LinearLayout div_affich_arret=(LinearLayout)findViewById(R.id.div_affich_arret);
            ArrayList<ArretProduction> arrets=((Production) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).getArretProd();
            for (ArretProduction arret : arrets ){
                View v=getLayoutInflater().inflate(R.layout.div_arret,null);
                ((TextView)v.findViewById(R.id.div_arret_motive)).setText(arret.motive_arret+"");
                ((TextView)v.findViewById(R.id.div_arret_tmp)).setText(arret.getTempsArret()+" Min");

                div_affich_arret.addView(v);
            }


            //endregion
        }
    }
}
