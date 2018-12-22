package com.steps.targe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SplashModeChooseActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /** Default creation code. */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_select_mode);
        Button oldmode= findViewById(R.id.button_mode_old);
        Button newmode= findViewById(R.id.button_mode_new);
        oldmode.setOnClickListener(this);
        newmode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i=null;
        switch(v.getId())
        {
            case R.id.button_mode_old:
                i=new Intent(getApplicationContext(),TrustActivityOld.class);
                break;
            case R.id.button_mode_new:
               i=new Intent(getApplicationContext(),TrustActivity.class);
                break;
        }
        startActivity(i);
    }
}
