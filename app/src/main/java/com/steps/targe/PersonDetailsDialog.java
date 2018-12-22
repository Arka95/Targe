package com.steps.targe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.steps.targe.app.AppConfig;
import com.steps.targe.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arka Bhowmik on 8/15/2017.
 */

//FOR VIEWING THE PRIZES IN THE QUIZ SECTION
public class PersonDetailsDialog extends DialogFragment {
    Button globalTrust;
    TextView numD;
    private static final String TAG = DialogFragment.class.getSimpleName();
    static PersonDetailsDialog newInstance( Person per) {
        PersonDetailsDialog f = new PersonDetailsDialog();
        //f.act=activity;
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("person",new Gson().toJson(per));
        f.setArguments(args);

        return f;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content=inflater.inflate(R.layout.detail_view, null);

      String  jsonMyObject = getArguments().getString("person");
        final Person current = new Gson().fromJson(jsonMyObject, Person.class);
        builder.setView(content)
                // Add action buttons
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PersonDetailsDialog.this.getDialog().cancel();
                    }
                });
        numD = content.findViewById(R.id.tvnumber);
        TextView nameD = content.findViewById(R.id.tvname);
        TextView trustD = content.findViewById(R.id.tvtrust);
        TextView cinD = content.findViewById(R.id.tvcin);
        TextView coutD = content.findViewById(R.id.tvcout);
        TextView dinD = content.findViewById(R.id.tvdin);
        TextView doutD = content.findViewById(R.id.tvdout);
        globalTrust= content.findViewById(R.id.GT);
        numD.setText(current.getNumber());
        DecimalFormat df = new DecimalFormat("0.00#");
        String name=current.getName();
        if (name.length() > 23) {
            name = name.substring(0, 22) + "..";
        }
        nameD.setText(name);
        trustD.setText( df.format(current.getTrust()));
        cinD.setText(current.getCin().toString());
        coutD.setText(current.getCout().toString());
        dinD.setText(current.getDin().toString());
        doutD.setText(current.getDout().toString());
        globalTrust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected())
                    checkTrust(current.getNumber());
                else
                    Toast.makeText(getActivity(),"You are not connected to the Internet !",Toast.LENGTH_SHORT).show();

            }
        });
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        return builder.create();
    }
    private void checkTrust(final String number) {
        // Tag used to cancel the request
        String tag_string_req = "req_trustfetch";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SEARCH, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Trust Fetch Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        String trust = jObj.getString("global_trust");
                        globalTrust.setText(trust);
                        globalTrust.setClickable(false);
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        globalTrust.setText(errorMsg);
                        globalTrust.setClickable(false);
                        Toast.makeText(getActivity(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("number",number);
                params.put("type",new String("1"));
                return params;
            }

        };

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}