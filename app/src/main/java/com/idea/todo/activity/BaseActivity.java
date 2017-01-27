package com.idea.todo.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.idea.todo.constants.C;
import com.idea.todo.listener.OnClickAlertDialog;
import com.idea.todo.model.AlertDialogArgs;

/**
 * Created by sha on 25/12/16.
 */

public class BaseActivity extends AppCompatActivity implements C {

    public void displayToast(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }

    public void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void log(String title, String val) {
        Log.e(title, val);
    }

    public String getInputTxt(EditText input){
        return input.getText().toString().trim();
    }

    public void alertDialog(final AlertDialogArgs args) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(args.getMsg());
        builder.setPositiveButton(args.getBtnPositiveRes(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onClickAlertDialog(true, args);
            }
        });

        if (args.isDisplayBtnCancel())
            builder.setNegativeButton(args.getBtnNegativeRes(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    onClickAlertDialog(false, args);
                    dialog.dismiss();
                }
            });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onClickAlertDialog(boolean isPositive, AlertDialogArgs args) {
        args.setPositive(isPositive);
        switch (args.getRequest()){

            case REQUEST_ALERT_DIALOG_TODO_DELETE:
                ((OnClickAlertDialog)args.getCurrentFrag()).onClickAlertDialog(args);
                break;

            case REQUEST_ALERT_DIALOG_TODO_DELETE_ALL:
                ((OnClickAlertDialog)args.getCurrentFrag()).onClickAlertDialog(args);
                break;

           case REQUEST_ALERT_DIALOG_GROUP_DELETE:
                ((OnClickAlertDialog)args.getCurrentFrag()).onClickAlertDialog(args);
                break;
        }
    }

    public String titledValue(int titleRes, String val){
        return new StringBuilder(getString(titleRes))
                .append(" : ")
                .append(val)
                .toString();
    }



}
