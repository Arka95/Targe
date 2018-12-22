package com.steps.targe;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.steps.targe.app.AppConfig;
import com.steps.targe.app.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arka Bhowmik on 6/30/2016.
 */


public class SearchResultsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public final String TAG=SearchResultsActivity.class.getSimpleName();
    String CID = "cid", NAME = "name", TRUST = "trust";
    public static final String DATABASE_TABLE = "trust_data";
    public static final String COL_WORD = "name";
    ArrayList<Person> contacts;
    RecyclerView rcContactsView;
    SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchView=null;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        pDialog=new ProgressDialog(getApplicationContext());
        rcContactsView = findViewById(R.id.searchList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Get Search item from action bar and Get Search service
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchResultsActivity.this.getComponentName()));
            searchView.setIconified(false);
        }

        //searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (searchView != null) {
                searchView.clearFocus();
            }

            searchContacts(query);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i=new Intent(getApplicationContext(),SplashModeChooseActivity.class);
        startActivity(i);
    }

    private void searchContacts(final String search) {
        // Tag used to cancel the request
        String tag_string_req = "search_Contacts";

        // pDialog.setMessage("Searching..");
        //showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SEARCH, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Search Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        JSONArray friendsArray = jObj.getJSONArray("contacts");
                        contacts=new ArrayList<>();
                        for (int i = 0; i < friendsArray.length(); i++) {
                            JSONObject jsonObject = friendsArray.getJSONObject(i);// 0 selects the latest updated data record
                            String id = jsonObject.getString("id_number");
                            Double trust;
                            String name;
                            if(jsonObject.getString("name")==null)
                                name="Unknown";
                            else
                               name = jsonObject.getString("name");

                            if(jsonObject.get("global_trust")==null)
                                trust=0.0;
                            else
                                 trust = jsonObject.getDouble("global_trust");
                            Person person = new Person(id, name, trust,0L);
                            contacts.add(person);
                        }

                        // hideDialog();
                        inflateContactsAdapter();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        hideDialog();
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    //        hideDialog();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Retrieval Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //      hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("number", search);
                params.put("type", "2");//showContacts
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void inflateContactsAdapter() {
        TrustAdapter adapter = new TrustAdapter(getApplicationContext(), contacts);
        // updating listview
        rcContactsView.setAdapter(adapter);
        rcContactsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }
    private void doSearch(String query) {
        try {
            TestDatabase tdb = new TestDatabase(getApplicationContext());
            tdb = tdb.open();
            contacts = new ArrayList<>();
            contacts = tdb.getData(query);
        } catch (Exception e) {
            e.printStackTrace();
            TrustAdapter adapter = new TrustAdapter(getApplicationContext(), contacts);
            // updating listview
            rcContactsView.setAdapter(adapter);
            rcContactsView.setLayoutManager(new LinearLayoutManager(SearchResultsActivity.this));

        }
    }

    @Override
    public void onRefresh() {

        if (searchView != null) {
            String query=searchView.getQuery().toString();
            searchView.clearFocus();
            searchContacts(query);
        }

    }
}

   



