package com.leadersoft.celtica.lsprod.Preparations;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.Productions.Production;
import com.leadersoft.celtica.lsprod.Productions.ProductionAdapter;
import com.leadersoft.celtica.lsprod.Productions.Produit;
import com.leadersoft.celtica.lsprod.R;

import java.util.ArrayList;

public class PanierPreparationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    AppCompatActivity c;
    public static ArrayList<ProduitPréparé> produits=new ArrayList<ProduitPréparé>();
    public static int itemSelected;

    public PanierPreparationAdapter(AppCompatActivity c) {
        this.c = c;

    }

    public static class ItemView extends RecyclerView.ViewHolder  {
        public TextView nom_pr;
        public TextView nom_depot;
        public TextView num_lot;
        public TextView qt;

        public LinearLayout body;
        public ItemView(View v) {
            super(v);
            nom_pr=(TextView)v.findViewById(R.id.div_pr_panier_pr);
            nom_depot=(TextView)v.findViewById(R.id.div_pr_panier_dep);
            num_lot=(TextView)v.findViewById(R.id.div_pr_panier_lot);
            qt=(TextView)v.findViewById(R.id.div_pr_panier_qt);
            body=(LinearLayout) v.findViewById(R.id.div_pr_panier_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_panier,parent,false);

        ItemView vh = new ItemView(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((ItemView)holder).nom_pr.setText(produits.get(position).designiation);
        ((ItemView)holder).nom_depot.setText(produits.get(position).nom_depot);
        ((ItemView)holder).qt.setText(DeviceConfig.session.formatQt(produits.get(position).qt)+"");


        if(produits.get(position).num_lot.equals("")){
            ((LinearLayout)((ItemView)holder).num_lot.getParent()).setVisibility(View.GONE);
        }else {
            ((LinearLayout)((ItemView)holder).num_lot.getParent()).setVisibility(View.VISIBLE);
            ((ItemView)holder).num_lot.setText(produits.get(position).num_lot);
        }

        ((ItemView)holder).body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected=position;


                if (ProductionAdapter.selectedItem ==-1 || ((Production)ProductionAdapter.Productions.get(ProductionAdapter.selectedItem)).etat == ETAT.EN_COURS){
                    AlertDialog.Builder mb = new AlertDialog.Builder(c); //c est l activity non le context ..

                    View v = c.getLayoutInflater().inflate(R.layout.supp_pr_panier, null);
                    TextView valider = (TextView) v.findViewById(R.id.panier_modif_valider);
                    TextView suppButt = (TextView) v.findViewById(R.id.panier_supp_pr_oui);
                    final EditText qt = (EditText) v.findViewById(R.id.panier_pr_modif_qt);
                    final EditText lot = (EditText) v.findViewById(R.id.panier_pr_modif_lot);
                    if (!DeviceConfig.session.isLot)
                        v.findViewById(R.id.div_change_lot).setVisibility(View.GONE);
                    else
                        lot.setText(produits.get(position).num_lot);


                    mb.setView(v);
                    final AlertDialog ad = mb.create();
                    ad.show();
                    ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..

                    qt.setText(DeviceConfig.session.formatQt(produits.get(position).qt)+ "");

                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!qt.getText().toString().equals("")){
                                produits.get(position).qt = Double.parseDouble(qt.getText().toString());
                                if (DeviceConfig.session.isLot && lot.getText().toString().equals("")){

                                }else {
                                    produits.get(position).num_lot = lot.getText().toString();
                                }
                                notifyDataSetChanged();
                                ad.dismiss();
                            }
                        }
                    });

                    suppButt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            produits.remove(position);
                            notifyDataSetChanged();
                            ad.dismiss();
                        }
                    });
                }

            }

        });




    }

    public double getProduitQt(ProduitPréparé p){
        int i=0;
        boolean existe=false;
        while (i != produits.size() && !existe){
            if(produits.get(i).codebar.equals(p.codebar) ){
                existe=true;
            }else {
                i++;
            }

        }

        if (existe){
            return produits.get(i).qt;
        }
        return 0;
    }

    public void addPrToPanier(ProduitPréparé p){
        int i=0;
        boolean existe=false;
        while (i != produits.size() && !existe){
            if(produits.get(i).codebar.equals(p.codebar) && produits.get(i).code_depot.equals(p.code_depot) && produits.get(i).num_lot.equals("") ){
                existe=true;
            }else {
                i++;
            }

        }

        if (existe){
            produits.get(i).qt+=p.qt;
        }else {
            produits.add(p);
        }
    }

    @Override
    public int getItemCount() {
        return produits.size();
    }
}
