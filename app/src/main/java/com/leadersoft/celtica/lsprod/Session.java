package com.leadersoft.celtica.lsprod;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by celtica on 13/11/18.
 */

public class Session {
    public String code_chef,code_ligne,type,defaultDepot,deviceId;
    public ServeurInfos serveur;
    public boolean isLot=false,prodIsPackaging;
    private boolean dejaScan=false;
    public Session(String code_chef, String code_ligne,String type) {
        this.code_chef = code_chef;
        this.code_ligne = code_ligne;
        this.serveur=new ServeurInfos();
        getSessionInfo();

    }

    public String getDefaultDepot(){
        Cursor r=Accueil.bd.read("select * from admin");
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("defaultDepot"));
        }
        return "";
    }

    public String getTypeDevice(){
        Cursor r=Accueil.bd.read("select * from admin");
        if (r.moveToNext()){
            return r.getString(r.getColumnIndex("type_device"));
        }
        return "";
    }

    public boolean isLot(){
        Cursor r=Accueil.bd.read("select * from admin");
        if (r.moveToNext()){
            if(r.getString(r.getColumnIndex("isLot")).equals("1"))
            return true;
        }
        return false;
    }

    public boolean prodIsPackaging(){
        Cursor r=Accueil.bd.read("select * from admin");
        if (r.moveToNext()){
            if(r.getString(r.getColumnIndex("prodIsPackaging")).equals("1"))
                return true;
        }
        return false;
    }

    public void getSessionInfo(){
        Cursor r=Accueil.bd.read("select * from admin");
        if (r.moveToNext()){
            isLot=r.getString(r.getColumnIndex("isLot")).equals("1");
            type=r.getString(r.getColumnIndex("type_device"));
            defaultDepot=r.getString(r.getColumnIndex("defaultDepot"));
            deviceId=r.getString(r.getColumnIndex("device_name"));
            prodIsPackaging=r.getString(r.getColumnIndex("prodIsPackaging")).equals("1");

        }

    }

    public void changeLotPermission(boolean isLot){
        this.isLot=isLot;
        int i=0;
        if (isLot)
            i=1;
        Log.e("ppp",""+i+" / "+isLot);
        Accueil.bd.write("update admin set isLot='"+i+"'");
    }

    public void changeProdPackgingPermission(boolean prodIsPackaging){
        this.prodIsPackaging=prodIsPackaging;
        int i=0;
        if (prodIsPackaging)
            i=1;
        Accueil.bd.write("update admin set prodIsPackaging='"+i+"'");
    }

    public void changeDeviceId(final String deviceId){
        Accueil.bd.write2("update admin set device_name=?", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,deviceId+"");
                stmt.execute();
            }
        });
        this.deviceId=deviceId;
    }

    public static String formatPrix(double prix){
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(" %,.2f", prix);

        return sb.toString().replaceAll(","," ");
    }


    public static String convertNull(String s){

        if(s == null){
            return "";

        }else {
            return s;
        }

    }

    public static String convertNullDouble(String s){

        if(s == null){
            return "0";

        }else {
            return s;
        }

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public  void openScannerCodeBarre(final AppCompatActivity c, final OnScanListener scan){

        if (type.equals(c.getResources().getString(R.string.config_type_sansscanner))) {
            c.startActivityForResult(new Intent(c,CodeBarScanner.class),2);
        } else {
            AlertDialog.Builder mb = new AlertDialog.Builder(c); //c est l activity non le context ..

            View v= c.getLayoutInflater().inflate(R.layout.div_scanner,null);
            final LinearLayout root=(LinearLayout)v.findViewById(R.id.div_scan_root);
            final EditText code=(EditText)v.findViewById(R.id.sacnner_code);
            code.requestFocus();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                code.setShowSoftInputOnFocus(false);
            }

            mb.setView(v);
            final AlertDialog ad=mb.create();
            ad.show();
            ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..


            code.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    scan.OnScan(s.subSequence(0,s.length()-1).toString(),root);
                    ad.dismiss();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }
    }

    public void formaterBD(){
        Accueil.bd.write("update production set sync='1',etat='supprimé'");
        Accueil.bd.write("update maintenance set sync='1',etat='supprimé'");

    }

    public  String formatQt(double qt){
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(" %.2f", qt);

        return sb.toString().replaceAll(","," ").replaceFirst(" ","");
    }

    public void opneClavier(AppCompatActivity c){
        InputMethodManager imm = (InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

    }

    public void closeClavier(AppCompatActivity c,View view){
        InputMethodManager imm = (InputMethodManager)c.getSystemService(INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.

        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            Log.e("ccc","is null");
            view = new View(c);
            view.requestFocus();
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public  void openScannerCodeBarreIncr(final AppCompatActivity c, final OnScanListenerIncr scan){

        if (type.equals(c.getResources().getString(R.string.config_type_sansscanner))) {

            Intent i=new Intent(c,CodeBarScanner.class);
            i.putExtra("request","incr");
            c.startActivityForResult(i,3);


        } else {
            dejaScan=false;
            AlertDialog.Builder mb = new AlertDialog.Builder(c); //c est l activity non le context ..

            View v= c.getLayoutInflater().inflate(R.layout.div_scanner2,null);
            final EditText code=(EditText)v.findViewById(R.id.sacnner_code);
            final CheckBox check_incr=(CheckBox)v.findViewById(R.id.div_scanner_check_incr);
            final LinearLayout body=(LinearLayout) v.findViewById(R.id.body);
            code.requestFocus();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                code.setShowSoftInputOnFocus(false);
            }

            mb.setView(v);
            final AlertDialog ad=mb.create();
            ad.show();
            ad.setCanceledOnTouchOutside(false); //ne pas fermer on click en dehors ..


            code.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {

                    if(!dejaScan){
                        dejaScan=true;

                        if (check_incr.isChecked()) {
                            scan.OnScan(s.subSequence(0,s.length()-1).toString(),body);
                            code.setText("");

                        }else {
                            scan.OnScanIncrNotChecked(s.subSequence(0,s.length()-1).toString(),body,ad);
                            code.setText("");
                        }
                    }else {
                        dejaScan=false;
                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }
    }

    public void changeColorOnScan(final View v, int color1, final int color2){
        v.setBackgroundColor(color1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setBackgroundColor(color2);
            }
        },600);
    }


    public void GoScanByCameraPhone(AppCompatActivity c,PreferenceManager.OnActivityResultListener res){
       c.startActivityForResult(new Intent(c,CodeBarScanner.class),2);

       //c.onActiv
   }

    public void playAudioFromAsset(AppCompatActivity c,String nomFile){
        MediaPlayer player = new MediaPlayer();
        try {
            AssetFileDescriptor afd = c.getAssets().openFd(nomFile);
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deconect() {
        Accueil.bd.write("delete from session");
    }

    public void libererDepot() {
        Accueil.bd.write("update admin set last_depot=''");
    }

    public interface OnScanListener {
        void OnScan(String code, LinearLayout div_scanner);
    }

    public interface OnScanListenerIncr extends OnScanListener {
        void OnScanIncrNotChecked(String code,LinearLayout div_scanner,AlertDialog ad);
    }



}
