package com.tempscaner.app;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.navigation.Navigation;

public class LoadingFragment extends Fragment implements ServiceConnection, SerialListener {
    public static LoadingFragment newInstance() {
        return new LoadingFragment();
    }

    private View view;

    private enum Connected {False, Pending, True}

    private SerialService service;
    private UsbSerialPort usbSerialPort;
    private LoadingFragment.Connected connected = LoadingFragment.Connected.False;
    private boolean initialStart = true;
    private BroadcastReceiver broadcastReceiver;

    private int deviceId = 1002;
    private int port = 0;
    private int baudRate = 9600;

    private int skipCounter = 0;
    private boolean startDetect = false;

    List<Double> tempArray = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity activity = (MainActivity)getActivity();
        activity.SetPageNumber(3);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        view = inflater.inflate(R.layout.fragment_loading, container, false);
        return view;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.INTENT_ACTION_GRANT_USB)) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };

        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
        MainActivity activity = (MainActivity) getActivity();

        deviceId = activity.GetDeviceId();
        port = activity.GetPort();
        baudRate = activity.GetBaudRate();

        startDetect = true;
        tempArray.clear();

        if (service != null) {
            service.attach(this);
        } else {
            getActivity().startService(new Intent(getActivity(), SerialService.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        CircleView circleView = getActivity().findViewById(R.id.circleView);

        final ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 360);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleView.setValue((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_GRANT_USB));
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (connected != LoadingFragment.Connected.False)
        {
            disconnect();
        }
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onSerialConnect() {
        connected = LoadingFragment.Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        disconnect();
        MainActivity activity = (MainActivity)getActivity();
        activity.RestartActivity();
    }

    @Override
    public void onSerialIoError(Exception e) {
        disconnect();
        MainActivity activity = (MainActivity)getActivity();
        activity.RestartActivity();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onSerialRead(byte[] data) {
        skipCounter++;

        if (tempArray.size() == 40 && startDetect) {
            skipCounter=0;
            startDetect = false;
            Collections.sort(tempArray);
            double currentTemp = tempArray.get(25);

            if (currentTemp < 35.0) {
                Navigation.findNavController(view).navigate(R.id.action_LoadingFragment_to_FailedFragment);
            } else {
                Bundle bundle = new Bundle();
                bundle.putDouble("realTemp", currentTemp);
                Navigation.findNavController(view).navigate(R.id.action_LoadingFragment_to_TempFragment, bundle);
            }
        } else if (tempArray.size() < 40 && skipCounter>=10) {
            double temp = 0.0;
            try {
                String tempStr = new String(data);
                temp = Double.parseDouble(tempStr);
                tempArray.add(temp);
            } catch (NumberFormatException ex) {

            }
        }
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        for (UsbDevice v : usbManager.getDeviceList().values())
            if (v.getDeviceId() == deviceId)
                device = v;
        if (device == null) {
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if (driver == null) {
            return;
        }
        if (driver.getPorts().size() < port) {
            return;
        }
        usbSerialPort = driver.getPorts().get(port);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(Constants.INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }

        connected = LoadingFragment.Connected.Pending;
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), usbConnection, usbSerialPort);
            service.connect(socket);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = LoadingFragment.Connected.False;
        service.disconnect();
        usbSerialPort = null;
    }
}