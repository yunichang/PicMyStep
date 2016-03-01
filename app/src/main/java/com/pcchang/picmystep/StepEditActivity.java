/**
 * @Purpose StepEditActivity
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class StepEditActivity extends AppCompatActivity
        implements LocationListener {

    char editType = 0;
    int stepId = 0;
    String imgPath = "";
    long eventTime = 0;
    ImageView imvPhoto;
    TextView tvTime, tvLat, tvLong, tvLocMsg, tvDebugMsg;
    EditText etxSubject, etxPlaceTag, etxNote;
    LinearLayout llLocSetup;
    LocationManager locMgr;
    Geocoder geocdr;
    Location nowLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_edit);

        // initial view
        imvPhoto = (ImageView)findViewById(R.id.imv_photo);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvLat = (TextView) findViewById(R.id.tv_lat);
        tvLong = (TextView) findViewById(R.id.tv_long);
        tvLocMsg = (TextView) findViewById(R.id.tv_loc_msg);
        tvDebugMsg = (TextView) findViewById(R.id.tv_debug_msg);
        etxSubject = (EditText) findViewById(R.id.etx_subject);
        etxPlaceTag = (EditText) findViewById(R.id.etx_place);
        etxNote = (EditText) findViewById(R.id.etx_note);
        llLocSetup = (LinearLayout) findViewById(R.id.ll_loc_setup);

        // get intent data
        Intent it = getIntent();
        editType = it.getCharExtra("edit_type", '0');

        // do action based on case new or edit
        if(editType == 'n'){
            imgPath = it.getStringExtra("img_path");
            showAsNew();

            geocdr = new Geocoder(this, Locale.getDefault());
            locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        else if(editType == 'e'){
            stepId = it.getIntExtra("step_id", 0);
            showAsEdit();
        }
        else {
            GlobalConfig.debugTrace(this, 1, "StepEditActivity onCreate invalid editType=[" + editType + "]");
            finish();
        }

        //devMode
        if(GlobalConfig.devMode == false){
            tvDebugMsg.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        doLocUpdateRequest();
    }

    @Override
    protected void onPause(){
        super.onPause();

        doLocUpdateRemove();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_step_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            // save
            case R.id.save:
                if(editType == 'n') {
                    addNewStep();
                    gotoReadStep("" + this.stepId);
                }
                else if(editType == 'e'){
                    updateStep();
                }
                else {
                    GlobalConfig.debugTrace(this, 1, "StepEditActivity onOptionsItemSelected invalid editType=[" + editType + "]");
                }
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        refreshLocStatus(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Do location update request (only when Case "new")
     */
    public void doLocUpdateRequest(){
        String locMsg = "";
        if(editType == 'n') {
            String bestPvdr = locMgr.getBestProvider(new Criteria(), true);

            if(bestPvdr == null){
                locMsg = "Please check the location function is enabled.";
            }
            else{
                locMsg = "Getting the location information...";
                try {
                    locMgr.requestLocationUpdates(bestPvdr, GlobalConfig.LOC_UPDATE_MIN_TIME, GlobalConfig.LOC_UPDATE_MIN_DIST, this);
                }
                catch(Exception e){
                    GlobalConfig.debugTrace(this, 2, "StepEditActivity doLocUpdateRequest");
                }
            }
        }
        else{
            locMsg = "Edit mode. Do not to locate.";
        }
        tvLocMsg.setText(locMsg);
    }

    /**
     * Do location update remove (only when Case "new")
     */
    public void doLocUpdateRemove(){
        if(editType == 'n') {
            try {
                locMgr.removeUpdates(this);
            }
            catch (Exception e) {
                GlobalConfig.debugTrace(this, 2, "StepEditActivity doLocUpdateRemove");
            }
        }
    }

    /**
     * Refresh location status
     */
    public void refreshLocStatus(Location loc){
        double latitude = 0;
        double longitude = 0;
        double altitude = 0;
        String provider = "";
        String locMsg = "";
        String debugMsg = "";

        nowLoc = loc;

        if(nowLoc == null){
            locMsg = "Cannot get location information!!";
        }
        else{
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            altitude = loc.getAltitude();
            provider = loc.getProvider();

            locMsg = "Get location information successfully.";
            debugMsg = String.format("locPvdr=[%s] lat=[%.5f] long=[%.5f] alt=[%.2f]m"
                    , provider, latitude, longitude, altitude);

        }
        tvLat.setText(String.valueOf(latitude));
        tvLong.setText(String.valueOf(longitude));
        etxPlaceTag.setText(GlobalConfig.getAddrByLocation(this, loc));
        tvLocMsg.setText(locMsg);
        tvDebugMsg.setText(debugMsg);
    }

    /**
     * Show data on view for Case New
     */
    public void showAsNew(){
        String tempStr = "";

        showImg();

        this.eventTime = System.currentTimeMillis();
        tempStr = GlobalConfig.convertTimestampToHumanTime(eventTime);
        tvTime.setText(tempStr);

        tvLat.setText("0.0");
        tvLong.setText("0.0");

    }

    /**
     * Show data on view for Case Edit
     */
    public void showAsEdit(){
        HashMap<String, String> rec;
        long tempLong = 0;
        String tempStr = "";

        rec = new GlobalConfig().getStepRecordById(this, this.stepId);
        if(rec == null){
            GlobalConfig.debugTrace(this, 1, "StepEditActivity showAsEdit cannot get record");
            finish();
        }

        this.imgPath = rec.get("imgPath");
        showImg();

        etxSubject.setText(rec.get("subject"));

        tempLong = Long.parseLong(rec.get("eventTime"));
        tempStr = GlobalConfig.convertTimestampToHumanTime(tempLong);
        tvTime.setText(tempStr);

        etxPlaceTag.setText(rec.get("placeTag"));
        tvLat.setText(rec.get("latitude"));
        tvLong.setText(rec.get("longitude"));

        etxNote.setText(rec.get("note"));

        llLocSetup.setVisibility(View.INVISIBLE);
    }

    /**
     * Show image view
     */
    public void showImg(){
        if(GlobalConfig.isFileExist(this.imgPath) == false){
            return;
        }

        //windows size
        int[] ws = GlobalConfig.getWindowSize(this);
        int imgMaxWH = Math.min(ws[0], ws[1])/2;

        //View: imgPhoto
        Bitmap bmp;
        BitmapFactory.Options bmpOpt = new BitmapFactory.Options();
        bmpOpt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(this.imgPath, bmpOpt);
        int imgOrigW = bmpOpt.outWidth;
        int imgOrigH = bmpOpt.outHeight;
        int imgScale = Math.min(imgOrigH/imgMaxWH, imgOrigW/imgMaxWH);
        bmpOpt.inJustDecodeBounds = false;
        bmpOpt.inSampleSize = imgScale;
        bmp = BitmapFactory.decodeFile(this.imgPath, bmpOpt);
        imvPhoto.setImageBitmap(bmp);
    }

    /**
     * action: add new step
     */
    public void addNewStep(){
        String subject = etxSubject.getText().toString();
        String placeTag = etxPlaceTag.getText().toString();
        String note = etxNote.getText().toString();
        double lat = Double.parseDouble(tvLat.getText().toString());
        double lng = Double.parseDouble(tvLong.getText().toString());
        int newId = 0;

        //check
        if(subject.trim().equals("")){
            subject = "Amazing Step!";
        }
        if(placeTag.trim().equals("")){
            placeTag = "Unknown Place";
        }

        //add
        newId = new GlobalConfig().addNewStepRecord(this
                , subject, eventTime, lat, lng, placeTag, note, this.imgPath);
        if(newId == -1) {
            GlobalConfig.debugTrace(this, 1, "StepEditActivity addNewStep add failed");
        }
        Toast.makeText(this, "Add new step record successfully!", Toast.LENGTH_SHORT).show();
        this.stepId = newId;
    }

    /**
     * action: update step
     */
    public void updateStep(){
        String subject = etxSubject.getText().toString();
        String placeTag = etxPlaceTag.getText().toString();
        String note = etxNote.getText().toString();
        boolean bool = false;

        //check
        if(subject.trim().equals("")){
            subject = "Amazing Step!";
        }
        if(placeTag.trim().equals("")){
            placeTag = "Unknown Place";
        }

        bool = new GlobalConfig().updateStepRecord(this
                , this.stepId, subject, placeTag, note);
        if(bool == false){
            GlobalConfig.debugTrace(this, 1, "StepEditActivity updateStep update failed");
        }
        Toast.makeText(this, "Update successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Go to activity: Read Step
     */
    public void gotoReadStep(String stepId){
        Intent it = new Intent(this, StepReadActivity.class);
        it.putExtra("step_id", stepId);
        startActivity(it);
    }

    /**
     * Go to activity: System Location settings
     */
    public void gotoLocSetting(View v){
        GlobalConfig.startActivityLocationSetting(this);
    }
}
