package com.android.settings;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.development.PifDataPreference;

public class KamisatoLabsFragment extends DashboardFragment {

    private static final String TAG = "KamisatoLabsFragment";
    private static final String PIF_DATA_KEY = "pif_data_setting";

    private ActivityResultLauncher<Intent> mPifFilePickerLauncher;
    private PifDataPreference mPifDataPreference;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPifFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    Preference pref = findPreference(PIF_DATA_KEY);
                    if (pref instanceof PifDataPreference) {
                        ((PifDataPreference) pref).handleFileSelected(uri);
                    }
                }
            }
        );
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPifDataPreference = findPreference(PIF_DATA_KEY);
        if (mPifDataPreference != null) {
            mPifDataPreference.setFilePickerLauncher(mPifFilePickerLauncher);
        }
    }

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
