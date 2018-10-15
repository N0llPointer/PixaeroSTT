package com.nollpointer.pixaerostt;

import android.text.Layout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScrollViewController {
    private ScrollView scrollView;
    private TextView textView;
    private String text;

    private MainActivity activity;

    //Mostly constants till the end of session
    private int lineCount;
    private int textViewHeight;
    private int scrollViewHeight;
    private int lineHeight;


    public ScrollViewController(ScrollView scrollView, TextView textView,MainActivity activity) {
        this.scrollView = scrollView;
        this.textView = textView;
        this.activity = activity;

        text = textView.getText().toString();
        initializeController();
    }

    private void initializeController(){
        Layout textViewLayout = textView.getLayout();
        lineCount = textViewLayout.getLineCount();
        textViewHeight = textViewLayout.getHeight();
        scrollViewHeight = scrollView.getHeight();
        lineHeight = textViewHeight / lineCount;
    }

    public String getCurrentShowingSubString(){
        Layout textViewLayout = textView.getLayout();

        int currentY = scrollView.getScrollY();
        int currentEndY = currentY + scrollViewHeight;

        int startLine = textViewLayout.getLineForVertical(currentY);
        int endLine = textViewLayout.getLineForVertical(currentEndY);

        int start = textViewLayout.getLineStart(startLine);
        int end = textViewLayout.getLineEnd(endLine);

        return text.substring(start,end);
    }

    private String getCurrentShowingReadngSubString(){
        Layout textViewLayout = textView.getLayout();

        int currentY = scrollView.getScrollY();
        int currentEndY = currentY + scrollViewHeight/2;

        int startLine = textViewLayout.getLineForVertical(currentY);
        int endLine = textViewLayout.getLineForVertical(currentEndY);

        int start = textViewLayout.getLineStart(startLine);
        int end = textViewLayout.getLineEnd(endLine);

        return text.substring(start,end);
    }

    public void swipeUp(){
        scrollView.smoothScrollTo(0,scrollViewHeight + scrollView.getScrollY());
    }


}
