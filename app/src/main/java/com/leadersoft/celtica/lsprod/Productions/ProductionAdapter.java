package com.leadersoft.celtica.lsprod.Productions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;


import com.leadersoft.celtica.lsprod.Preparations.AfficherPreparations;
import com.leadersoft.celtica.lsprod.R;

import java.util.ArrayList;

/**
 * Created by celtica on 15/08/18.
 */

public class ProductionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity c;
    public static int selectedItem;
    public static ArrayList<PublicationProduction> Productions=new ArrayList<PublicationProduction>();

    public ProductionAdapter( AppCompatActivity c) {
        this.c = c;

    }

    //region class de vue de production ..
    public static class ProductionView extends RecyclerView.ViewHolder  {
        public TextView codebar;
        public TextView qt;
        public TextView date_debut;
        public TextView date_fin;
        public TextView ligne;
        public LinearLayout divPr;
        public ProductionView(View v) {
            super(v);
            codebar=(TextView)v.findViewById(R.id.div_pr_codebar);
            date_debut=(TextView)v.findViewById(R.id.div_pr_time);
            date_fin=(TextView)v.findViewById(R.id.div_pr_time_fin);
            qt=(TextView)v.findViewById(R.id.div_pr_qt);
            ligne=(TextView)v.findViewById(R.id.div_pr_ligne);
            divPr=(LinearLayout)v.findViewById(R.id.div_Pr);
        }
    }

    //endregion

    //region class de vue de production en cours ..
    public static class ProdEnCoursView extends RecyclerView.ViewHolder  {

        public TextView codebar;
        public TextView ligne;
        public TextView msg;
        public TextView qt;
        public TextView date_debut;
        public TextView date_fin;
        public LinearLayout divPr;
        public ProdEnCoursView(View v) {
            super(v);
            msg=(TextView)v.findViewById(R.id.div_prSupp_msg);
            codebar=(TextView)v.findViewById(R.id.div_pr_codebar);
            date_debut=(TextView)v.findViewById(R.id.div_pr_time);
            date_fin=(TextView)v.findViewById(R.id.div_pr_time_fin);
            qt=(TextView)v.findViewById(R.id.div_pr_qt);
            ligne=(TextView)v.findViewById(R.id.div_pr_ligne);
            divPr=(LinearLayout)v.findViewById(R.id.div_Pr_supp);
        }
    }
    //endregion

    //region class de vue de arret de production ..
    public static class ProdEnArretView extends RecyclerView.ViewHolder  {

        public TextView codebar;
        public TextView ligne;
        public TextView msg;
        public TextView qt;
        public TextView date_debut;
        public TextView date_fin;
        public LinearLayout divPr;
        public ProdEnArretView(View v) {
            super(v);
            msg=(TextView)v.findViewById(R.id.div_prSupp_msg);
            codebar=(TextView)v.findViewById(R.id.div_pr_codebar);
            date_debut=(TextView)v.findViewById(R.id.div_pr_time);
            date_fin=(TextView)v.findViewById(R.id.div_pr_time_fin);
            qt=(TextView)v.findViewById(R.id.div_pr_qt);
            ligne=(TextView)v.findViewById(R.id.div_pr_ligne);
            divPr=(LinearLayout)v.findViewById(R.id.div_Pr_supp);
        }
    }
    //endregion

    //region class de vue de  production exporté ..
    public static class ProdExportéView extends RecyclerView.ViewHolder  {

        public TextView codebar;
        public TextView ligne;
        public TextView msg;
        public TextView qt;
        public TextView date_debut;
        public TextView date_fin;
        public LinearLayout divPr;
        public ProdExportéView(View v) {
            super(v);
            msg=(TextView)v.findViewById(R.id.div_prSupp_msg);
            codebar=(TextView)v.findViewById(R.id.div_pr_codebar);
            date_debut=(TextView)v.findViewById(R.id.div_pr_time);
            date_fin=(TextView)v.findViewById(R.id.div_pr_time_fin);
            qt=(TextView)v.findViewById(R.id.div_pr_qt);
            ligne=(TextView)v.findViewById(R.id.div_pr_ligne);
            divPr=(LinearLayout)v.findViewById(R.id.div_Pr_exp);
        }
    }
    //endregion

    //region class de vue de  production supprimé ..
    public static class ProdSuppView extends RecyclerView.ViewHolder  {

        public TextView codebar;
        public TextView ligne;
        public TextView msg;
        public TextView qt;
        public TextView date_debut;
        public TextView date_fin;
        public LinearLayout divPr;
        public ProdSuppView(View v) {
            super(v);
            msg=(TextView)v.findViewById(R.id.div_prSupp_msg);
            codebar=(TextView)v.findViewById(R.id.div_pr_codebar);
            date_debut=(TextView)v.findViewById(R.id.div_pr_time);
            date_fin=(TextView)v.findViewById(R.id.div_pr_time_fin);
            qt=(TextView)v.findViewById(R.id.div_pr_qt);
            ligne=(TextView)v.findViewById(R.id.div_pr_ligne);
            divPr=(LinearLayout)v.findViewById(R.id.div_Pr_supp);
        }
    }
    //endregion

    //region referencier le type de vue ..
    @Override
    public int getItemViewType(int i) {


        switch (Productions.get(i).type.toLowerCase()) {
            case "production": {return 1;}
            case "en_cours": {return 2;}
            case "en_arret": {return 3;}
            case "exporté": {return 4;}
            case "supprimé": {return 5;}
            default:return -1;
        }



    }
    //endregion

    //region Creation de model de la vue ..
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType){
            case 1:{
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr,parent,false);

                ProductionView vh = new ProductionView(v);
                return vh;
            }
            case 2: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_supp,parent,false);

                ProdEnCoursView vh = new ProdEnCoursView(v);
                return vh;
            }
            case 3: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_supp, parent, false);

                ProdEnArretView vh = new ProdEnArretView(v);
                return vh;
            }
            case 4:{
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_export, parent, false);

                ProdExportéView vh = new ProdExportéView(v);
                return vh;
            }
            case 5:{
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_pr_supp, parent, false);

                ProdSuppView vh = new ProdSuppView(v);
                return vh;
            }
            default: return null;
        }
    }
    //endregion

    //region configuration de la vue lors de scrolling ..
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        switch (Productions.get(position).type){
            case "production": {
                ((ProductionView)holder).codebar.setText(((Production)Productions.get(position)).nomPr+"");
                ((ProductionView)holder).date_debut.setText(((Production)Productions.get(position)).heure);
                ((ProductionView)holder).date_fin.setText(((Production)Productions.get(position)).heure_fin);
                ((ProductionView)holder).ligne.setText(((Production)Productions.get(position)).code_ligne);

                if(((Production) Productions.get(position)).isPackaging){
                    ((ProductionView)holder).qt.setText(((Production)Productions.get(position)).qt+" "+c.getResources().getString(R.string.add_pr_packaging_carton));
                }else {
                    ((ProductionView)holder).qt.setText(((Production)Productions.get(position)).qt+" "+c.getResources().getString(R.string.add_pr_packaging_unité));
                }
                ((ProductionView)holder).divPr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedItem=position;
                        Intent i=new Intent(c,UneProduction.class);
                        i.putExtra("request","production");
                        c.startActivity(i);
                    }
                });
                break;
            }

            case "en_cours":{
                ((ProdEnCoursView)holder).codebar.setText(((Production)Productions.get(position)).nomPr);
                ((ProdEnCoursView)holder).qt.setText(((Production)Productions.get(position)).qt+"");
                ((ProdEnCoursView)holder).date_debut.setText(((Production)Productions.get(position)).heure);
                ((ProdEnCoursView)holder).date_debut.setBackgroundColor(c.getResources().getColor(R.color.AppColor));
                ((ProdEnCoursView)holder).date_fin.setText(((Production)Productions.get(position)).heure_fin);
                ((ProdEnCoursView)holder).date_fin.setBackgroundColor(c.getResources().getColor(R.color.AppColor));
                ((ProdEnCoursView)holder).ligne.setText(((Production)Productions.get(position)).code_ligne);
                ((ProdEnCoursView)holder).msg.setText("EN COURS");

                ((ProdEnCoursView)holder).divPr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedItem=position;
                        Intent i=new Intent(c,FaireProdMode.class);

                            i.putExtra("request","second-scan");
                            Log.e("ppp",((Production) Productions.get(position)).isPrep+"");
                            i.putExtra("produit",((Production) Productions.get(position)).nomPr);
                            i.putExtra("id_prod",((Production) Productions.get(position)).id_prod);

                        c.startActivity(i);
                    }
                });

                ((ProdEnCoursView)holder).divPr.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        PopupMenu popup = new PopupMenu(c,((ProdEnCoursView)holder).ligne);

                        //popup.getMenu().add("Archive Préparation");
                        popup.getMenu().add("Supprimer");


                        //registering popup with OnMenuItemClickListener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().toString().equals("Supprimer")){
                                    ((Production) Productions.get(position)).suppProd();
                                     Productions.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, getItemCount() - position);

                                }

                                return true;
                            }
                        });

                        popup.show();//showing popup menu
                        return true;
                    }
                });
                break;

            }
            case "en_arret":{
                ((ProdEnArretView)holder).codebar.setText(((Production)Productions.get(position)).nomPr);
                ((ProdEnArretView)holder).qt.setText(((Production)Productions.get(position)).qt+"");
                ((ProdEnArretView)holder).date_debut.setText(((Production)Productions.get(position)).heure);
                ((ProdEnArretView)holder).date_debut.setBackgroundColor(c.getResources().getColor(R.color.AppColor));
                ((ProdEnArretView)holder).date_fin.setText(((Production)Productions.get(position)).heure_fin);
                ((ProdEnArretView)holder).date_fin.setBackgroundColor(c.getResources().getColor(R.color.AppColor));
                ((ProdEnArretView)holder).ligne.setText(((Production)Productions.get(position)).code_ligne);
                ((ProdEnArretView)holder).msg.setText("EN PAUSE");

                ((ProdEnArretView)holder).divPr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedItem=position;
                        Intent i=new Intent(c,FaireProdMode.class);

                        i.putExtra("request","lancer-prod");
                        i.putExtra("produit",((Production) Productions.get(position)).nomPr);
                        i.putExtra("id_prod",((Production) Productions.get(position)).id_prod);

                        c.startActivity(i);
                    }
                });

                break;
            }
            case "exporté":{
                ((ProdExportéView)holder).codebar.setText(((Production)Productions.get(position)).nomPr+"");
                ((ProdExportéView)holder).date_debut.setText(((Production)Productions.get(position)).heure);
                ((ProdExportéView)holder).date_fin.setText(((Production)Productions.get(position)).heure_fin);
                ((ProdExportéView)holder).ligne.setText(((Production)Productions.get(position)).code_ligne);

                if(((Production) Productions.get(position)).isPackaging){
                    ((ProdExportéView)holder).qt.setText(((Production)Productions.get(position)).qt+" "+c.getResources().getString(R.string.add_pr_packaging_carton));
                }else {
                    ((ProdExportéView)holder).qt.setText(((Production)Productions.get(position)).qt+" "+c.getResources().getString(R.string.add_pr_packaging_unité));
                }
                ((ProdExportéView)holder).divPr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedItem=position;
                        Intent i=new Intent(c,UneProduction.class);
                        i.putExtra("request","archive");
                        c.startActivity(i);
                    }
                });
                break;
            }
            case "supprimé":{
                ((ProdSuppView)holder).codebar.setText(((ProductionSupprimé)Productions.get(position)).nomPr+"");
                ((ProdSuppView)holder).date_debut.setText(((ProductionSupprimé)Productions.get(position)).heure);
                ((ProdSuppView)holder).date_fin.setText(((ProductionSupprimé)Productions.get(position)).heure_fin);
                ((ProdSuppView)holder).ligne.setText(((ProductionSupprimé)Productions.get(position)).code_ligne);

                if(((Production) Productions.get(position)).isPackaging){
                    ((ProdSuppView)holder).qt.setText(((Production)Productions.get(position)).qt+" "+c.getResources().getString(R.string.add_pr_packaging_carton));
                }else {
                    ((ProdSuppView)holder).qt.setText(((Production)Productions.get(position)).qt+" "+c.getResources().getString(R.string.add_pr_packaging_unité));
                }
                ((ProdSuppView)holder).divPr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedItem=position;
                        Intent i=new Intent(c,UneProduction.class);
                        i.putExtra("request","archive");
                        c.startActivity(i);
                    }
                });
                break;
            }
        }

    }
    //endregion

    @Override
    public int getItemCount() {
        return Productions.size();
    }
}
