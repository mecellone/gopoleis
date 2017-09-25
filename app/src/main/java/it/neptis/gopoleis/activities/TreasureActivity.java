package it.neptis.gopoleis.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import it.neptis.gopoleis.R;
import it.neptis.gopoleis.adapters.CardAdapter;
import it.neptis.gopoleis.adapters.ClickListener;
import it.neptis.gopoleis.adapters.RecyclerTouchListener;
import it.neptis.gopoleis.model.Card;

public class TreasureActivity extends AppCompatActivity {

    private static final String TAG = "TreasureaActivity";

    private String treasureCode;

    private TextView info, latitude, longitude;
    private ImageView coffer;

    private FirebaseAuth mAuth;

    private List<Card> treas_card_list;
    private String c_name, c_cost, c_description, c_code;

    private String[] random_card_codes = new String[5];

    private CardAdapter cardAdapter;

    boolean opened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure);

        mAuth = FirebaseAuth.getInstance();

        opened = false;

        treasureCode = getIntent().getStringExtra("code");

        info = (TextView) findViewById(R.id.treasure_description);
        latitude = (TextView) findViewById(R.id.treasure_latitude);
        longitude = (TextView) findViewById(R.id.treasure_longitude);
        coffer = (ImageView) findViewById(R.id.treasure_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.booster_pack);

        getSetTreasureInfo();
    }

    private void treasureFound() {
        coffer.setImageResource(R.drawable.forziere_aperto);
        View dynamicTreasureView = findViewById(R.id.dynamicTreasureView);
        ViewGroup parent = (ViewGroup) dynamicTreasureView.getParent();
        int index = parent.indexOfChild(dynamicTreasureView);
        parent.removeView(dynamicTreasureView);
        dynamicTreasureView = getLayoutInflater().inflate(R.layout.treasure_found_text, parent, false);
        parent.addView(dynamicTreasureView, index);
    }

    private void treasureNotFound() {
        Button open_treasure = (Button) findViewById(R.id.open_treasure_button);
        open_treasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(v.getContext());
                String url = getString(R.string.server_url) + "addTreasToPlayer/" + mAuth.getCurrentUser().getEmail() + "/" + treasureCode + "/";

                // Request a string response from the provided URL.
                JsonObjectRequest jsAddTreasToGame = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        treasureOpened();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                });

                queue.add(jsAddTreasToGame);
            }
        });
    }

    private void treasureOpened() {
        // TODO Card rarity, also in layout.xml
        opened = true;
        //coffer.setImageResource(0);
        View dynamicTreasureView = findViewById(R.id.dynamicTreasureView);
        ViewGroup parent = (ViewGroup) dynamicTreasureView.getParent();
        int index = parent.indexOfChild(dynamicTreasureView);
        parent.removeView(dynamicTreasureView);
        dynamicTreasureView = getLayoutInflater().inflate(R.layout.cards_found, parent, false);
        parent.addView(dynamicTreasureView, index);

        generateCards();

        treas_card_list = new LinkedList<>();

        // ------------------------------------------------------------------------
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.carte_forziere);

        cardAdapter = new CardAdapter(TreasureActivity.this, treas_card_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cardAdapter);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Card card = treas_card_list.get(position);
                Intent openCardDetails = new Intent(TreasureActivity.this, CardDetailsActivity.class);
                openCardDetails.putExtra("cardName", card.getName());
                openCardDetails.putExtra("cardCost", card.getCost());
                openCardDetails.putExtra("cardDescription", card.getDescription());
                // TODO add card image
                startActivity(openCardDetails);
            }
        }));
        // ------------------------------------------------------------------------


        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.server_url) + "getFiveTreasureCardsInfo/";
        for (String tempString : random_card_codes)
            url += tempString + "/";

        JsonArrayRequest jsInfoCardTreasure = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsObj = (JSONObject) response.get(i);
                        c_code = jsObj.getString("code");
                        c_name = jsObj.getString("name");
                        c_cost = jsObj.getString("cost");
                        c_description = jsObj.getString("description");
                        String filename = jsObj.getString("filename");
                        treas_card_list.add(new Card(c_code, c_cost, c_name, c_description, getString(R.string.server_url) + "images/cards/" + filename));
                    }
                    cardAdapter.notifyDataSetChanged();
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

        queue.add(jsInfoCardTreasure);

        addCardsToCollection(random_card_codes);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(TreasureActivity.this);
        builder.setTitle(R.string.congratulations)
                .setMessage(R.string.congratulations_treasure)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.star_off)
                .show();
    }

    private void getSetTreasureInfo() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.server_url) + "getInfoTreasure/" + treasureCode + "/" + mAuth.getCurrentUser().getEmail() + "/";
        JsonArrayRequest jsHeritageInfo = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsObj = (JSONObject) response.get(i);
                        info.setText(jsObj.getString("description"));
                        latitude.setText(String.format(getString(R.string.latitude), jsObj.getString("latitude")));
                        longitude.setText(String.format(getString(R.string.longitude), jsObj.getString("longitude")));
                        if (jsObj.getInt("found") == 1) {
                            treasureFound();
                        } else {
                            treasureNotFound();
                        }
                    }
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

    public void generateCards() {
        // TODO hardcoded card number
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i <= 20; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < random_card_codes.length; i++) {
            random_card_codes[i] = String.valueOf(list.get(i));
        }
    }

    public void addCardsToCollection(String[] card_codes) {
        RequestQueue queue3 = Volley.newRequestQueue(this);
        String url = getString(R.string.server_url) + "addFiveCardsToUserCollection/" + mAuth.getCurrentUser().getEmail() + "/";
        for (String tempString : random_card_codes)
            url += tempString + "/";

        JsonObjectRequest jsAddCardToCollection = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        });

        queue3.add(jsAddCardToCollection);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (opened) {
            Intent backToTreasurePortal = new Intent();
            backToTreasurePortal.putExtra("code", treasureCode);
            setResult(Activity.RESULT_OK, backToTreasurePortal);
        } else
            setResult(Activity.RESULT_CANCELED);
        finish();
    }

}