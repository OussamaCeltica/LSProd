package com.leadersoft.celtica.lsprod;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceConfig extends AppCompatActivity {
     Spinner type;
     TextView code_chef,code_ligne;
     EditText mdp_chef;
     public static AppCompatActivity me;

     public static Session session;

     int x=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_config);


        //region testé si il ya déja une session ..
        Cursor r=Accueil.bd.read("select * from session");
        if(r.moveToNext()){
            session=new Session(r.getString(r.getColumnIndex("code_chef")),r.getString(r.getColumnIndex("code_ligne")),r.getString(r.getColumnIndex("type_device")));
            finish();
        }
        //endregion
        me=this;

        code_chef=(TextView)findViewById(R.id.config_code_chef);
        code_ligne=(TextView)findViewById(R.id.config_code_ligne);
        mdp_chef=((EditText)findViewById(R.id.config_mdp));

        //region configuration de code chef ..
        code_chef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                afficherDivScan("Scanner le code de chef", code_chef,2);
            }
        });

        //endregion

        //region configuration de code ligne ..
        code_ligne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                afficherDivScan("Scanner le code de ligne", code_ligne,3);
            }
        });

        //endregion

        //region configuration de spinner de type de device ..
        type=(Spinner)findViewById(R.id.config_type_device);
        type.setPrompt("select type device");
        String[] wil ={getResources().getString(R.string.config_type_scanner),getResources().getString(R.string.config_type_sansscanner)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, wil);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int y, long l) {
                //lors de changement de selection ..
                ((TextView)type.getSelectedView()).setTextColor(getResources().getColor(R.color.White)); // set text color of selected item ..

                type.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //endregion

        //region connecter ..
        ((TextView)findViewById(R.id.config_connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(code_chef.getText().toString().equals("") || code_ligne.getText().toString().equals("") || mdp_chef.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.config_remplissage_err),Toast.LENGTH_SHORT).show();
                }else {
                    //region test si le chef et la ligne existe ..
                    Cursor r2=Accueil.bd.read("select * from chef_ligne where code_chef='"+code_chef.getText().toString()+"'");
                    if(r2.moveToNext()){
                        if(Accueil.bd.read("select * from ligne_production where code_ligne='"+code_ligne.getText().toString()+"' ").moveToNext()){

                            if(r2.getString(r2.getColumnIndex("mdp_chef")).equals(mdp_chef.getText().toString())){
                                Accueil.bd.write("insert into session (code_chef,code_ligne,type_device) values('"+code_chef.getText().toString()+"','"+code_ligne.getText().toString()+"','"+type.getSelectedItem().toString()+"')");
                                session=new Session(code_chef.getText().toString(),code_ligne.getText().toString(),type.getSelectedItem().toString());
                                startActivity(new Intent(DeviceConfig.this,Accueil.class));

                            }else {
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.config_mdp_err),Toast.LENGTH_SHORT).show();

                            }
                        }else {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.code_ligne_err),Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.code_chef_err),Toast.LENGTH_SHORT).show();

                    }
                    //endregion
                }
            }
        });
        //endregion
    }

    //region afficher le div de scanner de code ligne et code chef ..
    public void afficherDivScan(String desc, final TextView vv,int codeReq){
        if(type.getSelectedItem().toString().equals(getResources().getString(R.string.config_type_scanner))){
            AlertDialog.Builder mb = new AlertDialog.Builder(DeviceConfig.this); //c est l activity non le context ..

            final View v= getLayoutInflater().inflate(R.layout.div_scan,null);
            TextView titre=(TextView) v.findViewById(R.id.div_scan_titre);
            titre.setText(desc);
            final EditText code=(EditText)v.findViewById(R.id.div_scan_code);

            mb.setView(v);
            final AlertDialog ad=mb.create();
            ad.show();

            code.setFocusable(true);

            //region Fermer le clavier ..
            View view  = getCurrentFocus();
            if (view != null) {
                view.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                //on peut remplace le view par l id de notre EditText ..
            }
            //endregion

            code.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    //mettre ton code ici ..
                    vv.setText(s.toString().substring(0,s.toString().length()-1));

                    ad.dismiss();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


        }else {
            if(vv == code_chef) {
                startActivityForResult(new Intent(DeviceConfig.this, DeviceConfigScanner.class), 2);
            }else {
                startActivityForResult(new Intent(DeviceConfig.this, DeviceConfigScanner.class), 3);

            }
        }


    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK

                code_chef.setText(data.getStringExtra("code"));

            }
        }else {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
                code_ligne.setText(data.getStringExtra("code"));

            }
        }
    }
}
