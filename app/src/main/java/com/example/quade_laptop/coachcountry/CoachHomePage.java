package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private List<Runner> mapRunners;
    private SupportMapFragment runningFragment;
    private RecyclerView onlineRunnersRV;
    private LinearLayoutManager llm;

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

        //init RV
        onlineRunnersRV = (RecyclerView)findViewById(R.id.rv);
        llm = new LinearLayoutManager(getApplicationContext());
        onlineRunnersRV.setLayoutManager(llm);

        // init firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();


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

                        List<Runner> runners = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Runner runner = doc.toObject(Runner.class);
                            if(runner.getLiveSession().getRunning()) {
                                runners.add(runner);
                            }
                        }

                        
                        mapRunners = runners;
                        RunnerRVAdapter adapter = new RunnerRVAdapter(runners,CoachHomePage.this);
                        onlineRunnersRV.setAdapter(adapter);
                        runningMap.clear();
                        for (Runner runner: mapRunners) {
                            runningMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(runner.getLiveSession().getCurrentLocation().getLatitude(),
                                            runner.getLiveSession().getCurrentLocation().getLongitude()))
                                    .title(runner.getFullName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(31))
                            );
                        }
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
