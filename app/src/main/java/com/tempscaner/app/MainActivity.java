package com.tempscaner.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

import android.util.DisplayMetrics;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAGIN = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String TAGOUT = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private static Boolean HasDevice = false;

    private static int deviceId = 1002;
    private static int port = 0;
    private static int baudRate = 9600;

    private String vid = "2341";
    private String pid = "8036";

    private int pageNumber;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TAGIN);
        filter.addAction(TAGOUT);
        this.registerReceiver(mUsbReceiver, filter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }


    public void SetHasDevice(Boolean tuneOn) {
        HasDevice = tuneOn;
    }

    public Boolean GetHasDevice() {
        return HasDevice;
    }

    public void SetDeviceId(int id) {
        deviceId = id;
    }

    public int GetDeviceId() {
        return deviceId;
    }

    public void SetPort(int portNum) {
        port = portNum;
    }

    public int GetPort() {
        return port;
    }

    public void SetBaudRate(int baud) {
        baudRate = baud;
    }

    public int GetBaudRate() {
        return baudRate;
    }

    public void SetPageNumber(int page) {
        pageNumber = page;
    }

    public int GetPageNumber() {
        return pageNumber;
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int duration = Toast.LENGTH_SHORT;
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    HasDevice = false;
                    context.unregisterReceiver(mUsbReceiver);
                    RestartActivity();
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Check();
            }
        }
    };

    public void RestartActivity() {
        Intent restartIntent = getIntent();
        finish();
        startActivity(restartIntent);
    }

    public boolean Check() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
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
        if (tItem != null) { //TODO For Test
            SetHasDevice(true);
            SetDeviceId(tItem.device.getDeviceId());
            SetPort(tItem.port);
            return true;
        } else {
            if (listItems.size() > 0) {
                new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setMessage("The IR temperature sensor cannot be initialized, please insert it properly.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
        return false;
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

    @Override
    public void onBackPressed() {
        int pn = GetPageNumber();
        if (pn <= 2) {
            System.exit(0);
        } else if (pn == 3 || pn == 4) {
            RestartActivity();
        } else {
            super.onBackPressed();
        }
    }

}