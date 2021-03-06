package it.neptis.gopoleis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.neptis.gopoleis.R;
import it.neptis.gopoleis.RequestQueueSingleton;
import it.neptis.gopoleis.model.GlideApp;

public class CardDetailsActivity extends AppCompatActivity {

    //private static final String TAG = "CardDetailsActivity";

    private ImageView image;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        TextView name = (TextView) findViewById(R.id.card_details_name);
        TextView rarity = (TextView) findViewById(R.id.card_details_rarity);
        TextView description = (TextView) findViewById(R.id.card_details_description);
        image = (ImageView) findViewById(R.id.imageView3);

        Intent launchingIntent = getIntent();
        code = launchingIntent.getStringExtra("cardCode");
        name.setText(launchingIntent.getStringExtra("cardName"));

        rarity.setText(String.format(getString(R.string.card_details_rarity), launchingIntent.getStringExtra("cardRarity")));
        description.setText(launchingIntent.getStringExtra("cardDescription"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.card_details);

        getSetCardImage();
    }

    private void getSetCardImage() {
        String url = getString(R.string.server_url) + "getCardByCode/" + code + "/";
        JsonArrayRequest jsHeritageInfo = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject jsObj = (JSONObject) response.get(0);
                    GlideApp.with(CardDetailsActivity.this).load(getString(R.string.server_url) + "images/cards/" + jsObj.getString("filename")).placeholder(R.drawable.progress_animation).error(R.drawable.noimage).into(image);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(CardDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueueSingleton.getInstance(this).addToRequestQueue(jsHeritageInfo);
    }

}