package com.leadersoft.celtica.lsprod.Preparations;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.CodeBarScanner;
import com.leadersoft.celtica.lsprod.Depot;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.Productions.AfficherStock;
import com.leadersoft.celtica.lsprod.Productions.Production;
import com.leadersoft.celtica.lsprod.Productions.ProductionAdapter;
import com.leadersoft.celtica.lsprod.Productions.ProductionEnCours;
import com.leadersoft.celtica.lsprod.Productions.Produit;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.REQUEST_SCANNER;
import com.leadersoft.celtica.lsprod.Session;

public class FairePrep extends AppCompatActivity {

    public  static boolean withIncr=false;
    String request;
    public static FairePrep me;
    PanierPreparationAdapter mAdapter;
    int demandScan=0;
    String code_pr="",code_dep="";
    public TextView selectDepotPanier,selectPrPanier;
    LinearLayout divAddPr;
    boolean divAddPrIsOpen=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faire_prep);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {
            me=this;
            request=getIntent().getExtras().getString("request");
            divAddPr=(LinearLayout)findViewById(R.id.fairePrep_divAddPr);
            selectPrPanier=(TextView) findViewById(R.id.divPrepPanierAdd_pr);
            selectDepotPanier=(TextView) findViewById(R.id.divPrepPanierAdd_depot);

            //region configuration recyclerview
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_panier);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(FairePrep.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new PanierPreparationAdapter(FairePrep.this);

            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            //endregion

            //region type de request
            if(request.equals("bon_validé") || request.equals("archive")){
                ((ImageView)findViewById(R.id.add_pr_toPanier)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.faire_inventaire_valider)).setVisibility(View.GONE);
            }else if(request.equals("new_bon")) {
                 BonPreparationAdapter.itemSelected = -1;
            }else {
                //le bon est en cours de preparation ..
                Depot d=((ProductionEnCours)ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).getLastDepotPréparé();
                if(d != null){
                    code_dep=d.code_dep;
                    selectDepotPanier.setText(d.nom_dep+"");
                }
            }
            //endregion

            //region ajouter produit au panier
            ((ImageView) findViewById(R.id.add_pr_toPanier)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    divAddPrIsOpen=true;
                    divAddPr.setVisibility(View.VISIBLE);
                    TextView valider=(TextView) findViewById(R.id.divPrPanierAdd_valider);

                    final EditText lot=(EditText)findViewById(R.id.divPrepPanierAdd_lot);
                    final EditText qt=(EditText)findViewById(R.id.divPrepPanierAdd_qt);



                    if (!DeviceConfig.session.isLot){
                        lot.setVisibility(View.GONE);
                    }

                    selectPrPanier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            demandScan=0;
                            DeviceConfig.session.openScannerCodeBarre(FairePrep.this, new Session.OnScanListener() {
                                @Override
                                public void OnScan(String code, LinearLayout div_scanner) {
                                    CheckPrExiste2(code,selectPrPanier);
                                }
                            });

                        }
                    });

                    selectDepotPanier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            demandScan=1;
                            DeviceConfig.session.openScannerCodeBarre(FairePrep.this, new Session.OnScanListener() {
                                @Override
                                public void OnScan(String code, LinearLayout div_scanner) {
                                    CheckDepExiste2(code,selectDepotPanier);
                                }
                            });

                        }
                    });

                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //testé si les champ ne sont pas vide ..
                            if(code_dep.equals("") || code_pr.equals(""))
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.remplissage_err),Toast.LENGTH_SHORT).show();
                            else {
                                divAddPr.setVisibility(View.GONE);
                                mAdapter.addPrToPanier(new ProduitPréparé(code_pr,selectPrPanier.getText().toString(),code_dep,""+selectDepotPanier.getText().toString(),lot.getText().toString()+"",Double.parseDouble(qt.getText().toString())));
                                mAdapter.notifyDataSetChanged();
                                code_dep="";selectPrPanier.setText("");lot.setText("");qt.setText("");
                                divAddPrIsOpen=false;
                            }

                        }
                    });




                /* deprecated -- Utlilisé avec Preparation sans prod
                CodeBarScanner.requestScanner= REQUEST_SCANNER.PREPARATION;
                DeviceConfig.session.openScannerCodeBarreIncr(FairePrep.this, new Session.OnScanListenerIncr() {
                    @Override
                    public void OnScanIncrNotChecked(final String code, final LinearLayout body, AlertDialog ad) {
                        withIncr=false;
                        if (checkPrExiste(code)){
                            ad.dismiss();
                            affDivSetQt(code);
                        }else {
                            DeviceConfig.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));

                        }
                    }

                    @Override
                    public void OnScan(String code, final LinearLayout body) {
                        withIncr=true;
                        if (checkPrExiste(code)) {
                            DeviceConfig.session.changeColorOnScan(body,getResources().getColor(R.color.Green),getResources().getColor(R.color.White));
                            addPrToPanier(code,1);
                        }else {
                            DeviceConfig.session.changeColorOnScan(body,getResources().getColor(R.color.Red),getResources().getColor(R.color.White));
                        }
                    }
                });
                */
                }
            });
            //endregion

            //region valider le bon ..
            ((TextView) findViewById(R.id.faire_inventaire_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (PanierPreparationAdapter.produits.size() == 0){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.fairePrep_panierVide), Toast.LENGTH_SHORT).show();
                    }else {
                        if (request.equals("new_bon")) {
                            ProductionEnCours prep=new ProductionEnCours(getIntent().getExtras().getString("codebar_pr")+"", DeviceConfig.session.code_chef+"",DeviceConfig.session.code_ligne+"");
                            prep.isPrep=true;
                            prep.addToBD();

                            for (ProduitPréparé p : PanierPreparationAdapter.produits) {
                                p.addToBDProd(prep.id_prod);

                            }

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Accueil.class);
                            Intent i2 = new Intent(getApplicationContext(), AfficherStock.class);
                            i2.putExtra("request","productions");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivities(new Intent[]{intent, i2});
                        }else {
                            ((Production)ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).suppPrPréparé();
                            for (ProduitPréparé p : PanierPreparationAdapter.produits) {
                                p.addToBDProd(((ProductionEnCours) ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).id_prod);

                            }
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Accueil.class);
                            Intent i2 = new Intent(getApplicationContext(), AfficherStock.class);
                            i2.putExtra("request","productions");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivities(new Intent[]{intent, i2});
                        }


                    /* Deprecated --- On l utilise si on veut séparé la pré paration de production ..
                    if (request.equals("new_bon")) {
                        Log.e("ttt2", getIntent().getExtras().getString("codebar_pr") + "");
                        BonPreparation bon = new BonPreparation(getIntent().getExtras().getString("codebar_pr"), getIntent().getExtras().getString("nom_pr"), getIntent().getExtras().getString("code_ligne"), getIntent().getExtras().getString("nom_ligne"));
                        bon.addToBD();

                        for (ProduitPréparé p : PanierPreparationAdapter.produits) {
                            p.addToBD(bon.id_bon);

                        }

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), Accueil.class);
                        Intent i2 = new Intent(getApplicationContext(), AfficherPreparations.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivities(new Intent[]{intent, i2});
                    }else {
                        //supp produit preparer et réinséré ..
                        BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).suppPrPréparé();
                        for (ProduitPréparé p : PanierPreparationAdapter.produits) {
                            p.addToBD(BonPreparationAdapter.bons.get(BonPreparationAdapter.itemSelected).id_bon);

                        }
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), Accueil.class);
                        Intent i2 = new Intent(getApplicationContext(), AfficherPreparations.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivities(new Intent[]{intent, i2});
                    }
                    */

                    }

                }
            });
            //endregion

        }

    }


    public boolean checkPrExiste(String code){
        ProduitPréparé p=new ProduitPréparé(code);
        if (p.existe()) {

            double qt=0;

            if (withIncr)qt=1;//pour l affichage de quantity: si avec incr j ajout 1 par default ..
            DeviceConfig.session.playAudioFromAsset(FairePrep.this,"barcode_succ.mp3");
            Toast.makeText(getApplicationContext(),"Produit: "+p.designiation+" \n Quantité: "+(mAdapter.getProduitQt(p)+qt),Toast.LENGTH_SHORT).show();
            return true;
        }else{
            DeviceConfig.session.playAudioFromAsset(FairePrep.this,"barcode_err.wav");
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.fairePrep_noCodebar),Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void affDivSetQt(final String code){

        AlertDialog.Builder mb = new AlertDialog.Builder(FairePrep.this); //c est l activity non le context ..

        View v= getLayoutInflater().inflate(R.layout.div_qt_pr,null);
        TextView valider=(TextView) v.findViewById(R.id.valider_pr_panier);
        TextView nomPr=(TextView) v.findViewById(R.id.panier_qt_nom_pr);
        final EditText qt=(EditText)v.findViewById(R.id.panier_qt);

        mb.setView(v);
        final AlertDialog ad=mb.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

        qt.requestFocus();


        DeviceConfig.session.opneClavier(FairePrep.this);

        qt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if( !qt.getText().toString().equals("")){
                        DeviceConfig.session.closeClavier(FairePrep.this,qt);
                        addPrToPanier(code,Double.parseDouble(qt.getText().toString()));
                        ad.dismiss();
                    }
                }
                return false;
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !qt.getText().toString().equals("")){
                    DeviceConfig.session.closeClavier(FairePrep.this,qt);
                    addPrToPanier(code,Double.parseDouble(qt.getText().toString()));
                    ad.dismiss();

                }
            }
        });


    }

    public void addPrToPanier(String code,double qt){

        Produit p=new Produit(code);
        p.existe();
        mAdapter.addPrToPanier(new ProduitPréparé(code, p.designiation, "",0,qt));
        mAdapter.notifyDataSetChanged();
    }

    public void CheckPrExiste2(String code,TextView pr){
        ProduitPréparé p=new ProduitPréparé(code);
        if (p.existe()){
            code_pr=code;
            pr.setText(p.designiation+"");
        }else {
            code_pr="";
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.fairePrep_noCodebar),Toast.LENGTH_SHORT).show();
        }
    }

    public void CheckDepExiste2(String code,TextView depot){
        Depot dep=new Depot(code);
        if (dep.existe()){
            code_dep=code;
            depot.setText(dep.nom_dep+"");
        }else {
            code_dep="";
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.fairePrep_noCodebar),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            final String code=data.getExtras().getString("code");
            if(demandScan==0){
               CheckPrExiste2(code,selectPrPanier);
            }else {
                CheckDepExiste2(code,selectDepotPanier);
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (divAddPrIsOpen){
            divAddPr.setVisibility(View.GONE);
        }else {
            if(PanierPreparationAdapter.produits.size() != 0 && (request.equals("new_bon") || request.equals("en cours")) ){
                AlertDialog.Builder mb = new AlertDialog.Builder(FairePrep.this); //c est l activity non le context ..

                View v= getLayoutInflater().inflate(R.layout.confirm_box,null);
                TextView oui=(TextView) v.findViewById(R.id.confirm_oui);
                TextView non=(TextView) v.findViewById(R.id.confirm_non);
                TextView msg=(TextView) v.findViewById(R.id.confirm_msg);

                msg.setText(getResources().getString(R.string.panier_noVide_quiter));

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
                        finish();
                    }
                });



            }else
                super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PanierPreparationAdapter.produits.clear();
    }
}
