package me.oshoubber.gears;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign Buttons
        Button mButton1 = (Button) findViewById(R.id.dfc);
        Button mButton2 = (Button) findViewById(R.id.rti);
        Button mButton3 = (Button) findViewById(R.id.st);
        FloatingActionButton mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        // Navigation Drawer Button
        DrawerLayout mDrawerlayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                         break;
                    case R.id.FaultCodes:
                        Intent intentFaultCodes = new Intent(MainActivity.this, FaultCodeActivity.class);
                        startActivity(intentFaultCodes);
                        break;
                    case R.id.LiveInformation:
                        Intent intentLiveData = new Intent(MainActivity.this, LiveInformationActivity.class);
                        startActivity(intentLiveData);
                        break;
                    case R.id.SystemTesting:
                        Intent intentSystemTesting = new Intent(MainActivity.this, SystemTestingActivity.class);
                        startActivity(intentSystemTesting);
                        break;
                    case R.id.Feedback:
                        Intent intentFeedback = new Intent (MainActivity.this, FeedbackActivity.class);
                        startActivity(intentFeedback);
                        break;
                }
                return false;
            }
        });
        mToggle = new ActionBarDrawerToggle(this, mDrawerlayout, R.string.open, R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Bluetooth FAB
        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intentBluetooth = new Intent();
                intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentBluetooth);
            }
        });

        // Start the Fault Code Activity
        mButton1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentFaultCodes = new Intent(MainActivity.this, FaultCodeActivity.class);
                startActivity(intentFaultCodes);
            }
        });

        // Start the Live Data Activity
        mButton2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentLiveData = new Intent(MainActivity.this, LiveInformationActivity.class);
                startActivity(intentLiveData);
            }
        });

        // Start the System Testing Activity
        mButton3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentSystemTesting = new Intent(MainActivity.this, SystemTestingActivity.class);
                startActivity(intentSystemTesting);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Navigation Drawer Button
        return (mToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

}
