package com.zhimu.news.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zhimu.news.R;

/**
 *
 */
public class EmptyFragment extends Fragment {

    private Button mButton;

    private OnMyImageFragmentListener mListener;

    public EmptyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_empty, container, false);
        mButton = (Button) view.findViewById(R.id.back_button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 当按钮被按下时，接口回调
     */
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onCheckFragmentButton();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMyImageFragmentListener) {
            mListener = (OnMyImageFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "must implement OnMyImageFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     *
     */
    public interface OnMyImageFragmentListener {
        void onCheckFragmentButton();
    }

}
