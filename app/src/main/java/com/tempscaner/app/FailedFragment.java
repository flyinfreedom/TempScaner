package com.tempscaner.app;

import android.app.Service;
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

public class FailedFragment extends Fragment {
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_failed, container, false);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity)getActivity();
        TextView detection_failed_sub = view.findViewById(R.id.detection_failed_sub);
        detection_failed_sub.setTextSize(activity.GetTextSize(12, 16, 16));

        view.findViewById(R.id.button_rescan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FailedFragment.this)
                        .navigate(R.id.action_FailedFragment_to_LoadingFragment);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity activity = (MainActivity)getActivity();
        boolean hasDevice = activity.GetHasDevice();
        if(!hasDevice)
        {
            Navigation.findNavController(view).navigate(R.id.action_FailedFragment_to_FirstFragment);
        }

        Vibrator myVibrator = (Vibrator) getActivity().getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(1000);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone( getActivity().getApplication(), uri);
        rt.play();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}