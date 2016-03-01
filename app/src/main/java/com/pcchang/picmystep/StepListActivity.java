/**
 * @Purpose StepListActivity
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class StepListActivity extends AppCompatActivity
    implements AdapterView.OnItemClickListener {

    ArrayList<HashMap<String, String>> stepList;
    TextView tvTotal;
    TextView tvNoStepMsg;
    TextView tvDebugInfo;
    LinearLayout llDebugBlock;
    ListView lsvStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_list);

        //Initial View
        tvTotal = (TextView)findViewById(R.id.tv_total);
        tvNoStepMsg = (TextView) findViewById(R.id.tv_no_step_msg);
        tvDebugInfo = (TextView)findViewById(R.id.tv_debug_info);
        llDebugBlock = (LinearLayout)findViewById(R.id.ll_debug_block);
        lsvStep = (ListView)findViewById(R.id.lsv_step);
        lsvStep.setOnItemClickListener(this);

        //devMode
        if(GlobalConfig.devMode == false){
            llDebugBlock.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshStepList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_step_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            //refresh
            case R.id.refresh:
                refreshStepList();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GlobalConfig.debugTrace(this, 0, String.format("onItemClick position=[%d] id=[%d]", position, id));
        gotoReadStep(this.stepList.get(position).get("_id"));
    }

    /**
     * Go to activity: Read Step
     * @param stepId stepId
     */
    public void gotoReadStep(String stepId){
        Intent it = new Intent(this, StepReadActivity.class);
        it.putExtra("step_id", stepId);
        startActivity(it);
    }

    /**
     * Refresh step list (including retrieve data and update view)
     */
    public void refreshStepList(){
        //retrieve data
        this.stepList = new GlobalConfig().getAllStepRecord(this);

        //update view: ListAdapter
        StepListAdapter adpr = new StepListAdapter(this);
        adpr.setStepList(this.stepList);
        lsvStep.setAdapter(adpr);

        //windows size
        int[] ws = GlobalConfig.getWindowSize(this);
        int imgMaxWH = Math.min(ws[0], ws[1])/6;
        adpr.setImgMaxWH(imgMaxWH);

        //update view: total count
        tvTotal.setText("Total: " + this.stepList.size());
        tvNoStepMsg.setVisibility( (this.stepList.size() > 0) ? View.GONE : View.VISIBLE);

        //update view: debug info
        String dbPath = new GlobalConfig().getDBPath(this);
        String picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String debugText = String.format("dbPath=[%s]\npicDir=[%s]\nwindows size=[%dx%d]\nimgMaxWH=[%d]", dbPath, picDir, ws[0], ws[1], imgMaxWH);
        tvDebugInfo.setText(debugText);
    }

    /**
     * DEV API: switch the dev mode
     */
    int clickTimes = 0; //in case user click
    public void switchDevMode(View v){
        if(clickTimes == -1){
            new AlertDialog.Builder(this)
                    .setTitle("#DevMode#")
                    .setMessage("Dev Mode is disabled!")
                    .setPositiveButton("Close", null)
                    .show();
        }
        clickTimes++;
        GlobalConfig.devMode = false;
        if(clickTimes == 5){
            GlobalConfig.devMode = true;
            clickTimes = -1;
            new AlertDialog.Builder(this)
                    .setTitle("#DevMode#")
                    .setMessage("Dev Mode is enabled!")
                    .setPositiveButton("Close", null)
                    .show();
        }
    }

    /**
     * DEV Tool: Reset data
     */
    public void doResetData(View v){
        boolean bool = new GlobalConfig().clearData(this);
        if(bool == false){
            GlobalConfig.debugTrace(this, 1, "doClearData failed");
        }
        else {
            GlobalConfig.debugTrace(this, 0, "doClearData done");
        }
    }

    /**
     * DEV Tool: Build Demo data
     */
    public void doBuildDemoData(View v){
        new GlobalConfig().buildDemoData(this);
        GlobalConfig.debugTrace(this, 0, "doBuildDemoData done");
    }
}

class StepListAdapter extends BaseAdapter {
    private LayoutInflater stepListInflater;
    ArrayList<HashMap<String, String>> stepList;
    int imgMaxWH = 0;

    /**
     * Constructor
     * @param c context
     */
    public StepListAdapter(Context c){
        stepListInflater = LayoutInflater.from(c);
    }

    /**
     * Set the stepList
     * @param _stepList stepList
     */
    public void setStepList(ArrayList<HashMap<String, String>> _stepList){
       this.stepList = _stepList;
    }

    /**
     * Set the imgMaxWH
     * @param _imgMaxWH imgMaxWH
     */
    public void setImgMaxWH(int _imgMaxWH){
        this.imgMaxWH = _imgMaxWH;
    }

    @Override
    public int getCount() {
        return stepList.size();
    }

    @Override
    public Object getItem(int position) {
        return stepList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(stepList.get(position).get("_id"));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap<String, String> record;
        String debugText = "";
        long tempLong = 0;

        convertView = stepListInflater.inflate(R.layout.step_list, null);

        record = stepList.get(position);

        //initial view
        ImageView imvPhoto = (ImageView) convertView.findViewById(R.id.imv_photo);
        TextView tvStepId = (TextView) convertView.findViewById(R.id.tv_step_id);
        TextView tvSubject = (TextView) convertView.findViewById(R.id.tv_subject);
        TextView tvPlace = (TextView) convertView.findViewById(R.id.tv_place);
        TextView tvEventTime = (TextView) convertView.findViewById(R.id.tv_event_time);
        TextView tvDebugInfo = (TextView) convertView.findViewById(R.id.tv_debug_info);
        LinearLayout llDebugBlock = (LinearLayout) convertView.findViewById(R.id.ll_debug_block);
        if(GlobalConfig.devMode == false){
            tvDebugInfo.setVisibility(View.GONE);
        }

        //imvPhoto
        Uri imgUri = Uri.parse("file://" + record.get("imgPath"));
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

            debugText += "----------- imgPhoto --------\n";
            debugText += "imgMaxWH=["+ imgMaxWH + "]\t";
            debugText += "Orig size=["+ imgOrigW + "x" + imgOrigH +"]\t";
            debugText += "Load size=["+ bmp.getWidth() + "x" + bmp.getHeight() +"]\t";
            debugText += "Show size=["+ imvPhoto.getWidth() + "x" + imvPhoto.getHeight() +"]\t";
            debugText += "Scale=["+ imgScale +"]\n";
        }
        else{
            debugText += "----------- imgPhoto --------\n";
            debugText += "file not found!\n";
        }

        //record content
        tvStepId.setText(String.format("#%05d", Integer.parseInt(record.get("_id"))));
        tvSubject.setText(record.get("subject"));
        tvPlace.setText("At " + record.get("placeTag"));
        tempLong = Long.parseLong(record.get("eventTime"));
        tvEventTime.setText(GlobalConfig.convertTimestampToHumanTime(tempLong));

        debugText += "----------- Record content --------\n";
        Set<String> colNameList = record.keySet();
        for(String colName: colNameList){
            debugText += colName + "=[" + record.get(colName) +"]\n";
        }
        debugText += "-------------------\n";

        tvDebugInfo.setText(debugText);
        llDebugBlock.setVisibility((GlobalConfig.devMode == false)?View.GONE:View.VISIBLE);

        return convertView;
    }
}
