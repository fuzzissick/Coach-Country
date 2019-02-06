package com.example.quade_laptop.coachcountry;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.List;

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

    public String getEventGroup() {
        return eventGroup;
    }

    public void setEventGroup(String eventGroup) {
        this.eventGroup = eventGroup;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public LiveSession getSession() {
        return session;
    }

    public void setSession(LiveSession session) {
        this.session = session;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }

    private String documentID;
    private String firstName;
    private String lastName;
    private String eventGroup;
    private String year;
    private LiveSession session;

    Runner(DocumentSnapshot docSnap){
        this.documentID = docSnap.getId();
        this.firstName = docSnap.get("firstName").toString();
        this.lastName = docSnap.get("lastName").toString();
        this.eventGroup = docSnap.get("eventGroup").toString();
        this.year = docSnap.get("year").toString();
    }

}
