package com.leadersoft.celtica.lsprod.Preparations;

import android.database.sqlite.SQLiteStatement;

import com.leadersoft.celtica.lsprod.Accueil;
import com.leadersoft.celtica.lsprod.DeviceConfig;
import com.leadersoft.celtica.lsprod.ETAT;
import com.leadersoft.celtica.lsprod.MyBD;
import com.leadersoft.celtica.lsprod.Productions.Produit;
import com.leadersoft.celtica.lsprod.SqlServerBD;
import com.leadersoft.celtica.lsprod.Synchronisation;

import java.sql.SQLException;
import java.util.HashMap;

public class ProduitPréparé extends Produit {
    public double qt;
    public String code_depot,nom_depot,num_lot;

    public ProduitPréparé(String codebar, String designiation, String condition, int qt_condition,double qt) {
        super(codebar, designiation, condition, qt_condition);
        this.qt=qt;
    }

    public ProduitPréparé(String codebar, String designiation,String code_depot,String nom_depot,String num_lot,double qt){
        super();
        this.codebar=codebar;
        this.qt=qt;
        this.designiation=designiation;
        this.code_depot=code_depot;
        this.nom_depot=nom_depot;
        this.num_lot=num_lot;

    }



    public ProduitPréparé(String codebar) {
        super(codebar);
    }

    public void addToBD(String id_bon) {
        Accueil.bd.write2("insert into produit_preparer (id_bon,codebar_pr,nom_pr,quantité) values('" + id_bon + "','" + codebar + "',?,'" + qt + "')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,designiation+"");
                stmt.execute();
            }
        });
    }

    public void addToBDProd(String id_bon) {
        Accueil.bd.write2("insert into produit_preparer_production (id_prod,codebar_pr,nom_pr,code_depot,nom_depot,num_lot,quantité) values('" + id_bon + "','" + codebar + "',?,?,?,?,'" + qt + "')", new MyBD.SqlPrepState() {
            @Override
            public void putValue(SQLiteStatement stmt) {
                stmt.bindString(1,designiation+"");
                stmt.bindString(2,code_depot+"");
                stmt.bindString(3,nom_depot+"");
                if (num_lot != null){
                    stmt.bindString(4,num_lot+"");
                }
                stmt.execute();
            }
        });
    }

    public void exportéProduit(String RECORDID,final String id_bon){
        HashMap<Integer,String> datas=new HashMap<>();
        datas.put(1,designiation);
        datas.put(2,code_depot);
        datas.put(3,num_lot);
        Accueil.BDsql.write2("insert into bonProductionMobileItem (RECORDID, QTE, NUM_BON, CODE_BARRE, PRODUIT, CODE_DEPOT, NUM_LOT, BLOCAGE) values('"+RECORDID+"','"+qt+"','"+ DeviceConfig.session.deviceId +"_"+id_bon+"','"+codebar+"',?,?,?,'F')",datas, new SqlServerBD.doAfterBeforeGettingData() {
            @Override
            public void echec(SQLException e) {
                Accueil.BDsql.transactErr = true;
                Synchronisation.ExportationErr = Synchronisation.ExportationErr + "Erreur d insertion dans prduits préparées de bon: " + id_bon + " \n " + e.getMessage() + " \n ";

            }

            @Override
            public void before() {

            }

            @Override
            public void After() {

            }
        });
    }
}
