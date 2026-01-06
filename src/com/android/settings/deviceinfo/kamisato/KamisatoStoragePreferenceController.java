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

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Preference controller for displaying total internal storage.
 */
public class KamisatoStoragePreferenceController extends BasePreferenceController {

    private final Context mContext;

    public KamisatoStoragePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long totalBytes = statFs.getTotalBytes();
            // Round to nearest standard storage size (32, 64, 128, 256, 512, 1024 GB)
            long roundedStorage = roundToNearestStandardSize(totalBytes);
            return Formatter.formatShortFileSize(mContext, roundedStorage);
        } catch (Exception e) {
            return mContext.getString(R.string.kamisato_unknown);
        }
    }

    private long roundToNearestStandardSize(long bytes) {
        long gb = 1024L * 1024L * 1024L;
        long[] standardSizes = {32, 64, 128, 256, 512, 1024, 2048};
        
        long sizeInGb = bytes / gb;
        
        for (long size : standardSizes) {
            if (sizeInGb <= size) {
                return size * gb;
            }
        }
        // Default: round to nearest 256 GB
        return ((sizeInGb + 128) / 256) * 256 * gb;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(getSummary());
    }
}
