package com.example.pulsesensorysock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.example.pulsesensorysock.adapter.DeviceListAdapter;
import com.example.pulsesensorysock.connect.BluetoothService;
import com.example.pulsesensorysock.connect.BluetoothService.LocalBinder;
import com.example.pulsesensorysock.helper.Helper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class Connect extends AppCompatActivity {

    // UI Controls
    private ScrollView scrollView;
    private Switch btSwitch;
    private static TextView btStatus;
    private TextView txtPaired;
    private ListView lvPaired;
    private Button btnScan;
    private TextView txtAvailable;
    private ListView lvAvailable;

    //Bluetooth
    private BluetoothAdapter bluetoothAdapter;

    //BT paired devices
    private ArrayList<BluetoothDevice> pairedDeviceList;
    private DeviceListAdapter pairedDeviceAdapter;

    //BT available devices
    private ArrayList<BluetoothDevice> availableDeviceList = new ArrayList<BluetoothDevice>();
    private DeviceListAdapter availableDeviceAdapter;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    //Connect
    boolean mBounded;
    static BluetoothService mServer;

    // Progress Dialog
    private ProgressDialog mProgressDlg;
    private ProgressDialog progressDialog;

    //Custom Data
    BluetoothDevice myDevice;
    private int pos = 0;

    private ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == -1) {
                        // Allow
                        doBluetoothTask();
                    } else if(result.getResultCode() == 0) {
                        // Deny
                        setBluetoothDisabled();
                    }
                }
            });

    //Helper
    Helper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        helper = new Helper(getApplicationContext());

        //UI controls
        scrollView = findViewById(R.id.bt_scroll_view);
        btSwitch = findViewById(R.id.bt_switch);
        btStatus = findViewById(R.id.bt_status);
        txtPaired = findViewById(R.id.txt_paired);
        lvPaired = findViewById(R.id.lv_paired);
        btnScan = findViewById(R.id.btn_scan);
        txtAvailable = findViewById(R.id.txt_available);
        lvAvailable = findViewById(R.id.lv_available);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Initialize other tools
        initializeProgressDialog();
        initializeProgressDialogScan();
        initializePairedAdapter();
        initializeAvailableAdapter();

        if (bluetoothAdapter == null) {
            btStatus.setText("Bluetooth is unsupported by this device.");
        } else {
            btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                    if (isCheck) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        activityResultLaunch.launch(enableBtIntent);
                    } else {
                        setBluetoothDisabled();
                    }
                }
            });

            btnScan.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onClick(View view) {
                    if(!bluetoothAdapter.startDiscovery()) {
                        helper.showToast("Please turn on device location to use the scan device feature.");
                        requestOtherBluetoothPermission();
                    }

                    listen();
                }
            });

            doBluetoothTask();
        }
    }

    private void initializePairedAdapter() {
        lvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showAlertDialog(position);
            }
        });

        pairedDeviceAdapter	= new DeviceListAdapter(this);
        pairedDeviceAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onPairButtonClick(int position) {
                BluetoothDevice device = pairedDeviceList.get(position);
                onPairButtonClickProcess(device, position);
            }
        });

        lvPaired.setAdapter(pairedDeviceAdapter);
    }

    @SuppressLint("MissingPermission")
    public void showAlertDialog(final int position){
        BluetoothDevice device = pairedDeviceList.get(position);
        String title = device.getName();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(title);
        builder.setCancelable(true);

        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BluetoothDevice device = pairedDeviceList.get(position);
                myDevice = device;
                startBluetoothService();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void startBluetoothService()
    {
        progressDialog.setTitle("Connecting...");
        progressDialog.show();

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(1000);
                    Log.i("Process", "trying to connect");
                    Intent serviceIntent = new Intent(getApplicationContext(), BluetoothService.class);
                    serviceIntent.putExtra("device_address", myDevice.getAddress());
                    startService(serviceIntent);
                    scrollViewFocusUp();
                    sleep(500);
                } catch (Exception e) {

                } catch (Throwable t) {

                } finally {
                    if (!BluetoothService.isConnected(myDevice)) {
                        startBluetoothService();
                    } else {
                        progressDialog.dismiss();
                    }
                }
            }
        };
        welcomeThread.start();
    }


    private void initializeAvailableAdapter() {
        availableDeviceAdapter = new DeviceListAdapter(this);

        availableDeviceAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position) {
                BluetoothDevice device = availableDeviceList.get(position);
                onPairButtonClickProcess(device, position);

            }
        });

        lvAvailable.setAdapter(availableDeviceAdapter);
    }

    @SuppressLint("MissingPermission")
    private void onPairButtonClickProcess(BluetoothDevice device, int position) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            unpairDevice(device, position);
        } else {
            //if(device.getName().equals("HC-05")){
                helper.showToast("Pairing...");
                myDevice = device;
                pos = position;
                pairDevice(device);
            //}
            //else  helper.showToast("You cannot pair this device because it does not belong to this system.");
        }
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void unpairDevice(BluetoothDevice device, int position) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            pairedDeviceList.remove(position);
            pairedDeviceAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(lvPaired);

            if(!BluetoothService.isConnected(myDevice)) {
                Log.i("Unpair", "Not connected");
                btStatus.setText("Not Connected");
            } else {
                Log.i("Unpair", "Still connected");
            }

