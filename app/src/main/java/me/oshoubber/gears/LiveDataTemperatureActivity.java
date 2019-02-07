package me.oshoubber.gears;

import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.ResponseException;
import com.github.pires.obd.exceptions.StoppedException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;


public class LiveDataTemperatureActivity extends Fragment {

    BluetoothSocket bluetoothSocket = LiveInformationActivity.bluetoothSocket;

    TextView aView;
    TextView bView;
    TextView cView;

    AirIntakeTemperatureCommand a = new AirIntakeTemperatureCommand();
    AmbientAirTemperatureCommand b = new AmbientAirTemperatureCommand();
    EngineCoolantTemperatureCommand c = new EngineCoolantTemperatureCommand();
    Thread thread1;
    Thread thread2;
    Thread thread3;

    public View rootView;

    public LiveDataTemperatureActivity() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_temperature, container, false);

        aView = (TextView) rootView.findViewById(R.id.aintake);
        bView = (TextView) rootView.findViewById(R.id.ambair);
        cView = (TextView) rootView.findViewById(R.id.coolanttemp);

        Log.e("TEMPS", "PRE CHECK");
        getAirIntakeTemp();
        getAmbAirTemp();
        getEngineCoolantTemp();
        Log.e("TEMPS", "POST CHECK");

        return rootView;
    }

    public void getAirIntakeTemp() {
        thread1 = new Thread(new Runnable() {
            public void run() {
                Log.d("TEMPS", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        a.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

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
                    thread1.interrupt();
                    getAirIntakeTemp();
                    Log.d("REVIVED", "AIR INTAKE TEMP REVIVED");
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }
            }
        });
        thread1.start();
    }
    public void getAmbAirTemp() {
        thread2 = new Thread(new Runnable() {
            public void run() {
                Log.d("TEMPS", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        b.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                        Log.d("TEMPS",b.getFormattedResult());

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
                    getAmbAirTemp();
                    Log.d("REVIVED", "AMBIENT AIR TEMP REVIVED");
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }
            }
        });
        thread2.start();
    }
    public void getEngineCoolantTemp() {
        thread3 = new Thread(new Runnable() {
            public void run() {
                Log.d("TEMPS", "HAS STARTED!");
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        c.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                        Log.d("TEMPS", c.getFormattedResult());

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
                    getEngineCoolantTemp();
                    Log.d("REVIVED", "ENGINE COOLANT TEMP REVIVED");
                } catch (Exception e) {
                    Log.e("STOPPED", "EXCEPTION ERROR");
                }

            }
        });
        thread3.start();
    }
}
