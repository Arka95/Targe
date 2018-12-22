package com.steps.targe;

/**
 * Created by Arka Bhowmik on 10/24/2016.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.steps.targe.app.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class EditTrustAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<TrustElement> persons= new ArrayList<TrustElement>();
    //IN BAR:  0->SPAM 1->LOW  2->MEDIUM 3->HIGH
    //ACTUALLY:0->HIGH 1->SPAM 2->MEDIUM 3->LOW TRUST

    // return total item from List
    public int getItemCount() {
        return persons.size();
    }

    // create constructor to initialize context and persons sent from MainActivity
    public EditTrustAdapter(Context context, List<TrustElement>  p){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.persons=p;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.trust_edit_element, parent, false);
        EditTrustAdapter.RatingHolder holder = new EditTrustAdapter.RatingHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind persons and assign values from list
        RatingHolder ratingHolder= (RatingHolder) holder;
        ratingHolder.setPosition(position);
        TrustElement current=persons.get(position);
        float trustt = current.trust;
        // subject taking only first 23 chars to fit into screen
        String name=current.name;
        if (name.length() > 23) {
            name = name.substring(0, 22) + "..";
        }
        ratingHolder.textName.setText(name);
        ratingHolder.textNumber.setText(current.number);
        ratingHolder.rbTrust.setRating(trustt+1);
        Drawable drawable = ratingHolder.rbTrust.getProgressDrawable();
        drawable.setColorFilter(Color.parseColor(AppConfig.rating_bar_colors[(int)trustt]), PorterDuff.Mode.SRC_ATOP);
    }

    class RatingHolder extends RecyclerView.ViewHolder implements RatingBar.OnRatingBarChangeListener{

        TextView textName;
        TextView textNumber;
        RatingBar rbTrust;
        int position;
        public void setPosition(int i)
        {
            this.position=i;
        }
        // create constructor to get widget reference
        public RatingHolder(View itemView) {
            super(itemView);
            textName= itemView.findViewById(R.id.name2);
            textNumber = itemView.findViewById(R.id.number2);
            rbTrust = itemView.findViewById(R.id.ratingBar);
        }
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            ratingBar.setRating(rating);
            //makes corresponding changes in our dataset to be uploaded
            Toast.makeText(context,"Changed Trust of "+persons.get(position).name+"from "+persons.get(position).trust+" to "+(rating-1),Toast.LENGTH_SHORT);
            persons.get(position).trust=AppConfig.rating_bar_values[(int)rating-1];
            System.out.println(persons.get(position).getString());
            //change color
            Drawable drawable = ratingBar.getProgressDrawable();
            drawable.setColorFilter(Color.parseColor(AppConfig.rating_bar_colors[(int)rating-1]), PorterDuff.Mode.SRC_ATOP);
        }
    }
}