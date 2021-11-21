package com.example.milkanalyzer.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationManager;
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
import com.example.milkanalyzer.EventAdapter;
import com.example.milkanalyzer.bluetooth.ConnectedThread;
import com.example.milkanalyzer.bluetooth.CreateConnectThread;
import com.example.milkanalyzer.databinding.ActivityMainBinding;
import com.example.milkanalyzer.firebase.FireBaseHelper;
import com.example.milkanalyzer.R;
import com.example.milkanalyzer.firebase.FireBaseMilkHelper;
import com.example.milkanalyzer.object.DeviceInfoModel;
import com.example.milkanalyzer.object.Login;
import com.example.milkanalyzer.object.TakenMilk;
import com.example.milkanalyzer.object.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EventAdapter.onItemClickListener {

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
    private List<TakenMilk> takenMilkList = new ArrayList<>();
    private EventAdapter eventAdapter;
    private LocationManager mLocationManager;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.milkanalyzer.databinding.ActivityMainBinding mActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Intent intentLogin = getIntent();
        username = intentLogin.getStringExtra("username");
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
            for (DeviceInfoModel infoModel : deviceList) {
                if (infoModel.getDeviceName().equals(deviceName)) {
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
            if (!isBluetoothConnection) {
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        mBinding.fab.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, NewUserActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        eventAdapter = new EventAdapter(MainActivity.this, takenMilkList, MainActivity.this);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        mBinding.recyclerEventList.setAdapter(eventAdapter);
        mBinding.recyclerEventList.setLayoutManager(layoutManager);
        getEventData();


    }

    private Location getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ;
        }
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        else
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return location;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Location location = getLocation();
        FirebaseLocationHelper firebaseLocationHelper = new FirebaseLocationHelper();
        com.example.milkanalyzer.object.Location locationObject = new com.example.milkanalyzer.object.Location();
        locationObject.setLatitude(String.valueOf(location.getLatitude()));
        locationObject.setLongitude(String.valueOf(location.getLongitude()));
        firebaseLocationHelper.addLocation(username, locationObject);*/
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

    private void getEventData() {
        FireBaseMilkHelper fireBaseMilkHelper = new FireBaseMilkHelper();
        fireBaseMilkHelper.databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG", "onCancelled: ");
                takenMilkList.clear();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    takenMilkList.add(child.getValue(TakenMilk.class));
                }
                eventAdapter.setTakenMilkList(takenMilkList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "onCancelled: ");
            }
        });
    }


    private void getData(String[] strings) {
        if ("rfid".equals(strings[0])) {
            mUserId = strings[1];
            AppManager.setUserId(mUserId);
            Intent myIntent = new Intent(MainActivity.this, ResultActivity.class);
            myIntent.putExtra("userId", "");
            MainActivity.this.startActivity(myIntent);
        } else if ("avarage".equals(strings[0])) {
            Intent myIntent = new Intent(MainActivity.this, ResultActivity.class);
            myIntent.putExtra("userId", mUserId);
            MainActivity.this.startActivity(myIntent);
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
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

    @Override
    public void onSelected() {

    }

    class FirebaseLocationHelper {

        private DatabaseReference databaseReference;


        public FirebaseLocationHelper(){
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            databaseReference = db.getReference(Location.class.getSimpleName());
        }

        public Task<Void> addLocation(String key, com.example.milkanalyzer.object.Location location){
            return databaseReference.child(key).setValue(location);
        }

    }
}













