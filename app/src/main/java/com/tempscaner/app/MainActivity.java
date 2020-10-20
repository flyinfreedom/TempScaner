package com.tempscaner.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAGOUT = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private static Boolean HasDevice = false;

    private static int deviceId=1002;
    private static int port=0;
    private static int baudRate = 9600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TAGOUT);
        this.registerReceiver(mUsbReceiver, filter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void SetHasDevice(Boolean tuneOn)
    {
        HasDevice = tuneOn;
    }
    public Boolean GetHasDevice()
    {
        return HasDevice;
    }

    public void SetDeviceId(int id)
    {
        deviceId = id;
    }
    public int GetDeviceId()
    {
        return deviceId;
    }

    public void SetPort(int portNum)
    {
        port = portNum;
    }
    public int GetPort()
    {
        return port;
    }

    public void SetBaudRate(int baud)
    {
        baudRate = baud;
    }
    public int GetBaudRate()
    {
        return baudRate;
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
                    Intent restartIntent = getIntent();
                    finish();
                    startActivity(restartIntent);
                }
            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                Toast toast = Toast.makeText(context, "裝置接入",  duration);
                toast.show();
            }
        }
    };
}