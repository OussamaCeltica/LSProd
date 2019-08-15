package com.leadersoft.celtica.lsprod.Productions;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.Maintenances.AfficherMaintenance;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;

import java.util.ArrayList;
import java.util.Calendar;

public class AfficherStock extends AppCompatActivity {

    Cursor r;
    ProductionAdapter mAdapter;
    EditText inputSearch;
    private String ligne="";
    MySpinnerSearchable spinnerLignes;
    boolean affTous=true;
    String request;
    int sync=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_stock);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            request=getIntent().getExtras().getString("request");

            if(!request.equals("archive")){
                r = Accueil.bd.read("select  * from production where date_fin is not null and sync='0' order by date_debut desc");
            }else {
                sync=1;
                ((ImageView)findViewById(R.id.prod_ligne_menu)).setVisibility(View.VISIBLE);

                //region configuer le menu d archive
                ((ImageView)findViewById(R.id.prod_ligne_menu)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Creating the instance of PopupMenu
                        PopupMenu popup = new PopupMenu(AfficherStock.this,(ImageView)findViewById(R.id.prod_ligne_menu));

                        //popup.getMenu().add("Archive Préparation");
                        popup.getMenu().add(getResources().getString(R.string.param_archiveMaint));


                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().toString().equals(getResources().getString(R.string.param_archiveProd))){
                                    Intent i=new Intent(AfficherStock.this, AfficherPreparations.class);
                                    i.putExtra("request","archive");
                                    startActivity(i);
                                }else if (item.getTitle().toString().equals(getResources().getString(R.string.param_archiveMaint))){
                                    Intent i=new Intent(AfficherStock.this, AfficherMaintenance.class);
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
                //endregion

                r = Accueil.bd.read("select  * from production where  sync='1' order by date_debut desc");
            }

            //region open div de recherche
            inputSearch = ((EditText) findViewById(R.id.prod_searchInp));
            final FrameLayout div_search = (FrameLayout) findViewById(R.id.prod_div_search);

            ((ImageView) findViewById(R.id.prod_search_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) div_search.getLayoutParams();
                    params.topMargin = 0;
                    div_search.setLayoutParams(params);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    inputSearch.requestFocus();

                }
            });

            //region open calendar ..
            ((ImageView) findViewById(R.id.prod_calendar_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Calendar myCalendar = Calendar.getInstance();

                    DatePickerDialog dp = new DatePickerDialog(AfficherStock.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            // TODO Auto-generated method stub
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            String mois, jour;
                            mois = monthOfYear + 1 + "";
                            jour = dayOfMonth + "";
                            if (monthOfYear + 1 < 10) {
                                mois = "0" + (monthOfYear + 1);
                            }
                            if (dayOfMonth < 10) {
                                jour = "0" + dayOfMonth;
                            }

                            inputSearch.setText(year + "-" + mois + "-" + jour);

                        }

                    }, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));

                    dp.show();

                }
            });
            //endregion
            //endregion

            //region fermer div de recherche
            ((ImageView) findViewById(R.id.prod_back_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) div_search.getLayoutParams();
                    params.topMargin = (int) -Session.pxFromDp(getApplicationContext(), 80);
                    div_search.setLayoutParams(params);
                    inputSearch.setText("");

                    View view = AfficherStock.this.getCurrentFocus();
                    if (view != null) {
                        view.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            });
            //endregion

            //region configuration recyclerview ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherStock.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new ProductionAdapter(AfficherStock.this);

            mRecyclerView.setAdapter(mAdapter);
            //endregion

            //region la recherche ..
            inputSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {


                    if(affTous){
                        if(s.toString().equals("")){
                            r = Accueil.bd.read("select * from production where date_fin is not null and sync='"+sync+"'    order by date_debut desc");
                        }else {
                            r = Accueil.bd.read("select * from production where date_fin is not null and sync='"+sync+"' and (date_debut LIKE '"+s+"%' or nom_pr LIKE '%"+s+"%') order by date_debut desc");
                        }
                    }else {
                        if(s.toString().equals("")){
                            r = Accueil.bd.read("select * from production where date_fin is not null and sync='"+sync+"'  and  code_ligne='"+ligne+"'   order by date_debut desc");
                        }else {
                            r = Accueil.bd.read("select * from production where date_fin is not null and sync='"+sync+"' and code_ligne='"+ligne+"' and (date_debut LIKE '"+s+"%' or nom_pr LIKE '%"+s+"%') order by date_debut desc");
                        }
                    }

                    /*
                     if(!request.equals("archive")){
                        if(affTous){
                            if(s.toString().equals("")){
                                r = Accueil.bd.read("select * from production where date_fin is not null and sync='0'    order by date_debut desc");
                            }else {
                                r = Accueil.bd.read("select * from production where date_fin is not null and sync='0' and (date_debut LIKE '"+s+"%' or nom_pr LIKE '%"+s+"%') order by date_debut desc");
                            }
                        }else {
                            if(s.toString().equals("")){
                                r = Accueil.bd.read("select * from production where date_fin is not null and sync='0'  and  code_ligne='"+ligne+"'   order by date_debut desc");
                            }else {
                                r = Accueil.bd.read("select * from production where date_fin is not null and sync='0' and code_ligne='"+ligne+"' and (date_debut LIKE '"+s+"%' or nom_pr LIKE '%"+s+"%') order by date_debut desc");
                            }
                        }
                    }else {
                        if(affTous){
                            if(s.toString().equals("")){
                                r = Accueil.bd.read("select * from production where sync='1'  order by date_debut desc");
                            }else {
                                r = Accueil.bd.read("select * from production where sync='1' and (date_debut LIKE '"+s+"%' or nom_pr LIKE '%"+s+"%') order by date_debut desc");
                            }
                        }else {
                            if(s.toString().equals("")){
                                r = Accueil.bd.read("select * from production where sync='1'  and  code_ligne='"+ligne+"' order by date_debut desc");
                            }else {
                                r = Accueil.bd.read("select * from production where sync='1'  and code_ligne='"+ligne+"' and (date_debut LIKE '"+s+"%' or nom_pr LIKE '%"+s+"%') order by date_debut desc");
                            }
                        }
                    }
                     */



                    afficherStock(r);

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            //endregion

            //region select line ..
            ArrayList<SpinnerItem> lignes=new ArrayList<SpinnerItem>();
            Cursor r2=Accueil.bd.read("select * from ligne_production");
            while (r2.moveToNext()){
                lignes.add(new SpinnerItem(r2.getString(r2.getColumnIndex("code_ligne")),r2.getString(r2.getColumnIndex("designiation"))));
            }
            spinnerLignes=new MySpinnerSearchable(AfficherStock.this, lignes, "", new MySpinnerSearchable.SpinnerConfig() {
                @Override
                public void onChooseItem(int pos, SpinnerItem item) {
                    ProductionAdapter.Productions.clear();
                    affTous=false;
                    ((TextView)findViewById(R.id.aff_prod_nomLigne)).setText(""+item.key);
                    ligne=item.key;

                    r = Accueil.bd.read("select * from production where date_fin is not null and sync='"+sync+"'  and  code_ligne='" + ligne + "'   order by date_debut desc");

                    spinnerLignes.closeSpinner();
                    afficherStock(r);
                }
            });

            ((ImageView)findViewById(R.id.prod_ligne_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spinnerLignes.openSpinner();
                }
            });

            //endregion


            afficherStock(r);
        }

    }

    public void afficherStock(Cursor r){
        ProductionAdapter.Productions.clear();

        if(!request.equals("archive")) {

            //region testé si il ya une production en cours / en arret  ..
            if (affTous) {
                Cursor r2 = Accueil.bd.read("select p.*, pa.date_fin as date_fin_arret,pa.date_debut as date_deb_arret from production p left join production_arret pa on pa.id_prod=p.id_prod   where p.date_fin is null  order by  p.date_debut  ");
                while (r2.moveToNext()) {
                    boolean isPrep=false;
                    if (r2.getString(r2.getColumnIndex("isPrep")).equals("1")) isPrep=true;


                    if (r2.getString(r2.getColumnIndex("date_fin_arret")) != null || r2.getString(r2.getColumnIndex("date_deb_arret")) == null) {
                        ProductionEnCours p=new ProductionEnCours(r2.getString(r2.getColumnIndex("id_prod")), r2.getString(r2.getColumnIndex("code_pr")), r2.getString(r2.getColumnIndex("code_chef")), r2.getString(r2.getColumnIndex("code_ligne")), r2.getString(r2.getColumnIndex("date_debut")), " / ", Integer.parseInt(r2.getString(r2.getColumnIndex("quantité"))));
                        p.isPrep=isPrep;
                        ProductionAdapter.Productions.add(p);
                    } else {
                        ProductionEnArret p=new ProductionEnArret(r2.getString(r2.getColumnIndex("id_prod")), r2.getString(r2.getColumnIndex("code_pr")), r2.getString(r2.getColumnIndex("code_chef")), r2.getString(r2.getColumnIndex("code_ligne")), r2.getString(r2.getColumnIndex("date_debut")), " / ", Integer.parseInt(r2.getString(r2.getColumnIndex("quantité"))));
                        p.isPrep=isPrep;
                        ProductionAdapter.Productions.add(new ProductionEnArret(r2.getString(r2.getColumnIndex("id_prod")), r2.getString(r2.getColumnIndex("code_pr")), r2.getString(r2.getColumnIndex("code_chef")), r2.getString(r2.getColumnIndex("code_ligne")), r2.getString(r2.getColumnIndex("date_debut")), " / ", Integer.parseInt(r2.getString(r2.getColumnIndex("quantité")))));
                    }

                    //ProductionAdapter.Productions.add(p);
                }
            } else {
                Cursor r2 = Accueil.bd.read("select p.*, pa.date_fin as date_fin_arret,pa.date_debut as date_deb_arret from production p left join production_arret pa on pa.id_prod=p.id_prod   where p.date_fin is null and code_ligne='" + ligne + "'  order by  date_debut desc limit 1 ");
                if (r2.moveToNext()) {
                    boolean isPrep=false;
                    if (r2.getString(r2.getColumnIndex("isPrep")).equals("1")) isPrep=true;


                    if (r2.getString(r2.getColumnIndex("date_fin_arret")) != null || r2.getString(r2.getColumnIndex("date_deb_arret")) == null) {
                        ProductionEnCours p=new ProductionEnCours(r2.getString(r2.getColumnIndex("id_prod")), r2.getString(r2.getColumnIndex("code_pr")), r2.getString(r2.getColumnIndex("code_chef")), r2.getString(r2.getColumnIndex("code_ligne")), r2.getString(r2.getColumnIndex("date_debut")), " / ", Integer.parseInt(r2.getString(r2.getColumnIndex("quantité"))));
                        p.isPrep=isPrep;
                        ProductionAdapter.Productions.add(p);
                    } else {
                        ProductionEnArret p=new ProductionEnArret(r2.getString(r2.getColumnIndex("id_prod")), r2.getString(r2.getColumnIndex("code_pr")), r2.getString(r2.getColumnIndex("code_chef")), r2.getString(r2.getColumnIndex("code_ligne")), r2.getString(r2.getColumnIndex("date_debut")), " / ", Integer.parseInt(r2.getString(r2.getColumnIndex("quantité"))));
                        p.isPrep=isPrep;
                        ProductionAdapter.Productions.add(new ProductionEnArret(r2.getString(r2.getColumnIndex("id_prod")), r2.getString(r2.getColumnIndex("code_pr")), r2.getString(r2.getColumnIndex("code_chef")), r2.getString(r2.getColumnIndex("code_ligne")), r2.getString(r2.getColumnIndex("date_debut")), " / ", Integer.parseInt(r2.getString(r2.getColumnIndex("quantité")))));
                    }
                }
            }
            //endregion

            while (r.moveToNext()) {
                boolean isPrep=false;
                if (r .getString(r .getColumnIndex("isPrep")).equals("1")) isPrep=true;

                Production p=new Production(r.getString(r.getColumnIndex("id_prod")),r.getString(r.getColumnIndex("code_pr")), r.getString(r.getColumnIndex("code_chef")), r.getString(r.getColumnIndex("code_ligne")), r.getString(r.getColumnIndex("date_debut")),r.getString(r.getColumnIndex("date_fin")) ,Double.parseDouble(r.getString(r.getColumnIndex("quantité"))));
                p.isPrep=isPrep;
                ProductionAdapter.Productions.add(p);
            }
        }else {
            while (r.moveToNext()) {
                boolean isPrep=false;
                if (r .getString(r .getColumnIndex("isPrep")).equals("1")) isPrep=true;

                if(r.getString(r.getColumnIndex("etat")).equals("exporté")){

                    ProductionExporté p=new ProductionExporté(r.getString(r.getColumnIndex("id_prod")),r.getString(r.getColumnIndex("code_pr")), r.getString(r.getColumnIndex("code_chef")), r.getString(r.getColumnIndex("code_ligne")), r.getString(r.getColumnIndex("date_debut")),r.getString(r.getColumnIndex("date_fin")) ,Double.parseDouble(r.getString(r.getColumnIndex("quantité"))));
                    p.isPrep=isPrep;
                    ProductionAdapter.Productions.add(p);

                }else {
                    ProductionSupprimé p=new ProductionSupprimé(r.getString(r.getColumnIndex("id_prod")),r.getString(r.getColumnIndex("code_pr")), r.getString(r.getColumnIndex("code_chef")), r.getString(r.getColumnIndex("code_ligne")), r.getString(r.getColumnIndex("date_debut")),r.getString(r.getColumnIndex("date_fin")) ,Double.parseDouble(r.getString(r.getColumnIndex("quantité"))));
                    p.isPrep=isPrep;
                    ProductionAdapter.Productions.add(p);
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        View view = AfficherStock.this.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProductionAdapter.Productions.clear();
    }
}
