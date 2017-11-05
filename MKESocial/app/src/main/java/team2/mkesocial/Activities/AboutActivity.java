package team2.mkesocial.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.net.Uri;

import team2.mkesocial.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_email);

    }

    public void fab_email_on_click(View v)
    {
        if(v.getId() == R.id.fab_email) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String emailAddress = getString(R.string.MKE_social_email_address);
            String defaultSubject = getString(R.string.default_email_subject);
            Uri data = Uri.parse("mailto:"+ emailAddress +"?subject=" + defaultSubject + "&body=" + "");
            intent.setData(data);
            startActivity(intent);
        }
    }


}
