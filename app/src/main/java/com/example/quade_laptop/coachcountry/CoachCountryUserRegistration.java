package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CoachCountryUserRegistration extends AppCompatActivity {

    private static final String TAG = "CoachCountryUserReg";

    private Button registerUser;
    private Spinner teamChooser;
    private Spinner eventChooser;
    private Spinner yearChooser;


    private Intent User;

    private FirebaseFirestore db;

    private String UID;

    private class team {
        String name;
        String mascot;
        String coach;
        String location;
    }

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private ArrayList<team> teamsArray;
    private Random rand;

    int currentSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_country_user_registration);
        User = getIntent();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        rand = new Random();

       teamChooser = (Spinner) findViewById(R.id.spinner_team);
       eventChooser = (Spinner) findViewById(R.id.spinner_event);
       yearChooser = (Spinner) findViewById(R.id.spinner_year);
       teamsArray = new ArrayList<team>();
       registerUser = (Button) findViewById(R.id.register_user);


        db = FirebaseFirestore.getInstance();

        renderTeams();

       teamChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (currentSelection != position){
                        for(team temp: teamsArray){
                            if(teamChooser.getItemAtPosition(position).toString() == temp.name) {
                                TextView coachView = (TextView) findViewById(R.id.field_coach);
                                coachView.setText(temp.coach);
                                TextView mascotView = (TextView) findViewById(R.id.field_mascot);
                                mascotView.setText(temp.mascot);
                                TextView locationView = (TextView) findViewById(R.id.field_location);
                                locationView.setText(temp.location);
                            }
                        }
                    }
                    currentSelection = position;
                }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

       registerUser.setOnClickListener(registerUserHandeler);
    }

    View.OnClickListener registerUserHandeler = new View.OnClickListener() {
        public void onClick(View v) {
            registerEmail();
        }
    };

    private void registerEmail(){
        final CollectionReference runners = db.collection("runners");
        String[] split = mFirebaseUser.getDisplayName().toString().split("\\s+");
        Pace fakePace = new Pace(0,0);

        final Map<String,Object> liveSession = new HashMap<>();
        liveSession.put("currentDuration", "00:00:00");
        liveSession.put("currentPace", fakePace.getMappedObject());
        liveSession.put("currentLocation", new GeoPoint(0,0));
        liveSession.put("running", false);
        liveSession.put("currentDistance", 0.0);


        final Map<String, Object> data = new HashMap<>();
        data.put("firstName",split[0]);
        if (split.length != 2)
            data.put("lastName", "");
        else
            data.put("lastName", split[1]);
        data.put("event", eventChooser.getSelectedItem().toString());
        data.put("year", yearChooser.getSelectedItem().toString());
        data.put("color", new Integer(rand.nextInt( 361)).toString());
        data.put("online", true);
        data.put("LiveSession", liveSession);
        db.collection("teams")
                .whereEqualTo("teamName", teamChooser.getSelectedItem().toString()) // <-- This line
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                data.put("team", document.getId());
                                runners.document(mFirebaseUser.getUid()).set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Successfully wrote new runner!");
                                        startActivity(new Intent(CoachCountryUserRegistration.this, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "ERROR writing document ", e);
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }


    //render teams from database in spinner
    private void renderTeams(){
        CollectionReference teamsRef = db.collection("teams");
        final List<String>  teams  = new ArrayList<String>();

        db.collection("teams")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot document :task.getResult()){
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Log.d(TAG, document.get("teamName").toString());
                                teams.add(document.get("teamName").toString());

                                team temp = new team();
                                temp.coach = document.get("teamCoach").toString();
                                temp.name = document.get("teamName").toString();
                                temp.mascot = document.get("teamMascot").toString();
                                temp.location = document.get("teamLocation").toString();
                                teamsArray.add(temp);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(CoachCountryUserRegistration.this, android.R.layout.simple_spinner_item, teams);
                            teamChooser = (Spinner) findViewById(R.id.spinner_team);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            teamChooser.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            currentSelection = 0;
                        }
                        else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });



    }
}
