package com.example.inertialnavg1;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;
import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getPermissions();
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Location CurrentLoc = initInertialNav();
        double CurLat = CurrentLoc.getLatitude();
        double CurLon = CurrentLoc.getLongitude();

        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng you = new LatLng(CurLat, CurLon);
        mMap.addMarker(new MarkerOptions().position(you).title("you are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(you));
    }

    public static int tst = 0;
    public Location initInertialNav(){
        /*Context context = getApplicationContext();
        CharSequence text = "Hello toast!" + tst++;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();*/

        inNav tst = new inNav(this,this);
        tst.AlignmentManager();
        return tst.GLocation;

    }

    private void getPermissions(){
        int permissionCode = 777;
        String[] permissionsArr = {
                ACCESS_BACKGROUND_LOCATION,
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
        };

        if(!checkPermission()){
            ActivityCompat.requestPermissions(this, permissionsArr, permissionCode);
        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "granted";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == 777){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                initInertialNav();
            }
            else {
                //getPermissions(); //re-try getting permissions
            }
        }
        else{
             //code was intercepted shutdown or deploy countermeasures
            Context context = getApplicationContext();
            CharSequence text = "permission code was altered by an unknown source during runtime";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