//            bluetoothAdapter.startDiscovery();
//            listen();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
    }

    private void initializeProgressDialogScan() {
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning devices...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                bluetoothAdapter.cancelDiscovery();
            }
        });
    }

    public void requestOtherBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // If the app does not have the necessary permissions, request them
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void listen() {
        Log.i("Click", "Started listening");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(availableReceiver, filter);
        registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @SuppressLint("MissingPermission")
    private void doBluetoothTask() {
        if (bluetoothAdapter.isEnabled()) {
            setBluetoothEnabled();
            showPairedDevices();
        } else {
            setBluetoothDisabled();
        }
    }

    private void setBluetoothEnabled() {
        //UI controls
        btSwitch.setText("Disable");
        btSwitch.setChecked(true);
        btStatus.setVisibility(View.VISIBLE);
        txtPaired.setVisibility(View.VISIBLE);
        btnScan.setVisibility(View.VISIBLE);

        if (pairedDeviceList == null) {
            txtAvailable.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("MissingPermission")
    private void setBluetoothDisabled() {
        //Disable bluetooth
        bluetoothAdapter.disable();

        //UI controls
        btSwitch.setText("Enable");
        btSwitch.setChecked(false);
        btStatus.setText("Not Connected");
        btStatus.setVisibility(View.INVISIBLE);

        // Paired BT
        txtPaired.setVisibility(View.INVISIBLE);
        if (pairedDeviceList != null && !pairedDeviceList.isEmpty()) pairedDeviceList.clear();

        pairedDeviceAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(lvPaired);

        //Available BT
        btnScan.setVisibility(View.INVISIBLE);
        txtAvailable.setVisibility(View.INVISIBLE);
        if (availableDeviceList != null && !availableDeviceList.isEmpty()) availableDeviceList.clear();

        availableDeviceAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(lvAvailable);
    }

    @SuppressLint("MissingPermission")
    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices == null || pairedDevices.size() == 0) {
            helper.showToast("No Paired Devices Found");
        } else {
            pairedDeviceList = new ArrayList<BluetoothDevice>();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    pairedDeviceList.add(device);

                    // Display status if has connection
                    if (BluetoothService.isConnected(device)) {
                        btStatus.setText("Connected to " + device.getName());
                    }
                }
            }

            pairedDeviceAdapter.setData(pairedDeviceList, "");
            pairedDeviceAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(lvPaired);
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) view.setLayoutParams(new
                    ViewGroup.LayoutParams(desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight + (listView.getDividerHeight() *
                (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    // Create a BroadcastReceiver.
    private final BroadcastReceiver availableReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Click", "available receive");
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.i("Click", "ACTION_STATE_CHANGED");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    bluetoothAdapter.isEnabled();
                    doBluetoothTask();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("Click", "ACTION_DISCOVERY_STARTED");
                availableDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();
                Log.i("Click", "ACTION_DISCOVERY_FINISHED");
                availableDeviceAdapter.setData(availableDeviceList, "");
                availableDeviceAdapter.notifyDataSetChanged();

                if(availableDeviceList != null && availableDeviceList.size() > 0)  txtAvailable.setVisibility(View.VISIBLE);
                else txtAvailable.setVisibility(View.INVISIBLE);

                setListViewHeightBasedOnChildren(lvAvailable);

                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i("Click", "ACTION_FOUND");
                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("Click", "" + device.getName());

                // Do not add to list of available devices, If it is found in the list of paired devices
                if (!pairedDeviceList.contains(device)) {
                    availableDeviceList.add(device);
                }
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    try {
                        Log.i("Bonded", "Pairing processes here...");

                        // Remove in the list of available devices
                        availableDeviceList.remove(pos);

                        if(availableDeviceList != null && availableDeviceList.size() > 0)  txtAvailable.setVisibility(View.VISIBLE);
                        else txtAvailable.setVisibility(View.INVISIBLE);
                        setListViewHeightBasedOnChildren(lvAvailable);

                        // Add in the list of paired devices
                        pairedDeviceList.add(0, myDevice);
                        pairedDeviceAdapter.setData(pairedDeviceList, myDevice.getName());
                        pairedDeviceAdapter.notifyDataSetChanged();
                        setListViewHeightBasedOnChildren(lvPaired);

                        startBluetoothService();
                        scrollViewFocusUp();
                        helper.showToast("Paired");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    helper.showToast("Unpaired");
                    btStatus.setText("Not Connected");
                }

                pairedDeviceAdapter.notifyDataSetChanged();
            }
        }
    };

    private void scrollViewFocusUp() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    // Override the onRequestPermissionsResult() method to handle the result of the permission request
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start Bluetooth discovery
                    boolean started = bluetoothAdapter.startDiscovery();
                    if (started) {
                        Log.i("Click", "Started discovery11");
                        bluetoothAdapter.startDiscovery();
                        listen();
                    } else {
                        // Discovery did not start, handle error
                        Log.i("Click", "Discovery not started22");
                    }
                } else {
                    // Permission denied, handle error
                    Log.i("Click", "Permission denied333");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    // ============================Bluetooth Service========================================
    public static void notifyBtStatus(String msg) {
        btStatus.setText(msg);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent mIntent = new Intent(this, BluetoothService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //  Toast.makeText(Connect.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mBounded = false;
            mServer = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(Connect.this, "Service is connected", Toast.LENGTH_SHORT).show();
            mBounded = true;
            LocalBinder mLocalBinder = (LocalBinder)service;
            mServer = mLocalBinder.getServerInstance();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }
}
