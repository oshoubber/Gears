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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;

public class SystemTestingActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mToggle;
    public static BluetoothSocket bluetoothSocket;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public final static int REQUEST_BT_FINISH = 1;
    public char[] FixedResult;

    //Test Availability
    int avail[] = new int[12];
    int compl[] = new int[12];
    ImageView imageArray[] = new ImageView[12];
    PidCommand0101 pidCommand0101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_testing);

        DrawerLayout mDrawerlayout = (DrawerLayout) findViewById(R.id.sysDrawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        closeSocket();
                        break;
                    case R.id.FaultCodes:
                        Intent intentFaultCodes = new Intent(SystemTestingActivity.this, FaultCodeActivity.class);
                        startActivity(intentFaultCodes);
                        closeSocket();
                        break;
                    case R.id.LiveInformation:
                        Intent intentLiveData = new Intent(SystemTestingActivity.this, LiveInformationActivity.class);
                        startActivity(intentLiveData);
                        closeSocket();
                        break;
                    case R.id.SystemTesting:
                        break;
                    case R.id.Feedback:
                        Intent intentFeedback = new Intent (SystemTestingActivity.this, FeedbackActivity.class);
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

        // Start the Bluetooth Activity
        Intent intentBluetooth = new Intent(this, BluetoothActivity.class);
        startActivityForResult(intentBluetooth, REQUEST_BT_FINISH);

        imageArray[0] = (ImageView) findViewById(R.id.test1);
        imageArray[1] = (ImageView) findViewById(R.id.test2);
        imageArray[2] = (ImageView) findViewById(R.id.test3);
        imageArray[3] = (ImageView) findViewById(R.id.test4);
        imageArray[4] = (ImageView) findViewById(R.id.test5);
        imageArray[5] = (ImageView) findViewById(R.id.test6);
        imageArray[6] = (ImageView) findViewById(R.id.test7);
        imageArray[7] = (ImageView) findViewById(R.id.test8);
        imageArray[8] = (ImageView) findViewById(R.id.test9);
        imageArray[9] = (ImageView) findViewById(R.id.test10);
        imageArray[10] = (ImageView) findViewById(R.id.test11);
        imageArray[11] = (ImageView) findViewById(R.id.test12);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_BT_FINISH:
                if (resultCode == Activity.RESULT_OK) {
                    // Once Activity has finished, update LiveDatActivity's Bluetooth Socket
                    bluetoothSocket = BluetoothActivity.bluetoothSocket;
                    Log.e("SOCKET", "SOCKET ID: " + bluetoothSocket);

                    TestODB testODB = new TestODB();
                    testODB.execute();
                } else {
                    Log.e("ERROR", "BluetoothActivity DID NOT FINISH");
                    finish();
                }
        }
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

    private class TestODB extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SystemTestingActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Testing Input & Output streams...");
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
                    e.printStackTrace();
                    Log.e("Test Error", "OBD I/O STREAM ERROR");
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            if (flag) {
                pidCommand0101 = new PidCommand0101();
                pidCommand0101.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(SystemTestingActivity.this);
                dialog.setTitle("Device Error");
                dialog.setMessage("Could not read or write data to the Bluetooth adapter.");
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

    private class PidCommand0101 extends AsyncTask<Void, Void, Boolean> {

        SystemTestingCommand stc = new SystemTestingCommand();

        @Override
        protected Boolean doInBackground(Void... v) {
            // Tests Input & Output Streams, then runs any API Commands
            if (mBluetoothAdapter != null && bluetoothSocket != null) {
                try {
                    stc.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Test Error", "OBD I/O STREAM ERROR");
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            final String hexResult = stc.getFormattedResult();
            final String[] binaryArray;
            Log.e("FORMATTED RESULT:", hexResult);

            FixedResult = hexResult.substring(2, 8).toCharArray();
            binaryArray = getBinaryArray(FixedResult);

            for (int i = 0; i < 6; i++) {
                Log.e("BINARY ARRAY", binaryArray[i]);
            }

            binToArray(binaryArray);
        }
    }

    static String[] getBinaryArray(char[] c) {
        String[] binaryArray = new String[6];

        for (int i = 0; i < 6; i++) {
            binaryArray[i] = hexToBin(c[i]);
        }

        return binaryArray;
    }

    static String hexToBin(char s) {
        switch (s) {
            case '0':
                return "0000";
            case '1':
                return "0001";
            case '2':
                return "0010";
            case '3':
                return "0011";
            case '4':
                return "0100";
            case '5':
                return "0101";
            case '6':
                return "0110";
            case '7':
                return "0111";
            case 'A':
                return "1010";
            case 'B':
                return "1011";
            case 'C':
                return "1100";
            case 'D':
                return "1101";
            case 'E':
                return "1110";
            case 'F':
                return "1111";
        }
        return null;
    }

    public void binToArray(String[] s) {
        // Availability Arrays
        char[] b1 = s[1].toCharArray();
        char[] c1 = s[3].toCharArray();
        char[] c2 = s[2].toCharArray();

        // Completeness Arrays
        char[] b2 = s[0].toCharArray();
        char[] d1 = s[5].toCharArray();
        char[] d2 = s[4].toCharArray();


        // Final Availability Array
        int availIndex = 0;
        for (int i = 3; i >= 0; i--, availIndex++) {
            avail[availIndex] = (b1[i] == '1' ? 1 : 0);
        }

        for (int i = 3; i >= 0; i--, availIndex++) {
            avail[availIndex] = (c1[i] == '1' ? 1 : 0);
        }

        for (int i = 3; i >= 0; i--, availIndex++) {
            avail[availIndex] = (c2[i] == '1' ? 1 : 0);
        }

        // Final Completeness Array
        int complIndex = 0;
        for (int i = 3; i >= 0; i--, complIndex++) {
            compl[complIndex] = (b2[i] == '0' ? 1 : 0);
        }

        for (int i = 3; i >= 0; i--, complIndex++) {
            compl[complIndex] = (d1[i] == '0' ? 1 : 0);
        }
        for (int i = 3; i >= 0; i--, complIndex++) {
            compl[complIndex] = (d2[i] == '0' ? 1 : 0);
        }

        ArrayToUI();

        // Log both Arrays
        for (int i = 0; i < 12; i++) {
            Log.e("AVAILABILITY ARRAY", "" + avail[i]);
        }
        for (int i = 0; i < 12; i++) {
            Log.e("COMPLETENESS ARRAY", "" + compl[i]);
        }
    }

    public void ArrayToUI() {
        for (int i = 0; i < 12; i++) {
            if (avail[i] == 1 && compl[i] == 1) {
                imageArray[i].setImageResource(R.drawable.checkmark);
            }
            else if (avail[i] == 1 && compl[i] == 0) {
                imageArray[i].setImageResource(R.drawable.failed);
            } else {
                imageArray[i].setImageResource(R.drawable.not_available);
            }
        }
    }
}