package com.steps.targe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.steps.targe.app.AppConfig;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
//TODO:update persons from the database and use database records to calculate recency too
//TODO:update records of database that are recent only. dont tamper old ones.
//TODO:if old ones are tampered, make sure that the values for the same have been updated from scratch
//TODO: try our sab and rba using todatal in duration and out duration separately instead of dividing by total duration
//------------------------------------NOTE---------------------------------------------------------
/*0 or 1 duration calls HAVE BEEN considered too.
everytime a new list is created and we are not feeding previous data from database because of 'days'
limit.
*/
//-------------------------------------------------------------------------------------------------
class TrustManager extends AsyncTask<String, String, String>{
    AsyncResponse delegate =null;

    boolean flagger = true;
    int tdin = 0, tdout = 0, tot_dur = 0;// in=incoming, out=outgoing,t=total
    int tcout = 0, tcin = 0, tc, tdinmax = 1;
    Integer tot_participants = 0, topoutcalls = 0;//at=total participating calls out=outgoing
    Long most_recent_calldate,last_call_date,limit;
    int tot_recent_attended_calls=0;
    Integer mean_dur=new Integer(0);Float sd_dur=new Float(0);//mean duration of calls in secs
    Float mean_freq,sd_freq;//mean frequency
    SharedPreferences sharedPreferences;
    String csv_data="";
    String rec_row="";
    private Context context;
    int sq_dur=0;
    private ProgressDialog pDialog;

    public TrustManager(Context c, SharedPreferences sp) {
        this.sharedPreferences= sp;
        this.context = c;
    }

    HashMap<String, Person> alist = new HashMap<>();
    Calendar cat = Calendar.getInstance();

