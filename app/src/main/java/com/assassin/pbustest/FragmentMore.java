package com.assassin.pbustest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentMore extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        view.findViewById(R.id.btnSetLine).setOnClickListener(this);
        view.findViewById(R.id.btnSetLocation).setOnClickListener(this);



        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnSetLine:
                intent = new Intent(getActivity(), SetLineNumPage.class);
                startActivity(intent);
                break;

            case R.id.btnSetLocation:
                intent = new Intent(getActivity(), SetLocationPage.class);
                startActivity(intent);
                break;
        }
    }
}
