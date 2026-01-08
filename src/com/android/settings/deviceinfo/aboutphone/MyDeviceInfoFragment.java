/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.deviceinfo.aboutphone;

import static androidx.core.content.ContextCompat.getMainExecutor;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.UserManager;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.deviceinfo.BluetoothAddressPreferenceController;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController;
import com.android.settings.deviceinfo.FeedbackPreferenceController;
import com.android.settings.deviceinfo.IpAddressPreferenceController;
import com.android.settings.deviceinfo.ManualPreferenceController;
import com.android.settings.deviceinfo.RegulatoryInfoPreferenceController;
import com.android.settings.deviceinfo.SafetyInfoPreferenceController;
import com.android.settings.deviceinfo.UptimePreferenceController;
import com.android.settings.deviceinfo.WifiMacAddressPreferenceController;
import com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoBuildDatePreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoDeviceCodenamePreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoDisplayPreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoFrontCameraPreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoMaintainerPreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoRamPreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoRearCameraPreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoRomNamePreferenceController;
import com.android.settings.deviceinfo.kamisato.KamisatoSocPreferenceController;
import com.android.settings.deviceinfo.simstatus.EidStatus;
import com.android.settings.deviceinfo.simstatus.SimEidPreferenceController;
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController;
import com.android.settings.deviceinfo.simstatus.SlotSimStatus;
import com.android.settings.flags.Flags;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SearchIndexable
public class MyDeviceInfoFragment extends DashboardFragment {

    private static final String LOG_TAG = "MyDeviceInfoFragment";
    private static final String KEY_EID_INFO = "eid_info";
    private static final String KEY_MY_DEVICE_INFO_HEADER = "my_device_info_header";
    private static final String KEY_KAMISATO_STORAGE_CARD = "kamisato_storage_card";
    private static final String KEY_KAMISATO_ABOUT_HEADER = "kamisato_about_header";

