package me.oshoubber.gears;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;

public class LiveInformationActivity extends AppCompatActivity {



    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ActionBarDrawerToggle mToggle;
    public static BluetoothSocket bluetoothSocket;
    public final static int REQUEST_BT_FINISH = 1;
    public boolean enableTasks;
    public ViewPager mViewPager;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_information);

        // Set Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SectionsPagerAdapter mSectionsPagerAdapter;
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        DrawerLayout mDrawerlayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        closeSocket();
                        break;
                    case R.id.FaultCodes:
                        Intent intentFaultCodes = new Intent(LiveInformationActivity.this, FaultCodeActivity.class);
                        startActivity(intentFaultCodes);
                        closeSocket();
                        break;
                    case R.id.LiveInformation:
                        break;
                    case R.id.SystemTesting:
                        Intent intentSystemTesting = new Intent(LiveInformationActivity.this, SystemTestingActivity.class);
                        startActivity(intentSystemTesting);
                        closeSocket();
                        break;
                    case R.id.Feedback:
                        Intent intentFeedback = new Intent (LiveInformationActivity.this, FeedbackActivity.class);
                        startActivity(intentFeedback);
                        break;
                }
                return false;
            }
        });
        mToggle = new ActionBarDrawerToggle(this, mDrawerlayout, toolbar, R.string.open, R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();

        // ViewPager Behavior
        mViewPager.setCurrentItem(1, false);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab){}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        // Start Bluetooth Activity
        Intent intentBluetooth = new Intent(this, BluetoothActivity.class);
        startActivityForResult(intentBluetooth,REQUEST_BT_FINISH);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return (mToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException ioException) {
                Log.e("IO Exception", "Could not close Socket");
            }
        }
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case REQUEST_BT_FINISH:
                if (resultCode == Activity.RESULT_OK) {
                    // Once Activity has finished, update LiveDatActivity's Bluetooth Socket
                    bluetoothSocket = BluetoothActivity.bluetoothSocket;
                    Log.e("SOCKET", "SOCKET ID: " + bluetoothSocket);

                    try {
                        TestODB testODB = new TestODB();
                        testODB.execute();
                    } catch (Exception e) {
                        Log.e("ERROR", "TEST ODB FAILED");
                    }
                }
                else {
                    Log.e("ERROR", "BluetoothActivity DID NOT FINISH");
                    finish();
                }
        }
    }

    public void closeSocket(){
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException ioException) {
                Log.e("IO Exception", "Could not close Socket");
            }
        }
        finish();
    }


    private class TestODB extends AsyncTask<Void,Void,Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LiveInformationActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Preparing Input & Output streams...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            // Tests Input & Output Streams, then runs any API Commands
            if (mBluetoothAdapter != null && bluetoothSocket != null) {
                try {
                    new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new TimeoutCommand(125).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                    progressDialog.dismiss();

                    return true;

                } catch (Exception e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                    Log.e("Test Error", "OBD I/O STREAM ERROR");
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            if (flag) {
                enableTasks = true;
            } else {
                enableTasks = false;
                final AlertDialog.Builder dialog = new AlertDialog.Builder(LiveInformationActivity.this);
                dialog.setTitle("Device Error");
                dialog.setMessage("Could not read data from the Bluetooth adapter.");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        }
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new LiveDataTemperatureActivity();
                    case 1:
                        return new LiveDataDrivingActivity();
                    case 2:
                        return new LiveDataPressureActivity();
                    default:
                        return null;
                }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "TEMPERATURES";
                case 1:
                    return "DRIVING";
                case 2:
                    return "PRESSURE";
            }
            return null;
        }
    }
}
