package com.example.milkanalyzer.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {


    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public Handler handler;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;


    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        this.handler = handler;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

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

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}