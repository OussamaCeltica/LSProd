    package com.leadersoft.celtica.lsprod.Maintenances;

    import android.content.Intent;
    import android.os.Build;
    import android.support.annotation.RequiresApi;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.RecyclerView;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.LinearLayout;
    import android.widget.TextView;


    import com.leadersoft.celtica.lsprod.ETAT;
    import com.leadersoft.celtica.lsprod.R;

    import java.util.ArrayList;

    /**
    * Created by celtica on 15/08/18.
    */

    public class MaintenanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    AppCompatActivity c;
    public static int selectedItem;
    public static ArrayList<Maintenance> Maintenances=new ArrayList<Maintenance>();

    public MaintenanceAdapter( AppCompatActivity c) {
        this.c = c;

    }

    //region class de vue de production ..
    public static class MaintView extends RecyclerView.ViewHolder  {
        public TextView type;
        public TextView employe;
        public TextView date_debut;
        public TextView date_fin;
        public TextView ligne;
        public TextView msg;
        LinearLayout body;
        public MaintView(View v) {
            super(v);
            type=(TextView)v.findViewById(R.id.div_mantain_type);
            date_debut=(TextView)v.findViewById(R.id.div_mantain_time);
            date_fin=(TextView)v.findViewById(R.id.div_pr_time_fin);
            employe=(TextView)v.findViewById(R.id.div_mantain_emp);
            ligne=(TextView)v.findViewById(R.id.div_maint_ligne);
            msg=(TextView)v.findViewById(R.id.div_maint_msg);
            body=(LinearLayout) v.findViewById(R.id.div_maint_body);
        }
    }

    //endregion





    //region Creation de model de la vue ..
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.div_mantain,parent,false);

                MaintView vh = new MaintView(v);
                return vh;


    }
    //endregion

    //region configuration de la vue lors de scrolling ..
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((MaintView)holder).type.setText(((Maintenance)Maintenances.get(position)).getMotiveFromMaint());
        ((MaintView)holder).employe.setText(((Maintenance)Maintenances.get(position)).getNomEmployeFromMaint()+"");
        ((MaintView)holder).date_debut.setText(((Maintenance)Maintenances.get(position)).date_debut);
        ((MaintView)holder).date_fin.setText(((Maintenance)Maintenances.get(position)).date_fin);
        ((MaintView)holder).ligne.setText(((Maintenance)Maintenances.get(position)).getNomLigneFromMaint()+"");

        if (Maintenances.get(position).etat == ETAT.EN_COURS){
            ((MaintView)holder).msg.setText(c.getResources().getString(R.string.etat_enCours));
            ((MaintView)holder).msg.setVisibility(View.VISIBLE);
            ((MaintView)holder).date_debut.setBackgroundColor(c.getResources().getColor(R.color.AppColor));
            ((MaintView)holder).date_fin.setBackgroundColor(c.getResources().getColor(R.color.AppColor));

            ((MaintView)holder).body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItem=position;
                    Intent i=new Intent(c,AddMaintenance.class);
                    i.putExtra("request","second-scan");
                    i.putExtra("id",Maintenances.get(position).id);
                    i.putExtra("type_maint",Maintenances.get(position).getMotiveFromMaint());

                    c.startActivity(i);
                }
            });
        }
        else {
            ((MaintView)holder).msg.setVisibility(View.GONE);
            ((MaintView)holder).date_debut.setBackground(c.getResources().getDrawable(R.drawable.bg_export));
            ((MaintView)holder).date_fin.setBackground(c.getResources().getDrawable(R.drawable.bg_supp));

            if(Maintenances.get(position).etat == ETAT.EXPORTÉ){
                ((MaintView)holder).msg.setText(c.getResources().getString(R.string.etat_exporté));
                ((MaintView)holder).msg.setVisibility(View.VISIBLE);
                ((MaintView)holder).msg.setBackground(c.getResources().getDrawable(R.drawable.bg_export));
            }else if (Maintenances.get(position).etat == ETAT.SUPPRIMÉ){
                ((MaintView)holder).msg.setText(c.getResources().getString(R.string.etat_supprimé));
                ((MaintView)holder).msg.setVisibility(View.VISIBLE);
                ((MaintView)holder).msg.setBackground(c.getResources().getDrawable(R.drawable.bg_supp));
            }else {
                ((MaintView)holder).msg.setVisibility(View.GONE);
            }
        }

    }
    //endregion

    @Override
    public int getItemCount() {
        return Maintenances.size();
    }
    }
