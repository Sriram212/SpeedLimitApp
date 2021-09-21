package com.speedlimitnotifyer;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    TextView currentSpeed;
    LocationListener locationListener;
    LocationManager locationManager;
    /* access modifiers changed from: private */
    public GoogleMap mMap;
    MediaPlayer mediaPlayer;
    LatLng myLocation;
    int speedInt;
    TextView speedLimit;
    int speedLimitInt;
    String speedLimitText;
    String speedText;

    public class DownloadTask extends AsyncTask<String, Void, String> {
        public DownloadTask() {
        }

        /* access modifiers changed from: protected */
        public String doInBackground(String... strArr) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(((HttpURLConnection) new URL(strArr[0]).openConnection()).getInputStream());
                String str = "";
                for (int read = inputStreamReader.read(); read != -1; read = inputStreamReader.read()) {
                    str = str + ((char) read);
                }
                return str;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(String str) {
            super.onPostExecute(str);
            Log.i("JSON", str);
            try {
                JSONArray jSONArray = new JSONArray(new JSONObject(str).getString("resourceSets"));
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONArray jSONArray2 = new JSONArray(jSONArray.getJSONObject(i).getString("resources"));
                    for (int i2 = 0; i2 < jSONArray2.length(); i2++) {
                        JSONArray jSONArray3 = new JSONArray(jSONArray2.getJSONObject(i2).getString("snappedPoints"));
                        for (int i3 = 0; i3 < jSONArray3.length(); i3++) {
                            JSONObject jSONObject = jSONArray3.getJSONObject(i3);
                            Log.i("Speed Limit", jSONObject.getString("speedLimit"));
                            MapsActivity.this.speedLimitText = jSONObject.getString("speedLimit");
                            MapsActivity.this.speedLimitInt = Integer.parseInt(MapsActivity.this.speedLimitText);
                        }
                    }
                }
            } catch (JSONException unused) {
            }
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (iArr.length > 0 && iArr[0] == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            this.locationManager.requestLocationUpdates("gps", 2000, 0.0f, this.locationListener);
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_maps);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        this.mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
    }

    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        this.speedLimit = (TextView) findViewById(R.id.speedLimit);
        this.currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        this.speedLimit.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.currentSpeed.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.locationManager = (LocationManager) getSystemService("location");
        this.locationListener = new LocationListener() {
            public void onProviderDisabled(String str) {
            }

            public void onProviderEnabled(String str) {
            }

            public void onStatusChanged(String str, int i, Bundle bundle) {
            }

            public void onLocationChanged(Location location) {
                Log.i("Location", location.toString());
                MapsActivity.this.myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                MapsActivity.this.mMap.clear();
                MapsActivity.this.mMap.addMarker(new MarkerOptions().position(MapsActivity.this.myLocation).title("My Location"));
                MapsActivity.this.mMap.moveCamera(CameraUpdateFactory.newLatLng(MapsActivity.this.myLocation));
                MapsActivity.this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsActivity.this.myLocation, 12.0f));
                MapsActivity.this.myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                String str = "https://dev.virtualearth.net/REST/v1/Routes/SnapToRoad?points=" + Double.toString(MapsActivity.this.myLocation.latitude) + "," + Double.toString(MapsActivity.this.myLocation.longitude) + "&includeTruckSpeedLimit=" + "false" + "&IncludeSpeedLimit=" + "true" + "&speedUnit=" + "MPH" + "&travelMode=" + "driving" + "&key=" + "AjhUPXh7oQRA3eKvdmBAo-YBPuIAEifVWnb58m9G2HCNaG1kXY8oTA4pfozWm-oI";
                Log.i("URL", str);
                new DownloadTask().execute(new String[]{str});
                MapsActivity.this.speedInt = (int) (((double) location.getSpeed()) * 2.236936d);
                MapsActivity.this.speedText = "Your Speed: " + MapsActivity.this.speedInt;
                SpannableString spannableString = new SpannableString(MapsActivity.this.speedText);
                if (MapsActivity.this.speedLimitInt == 0) {
                    spannableString.setSpan(new ForegroundColorSpan(ViewCompat.MEASURED_STATE_MASK), 11, MapsActivity.this.speedText.length(), 33);
                    MapsActivity.this.speedLimit.setText("Speed Limit: N/A");
                    MapsActivity.this.mediaPlayer.stop();
                } else if (MapsActivity.this.speedInt > MapsActivity.this.speedLimitInt) {
                    spannableString.setSpan(new ForegroundColorSpan(SupportMenu.CATEGORY_MASK), 11, MapsActivity.this.speedText.length(), 33);
                    MapsActivity.this.speedLimit.setText("Speed Limit: " + MapsActivity.this.speedLimitInt);
                    MapsActivity.this.mediaPlayer.start();
                } else {
                    spannableString.setSpan(new ForegroundColorSpan(ViewCompat.MEASURED_STATE_MASK), 11, MapsActivity.this.speedText.length(), 33);
                    MapsActivity.this.speedLimit.setText("Speed Limit: " + MapsActivity.this.speedLimitInt);
                    MapsActivity.this.mediaPlayer.stop();
                }
                MapsActivity.this.currentSpeed.setText(spannableString, TextView.BufferType.SPANNABLE);
            }
        };
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
        } else {
            this.locationManager.requestLocationUpdates("gps", 2000, 0.0f, this.locationListener);
        }
    }
}
