package com.steps.targe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.steps.targe.app.AppConfig;
import com.steps.targe.app.AppController;
import com.steps.targe.helper.SQLiteHandler;
import com.steps.targe.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arka Bhowmik on 1/2/2017.
 */
public class RegisterActivity extends Activity implements View.OnClickListener {
    ProgressDialog pDialog;
    SessionManager session;
    SQLiteHandler db;
    String TAG = RegisterActivity.class.getSimpleName();
    EditText etEmail, etPassword, etCpassword, etCity, etState, etCountry, etName,  etUid;
    Button submit;

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etEmail = findViewById(R.id.etEmail);
        etUid = findViewById(R.id.etUid);
        etPassword = findViewById(R.id.etPassword);
        etCpassword = findViewById(R.id.etCpassword);

        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);
        etState = findViewById(R.id.etState);
        etName = findViewById(R.id.etName);
        TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String phNum = tMgr.getLine1Number();
        PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber pn;
        try {
            System.out.println("phone:"+phNum);
            pn = pnu.parse(phNum, "IN");
            phNum = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164).replaceAll("-", "").replaceAll(" ", "");
            etUid.setText(phNum);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        submit = findViewById(R.id.submit);
        submit.setOnClickListener(this);

        // Session manager
        session = new SessionManager(getApplicationContext());
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    TrustActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        String email, cpassword, password, name,  uid, city, country, state;

        email = etEmail.getText().toString().trim();
        uid = etUid.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        cpassword = etCpassword.getText().toString().trim();
        country = etCountry.getText().toString().trim();
        city = etCity.getText().toString().trim();
        state = etState.getText().toString().trim();

        name = etName.getText().toString().trim();
        // Progress dialog


        if (v.getId() == R.id.submit) {
            if (email.isEmpty()) {
                Toast.makeText(this.getApplicationContext(), "Email Cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this.getApplicationContext(), "Password Cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (uid.isEmpty()) {
                Toast.makeText(this.getApplicationContext(), "User_ID Cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!cpassword.equals(password) && password.isEmpty()) {
                Toast.makeText(this.getApplicationContext(), "User_ID Cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isConnected()) {
                Toast.makeText(getApplicationContext(), "Not connected to the internet!", Toast.LENGTH_SHORT).show();
            } else {

                registerUser(uid, email, password, country, state, city,name);
            }

        }

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String uid, final String email,
                              final String password, final String country, final String state, final String city, final String name) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite

                        JSONObject user = jObj.getJSONObject("user");
                        String email = user.getString("email");
                        String uid = user.getString("id_number");
                        String password = user.getString("password");
                        /*Inserting row in users table
                        db.addUser(uid, email, password);*/

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_number", uid);
                params.put("email", email);
                params.put("password", password);
                params.put("country", country);
                params.put("state", state);
                params.put("city", city);
                params.put("name", name);


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


}
