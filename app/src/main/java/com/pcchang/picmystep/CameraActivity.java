/**
 * @Purpose CameraActivity
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class CameraActivity extends AppCompatActivity {
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        gotoCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the file path of new photo
     * @return file path
     */
    public String getPhotoPath(){
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String fileName = "photo" + System.currentTimeMillis() + ".jpg";
        return String.format("file://%s/%s", dir, fileName);
    }

    /**
     * Go to activity: Camera
     */
    public void gotoCamera(){
        String imgPath = getPhotoPath();
        imgUri = Uri.parse(imgPath);

        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(it, 100);
    }

    /**
     * Go to activity: Edit Step as New
     */
    public void gotoEditStepAsNew(){
        Intent it = new Intent(this, StepEditActivity.class);
        it.putExtra("edit_type", 'n');
        it.putExtra("img_path", imgUri.getPath());
        startActivity(it);
    }

    /**
     * Deal with camera activity result
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            switch(requestCode){
                case 100:
                    //set for system sharing
                    Intent it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imgUri);
                    sendBroadcast(it);

                    gotoEditStepAsNew();
                    break;
                default:
                    finish();
            }
        }
        finish();
    }
}
