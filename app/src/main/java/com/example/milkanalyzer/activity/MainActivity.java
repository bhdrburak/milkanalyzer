package com.example.milkanalyzer.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.example.milkanalyzer.AppManager;
import com.example.milkanalyzer.bluetooth.ConnectedThread;
import com.example.milkanalyzer.bluetooth.CreateConnectThread;
import com.example.milkanalyzer.databinding.ActivityMainBinding;
import com.example.milkanalyzer.firebase.FireBaseHelper;
import com.example.milkanalyzer.R;
import com.example.milkanalyzer.object.DeviceInfoModel;
import com.example.milkanalyzer.object.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String deviceName = "HC-06";
    private String deviceAddress = "";
    private boolean isBluetoothConnection = false;
    public static Handler handler;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static CreateConnectThread createConnectThread;
    private ActivityMainBinding mBinding;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;
    private String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.milkanalyzer.databinding.ActivityMainBinding mActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        AppManager.initPrinter(this, usbReceiver);
        mBinding.cardViewPrinter.setOnClickListener(view -> {
            AppManager.initPrinter(this, usbReceiver);
        });

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                isBluetoothConnection = true;
                                mBinding.toolbar.setSubtitle("Connected to " + deviceName);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mBinding.bluetoothCon.setBackgroundColor(getColor(R.color.success));
                                }
                                mBinding.imageBl.setImageDrawable(getDrawable(R.drawable.devices_online));
                                mBinding.bluetoothConnectionText.setText("Cihaz Bağlantısı Yapıldı.");
                                mBinding.bluetoothDeviceName.setText(deviceName);
                                mBinding.bluetoothDeviceAddress.setText(deviceAddress);
                                break;
                            case -1:
                                isBluetoothConnection = false;
                                mBinding.toolbar.setSubtitle("Device fails to connect");
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String readMessage = msg.obj.toString(); // Read message from Arduino
                        getData(readMessage.split(":"));
                        break;
                }
            }
        };

        if (deviceName != null) {

            List<DeviceInfoModel> deviceList = AppManager.getBluetoothConnection(mActivityBinding.getRoot());

            deviceAddress = "";
            for (DeviceInfoModel infoModel : deviceList){
                if (infoModel.getDeviceName().equals(deviceName)){
                    deviceAddress = infoModel.getDeviceHardwareAddress();
                    break;
                }
            }
            mBinding.toolbar.setSubtitle("Connecting to " + deviceName + "...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, handler);
            createConnectThread.start();
        }

        mBinding.cardviewBluetooth.setOnClickListener(view -> {
            if (!isBluetoothConnection){
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        mBinding.fab.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, NewUserActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler != null && AppManager.getConnectedThread() != null)
            AppManager.getConnectedThread().setHandler(handler);
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

    public void getData(String[] strings) {
        if ("rfid".equals(strings[0])) {
            mUserId = strings[1];
            FireBaseHelper fireBaseHelper = new FireBaseHelper();
            fireBaseHelper.databaseReference.child(mUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    AppManager.setUser(user);
                    Intent myIntent = new Intent(MainActivity.this, ResultActivity.class);
                    myIntent.putExtra("userId", "");
                    MainActivity.this.startActivity(myIntent);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("TAG", "onCancelled: ");
                }
            });
        } else if ("avarage".equals(strings[0])) {
            Intent myIntent = new Intent(MainActivity.this, ResultActivity.class);
            myIntent.putExtra("userId", mUserId);
            MainActivity.this.startActivity(myIntent);
        }
    }

    private  final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            AppManager.getCreatePrinter().setmUsbManager(usbManager);
                            AppManager.getCreatePrinter().setmUsbDevice(usbDevice);
                            Toast.makeText(context, "Printer Bağlandı.", Toast.LENGTH_LONG).show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mBinding.printerCon.setBackgroundColor(getColor(R.color.success));
                            }
                            mBinding.imagePrinter.setImageDrawable(getDrawable(R.drawable.printer_online));
                            mBinding.printerConnectionText.setText("Printer Bağlantısı Yapıldı.");
                            mBinding.printerReconnect.setText("");

                        }
                    }
                }
            }
        }
    };

}













