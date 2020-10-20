package com.tempscaner.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

public class SecondFragment extends Fragment {
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        view = inflater.inflate(R.layout.fragment_second, container, false);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity)getActivity();
        TextView textView = view.findViewById(R.id.textView);
        textView.setTextSize(activity.GetTextSize(12, 16, 20));
        TextView detect_temp_txt =  view.findViewById(R.id.detect_temp_txt);
        detect_temp_txt.setTextSize(activity.GetTextSize(14, 20, 20));

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_LoadingFragment);
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
            Navigation.findNavController(view).navigate(R.id.action_SecondFragment_to_FirstFragment);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}