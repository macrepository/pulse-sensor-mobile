package com.example.pulsesensorysock.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.pulsesensorysock.R;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mListener;
    private String deviceConnected;
    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data, String d) {
        mData = data;
        deviceConnected = d;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("MissingPermission")
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Intent intent = new Intent();
        String action = intent.getAction();
        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.list_item_device, null);

            holder 				= new ViewHolder();

            holder.deviceName		= convertView.findViewById(R.id.device_name);
            holder.deviceAddress 	= convertView.findViewById(R.id.device_address);
            holder.pairBtn		= convertView.findViewById(R.id.btn_pair);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device	= mData.get(position);
        try {
            holder.deviceName.setText((device.getName().equals("HC-05")) ? "POWLET" : device.getName());
            holder.deviceAddress.setText(device.getAddress());
            holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");
            holder.pairBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onPairButtonClick(position);
                    }
                }
            });
        }catch (Exception e){}
        return convertView;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView pairBtn;
    }

    public interface OnPairButtonClickListener {
        void onPairButtonClick(int position);
    }
}
