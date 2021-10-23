package com.example.milkanalyzer.activity;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.example.milkanalyzer.FireBaseHelper;
import com.example.milkanalyzer.R;
import com.example.milkanalyzer.databinding.ActivityMainBinding;
import com.example.milkanalyzer.object.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private List<User> userList = new ArrayList<>();
    private ActivityMainBinding mBinding;

    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;

    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBinding.progressBar.setVisibility(View.GONE);
        mBinding.buttonToggle.setEnabled(false);

        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            mBinding.toolbar.setSubtitle("Connecting to " + deviceName + "...");
            mBinding.progressBar.setVisibility(View.VISIBLE);
            mBinding.buttonConnect.setEnabled(false);

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, mBinding);
            createConnectThread.start();
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                mBinding.toolbar.setSubtitle("Connected to " + deviceName);
                                mBinding.progressBar.setVisibility(View.GONE);
                                mBinding.buttonConnect.setEnabled(true);
                                mBinding.buttonToggle.setEnabled(true);
                                break;
                            case -1:
                                mBinding.toolbar.setSubtitle("Device fails to connect");
                                mBinding.progressBar.setVisibility(View.GONE);
                                mBinding.buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        switch (arduinoMsg.toLowerCase()) {
                            case "led is turned on":
                                mBinding.textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                            case "led is turned off":
                                mBinding.textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                        }
                        break;
                }
            }
        };

        mBinding.buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        mBinding.buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmdText = null;
                cmdText = "start\n";
                // Send command to Arduino board
                connectedThread.write(cmdText);
            }
        });

        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, NewUserActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        mBinding.print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*EscPosPrinter printer = null;
                try {
                    printer = new EscPosPrinter(new UsbConnection(mUsbManager, mUsbDevice), 203, 48f, 32);
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }
                try {
                    printer
                            .printFormattedText("Bahadır Burak Karaoğlu - Milk Analyzer"
                            );
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                } catch (EscPosParserException e) {
                    e.printStackTrace();
                } catch (EscPosEncodingException e) {
                    e.printStackTrace();
                } catch (EscPosBarcodeException e) {
                    e.printStackTrace();
                }*/
            }
        });
        printUsb();
    }


    public static class CreateConnectThread extends Thread {


        ActivityMainBinding mActivityMainBinding;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, ActivityMainBinding activityMainBinding) {

            mActivityMainBinding = activityMainBinding;
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            connectedThread = new ConnectedThread(mmSocket, mActivityMainBinding);
            connectedThread.run();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private ActivityMainBinding mActivityMainBinding;
        private List<User> userList = new ArrayList<>();

        public ConnectedThread(BluetoothSocket socket, ActivityMainBinding activityMainBinding) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mActivityMainBinding = activityMainBinding;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            String readMessage;
            String search = "\n";
            while (true) {
                try {
                    buffer[bytes] = (byte) mmInStream.read();
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                        buffer = new byte[1024];
                        Log.d("Arduino Message", readMessage);
                        String[] stringArray = readMessage.split(":");
                        if ("rfid".equals(stringArray[0])) {
                            //initUser(stringArray[1]);
                        } else if ("avarage".equals(stringArray[0])) {
                            mActivityMainBinding.textViewInfo.setText(stringArray[1]);
                        }
                    } else {
                        bytes++;
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }



        private void initText(String id) {
            for (int i = 0; i < userList.size(); i++) {
                if (id.equals(userList.get(i).getId())) {
                    mActivityMainBinding.textViewId.setText(id);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy");
                    String dateString = sdf.format(new Date());
                    mActivityMainBinding.textViewDate.setText(dateString);
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }


    private void saveDataBase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            // YOUR PRINT CODE HERE
                            mUsbManager = usbManager;
                            mUsbDevice = usbDevice;
                            Toast.makeText(MainActivity.this, "Printer Bağlandı.", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }
        }
    };

    public void printUsb() {
        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        if (usbConnection != null && usbManager != null) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(MainActivity.ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(MainActivity.ACTION_USB_PERMISSION);
            registerReceiver(this.usbReceiver, filter);
            usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
        }
    }

}











