/*
 * Copyright (C) 2024-2025 Kamisato-AOSP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.kamisato;

import android.app.ActivityManager;
import android.content.Context;
import android.text.format.Formatter;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Preference controller for displaying total RAM.
 */
public class KamisatoRamPreferenceController extends BasePreferenceController {

    private final Context mContext;

    public KamisatoRamPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        ActivityManager activityManager = mContext.getSystemService(ActivityManager.class);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (activityManager != null) {
            activityManager.getMemoryInfo(memInfo);
            long totalRam = memInfo.totalMem;
            // Round to nearest GB for cleaner display
            long roundedRam = roundToNearestGb(totalRam);
            return Formatter.formatShortFileSize(mContext, roundedRam);
        }
        return mContext.getString(R.string.kamisato_unknown);
    }

    private long roundToNearestGb(long bytes) {
        long gb = 1024L * 1024L * 1024L;
        long rounded = (bytes + gb / 2) / gb * gb;
        return rounded > 0 ? rounded : bytes;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(getSummary());
    }
}
