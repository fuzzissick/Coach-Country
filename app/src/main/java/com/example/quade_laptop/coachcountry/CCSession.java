package com.example.quade_laptop.coachcountry;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CCSession {
    private String documentID;
    private Double sessionDistance;
    private Pace sessionPace;
    private String sessionDuration;
    private Timestamp sessionDate;
    private int sessionNum;
    private List<GeoPoint> locations;


    public int getSessionNum() {
        return sessionNum;
    }

    public void setSessionNum(int sessionNum) {
        this.sessionNum = sessionNum;
    }


    public Double getSessionDistance() {
        return sessionDistance;
    }

    public void setSessionDistance(Double distance) {
        this.sessionDistance = distance;
    }

    public Pace getSessionPace() {
        return sessionPace;
    }

    public void setSessionPace(Pace averagePace) {
        this.sessionPace = averagePace;
    }

    public String getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(long duration) {
        int h   = (int)(duration /3600000);
        int m = (int)(duration - h*3600000)/60000;
        int s= (int)(duration - h*3600000- m*60000)/1000 ;
        this.sessionDuration = (h < 10 ? "0"+h: h)+":"+(m < 10 ? "0"+m: m)+":"+ (s < 10 ? "0"+s: s);
    }

    public Timestamp getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Timestamp sessionDate) {
        this.sessionDate = sessionDate;
    }

    public List<GeoPoint> getLocations() {
        return locations;
    }

    public void setLocations(List<GeoPoint> locations) {
        this.locations = locations;
    }

    public void addLocation(LatLng location){
        GeoPoint local = new GeoPoint(location.latitude, location.longitude);
        locations.add(local);
    }

    public String getDocumentID(){
        return documentID;
    }

    CCSession() {
        sessionDate = Timestamp.now();
        locations = new ArrayList<GeoPoint>();
        sessionDuration = "";
        sessionPace = new Pace(0, 0);
        sessionDistance = 0.0;
        sessionNum = 0;
    }

    CCSession(String documentID, Date sessionDate, List<GeoPoint> locations, String sessionDuration, Pace sessionPace, Double sessionDistance, int sessionNum){
        this.documentID = documentID;
        this.sessionDate = new Timestamp(sessionDate);
        this.locations = locations;
        this.sessionDuration = sessionDuration;
        this.sessionPace = sessionPace;
        this.sessionDistance = sessionDistance;
        this.sessionNum = sessionNum;
    }


}
