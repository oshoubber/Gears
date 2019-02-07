package me.oshoubber.gears;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_TEXT, "Leave constructive feedback here!");
        intent.putExtra(Intent.EXTRA_EMAIL,  new String[] { "gearsobd2@gmail.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT,"Gears Feedback");
        startActivity(intent);
        finish();
    }
}
