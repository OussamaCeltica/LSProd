package com.leadersoft.celtica.lsprod.Productions;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.R;
import com.leadersoft.celtica.lsprod.ScannerIncrementation;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddStock extends AppCompatActivity {
    private ZXingScannerView mScannerView;
    boolean selectPr=false;
      EditText qt;
      TextView code_bar;
      EditText code;
      AlertDialog ad;
      boolean dejaSelect=false;//pour l incrementation si on select le premier pr pour verifier q il ne change pas .. ..
      String prScanned =""; //le premier pr qu on va scanner dans l incrementation ..
      int qt_incr=0;
      String ExportationErr="";
      int code_prod=0;
    ProgressDialog progress;
    boolean prodTerminé=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);
        if (savedInstanceState != null) {

            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion$

        }else {

            code_bar = (TextView) findViewById(R.id.add_stock_codebar);
            qt = (EditText) findViewById(R.id.add_stock_qt);

            //region si le type d appareil est avec scanner intégré ..
            if (DeviceConfig.session.type.equals(getResources().getString(R.string.config_type_scanner))) {

                //region si l ajout est par quantité ..
                if (getIntent().getExtras().getString("mode").equals("quantité")) {
                    if(getIntent().getExtras().getString("request").equals("first-scan")){
                        qt.setVisibility(View.GONE);
                    }

                    qt.setVisibility(View.GONE);

                    openDivScanner();

                    code_bar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openDivScanner();
                        }
                    });

                    //region Fermer le clavier ..
                    View view = getCurrentFocus();
                    if (view != null) {
                        view.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        //on peut remplace le view par l id de notre EditText ..
                    }
                    //endregion
                }
                //endregion

                //region l ajout par incrémentation ..
                else {
                    startActivityForResult(new Intent(AddStock.this, ScannerIncrementation.class), 2);

                }
                //endregion
            }
            //endregion

            //region si le type est sans scanner intégré ..
            else {

                mScannerView = new ZXingScannerView(this); // Programmatically initialize the scanner view

                //region check camera permission ..
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(AddStock.this, new String[]{Manifest.permission.CAMERA}, 8);

                }
                //endregion

                else {
                    setContentView(mScannerView);

                    //region add pr to bd ..
                    if (getIntent().getExtras().getString("mode").equals("quantité")) {
                        //region select quantity apres le scan ..
                        mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                            @Override
                            public void handleResult(final Result result) {

                                mScannerView.stopCamera();

                                //region test si ce produit existe ..
                                if (Accueil.bd.read("select * from produit where codebar='" + result.getText() + "'").moveToNext()) {

                                    if(getIntent().getExtras().getString("request").equals("first-scan")){
                                        addToBD(result.getText(),0);
                                    }else {

                                        Production p=new Production(getIntent().getExtras().getString("id_prod")+"");
                                        if(p.produitExisteInProd(result.getText())){
                                            AlertDialog.Builder mb = new AlertDialog.Builder(AddStock.this); //c est l activity non le context ..

                                            View v = getLayoutInflater().inflate(R.layout.div_give_qt, null);
                                            TextView valider = (TextView) v.findViewById(R.id.div_qt_add);
                                            final EditText qt = (EditText) v.findViewById(R.id.div_qt_qt);

                                            valider.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (qt.getText().toString().equals("")) {
                                                        Toast.makeText(getApplicationContext(), "Donner une quantité !", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        addToBD("055",Integer.parseInt(qt.getText().toString()));

                                                    }
                                                }
                                            });

                                            mb.setView(v);
                                            final AlertDialog ad = mb.create();
                                            ad.show();

                                        }else {
                                            Toast.makeText(getApplicationContext(),"Ce produit n'appartient pas a cette production , veuillez scanner le bon produit ! ",Toast.LENGTH_SHORT).show();

                                            mScannerView.resumeCameraPreview(this);
                                            mScannerView.startCamera();
                                        }



                                    }



                                }
                                //endregion

                                //region si le produit n existe pas
                                else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_pr_err), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                //endregion


                            }
                        }); // Register ourselves as a handler for scan results.// Register ourselves as a handler for scan results.
                        //endregion

                    } else {
                        //region ajouter par incrementation ..
                        mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                            @Override
                            public void handleResult(Result result) {
                                mScannerView.stopCamera();

                                //region vérifier si c est un nv pr ou l ancien ..
                                if(prExiste(result.getText())){
                                    if(!dejaSelect){
                                        dejaSelect=true;
                                        prScanned=result.getText();
                                        qt_incr++;
                                        Toast.makeText(getApplicationContext(), "Quantité: " + qt_incr, Toast.LENGTH_SHORT).show();

                                    }else {
                                        if (result.getText().equals(prScanned)){
                                            qt_incr++;
                                            Toast.makeText(getApplicationContext(), "Quantité: " + qt_incr, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                //endregion

                                mScannerView.resumeCameraPreview(this);
                                mScannerView.startCamera();
                                //addToBD();
                            }
                        }); // Register ourselves as a handler for scan results.// Register ourselves as a handler for scan results.
                        //endregion
                    }
                    //endregion

                    mScannerView.startCamera();
                }
            }
            //endregion
        }


    }

    //region les methodes ..
    @Override
    public void onStop() {
        super.onStop();
        if(DeviceConfig.session.type.equals(getResources().getString(R.string.config_type_sansscanner))){
           mScannerView.stopCamera();           // Stop camera on pause
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bundle b=data.getExtras();
            addToBD(b.getString("code"),Integer.parseInt(b.getString("qt")));
        }
    }

    //region la methode d insertion au BD ..
    private void addToBD(String pr,int qt){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        if(getIntent().getExtras().getString("request").equals("first-scan")){
            ProductionEnCours p=new ProductionEnCours(pr,DeviceConfig.session.code_chef,DeviceConfig.session.code_ligne);
            p.addToBD();
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
            revenirAccueil();
            /*
            int id=0;
            Cursor r=Accueil.bd.read("select id_prod from derniere_export order by id_prod desc limit 1");
            if(r.moveToNext()){
                id=Integer.parseInt(r.getString(r.getColumnIndex("id_prod")))+1;
            }

            String nom_pr="";
            r=Accueil.bd.read("select nom_pr from produit where codebar='"+pr+"'");
            if(r.moveToNext()){
                nom_pr=r.getString(r.getColumnIndex("nom_pr"));
            }
            Accueil.bd.write("insert into production (id_prod,code_chef,nom_chef,code_ligne,code_pr,nom_pr,date_debut,sync,tmp_arret,quantité) values('"+id+"','"+DeviceConfig.session.code_chef+"','"+getNomchef(DeviceConfig.session.code_chef)+"','"+DeviceConfig.session.code_ligne+"','"+pr+"','"+nom_pr+"','"+date+"',0,'0',0)");
            Accueil.bd.write("update derniere_export set id_prod='"+id+"' ");
            */

        }else if(getIntent().getExtras().getString("request").equals("cummule")){
            Accueil.bd.write("update production set quantité=quantité+"+qt+" where id_prod='"+getIntent().getExtras().getString("id_prod")+"'");
        } else{
            //Accueil.bd.write("update production set date_fin='"+date+"',quantité=quantité+"+qt+" where id_prod='"+getIntent().getExtras().getString("id_prod")+"'");
            final ProductionEnCours p=new ProductionEnCours(getIntent().getExtras().getString("id_prod"));
            //p.terminerProd(qt);
            prodTerminé=true;

            //region exporté prod ..
            try {
                Accueil.BDsql=new SqlServerBD(DeviceConfig.session.serveur.ip,DeviceConfig.session.serveur.port,DeviceConfig.session.serveur.bdName,DeviceConfig.session.serveur.user,DeviceConfig.session.serveur.mdp,"net.sourceforge.jtds.jdbc.Driver",new SqlServerBD.doAfterBeforeConnect(){

                    @Override
                    public void echec() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_conect_err),Toast.LENGTH_SHORT).show();
                                revenirAccueil();
                            }
                        });

                    }

                    @Override
                    public void before() {
                        progress = new ProgressDialog(AddStock.this); // activité non context ..

                        progress.setTitle("Connexion");
                        progress.setMessage("attendez SVP ...");
                        progress.show();
                    }

                    @Override
                    public void After() throws SQLException {
                        //region exportation des productions ..
                            Accueil.BDsql.read("select top 1 RECORDID from BonProductionMobile order by RECORDID desc", new SqlServerBD.doAfterBeforeGettingData() {
                                @Override
                                public void echec(final SQLException e) {
                                    ExportationErr="Erreur de Récupération de RECORDID de BonProductionMobile \n ";

                                }

                                @Override
                                public void before() {

                                }

                                @Override
                                public void After()   {
                                    ResultSet r = Accueil.BDsql.r;
                                    try {
                                        if (r.next()) {
                                            code_prod = Integer.parseInt(r.getString("RECORDID")) + 1;
                                        }
                                        Log.e("recc", "" + code_prod);
                                    }catch (SQLException e){
                                        Synchronisation.ExportationErr=Synchronisation.ExportationErr+" "+e.getMessage();
                                    }

                                }
                            });

                        Accueil.BDsql.es.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("sqll","9bel");
                                final Cursor r2 = Accueil.bd.read("select p.*,cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_debut)) as double) / (3600 * 24) as date_deb" +
                                        " , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_fin)) as double) / (3600 * 24) as date_f" +
                                        " from production p where sync='0' order by date_debut");
                                while (r2.moveToNext()){
                                    Production p2=new Production(r2.getString(r2.getColumnIndex("id_prod")),r2.getString(r2.getColumnIndex("code_pr")),r2.getString(r2.getColumnIndex("code_chef")),r2.getString(r2.getColumnIndex("code_ligne")),r2.getDouble(r2.getColumnIndex("date_deb"))+"",r2.getDouble(r2.getColumnIndex("date_f"))+"",r2.getInt(r2.getColumnIndex("quantité")));
                                    p2.exportéProd(code_prod);
                                    code_prod++;
                                }

                            }
                        });

                        //region afficher msg d err
                        Accueil.BDsql.es.execute(new Runnable() {
                            @Override
                            public void run() {
                                Accueil.BDsql.es.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("sqll","ih");
                                        if(!Synchronisation.ExportationErr.equals("")){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progress.dismiss();
                                                    AlertDialog.Builder mb = new AlertDialog.Builder(AddStock.this); //c est l activity non le context ..

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
                                                            revenirAccueil();
                                                        }
                                                    });

                                                }
                                            });
                                        }else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                                                    revenirAccueil();
                                                }
                                            });
                                        }


                                    }
                                });
                            }
                        });
                        //endregion
