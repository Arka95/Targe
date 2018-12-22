package com.steps.targe;

import android.content.Context;

import com.steps.targe.app.AppConfig;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
    //the file in which the data is saved
    static String filename = AppConfig.csv4class;
    Context context;

    public CSVUtils(Context c) {
        this.context = c;
    }

    //from text file in csv format
    public List<TrustElement> from_csv(String text) {
        String[] data = text.split("\n");
        List<TrustElement> persons = new ArrayList<TrustElement>();
        for (String lines : data) {
            String[] vars = lines.split(",");
            String num = vars[0], nam = vars[1];
            float frequency = Float.parseFloat(vars[2]), intimacy = Float.parseFloat(vars[3]), recency = Float.parseFloat(vars[4]),
                    trust = Float.parseFloat(vars[5]);
            persons.add(new TrustElement(num, nam, frequency, intimacy, recency, trust));
        }
        return persons;
    }

    //from the internal data csv file
    public List<TrustElement> from_csv() {
        FileInputStream fstream = null;
        try {
            String path=context.getFilesDir() +"/"+ filename;
            System.out.println(path);
            fstream = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String line;
            List<TrustElement> persons = new ArrayList<TrustElement>();
            System.out.println("------------------------------------------------------------------------------");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                String[] vars = line.split(",");
                String num = vars[0], nam = vars[1];
                float frequency = Float.parseFloat(vars[2]), intimacy = Float.parseFloat(vars[3]),
                        recency = Float.parseFloat(vars[4]), trust = Float.parseFloat(vars[5]);
                persons.add(new TrustElement(num, nam, frequency, intimacy, recency, trust));
            }
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("LOADED FROM CSV");
            return persons;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String to_csv(List<TrustElement> persons) {
        String text = "";
        for (TrustElement person : persons) {
            text += person.getString();
        }
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(text.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("SAVED TO CSV");
        return text;
    }
}
