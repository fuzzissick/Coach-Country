package com.example.quade_laptop.coachcountry;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

public class LiveSession {
    private static final String TAG = "LiveSession";
    private Pace currentPace;
    private String currentDuration;
    private GeoPoint currentLocation;
    private Double currentDistance;
    private Boolean running;


    public Pace getCurrentPace() {
        return currentPace;
    }

    public void setCurrentPace(Pace currentPace) {
        this.currentPace = currentPace;
    }

    public String getCurrentDuration() {
        return currentDuration;
    }

    public void setCurrentDuration(long duration) {
        int h   = (int)(duration /3600000);
        int m = (int)(duration - h*3600000)/60000;
        int s= (int)(duration - h*3600000- m*60000)/1000 ;
        this.currentDuration = (h < 10 ? "0"+h: h)+":"+(m < 10 ? "0"+m: m)+":"+ (s < 10 ? "0"+s: s);
    }

    public GeoPoint getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = new GeoPoint(currentLocation.latitude,currentLocation.longitude);
    }

    public Double getCurrentDistance() {
        return currentDistance;
    }

    public void setCurrentDistance(Double currentDistance) {
        this.currentDistance = currentDistance;
    }

    public Boolean getRunning(){
        return running;
    }
    public void setRunning(Boolean running){
        this.running = running;
    }

    public void update(DocumentReference userDocRef){
        userDocRef.update(
                "LiveSession.running",this.running,
                "LiveSession.currentDistance", this.currentDistance,
                "LiveSession.currentPace", currentPace.getMappedObject(),
                "LiveSession.currentDuration", this.currentDuration,
                "LiveSession.currentLocation", this.currentLocation
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully updated!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    LiveSession(){
        currentPace = new Pace(0,0);
        currentDistance = 0.0;
        currentDuration = "";
        currentLocation = null;
        running = false;
    }
}
