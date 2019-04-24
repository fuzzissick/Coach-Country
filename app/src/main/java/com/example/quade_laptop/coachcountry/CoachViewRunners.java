package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CoachViewRunners extends AppCompatActivity {
    private static final String TAG = "CoachVRunners";

    RecyclerView sessionHistoryRV;
    LinearLayoutManager llm;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DrawerLayout mDrawerLayout;
    private ArrayList<Runner> runners;
    private RecyclerView runnerRV;
    private DocumentSnapshot coachDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_view_runners);

        Intent i = getIntent();

        //Init toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.c_h_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        NavigationView navigationView = findViewById(R.id.c_h_navbar);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.coachDashboard:
                                startActivity(new Intent(CoachViewRunners.this, CoachHomePage.class));
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
        mDrawerLayout = findViewById(R.id.c_h_drawer_layout);


        runnerRV =  findViewById(R.id.runnerRV);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference coachDocument = db.collection("coaches").document(mFirebaseUser.getUid().toString());
        sessionHistoryRV = (RecyclerView)findViewById(R.id.runnerRV);
        llm = new LinearLayoutManager(getApplicationContext());
        sessionHistoryRV.setLayoutManager(llm);
        runners = new ArrayList<Runner>();
        //get the coaching document
        coachDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    coachDoc = task.getResult();
                    getRunnersData();
                    return;
                }
                else{
                    Log.d(TAG, "Doesn't Exist");
                }
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
                startActivity(new Intent(this, CoachCountrySignIn.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getRunnersData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("runners").whereEqualTo("team", coachDoc.get("team")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    QuerySnapshot result = task.getResult();
                    List<DocumentSnapshot> results = result.getDocuments();

                    List<Runner> runners = new ArrayList<>();
                    for (DocumentSnapshot doc : results) {
                        Runner runner = doc.toObject(Runner.class);
                        runner.setDocumentID(doc.getId());
                        runners.add(runner);
                    }
                    if(results.size() == 0)
                        Toast.makeText(getApplicationContext(), "There are no runners to show.", Toast.LENGTH_LONG);
                    else{
                        RunnerRVAdapter adapter = new RunnerRVAdapter(runners,CoachViewRunners.this);
                        runnerRV.setAdapter(adapter);
                }
            }
        }
        });
        TextView Team = findViewById(R.id.school_name);
        Team.setText(coachDoc.get("team").toString());
    }
}
