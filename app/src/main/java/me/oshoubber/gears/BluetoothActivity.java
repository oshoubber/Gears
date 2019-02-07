package me.oshoubber.gears;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class BluetoothActivity extends Activity{

    public static BluetoothSocket bluetoothSocket;
    private int position;
    public static String deviceAddress;
    private ProgressDialog progressDialog;
    public final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        if (bluetoothSocket == null) {
            checkForBluetooth();
        } else {
            new ConnectBluetooth().execute(deviceAddress);
        }
    }

    public void checkForBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null) {
            Toast.makeText(BluetoothActivity.this, "This device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            Toast.makeText(BluetoothActivity.this, "Please enable Bluetooth and make sure you have paired your device.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(BluetoothActivity.this, "Bluetooth is enabled!", Toast.LENGTH_SHORT).show();
            displayDevices();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    displayDevices();
                    break;
                }
            default:
                break;
        }
    }

    private void displayDevices() {
        final ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(BluetoothActivity.this);
        ArrayAdapter adapter = new ArrayAdapter(BluetoothActivity.this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                ConnectBluetooth connectBluetooth = new ConnectBluetooth();
                deviceAddress = devices.get(position);
                connectBluetooth.execute(devices.get(position));
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        alertDialog.setTitle("Choose Paired Bluetooth OBD2 Device");
        alertDialog.show();
    }

    private class ConnectBluetooth extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(BluetoothActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Connecting to your Bluetooth device...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected Boolean doInBackground(String... addresses) {

            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = btAdapter.getRemoteDevice(addresses[0]);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
                bluetoothSocket = socket;
                Log.v("Device", "Connected: " + uuid);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("connectionError", "CONNECTION ERROR");
            }
            return false;
        }

        protected void onPostExecute(Boolean ans) {
            if (ans) {
                progressDialog.dismiss();
                setIntentForCallingActivity();
                finish();
            } else {
                progressDialog.dismiss();
                errorDialog();
            }
        }
    }

    public void setIntentForCallingActivity() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK,resultIntent);
    }

    private void errorDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(BluetoothActivity.this);
        dialog.setTitle("Communication Error");
        dialog.setMessage("A connection could not be established between your phone and your OBD2 Bluetooth Adapter.");

        dialog.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                displayDevices();
            }
        });
        dialog.setNegativeButton("Help", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                final AlertDialog.Builder helpDialog = new AlertDialog.Builder(BluetoothActivity.this);
                helpDialog.setTitle("Troubleshooting");
                helpDialog.setMessage(R.string.helpDialog);

                helpDialog.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface helpDialog, int id) {
                        displayDevices();
                    }
                });

                helpDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface helpDialog, int id) {
                        helpDialog.dismiss();
                        finish();
                    }
                });
                helpDialog.setCancelable(false);
                helpDialog.show();
            }
        });
        dialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

}