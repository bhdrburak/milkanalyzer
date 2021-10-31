package com.example.milkanalyzer.printer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.example.milkanalyzer.activity.MainActivity;

public class CreatePrinter {



    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private BroadcastReceiver usbReceiver;

    public CreatePrinter(BroadcastReceiver usbReceiver) {
        this.usbReceiver = usbReceiver;
    }

    public BroadcastReceiver getUsbReceiver() {
        return usbReceiver;
    }

    public void setUsbReceiver(BroadcastReceiver usbReceiver) {
        this.usbReceiver = usbReceiver;
    }

    public UsbManager getmUsbManager() {
        return mUsbManager;
    }

    public void setmUsbManager(UsbManager mUsbManager) {
        this.mUsbManager = mUsbManager;
    }

    public UsbDevice getmUsbDevice() {
        return mUsbDevice;
    }

    public void setmUsbDevice(UsbDevice mUsbDevice) {
        this.mUsbDevice = mUsbDevice;
    }

    public EscPosPrinter getPointer(){
        EscPosPrinter printer = null;
        try {
            printer = new EscPosPrinter(new UsbConnection(mUsbManager, mUsbDevice), 203, 48f, 32);
        } catch (EscPosConnectionException e) {
            e.printStackTrace();
        }
        return printer;
    }



    public void printUsb(Context context) {
        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(context);
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbConnection != null && usbManager != null) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            context.registerReceiver(this.usbReceiver, filter);
            usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
        }
    }

}
