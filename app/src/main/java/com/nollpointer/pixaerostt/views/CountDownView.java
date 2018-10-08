package com.nollpointer.pixaerostt.views;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.widget.FrameLayout;


public class CountDownView extends AppCompatTextView {

    private Handler handler;
    private Runnable countDownRunnable;


    public CountDownView(Context context) {
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
    }

    public void startCountDown(int count){
        setText(Integer.toString(count));
        //Handler handler = new Handler();

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                String text = getText().toString();
//                int number = Integer.parseInt(text);
//                number--;
//                setText(Integer.toString(number));
//                postDelayed(this,1000);
//            }
//        },1000);

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

    public void stopCountDown(){
        //handler.re
    }

}