    private BuildNumberPreferenceController mBuildNumberPreferenceController;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DEVICEINFO;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBuildNumberPreferenceController = use(BuildNumberPreferenceController.class);
        mBuildNumberPreferenceController.setHost(this /* parent */);
    }

    @Override
    public void onStart() {
        super.onStart();
        initKamisatoHeader();
        initStorageCard();
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.my_device_info;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this /* fragment */, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, MyDeviceInfoFragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        final Executor executor = (fragment == null) ? getMainExecutor(context) :
                Executors.newSingleThreadExecutor();
        androidx.lifecycle.Lifecycle lifecycleObject = (fragment == null) ? null :
                fragment.getLifecycle();
        final SlotSimStatus slotSimStatus = new SlotSimStatus(context, executor, lifecycleObject);

        controllers.add(new IpAddressPreferenceController(context, lifecycle));
        controllers.add(new WifiMacAddressPreferenceController(context, lifecycle));
        controllers.add(new BluetoothAddressPreferenceController(context, lifecycle));
        controllers.add(new RegulatoryInfoPreferenceController(context));
        controllers.add(new SafetyInfoPreferenceController(context));
        controllers.add(new ManualPreferenceController(context));
        controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(new UptimePreferenceController(context, lifecycle));

        // Kamisato preference controllers
        controllers.add(new KamisatoDeviceCodenamePreferenceController(context, "kamisato_device_codename"));
        controllers.add(new KamisatoMaintainerPreferenceController(context, "kamisato_maintainer"));
        controllers.add(new KamisatoSocPreferenceController(context, "kamisato_soc"));
        controllers.add(new KamisatoRamPreferenceController(context, "kamisato_ram"));
        controllers.add(new KamisatoDisplayPreferenceController(context, "kamisato_display"));
        controllers.add(new KamisatoFrontCameraPreferenceController(context, "kamisato_front_camera"));
        controllers.add(new KamisatoRearCameraPreferenceController(context, "kamisato_rear_camera"));
        controllers.add(new KamisatoRomNamePreferenceController(context, "kamisato_rom_name"));
        controllers.add(new KamisatoBuildDatePreferenceController(context, "kamisato_build_date"));

        Consumer<String> imeiInfoList = imeiKey -> {
            if (Flags.catalystMyDeviceInfoPrefScreen()) {
                return;
            }
            ImeiInfoPreferenceController imeiRecord =
                    new ImeiInfoPreferenceController(context, imeiKey);
            imeiRecord.init(fragment, slotSimStatus);
            controllers.add(imeiRecord);
        };

        if (fragment != null) {
            imeiInfoList.accept(ImeiInfoPreferenceController.DEFAULT_KEY);
        }

        for (int slotIndex = 0; slotIndex < slotSimStatus.size(); slotIndex++) {
            SimStatusPreferenceController slotRecord =
                    new SimStatusPreferenceController(context,
                            slotSimStatus.getPreferenceKey(slotIndex));
            slotRecord.init(fragment, slotSimStatus);
            controllers.add(slotRecord);

            if (fragment != null) {
                imeiInfoList.accept(ImeiInfoPreferenceController.DEFAULT_KEY + (1 + slotIndex));
            }
        }

        EidStatus eidStatus = new EidStatus(slotSimStatus, context, executor);
        SimEidPreferenceController simEid = new SimEidPreferenceController(context,
                KEY_EID_INFO);
        simEid.init(slotSimStatus, eidStatus);
        controllers.add(simEid);

        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBuildNumberPreferenceController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initHeader() {
        // Legacy header - keep for compatibility but don't display
        final LayoutPreference headerPreference =
                getPreferenceScreen().findPreference(KEY_MY_DEVICE_INFO_HEADER);
        if (headerPreference != null) {
            headerPreference.setVisible(false);
        }
    }

    /**
     * Initialize the Kamisato hero header with device name, Android version, and security patch.
     */
    private void initKamisatoHeader() {
        final LayoutPreference kamisatoHeader =
                getPreferenceScreen().findPreference(KEY_KAMISATO_ABOUT_HEADER);
        if (kamisatoHeader == null) {
            return;
        }

        // Set device name from ro.kamisato.device (device codename)
        final TextView deviceNameView = kamisatoHeader.findViewById(R.id.kamisato_device_name);
        if (deviceNameView != null) {
            String deviceCodename = android.os.SystemProperties.get("ro.kamisato.device", "");
            if (deviceCodename.isEmpty()) {
                deviceCodename = Build.MODEL; // Fallback to Build.MODEL if not set
            }
            deviceNameView.setText(deviceCodename);
        }

        // Set Android version
        final TextView androidVersionView = kamisatoHeader.findViewById(R.id.kamisato_android_version);
        if (androidVersionView != null) {
            androidVersionView.setText(getString(R.string.kamisato_android_version) + " " + Build.VERSION.RELEASE);
        }

        // Set security patch
        final TextView securityPatchView = kamisatoHeader.findViewById(R.id.kamisato_security_patch);
        if (securityPatchView != null) {
            String patch = DeviceInfoUtils.getSecurityPatch();
            if (patch != null && !patch.isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(patch);
                    patch = outputFormat.format(date);
                } catch (ParseException e) {
                    // Keep original format if parsing fails
                }
            }
            securityPatchView.setText(patch != null ? patch : getString(R.string.kamisato_unknown));
        }
    }

    /**
     * Initialize the storage card with used/total storage and progress bar.
     */
    private void initStorageCard() {
        final LayoutPreference storageCard =
                getPreferenceScreen().findPreference(KEY_KAMISATO_STORAGE_CARD);
        if (storageCard == null) {
            return;
        }

        final View cardContainer = storageCard.findViewById(R.id.kamisato_storage_card_container);
        final TextView usageText = storageCard.findViewById(R.id.kamisato_storage_usage);
        final ProgressBar progressBar = storageCard.findViewById(R.id.kamisato_storage_progress);

        if (usageText == null || progressBar == null) {
            return;
        }

        // Load storage info in background thread
        final Context context = getContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Use StorageStatsManager for accurate storage like Storage settings
                StorageStatsManager storageStatsManager = 
                        context.getSystemService(StorageStatsManager.class);
                
                // Get actual system values
                final long totalBytes = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT);
                final long freeBytes = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT);
                
                // Calculate used storage accurately: Used = Total - Available
                final long usedBytes = totalBytes - freeBytes;

                // Round total to nearest standard size for display only
                final long displayTotal = roundToNearestStandardSize(totalBytes);

                // Calculate percentage based on actual total, not rounded
                final int percentage = (int) ((usedBytes * 100) / totalBytes);

                // Format sizes - used shows actual, total shows rounded for clean display
                final String usedFormatted = formatStorageSizeAccurate(usedBytes);
                final String totalFormatted = formatStorageSizeSimple(displayTotal);
                final String usageString = usedFormatted + "/" + totalFormatted;

                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        usageText.setText(usageString);
                        progressBar.setProgress(percentage);
                        progressBar.setProgressTintList(ColorStateList.valueOf(getProgressColor(percentage)));
                    });
                }
            } catch (Exception e) {
                // Handle error gracefully
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        usageText.setText(getString(R.string.kamisato_unknown));
                        progressBar.setProgress(0);
                    });
                }
            }
        });

        // Set click listener to open Storage settings
        if (cardContainer != null) {
            cardContainer.setOnClickListener(v -> {
                Intent intent = new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                startActivity(intent);
            });
        }

        // Initialize RAM card as well
        initRamCard(storageCard);
    }

    /**
     * Initialize the RAM card with used/total RAM and progress bar.
     */
    private void initRamCard(LayoutPreference storageCard) {
        if (storageCard == null) {
            return;
        }

        final View ramContainer = storageCard.findViewById(R.id.kamisato_ram_card_container);
        final TextView ramUsageText = storageCard.findViewById(R.id.kamisato_ram_usage);
        final ProgressBar ramProgressBar = storageCard.findViewById(R.id.kamisato_ram_progress);

        if (ramUsageText == null || ramProgressBar == null) {
            return;
        }

        // Load RAM info using ActivityManager.MemoryInfo (same as Running Services)
        final Context context = getContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ActivityManager activityManager = context.getSystemService(ActivityManager.class);
                ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memInfo);

                final long totalRam = memInfo.totalMem;
                final long availRam = memInfo.availMem;
                final long usedRam = totalRam - availRam;

                // Round total to nearest standard RAM size
                final long roundedTotal = roundToNearestRamSize(totalRam);

                // Calculate percentage based on rounded total
                final int percentage = (int) ((usedRam * 100) / roundedTotal);

                // Format sizes
                final String usedFormatted = formatRamSize(usedRam);
                final String totalFormatted = formatRamSize(roundedTotal);
                final String usageString = usedFormatted + "/" + totalFormatted;

                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ramUsageText.setText(usageString);
                        ramProgressBar.setProgress(percentage);
                        ramProgressBar.setProgressTintList(ColorStateList.valueOf(getProgressColor(percentage)));
                    });
                }
            } catch (Exception e) {
                // Handle error gracefully
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ramUsageText.setText(getString(R.string.kamisato_unknown));
                        ramProgressBar.setProgress(0);
                    });
                }
            }
        });

        // Set click listener to open Running Services
        if (ramContainer != null) {
            ramContainer.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$RunningServicesActivity");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // Fallback to developer options if running services not accessible
                }
            });
        }
    }

    /**
     * Round bytes to nearest standard RAM size (4, 6, 8, 12, 16, 24, 32 GB).
     */
    private long roundToNearestRamSize(long bytes) {
        long gb = 1024L * 1024L * 1024L;
        long[] standardSizes = {4, 6, 8, 12, 16, 24, 32, 64};

        long sizeInGb = bytes / gb;

        for (long size : standardSizes) {
            if (sizeInGb <= size) {
                return size * gb;
            }
        }
        // Default: round up
        return ((sizeInGb + 4) / 8) * 8 * gb;
    }

    /**
     * Format RAM size to compact GB format with decimal for small values.
     */
    private String formatRamSize(long bytes) {
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb < 10) {
            return String.format(java.util.Locale.US, "%.1fGB", gb);
        }
        return (int) gb + "GB";
    }

    /**
     * Round bytes to nearest standard storage size (32, 64, 128, 256, 512, 1024 GB).
     */
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


    /**
     * Format storage size to compact GB format (e.g., "96GB").
     */
    private String formatStorageSize(long bytes) {
        long gb = 1024L * 1024L * 1024L;
        long sizeInGb = bytes / gb;
        return sizeInGb + "GB";
    }

    /**
     * Format storage size accurately with decimal for used storage (e.g., "22.4GB").
     */
    private String formatStorageSizeAccurate(long bytes) {
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb < 100) {
            // Show one decimal for better accuracy
            return String.format(java.util.Locale.US, "%.1fGB", gb);
        }
        return (int) gb + "GB";
    }

    /**
     * Format storage size simply for total display (e.g., "128GB").
     */
    private String formatStorageSizeSimple(long bytes) {
        long gb = 1024L * 1024L * 1024L;
        long sizeInGb = bytes / gb;
        return sizeInGb + "GB";
    }

    /**
     * Get progress bar color based on usage percentage.
     * <50% = Green, <80% = Orange, >=80% = Red
     */
    private int getProgressColor(int percentage) {
        if (percentage < 50) {
            return Color.parseColor("#4CAF50"); // Green
        } else if (percentage < 80) {
            return Color.parseColor("#FF9800"); // Orange
        } else {
            return Color.parseColor("#F44336"); // Red
        }
    }
    /**
     * Called when user confirms or cancels device name change in DeviceNameWarningDialog.
     * This method is required by DeviceNameWarningDialog even if device_name preference is not shown.
     */
    public void onSetDeviceNameConfirm(boolean confirm) {
        // No-op since device_name preference is not in our layout
        // but this method is still needed for DeviceNameWarningDialog compatibility
    }

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return MyDeviceInfoScreen.KEY;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.my_device_info) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null /* fragment */,
                            null /* lifecycle */);
                }
            };
}
