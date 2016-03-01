/**
 * @Purpose Database API (Common)
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class DBUtil {
    private SQLiteDatabase db;
    private Activity activ;
    private String dbName;

    /**
     * Constructor: specify activity and database name
     */
    public DBUtil(Activity _activ, String _dbName){
        this.activ = _activ;
        this.dbName = _dbName;
    }

    /**
     * Open or create DB
     * @return Success or failure
     */
    public boolean openDB(){
        try {
            this.db = this.activ.openOrCreateDatabase(this.dbName, Context.MODE_PRIVATE, null);
        }
        catch(Exception e){
            this.debugTrace(2, "openDB.");
            return false;
        }
        return true;
    }

    /**
     * Close DB
     * @return Success or failure
     */
    public boolean closeDB(){
        try {
            this.db.close();
        }
        catch(Exception e){
            this.debugTrace(2, "closeDB.");
            return false;
        }
        return true;
    }

    /**
     * Execute SQL
     * @param sql SQL
     * @return Success or failure
     */
    public boolean executeSql(String sql){
        if(this.db == null){
            this.debugTrace(1, "executeSql DB null.");
            return false;
        }
        try {
            this.db.execSQL(sql);
        }
        catch(Exception e){
            this.debugTrace(2, "executeSql execSQL.");
            return false;
        }
        return true;
    }

    /**
     * Insert a row into given table by ContentValues
     * @param tableName Table Name
     * @param cv Row data
     * @return Row id(successful) or -1(failed)
     */
    public long insertByCv(String tableName, ContentValues cv){
        long id = 0;

        if(this.db == null){
            this.debugTrace(1, "insertByCv DB null.");
            return -1;
        }

        try {
            id = this.db.insert(tableName, null, cv);
        }
        catch(Exception e){
            this.debugTrace(2, "executeSql insert.");
            return -1;
        }
        return id;
    }

    /**
     * Query SQL and return ArrayList
     *     Format: ArrayList<HashMap<"ColName", "ColValue">>
     * @param sql SQL
     * @return Result(successful) or empty ArrayList(failed)
     */
    public ArrayList<HashMap<String, String>> querySql(String sql){
        ArrayList<HashMap<String, String>> rlt = new ArrayList<HashMap<String, String>>();
        Cursor csr;
        int colCount = 0;

        if(this.db == null){
            this.debugTrace(1, "querySql DB null.");
            return rlt;
        }
        try {
            csr = this.db.rawQuery(sql, null);
        }
        catch(Exception e){
            this.debugTrace(2, "querySql rawQuery.");
            return rlt;
        }

        if(csr.getCount()==0){
            return rlt;
        }
        colCount = csr.getColumnCount();
        csr.moveToFirst();
        do{
            HashMap<String, String> row = new HashMap<String, String>();
            for(int i = 0 ; i < colCount ; i++){
                String tempKey = csr.getColumnName(i);
                String tempVal = csr.getString(i);
                row.put(tempKey, tempVal);
            }
            rlt.add(row);
        }while(csr.moveToNext());

        csr.close();
        return rlt;
    }

    /**
     * Query SQL and return Cursor
     * @param sql SQL
     * @return Result(successful) or null(failed)
     */
    public Cursor querySqlGetCsr(String sql){
        Cursor csr;

        if(this.db == null){
            this.debugTrace(1, "querySqlGetCsr DB null.");
            return null;
        }
        try {
            csr = this.db.rawQuery(sql, null);
        }
        catch(Exception e){
            this.debugTrace(2, "querySqlGetCsr rawQuery.");
            return null;
        }

        return csr;
    }

    /**
     * Get DB Object
     * @return DB Object
     */
    public SQLiteDatabase getDB(){
        return this.db;
    }

    /**
     * Get DB Path
     * @return DB path
     */
    public String getDBPath(){
        if(this.db == null){
            this.debugTrace(1, "getDBPath DB null.");
            return "";
        }
        return this.db.getPath();
    }

    /**
     * Dev Tool
     */
    protected static boolean devMode = false;
    protected static String[] devMsgType = new String[]{"Info", "Error", "Exception"};
    /**
     * Debug API: Trace message
     * @param type message type (0:info, 1:error, 2:exception)
     * @param msg message
     */
    protected void debugTrace(int type, String msg){
        if(devMode == true){
            String text = String.format("[%s]%s", devMsgType[type], msg);

            /*new AlertDialog.Builder(this.activ)
                    .setTitle("#DevMode#")
                    .setMessage(text)
                    .setPositiveButton("Close", null)
                    .show();*/

            Toast.makeText(this.activ, text, Toast.LENGTH_LONG).show();

            System.out.println("@@@ ###DevMode### " + text + " @@@");
        }
    }
}
