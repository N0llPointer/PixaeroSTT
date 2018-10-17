package com.nollpointer.pixaerostt;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.nollpointer.pixaerostt.MainActivity.TAG;

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
        //prepareText();
        //initializeController();
    }

    private void initializeController(){

        Layout textViewLayout = contentText.getLayout();
        lineCount = textViewLayout.getLineCount();
        totalHeight = textViewLayout.getHeight();
        screenHeight = scrollView.getHeight();

        //handler = new Handler();

    }

    private void prepareText(){
        int count = screenHeight/(totalHeight/lineCount) + 1;
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

//    public String getCurrentShowingSubString(){
//        Layout textViewLayout = contentText.getLayout();
//
//        int currentY = contentText.getScrollY();
//        int currentEndY = currentY + screenHeight;
//
//        int startLine = textViewLayout.getLineForVertical(currentY);
//        int endLine = textViewLayout.getLineForVertical(currentEndY);
//
//        int start = textViewLayout.getLineStart(startLine);
//        int end = textViewLayout.getLineEnd(endLine);
//
//        return text.substring(start,end);
//    }

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

//    public void swipeUp(int swipe){
//        contentText.scrollBy(0,swipe);
//    }

//    public void startScroll(){
//        isPaused = false;
//        //handler.post(scrollRunnable);
//        new Thread(){
//            @Override
//            public void run() {
//
//                while(true){
//
//                    if(isPaused) {
//                        threadPause(100);
//                        continue;
//                    }
//                    //ObjectAnimator.ofInt(scrollView, "scrollY",  scrollView.getScrollY() + scrollDelta).setDuration(delayTime).start();
//                    scrollView.smoothScrollTo(0,scrollView.getScrollY() + scrollDelta);
//
//                    if(scrollView.getScrollY() + screenHeight >= totalHeight)
//                        scrollView.scrollTo(0,0);
//
//                    threadPause(delayTime);
//
//                }
//            }
//        }.start();
//    }

//    public void stopScroll(){
//        isPaused = true;
//    }
//
//    public void setScrollDelta(int delta){
//        scrollDelta = delta;
//    }
//
//    public void increaseScrollDelta(int increase){
//        //scrollDelta += increase;
//        delayTime -= increase;
//        //changeSpeed();
//    }
//
//    public void changeSpeed(){
//
//    }

    public void processData(List<String> recognized,List<String> unique){

    }

//    public int testRecognizedWords(){
//
//        int size = uniqueWords.size();
//        int recognized = 0;
//        for(String s:recognizedWords){
//            if(uniqueWords.contains(s))
//                recognized++;
//        }
//        double percent = recognized;
//        percent /= size;
//        Log.wtf(TAG,recognized + " " + size + " " + percent);
//        //if(percent > 0.5){
//        //    controller.swipeUp();
//        //    uniqueWords.clear();
//        //    uniqueWords.addAll(TextToGrammar.getUniqueWordsList(controller.getCurrentShowingSubString()));
//        //}
//
//        Log.wtf(TAG,Double.toString(Math.ceil(percent * 100)));
//        //progressBar.setProgress();
//
//        return ((int) Math.ceil(percent * 100));
//    }

    public List<String> getWordsListFromScreen(){

        Layout textViewLayout = contentText.getLayout();

        ArrayList<String> words = new ArrayList<>();

        int currentY = contentText.getScrollY();
        int lineHeight = contentText.getLineHeight();
        int currentEndY = currentY + screenHeight;

        for(int i=currentY;i<currentEndY;i+=lineHeight){
            int line = textViewLayout.getLineForVertical(i);
            int start = textViewLayout.getLineStart(line);
            int end = textViewLayout.getLineEnd(line);
            String s = text.substring(start,end);
            words.add(s);
        }

//        int startLine = textViewLayout.getLineForVertical(currentY);
//        int endLine = textViewLayout.getLineForVertical(currentEndY);
//
//        int start = textViewLayout.getLineStart(startLine);
//        int end = textViewLayout.getLineEnd(endLine);

        return words;

    }

}
