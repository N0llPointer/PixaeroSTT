package com.nollpointer.pixaerostt.views;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.widget.FrameLayout;


public class CountDownView extends AppCompatTextView {

    public CountDownView(Context context) {
        super(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);

        setLayoutParams(params);

        setPadding(15,15,15,15);
        setBackgroundColor(0xc3757575);
        setVisibility(GONE);
        setTextSize(40);
        setTextColor(Color.WHITE);

        setGravity(Gravity.CENTER);
    }

    public void startCountDown(int count){
        setText(Integer.toString(count));

        new CountDownTimer(count*1000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                double number = millisUntilFinished/1000.;
                int numero = ((int) Math.ceil(number));
                setText(Integer.toString(numero));
            }

            @Override
            public void onFinish() {
                setVisibility(GONE);
            }
        }.start();

    }

}


