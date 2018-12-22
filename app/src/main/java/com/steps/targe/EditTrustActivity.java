package com.steps.targe;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


//0->HIGH TRUST 1->SPAM 2->MEDIUM 3->LOW TRUST

public class EditTrustActivity extends Activity implements View.OnClickListener{
    RecyclerView lv;

    public void inflateEditTrustAdapter(List<TrustElement> persons) {
        EditTrustAdapter adapter = new EditTrustAdapter(EditTrustActivity.this, persons);
        // updating listview
        lv.setAdapter(adapter);
        lv.setLayoutManager(new LinearLayoutManager(EditTrustActivity.this));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trust_list);
        lv = (RecyclerView) findViewById(R.id.listTrustEdit);
        Button upload= (Button)findViewById(R.id.button_mlp_trust_upload);
        CSVUtils csvutils=new CSVUtils(getApplicationContext());
        List<TrustElement> persons=csvutils.from_csv();
        if(!persons.isEmpty())
        {
            upload.setOnClickListener(this);
            inflateEditTrustAdapter(persons);
        }

    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getApplicationContext(),"Uploading data to cloud",Toast.LENGTH_SHORT);
    }

}

