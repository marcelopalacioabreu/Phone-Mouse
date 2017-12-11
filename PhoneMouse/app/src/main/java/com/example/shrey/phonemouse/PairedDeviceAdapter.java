package com.example.shrey.phonemouse;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by dino on 11/19/17.
 */

public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> mPairedDevices;
    private Context mContext;

    public PairedDeviceAdapter(Context context, Set<BluetoothDevice> pairedDevices) {
        mContext = context;
        this.mPairedDevices = new ArrayList<BluetoothDevice>(pairedDevices);
    }


    @Override
    public PairedDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PairedDeviceAdapter.ViewHolder holder, int position) {
        final BluetoothDevice device = mPairedDevices.get(position);
        holder.mDeviceNameText.setText(device.getName());
        holder.mDeviceAddressText.setText(device.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mouseIntent = new Intent(mContext, MouseActivity.class);
                mouseIntent.putExtra("BLUETOOTH_DEVICE", device);
                mContext.startActivity(mouseIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPairedDevices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDeviceNameText;
        public TextView mDeviceAddressText;

        public ViewHolder(View itemView) {
            super(itemView);
            mDeviceNameText = (TextView) itemView.findViewById(R.id.device_name);
            mDeviceAddressText = (TextView) itemView.findViewById(R.id.device_address);
        }
    }
}
