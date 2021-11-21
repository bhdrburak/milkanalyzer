package com.example.milkanalyzer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.example.milkanalyzer.bluetooth.ConnectedThread;
import com.example.milkanalyzer.object.DeviceInfoModel;
import com.example.milkanalyzer.object.User;
import com.example.milkanalyzer.printer.CreatePrinter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class AppManager {

    public static User user;
    public static String userId;
    public static ConnectedThread connectedThread;
    public static Handler handler;
    public static CreatePrinter createPrinter;
    public static BroadcastReceiver usbReceiver;

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        AppManager.user = user;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        AppManager.userId = userId;
    }

    public static ConnectedThread getConnectedThread() {
        return connectedThread;
    }

    public static void setConnectedThread(ConnectedThread connectedThread) {
        AppManager.connectedThread = connectedThread;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        AppManager.handler = handler;
    }

    public static CreatePrinter getCreatePrinter() {
        return createPrinter;
    }

    public static void setCreatePrinter(CreatePrinter createPrinter) {
        AppManager.createPrinter = createPrinter;
    }

    public static void initPrinter(Context context, BroadcastReceiver usbReceiver){
        AppManager.usbReceiver = usbReceiver;
        createPrinter = new CreatePrinter(usbReceiver);
        createPrinter.printUsb(context);
    }

    public static List<DeviceInfoModel> getBluetoothConnection(View view){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<DeviceInfoModel> deviceList = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName,deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
            }
            return deviceList;
        } else {
            Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", view1 -> { });
            snackbar.show();
            return null;
        }
    }

}
