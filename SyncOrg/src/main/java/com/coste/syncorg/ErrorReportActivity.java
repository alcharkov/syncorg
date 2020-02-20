package com.coste.syncorg;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ErrorReportActivity extends AppCompatActivity {
    public final static String SYNC_FAILED = "com.coste.syncorg.SYNC_FAILED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        if (intent.getAction().equals(SYNC_FAILED)) {
//            Bundle extrasBundle = intent.getExtras();
//            String errorMsg = extrasBundle.getString("ERROR_MESSAGE");
//            showSyncFailPopup(errorMsg);
//        }
//    }

    private void showSyncFailPopup(String errorMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(errorMsg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
