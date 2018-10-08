package com.nollpointer.pixaerostt;

import android.widget.ScrollView;
import android.widget.TextView;

public class ScrollViewController {
    private ScrollView scrollView;
    private TextView textView;
    private String text;


    public ScrollViewController(ScrollView scrollView, TextView textView) {
        this.scrollView = scrollView;
        this.textView = textView;
        initializeController();
    }

    private void initializeController(){

    }
}
