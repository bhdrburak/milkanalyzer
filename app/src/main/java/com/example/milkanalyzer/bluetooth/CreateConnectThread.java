package com.example.milkanalyzer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


import com.example.milkanalyzer.AppManager;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class CreateConnectThread extends Thread {


    public static BluetoothSocket mmSocket;
    public Handler handler;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;
    public static ConnectedThread connectedThread;

    public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, Handler handler) {
        this.handler = handler;
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

        connectedThread = new ConnectedThread(mmSocket, handler);
        AppManager.setConnectedThread(connectedThread);
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

