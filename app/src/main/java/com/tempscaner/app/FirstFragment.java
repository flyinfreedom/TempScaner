package com.tempscaner.app;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;

public class FirstFragment extends Fragment {
    private View view;

    private String vid = "2341";
    private String pid = "8036";

    class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }

    private ArrayList<ListItem> listItems = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        view = inflater.inflate(R.layout.fragment_first, container, false);

        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 螢幕寬度（畫素）

        TextView textview = view.findViewById(R.id.textView);
        textview.setTextSize(width < 1100 ? 12 : 16);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Check();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    void Check() {
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        listItems.clear();
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if (driver != null) {
                for (int port = 0; port < driver.getPorts().size(); port++)
                    listItems.add(new ListItem(device, port, driver));
            } else {
                listItems.add(new ListItem(device, 0, null));
            }
        }

        ListItem tItem = GetOurDeviceItem();
        if (tItem != null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.SetHasDevice(true);

            activity.SetDeviceId(tItem.device.getDeviceId());
            activity.SetPort(tItem.port);

            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        } else {
            if (listItems.size() > 0) {
                new AlertDialog.Builder(getActivity())
                        .setMessage("The IR temperature sensor cannot be initialized, please insert it properly.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    public ListItem GetOurDeviceItem() {
        for (ListItem c : listItems) {
            if (Integer.toHexString(c.device.getVendorId()).equals(vid)
                    && Integer.toHexString(c.device.getProductId()).equals(pid)) {
                return c;
            }
        }
        return null;
    }
}