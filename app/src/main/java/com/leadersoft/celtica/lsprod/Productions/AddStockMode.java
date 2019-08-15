package com.leadersoft.celtica.lsprod.Productions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.MySpinner.MySpinnerSearchable;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem;
import com.leadersoft.celtica.lsprod.MySpinner.SpinnerItem3;
import com.leadersoft.celtica.lsprod.R;

import java.util.ArrayList;

public class AddStockMode extends AppCompatActivity {
    MySpinnerSearchable spinnerTypePause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock_mode);



        if (savedInstanceState != null) {
            //region Revenir a au Deviceconfig ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {
            final String request=getIntent().getExtras().getString("request");

            //region add par first/second scan ..
            ((TextView)findViewById(R.id.add_stock_par_incr)).setVisibility(View.GONE);
            if(request.equals("second-scan")){

                ((TextView)findViewById(R.id.add_stock_par_qt)).setText("Dernier scanner");

                ((TextView)findViewById(R.id.pr_label)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.add_stock_mode_nom_pr)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.add_stock_mode_nom_pr)).setText(getIntent().getExtras().getString("produit"));

                //region cummulé le stock (deprecated) ..
                /*
                ((TextView)findViewById(R.id.add_stock_cumule_qt)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.add_stock_cumule_qt)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i=new Intent(AddStockMode.this,AddStock.class);
                        i.putExtra("mode","quantité");
                        i.putExtra("id_prod",getIntent().getExtras().getString("id_prod"));
                        i.putExtra("request","cummule");
                        startActivity(i);
                        finish();
                    }
                });
                */
                //endregion

                //region mettre en pause production ..
                ((TextView)findViewById(R.id.add_stock_stopProd)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.add_stock_stopProd)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        /*
                        AlertDialog.Builder mb = new AlertDialog.Builder(AddStockMode.this); //c est l activity non le context ..

                        View v= getLayoutInflater().inflate(R.layout.confirm_box,null);
                        TextView msg=(TextView) v.findViewById(R.id.confirm_msg);
                        TextView oui=(TextView) v.findViewById(R.id.confirm_oui);
                        TextView non=(TextView) v.findViewById(R.id.confirm_non);

                        msg.setText("Voulez vraiment mettre en pause la production ? ");
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
                                ProductionEnCours p=new ProductionEnCours(getIntent().getExtras().getString("id_prod"));
                                p.arreterProd("");
                                Toast.makeText(getApplicationContext(),"La production est en pause.",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Accueil.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                        */

                        ArrayList<SpinnerItem> types=new ArrayList<SpinnerItem>();
                        types.add(new SpinnerItem("","Coupure D electricité"));
                        spinnerTypePause=new MySpinnerSearchable(AddStockMode.this, types, "Séléctionner le type de pause ..", new MySpinnerSearchable.SpinnerConfig() {
                            @Override
                            public void onChooseItem(int pos, SpinnerItem item) {
                                spinnerTypePause.closeSpinner();
                            }
                        });

                        spinnerTypePause.openSpinner();




                    }
                });
                //endregion

            }else if (request.equals("lancer-prod")){
                ((TextView)findViewById(R.id.add_stock_par_incr)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.add_stock_par_qt)).setVisibility(View.GONE);
                TextView relancerProd=((TextView)findViewById(R.id.add_stock_stopProd));
                relancerProd.setVisibility(View.VISIBLE);
                relancerProd.setText("Relancer la production");

                ((TextView)findViewById(R.id.pr_label)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.add_stock_mode_nom_pr)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.add_stock_mode_nom_pr)).setText(getIntent().getExtras().getString("produit"));

                relancerProd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

            }
            ((TextView)findViewById(R.id.add_stock_par_qt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(AddStockMode.this,AddStock.class);
                    i.putExtra("mode","quantité");
                    i.putExtra("id_prod",getIntent().getExtras().getString("id_prod"));
                    i.putExtra("request",request);
                    startActivity(i);
                    finish();
                }
            });
            //endregion

            //region add par incrémentation ..
            ((TextView)findViewById(R.id.add_stock_par_incr)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(AddStockMode.this,AddStock.class);
                    i.putExtra("mode","incrémentation");
                    i.putExtra("request",request);
                    startActivity(i);
                    finish();
                }
            });
            //endregion
        }


    }

}
