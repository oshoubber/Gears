package me.oshoubber.gears;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.enums.AvailableCommandNames;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.ResponseException;
import com.github.pires.obd.exceptions.StoppedException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;
import static me.oshoubber.gears.R.string.ms2;


public class LiveDataDrivingActivity extends Fragment implements SensorEventListener {

    BluetoothSocket bluetoothSocket = LiveInformationActivity.bluetoothSocket;

    TextView aView;
    TextView bView;
    TextView cView;
    TextView accelView;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    RPMCommand a = new RPMCommand();
    SpeedCommand b = new SpeedCommand();
    ThrottlePositionCommand c = new ThrottlePositionCommand();

    Thread thread1;
    Thread thread2;
    Thread thread3;

    public View rootView;

    public LiveDataDrivingActivity() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_driving, container, false);

        aView = (TextView) rootView.findViewById(R.id.rpm);
        bView = (TextView) rootView.findViewById(R.id.speed);
        cView = (TextView) rootView.findViewById(R.id.throttle);
        accelView = (TextView) rootView.findViewById(R.id.accel);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);

        getRPMs();
        getSpeed();
        getThrottle();

        return rootView;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        accelView.setText("0");
        Float acceleration = event.values[0];
        String result = String.format(Locale.US, "%.1f", acceleration);
        accelView.setText(result);
    }


    public void getRPMs() {
        thread1 = new Thread(new Runnable() {
            public void run() {
                Log.d("RPM", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        a.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                        Log.d("RPM", a.getFormattedResult());

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
                    thread1.interrupt();
                    getRPMs();
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }

            }
        });
        thread1.start();
    }

    public void getSpeed() {
        thread2 = new Thread(new Runnable() {
            public void run() {
                Log.d("SPEED", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        b.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        b.useImperialUnits(true);

                        Log.d("SPEED", b.getFormattedResult());

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
                    thread2.interrupt();
                    getSpeed();
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }

            }
        });
        thread2.start();
    }
    public void getThrottle() {
        thread3 = new Thread(new Runnable() {
            public void run() {
                Log.d("THROTTLE", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        c.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                        Log.d("THROTTLE", c.getFormattedResult());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cView.setText(c.getFormattedResult());
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
                            cView.setText("N/A");
                        }
                    });
                } catch (UnsupportedCommandException unse) {
                    Log.e("ERROR", "UNSUPPORTED COMMAND");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cView.setText("N/A");
                        }
                    });
                } catch (StoppedException se) {
                    Log.e("ERROR", "STOPPED EXCEPTION");
                } catch (ResponseException re) {
                    Log.e(re.getMessage(), "RESPONSE EXCEPTION");
                    thread3.interrupt();
                    getThrottle();
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }

            }
        });
        thread3.start();
    }

}