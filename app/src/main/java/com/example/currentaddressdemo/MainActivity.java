package com.example.currentaddressdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DEBUG_LOGGING = "debuglogging";
    private final static int LOCATION_PERMISSION_REQUEST = 1000;
    private GoogleApiClient mGoogleApiClient;

    private Button mGetAddressButton;
    private TextView mAddressTextView;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        initViews();
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(DEBUG_LOGGING, TAG + " : onConnected() : connection success");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(DEBUG_LOGGING, TAG + " : onConnectionSuspended() : connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(DEBUG_LOGGING, TAG + " : onConnectionFailed() : connection failed");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private void initViews() {
        mGetAddressButton = (Button) findViewById(R.id.getAddressButton);
        mGetAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLatitudeLongitude();
            }
        });

        mAddressTextView = (TextView) findViewById(R.id.addressTextView);
    }

    private void fetchLatitudeLongitude() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            // Has location permission

            // Get latitude and longitude
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                String latitudeLongitude = "latitude : " + latitude + ", longitude : " + longitude + "\n\n";

                // Get address
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    Log.w(DEBUG_LOGGING, TAG + " : fetchLatitudeLongitude() : fetching address using geocoder");
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        Log.w(DEBUG_LOGGING, TAG + " : fetchLatitudeLongitude() : address : " + address);
                        String addressLine = address.getAddressLine(0);
                        mAddressTextView.setText(latitudeLongitude + addressLine);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                mAddressTextView.setText("Couldn't get the location. Make sure location is enabled on the device");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLatitudeLongitude();
                } else {
                    // Do nothing
                }
                return;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