/*
                        //region envoyé les données aux tables intermédiaire ..
                        Accueil.BDsql.es.execute(new Runnable() {
                            @Override
                            public void run() {
                                final Cursor r2 = Accueil.bd.read("select p.*,cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_debut)) as double) / (3600 * 24) as date_deb" +
                                        " , cast(-strftime('%s','1900-01-01') + strftime('%s',strftime('%Y-%m-%d %H:%M:00',date_fin)) as double) / (3600 * 24) as date_f" +
                                        " from production p where id_prod='"+p.id_prod+"'");

                                while (r2.moveToNext()) {
                                    Log.e("prodd", String.format("%5f",r2.getDouble(r2.getColumnIndex("date_deb")))+" / "+r2.getFloat(r2.getColumnIndex("date_f")));
                                    HashMap<Integer,String> datas=new HashMap<Integer,String> ();
                                    datas.put(1,r2.getString(r2.getColumnIndex("code_ligne")));

                                        Accueil.BDsql.write2("insert into BonProductionMobile (RECORDID,CODE_BARRE,LIGNE_PRODUCTION,CHEF_LIGNE_PRODUCTION,QTE,DATE_DEBUT,DATE_FIN,BLOCAGE,TEMPS_ARRET) values ('" + code_prod + "','" + r2.getString(r2.getColumnIndex("code_pr")) + "',?,'" + r2.getString(r2.getColumnIndex("code_chef")) + "','" + r2.getString(r2.getColumnIndex("quantité")) + "',CAST(" +r2.getDouble(r2.getColumnIndex("date_deb"))+ " as datetime),CAST(" + r2.getDouble(r2.getColumnIndex("date_f")) + " as datetime),'F','0') ",datas
                                                , new SqlServerBD.doAfterBeforeGettingData() {
                                                    @Override
                                                    public void echec(SQLException e) {
                                                        Log.e("errr"," rahi goood"+e.getMessage());
                                                        ExportationErr=ExportationErr+"Erreur d insertion dans BonProductionMobile: "+e.getMessage()+" \n ";
                                                    }

                                                    @Override
                                                    public void before() {

                                                    }

                                                    @Override
                                                    public void After()  {
                                                        p.changeState("exporté");

                                                    }
                                                });

                                    //region afficher msg d err
                                    Accueil.BDsql.es.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!ExportationErr.equals("")){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progress.dismiss();
                                                        AlertDialog.Builder mb = new AlertDialog.Builder(AddStock.this); //c est l activity non le context ..

                                                        View v= getLayoutInflater().inflate(R.layout.div_aff_msg_err,null);
                                                        TextView msg=(TextView) v.findViewById(R.id.err_msg);
                                                        final TextView num=(TextView) v.findViewById(R.id.ok);

                                                        msg.setText(""+ExportationErr);

                                                        mb.setView(v);
                                                        final AlertDialog ad=mb.create();
                                                        ad.show();
                                                        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                                                        ad.setCancelable(false); //désactiver le button de retour ..

                                                    }
                                                });
                                            }else {
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                                            }

                                            revenirAccueil();
                                        }
                                    });
                                    //endregion

                                }
                            }
                        });
                        //endregion
                        */
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
    //endregion

    //region ouverture de div de scanner ..
    public void openDivScanner() {

        //region afficher le div de scanner ..
        AlertDialog.Builder mb = new AlertDialog.Builder(AddStock.this); //c est l activity non le context ..

        final View v = getLayoutInflater().inflate(R.layout.div_scan, null);
        TextView titre = (TextView) v.findViewById(R.id.div_scan_titre);
        titre.setText(getResources().getString(R.string.add_pr_codebar_hint));
        final EditText code = (EditText) v.findViewById(R.id.div_scan_code);

        mb.setView(v);
        ad = mb.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false);

        code.setFocusable(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            code.setShowSoftInputOnFocus(false);
        }
        //endregion

        //region configuration apres scanner et validation ""..
        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                //mettre ton code ici ..
                code_bar.setText(s.toString().substring(0,s.toString().length()-1));
                Cursor r=Accueil.bd.read("select * from produit where codebar='"+s.toString().substring(0,s.toString().length()-1)+"' order by codebar desc limit 1");
                if(r.moveToNext()){
                    //testé si le 2eme scan , si le produit n est pa changé ..
                    if (getIntent().getExtras().getString("request").equals("second-scan")){

                        Production p=new Production(getIntent().getExtras().getString("id_prod")+"");
                        if(p.produitExisteInProd(s.toString().substring(0,s.toString().length()-1))){
                            selectPr=true;
                            qt.setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.add_stock_depot)).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.add_pr_nom)).setText(r.getString(r.getColumnIndex("nom_pr")));

                        }else {
                            code_bar.setText("");
                            Toast.makeText(getApplicationContext(),"Ce produit n'appartient pas a cette production , veuillez scanner le bon produit ! ",Toast.LENGTH_SHORT).show();
                        }

                    }else if(getIntent().getExtras().getString("request").equals("cummule")){
                        Production p=new Production(getIntent().getExtras().getString("id_prod")+"");
                        if(p.produitExisteInProd(s.toString().substring(0,s.toString().length()-1))){
                            selectPr=true;
                            qt.setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.add_pr_nom)).setText(r.getString(r.getColumnIndex("nom_pr")));

                        }else {
                            code_bar.setText("");
                            Toast.makeText(getApplicationContext(),"Ce produit n'appartient pas a cette production , veuillez scanner le bon produit ! ",Toast.LENGTH_SHORT).show();
                        }
                    }else {

                        selectPr = true;
                        ((TextView) findViewById(R.id.add_pr_nom)).setText(r.getString(r.getColumnIndex("nom_pr")));

                    }
                }else {
                    code_bar.setText("");
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_pr_err),Toast.LENGTH_SHORT).show();
                }

                //region add  prod to BD ..
                ((TextView)findViewById(R.id.add_pr_valider)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!selectPr){
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_valider_pr_vide),Toast.LENGTH_SHORT).show();
                        }else {
                            if(getIntent().getExtras().getString("request").equals("first-scan")){
                                addToBD(code_bar.getText().toString(),0);
                            }else {
                                if(!qt.getText().toString().equals("") && Integer.parseInt(qt.getText().toString()) > 0){
                                    //valider l ajout ..
                                    addToBD(code_bar.getText().toString(),Integer.parseInt(qt.getText().toString()));

                                }else {
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_pr_valider_qt_vide),Toast.LENGTH_SHORT).show();

                                }
                            }

                        }
                    }
                });

                //endregion

                ad.dismiss();


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //endregion

    }
    //endregion

    //region tester si le produit existe dans la bd ..
    public  boolean prExiste(String code){
        Cursor r=Accueil.bd.read("select * from produit where codebar='"+code+"'");
        boolean existe=false;
        if(existe=r.moveToNext() == true){
        }
        return  existe;
    }
    //endregion

    public String getNomchef(String code){
        Cursor r=Accueil.bd.read("select nom from chef_ligne where code_chef='"+code+"'");
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("nom"));
        }
        return "";
    }

    //endregion

    public void revenirAccueil(){
        //region revenir au Accueil ..
        Intent intent = new Intent(getApplicationContext(), Accueil.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //endregion
    }

    @Override
    public void onBackPressed() {
        if(prodTerminé){
            revenirAccueil();
        }else {
            super.onBackPressed();
        }
    }
}
