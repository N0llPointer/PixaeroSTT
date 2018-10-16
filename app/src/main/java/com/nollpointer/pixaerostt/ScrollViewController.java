package com.nollpointer.pixaerostt;

import android.os.Handler;
import android.text.Layout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScrollViewController {
    private ScrollView scrollView;
    private TextView contentText;
    private String text;

    private MainActivity activity;

    //Mostly constants till the end of session
    private int lineCount;

    private int totalHeight;
    private int screenHeight;

    private Runnable scrollRunnable;
    private Handler handler;

    private boolean isPaused = false;

    private int scrollDelta = 2;
    private int delayTime = 100;


    public ScrollViewController(ScrollView scrollView,TextView textView,MainActivity activity) {
        this.scrollView = scrollView;
        this.contentText = textView;
        this.activity = activity;

        text = textView.getText().toString();
        initializeController();
        prepareText();
        initializeController();
    }

    private void initializeController(){

        Layout textViewLayout = contentText.getLayout();
        lineCount = textViewLayout.getLineCount();
        totalHeight = textViewLayout.getHeight();
        screenHeight = scrollView.getHeight();

        //handler = new Handler();

    }

    private void prepareText(){
        int count = screenHeight/(totalHeight/lineCount);
        for(int i=0;i<count;i++){
            text = "\n" + text;
        }
        for(int i=0;i<count;i++){
            text = text + "\n";
        }

        contentText.setText(text);
    }


    private void threadPause(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentShowingSubString(){
        Layout textViewLayout = contentText.getLayout();

        int currentY = contentText.getScrollY();
        int currentEndY = currentY + screenHeight;

        int startLine = textViewLayout.getLineForVertical(currentY);
        int endLine = textViewLayout.getLineForVertical(currentEndY);



        int start = textViewLayout.getLineStart(startLine);
        int end = textViewLayout.getLineEnd(endLine);

        return text.substring(start,end);
    }

    /*private String getCurrentShowingReadngSubString(){
        Layout textViewLayout = contentText.getLayout();

        int currentY = contentText.getScrollY();
        int currentEndY = currentY + scrollViewHeight/2;

        int startLine = textViewLayout.getLineForVertical(currentY);
        int endLine = textViewLayout.getLineForVertical(currentEndY);

        int start = textViewLayout.getLineStart(startLine);
        int end = textViewLayout.getLineEnd(endLine);

        return text.substring(start,end);
    }*/

    public void swipeUp(int swipe){
        contentText.scrollBy(0,swipe);
    }

    public void startScroll(){
        isPaused = false;
        //handler.post(scrollRunnable);
        new Thread(){
            @Override
            public void run() {

                while(true){

                    if(isPaused) {
                        threadPause(100);
                        continue;
                    }
                    scrollView.smoothScrollTo(0,scrollView.getScrollY() + scrollDelta);

                    if(scrollView.getScrollY() + screenHeight >= totalHeight)
                        scrollView.scrollTo(0,0);

                    threadPause(delayTime);

                }
            }
        }.start();
    }

    public void stopScroll(){
        isPaused = true;
    }

    public void setScrollDelta(int delta){
        scrollDelta = delta;
    }

    public void increaseScrollDelta(int increase){
        //scrollDelta += increase;
        delayTime -= increase;
        //changeSpeed();
    }

    public void changeSpeed(){

    }

}
