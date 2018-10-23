package com.nollpointer.pixaerostt;

import android.text.Layout;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nollpointer.pixaerostt.utils.TextToGrammar;

import java.util.ArrayList;
import java.util.List;

import static com.nollpointer.pixaerostt.MainActivity.TAG;

public class ScrollViewController {
    private ScrollView scrollView;
    private TextView contentText;
    private String text;


    ScrollViewController(ScrollView scrollView, TextView textView) {
        this.scrollView = scrollView;
        this.contentText = textView;

        text = textView.getText().toString();
        text = text.toLowerCase();
        text = TextToGrammar.deletePunctuationSigns(text);
        scrollView.scrollTo(0, 0);
    }

    public String processData(List<String> recognized) {

        if (recognized.size() == 0)
            return null;

        List<String> linesShown = getWordsListFromScreen();
        int lineCountToScroll = 0;

        String lastWordInLine = "";
        int startIndex = 0;

        for (int i = 0; i < linesShown.size(); i++) {
            String line = linesShown.get(i);
            //int length = line.length();

            String words[] = line.split("\\s+");

            double percent = 0;
            int size = words.length;
            String lastRecognizedWord = "";
            //boolean shouldScroll = false;
            for (int j = 0; j < size; j++) {

                if (startIndex == 0) {
                    startIndex = recognized.indexOf(words[j]);
                    //endIndex = recognized.indexOf(words[j]);
                    lastRecognizedWord = words[j];
                }

                if (recognized.contains(words[j])) {
                    int index = recognized.indexOf(words[j]);
                    if (index >= startIndex) {
                        percent++;
                        lastRecognizedWord = words[j];
                    }
                }
            }

            percent = (percent / size) * 100;
            //shouldScroll = percent > 60;

            //int delta = endIndex - startIndex;

            if (percent > 60) {
                lineCountToScroll = i + 1;
                lastWordInLine = lastRecognizedWord;
            }

            startIndex = 0;
        }

        if (lineCountToScroll != 0) {
            scrollLines(lineCountToScroll, contentText.getLineHeight());
            return lastWordInLine;
        } else
            return null;

    }


//    public void processData(String recognized) {
//
//        //screenHeight = scrollView.getHeight();
//        if (recognized.length() == 0)
//            return;
//
//        List<String> linesShown = getWordsListFromScreen();
//        int lineCountToScroll = 0;
//
//        for (int i = 0; i < linesShown.size(); i++) {
//            String line = linesShown.get(i);
//            if (recognized.contains(line)) {
//                lineCountToScroll = i + 1;
//                Log.e(TAG, "IsRecognized = true");
//            }
//            Log.e(TAG, "IsRecognized = false");
//
//        }
//
//        if (lineCountToScroll != 0)
//            scrollLines(lineCountToScroll, contentText.getLineHeight());
//
//    }

    public void processDataV2(String recognized) {

        //screenHeight = scrollView.getHeight();
        if (recognized.length() == 0)
            return;

        List<String> linesShown = getWordsListFromScreen();
        int lineCountToScroll = 0;

        for (int i = 0; i < linesShown.size(); i++) {
            String line = linesShown.get(i);
            if (line.equals(" "))
                continue;
            line = line.replaceAll("\\s+", " ");
            //String[] words = line.split("\\s+");

            if (recognized.contains(line)) {
                lineCountToScroll = i + 1;
                Log.e(TAG, "IsRecognized = true + " + line);
            }
            Log.e(TAG, "IsRecognized = false");

        }

        if (lineCountToScroll != 0)
            scrollLines(lineCountToScroll, contentText.getLineHeight());

    }

    private void scrollLines(final int lines, final int lineHeight) {
        final int currentScroll = scrollView.getScrollY();
        final int scrollTo = currentScroll + lineHeight * (lines - 1);

        scrollView.smoothScrollTo(0, scrollTo);

    }


    //Production Methods

    private List<String> getWordsListFromScreen() {

        int screenHeight = scrollView.getHeight();

        Layout textViewLayout = contentText.getLayout();

        ArrayList<String> words = new ArrayList<>();

        int currentY = scrollView.getScrollY();
        int lineHeight = contentText.getLineHeight();
        int currentEndY = currentY + screenHeight;

        for (int i = currentY; i < currentEndY; i += lineHeight) {
            int line = textViewLayout.getLineForVertical(i);
            int start = textViewLayout.getLineStart(line);
            int end = textViewLayout.getLineEnd(line);
            String s = text.substring(start, end);
            Log.e(TAG, "getWordsListFromScreen: " + i + " " + s);
            words.add(s);
        }

        return words;

    }


//        new Handler().postDelayed(new Runnable() {
//                               @Override
//                               public void run() {
//                                   scrollView.smoothScrollTo(0, scrollView.getScrollY() + (lines-2) * lineHeight);
//                               }
//                           },400);
    //ObjectAnimator.ofInt(scrollView, "scrollY",  scrollView.getScrollY() + lines*lineHeight).setDuration(100).start();

//    private void threadPause(long millis){
//        try {
//            Thread.sleep(millis);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

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

//    /*private String getCurrentShowingReadngSubString(){
//        Layout textViewLayout = contentText.getLayout();
//
//        int currentY = contentText.getScrollY();
//        int currentEndY = currentY + scrollViewHeight/2;
//
//        int startLine = textViewLayout.getLineForVertical(currentY);
//        int endLine = textViewLayout.getLineForVertical(currentEndY);
//
//        int start = textViewLayout.getLineStart(startLine);
//        int end = textViewLayout.getLineEnd(endLine);
//
//        return text.substring(start,end);
//    }*/

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
//
//    private void prepareText(){
//        int count = screenHeight/(totalHeight/lineCount) + 1;
//        for(int i=0;i<count;i++){
//            text = "\n" + text;
//        }
//        for(int i=0;i<count;i++){
//            text = text + "\n";
//        }
//
//        contentText.setText(text);
//    }

}
