package com.leadersoft.celtica.lsprod;

import android.database.Cursor;

/**
 * Created by celtica on 26/04/19.
 */

public class ServeurInfos {
    public String ip,port,bdName,user,mdp;

    public ServeurInfos(String ip, String port, String bdName, String user, String mdp) {
        this.ip = ip;
        this.port = port;
        this.bdName = bdName;
        this.user = user;
        this.mdp = mdp;
    }

    public ServeurInfos(){
        getServeurINfos();
    }

    public void getServeurINfos(){
        Cursor r=Accueil.bd.read("select * from sqlconnect");
        if (r.moveToNext()){
            ip=r.getString(r.getColumnIndex("ip"));
            port=r.getString(r.getColumnIndex("port"));
            bdName=r.getString(r.getColumnIndex("bd_name"));
            user=r.getString(r.getColumnIndex("user"));
            mdp=r.getString(r.getColumnIndex("mdp"));
        }

    }

    public void majInfos(String ip, String port, String bdName, String user, String mdp){
        this.ip = ip;
        this.port = port;
        this.bdName = bdName;
        this.user = user;
        this.mdp = mdp;
        Accueil.bd.write("update sqlconnect set ip='"+ip+"', port='"+port+"', bd_name='"+bdName+"', user='"+user+"',mdp='"+mdp+"' ");

    }
}
