package com.example.pulsesensorysock.connect;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.pulsesensorysock.Connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;

    private volatile boolean stopWorker;
    private int readBufferPosition;
    private byte[] readBuffer;
    private Thread workerThread;

    @Override
    public  void onCreate(){
        super.onCreate();
    }

    IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BluetoothService getServerInstance() {
            return BluetoothService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("onStartCommand", "starting bluetooth service");
        String deviceAddress = intent.getStringExtra("device_address");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        connectToDevice();

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice() {
        // Connect to the Bluetooth device here
        try {
            if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                Connect.notifyBtStatus("Not Connected");
                closeSocket();
            }
            // Create a BluetoothSocket
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

            // Connect to the remote device
            bluetoothSocket.connect();

            Connect.notifyBtStatus("Connected to " + bluetoothDevice.getName());

            mmOutputStream = bluetoothSocket.getOutputStream();
            mmInputStream = bluetoothSocket.getInputStream();
            beginListenForData();

        } catch (IOException e) {
            e.printStackTrace();
            stopWorker = true;
            startBluetoothService();
        } catch (Throwable th) {
            stopWorker = true;
            th.printStackTrace();
            startBluetoothService();
        }
    }

    private void beginListenForData() {
        try {
            final android.os.Handler handler = new android.os.Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        if (!bluetoothAdapter.isEnabled()) {
                            stopWorker = true;
                            closeSocket();
                            Log.i("Bluetooth Socket", "Bluetooth socket was closed. Process stops.");
                            break;
                        }

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                                                Log.i("From device", "" + data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                            ex.printStackTrace();
                            startBluetoothService();
                        } catch (Throwable th) {
                            stopWorker = true;
                            th.printStackTrace();
                            startBluetoothService();
                        }
                    }
                }
            });
            workerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startBluetoothService() {
        Connect.notifyBtStatus("Not Connected");
        Log.i("startBluetoothService", "Attempting to start bluetooth service.");
        Intent serviceIntent = new Intent(getApplicationContext(), BluetoothService.class);
        serviceIntent.putExtra("device_address", bluetoothDevice.getAddress());
        startService(serviceIntent);
        Log.i("startBluetoothService", "Bluetooth service started.");
    }

    public static boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        closeSocket();
    }

    private void closeSocket() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                stopWorker = true;
                mmOutputStream.close();
                mmInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
