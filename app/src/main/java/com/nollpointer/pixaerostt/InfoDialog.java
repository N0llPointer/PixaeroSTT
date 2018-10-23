package com.nollpointer.pixaerostt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoDialog extends BottomSheetDialogFragment {

    public static final String INFO = "info";

    public static InfoDialog getInstance(String info){
        InfoDialog dialog = new InfoDialog();
        Bundle args = new Bundle();
        args.putString(INFO,info);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        TextView mainView = ((TextView) inflater.inflate(R.layout.info_dialog, container, false));

        String info = getArguments().getString(INFO);

        mainView.setText(info);

        return mainView;

    }
}
