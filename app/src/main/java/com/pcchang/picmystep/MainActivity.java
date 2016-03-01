/**
 * @Purpose MainActivity
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initial DB
        initialDB();

        //lock the screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            //Location Setting
            case R.id.location_settings:
                GlobalConfig.startActivityLocationSetting(this);
                break;
            //About
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setTitle("About PicMyStep!")
                        .setMessage("PicMyStep! v1.0.1\n\nCopyright 2016 Yuni Chang.")
                        .setPositiveButton("Close", null)
                        .show();
                break;
            //Exit
            case R.id.exit:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Go to activity: Start To Pic
     * @param v view
     */
    public void gotoPicStep(View v){
        Intent it = new Intent(this, CameraActivity.class);
        startActivity(it);
    }

    /**
     * Go to activity: Review My Step
     * @param v view
     */
    public void gotoStepHistory(View v){
        Intent it = new Intent(this, StepListActivity.class);
        startActivity(it);
    }

    /**
     * Go to activity: Where am I
     * @param v view
     */
    public void gotoMap(View v){
        Intent it = new Intent(this, MapActivity.class);
        startActivity(it);
    }

    /**
     * Initial App DB
     */
    public void initialDB(){
        boolean bool = new GlobalConfig().createTableStep(this);
        if(bool == false) {
            GlobalConfig.debugTrace(this, 1, "initialDB failed");
        }
    }


}
