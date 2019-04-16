package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import static com.example.quade_laptop.coachcountry.MainActivity.ANONYMOUS;

public class CoachHomePage extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "Coaching Home Page";
    private GoogleMap runningMap;
    private List<Runner> mapRunners;
    private HashMap<String,Marker> mapMarkers;
    private SupportMapFragment runningFragment;
    private RecyclerView onlineRunnersRV;
    private GridView gridView;
    private boolean firsttime;
    private LinearLayoutManager llm;
    private float zoomlevel;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFunctions mFunctions;
    private String mUsername;
    private DocumentSnapshot coachDoc;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_home_page);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        firsttime = true;
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
        //onlineRunnersRV = (RecyclerView)findViewById(R.id.rv);
        gridView = (GridView)findViewById(R.id.gv);
        llm = new LinearLayoutManager(getApplicationContext());


        //Init toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.c_h_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        //init navbar
        NavigationView navigationView = findViewById(R.id.c_h_navbar);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.yourRunners:
                                startActivity(new Intent(CoachHomePage.this, CoachSessionsHistory.class));
                                finish();
                                return true;
                        }
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // init firestore
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference coachDocument = db.collection("coaches").document(mFirebaseUser.getUid().toString());

        mapMarkers = new HashMap<>();


        //get the coaching document
        coachDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    coachDoc = task.getResult();
                    //set up recycler view based on coach
                    setUpRV(db);
                    Log.d(TAG, "SUCCESS");
                    return;
                }
                else{
                    Log.d(TAG, "Doesn't Exist");
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapMarkers.get(mapRunners.get(position).getDocumentID()).showInfoWindow();
                runningMap.animateCamera((CameraUpdateFactory.newLatLngZoom(new LatLng(mapRunners.get(position).getLiveSession().getCurrentLocation().getLatitude(),
                        mapRunners.get(position).getLiveSession().getCurrentLocation().getLongitude()),
                        zoomlevel)));
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(CoachHomePage.this, mapRunners.get(position).getFullName(),Toast.LENGTH_LONG).show();

                Intent i = new Intent(CoachHomePage.this, CoachSessionsHistory.class);
                i.putExtra("runnerID", mapRunners.get(position).getDocumentID());
                startActivity(i);
                finish();
                return false;
            }
        });

    }

    //grab all runners that are a part of the coaches team and that are presently online
    private void setUpRV(FirebaseFirestore db){
        db.collection("runners")
                .whereEqualTo("team", coachDoc.get("team"))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        //grab every runner and populate a list
                        List<Runner> runners = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Runner runner = doc.toObject(Runner.class);
                            runner.setDocumentID(doc.getId());
                            runners.add(runner);
                        }


                        mapRunners = runners;
                        //
                        //RunnerRVAdapter adapter = new RunnerRVAdapter(runners,CoachHomePage.this);

                        CurrentRunnersAdapter adapter = new CurrentRunnersAdapter(runners, CoachHomePage.this);

                        if (firsttime == false) {
                                //onlineRunnersRV.setAdapter(adapter);
                                gridView.setAdapter(adapter);
                        } else {
                            firsttime = false;
                            //onlineRunnersRV.setAdapter(adapter);
                            gridView.setAdapter(adapter);
                        }





                        //add and update markers on the map.
                        //Currently not what I want it to be, I want the blips to not unfocus and just update the information on them

                        for (Runner runner : mapRunners) {
                            if (mapMarkers.containsKey(runner.getDocumentID())) {
                                //Update pos
                                LatLng pos = new LatLng(runner.getLiveSession().getCurrentLocation().getLatitude(),
                                        runner.getLiveSession().getCurrentLocation().getLongitude());
                                //check if pos changed
                                if (pos != mapMarkers.get(runner.getDocumentID()).getPosition()) {
                                    mapMarkers.get(runner.getDocumentID()).setPosition(pos);
                                    mapMarkers.get(runner.getDocumentID()).setSnippet(runner.getLiveSession().getPaceOrStatus());
                                    if(mapMarkers.get(runner.getDocumentID()).isInfoWindowShown()){
                                        runningMap.animateCamera((CameraUpdateFactory.newLatLngZoom(pos, zoomlevel)));
                                    }
                                }
                            } else {
                                Marker newM = runningMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(runner.getLiveSession().getCurrentLocation().getLatitude(),
                                                runner.getLiveSession().getCurrentLocation().getLongitude()))
                                        .title(runner.getFullName())
                                        .snippet(runner.getLiveSession().getPaceOrStatus())
                                        .icon(BitmapDescriptorFactory.defaultMarker(Integer.parseInt(runner.getColor())))
                                );
                                mapMarkers.put(runner.getDocumentID(), newM);
                            }
                        }


                        Iterator<String> iterator = mapMarkers.keySet().iterator();

                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            Marker current = mapMarkers.get(key);
                            boolean found = false;
                            for (Runner runner : mapRunners) {
                                if (key.equals(runner.getDocumentID())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                current.remove();
                                iterator.remove();
                                found = false;
                            }
                        }



/*                        for (String key : mapMarkers.keySet()) {
                            boolean found = false;
                            for(Runner runner: mapRunners){
                                if(key == runner.getDocumentID()){
                                    found = true;
                                    break;
                                }
                            }
                            if(!found){
                                mapMarkers.remove(key);
                                runningMap.
                                found = false;
                            }
                        }*/
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, CoachCountrySignIn.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // DO WHATEVER YOU WANT WITH GOOGLEMAP
        runningMap = map;
        runningMap.setIndoorEnabled(true);

        runningMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = runningMap.getCameraPosition();
                zoomlevel = cameraPosition.zoom;
            }
        });

    }
}
