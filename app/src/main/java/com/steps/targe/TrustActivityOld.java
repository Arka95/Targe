package com.steps.targe;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Arka Bhowmik on 7/21/2016.
 * TODO:set proper region-code independent
 * TODO:use transaction instead of sqls
 */
public class TrustActivityOld extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String Name = "LastDate";
    public static final String MyPREFERENCES = "MyPrefs";
    String CID = "cid", NAME = "name", TRUST = "trust";
    ArrayList<Person> persons;
    String option1 = "DESC", option2 = "DESC", option3 = TestDatabase.KEY_NAME, selection = "ASC";
    SharedPreferences sharedPreferences;
    RecyclerView lv;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView nameView, trustView;
    private ProgressDialog pDialog;


    public void inflateCustomAdapter() {
        TrustAdapter adapter = new TrustAdapter(TrustActivityOld.this, persons);
        // updating listview
        lv.setAdapter(adapter);
        lv.setLayoutManager(new LinearLayoutManager(TrustActivityOld.this));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trust_list);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
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

        try {
            TestDatabase tdb = new TestDatabase(getApplicationContext());
            tdb = tdb.open();
            persons = new ArrayList<Person>();
            persons = tdb.getData(TestDatabase.KEY_TRUST, "DESC");
            tdb.close();

            if (!persons.isEmpty()) {

                inflateCustomAdapter();
            } else
                new TrustManagerOld().execute();//database doesnt exist yet or is empty
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        new TrustManagerOld().execute();
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
        if (id == R.id.action_upload) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.personname:
                option3 = TestDatabase.KEY_NAME;
                if (option1.equals("DESC"))
                    option1 = "ASC";
                else
                    option1 = "DESC";
                selection = option1;
                break;
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


    class TrustManagerOld extends AsyncTask<String, String, String> {

        boolean flagger = true;
        Double alfa = .33, beta = .33, gama = .33;  //behavioral determinants
        int tdin = 0, tdout = 0, t = 0;// in=incoming, out=outgoing,t=total
        int tcout = 0, tcin = 0, tc, tdinmax = 1;
        Integer topatcalls = 0, topoutcalls = 0;//at=total participating calls out=outgoing
        Double sd = 0.0;
        String lastDate = sharedPreferences.getString(Name, "");//last date when the calculations were updated
        Long most_recent_calldate;
        Long last_updated_date;
        Integer mean;//mean duration of calls in secs
        HashMap<String, Person> alist = new HashMap<>();
        Calendar cat = Calendar.getInstance();
        Long limit = cat.getTimeInMillis() - 8640000000L;

        void calculateBasics() {

            System.out.println("\n\n\n basics calculating\n\n\n");
            double sqsum = 0;
            Long totstamp = 0L, avgstamp = 0L;
            for (Person it : alist.values()) {
                float avgdin = 0, avgdout = 0;
                tdin = tdin + it.getDin();
                tdout = tdout + it.getDout();
                tcout = tcout + it.getCout();
                tcin = tcin + it.getCin();
                if (it.getLastcallstamp() > 0L) {
                    Long contactstamp = it.getLastcallstamp() - last_updated_date;
                    totstamp = totstamp + contactstamp;
                }
                if (it.getCin() > 0) {
                    avgdin = it.getDin() / it.getCin();//avg in duration of that person
                    topatcalls++;
                }
                if (it.getCout() > 0) {
                    topatcalls++;
                    topoutcalls++;
                    avgdout = it.getDout() / it.getCout();//avg out duration of that person
                }
                sqsum = avgdin * avgdin + avgdout * avgdout + sqsum;
            }
            System.out.println("totstamp= " + totstamp + " topoutcalls= " + topoutcalls);
            if (topoutcalls != 0)
                avgstamp = totstamp / Long.valueOf(topoutcalls);//average timestamp based on attended calls
            System.out.println("average timestamp=" + avgstamp);
            System.out.println("sqsum:" + sqsum);

            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_CALL_LOG);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Cursor cur = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.DURATION}, "duration=(SELECT MAX(duration) from calls)", null, null);
                try {
                    cur.moveToFirst();
                    tdinmax = cur.getInt(0);
                } finally {
                    cur.close();
                }
            }
            /*gamma calculation: (avg of (timestamp value of current call- last update))/timestamp of most recent - timestamp of last update based on number of days*/
            t = tdin + tdout;
            System.out.println("total duration:" + t);
            tc = tcin + tcout;
            System.out.println("total calls:" + tc);
            alfa = (double) tcout / tc;
            mean = t / tc;
            System.out.println("dinmax=" + tdinmax + " Mean " + t / tc + " topatcalls: " + topatcalls);
            sd = Math.sqrt(sqsum / topatcalls - Math.pow(t / tc, 2));//calculating sd for call durations
            System.out.println("sd=" + sd);
            beta = (double) 1 / (1 + 3 * sd / (t / tc)); // mean/(mean+3sigmma) sigma is sd. mean of tdout instead of tdin
            System.out.println("oldest record is" + ((most_recent_calldate - last_updated_date) / 86400000L) + "days old");
            gama = (double) (avgstamp / 86400000L) / (double) ((most_recent_calldate - last_updated_date) / 86400000L);
            System.out.println(gama);
            //gamma proper weighteage determination
            // too recent updated values and too old values are not to be considered
            if (gama > 0.375 && gama < 0.75)
                gama = 0.8;
            else
                gama = 0.2;
            double tot = alfa + beta + gama;
            alfa = alfa / tot;
            beta = beta / tot;
            gama = gama / tot;
            System.out.println(alfa + " " + beta + " " + gama);
            System.out.println("\n\n\n basics have been calculated\n\n\n");
        }


        void calculateTrust() {

            Long j = 0L;
            double recency = 0, frequency = 0, sab, rba, intimacy = 0, tendency = 0, p = 0, trust = 0;

            tendency = ((double) (tdout) / ((double) t));
            System.out.println("tendency of outgoing=" + tendency);
            if (tendency < 0.25)//reception oriented
            {
                p = 1 - 2 * tendency;
                //intimacy=rba;
            } else if (0.25 <= tendency && tendency <= 0.75)//equivalent exchange
            {
                p = 0.5;
                //intimacy=(sab+rba)/2;
            } else//>.75 send centred
            {
                p = 2 * tendency - 1;
                //intimacy=sab;
            }
            for (Person it : alist.values()) {

                sab = 0;
                rba = 0;
                String number = it.getNumber();
                if (tcout != 0 && t != 0) {
                    frequency = (double) it.getCout() / tcout;//att1
                    //intimacy-------------------------------------------------------------
                    sab = (double) it.getDout() / tdout;//att2.1
                    rba = (double) it.getDin() / tdin;//att2.2
                    //System.out.println("sab= "+sab+"rba= "+rba);
                }

                intimacy = p * sab + (1 - p) * rba;//attribute 2

                //recency-------------------------------------------------------------------

                j = it.getLastcallstamp() - last_updated_date;
                if (j > 0L) {
                    recency = ((double) j) / ((double) (most_recent_calldate - last_updated_date));//attribute 3

                    //System.out.println(it.getName() + " frequency: " + frequency + " recency: " + recency + " intimacy " + intimacy);
                    trust = alfa * frequency + beta * intimacy + gama * recency;

                    it.setTrust(trust);
                } else if (j > -8640000000L) {
                    trust = alfa * frequency + beta * intimacy; //recency=0 if a record is older than last update but not older than 100 days

                    it.setTrust(trust);
                } else {
                    it.setTrust(0);
                }
                //trust---------------------------------------------------------------------

            }


        }

        public HashMap<String, Person> fetchContacts() {

            ContentResolver cr = getContentResolver(); //Activity/Application android.content.Context
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            HashMap<String, Person> personMap = new HashMap<>();
            if (cursor.moveToFirst()) {

                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

                        while (pCur.moveToNext()) {
                            String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("-", "").replaceAll(" ", "");
                            if (contactNumber.charAt(0) == '0')
                                contactNumber = contactNumber.substring(1);
                            if (!contactNumber.contains("+")) {
                                try {

                                    PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
                                    Phonenumber.PhoneNumber pn;
                                    pn = pnu.parse(contactNumber, "IN");
                                    contactNumber = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164).replaceAll("-", "").replaceAll(" ", "");

                                } catch (NumberParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            if (!(contactName == null)) {
                                Person v = new Person(contactNumber, contactName, 0.00, 0L);
                                //alContacts.add(v);
                                personMap.put(contactNumber, v);
                            }

                            break;
                        }
                        pCur.close();
                    }

                } while (cursor.moveToNext());
            }
            System.out.println("---------------------------------------------------------------");
            return personMap;
        }

        private void getCallDetails() {

            last_updated_date = limit;//the limit is 100 days ago
            String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
            System.out.println("lastDate: " + lastDate);
            if (lastDate != "" && Long.parseLong(lastDate) > last_updated_date)//checks if the lastUpdatedDate satisfies the limit
                last_updated_date = Long.parseLong(lastDate);
            //set time inmillis  which will by default consider calls not older than 100 days //to null if any probs

            String where = CallLog.Calls.DATE + " > " + last_updated_date;
            System.out.println("where clause:" + where);
            Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, where, null, sortOrder);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            if (managedCursor.getCount() == 0) {
                flagger = false;
                return;
            }
            boolean flag3 = true;//flag determining most recent call has/has not been retrieved

            while (managedCursor.moveToNext()) {
                if (flag3) {
                    //stores the most recent calldate
                    most_recent_calldate = Long.parseLong(managedCursor.getString(date));
                    System.out.println("most recent date:" + most_recent_calldate);
                    flag3 = false;
                }
                String phNum = managedCursor.getString(number).replaceAll("-", "").replaceAll(" ", "");
                if (!phNum.contains("+")) {
                    try {
                        PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
                        Phonenumber.PhoneNumber pn;
                        pn = pnu.parse(phNum, "IN");
                        phNum = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164).replaceAll("-", "").replaceAll(" ", "");
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }
                String callType = managedCursor.getString(type);
                String named = managedCursor.getString(name);
                int callDuration = Integer.parseInt(managedCursor.getString(duration));
                int dircode = Integer.parseInt(callType);
                Long dated = Long.parseLong(managedCursor.getString(date));

                if (!(phNum == null) && alist.get(phNum) == null) {
                    if (named == null)
                        named = phNum;
                    alist.put(phNum, new Person(phNum, named, 0.0, 0L));
                }
                if (alist.get(phNum).getLastcallstamp() < dated)
                    alist.get(phNum).setLastCallStamp(dated);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        alist.get(phNum).setCout(alist.get(phNum).getCout() + 1);
                        alist.get(phNum).setDout(alist.get(phNum).getDout() + callDuration);
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        alist.get(phNum).setCin(alist.get(phNum).getCin() + 1);
                        alist.get(phNum).setDin(alist.get(phNum).getDin() + callDuration);
                        break;
                }
            }
            try {
                managedCursor.moveToLast();
                managedCursor.getString(date);
                Long dated = Long.parseLong(managedCursor.getString(date));
                if (last_updated_date == 0L)
                    last_updated_date = dated;
            } catch (Exception e) {
                System.out.println(e);
            }
            managedCursor.close();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TrustActivityOld.this);
            pDialog.setMessage("Calculating...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // Building Parameters

            try {
                TestDatabase tdb = new TestDatabase(getApplicationContext());
                tdb = tdb.open();
                // if (tdb != null)//if no database created

                //only takes records not older than 100 days for calculation
                alist = tdb.getData(limit);
                //if database doesnt have contacts*/
               /* if (alist.isEmpty())
                    alist = fetchContacts();*/
                getCallDetails();
                if (flagger) {
                    calculateBasics();
                    calculateTrust();
                    for (Person it : alist.values()) {
                        {
                            tdb.createEntry(it);//create table if not exists or update existing table
                        }
                    }
                }
                tdb.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //------------------------save the date-----------------------------------
            Calendar calendar = Calendar.getInstance();
            String date = String.valueOf(calendar.getTimeInMillis());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Name, date);
            if (flagger) {
                editor.putString("SD", sd.toString());
                editor.putString("ContactedPersons", topatcalls.toString());
                editor.putString("MeanCallDuration", mean.toString());
                editor.putString("Alfa", alfa.toString());
                editor.putString("Beta", beta.toString());
                editor.putString("Gamma", gama.toString());
            }
            editor.commit();
            //------------------------saves the date---------------------------------
           /* persons = new ArrayList<>();
            for (Person it : alist.values()) {
                persons.add(it);
            }*/
            //viewPersons(persons);
            return null;
        }


        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
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

    public void viewPersons(ArrayList<Person> persons) {
        for (Person it : persons) {
            System.out.println(it.getNumber() + " " + it.getName() + "dout: " + it.getDout() + "din: " + it.getDin() + " \n");

        }
    }
}

