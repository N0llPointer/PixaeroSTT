package com.nollpointer.pixaerostt.views;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class PartialResultsView extends AppCompatTextView {

    public PartialResultsView(Context context) {
        super(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        //params.set

        setLayoutParams(params);

        setPadding(15,15,15,15);
        setBackgroundColor(0xc3757575);
        setVisibility(GONE);
        setTextSize(40);
        setTextColor(Color.WHITE);
        setGravity(Gravity.CENTER);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(GONE);
            }
        });
    }

    public void setText(String text){
        super.setText(text);
    }

    public void show(){
        setVisibility(VISIBLE);
    }

}
