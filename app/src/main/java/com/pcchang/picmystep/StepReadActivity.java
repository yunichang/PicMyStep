/**
 * @Purpose StepReadActivity
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class StepReadActivity extends AppCompatActivity {
    int stepId = 0;
    ImageView imvPhoto;
    TextView tvStepId, tvSubject;
    TextView tvEventTime, tvBuildTime, tvLastUpdateTime;
    TextView tvPlaceTag, tvLat, tvLong, tvNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_read);

        //Get Intent Data
        Intent it = getIntent();
        stepId = Integer.parseInt(it.getStringExtra("step_id"));
        GlobalConfig.debugTrace(this, 0, "StepReadActivity onCreate stepId=[" + stepId + "]");
        if(stepId <= 0){
            GlobalConfig.debugTrace(this, 1, "StepReadActivity onCreate stepId<=0");
            finish();
        }

        imvPhoto = (ImageView) findViewById(R.id.imv_photo);
        tvStepId = (TextView) findViewById(R.id.tv_step_id);
        tvSubject = (TextView) findViewById(R.id.tv_subject);
        tvEventTime = (TextView) findViewById(R.id.tv_event_time);
        tvBuildTime = (TextView) findViewById(R.id.tv_build_time);
        tvLastUpdateTime = (TextView) findViewById(R.id.tv_last_update_time);
        tvPlaceTag = (TextView) findViewById(R.id.tv_place_tag);
        tvLat = (TextView) findViewById(R.id.tv_lat);
        tvLong = (TextView) findViewById(R.id.tv_long);
        tvNote = (TextView) findViewById(R.id.tv_note);
    }

    @Override
    protected void onResume(){
        super.onResume();

        refreshStepContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_step_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            //edit
            case R.id.edit:
                gotoEditStepAsEdit();
                break;
            //delete
            case R.id.delete:
                boolean bool = new GlobalConfig().deleteStepRecord(this, this.stepId);
                if(bool == true){
                    Toast.makeText(this, "Delete Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    GlobalConfig.debugTrace(this, 1, "StepReadActivity onOptionsItemSelected delete failed");
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Refresh step content (including retrieve data and update view)
     */
    public void refreshStepContent(){
        //retrieve data
        HashMap<String, String> rec = new GlobalConfig().getStepRecordById(this, this.stepId);
        if(rec == null){
            GlobalConfig.debugTrace(this, 1, "StepReadActivity refreshStepContent cannot get record");
            finish();
        }

        //update view
        showData(rec);
    }

    /**
     * Show the data on view
     * @param rec the specified step row
     */
    public void showData(HashMap<String, String> rec){
        String tempStr = "";
        long tempLong = 0;

        if(rec == null){
            GlobalConfig.debugTrace(this, 1, "StepReadActivity showData record is null");
            return;
        }

        //windows size
        int[] ws = GlobalConfig.getWindowSize(this);
        int imgMaxWH = Math.min(ws[0], ws[1])/2;

        //View: imgPhoto
        Uri imgUri = Uri.parse("file://" + rec.get("imgPath"));
        if(GlobalConfig.isFileExist(imgUri.getPath()) == true){
            Bitmap bmp;
            BitmapFactory.Options bmpOpt = new BitmapFactory.Options();
            bmpOpt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgUri.getPath(), bmpOpt);
            int imgOrigW = bmpOpt.outWidth;
            int imgOrigH = bmpOpt.outHeight;
            int imgScale = Math.min(imgOrigH/imgMaxWH, imgOrigW/imgMaxWH);
            bmpOpt.inJustDecodeBounds = false;
            bmpOpt.inSampleSize = imgScale;
            bmp = BitmapFactory.decodeFile(imgUri.getPath(), bmpOpt);
            imvPhoto.setImageBitmap(bmp);
        }

        //View: subject, id
        tvStepId.setText(String.format("#%05d", Integer.parseInt(rec.get("_id"))));
        tvSubject.setText(rec.get("subject"));

        //View: time
        tempLong = Long.parseLong(rec.get("eventTime"));
        tempStr = GlobalConfig.convertTimestampToHumanTime(tempLong);
        tvEventTime.setText(tempStr);
        tempLong = Long.parseLong(rec.get("buildTime"));
        tempStr = GlobalConfig.convertTimestampToHumanTime(tempLong);
        tvBuildTime.setText(tempStr);
        tempLong = Long.parseLong(rec.get("lastUpdateTime"));
        tempStr = GlobalConfig.convertTimestampToHumanTime(tempLong);
        tvLastUpdateTime.setText(tempStr);

        //View: place
        tvPlaceTag.setText(rec.get("placeTag"));
        tvLat.setText(rec.get("latitude"));
        tvLong.setText(rec.get("longitude"));

        //View: note
        tvNote.setText(rec.get("note"));
    }

    /**
     * Go to activity: Edit Step as Edit
     */
    public void gotoEditStepAsEdit(){
        Intent it = new Intent(this, StepEditActivity.class);
        it.putExtra("edit_type", 'e');
        it.putExtra("step_id", stepId);
        startActivityForResult(it, 404);
    }
}
