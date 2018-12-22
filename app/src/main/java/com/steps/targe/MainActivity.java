package com.steps.targe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.steps.targe.app.AppConfig;
import com.steps.targe.app.AppController;
import com.steps.targe.helper.SQLiteHandler;
import com.steps.targe.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    TextView tvIsConnected;
    Button btnPost;
    private ProgressDialog pDialog;

    public static final String MyPREFERENCES = "MyPrefs";
    String alfa, beta, gamma, contacted, mean, id, sd;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (!session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Toast.makeText(getApplicationContext(), "You are not logged in as a member", Toast.LENGTH_LONG);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        id = user.get("id_number");

        // get reference to the views
        tvIsConnected = findViewById(R.id.tvIsConnected);
        btnPost = findViewById(R.id.btnPost);

        // check if you are connected or not
        if (isConnected()) {
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are conncted");
        } else {
            tvIsConnected.setText("You are NOT conncted");
        }

        // add click listener to Button "POST"
        btnPost.setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        // String lastDate = sharedPreferences.getString("LastDate", "");
        alfa = sharedPreferences.getString("Alfa", "");
        beta = sharedPreferences.getString("Beta", "");
        gamma = sharedPreferences.getString("Gamma", "");
        contacted = sharedPreferences.getString("ContactedPersons", "");
        sd = sharedPreferences.getString("SD", "");
        mean = sharedPreferences.getString("MeanCallDuration", "");
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        TextView alfaa = findViewById(R.id.alfa);
        TextView betaa = findViewById(R.id.beta);
        TextView gama = findViewById(R.id.gama);
        TextView meanText = findViewById(R.id.mean);
        TextView sdText = findViewById(R.id.sd);
        TextView contactedText = findViewById(R.id.contacted);

        alfaa.setText("Alfa: " + alfa);
        betaa.setText("Beta: " + beta);
        gama.setText("Gama: " + gamma);
        meanText.setText("Mean Call Duration: " + mean);
        sdText.setText("Standard Devation: " + sd);
        contactedText.setText("Contated people: " + contacted);

    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnPost:
                sendData();
                //new HttpAsyncTask().execute(AppConfig.URL_UPLOAD);
                break;
        }

    }

    //-------------------------------------------------------------------------------------------
    private void sendData() {
        // Tag used to cancel the request
        String tag_string_req = "req_upload";

        pDialog.setMessage("SUploading Data ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPLOAD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Upload Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        Toast.makeText(getApplicationContext(), "Data Sent!", Toast.LENGTH_SHORT).show();

                        // Launch main activity

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            /*@Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }*/

            @Override
            public byte[] getBody() {
                JSONArray bucket = new JSONArray();
                String json=null;
                TestDatabase tdb = new TestDatabase(getBaseContext());

                ArrayList<Person> persons = new ArrayList<Person>();
                try {
                    tdb = tdb.open();
                    persons = tdb.getData(TestDatabase.KEY_TRUST, "DESC");
                    tdb.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                try {
                    for (Person person : persons) {
                        // 3. build jsonObject
                        if (person.getTrust() != 0) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.accumulate("name", person.getName());
                            jsonObject.accumulate("number", person.getNumber());
                            jsonObject.accumulate("trust", person.getTrust());
                            // 4. convert JSONObject to JSON to String
                            bucket.put(jsonObject);
                        }
                    }

                    JSONObject personsObj = new JSONObject();
                    personsObj.put("my_id", id);
                    personsObj.put("contacts", bucket);
                    personsObj.put("mean", mean);
                    personsObj.put("sd", sd);
                    personsObj.put("alfa", alfa);
                    personsObj.put("beta", beta);
                    personsObj.put("gamma", gamma);
                    json= personsObj.toString();

                }catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                    System.out.println(json);
                    String your_string_json = json; // put your json
                return json.getBytes();
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

}