package com.example.quade_laptop.coachcountry;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class GPSService extends Service {
    private LocationListener listener;
    private LocationManager locationManager;
    private List<Location> locations;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    locations = new ArrayList<Location>();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locations.add(location);
                Intent i = new Intent("location_update");
                if(locations.size() != 1){
                    i.putExtra("prevLong" , locations.get(locations.size() - 2).getLongitude());
                    i.putExtra("prevLat" , locations.get(locations.size() - 2).getLatitude());
                    Double distance = SphericalUtil.computeDistanceBetween(new LatLng(locations.get(locations.size() - 2).getLatitude(),locations.get(locations.size() - 2).getLongitude()), new LatLng(location.getLatitude(),location.getLongitude()));
                    i.putExtra("distance", distance);
                }
                i.putExtra("longitude",location.getLongitude());
                i.putExtra("latitude", location.getLatitude());
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };



        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,20000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}