    private void getCallDetails(int days) {
        limit = cat.getTimeInMillis() - days * 86400000L;
        last_call_date=cat.getTimeInMillis();//initialization
        String sortOrder = CallLog.Calls.DATE + " DESC";

        //set time inmillis  which will by default consider calls not older than 100 days //to null if any probs
        String where = CallLog.Calls.DATE + " >= " + limit;
        System.out.println("where clause:" + where);
        Cursor managedCursor =context.getContentResolver().query( CallLog.Calls.CONTENT_URI, null, where, null, sortOrder);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        if (managedCursor.getCount()==0) {
            flagger = false;
            return;
        }
        boolean flag3 = true;

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
            if(callDuration>1){
                sq_dur+=callDuration*callDuration;
                tot_recent_attended_calls++;
            }
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
            last_call_date = dated;
        } catch (Exception e) {
            System.out.println(e);

        }
        managedCursor.close();
        sd_dur=(float)(Math.sqrt(sq_dur/tot_recent_attended_calls-mean_dur*mean_dur));
        System.out.println("lastDate: " + last_call_date);
    }


    void calculateBasics() {

        System.out.println("\n\n\n basics calculating\n\n\n");
        for (Person it : alist.values()) {
            float avgdin = 0, avgdout = 0;
            tdin = tdin + it.getDin();
            tdout = tdout + it.getDout();
            tcout = tcout + it.getCout();
            tcin = tcin + it.getCin();
            //it.display();
            if (it.getCin() > 0 || it.getCout() > 0)
                tot_participants++;
        }
        tot_dur=tdin+tdout;
        mean_dur=(tdin+tdout)/tot_recent_attended_calls;
    }

    void calculateTrust() {
        Long j = 0L;int n=alist.size();
        Float mean_f=0f,mean_i=0f,mean_r=0f,f_sq=0f,i_sq=0f,r_sq=0f,sd_f=0f,sd_r=0f,sd_i=0f;
        String record="",textdata="";
        for (Person it : alist.values()) {
            float recency = 0, frequency = 0, sab, rba, intimacy = 0, tendency = 0, p = 0, trust = 0;
            sab = 0;
            rba = 0;
            String number = it.getNumber();
            //intimacy-------------------------------------------------------------
            if (tcout != 0 && tot_dur != 0) {
                frequency = (float) it.getCout() / tcout;//att1
                sab = (float) it.getDout() / tot_dur;//att2.1 or might wanna change to tdout
                rba = (float) it.getDin() / tot_dur;//att2.2 or might wanna change to tdin
                //System.out.println("sab= "+sab+"rba= "+rba);
            }
            intimacy = tendency * sab + (1 - tendency) * rba;//attribute 2

            //recency-------------------------------------------------------------------
            long last_contacted_user_on = it.getLastcallstamp();
            j = last_contacted_user_on - last_call_date;
            //recency=(timestamp(most_recent_user_person)-timestamp(least_recent_user))/total_period_of_consideration
            recency = ((float) j) / ((float) (most_recent_calldate - last_call_date));//attribute 3

            mean_f+=frequency; f_sq+=frequency*frequency;//calculate mean and sd of frequency
            mean_i+=intimacy; i_sq+=intimacy*intimacy;//calculate mean and sd of intimacy
            mean_r+=recency; r_sq+=recency*recency;//calculate mean and sd of recency

            //format for storing in csv:
            //NUMBER,FREQUENCY,INTIMACY,RECENCY
            record =it.getNumber()+","+frequency+","+intimacy+","+recency+"\n";
            //System.out.println(record);
            textdata += record;
        }
        mean_f/=n;mean_r/=n;mean_i/=n;
        sd_f=(float)Math.sqrt(f_sq/n-mean_f*mean_f);
        sd_i=(float)Math.sqrt(i_sq/n-mean_i*mean_i);
        sd_r=(float)Math.sqrt(r_sq/n-mean_r*mean_r);
        System.out.println("----------------------STATS FOR MEAN ND SD---------------------------");
        System.out.println("STATS\tFREQUENCT\tINTIMACY\tRECENCY");
        System.out.println("Means:\t"+mean_f+"\t"+mean_i+"\t"+mean_r+"\nSDs:\t"+sd_f+"\t"+sd_i+"\t"+sd_r);
        System.out.println("\n-----------------------TRUST.CSV---------------------------------\n");

        //trust---------------------------------------------------------------------

        TensorFlowInferenceInterface tensorflow = new TensorFlowInferenceInterface(context.getAssets(),"file:///android_asset/trust_4class_mlp.pb");
        //System.out.println(tensorflow.toString());
        /* Continuous inference (floats used in example, can be any primitive):
        running inference for given input and reading output
        the name of the input node can is available from your .pb file and may change from the model you imported
        same goes for the output*/
        long []INPUT_SHAPE={1,3};
        String outputNode = "output_node0:0";
        String inputNode="dense_46_input_1:0";
        String[] outputNodes = {outputNode};
        boolean enableStats = false;

        String []data=textdata.split("\n");
        for(String lines:data){
            String []vars=lines.split(",");
            String num=vars[0];
            float frequency=Float.parseFloat(vars[1]),intimacy=Float.parseFloat(vars[2]),recency=Float.parseFloat(vars[3]);
            //converting to normalized values
            frequency= (frequency-mean_f)/sd_f;
            intimacy= (intimacy-mean_f)/sd_i;
            recency= (recency-mean_f)/sd_r;

            Person it=alist.get(num);
            float []input={frequency,intimacy,recency};//op=3
            float []output={0,0,1,0};
            // loading new input
            tensorflow.feed(inputNode, input, INPUT_SHAPE); // INPUT_SHAPE is an long[] of expected shape, input is a float[] with the input data
            tensorflow.run(outputNodes, enableStats);
            tensorflow.fetch(outputNode, output); // output is a preallocated float[] in the size of the expected output vector
            int maxi=0;//selecting the highest output index as our class output
            for(int i=0;i<4;i++){if (output[i]>output[maxi])maxi=i;}
            Float trust=(float)maxi;
            it.setTrust(trust);
            String name=it.getName();
            rec_row=num+","+name+","+frequency+","+intimacy+","+recency+","+trust+"\n";
            csv_data+=rec_row;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Calculating...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        //pDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        // Building Parameters
        try {
            TestDatabase tdb = new TestDatabase(context);
            tdb = tdb.open();
            //only takes records not older than d days for calculation
            //88888888888888888888888888888CHANGELOG88888888888888888888888888888888888888888888888
           // alist = tdb.getData(limit);
            alist=new HashMap<String,Person>();
            //8888888888888888888888888888888888888888888888888888888888888888888888888888888888888
            //if database doesnt have contacts*/
            int days = 100;
            getCallDetails(days);
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
        editor.putString("LastDate", date);
        if (flagger) {
            editor.putString("SD", sd_dur.toString());
            editor.putString("ContactedPersons", tot_participants.toString());
            editor.putString("MeanCallDuration", mean_dur.toString());
            editor.putInt("totalCalls",tcin+tcout);
        }
        editor.commit();
        return null;
    }

    protected void onPostExecute(String file_url) {
        // save the data in a csv file
        //System.out.println(csv_data);
        String filename = AppConfig.csv4class;
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(csv_data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pDialog.dismiss();
        delegate.processFinish(csv_data);
        // updating UI from Background Thread
    }
}
