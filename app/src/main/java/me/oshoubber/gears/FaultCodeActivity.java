package me.oshoubber.gears;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.bluetooth.BluetoothSocket;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.control.PendingTroubleCodesCommand;
import com.github.pires.obd.commands.control.PermanentTroubleCodesCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.oshoubber.gears.R.layout.activity_faultcodes;


public class FaultCodeActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ActionBarDrawerToggle mToggle;
    public static BluetoothSocket bluetoothSocket;
    public final static int REQUEST_BT_FINISH = 1;
    public Context context;

    public RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    public GetPendingCodes gpc;
    public GetCurrentCodes gcc;
    public GetPermanentCodes gpmc;
    public int codeCount;
    public ArrayList<ErrorArray> errorArray = new ArrayList<>();
    public Map<String, String> FaultCodesError;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_faultcodes);

        FaultCodesError = getDict(R.array.dtc_keys, R.array.dtc_values);

        // Navigation Drawer Button
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
                        break;
                    case R.id.LiveInformation:
                        Intent intentLiveData = new Intent(FaultCodeActivity.this, LiveInformationActivity.class);
                        startActivity(intentLiveData);
                        closeSocket();
                        break;
                    case R.id.SystemTesting:
                        Intent intentSystemTesting = new Intent(FaultCodeActivity.this, SystemTestingActivity.class);
                        startActivity(intentSystemTesting);
                        closeSocket();
                        break;
                    case R.id.Feedback:
                        Intent intentFeedback = new Intent (FaultCodeActivity.this, FeedbackActivity.class);
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

        // Start Bluetooth Activity
        Intent intentBluetooth = new Intent(this, BluetoothActivity.class);
        startActivityForResult(intentBluetooth,REQUEST_BT_FINISH);
        context = getApplicationContext();

        // RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.clearDTCs) {
            ClearDTCTask c = new ClearDTCTask();
            c.execute();
        }
        return (mToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cleardtc_menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case REQUEST_BT_FINISH:
                if (resultCode == Activity.RESULT_OK) {
                    // Once Activity has finished, update LiveDatActivity's Bluetooth Socket
                    bluetoothSocket = BluetoothActivity.bluetoothSocket;
                    Log.e("SOCKET", "SOCKET ID: " + bluetoothSocket);

                    TestODB testODB = new TestODB();
                    testODB.execute();
                }
                else {
                    Log.e("ERROR", "BluetoothActivity DID NOT FINISH");
                    finish();
                }
        }
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

    private class ModifiedTroubleCodesObdCommand extends TroubleCodesCommand {
        @Override
        public String getResult() {
            // remove unwanted response from output since this results in erroneous error codes
            return rawData.replace("SEARCHING...", "").replace("NODATA", "");
        }
    }

    private class ModifiedPendingCodesCommand extends PendingTroubleCodesCommand {
        @Override
        public String getResult() {
            // remove unwanted response from output since this results in erroneous error codes
            return rawData.replace("SEARCHING...", "").replace("NODATA", "");
        }
    }

    private class ModifiedPermanentCodesCommand extends PermanentTroubleCodesCommand {
        @Override
        public String getResult() {
            // remove unwanted response from output since this results in erroneous error codes
            return rawData.replace("SEARCHING...", "").replace("NODATA", "");
        }
    }

    private class TestODB extends AsyncTask<Void,Void,Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(FaultCodeActivity.this);
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
                gpc = new GetPendingCodes();
                gpmc = new GetPermanentCodes();
                gcc = new GetCurrentCodes();

                gpc.execute();
                gcc.execute();
                gpmc.execute();

            } else {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(FaultCodeActivity.this);
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

    private class GetPendingCodes extends AsyncTask<Void, String, Integer> {

        ModifiedPendingCodesCommand modpcCommand = new ModifiedPendingCodesCommand();
        String pendingCodes;
        ProgressDialog rDialog;

        @Override
        protected void onPreExecute() {
            rDialog = new ProgressDialog(FaultCodeActivity.this);
            rDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            rDialog.setMessage("Searching for pending codes...");
            rDialog.setIndeterminate(true);
            rDialog.setCancelable(false);
            rDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... param) {
            try {
                modpcCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                pendingCodes = modpcCommand.getFormattedResult();
                Log.e("PENDING: ", pendingCodes);

                if (pendingCodes == null) { return 0; } else {
                    for (final String pCodes : pendingCodes.split("\n")) {
                        if (FaultCodesError.get(pCodes) != null) {
                            errorArray.add(new ErrorArray(pCodes, FaultCodesError.get(pCodes), 1));
                            codeCount++;
                            Log.d("WORKING", "PENDING CODE: " + pendingCodes);
                        }
                    }
                    return 0;
                }
            } catch (IOException io) {
                Log.e("IO EXCEPTION", "YOU'RE GOOD ERROR");
                return 0;
            } catch (NoDataException nd) {
                Log.e("NO", "DATA");
                return 0;
            } catch (InterruptedException ie) {
                Log.e("INTERRUPTION", "ERROR");
                return 1;
            } catch (UnableToConnectException ute) {
                Log.e("ERROR", "Unable to connect");
                return 2;
            } catch (StringIndexOutOfBoundsException se) {
                return 3;
            }
        }

        @Override
        protected void onPostExecute(Integer flag) {
            switch (flag) {
                case 0:
                    // NO DATA or IO EXCEPTION or FINISHED
                    rDialog.dismiss();
                    break;
                case 1:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog.setTitle("Error");
                    dialog.setMessage("Could not read fault codes.");
                    dialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                    break;
                case 2:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog2.setTitle("Error");
                    dialog2.setMessage("Could not connect to the device. Unplug the adapter and try again.");
                    dialog2.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog2.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            closeSocket();
                            finish();
                            startActivity(getIntent());
                        }
                    });
                    dialog2.setCancelable(false);
                    dialog2.show();
                    break;
                case 3:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog3 = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog3.setTitle("Error");
                    dialog3.setMessage("This bluetooth device is not an OBD2 device. You must purchase an OBD2 adapter and connect it to your car.");
                    dialog3.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog3.setCancelable(false);
                    dialog3.show();
                    break;
            }
        }
    }

    private class GetCurrentCodes extends AsyncTask<Void, String, Integer> {

        ModifiedTroubleCodesObdCommand modtcCommand = new ModifiedTroubleCodesObdCommand();
        String currentCodes;
        ProgressDialog rDialog;

        @Override
        protected void onPreExecute() {
            rDialog = new ProgressDialog(FaultCodeActivity.this);
            rDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            rDialog.setMessage("Searching for current codes...");
            rDialog.setIndeterminate(true);
            rDialog.setCancelable(false);
            rDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... param) {
            try {
                modtcCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                currentCodes = modtcCommand.getFormattedResult();
                Log.e("CURRENT: ", currentCodes);

                if (currentCodes == null) { return 0; } else {
                    for (final String cCodes : currentCodes.split("\n")) {
                        if (FaultCodesError.get(cCodes) != null) {
                            errorArray.add(new ErrorArray(cCodes, FaultCodesError.get(cCodes), 0));
                            codeCount++;
                            Log.d("WORKING", "CURRENT CODE: " + currentCodes);
                        }
                    }
                    return 0;
                }
            } catch (IOException io) {
                Log.e("IO EXCEPTION", "YOU'RE GOOD ERROR");
                return 0;
            } catch (NoDataException nd) {
                Log.e("NO", "DATA");
                return 0;
            } catch (InterruptedException ie) {
                Log.e("INTERRUPTION", "ERROR");
                return 1;
            } catch (UnableToConnectException ute) {
                Log.e("ERROR", "Unable to connect");
                return 2;
            } catch (StringIndexOutOfBoundsException se) {
                return 3;
            }

        }

        @Override
        protected void onPostExecute(Integer flag) {
            switch (flag) {
                case 0:
                    // NO DATA or IO EXCEPTION or FINISHED
                    rDialog.dismiss();
                    break;
                case 1:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog.setTitle("Error");
                    dialog.setMessage("Could not read fault codes.");
                    dialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                    break;
                case 2:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog2.setTitle("Error");
                    dialog2.setMessage("Could not connect to the device. Unplug the adapter and try again.");
                    dialog2.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog2.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            closeSocket();
                            finish();
                            startActivity(getIntent());
                        }
                    });
                    dialog2.setCancelable(false);
                    dialog2.show();
                    break;
                case 3:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog3 = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog3.setTitle("Error");
                    dialog3.setMessage("This bluetooth device is not an OBD2 device. You must purchase an OBD2 adapter and connect it to your car.");
                    dialog3.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog3.setCancelable(false);
                    dialog3.show();
                    break;
            }
        }
    }

    private class GetPermanentCodes extends AsyncTask<Void, String, Integer> {

        ModifiedPermanentCodesCommand modpmcCommand = new ModifiedPermanentCodesCommand();
        String permanentCodes;
        ProgressDialog rDialog;

        @Override
        protected void onPreExecute() {
            rDialog = new ProgressDialog(FaultCodeActivity.this);
            rDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            rDialog.setMessage("Searching for permanent codes...");
            rDialog.setIndeterminate(true);
            rDialog.setCancelable(false);
            rDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... param) {
            try {
                modpmcCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                permanentCodes = modpmcCommand.getFormattedResult();
                Log.e("PERMANENT: ", permanentCodes);


                if (permanentCodes == null) { return 0; } else {
                    for (final String pmCodes : permanentCodes.split("\n")) {
                        if (FaultCodesError.get(pmCodes) != null) {
                            errorArray.add(new ErrorArray(pmCodes, FaultCodesError.get(pmCodes), 2));
                            codeCount++;
                            Log.d("WORKING", "PERMANENT CODE: " + permanentCodes);
                        }
                    }
                    return 1;
                }
            } catch (IOException io) {
                Log.e("NOTHING FOUND", "YOU'RE GOOD ERROR");
                return 0;
            } catch (InterruptedException ie) {
                Log.e("INTERRUPTION", "ERROR");
                return 2;
            } catch (NoDataException nd) {
                Log.e("NO", "DATA");
                if (codeCount > 0) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (UnableToConnectException ute) {
                Log.e("ERROR", "Unable to connect");
                return 3;
            } catch (StringIndexOutOfBoundsException se) {
                return 4;
            }
        }

        @Override
        protected void onPostExecute(Integer flag) {

            mAdapter = new CustomAdapter(errorArray);
            mRecyclerView.setAdapter(mAdapter);

            switch (flag) {
                case 0:
                    rDialog.dismiss();
                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "No fault codes were found!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("DISMISS", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    })
                            .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                            .show();
                    break;

                case 1:
                    // Ran & result != null
                    rDialog.dismiss();
                    if (codeCount <= 0) {
                        final Snackbar snackbar1 = Snackbar.make(findViewById(android.R.id.content),
                                "No fault codes were found!", Snackbar.LENGTH_INDEFINITE);
                        snackbar1.setAction("DISMISS", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar1.dismiss();
                            }
                        })
                                .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                                .show();
                    } else {
                        final Snackbar snackbar2 = Snackbar.make(findViewById(android.R.id.content),
                                codeCount + " fault code(s) were found.", Snackbar.LENGTH_INDEFINITE);
                        snackbar2.setAction("DISMISS", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar2.dismiss();
                            }
                        })
                                .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                                .show();
                    }
                    break;

                case 2:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog.setTitle("Error");
                    dialog.setMessage("Could not read fault codes.");
                    dialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                    break;
                case 3:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog2 = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog2.setTitle("Error");
                    dialog2.setMessage("Could not connect to the device. Unplug the adapter and try again.");
                    dialog2.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog2.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            closeSocket();
                            finish();
                            startActivity(getIntent());
                        }
                    });
                    dialog2.setCancelable(false);
                    dialog2.show();
                    break;
                case 4:
                    rDialog.dismiss();
                    final AlertDialog.Builder dialog3 = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog3.setTitle("Error");
                    dialog3.setMessage("This bluetooth device is not an OBD2 device. You must purchase an OBD2 adapter and connect it to your car.");
                    dialog3.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog3.setCancelable(false);
                    dialog3.show();
                    break;
            }
        }
    }

    Map<String, String> getDict(int keyId, int valId) {
        String[] keys = getResources().getStringArray(keyId);
        String[] vals = getResources().getStringArray(valId);

        Map<String, String> dict = new HashMap<>();
        for (int i = 0, l = keys.length; i < l; i++) {
            dict.put(keys[i], vals[i]);
        }
        return dict;
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

        private ArrayList<ErrorArray> errorSet;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView errorCode;
            TextView errorMessage;
            ImageView errorType;
            ImageView gSearch;


            private MyViewHolder(View itemView) {
                super(itemView);
                this.errorCode = (TextView) itemView.findViewById(R.id.errorCode);
                this.errorMessage = (TextView) itemView.findViewById(R.id.errorMessage);
                this.errorType = (ImageView) itemView.findViewById(R.id.errorType);
                this.gSearch = (ImageView) itemView.findViewById(R.id.gSearchButton);
            }
        }

        private CustomAdapter(ArrayList<ErrorArray> data) {
            this.errorSet = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_card_layout, parent, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

            TextView textViewCode = holder.errorCode;
            TextView textViewMessage = holder.errorMessage;
            ImageView imageErrorType = holder.errorType;
            ImageView gSearchButton = holder.gSearch;

            textViewCode.setText(errorSet.get(holder.getAdapterPosition()).getErrorCode());
            textViewMessage.setText(errorSet.get(holder.getAdapterPosition()).getErrorMessage());
            imageErrorType.setImageResource(errorSet.get(holder.getAdapterPosition()).getErrorImage());
            imageErrorType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(FaultCodeActivity.this);
                    dialog.setTitle("Fault Code Type");
                    switch (errorSet.get(holder.getAdapterPosition()).getErrorType()) {
                        case 0:
                            dialog.setMessage("This is a current fault code. All orange colored" +
                                    " codes are current.");
                            break;
                        case 1:
                            dialog.setMessage("This is a pending fault code. All yellow colored" +
                                    " codes are pending.");
                            break;
                        case 2:
                            dialog.setMessage("This is a permanent fault code. All red colored" +
                                    " codes are permanent.");
                            break;
                        default:
                            break;
                    }
                    dialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.setCancelable(true);
                }
            });
            gSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("http://www.google.com/#q=fault+code+" +
                            errorSet.get(holder.getAdapterPosition()).getErrorCode());
                    Intent gSearchIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(gSearchIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return errorSet.size();
        }
    }

    private class ErrorArray {
        String errorCode;
        String errorMessage;
        int errorType;

        private ErrorArray(String error_Code, String error_Message, int error_Type){
            this.errorCode = error_Code;
            this.errorMessage = error_Message;
            this.errorType = error_Type;
        }

        private String getErrorCode() {
            return errorCode;
        }

        private String getErrorMessage() {
            return errorMessage;
        }

        private int getErrorType(){ return errorType; }

        private int getErrorImage(){
            if (errorType == 0) { return R.drawable.error_current; }
            if (errorType == 1) { return R.drawable.error_pending; }
            if (errorType == 2) { return R.drawable.error_perm; }
            else return R.drawable.error_current;
        }

    }

    private class ClearDTCTask extends AsyncTask<Void,Void,Boolean> {
        ProgressDialog clearDialog;
        ResetTroubleCodesCommand clearDTC = new ResetTroubleCodesCommand();

        @Override
        protected void onPreExecute() {
            clearDialog = new ProgressDialog(FaultCodeActivity.this);
            clearDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            clearDialog.setMessage("Clearing fault codes...");
            clearDialog.setIndeterminate(true);
            clearDialog.setCancelable(false);
            clearDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            if (mBluetoothAdapter != null && bluetoothSocket != null) {
                try {
                    clearDTC.run(bluetoothSocket.getInputStream(),bluetoothSocket.getOutputStream());

                } catch (NoDataException n) {
                    Log.e("EXCEPTION", "NO FAULT CODES TO CLEAR");
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ERROR", "COULD NOT CLEAR FAULT CODES");
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            if (flag) {
                clearDialog.dismiss();
                Toast.makeText(FaultCodeActivity.this, "Fault codes cleared!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                clearDialog.dismiss();
                final AlertDialog.Builder dialog = new AlertDialog.Builder(FaultCodeActivity.this);
                dialog.setTitle("Error");
                dialog.setMessage("Could not clear fault codes.");
                dialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        }
    }
}
