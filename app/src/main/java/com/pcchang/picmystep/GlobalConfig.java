/**
 * @Purpose Global Configuration and Utility API (for this project)
 * @author Yuni Chang
 */

package com.pcchang.picmystep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GlobalConfig {
    static final String DB_NAME = "picMyStep";
    static final String DB_TABLE_NAME_STEP = "stepHistory";
    static final int LOC_UPDATE_MIN_TIME = 1000; //update condition: 1000ms=1s
    static final float LOC_UPDATE_MIN_DIST = 5; //update condition: 5m

    /**
     * Generate a new DBUtil object for DB_NAME
     * @return new DBUtil object
     */
    public DBUtil getNewDBObj(Activity activ){
        return new DBUtil(activ, GlobalConfig.DB_NAME);
    }

    /**
     * Get DB path for DB_NAME
     * @return new DBUtil object
     */
    public String getDBPath(Activity activ){
        String rlt = "";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB() == false){
            GlobalConfig.debugTrace(activ, 1, "getDBPath openDB");
            return rlt;
        }
        rlt = db.getDBPath();
        db.closeDB();
        return rlt;
    }

    /**
     * Create table DB_TABLE_NAME_STEP if not exist
     * @param activ activity
     * @return Successful or failed
     */
    public boolean createTableStep(Activity activ){
        String sql;
        boolean bool = false;

        sql = "CREATE TABLE IF NOT EXISTS " + GlobalConfig.DB_TABLE_NAME_STEP + " (" +
                "`_id` INTEGER PRIMARY KEY " +
                ",`isDeleted` INTEGER" +
                ",`subject` VARCHAR(255) " +
                ",`eventTime` INTEGER " +
                ",`buildTime` INTEGER " +
                ",`lastUpdateTime` INTEGER " +
                ",`latitude` FLOAT " +
                ",`longitude` FLOAT " +
                ",`placeTag` VARCHAR(255) " +
                ",`note` TEXT " +
                ",`imgPath` TEXT " +
                ")";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB() == false){
            GlobalConfig.debugTrace(activ, 1, "createTableStep openDB");
            return false;
        }

        bool = db.executeSql(sql);
        if(bool==false){
            GlobalConfig.debugTrace(activ, 1, "createTableStep failed SQL=[" + sql + "]");
            return false;
        }

        db.closeDB();
        return true;
    }

    /**
     * Add a new record of table DB_TABLE_NAME_STEP
     * @param activ activity
     * @param subject subject
     * @param eventTime eventTime timestamp
     * @param latitude latitude
     * @param longitude longitude
     * @param placeTag placeTag
     * @param note note
     * @param imgPath image file path
     * @return stepId(successful) or -1(failed)
     */
    public int addNewStepRecord(Activity activ, String subject
            , long eventTime, double latitude, double longitude, String placeTag
            , String note, String imgPath){

        int stepId = 0;

        ContentValues cv = new ContentValues(10);
        cv.put("isDeleted", 0);
        cv.put("subject", subject);
        cv.put("eventTime", eventTime);
        cv.put("buildTime", System.currentTimeMillis());
        cv.put("lastUpdateTime", System.currentTimeMillis());
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("placeTag", placeTag);
        cv.put("note", note);
        cv.put("imgPath", imgPath);

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB() == false){
            GlobalConfig.debugTrace(activ, 1, "addNewStepRecord openDB");
            return -1;
        }
        stepId = (int)db.insertByCv(GlobalConfig.DB_TABLE_NAME_STEP, cv);
        db.closeDB();

        return stepId;
    }

    /**
     * Add a new record of table DB_TABLE_NAME_STEP
     * @param activ activity
     * @param subject subject
     * @param eventTime eventTime timestamp
     * @param buildTime eventTime timestamp
     * @param lastUpdateTime eventTime timestamp
     * @param latitude latitude
     * @param longitude longitude
     * @param placeTag placeTag
     * @param note note
     * @param imgPath image file path
     * @return stepId(successful) or -1(failed)
     */
    public int addNewStepRecordAllCol(Activity activ, String subject
            , long eventTime, long buildTime, long lastUpdateTime
            , double latitude, double longitude, String placeTag
            , String note, String imgPath){

        int stepId = 0;

        ContentValues cv = new ContentValues(10);
        cv.put("isDeleted", 0);
        cv.put("subject", subject);
        cv.put("eventTime", eventTime);
        cv.put("buildTime", buildTime);
        cv.put("lastUpdateTime", lastUpdateTime);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("placeTag", placeTag);
        cv.put("note", note);
        cv.put("imgPath", imgPath);

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB() == false){
            GlobalConfig.debugTrace(activ, 1, "addNewStepRecordAllCol openDB");
            return -1;
        }
        stepId = (int)db.insertByCv(GlobalConfig.DB_TABLE_NAME_STEP, cv);
        db.closeDB();

        return stepId;
    }

    /**
     * Delete a record of table DB_TABLE_NAME_STEP by step id
     *      Note: it's not really deleted, instead change the isDeleted flag to true.
     * @param activ activity
     * @param stepId step id
     * @return Success or failure
     */
    public boolean deleteStepRecord(Activity activ, int stepId){
        String sql = "";
        boolean bool = false;

        sql = "UPDATE `" + GlobalConfig.DB_TABLE_NAME_STEP + "` SET " +
                " `isDeleted` = 1 "+
                ", `lastUpdateTime` = " + System.currentTimeMillis() +" " +
                " WHERE _id = " + stepId + ";";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB() == false){
            GlobalConfig.debugTrace(activ, 1, "deleteStepRecord openDB");
            return false;
        }
        bool = db.executeSql(sql);
        db.closeDB();

        return bool;
    }

    /**
     * Update a record of table DB_TABLE_NAME_STEP by step id
     * @param activ activity
     * @param stepId step id
     * @param subject subject
     * @param placeTag placeTag
     * @param note note
     * @return Success or failure
     */
    public boolean updateStepRecord(Activity activ
            , int stepId, String subject, String placeTag, String note){

        String sql = "";
        boolean bool = false;

        sql = "UPDATE `" + GlobalConfig.DB_TABLE_NAME_STEP + "` SET " +
                " `subject` = '" + subject+"' " +
                ", `placeTag` = '" + placeTag+"' " +
                ", `note` = '" + note+"' " +
                ", `lastUpdateTime` = " + System.currentTimeMillis() +" " +
                " WHERE _id = " + stepId + ";";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB() == false){
            GlobalConfig.debugTrace(activ, 1, "updateStepRecord openDB");
            return false;
        }
        bool = db.executeSql(sql);
        db.closeDB();

        return bool;
    }

    /**
     * Get all record content of table DB_TABLE_NAME_STEP
     *      Note: (1) Except isDeleted!=0
     *            (2) Order by id desc
     * @param activ activity
     * @return ArrayList
     *          Format: ArrayList<HashMap<"ColName", "ColValue">>
     */
    public ArrayList<HashMap<String, String>> getAllStepRecord(Activity activ){
        ArrayList<HashMap<String, String>> rlt;
        String sql;

        sql = "SELECT * FROM " + GlobalConfig.DB_TABLE_NAME_STEP + " " +
                " WHERE isDeleted=0 ORDER BY _id desc;";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB()==false){
            GlobalConfig.debugTrace(activ, 1, "getAllStepRecord openDB");
            return null;
        }
        rlt = db.querySql(sql);
        db.closeDB();

        return rlt;
    }

    /**
     * Get the record content of table DB_TABLE_NAME_STEP by step id
     * @param activ activity
     * @param stepId step id
     * @return HashMap(Success) or null (not exist or failure)
     *          Format: HashMap<"ColName", "ColValue">
     */
    public HashMap<String, String> getStepRecordById(Activity activ, int stepId){
        ArrayList<HashMap<String, String>> stepList;
        HashMap<String, String> rlt;
        String sql;

        sql = "SELECT * FROM " + GlobalConfig.DB_TABLE_NAME_STEP + "" +
                " WHERE _id=" + stepId + " and isDeleted=0;";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB()==false){
            GlobalConfig.debugTrace(activ, 1, "getStepRecordById openDB");
            return null;
        }
        stepList = db.querySql(sql);
        db.closeDB();

        if(stepList.size()==0){
            GlobalConfig.debugTrace(activ, 1, "getStepRecordById cannot retrieve");
            return null;
        }

        return stepList.get(0);
    }

    /**
     * Tool API: Convert time from timestamp to human readable time string
     * @param ts timestamp
     * @return Human readable time string
     */
    public static String convertTimestampToHumanTime(long ts){
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(ts));
    }

    /**
     * Tool API: Convert time from human readable time string to timestamp
     * @param hrt Human readable time string (Format: "yyyy-MM-dd HH:mm:ss")
     * @return timestamp
     */
    public static long convertTimeHumanToTimestamp(String hrt){
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse(hrt).getTime();
        }
        catch (Exception e){
            return 0;
        }
    }

    /**
     * Tool API: Return if the specified file exists
     * @param filePath file path
     * @return boolean
     */
    public static boolean isFileExist(String filePath){
        return new File(filePath).exists();
    }

    /**
     * Tool API: Launch System Location Settings Activity
     * @param activ activity
     */
    public static void startActivityLocationSetting(Activity activ){
        Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activ.startActivity(it);
    }

    /**
     * Tool API: Get the Address String by latitude and longitude
     * @param activ activity
     * @param lat latitude
     * @param lon longitude
     * @return Address String. If retrieving failed, return "(Cannot retrieve the address)"
     */
    public static String getAddrByLatLon(Activity activ, double lat, double lon){
        String rltAddr = "(Cannot retrieve the address)";
        Geocoder geocdr = new Geocoder(activ, Locale.getDefault());
        List<Address> listAddr;
        Address addr;

        try{
            listAddr = geocdr.getFromLocation(lat, lon, 1);
            if(listAddr == null || listAddr.size() == 0){
                GlobalConfig.debugTrace(activ, 0, "getAddrByLatLon cannot retrieve");
                return rltAddr;
            }

             addr = listAddr.get(0);
             rltAddr = "";
             for(int i = 0 ; i <= addr.getMaxAddressLineIndex() ; i++){
                 rltAddr += addr.getAddressLine(i) + " ";
             }
        }
        catch(Exception e){
            GlobalConfig.debugTrace(activ, 2, "getAddrByLatLon");
        }

        return rltAddr;
    }

    /**
     * Tool API: Get the Address String by location
     * @param activ activity
     * @param loc location object
     * @return Address String. If retrieving failed, return "(Cannot retrieve the address)"
     */
    public static String getAddrByLocation(Activity activ, Location loc){
        return getAddrByLatLon(activ, loc.getLatitude(), loc.getLongitude());
    }

    /**
     * Tool API: Get the Windows Size
     * @param activ activity
     * @return size
     *      Format: int[] {width, height}
     */
    public static int[] getWindowSize(Activity activ){
        int width = 0, height = 0;
        DisplayMetrics dm = new DisplayMetrics();
        activ.getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        return new int[]{width, height};
    }

    /**
     * Dev Tool
     */
    protected static boolean devMode = false;
    protected static String[] devMsgType = new String[]{"Info", "Error", "Exception"};
    /**
     * Debug API: Trace message
     * @param activ activity
     * @param type message type (0:info, 1:error, 2:exception)
     * @param msg message
     */
    public static void debugTrace(Activity activ, int type, String msg){
        if(devMode == true){
            String text = String.format("[%s]%s", devMsgType[type], msg);

            new AlertDialog.Builder(activ)
                    .setTitle("#DevMode#")
                    .setMessage(text)
                    .setPositiveButton("Close", null)
                    .show();

            Toast.makeText(activ, text, Toast.LENGTH_LONG).show();

            System.out.println("@@@ ###DevMode### " + text + " @@@");
        }
    }
    /**
     * DEV API: Build demo data
     * @param activ activity
     */
    public void buildDemoData(Activity activ) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        //demo data
        new GlobalConfig().addNewStepRecordAllCol(activ
                , "Super Big Merlion"
                , GlobalConfig.convertTimeHumanToTimestamp("2015-10-12 16:10:12")
                , GlobalConfig.convertTimeHumanToTimestamp("2015-10-12 16:11:32")
                , GlobalConfig.convertTimeHumanToTimestamp("2015-10-12 16:15:11")
                , 1.250432, 103.830317
                , "Singapore Sentosa"
                , "It's really very tall."
                , String.format("%s/%s", dir, "demo001.jpg"));

        new GlobalConfig().addNewStepRecordAllCol(activ
                , "My first one of Taiwan 100 peaks!"
                , GlobalConfig.convertTimeHumanToTimestamp("2015-12-12 07:10:31")
                , GlobalConfig.convertTimeHumanToTimestamp("2015-12-12 07:11:32")
                , GlobalConfig.convertTimeHumanToTimestamp("2015-12-12 07:15:11")
                , 23.292921, 121.03451
                , "JiaMing Hu"
                , "So cold but so cool!!!"
                , String.format("%s/%s", dir, "demo002.jpg"));

        new GlobalConfig().addNewStepRecordAllCol(activ
                , "How a beautiful view in Taiwan!"
                , GlobalConfig.convertTimeHumanToTimestamp("2015-12-12 15:43:01")
                , GlobalConfig.convertTimeHumanToTimestamp("2015-12-12 15:43:52")
                , GlobalConfig.convertTimeHumanToTimestamp("2015-12-12 16:01:11")
                , 23.292921, 121.03451
                , "JiaMing Hu"
                , "Taiwan is really beautiful."
                , String.format("%s/%s", dir, "demo003.jpg"));
    }
    /**
     * DEV API: clear all data
     * @param activ activity
     * @return Success or failure
     */
    public boolean clearData(Activity activ) {
        String sql;
        boolean rlt = false;

        sql = "DELETE FROM `" + GlobalConfig.DB_TABLE_NAME_STEP + "`;";

        DBUtil db = this.getNewDBObj(activ);
        if(db.openDB()==false){
            GlobalConfig.debugTrace(activ, 1, "clearData openDB");
            return false;
        }
        rlt = db.executeSql(sql);
        db.closeDB();

        return rlt;
    }
}
