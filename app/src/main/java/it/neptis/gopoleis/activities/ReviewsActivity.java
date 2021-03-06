package it.neptis.gopoleis.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.neptis.gopoleis.R;
import it.neptis.gopoleis.RequestQueueSingleton;
import it.neptis.gopoleis.adapters.ReviewAdapter;
import it.neptis.gopoleis.model.Review;

public class ReviewsActivity extends AppCompatActivity {

    //private static final String TAG = "ReviewsActivity";

    private List<Review> all_reviews;
    private ReviewAdapter reviewAdapter;
    private String heritageCode;
    private FirebaseAuth mAuth;
    private LinearLayout reviewsContainerLayout;
    private TextView noReviewsText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.reviews);

        reviewsContainerLayout = (LinearLayout) findViewById(R.id.reviews_container_layout);
        noReviewsText = new TextView(ReviewsActivity.this);
        noReviewsText.setText(R.string.no_reviews);
        noReviewsText.setTextSize(30);

        mAuth = FirebaseAuth.getInstance();

        heritageCode = getIntent().getStringExtra("code");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.reviews_recyclerView);

        all_reviews = new ArrayList<>();

        // ------------------------------------------------------------------------
        reviewAdapter = new ReviewAdapter(ReviewsActivity.this, all_reviews);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setNestedScrollingEnabled(true);
        // ------------------------------------------------------------------------

        getReviews();
    }

    private void getReviews() {
        //noinspection ConstantConditions
        String url = getString(R.string.server_url) + "getReviews/" + heritageCode + "/" + mAuth.getCurrentUser().getEmail() + "/";
        final JsonArrayRequest reviewsRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    int contLength = response.length();
                    for (int i = 0; i < contLength; i++) {
                        JSONObject jsObj = (JSONObject) response.get(i);
                        String code = jsObj.getString("code");
                        String review = jsObj.getString("review");
                        String likes = jsObj.getString("likes");
                        String disklikes = jsObj.getString("dislikes");
                        String player = jsObj.getString("player");
                        boolean wasVoted = !jsObj.isNull("positiveVote");
                        boolean wasVotedPositively = false;
                        if (wasVoted)
                            wasVotedPositively = Boolean.parseBoolean(jsObj.getString("positiveVote"));
                        all_reviews.add(new Review(code, player, heritageCode, review, Integer.parseInt(likes), Integer.parseInt(disklikes), wasVoted, wasVotedPositively));
                    }
                    Collections.sort(all_reviews, new Comparator<Review>() {
                        @Override
                        public int compare(Review r1, Review r2) {
                            int likes1 = r1.getLikes(), likes2 = r2.getLikes();
                            return Integer.compare(likes1, likes2) * -1;
                        }
                    });
                    reviewAdapter.notifyDataSetChanged();

                    if (all_reviews.isEmpty()) {
                        reviewsContainerLayout.addView(noReviewsText, 0);
                    } else {
                        reviewsContainerLayout.removeView(noReviewsText);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(ReviewsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueueSingleton.getInstance(this).addToRequestQueue(reviewsRequest);
    }

}