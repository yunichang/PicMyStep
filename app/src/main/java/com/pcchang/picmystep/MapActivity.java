/**
 * @Purpose MapActivity
 * @author Yuni Chang
 */
package com.pcchang.picmystep;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity
        implements LocationListener {

    LocationManager locMgr;
    TextView tvLocInfo, tvDebugMsg;
    GoogleMap map;
    LatLng currPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        GlobalConfig.debugTrace(this, 0, "MapActivity onCreate start");

        // Initial View
        tvLocInfo = (TextView) findViewById(R.id.tv_loc_info);
        tvDebugMsg = (TextView) findViewById(R.id.tv_debug_msg);
        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initial Map
        setupMapIfNeeded();

        //devMode
        if(GlobalConfig.devMode == false){
            tvDebugMsg.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        doLocUpdateRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();

        doLocUpdateRemove();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            // Refresh
            case R.id.refresh:
                refreshCurrLocStatus(map.getMyLocation());
                map.animateCamera(CameraUpdateFactory.newLatLng(currPoint));
                break;
            // Mark
            case R.id.mark:
                LatLng target = map.getCameraPosition().target;
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(map.getCameraPosition().target)
                        .title(GlobalConfig.getAddrByLatLon(this, target.latitude, target.longitude)));
                break;
            // Satellite
            case R.id.satellite:
                item.setChecked(!item.isChecked());
                if(item.isChecked()){
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                else{
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
            // Traffic
            case R.id.traffic:
                item.setChecked(!item.isChecked());
                map.setTrafficEnabled(item.isChecked());
                break;
            // Current Location
            case R.id.curr_location:
                map.animateCamera(CameraUpdateFactory.newLatLng(currPoint));
                break;
            // Location Settings
            case R.id.location_settings:
                GlobalConfig.startActivityLocationSetting(this);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        GlobalConfig.debugTrace(this, 0, "MapActivity onLocationChanged");
        refreshCurrLocStatus(location);
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
     * Set up Map Fragment
     */
    public void setupMapIfNeeded(){
        GlobalConfig.debugTrace(this, 0, "MapActivity setupMapIfNeeded start");
        try {
            if (map == null) {
                map = ((SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.frg_map)).getMap();
                if (map != null) {
                    map.setMyLocationEnabled(true);
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    map.moveCamera(CameraUpdateFactory.zoomTo(18));

                    currPoint = map.getCameraPosition().target;
                }
            }

            if (currPoint == null) {
                currPoint = new LatLng(0, 0);
            }
        }
        catch(Exception e){
            GlobalConfig.debugTrace(this, 2, "MapActivity setupMapIfNeeded");
        }
    }

    /**
     * Do location update request
     */
    public void doLocUpdateRequest(){
        GlobalConfig.debugTrace(this, 0, "MapActivity doLocUpdateRequest start");
        setupMapIfNeeded();
        String locMsg = "";
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
                GlobalConfig.debugTrace(this, 2, "MapActivity doLocUpdateRequest");
            }
        }
        tvLocInfo.setText(locMsg);
    }

    /**
     * Do location update remove
     */
    public void doLocUpdateRemove(){
        GlobalConfig.debugTrace(this, 0, "MapActivity doLocUpdateRemove start");
        try {
            locMgr.removeUpdates(this);
        }
        catch (Exception e) {
            GlobalConfig.debugTrace(this, 2, "MapActivity doLocUpdateRemove");
        }
    }

    /**
     * Refresh current location status
     */
    public void refreshCurrLocStatus(Location loc){
        double latitude = 0;
        double longitude = 0;
        double altitude = 0;
        String provider = "";
        String locMsg = "";
        String debugMsg = "";

        GlobalConfig.debugTrace(this, 0, "MapActivity refreshCurrLocStatus start");

        if(loc == null){
            locMsg = "Cannot get location information!!";
        }
        else{
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            altitude = loc.getAltitude();
            provider = loc.getProvider();

            locMsg = String.format("Provider: %s Latitude: %.5f Longitude: %.5f Altitude: %.2fm"
                        , provider, latitude, longitude, altitude);
            debugMsg = String.format("addr=[%s]", GlobalConfig.getAddrByLocation(this, loc));

            map.animateCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(loc.getLatitude(), loc.getLongitude())));

            map.moveCamera(CameraUpdateFactory.zoomTo(16));
            currPoint = new LatLng(loc.getLatitude(), loc.getLongitude());
        }
        tvLocInfo.setText(locMsg);
        tvDebugMsg.setText(debugMsg);
    }
}
