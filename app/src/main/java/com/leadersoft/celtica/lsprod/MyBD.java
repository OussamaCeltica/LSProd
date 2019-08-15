package com.leadersoft.celtica.lsprod;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by celtica on 25/03/18.
 */

public class MyBD {

    public MyBDhelper bDhelper;

    public MyBD(String bdName, Context con){
        this.bDhelper=new MyBDhelper(con,bdName);

        //dans le context il faut lui donner une activité ..

    }

    /*---------- N oublier pas de fermer le cursor avec r.close(); -------*/
    public Cursor read(String query){
        return bDhelper.getReadableDatabase().rawQuery(query,null);
    }

    /*----------
    cette methode pour ignorer le sql injection , l utilisation est comme suit:
    query=select * from table where col=? and col2=? and col3='normal'
    values=["Amine","Ahmed"];

    read2(query,values);

    --------- */
    public Cursor read2(String query,String[] values){
        return bDhelper.getReadableDatabase().rawQuery(query,values);
    }

    public void write(String query){
      bDhelper.getWritableDatabase().execSQL(query);

    }
/*----------------- Documentation de write2 ------------------- */
/*
il faut entouré cette methode avec un try catch du type android.database.SQLException pour
 testé si la requete est valide synataxiquement ou nn ..
et meme entouré l implementation de l interface SqlPrep lors d appel de cette methode pour
testé si le stockage n a pas d erreur ..
 */
    public void write2(String query,SqlPrepState val)  {

        SQLiteStatement stmt = bDhelper.getWritableDatabase().compileStatement(query);
        val.putValue(stmt);

    }
    /*
    stmt.bindString(1,convertNull(r.getString("CODE_CLIENT")));
    stmt.execute();

     */


    /*------------------- Utilisation des TRansaction ------------------------------
         try {
            bd.beginTransact();
           //les requete sont ici ..
            bd.setSuccefulTransact(); // like Commit ..
        }catch (SQLException e) {
            e.printStackTrace();

        }finally {
            bd.endTransact();
        }
     */

    public void beginTransact(){

        bDhelper.getWritableDatabase().beginTransaction();
    }

    public void setSuccefulTransact(){

        bDhelper.getWritableDatabase().setTransactionSuccessful();
    }

    public void endTransact(){

        bDhelper.getWritableDatabase().endTransaction();

    }

    //region SqliteHelper is here ..

    public class MyBDhelper extends SQLiteOpenHelper {



        //The Android's default system path of your application database.
        private  String DB_PATH = "";

        private String DB_NAME = "";

        private SQLiteDatabase myDataBase;

        private final Context myContext;

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);

                db.execSQL("PRAGMA foreign_keys=ON;");

        }

        /**
         * Constructor
         * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
         * @param context
         */
        public MyBDhelper(Context context,String dbName) {

            super(context, dbName, null, 1);

            this.DB_NAME=dbName;
            this.myContext = context;
            DB_PATH= myContext.getDatabasePath(dbName).toString();

            try {
                createDataBase();
                openDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * Creates a empty database on the system and rewrites it with your own database.
         * */
        public void createDataBase() throws IOException{

            boolean dbExist = checkDataBase();

            if(dbExist){
                //do nothing - database already exist
            }else{

                //By calling this method and empty database will be created into the default system path
                //of your application so we are gonna be able to overwrite that database with our database.
                this.getWritableDatabase();

                try {

                    copyDataBase();

                } catch (IOException e) {

                    throw new Error("Error copying database");

                }

            }

        }

        /**
         * Check if the database already exist to avoid re-copying the file each time you open the application.
         * @return true if it exists, false if it doesn't
         */
        private boolean checkDataBase(){
            //  this.getReadableDatabase();

            SQLiteDatabase checkDB = null;

            try{
                String myPath = DB_PATH ;
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);



            }catch(SQLiteException e){

                //database does't exist yet.

            }

            if(checkDB != null){

                checkDB.close();

            }

            return checkDB != null ? true : false;
        }

        /**
         * Copies your database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transfering bytestream.
         * */
        private void copyDataBase() throws IOException{

            //Open your local db as the input stream
            InputStream myInput = myContext.getAssets().open(DB_NAME);

            // Path to the just created empty db
            String outFileName = DB_PATH ;

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();

        }

        public void openDataBase() throws SQLException {

            //Open the database
            String myPath = DB_PATH ;
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }

        @Override
        public synchronized void close() {

            if(myDataBase != null)
                myDataBase.close();

            super.close();

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        // Add your public helper methods to access and get content from the database.
        // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
        // to you to create adapters for your views.


        //add your public methods for insert, get, delete and update data in database.

    }
    //endregion

    public interface SqlPrepState{
        void putValue(SQLiteStatement stmt);
    }

}
