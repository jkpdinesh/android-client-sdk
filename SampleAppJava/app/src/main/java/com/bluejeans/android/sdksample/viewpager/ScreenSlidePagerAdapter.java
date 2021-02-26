/*
 * Copyright (c) 2020 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample.viewpager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2;

    public ScreenSlidePagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new ContentViewFragment();
            default:
                return new RemoteViewFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
