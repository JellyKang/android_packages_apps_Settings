/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.slim;

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

/**
 * Lockscreen Buttons Settings
 */
public class LockscreenButtons extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenButtons";

    private static final String LONG_PRESS_BACK = "lockscreen_long_press_back";
    private static final String LONG_PRESS_HOME = "lockscreen_long_press_home";
    private static final String LONG_PRESS_MENU = "lockscreen_long_press_menu";
    private static final String LONG_PRESS_ASSIST = "lockscreen_long_press_assist";
    private static final String LONG_PRESS_APP_SWITCH = "lockscreen_long_press_app_switch";
    private static final String LONG_PRESS_CAMERA = "lockscreen_long_press_camera";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA = 0x20;

    private ListPreference mLongBackAction;
    private ListPreference mLongHomeAction;
    private ListPreference mLongMenuAction;
    private ListPreference mLongAssistAction;
    private ListPreference mLongAppSwitchAction;
    private ListPreference mLongCameraAction;
    private ListPreference[] mActions;

    private boolean torchSupported() {
        return getResources().getBoolean(R.bool.has_led_flash);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;

        addPreferencesFromResource(R.xml.lockscreen_buttons_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLongBackAction = (ListPreference) prefSet.findPreference(LONG_PRESS_BACK);
        if (hasBackKey) {
            mLongBackAction.setKey(Settings.System.LOCKSCREEN_LONG_BACK_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongBackAction);
        }

        mLongHomeAction = (ListPreference) prefSet.findPreference(LONG_PRESS_HOME);
        if (hasHomeKey) {
            mLongHomeAction.setKey(Settings.System.LOCKSCREEN_LONG_HOME_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongHomeAction);
        }

        mLongMenuAction = (ListPreference) prefSet.findPreference(LONG_PRESS_MENU);
        if (hasMenuKey) {
            mLongMenuAction.setKey(Settings.System.LOCKSCREEN_LONG_MENU_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongMenuAction);
        }

        mLongAssistAction = (ListPreference) prefSet.findPreference(LONG_PRESS_ASSIST);
        if (hasAssistKey) {
            mLongAssistAction.setKey(Settings.System.LOCKSCREEN_LONG_ASSIST_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongAssistAction);
        }

        mLongAppSwitchAction = (ListPreference) prefSet.findPreference(LONG_PRESS_APP_SWITCH);
        if (hasAppSwitchKey) {
            mLongAppSwitchAction.setKey(Settings.System.LOCKSCREEN_LONG_APP_SWITCH_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongAppSwitchAction);
        }

        mLongCameraAction = (ListPreference) prefSet.findPreference(LONG_PRESS_CAMERA);
        if (hasCameraKey) {
            mLongCameraAction.setKey(Settings.System.LOCKSCREEN_LONG_CAMERA_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongCameraAction);
        }

        mActions = new ListPreference[] {
            mLongBackAction, mLongHomeAction, mLongMenuAction,
            mLongAssistAction, mLongAppSwitchAction, mLongCameraAction
        };
        for (ListPreference pref : mActions) {
            if (torchSupported()) {
                final CharSequence[] oldEntries = pref.getEntries();
                final CharSequence[] oldValues = pref.getEntryValues();
                ArrayList<CharSequence> newEntries = new ArrayList<CharSequence>();
                ArrayList<CharSequence> newValues = new ArrayList<CharSequence>();
                for (int i = 0; i < oldEntries.length; i++) {
                    newEntries.add(oldEntries[i].toString());
                    newValues.add(oldValues[i].toString());
                }
                newEntries.add(getString(R.string.lockscreen_buttons_flashlight));
                newValues.add("FLASHLIGHT");
                pref.setEntries(
                        newEntries.toArray(new CharSequence[newEntries.size()]));
                pref.setEntryValues(
                        newValues.toArray(new CharSequence[newValues.size()]));
            }
            pref.setOnPreferenceChangeListener(this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        for (ListPreference pref : mActions) {
            updateEntry(pref);
        }
    }

    private void updateEntry(ListPreference pref) {
        String value = Settings.System.getString(getContentResolver(), pref.getKey());
        if (value == null) {
            value = "";
        }

        CharSequence entry = findEntryForValue(pref, value);
        if (entry != null) {
            pref.setValue(value);
            pref.setSummary(entry);
            return;
        }
    }

    private CharSequence findEntryForValue(ListPreference pref, CharSequence value) {
        CharSequence[] entries = pref.getEntryValues();
        for (int i = 0; i < entries.length; i++) {
            if (TextUtils.equals(entries[i], value)) {
                return pref.getEntries()[i];
            }
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        /* we only have ListPreferences, so know newValue is a string */
        ListPreference list = (ListPreference) pref;
        String value = (String) newValue;

        if (Settings.System.putString(getContentResolver(), list.getKey(), value)) {
            pref.setSummary(findEntryForValue(list, value));
        }

        return true;
    }

}
