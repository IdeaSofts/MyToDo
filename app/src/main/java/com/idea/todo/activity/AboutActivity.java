package com.idea.todo.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.idea.todo.R;

public class AboutActivity extends Activity implements View.OnClickListener {
    private Button
            shareButton,
            feedbackButton,
            appsButton,
            rateButton;
    private ImageView
            twitterLogo,
            facebookLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
    }


    private void init() {
        TextView appVersion = (TextView) findViewById(R.id.app_version);
        appVersion.setText(appVersion.getText() + " " + getAppVersionName());

        shareButton = (Button) findViewById(R.id.button_share);
        feedbackButton = (Button)findViewById(R.id.button_feedback);
        appsButton = (Button) findViewById(R.id.button_apps);
        rateButton = (Button) findViewById(R.id.button_rate);
        twitterLogo = (ImageView) findViewById(R.id.btnTwitter);
        facebookLogo = (ImageView) findViewById(R.id.btnFacebook);
        twitterLogo.setOnClickListener(this);
        facebookLogo.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        feedbackButton.setOnClickListener(this);
        appsButton.setOnClickListener(this);
        rateButton.setOnClickListener(this);

    }
    private String getAppVersionName(){
        try
        {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    public void onClick(View v) {

        if (v == shareButton){
            Intent sharingIntent = new Intent("android.intent.action.SEND");
            String body = getString(R.string.description1) + " " + getString(R.string.app_name) + " " +
                    getString(R.string.description2) + " " +"\n" + "http://play.google.com/store/apps/details?id=" + getPackageName();

            sharingIntent.setType("text/plain");
            sharingIntent.putExtra("android.intent.extra.SUBJECT", getString(R.string.app_name));
            sharingIntent.putExtra("android.intent.extra.TEXT", body);
            startActivity(Intent.createChooser(sharingIntent, "send"));

        }

        else if (v == feedbackButton){
            Intent intent = new Intent("android.intent.action.SENDTO");
            String subject = getString(R.string.about_feedback_title) +
                    " " + getString(R.string.app_name) + " v" + getAppVersionName();
            intent.setData(Uri.parse("mailto:ideasoftwaretech@gmail.com"));
            intent.putExtra("android.intent.extra.SUBJECT", subject);
            startActivity(Intent.createChooser(intent, "Send"));
        }

        else if (v == appsButton){
            startActivity(Intent.createChooser(new Intent("android.intent.action.VIEW",
                    Uri.parse("market://search?q=pub:\"IdeaS0ft\"")), ""));
        }

        else if (v == rateButton){
            startActivity(Intent.createChooser(new Intent("android.intent.action.VIEW",
                    Uri.parse("market://details?id=" + getPackageName())), ""  ));

        }

        else if (v == twitterLogo){
            startActivity(Intent.createChooser(new Intent("android.intent.action.VIEW",
                    Uri.parse("http://twitter.com/IdeaS0ft")), ""));

        }

        else if (v == facebookLogo){
            startActivity(Intent.createChooser(new Intent("android.intent.action.VIEW",
                    Uri.parse("https://www.facebook.com/Idea-Soft-249042682134802/")), ""));
        }
    }

}

