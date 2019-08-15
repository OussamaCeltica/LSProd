package com.leadersoft.celtica.lsprod;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SqlServerBD {
    
     private Connection connect=null;
       private Statement s=null;
        private PreparedStatement p=null;
         public ResultSet r=null;
         private doAfterBeforeConnect doCon;
         private String res;
         public ExecutorService es=Executors.newSingleThreadExecutor();
         public String msgErr="";

         public boolean RequestErr=false;

         public boolean transactErr=false;

        // public int threadEnExec=0;//pour assurer l exécution d un seul read a la fois N oublier pas a remetre a 0 a chaque fois..


        // public String typeBD,lien;
    
    public SqlServerBD(String ip, String port, String bd, String user, String mdp, String typeDB, doAfterBeforeConnect d) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
      doCon=d;
      String lien="jdbc:jtds:sqlserver://"+ip+":"+port+";"
              + "databaseName=" +bd + ";user=" +user + ";password="
              + mdp + ";loginTimeout=600;socketTimeout=3600";
      new doIt().execute(typeDB,lien);
     }

    //AsynchTask pour rétablir la connexion ..
    class doIt extends AsyncTask<String,String,String> {


        @Override
        protected String doInBackground(String... strings) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


            try {
                Class.forName(strings[0]);
                connect = DriverManager.getConnection(strings[1]);
                s = connect.createStatement();
            } catch (ClassNotFoundException e) {
                doCon.echec();
                e.printStackTrace();
                return "NO";
            } catch (SQLException e) {
                e.printStackTrace();
                doCon.echec();
                return "NO";
            }
            return "OK";

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            doCon.before();
        }

        @Override
        protected void onPostExecute(String r) {
            super.onPostExecute(r);
            if(r.equals("OK")){
                try {
                    doCon.After();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }


        }
    }

        /*
        //read from DB  , //le retour de resultas on l acced avec Instance de  ResultSet r=SqlServerBD.r
         et on manipule avec: while(r.next()){ r.getString(nom_Column); }

         */
        public synchronized ResultSet read(final String query, final doAfterBeforeGettingData d) {
            d.before();
           //Un Thread Qui Exécute la Requete, leur Utilité est Pour Assurer le Callback ..
            Thread t= null;
            try {
                t = new ThreadCallback(new doAfterBeforeGettingThread() {

                     @Override
                     public void doInBackground() {
                         try {
                            r = s.executeQuery(query);
                             res="true";

                         } catch (SQLException e) {
                             res="false";
                             Log.e("SQLL SERVER READ ERR ",e.getMessage());
                             msgErr="SQL SERVER READ ERR:"+e.getMessage();
                             e.printStackTrace();
                             d.echec(e);
                         }
                     }

                 });
            } catch (SQLException e) {
                e.printStackTrace();
            }
            es.execute(t);

            es.execute(new Runnable() {
                @Override
                public void run() {
                if(res.equals("true")){
                    d.After();
                }
                }
            });

            return r;
        }


        //write into db ..
        public void write(final String query, final doAfterBeforeGettingData d)  {

            d.before();

            Thread t= null;
            try {
                t = new ThreadCallback(new  doAfterBeforeGettingThread() {


                    @Override
                    public void doInBackground() {
                        try {
                            connect.prepareStatement(query).executeUpdate();
                            res= "true";
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Log.e("SQLL SERVER WRITE ERR ",e.getMessage());
                            msgErr="SQL SERVER WRITE ERR:"+e.getMessage();
                            d.echec(e);
                            res="false";
                        }
                    }

                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
            es.execute(t);

            es.execute(new Runnable() {
                @Override
                public void run() {
                if(res.equals("true")){
                        d.After();
                }
                }
            });

        }


    //write into db with bind data ..
    /*---------------------- Utilisation Du HashMap -----------------*/
    /*
      le Hashmap contient les valeur a bindé avec la position ex:
       values(?,?)
       on a 2 a bindi donc , index 1 et index 2 ..
     */
    public void write2(final String query, final HashMap<Integer,String> datas , final doAfterBeforeGettingData d) {

        d.before();

        Thread t= null;
        try {
            t = new ThreadCallback(new  doAfterBeforeGettingThread() {


                @Override
                public void doInBackground() {
                    try {
                        PreparedStatement prep=connect.prepareStatement(query);
                        int i =1;
                        while (i != datas.size()+1){
                            prep.setString(i,datas.get(i));
                            Log.e("ttt",datas.get(i)+"");
                            i++;
                        }
                        prep.execute();

                        res= "true";
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Log.e("SQLL SERVER WRITE ERR ",e.getMessage());
                        msgErr="SQL SERVER WRITE ERR:"+e.getMessage();
                        d.echec(e);
                        res="false";
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        es.execute(t);

        es.execute(new Runnable() {
            @Override
            public void run() {
            if(res.equals("true")){
                d.After();
            }
            }
        });

    }

        /*--------------- TRansaction -------------*/

        /* UTilisation des transactions
        Accuiel.BDsql.beginTRansact();
        .
        .
        //les requete du write ..
        //dans les requete du write et dans echec() { Accuiel.BDsql.transactErr=true; }
        .
        .
        Accuiel.BDsql.commitTRansact();





         */

    public synchronized void beginTRansact() {
        es.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    connect.setAutoCommit(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public synchronized void commitTRansact() {
        es.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    if(!transactErr) {
                        connect.commit();
                    }else {
                        connect.rollback();
                    }
                    connect.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

    }



    //region interface pour les callback de inser/select ..
    public interface doAfterBeforeGettingData{
        public void echec(SQLException e);
        public void before();
        public void After();
    }
    //endregion

    //region interface pour les callback de connection a une BD SQL ..
    public interface doAfterBeforeConnect{
        public void echec();
        public void before();
        public void After() throws SQLException;

    }
    //endregion

    //region interface pour les callback d un Thread ..
    public interface doAfterBeforeGettingThread {
        void doInBackground();
    }
    //endregion

    //region interface Mon AsyncTask personnalissé travail avec les callback ..
    public class ThreadCallback  extends Thread {
          doAfterBeforeGettingThread callback;

        public  ThreadCallback( doAfterBeforeGettingThread getCallback) throws SQLException {
            callback=getCallback;

        }

        public void run(){
            callback.doInBackground();
        }

    }
    //endregion


}
