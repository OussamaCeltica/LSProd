package com.leadersoft.celtica.lsprod;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Productions.AfficherStock;

import java.sql.SQLException;

public class Parametrage extends AppCompatActivity {

    private EditText nom,mdp;
    private LinearLayout div_admin;
    private ScrollView div_param;
    private LinearLayout div_sql;
    ProgressDialog progress;

    EditText ip,port,bd,user,mdp2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametrage);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            progress = new ProgressDialog(Parametrage.this);

            //region Conexion d admin ..
            div_admin = (LinearLayout) findViewById(R.id.param_div_admin);
            div_param = (ScrollView) findViewById(R.id.param_div_param);



            nom = (EditText) findViewById(R.id.param_admin_nom);
            mdp = (EditText) findViewById(R.id.param_admin_mdp);
            ((TextView) findViewById(R.id.param_admin_conn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nom.getText().toString().equals("") || mdp.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.remplissage_err), Toast.LENGTH_SHORT).show();
                    } else {
                        Cursor r = Accueil.bd.read("select * from admin where pseudo='" + nom.getText().toString().replaceAll("'", "") + "'");
                        if (r.moveToNext()) {
                            if (r.getString(r.getColumnIndex("mdp")).equals(mdp.getText().toString().replaceAll("'", ""))) {
                                div_admin.setVisibility(View.GONE);
                                div_param.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_mdp_err), Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_user_err), Toast.LENGTH_SHORT).show();

                        }

                    }

                }
            });

            //endregion

            //region autoriser lot ..
            ((LinearLayout)findViewById(R.id.param_lot)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..

                    View v= getLayoutInflater().inflate(R.layout.div_autorise_lot,null);
                    RadioGroup check=(RadioGroup) v.findViewById(R.id.check_lot);
                    RadioButton oui=(RadioButton)v.findViewById(R.id.lot_oui);
                    RadioButton non=(RadioButton)v.findViewById(R.id.lot_non);
                    mb.setView(v);
                    final AlertDialog ad=mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                    //ad.setCancelable(false); //désactiver le button de retour ..

                    if(DeviceConfig.session.isLot){
                        oui.setChecked(true);
                    }else {
                        non.setChecked(true);
                    }


                    check.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup ch, int i) {
                            if(ch.getCheckedRadioButtonId() == R.id.lot_oui){
                                DeviceConfig.session.changeLotPermission(true);
                            }else {
                                DeviceConfig.session.changeLotPermission(false);
                            }
                            ad.dismiss();
                        }
                    });

                }
            });

            //endregion

            //region archive ..
            ((LinearLayout)findViewById(R.id.param_archive)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                     Intent i=new Intent(Parametrage.this, AfficherStock.class);
                     i.putExtra("request","archive");
                     startActivity(i);

                }
            });
            //endregion

            //region change  type device ..
            ((LinearLayout)findViewById(R.id.param_type_device)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..

                    View v= getLayoutInflater().inflate(R.layout.change_type_device,null);
                    RadioGroup check=(RadioGroup) v.findViewById(R.id.check);
                    RadioButton sans=(RadioButton)v.findViewById(R.id.type_sans);
                    RadioButton avec=(RadioButton)v.findViewById(R.id.type_avec);
                    mb.setView(v);
                    final AlertDialog ad=mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                    //ad.setCancelable(false); //désactiver le button de retour ..

                    if(DeviceConfig.session.type.equals(getResources().getString(R.string.config_type_sansscanner))){
                        sans.setChecked(true);
                    }else {
                        avec.setChecked(true);
                    }


                    check.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup ch, int i) {
                            if(ch.getCheckedRadioButtonId() == R.id.type_avec){
                                DeviceConfig.session.type=getResources().getString(R.string.config_type_scanner);
                                Accueil.bd.write("update admin set type_device='"+getResources().getString(R.string.config_type_scanner)+"'");
                            }else {
                                DeviceConfig.session.type=getResources().getString(R.string.config_type_sansscanner);
                                Accueil.bd.write("update admin set type_device='"+getResources().getString(R.string.config_type_sansscanner)+"'");
                            }
                            ad.dismiss();
                        }
                    });


                }
            });

            //endregion

            //region configure sqlserver
            div_sql=(LinearLayout)findViewById(R.id.DivSqlConnect);
            ((LinearLayout)findViewById(R.id.param_sql_config)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    div_param.setVisibility(View.GONE);
                    div_sql.setVisibility(View.VISIBLE);

                    ip=(EditText)findViewById(R.id.synch_ip);
                    port=(EditText)findViewById(R.id.synch_port);
                    user=(EditText)findViewById(R.id.synch__user);
                    bd=(EditText)findViewById(R.id.synch_bd);
                    mdp2=(EditText)findViewById(R.id.synch_mdp);

                    Cursor r5= Accueil.bd.read("select * from sqlconnect");

                        if(DeviceConfig.session.serveur.ip != null) {
                            ip.setText(DeviceConfig.session.serveur.ip);
                            port.setText(DeviceConfig.session.serveur.port);
                            user.setText(DeviceConfig.session.serveur.user);
                            bd.setText(DeviceConfig.session.serveur.bdName);
                            mdp2.setText(DeviceConfig.session.serveur.mdp);
                        }
                }
            });
            //endregion

            //region connecter sql server
            ((TextView)findViewById(R.id.synch_Butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Accueil.BDsql=new SqlServerBD(ip.getText().toString(), port.getText().toString(), bd.getText().toString(), user.getText().toString(), mdp2.getText().toString(), "net.sourceforge.jtds.jdbc.Driver", new SqlServerBD.doAfterBeforeConnect() {
                            @Override
                            public void echec() {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_err),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void before() {
                                progress.setTitle("Connexion");
                                progress.setMessage("attendez SVP...");
                                progress.show();
                            }

                            @Override
                            public void After() throws SQLException {
                                progress.dismiss();
                                DeviceConfig.session.serveur.majInfos(ip.getText().toString(), port.getText().toString(), bd.getText().toString(), user.getText().toString(), mdp2.getText().toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.sync_conect_succ),Toast.LENGTH_SHORT).show();
                                    }
                                });

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


                }
            });

            //endregion

            //region changer le mot de passe ..
            ((LinearLayout) findViewById(R.id.param_change_adminMdp)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //region Affichage de div de changement ..
                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..
                    View v = getLayoutInflater().inflate(R.layout.change_admin_mdp, null);
                    TextView valider = (TextView) v.findViewById(R.id.param_changeMdp_valider);
                    final EditText nomAdmin = (EditText) v.findViewById(R.id.param_changeMdp_nomAdmin);
                    final EditText newMdp = (EditText) v.findViewById(R.id.param_changeMdp_new);
                    final EditText oldMdp = (EditText) v.findViewById(R.id.param_changeMdp_actuel);
                    mb.setView(v);
                    final AlertDialog ad = mb.create();
                    ad.show();
                    nomAdmin.setText(nom.getText());
                    //endregion

                    //region valider le changement ..
                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (oldMdp.getText().toString().equals("") || newMdp.getText().toString().equals("") || nomAdmin.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.remplissage_err), Toast.LENGTH_SHORT).show();

                            } else {
                                Cursor r = Accueil.bd.read("select * from admin");
                                while (r.moveToNext()) {
                                    if (oldMdp.getText().toString().equals(r.getString(r.getColumnIndex("mdp")))) {
                                        Accueil.bd.write("update admin set pseudo='" + nomAdmin.getText().toString().replaceAll("'", "") + "' , mdp='" + newMdp.getText().toString().replaceAll("'", "") + "'");
                                        ad.dismiss();
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_admin_ChangeMdp_succes), Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.param_admin_ChangeMdp_err), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }

                        }
                    });
                    //endregion
                }
            });
            //endregion

            //region changer le deviceId ..
            ((LinearLayout) findViewById(R.id.param_change_deviceId)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..
                    View v = getLayoutInflater().inflate(R.layout.div_msg_inp, null);
                    TextView valider = (TextView) v.findViewById(R.id.div_msgInp_add);
                    TextView msg = (TextView) v.findViewById(R.id.div_msgInp_msg);
                    final EditText deviceId = (EditText) v.findViewById(R.id.div_msgInp_value);

                    mb.setView(v);
                    final AlertDialog ad = mb.create();
                    ad.show();

                    msg.setText(getResources().getString(R.string.param_deviecId_titre)+"");
                    deviceId.setText(DeviceConfig.session.deviceId+"");

                    //region valider le changement ..
                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (deviceId.getText().toString().equals("")){
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.remplissage_err),Toast.LENGTH_SHORT).show();

                            }else {
                                DeviceConfig.session.changeDeviceId(deviceId.getText().toString());
                                ad.dismiss();
                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.add_ok),Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    //endregion

                }
            });
            //endregion

            //region formater base Donnée ..
            ((LinearLayout)findViewById(R.id.param_formater_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mb = new AlertDialog.Builder(Parametrage.this); //c est l activity non le context ..

                    View v= getLayoutInflater().inflate(R.layout.confirm_box,null);
                    TextView msg=(TextView) v.findViewById(R.id.confirm_msg);
                    TextView oui=(TextView) v.findViewById(R.id.confirm_oui);
                    TextView non=(TextView) v.findViewById(R.id.confirm_non);

                    msg.setText("Voulez vous vraiment supprimez ! ");

                    mb.setView(v);
                    final AlertDialog ad=mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
                    ad.setCancelable(false); //désactiver le button de retour ..

                    oui.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DeviceConfig.session.formaterBD();
                            ad.dismiss();
                            Toast.makeText(getApplicationContext(),"Réinitialisation terminé.",Toast.LENGTH_SHORT).show();
                        }
                    });

                    non.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ad.dismiss();
                        }
                    });

                }
            });

            //endregion

            //region fermer le session actuel de user ..
            ((LinearLayout) findViewById(R.id.param_deconect_butt)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Accueil.bd.write("delete  from session where '1'='1'");
                    Intent intent = new Intent(getApplicationContext(), DeviceConfig2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
            //endregion


        }

    }

    @Override
    public void onBackPressed() {
        if (div_sql.getVisibility()==View.VISIBLE){
            div_sql.setVisibility(View.GONE);
            div_param.setVisibility(View.VISIBLE);
        }else {
            super.onBackPressed();
        }

    }
}
