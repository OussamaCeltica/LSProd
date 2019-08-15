package com.leadersoft.celtica.lsprod.Productions;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.Preparations.FairePrep;
import com.leadersoft.celtica.lsprod.Preparations.PanierPreparationAdapter;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;

import java.util.ArrayList;

public class FaireProdMode extends AppCompatActivity {

    MySpinnerSearchable spinnerArretProdMotive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_prod_mode);

        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {
            final String request = getIntent().getExtras().getString("request");
            TextView firstScan=((TextView)findViewById(R.id.add_stock_firstScan));
            TextView secondScan,mettreEnPause,relncerProd;
            secondScan = (TextView)findViewById(R.id.faire_prod_dernierScan);
            mettreEnPause = (TextView)findViewById(R.id.faire_prod_stopProd);
            relncerProd= (TextView)findViewById(R.id.faire_prod_RelancerProd);

            if (request.equals("first-scan")){
                firstScan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent i=new Intent(FaireProdMode.this,FaireProd.class);
                        i.putExtra("request","first-scan");
                        startActivity(i);

                    }
                });
            }else if (request.equals("lancer-prod")){
                firstScan.setVisibility(View.GONE);
                ((TextView)findViewById(R.id.pr_label)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.faire_prod_mode_nom_pr)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.faire_prod_mode_nom_pr)).setText(getIntent().getExtras().getString("produit"));

                relncerProd.setVisibility(View.VISIBLE);
                relncerProd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProductionEnArret p=new ProductionEnArret(getIntent().getExtras().getString("id_prod"));
                        p.relancerProduction(p.getLastArretId());

                        Toast.makeText(getApplicationContext(),"La production est relancer.",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), Accueil.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
            else {
                firstScan.setVisibility(View.GONE);
                secondScan.setVisibility(View.VISIBLE);
                mettreEnPause.setVisibility(View.VISIBLE);

                if (ProductionAdapter.selectedItem != -1 && ((Production)ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).isPrep){
                    ((TextView)findViewById(R.id.faireProdMode_addPr)).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.faireProdMode_addPr)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i=new Intent(FaireProdMode.this,FairePrep.class);
                            PanierPreparationAdapter.produits=((Production)ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).getProduitPreparer();
                            i.putExtra("request","en cours");
                            startActivity(i);
                        }
                    });
                }

                ((TextView)findViewById(R.id.pr_label)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.faire_prod_mode_nom_pr)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.faire_prod_mode_nom_pr)).setText(getIntent().getExtras().getString("produit"));

                secondScan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i=new Intent(FaireProdMode.this,FaireProd.class);
                        i.putExtra("request","second-scan");
                        i.putExtra("id_prod",getIntent().getExtras().getString("id_prod"));
                        i.putExtra("produit",getIntent().getExtras().getString("produit"));
                        startActivity(i);
                    }
                });

                //region mettre en pause la prod ..
                mettreEnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<SpinnerItem> arretMotives=new ArrayList<SpinnerItem>();
                        Cursor r=Accueil.bd.read("select * from type_arret");
                        while (r.moveToNext()){
                            arretMotives.add(new SpinnerItem("",r.getString(r.getColumnIndex("type"))));
                        }
                        spinnerArretProdMotive=new MySpinnerSearchable(FaireProdMode.this, arretMotives, "", new MySpinnerSearchable.SpinnerConfig() {
                            @Override
                            public void onChooseItem(int pos, final SpinnerItem item) {
                                AlertDialog.Builder mb = new AlertDialog.Builder(FaireProdMode.this); //c est l activity non le context ..

                                View v= getLayoutInflater().inflate(R.layout.confirm_box,null);
                                TextView msg=(TextView) v.findViewById(R.id.confirm_msg);
                                TextView oui=(TextView) v.findViewById(R.id.confirm_oui);
                                TextView non=(TextView) v.findViewById(R.id.confirm_non);

                                msg.setText(Html.fromHtml("<span>"+getResources().getString(R.string.add_pr_pauseTitre)+" <br><br> <font color='#f9a327'>type:</font> "+item.value+" </span> "));
                                mb.setView(v);
                                final AlertDialog ad=mb.create();
                                ad.show();
                                ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                ad.setCancelable(false); //désactiver le button de retour ..

                                non.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ad.dismiss();
                                    }
                                });

                                oui.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ArretProduction p=new ArretProduction(item.value);
                                        p.addToBD(getIntent().getExtras().getString("id_prod"));

                                        Toast.makeText(getApplicationContext(),"La production est en pause.",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), Accueil.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });

                                spinnerArretProdMotive.closeSpinner();
                            }
                        });

                        spinnerArretProdMotive.openSpinner();

                    }
                });
                //endregion

            }
        }
    }
}
