package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.quade_laptop.coachcountry.MainActivity.ANONYMOUS;

public class CoachHomePage extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "Coaching Home Page";
    private GoogleMap runningMap;
    private SupportMapFragment runningFragment;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFunctions mFunctions;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_home_page);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, CoachCountrySignIn.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }

        //init map
        runningFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.runningMap);
        runningFragment.getMapAsync(this);

        // init firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("runners").document(mFirebaseUser.getUid()).collection("sessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    QuerySnapshot result = task.getResult();
                    List<DocumentSnapshot> results = result.getDocuments();
                    if(results.size() == 0)
                        Toast.makeText(getApplicationContext(), "There are no sessions to show.", Toast.LENGTH_LONG);
                    else
                        for(DocumentSnapshot sessionDoc : results){
                            List<GeoPoint> locations = new ArrayList<GeoPoint>();
                            locations = (List<GeoPoint>) sessionDoc.get("locations");
                            Pace sessionPace = new Pace(Integer.parseInt(sessionDoc.get("sessionPace.minute").toString()),Integer.parseInt(sessionDoc.get("sessionPace.seconds").toString()));
                            sessionsSummaryList.add(new CCSession(
                                    sessionDoc.getId(),
                                    (Date)sessionDoc.get("sessionDate"),
                                    locations,
                                    sessionDoc.get("sessionDuration").toString(),
                                    sessionPace,
                                    Double.parseDouble(sessionDoc.get("sessionDistance").toString()),
                                    Integer.parseInt(sessionDoc.get("sessionNum").toString())
                            ));
                        }
                    SessionRVAdapter adapter = new SessionRVAdapter(sessionsSummaryList,SessionHistory.this);
                    sessionHistoryRV.setAdapter(adapter);
                }
            }
        });


        db.collection("runners")
                .whereEqualTo("team", "carthage")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<String> cities = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("name") != null) {
                                cities.add(doc.getString("name"));
                            }
                        }
                        Log.d(TAG, "Current cites in CA: " + cities);
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // DO WHATEVER YOU WANT WITH GOOGLEMAP
        runningMap = map;
        runningMap.setIndoorEnabled(true);

    }
}
