package com.example.quade_laptop.coachcountry;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Runner {
    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String eventGroup) {
        this.event = eventGroup;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }


    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }


    public LiveSession getLiveSession() {
        return LiveSession;
    }

    public void setLiveSession(LiveSession session) {
        this.LiveSession = session;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }


    private String documentID;
    private String firstName;
    private String lastName;
    private String event;

    private String team;
    private String year;
    private LiveSession LiveSession;

    private boolean online;
    private String color;


    Runner(QueryDocumentSnapshot docSnap){
        this.documentID = docSnap.getId();
        this.firstName = docSnap.get("firstName").toString();
        this.lastName = docSnap.get("lastName").toString();
        this.event = docSnap.get("event").toString();
        this.year = docSnap.get("year").toString();
        this.team = docSnap.get("team").toString();
        this.color = docSnap.get("color").toString();
    }

    Runner(){
    };

}
