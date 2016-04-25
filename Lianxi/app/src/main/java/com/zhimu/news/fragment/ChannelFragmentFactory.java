package com.zhimu.news.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * 所有频道工厂类:以后加频道，再这里判断
 * Created by Administrator on 2016.4.22.
 */
public class ChannelFragmentFactory {

    public static Fragment createFragment(String titleChannel) {

        Fragment fragment = null;
        if (titleChannel.equals("段子")) {
            fragment = new JokeTextFragment();
        } else if (titleChannel.equals("囧图")) {
            fragment = new JokeImageFragment();
        } else {
                Bundle bundle = new Bundle();
                bundle.putString("title", titleChannel);
                fragment = NewsFragment.newInstance(titleChannel);
                fragment.setArguments(bundle);
            }

            return fragment;

        }
    }
