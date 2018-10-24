package com.nollpointer.pixaerostt;

import android.animation.ObjectAnimator;
import android.text.Layout;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nollpointer.pixaerostt.utils.TextToGrammar;

import java.util.ArrayList;
import java.util.List;

import static com.nollpointer.pixaerostt.MainActivity.TAG;

public class ScrollViewController {

    private static final int SCROLL_TIME = 250;

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


    //Legacy code
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

        if (recognized.length() == 0)
            return;

        List<String> linesShown = getWordsListFromScreen();
        int lineCountToScroll = 0;

        for (int i = 0; i < linesShown.size(); i++) {
            String line = linesShown.get(i);
            if (line.equals(" "))
                continue;
            line = line.replaceAll("\\s+", " ");

            if (recognized.contains(line)) {
                lineCountToScroll = i + 1;
                Log.e(TAG, "Recognized " + line);
            }
            Log.e(TAG, "Not Recognized " + line);

        }

        if (lineCountToScroll != 0)
            scrollLines(lineCountToScroll, contentText.getLineHeight());

    }

    private void scrollLines(final int lines, final int lineHeight) {
        int currentScroll = scrollView.getScrollY();
        int scrollTo = currentScroll + lineHeight * (lines - 1);
        scrollTo = scrollTo - (scrollTo % lineHeight);

        //scrollView.smoothScrollTo(0, scrollTo);

        ObjectAnimator.ofInt(scrollView, "scrollY",  scrollTo).setDuration(SCROLL_TIME).start();

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

}
