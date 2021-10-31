package com.example.milkanalyzer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;

import com.example.milkanalyzer.AppManager;
import com.example.milkanalyzer.databinding.ActivityResultBinding;
import com.example.milkanalyzer.databinding.ActivitySelectDeviceBinding;
import com.example.milkanalyzer.object.DeviceInfoModel;
import com.example.milkanalyzer.bluetooth.DeviceListAdapter;
import com.example.milkanalyzer.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {

    private ActivitySelectDeviceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_device);

        List<DeviceInfoModel> deviceList = AppManager.getBluetoothConnection(binding.getRoot());
        binding.recyclerViewDevice.setLayoutManager(new LinearLayoutManager(this));
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this,deviceList);
        binding.recyclerViewDevice.setAdapter(deviceListAdapter);
        binding.recyclerViewDevice.setItemAnimator(new DefaultItemAnimator());


    }
}