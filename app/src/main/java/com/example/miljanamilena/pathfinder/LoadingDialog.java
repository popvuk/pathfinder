package custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;

import com.example.popvuk.vchat.R;

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
        //here we set layout of progress dialog
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return dialog;
    }
}
