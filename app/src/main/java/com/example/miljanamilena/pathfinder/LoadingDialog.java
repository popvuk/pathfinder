package com.example.miljanamilena.pathfinder;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;


/**
 * Created by MiljanaMilena on 2/8/2018.
 */

public class LoadingDialog {

    Context ctx;

    public LoadingDialog(Context context)
    {
        ctx = context;
    }

    public Dialog createLoadingSpinner()
    {
        Dialog dialog = new Dialog(ctx, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return dialog;
    }
}
