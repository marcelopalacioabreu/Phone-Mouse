package com.example.shrey.phonemouse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConnectionActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private RecyclerView mDevicesRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private PairedDeviceAdapter mPairedDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        mDevicesRecyclerView = (RecyclerView) findViewById(R.id.paired_devices_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mPairedDeviceAdapter = new PairedDeviceAdapter(this,pairedDevices);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mDevicesRecyclerView.getContext(),
                        LinearLayoutManager.VERTICAL);

        mDevicesRecyclerView.addItemDecoration(dividerItemDecoration);
        mDevicesRecyclerView.setAdapter(mPairedDeviceAdapter);
        mDevicesRecyclerView.setLayoutManager(mLinearLayoutManager);
    }
}