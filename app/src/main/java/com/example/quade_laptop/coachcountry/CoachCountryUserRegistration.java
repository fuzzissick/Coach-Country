package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CoachCountryUserRegistration extends AppCompatActivity {

    private static final String TAG = "CoachCountryUserReg";

    private Button registerUser;
    private Spinner teamChooser;
    private Spinner eventChooser;
    private Spinner yearChooser;

    private String password;
    private String username;

    private Intent User;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_country_user_registration);
        User = getIntent();

       username = User.getStringExtra("username");
       password = User.getStringExtra("password");
       teamChooser = (Spinner) findViewById(R.id.spinner_team);

       db = FirebaseFirestore.getInstance();

       renderTeams();
    }


    private void registerEmail(){
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

                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(CoachCountryUserRegistration.this, android.R.layout.simple_spinner_item, teams);
                            teamChooser = (Spinner) findViewById(R.id.spinner_team);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            teamChooser.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });



    }
}
