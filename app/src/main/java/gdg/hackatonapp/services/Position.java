package gdg.hackatonapp.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;

import gdg.hackatonapp.Application;

/**
 * Created by aortuno on 9/16/2015.
 */
public class Position extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationRequest locationRequest;

    private GoogleApiClient apiClient;

    @Override
    public void onCreate() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Application.locationUpdateTime);
        if (Application.locationMinDistance > 0) {
            locationRequest.setSmallestDisplacement(Application.locationMinDistance);
        }
        apiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        apiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(Location arg0) {
        Log.i(Application.tag, arg0.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onConnectionSuspended(int arg0) {

    }

}