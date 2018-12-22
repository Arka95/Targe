package com.steps.targe;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
//0->HIGH TRUST 1->SPAM 2->MEDIUM 3->LOW TRUST
/**
 * Created by Arka Bhowmik on 7/21/2016.
 * TODO:set proper region-code independent
 * TODO: filter database to select only records that are updated
 * TODO:use transaction instead of sqls
 * TODO: update the things into database
 */
public class TrustActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,AsyncResponse {

    public static final String Name = "LastDate";
    public static final String MyPREFERENCES = "MyPrefs";
    String CID = "cid", NAME = "name", TRUST = "trust";
    String lastdate="0";
    ArrayList<Person> persons;
    //options for sorting the results appearing on the screen
    String option1 = "DESC", option2 = "DESC", option3 = TestDatabase.KEY_NAME, selection = "ASC";
    SharedPreferences sharedPreferences;
    RecyclerView lv;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView nameView, trustView;
    TrustManager tm;

    public void inflateCustomAdapter() {
        TrustAdapter adapter = new TrustAdapter(TrustActivity.this, persons);
        // updating listview
        lv.setAdapter(adapter);
        lv.setLayoutManager(new LinearLayoutManager(TrustActivity.this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trust_list);
        lv = findViewById(R.id.my_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        nameView = findViewById(R.id.personname);
        trustView = findViewById(R.id.trustvalue);
        nameView.setOnClickListener(this);
        trustView.setOnClickListener(this);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lastdate=sharedPreferences.getString(Name, "");
        //TrustManager need the last date of update to access last call details
        tm=new TrustManager(getApplicationContext(),sharedPreferences);
        //this sets up the async task'd onpostexecute to work on the main thread using our custom interface AsyncResponse
        tm.delegate=this;

        try {
            TestDatabase tdb = new TestDatabase(getApplicationContext());
            tdb = tdb.open();
            persons = new ArrayList<Person>();
            persons = tdb.getData(TestDatabase.KEY_TRUST, "DESC");
            tdb.close();
            //initially display the previous results from the local database, if any
            if (!persons.isEmpty()) {
                inflateCustomAdapter();
            } else {
                // no previous records, so calculate new record
                tm.execute();//database doesnt exist yet or is empty
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        //TrustManager tm=new TrustManager(getApplicationContext(),sharedPreferences);
        tm.execute();//recalculate
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        //Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        ComponentName cn = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            Intent i = new Intent(getApplicationContext(), EditTrustActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //sorts by person name
            case R.id.personname:
                option3 = TestDatabase.KEY_NAME;
                if (option1.equals("DESC"))
                    option1 = "ASC";
                else
                    option1 = "DESC";
                selection = option1;
                break;
            //sorts by the trust value
            case R.id.trustvalue:
                option3 = TestDatabase.KEY_TRUST;
                if (option2.equals("DESC"))
                    option2 = "ASC";
                else
                    option2 = "DESC";
                selection = option2;
                break;
        }
        TestDatabase tdb = new TestDatabase(getApplicationContext());

        try {
            tdb = tdb.open();
            persons = tdb.getData(option3, selection);
            tdb.close();
            if (!persons.isEmpty()) {
                inflateCustomAdapter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void processFinish(String output){
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        runOnUiThread(new Runnable() {
            public void run() {
                /*Updating parsed  data into ListView*/
                /* inflateCustomAdapter();*/
                try {
                    TestDatabase tdb = new TestDatabase(getApplicationContext());
                    tdb = tdb.open();
                    persons = new ArrayList<Person>();
                    persons = tdb.getData(TestDatabase.KEY_TRUST, "DESC");
                    tdb.close();

                    if (!persons.isEmpty()) {
                        inflateCustomAdapter();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

