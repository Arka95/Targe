package com.steps.targe;

/**
 * Created by Arka Bhowmik on 10/24/2016.
 */

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.steps.targe.app.AppConfig;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TrustAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    ArrayList<Person> persons= new ArrayList<>();
    Person current;

    // create constructor to initialize context and persons sent from MainActivity
    public TrustAdapter(Context context, ArrayList<Person> p){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.persons=p;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.trust_element, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind persons
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind persons and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        myHolder.setPosition(position);
        Person current=persons.get(position);
        float trustt = Float.parseFloat(current.getTrust().toString());
        // subject taking only first 23 chars
        // to fit into screen
        String name=current.getName();
        if (name.length() > 23) {
            name = name.substring(0, 22) + "..";
        }
        myHolder.textName.setText(name);
        myHolder.textNumber.setText(current.getNumber());
        String label=AppConfig.map_value2class[(int)trustt];
        String color=AppConfig.rating_bar_colors[AppConfig.map_values_to_indices[(int)trustt]];
        myHolder.textTrust.setText(label);
        myHolder.textTrust.setBackgroundColor(Color.parseColor(color));
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return persons.size();
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textName;
        TextView textNumber;
        TextView textTrust;
        ImageButton call;
        int position;
        public void setPosition(int i)
        {
            this.position=i;
        }
        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textName= itemView.findViewById(R.id.name);
            textNumber = itemView.findViewById(R.id.number);
            textTrust = itemView.findViewById(R.id.trust);
            call = itemView.findViewById(R.id.call);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Your code that you want to execute on this button click
                    Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + textNumber.getText().toString()));
                         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)   ;
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        Toast.makeText(context, "Sorry, Your phone is as good as an Iphone is to a developer", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    context.startActivity(i);

                }

            });
            itemView.setOnClickListener(this);
        }
        public void showDetails(Person par) {
            AppCompatActivity activity = (AppCompatActivity)(context);
            DialogFragment newFragment = new PersonDetailsDialog().newInstance(par);
            newFragment.show(activity.getFragmentManager(),"DETAILS");  //.show(getSupportFragmentManager(), "missiles");
        }
        // Click event for all items
        @Override
        public void onClick(View v) {
           showDetails(persons.get(position));
        }

    }
}