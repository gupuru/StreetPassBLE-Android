package gupuru.streetpass.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import gupuru.streetpass.R;
import gupuru.streetpass.bean.BleData;
import gupuru.streetpassble.StreetPassBle;

public class BleRecyclerAdapter extends RecyclerView.Adapter<BleRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BleData> bleDataArrayList;
    private boolean isOpenServer = false;
    private StreetPassBle streetPassBle;

    public BleRecyclerAdapter(Context context, StreetPassBle streetPassBle, ArrayList<BleData> bleDataArrayList) {
        this.context = context;
        this.bleDataArrayList = bleDataArrayList;
        this.streetPassBle = streetPassBle;
    }

    public void setbleDataArrayList(ArrayList<BleData> bleDataArrayList) {
        this.bleDataArrayList = bleDataArrayList;
    }

    public void setIsOpenServer(boolean isOpenServer) {
        this.isOpenServer = isOpenServer;
    }

    public void clear() {
        bleDataArrayList.clear();
    }

    @Override
    public int getItemCount() {
        if (bleDataArrayList == null || bleDataArrayList.isEmpty()) {
            return 0;
        }
        return bleDataArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view_ble, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        if (!bleDataArrayList.isEmpty()) {
            viewHolder.deviceName.setText(bleDataArrayList.get(position).getDeviceName());
            viewHolder.deviceAddress.setText(bleDataArrayList.get(position).getDeviceAddress());
            viewHolder.serviceData.setText(bleDataArrayList.get(position).getServiceData());
            viewHolder.time.setText(bleDataArrayList.get(position).getTime());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName;
        private TextView deviceAddress;
        private TextView serviceData;
        private TextView time;

        public ViewHolder(View v) {
            super(v);
            deviceName = (TextView) v.findViewById(R.id.device_name);
            deviceAddress = (TextView) v.findViewById(R.id.device_address);
            serviceData = (TextView) v.findViewById(R.id.service_data);
            time = (TextView) v.findViewById(R.id.time);
        }
    }

}