package com.leadersoft.celtica.lsprod.MySpinner;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadersoft.celtica.lsprod.R;

import java.util.ArrayList;

/**
 * Created by celtica on 30/11/18.
 */

public class MySpinnerSearchable {

    private ArrayList<SpinnerItem> SpinnerItems;
    private SpinnerConfig sc;
    private ButtonSpinnerOnClick click;
    private boolean withButt=false;
    private AppCompatActivity c;
    private String searchHint;
    public View v;

    private AlertDialog.Builder mb;
     private AlertDialog ad;

    public MySpinnerSearchable(AppCompatActivity c,ArrayList<SpinnerItem> spinnerItems,String searchHint, SpinnerConfig sc) {
        SpinnerItems = spinnerItems;
        this.sc = sc;
        this.c=c;
        this.searchHint=searchHint;



    }

    public MySpinnerSearchable(AppCompatActivity c,ArrayList<SpinnerItem> spinnerItems,String searchHint, SpinnerConfig sc,ButtonSpinnerOnClick click) {
        SpinnerItems = spinnerItems;
        this.sc = sc;
        this.c=c;
        this.searchHint=searchHint;
        this.click=click;
        withButt=true;

    }

    public void openSpinner(){

        mb = new AlertDialog.Builder(c); //c est l activity non le context ..

        if(!withButt) {
            v = c.getLayoutInflater().inflate(R.layout.spinner_view, null);
        }else {
            v = c.getLayoutInflater().inflate(R.layout.spinner_view2, null);
            ImageView butt=(ImageView)v.findViewById(R.id.spinner_butt);
            butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    click.onClick();
                }
            });
        }
        TextView fermer=(TextView) v.findViewById(R.id.close_spinner_butt);
        final EditText search=(EditText)v.findViewById(R.id.spinner_search);
        search.setHint(searchHint);

        //region configuration recyclerview ..
        RecyclerView mRecyclerView;
        mRecyclerView = (RecyclerView)v.findViewById(R.id.spinner_div_affich);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        final MySpinnerAdapter mAdapter = new MySpinnerAdapter(c);

        mRecyclerView.setAdapter(mAdapter);
        int y=0;
        while (y != SpinnerItems.size()){
            mAdapter.AdapterItems.add(SpinnerItems.get(y));
            y++;
        }
        //endregion

        mb.setView(v);

        ad=mb.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                mAdapter.notifyDataSetChanged();
            }
        });
        ad.show();
        ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..
        ad.setCancelable(false); //désactiver le button de retour ..



        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                mAdapter.AdapterItems.clear();
                mAdapter.notifyDataSetChanged();
                int index=0;
                while (index != SpinnerItems.size()){
                    if(SpinnerItems.get(index).value.toLowerCase().indexOf(s.toString().toLowerCase()) != -1 || SpinnerItems.get(index).key.toLowerCase().indexOf(s.toString().toLowerCase()) != -1 ){
                        mAdapter.AdapterItems.add(SpinnerItems.get(index));
                    }
                    index++;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //region fermer le spinner
        fermer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        //endregion






    }

    public void closeSpinner(){
        ad.dismiss();
    }


    public class MySpinnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        AppCompatActivity c;
        public  ArrayList<SpinnerItem> AdapterItems=new ArrayList<SpinnerItem>() ;

        public MySpinnerAdapter( AppCompatActivity c) {
            this.c = c;
        }

        public  class SpinnerItemView extends RecyclerView.ViewHolder  {
            public TextView codebar;

            public TextView key;
            public TextView value;
            public SpinnerItemView(View v) {
                super(v);
                key=(TextView)v.findViewById(R.id.spinner_item_key);
                value=(TextView)v.findViewById(R.id.spinner_item_value);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item,parent,false);
            SpinnerItemView vh = new SpinnerItemView(v);
            return vh;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            ((SpinnerItemView)holder).key.setText(AdapterItems.get(position).key);
            ((SpinnerItemView)holder).value.setText(AdapterItems.get(position).value);

            ((LinearLayout)(( SpinnerItemView)holder).key.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sc.onChooseItem(position,AdapterItems.get(position));
                }
            });

        }

        @Override
        public int getItemCount() {
            return AdapterItems.size();
        }
    }

    public interface SpinnerConfig {
        void onChooseItem(int pos, SpinnerItem item);
    }

    public interface ButtonSpinnerOnClick {
        void onClick();
    }


}
