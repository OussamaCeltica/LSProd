package com.leadersoft.celtica.lsprod.Preparations;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.Productions.AfficherStock;
import com.leadersoft.celtica.lsprod.R;

public class AfficherPreparations extends AppCompatActivity {

    private BonPreparationAdapter mAdapter;
    Cursor r;
    ETAT affichage=ETAT.EN_COURS;
    int test1=0;
    int min;
    private EditText searchInp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_preparations);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            searchInp=(EditText)findViewById(R.id.affprep_search);


            //region detecter l ouvrage de clavier ..
            final FrameLayout body=((FrameLayout)findViewById(R.id.affprep_root));//le view root de layout ..
            final LinearLayout div_options=((LinearLayout)findViewById(R.id.affprep_divOptions));

            body.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                    body.getWindowVisibleDisplayFrame(measureRect);
                    // measureRect.bottom is the position above soft keypad
                    if (getIntent().getExtras() == null){
                        int keypadHeight = body.getRootView().getHeight() - measureRect.bottom;

                        if(test1==0){
                            test1=1;
                            min=keypadHeight;
                        }

                        if (keypadHeight > min) {
                            // keyboard is opened
                            div_options.setVisibility(View.GONE);

                        } else {
                            //Keyboard is close ..
                            div_options.setVisibility(View.VISIBLE);

                        }
                    }

                }
            });

            //endregion

            //region configuration recyclerView
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_prep);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherPreparations.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new BonPreparationAdapter(AfficherPreparations.this);

            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            //endregion

            //region la Recherche ..
            searchInp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    Cursor r;
                    if (affichage==ETAT.EN_COURS){
                        if(s.toString().equals("")){
                            r=Accueil.bd.read("select * from bon_preparation where sync='0' and etat='en cours' order by id_bon desc");
                        }else {
                            r=Accueil.bd.read2("select * from bon_preparation where sync='0' and etat='en cours'  and nom_pr LIKE ? order by id_bon desc",new String[]{"%"+s.toString()+"%"});
                        }
                        afficherPrepEnCour(r);
                    }else {
                        if(s.toString().equals("")){
                            r=Accueil.bd.read("select * from bon_preparation where sync='0' and etat='validé' order by id_bon desc");
                        }else {
                            r=Accueil.bd.read2("select * from bon_preparation where sync='0' and etat='validé'  and nom_pr LIKE ? order by id_bon desc",new String[]{"%"+s.toString()+"%"});
                        }
                        afficherPrepValidé(r);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            //endregion

            //region changer le type de bon affiché ..
            ((LinearLayout)findViewById(R.id.affprep_prepCours)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    affichage=ETAT.EN_COURS;
                    setTypeBonTitre(getResources().getString(R.string.prep_titre_cours));
                    r=Accueil.bd.read("select * from bon_preparation where sync='0' and etat='en cours' order by id_bon desc");
                    afficherPrepEnCour(r);
                }
            });

            ((LinearLayout)findViewById(R.id.affprep_prepValid)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    affichage=ETAT.VALIDÉ;
                    setTypeBonTitre(getResources().getString(R.string.prep_titre_valid));
                    Cursor r=Accueil.bd.read("select * from bon_preparation where sync='0' and etat='validé' order by id_bon desc");
                    afficherPrepValidé(r);
                }
            });
            //endregion

            if (getIntent().getExtras() == null){
                setTypeBonTitre(getResources().getString(R.string.prep_titre_cours));
                r=Accueil.bd.read("select * from bon_preparation where sync='0' and etat='en cours' order by id_bon desc");
                afficherPrepEnCour(r);
            }else {
                //afficher l archive
                ((ImageView)findViewById(R.id.affprep_addPrep)).setVisibility(View.GONE);
                ((LinearLayout)findViewById(R.id.affprep_divOptions)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.affprep_titre)).setVisibility(View.GONE);

                ImageView menu=((ImageView)findViewById(R.id.affprep_menu));
                menu.setVisibility(View.VISIBLE);

                menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Creating the instance of PopupMenu
                        PopupMenu popup = new PopupMenu(AfficherPreparations.this,(ImageView)findViewById(R.id.affprep_menu));

                        popup.getMenu().add("Archive Production");
                        popup.getMenu().add("Archive Maintenance");


                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().toString().equals("Archive Production")){
                                    Intent i=new Intent(AfficherPreparations.this, AfficherStock.class);
                                    i.putExtra("request","archive");
                                    startActivity(i);
                                }

                                finish();
                                return true;
                            }
                        });

                        popup.show();//showing popup menu
                    }
                });



                r=Accueil.bd.read("select * from bon_preparation where sync='1'  order by id_bon desc");
                afficherArchive(r);
            }

        }
    }

    public void afficherPrepEnCour(Cursor r){
        BonPreparationAdapter.bons.clear();
        while (r.moveToNext()){
            BonPreparationAdapter.bons.add(new BonPreparation(r.getString(r.getColumnIndex("id_bon")),r.getString(r.getColumnIndex("code_pr"))+"",r.getString(r.getColumnIndex("nom_pr"))+"",r.getString(r.getColumnIndex("code_ligne"))+"",r.getString(r.getColumnIndex("nom_ligne"))+"",r.getString(r.getColumnIndex("date_bon"))+"", ETAT.EN_COURS));
        }

        mAdapter.notifyDataSetChanged();

    }

    public void afficherPrepValidé(Cursor r){
        BonPreparationAdapter.bons.clear();
        while (r.moveToNext()){
            BonPreparationAdapter.bons.add(new BonPreparation(r.getString(r.getColumnIndex("id_bon")),r.getString(r.getColumnIndex("code_pr"))+"",r.getString(r.getColumnIndex("nom_pr"))+"",r.getString(r.getColumnIndex("code_ligne"))+"",r.getString(r.getColumnIndex("nom_ligne"))+"",r.getString(r.getColumnIndex("date_bon"))+"", ETAT.VALIDÉ));
        }

        mAdapter.notifyDataSetChanged();
    }

    public void afficherArchive(Cursor r){
        BonPreparationAdapter.bons.clear();
        while (r.moveToNext()){
            BonPreparationAdapter.bons.add(new BonPreparation(r.getString(r.getColumnIndex("id_bon")),r.getString(r.getColumnIndex("code_pr"))+"",r.getString(r.getColumnIndex("nom_pr"))+"",r.getString(r.getColumnIndex("code_ligne"))+"",r.getString(r.getColumnIndex("nom_ligne"))+"",r.getString(r.getColumnIndex("date_bon"))+"", ETAT.EXPORTÉ));
        }

        mAdapter.notifyDataSetChanged();
    }

    public void setTypeBonTitre(String type){
        ((TextView)findViewById(R.id.affprep_titre)).setText(Html.fromHtml("<span><font color='black'>"+getResources().getString(R.string.prep_titre)+"</font> "+type+"</span>"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BonPreparationAdapter.bons.clear();
    }
}
