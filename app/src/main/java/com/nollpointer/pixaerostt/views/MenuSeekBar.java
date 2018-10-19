package com.nollpointer.pixaerostt.views;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.widget.FrameLayout;

import com.nollpointer.pixaerostt.R;

public class MenuSeekBar extends AppCompatSeekBar {

    public MenuSeekBar(Context context) {
        super(context);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        //setSplitTrack(false);

        setThumb(context.getResources().getDrawable(R.drawable.seekbar_thumb));

        setMax(40);
        //setProgress(7);
        setBackground(context.getResources().getDrawable(R.drawable.seekbar_background));
        setVisibility(GONE);
        //setRotation(270);

    }

    public void show(View target){
        setY(target.getHeight()/2);
        setVisibility(VISIBLE);
    }

}
