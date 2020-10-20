package com.tempscaner.app;

import android.app.Service;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TempFragment extends Fragment {
    private View view;
    private double highTemp = 37.9;
    private double realTemp = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realTemp = getArguments().getDouble("realTemp");

        MainActivity activity = (MainActivity)getActivity();
        activity.SetPageNumber(4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_temp, container, false);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_rescan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TempFragment.this)
                        .navigate(R.id.action_TempFragment_to_LoadingFragment);
            }
        });

        try {
            realTemp = (double) (Math.round(realTemp * 10)) / 10; //四捨五入

            double realTempF = realTemp * (9 / 5) + 32;
            realTempF = (double) (Math.round(realTempF * 10)) / 10;

            String colorCode = realTemp >= highTemp ? "#B1256C" : "#006D94";
            TextView tempTextView = view.findViewById(R.id.temp_txt);
            tempTextView.setTextColor(Color.parseColor(colorCode));
            tempTextView.setText(realTemp + "°C / " + realTempF + "°F");

            String tempMessage = realTemp >= highTemp ? "The temperature is higher than normal" : "The temperature is normal";
            TextView tempMessageView = view.findViewById(R.id.temp_message);
            tempMessageView.setText(tempMessage);
        } catch (Exception e) {
            TextView tempTextView = view.findViewById(R.id.temp_txt);
            tempTextView.setText(e.getMessage()+"/"+e.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity activity = (MainActivity) getActivity();
        boolean hasDevice = activity.GetHasDevice();
        if (!hasDevice) {
            Navigation.findNavController(view).navigate(R.id.action_TempFragment_to_FirstFragment);
        }

        Vibrator myVibrator = (Vibrator) getActivity().getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(1000);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(getActivity().getApplication(), uri);
        rt.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}