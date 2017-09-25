package it.neptis.gopoleis.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.neptis.gopoleis.R;
import it.neptis.gopoleis.adapters.ClickListener;
import it.neptis.gopoleis.adapters.MedalAdapter;
import it.neptis.gopoleis.adapters.RecyclerTouchListener;
import it.neptis.gopoleis.model.Medal;

public class MedalsActivity extends AppCompatActivity {

    private static final String TAG = "MedalsActivity";

    private FirebaseAuth mAuth;
    private List<Medal> regionMedals, structuretypeMedals, historicalperiodMedals;
    private MedalAdapter regionsMedalAdapter;
    private MedalAdapter historicalPeriodMedalAdapter;
    private MedalAdapter structuretypeMedalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medals);

        mAuth = FirebaseAuth.getInstance();

        regionMedals = new ArrayList<>();
        structuretypeMedals = new ArrayList<>();
        historicalperiodMedals = new ArrayList<>();
        getPlayerMedals();

        RecyclerView regionsRecyclerView = (RecyclerView) findViewById(R.id.regionsRecyclerView);
        // TODO Get regionMedals list from server and pass it to adapter constructor, then implement onItemTouch
        regionsMedalAdapter = new MedalAdapter(this, regionMedals);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MedalsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        regionsRecyclerView.setLayoutManager(layoutManager);
        regionsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        regionsRecyclerView.setAdapter(regionsMedalAdapter);
        regionsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), regionsRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(MedalsActivity.this, regionMedals.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));

        RecyclerView historicalPeriodRecyclerView = (RecyclerView) findViewById(R.id.historicalPeriodRecyclerView);
        historicalPeriodMedalAdapter = new MedalAdapter(this, historicalperiodMedals);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(MedalsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        historicalPeriodRecyclerView.setLayoutManager(layoutManager2);
        historicalPeriodRecyclerView.setItemAnimator(new DefaultItemAnimator());
        historicalPeriodRecyclerView.setAdapter(historicalPeriodMedalAdapter);
        historicalPeriodRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), historicalPeriodRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(MedalsActivity.this, historicalperiodMedals.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));

        RecyclerView structuretypeRecyclerView = (RecyclerView) findViewById(R.id.typologyRecyclerView);
        structuretypeMedalAdapter = new MedalAdapter(this, structuretypeMedals);
        RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(MedalsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        structuretypeRecyclerView.setLayoutManager(layoutManager3);
        structuretypeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        structuretypeRecyclerView.setAdapter(structuretypeMedalAdapter);
        structuretypeRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), structuretypeRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(MedalsActivity.this, structuretypeMedals.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.medals);
    }

    private void getPlayerMedals() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.server_url) + "getPlayerMedals/" + mAuth.getCurrentUser().getEmail() + "/";
        JsonArrayRequest jsHeritageInfo = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsObj = (JSONObject) response.get(i);
                        Medal tempMedal = new Medal(jsObj.getInt("code"), jsObj.getString("name"), getString(R.string.server_url) + "images/medals/" + jsObj.getString("filename"), jsObj.getInt("category"));
                        if (tempMedal.getCategory() == 1)
                            regionMedals.add(tempMedal);
                        else if (tempMedal.getCategory() == 2)
                            historicalperiodMedals.add(tempMedal);
                        else if (tempMedal.getCategory() == 3)
                            structuretypeMedals.add(tempMedal);
                    }
                    regionsMedalAdapter.notifyDataSetChanged();
                    historicalPeriodMedalAdapter.notifyDataSetChanged();
                    structuretypeMedalAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        });

        queue.add(jsHeritageInfo);
    }

}