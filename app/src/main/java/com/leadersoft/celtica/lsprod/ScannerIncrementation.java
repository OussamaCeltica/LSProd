package com.leadersoft.celtica.lsprod;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ScannerIncrementation extends AppCompatActivity {


    String CurrentCodebare="";
    EditText codebar;
    TextView qtView;
    boolean firstCodeSelected=false;//pour tester si on a scanner un produit donc on ne le change pas
    int dejaScan=0;
    int qt=0;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_incrementation);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {
            codebar = (EditText) findViewById(R.id.scann_incr_codebar);
            qtView = (TextView) findViewById(R.id.scann_incr_qt);


            //region manipuler l incrémentation ..
            codebar.setShowSoftInputOnFocus(false);
            codebar.setFocusable(true);
            codebar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                    dejaScan++;
                    if (dejaScan == 2) {
                        Log.e("codd", " pr=" + s.toString().substring(0, s.length() - 1));
                        //region tester si le produit existe
                        if (prExiste(s.toString().substring(0, s.length() - 1))) {
                            //region tester si  c est le mm produit ..
                            if (!firstCodeSelected) {
                                firstCodeSelected = true;
                                CurrentCodebare = s.toString().substring(0, s.length() - 1);
                                qt = qt + 1;
                            } else {
                                if (CurrentCodebare.equals(s.toString().substring(0, s.length() - 1))) {
                                    qt = qt + 1;
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_pr_err), Toast.LENGTH_SHORT).show();

                                }
                            }
                            //endregion

                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_pr_incr_changePrErr), Toast.LENGTH_SHORT).show();

                        }
                        //endregion

                        codebar.setText("");

                        qtView.setText("" + qt);

                        dejaScan = 0;
                    }


                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            //endregion

            //region valider le stock ..
            ((TextView) findViewById(R.id.scann_incr_valider)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (qt != 0) {
                        Intent i = new Intent();
                        i.putExtra("qt", "" + qt);
                        i.putExtra("code", CurrentCodebare);
                        setResult(RESULT_OK, i);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.remplissage_err), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //endregion


        }

    }

    public  boolean prExiste(String code){
        Cursor r=Accueil.bd.read("select * from produit where codebar='"+code+"'");
        boolean existe=false;
        if(existe=r.moveToNext() == true){
            ((TextView)findViewById(R.id.scann_incr_nom_pr)).setText(r.getString(r.getColumnIndex("nom_pr")));
        }
        return  existe;
    }

}
