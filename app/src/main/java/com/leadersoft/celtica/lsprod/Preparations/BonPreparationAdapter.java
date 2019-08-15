package com.leadersoft.celtica.lsprod.Preparations;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.R;

import java.util.ArrayList;

public class BonPreparationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    AppCompatActivity c;
    public static ArrayList<BonPreparation> bons=new ArrayList<BonPreparation>();
    public static int itemSelected;
    ETAT etat=ETAT.EN_COURS;

    public BonPreparationAdapter(AppCompatActivity c) {
        this.c = c;

    }

    public static class BonView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView id_bon;
        public TextView date_bon;
        public TextView msg;

        public LinearLayout body;
        public LinearLayout valider;
        public BonView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.divPrep_pr);
            date_bon=(TextView)v.findViewById(R.id.divPrep_date);
            id_bon=(TextView)v.findViewById(R.id.divPrep_bon);
            msg=(TextView)v.findViewById(R.id.divPrep_msg);
            body=(LinearLayout) v.findViewById(R.id.divPrep_body);
            valider=(LinearLayout) v.findViewById(R.id.divPrep_terminerButt);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_prep_exporte,parent,false);

          BonView vh = new BonView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((BonView)holder).nom_pr.setText(bons.get(position).nom_pr);
        ((BonView)holder).date_bon.setText(bons.get(position).date);
        ((BonView)holder).id_bon.setText(bons.get(position).id_bon);

        ((BonView)holder).valider.setVisibility(View.GONE);

        //region configurer la bar d etat ..
        if(bons.get(position).etat == ETAT.VALIDÉ){
            ((BonView)holder).msg.setVisibility(View.GONE);
        }else if(bons.get(position).etat == ETAT.EN_COURS){
            ((BonView)holder).valider.setVisibility(View.VISIBLE);
            ((BonView)holder).msg.setBackgroundColor(c.getResources().getColor(R.color.Red));
            ((BonView)holder).msg.setText("EN COURS");
        }else if(bons.get(position).etat == ETAT.EXPORTÉ){
            ((BonView)holder).msg.setText("EXPORTÉ");
        }
        //endregion

        //region afficher les produit a préparer ..
        ((BonView)holder).body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected = position;
                PanierPreparationAdapter.produits=bons.get(position).getProduitPreparer();
                Intent i=new Intent(c,FairePrep.class);
                if(bons.get(position).etat == ETAT.VALIDÉ){
                    i.putExtra("request","bon_validé");
                }else if(bons.get(position).etat == ETAT.EN_COURS){
                    i.putExtra("request","en cours");
                }else if(bons.get(position).etat == ETAT.EXPORTÉ){
                    i.putExtra("request","archive");
                }
                c.startActivity(i);
            }
        });
        //endregion

        //region valider un bon ..
        ((BonView)holder).valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bons.get(position).validerBon();
                Toast.makeText(c, c.getResources().getString(R.string.add_ok), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(c, Accueil.class);
                Intent i2 = new Intent(c, AfficherPreparations.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                c.startActivities(new Intent[]{intent, i2});
            }
        });
        //endregion


    }



    @Override
    public int getItemCount() {
        return bons.size();
    }
}
