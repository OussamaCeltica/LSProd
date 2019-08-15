package com.leadersoft.celtica.lsprod.Maintenances;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem3;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.Session;

import java.util.ArrayList;
import java.util.Calendar;

public class AfficherMaintenance extends AppCompatActivity {
    Cursor r;
    EditText inputSearch;
    String ancienDate="";
    private String ligne="";
    MySpinnerSearchable spinnerLignes;
    MaintenanceAdapter mAdapter;
    boolean affTous=true;
    public static boolean modeArchive=false;
    String request;
    int sync=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afficher_maintenance);
        if (savedInstanceState != null) {
            //region Revenir a au Deviceconfig ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {

            request=getIntent().getExtras().getString("request");

            if (!request.equals("archive")) {

                r = Accueil.bd.read("select * from maintenance where date_fin is not null and sync='0' order by date_debut desc");
            }else {
                sync=1;
                r = Accueil.bd.read("select * from maintenance where  sync='1' order by date_debut desc");
            }

            //region open div de recherche
            inputSearch = ((EditText) findViewById(R.id.mantain_searchInp));
            final FrameLayout div_search = (FrameLayout) findViewById(R.id.mantain_div_search);

            ((ImageView) findViewById(R.id.mantain_search_butt)).setOnClickListener(new View.OnClickListener() {
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
            ((ImageView) findViewById(R.id.mantain_calendar_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Calendar myCalendar = Calendar.getInstance();

                    DatePickerDialog dp = new DatePickerDialog(AfficherMaintenance.this, new DatePickerDialog.OnDateSetListener() {

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
            ((ImageView) findViewById(R.id.mantain_back_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) div_search.getLayoutParams();
                    params.topMargin = (int) -Session.pxFromDp(getApplicationContext(), 80);
                    div_search.setLayoutParams(params);
                    inputSearch.setText("");

                    View view = AfficherMaintenance.this.getCurrentFocus();
                    if (view != null) {
                        view.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            });
            //endregion

            //region configuration recyclerView ..
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.div_affich_mantain);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(AfficherMaintenance.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
             mAdapter = new MaintenanceAdapter(AfficherMaintenance.this);

            mRecyclerView.setAdapter(mAdapter);
            //endregion

            //region la recherche
            inputSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    MaintenanceAdapter.Maintenances.clear();



                    if(affTous){
                        if(s.toString().equals("")){
                            r = Accueil.bd.read("select * from maintenance where date_fin is not null and sync='"+sync+"' order by date_debut desc");
                        }else {
                            r = Accueil.bd.read("select * from maintenance where date_fin is not null and sync='"+sync+"' and (date_debut LIKE '"+s+"%' or nom_emp LIKE '%"+s+"%') order by date_debut desc");
                        }
                    }else {
                        if(s.toString().equals("")){
                            r = Accueil.bd.read("select * from maintenance where date_fin is not null and sync='"+sync+"' and code_ligne='"+ligne+"' order by date_debut desc");
                        }else {
                            Log.e("ttt"," Rani tem sync="+sync);
                            r = Accueil.bd.read("select * from maintenance where date_fin is not null and sync='"+sync+"' and code_ligne='"+ligne+"' and (date_debut LIKE '"+s+"%' or nom_emp LIKE '%"+s+"%') order by date_debut desc");
                        }
                    }

                    afficherMaintenance(r);


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
                lignes.add(new SpinnerItem3(r2.getString(r2.getColumnIndex("code_ligne")),r2.getString(r2.getColumnIndex("designiation")),r2.getString(r2.getColumnIndex("Oid"))));
            }
            spinnerLignes=new MySpinnerSearchable(AfficherMaintenance.this, lignes, "", new MySpinnerSearchable.SpinnerConfig() {
                @Override
                public void onChooseItem(int pos, SpinnerItem item) {
                    MaintenanceAdapter.Maintenances.clear();
                    affTous=false;
                    ((TextView)findViewById(R.id.aff_prod_nomLigne)).setText(""+item.value);
                    ligne=((SpinnerItem3)item).key2;
                    r= Accueil.bd.read("select * from maintenance where date_fin is not null and sync='"+sync+"'  and  code_ligne='"+ligne+"'   order by date_debut desc");
                    spinnerLignes.closeSpinner();
                    afficherMaintenance(r);
                }
            });

            ((ImageView)findViewById(R.id.mantain_ligne_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spinnerLignes.openSpinner();
                }
            });

            //endregion


            afficherMaintenance(r);



            /*------------------------------------*/
/*
            //region ajouter une maintenance ..
            ((ImageView)findViewById(R.id.mantain_add_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(AfficherMaintenance.this,AddMaintenance.class);
                    i.putExtra("request",request);
                    if(request.equals("second-scan")){
                        i.putExtra("id",getIntent().getExtras().getString("id"));
                        i.putExtra("type_maint",getIntent().getExtras().getString("type_maint"));
                    }
                    startActivity(i);
                }
            });
            //endregion
            */

        }
    }

    public void afficherMaintenance(Cursor r){

        MaintenanceAdapter.Maintenances.clear();
        if (!request.equals("archive")){
            //region testé si il ya une manitenance en cours ..
            Cursor r2;
            if (affTous){
                r2=Accueil.bd.read("select * from maintenance where date_fin is null  order by  date_debut");

            }else {
                r2=Accueil.bd.read("select * from maintenance where date_fin is null and code_ligne='"+ligne+"' order by  date_debut desc limit 1");
            }
            while (r2.moveToNext()){
                MaintenanceAdapter.Maintenances.add(new Maintenance(r2.getString(r2.getColumnIndex("id")) ,r2.getString(r2.getColumnIndex("date_debut")),"/", r2.getString(r2.getColumnIndex("code_ligne")),r2.getString(r2.getColumnIndex("code_emp")),r2.getString(r2.getColumnIndex("oid_type_maint")), ETAT.EN_COURS));
            }
            //endregion
        }

        while (r.moveToNext()) {
            MaintenanceAdapter.Maintenances.add(new Maintenance(r.getString(r.getColumnIndex("id")), r.getString(r.getColumnIndex("date_debut")), r.getString(r.getColumnIndex("date_fin")), r.getString(r.getColumnIndex("code_ligne")),r.getString(r.getColumnIndex("code_emp")) ,r.getString(r.getColumnIndex("type_mantain")),(r.getString(r.getColumnIndex("sync")).equals("0")==true ? ETAT.VALIDÉ : (r.getString(r.getColumnIndex("etat")).equals("exporté")==true ? ETAT.EXPORTÉ : ETAT.SUPPRIMÉ ))));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        View view = AfficherMaintenance.this.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MaintenanceAdapter.Maintenances.clear();
    }
}
