package com.android.settings;

import android.app.settings.SettingsEnums;

import com.android.settings.dashboard.DashboardFragment;

public class KamisatoLabsFragment extends DashboardFragment {

    private static final String TAG = "KamisatoLabsFragment";

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.kamisato_labs_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN; // Use PAGE_UNKNOWN or define a new metric id if possible
    }
}
