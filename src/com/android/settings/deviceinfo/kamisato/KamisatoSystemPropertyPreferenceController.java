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
import android.os.SystemProperties;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Preference controller for displaying system property values from ro.kamisato.* properties.
 * Used for device codename, maintainer, SoC, display, camera information etc.
 */
public class KamisatoSystemPropertyPreferenceController extends BasePreferenceController {

    private final String mPropertyName;
    private final String mFallbackValue;

    public KamisatoSystemPropertyPreferenceController(Context context, String preferenceKey,
            String propertyName) {
        super(context, preferenceKey);
        mPropertyName = propertyName;
        mFallbackValue = context.getString(R.string.kamisato_unknown);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        String value = SystemProperties.get(mPropertyName, "");
        if (TextUtils.isEmpty(value)) {
            return mFallbackValue;
        }
        return value;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(getSummary());
    }
}
