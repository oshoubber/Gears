package me.oshoubber.gears;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.ResponseException;
import com.github.pires.obd.exceptions.StoppedException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

public class LiveDataPressureActivity extends Fragment {

    BluetoothSocket bluetoothSocket = LiveInformationActivity.bluetoothSocket;

    TextView aView;
    TextView bView;

    IntakeManifoldPressureCommand a = new IntakeManifoldPressureCommand();
    BarometricPressureCommand b = new BarometricPressureCommand();
    Thread thread1;
    Thread thread2;

    public View rootView;

    public LiveDataPressureActivity() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pressure, container,false);

        aView = (TextView) rootView.findViewById(R.id.impressure);
        bView = (TextView) rootView.findViewById(R.id.bpressure);

        getIntakeManifoldPressure();
        getBarometricPressure();

        return rootView;
    }


    public void getIntakeManifoldPressure() {
        thread1 = new Thread(new Runnable() {
            public void run() {
                Log.d("TEMPS", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        a.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        a.useImperialUnits(true);

                        Log.d("TEMPS", a.getFormattedResult());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aView.setText(a.getFormattedResult());
                            }
                        });
                    }
                } catch (UnableToConnectException ute) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                            dialog.setTitle("Error");
                            dialog.setMessage("Unable to connect. Try reconnecting your adapter and try again");
                            dialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    });
                } catch (NoDataException nd) {
                    Log.e("ERROR","NO DATA EXCEPTION");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aView.setText("N/A");
                        }
                    });
                } catch (UnsupportedCommandException unse) {
                    Log.e("ERROR", "UNSUPPORTED COMMAND");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aView.setText("N/A");
                        }
                    });
                } catch (StoppedException se) {
                    Log.e("ERROR", "STOPPED EXCEPTION");
                } catch (ResponseException re) {
                    Log.e(re.getMessage(), "RESPONSE EXCEPTION");
                    thread1.start();
                    getIntakeManifoldPressure();
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }
            }
        });
        thread1.start();
    }
    public void getBarometricPressure() {
        thread2 = new Thread(new Runnable() {
            public void run() {
                Log.d("TEMPS", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        b.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        b.useImperialUnits(true);

                        Log.d("TEMPS", b.getFormattedResult());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bView.setText(b.getFormattedResult());
                            }
                        });
                    }
                } catch (UnableToConnectException ute) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                            dialog.setTitle("Error");
                            dialog.setMessage("Unable to connect. Try reconnecting your adapter and try again");
                            dialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    });
                } catch (NoDataException nd) {
                    Log.e("ERROR","NO DATA EXCEPTION");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bView.setText("N/A");
                        }
                    });
                } catch (UnsupportedCommandException unse) {
                    Log.e("ERROR", "UNSUPPORTED COMMAND");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bView.setText("N/A");
                        }
                    });
                } catch (StoppedException se) {
                    Log.e("ERROR", "STOPPED EXCEPTION");
                } catch (ResponseException re) {
                    Log.e(re.getMessage(), "RESPONSE EXCEPTION");
                    thread1.interrupt();
                    getBarometricPressure();
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }

            }
        });
        thread2.start();
    }
}
