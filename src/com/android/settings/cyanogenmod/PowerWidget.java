/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.ListFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.wimax.WimaxHelper;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Spannable;
import android.widget.EditText;

import com.android.internal.telephony.Phone;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
+import com.android.settings.widget.SeekBarPreference;
+import net.margaritov.preference.colorpicker.ColorPickerView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PowerWidget extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "PowerWidget";
    private static final String SEPARATOR = "OV=I=XseparatorX=I=VO";
    private static final String UI_EXP_WIDGET = "expanded_widget";
    private static final String UI_EXP_WIDGET_HIDE_ONCHANGE = "expanded_hide_onchange";
    private static final String UI_EXP_WIDGET_HIDE_SCROLLBAR = "expanded_hide_scrollbar";
    private static final String UI_EXP_WIDGET_HAPTIC_FEEDBACK = "expanded_haptic_feedback";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final String PREF_NOTIFICATION_WALLPAPER_LANDSCAPE = "notification_wallpaper_landscape";
    private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";


    private CheckBoxPreference mPowerWidget;
    private CheckBoxPreference mPowerWidgetHideOnChange;
    private CheckBoxPreference mPowerWidgetHideScrollBar;
    private ListPreference mPowerWidgetHapticFeedback;
    private Preference mCustomLabel;
    private String mCustomLabelText = null;
    private int newDensityValue;
    private ListPreference mNotificationWallpaper;
    private ListPreference mNotificationWallpaperLandscape;
    SeekBarPreference mWallpaperAlpha;

    private File customnavTemp;
    private File customnavTempLandscape;

    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int REQUEST_PICK_WALLPAPER_LANDSCAPE = 202;
    private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";
    private static final String WALLPAPER_NAME_LANDSCAPE = "notification_wallpaper_landscape.jpg";

    private ContentResolver mResolver;
    private Activity mActivity;

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();

        mResolver = getContentResolver();
        mActivity = getActivity();

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

	customnavTemp = new File(getActivity().getFilesDir()+"/notification_wallpaper_temp.jpg");
        customnavTempLandscape = new File(getActivity().getFilesDir()+"/notification_wallpaper_temp_landscape.jpg");

        mNotificationWallpaper = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER);
        mNotificationWallpaper.setOnPreferenceChangeListener(this);

        mNotificationWallpaperLandscape = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_LANDSCAPE);
        mNotificationWallpaperLandscape.setOnPreferenceChangeListener(this);

        float wallpaperTransparency;
        try{
            wallpaperTransparency = Settings.System.getFloat(getActivity().getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA);
        }catch (Exception e) {
            wallpaperTransparency = 0;
            Settings.System.putFloat(getActivity().getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA, 0.1f);
        }
        mWallpaperAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);
        mWallpaperAlpha.setInitValue((int) (wallpaperTransparency * 100));
        mWallpaperAlpha.setProperty(Settings.System.NOTIF_WALLPAPER_ALPHA);
        mWallpaperAlpha.setOnPreferenceChangeListener(this);

        updateCustomBackgroundSummary();

        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.power_widget_settings);

            mPowerWidget = (CheckBoxPreference) prefSet.findPreference(UI_EXP_WIDGET);
            mPowerWidgetHideOnChange = (CheckBoxPreference) prefSet
                    .findPreference(UI_EXP_WIDGET_HIDE_ONCHANGE);
            mPowerWidgetHideScrollBar = (CheckBoxPreference) prefSet
                    .findPreference(UI_EXP_WIDGET_HIDE_SCROLLBAR);

            mPowerWidgetHapticFeedback = (ListPreference) prefSet
                    .findPreference(UI_EXP_WIDGET_HAPTIC_FEEDBACK);
            mPowerWidgetHapticFeedback.setOnPreferenceChangeListener(this);
            mPowerWidgetHapticFeedback.setSummary(mPowerWidgetHapticFeedback.getEntry());

            mPowerWidget.setChecked((Settings.System.getInt(getActivity().getApplicationContext()
                    .getContentResolver(),
                    Settings.System.EXPANDED_VIEW_WIDGET, 0) == 1));
            mPowerWidgetHideOnChange.setChecked((Settings.System.getInt(getActivity()
                    .getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1));
            mPowerWidgetHideScrollBar.setChecked((Settings.System.getInt(getActivity()
                    .getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_HIDE_SCROLLBAR, 0) == 1));
            mPowerWidgetHapticFeedback.setValue(Integer.toString(Settings.System.getInt(
                    getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_HAPTIC_FEEDBACK, 2)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCustomBackgroundSummary();
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND);
        if (value == null) {
            resId = R.string.notification_background_default_wallpaper;
            mNotificationWallpaper.setValueIndex(2);
            mNotificationWallpaperLandscape.setEnabled(false);
        } else if (value.isEmpty()) {
            resId = R.string.notification_background_custom_image;
            mNotificationWallpaper.setValueIndex(1);
            mNotificationWallpaperLandscape.setEnabled(true);
        } else {
            resId = R.string.notification_background_color_fill;
            mNotificationWallpaper.setValueIndex(0);
            mNotificationWallpaperLandscape.setEnabled(false);
        }
        mNotificationWallpaper.setSummary(getResources().getString(resId));

        value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE);
        if (value == null) {
            resId = R.string.notification_background_default_wallpaper;
            mNotificationWallpaperLandscape.setValueIndex(1);
        } else {
            resId = R.string.notification_background_custom_image;
            mNotificationWallpaperLandscape.setValueIndex(0);
        }
        mNotificationWallpaperLandscape.setSummary(getResources().getString(resId));
    }

    public void deleteWallpaper (boolean orientation) {
      File wallpaperToDelete = new File(getActivity().getFilesDir()+"/notification_wallpaper.jpg");
      File wallpaperToDeleteLandscape = new File(getActivity().getFilesDir()+"/notification_wallpaper_landscape.jpg");

      if (wallpaperToDelete.exists() && !orientation) {
         wallpaperToDelete.delete();
      }

      if (wallpaperToDeleteLandscape.exists() && orientation) {
         wallpaperToDeleteLandscape.delete();
      }

      if (orientation) {
         Settings.System.putString(getContentResolver(),
            Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE, null);
      }
    }

    public void observerResourceHelper() {
       float helper;
       float first = Settings.System.getFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, 0.1f);
        if (first < 0.9f) {
            helper = first + 0.1f;
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, helper);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, first);
        }else {
            helper = first - 0.1f;
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, helper);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, first);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
          if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER) {
              FileOutputStream wallpaperStream = null;
              Settings.System.putString(getContentResolver(),
                      Settings.System.NOTIFICATION_BACKGROUND,"");
              try {
                 wallpaperStream = getActivity().getApplicationContext().openFileOutput(WALLPAPER_NAME,
                         Context.MODE_WORLD_READABLE);
                 Uri selectedImageUri = Uri.fromFile(customnavTemp);
                 Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                 wallpaperStream.close();
                 customnavTemp.delete();
               } catch (Exception e) {
                     Log.e(TAG, e.getMessage(), e);
               }
            }else if (requestCode == REQUEST_PICK_WALLPAPER_LANDSCAPE) {
              FileOutputStream wallpaperStream = null;
              Settings.System.putString(getContentResolver(),
                      Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE,"");
              try {
                 wallpaperStream = getActivity().getApplicationContext().openFileOutput(WALLPAPER_NAME_LANDSCAPE,
                         Context.MODE_WORLD_READABLE);
                 Uri selectedImageUri = Uri.fromFile(customnavTempLandscape);
                 Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                 wallpaperStream.close();
                 customnavTempLandscape.delete();
               } catch (Exception e) {
                     Log.e(TAG, e.getMessage(), e);
               }
            }
        }
        observerResourceHelper();
        updateCustomBackgroundSummary();
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPowerWidgetHapticFeedback) {
            int intValue = Integer.parseInt((String) newValue);
            int index = mPowerWidgetHapticFeedback.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_HAPTIC_FEEDBACK, intValue);
            mPowerWidgetHapticFeedback.setSummary(mPowerWidgetHapticFeedback.getEntries()[index]);
            return true;
        } else if (preference == mWallpaperAlpha) {
            float valNav = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, valNav / 100);
            return true;
        }else if (preference == mNotificationWallpaper) {
            int indexOf = mNotificationWallpaper.findIndexOfValue(newValue.toString());
            switch (indexOf) {
            //Displays color dialog when user has chosen color fill
            case 0:
                final ColorPickerView colorView = new ColorPickerView(mActivity);
                int currentColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.NOTIFICATION_BACKGROUND, -1);
                if (currentColor != -1) {
                    colorView.setColor(currentColor);
                }
                colorView.setAlphaSliderVisible(false);
                new AlertDialog.Builder(mActivity)
                .setTitle(R.string.notification_drawer_custom_background_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_BACKGROUND, colorView.getColor());
                        updateCustomBackgroundSummary();
                        deleteWallpaper(false);
                        deleteWallpaper(true);
                        observerResourceHelper();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setView(colorView).show();
                break;
            //Launches intent for user to select an image/crop it to set as background
            case 1:
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = getActivity().getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                boolean isPortrait = getResources()
                        .getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT;
                intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
                intent.putExtra("outputX", isPortrait ? width : height);
                intent.putExtra("outputY", isPortrait ? height : width);
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                try {
                     customnavTemp.createNewFile();
                     customnavTemp.setWritable(true, false);
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(customnavTemp));
                     startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            //Sets background to default
            case 2:
                Settings.System.putString(getContentResolver(),
                        Settings.System.NOTIFICATION_BACKGROUND, null);
                deleteWallpaper(false);
                deleteWallpaper(true);
                observerResourceHelper();
                updateCustomBackgroundSummary();
                break;
            }
            return true;
        }else if (preference == mNotificationWallpaperLandscape) {

            int indexOf = mNotificationWallpaperLandscape.findIndexOfValue(newValue.toString());
            switch (indexOf) {
            //Launches intent for user to select an image/crop it to set as background
            case 0:
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = getActivity().getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                boolean isPortrait = getResources()
                        .getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT;
                intent.putExtra("aspectX", isPortrait ? height - titleBarHeight : width);
                intent.putExtra("aspectY", isPortrait ? width : height - titleBarHeight);
                intent.putExtra("outputX", isPortrait ? height : width);
                intent.putExtra("outputY", isPortrait ? width : height);
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                try {
                     customnavTempLandscape.createNewFile();
                     customnavTempLandscape.setWritable(true, false);
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(customnavTempLandscape));
                     startActivityForResult(intent, REQUEST_PICK_WALLPAPER_LANDSCAPE);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            //Sets background to default
            case 1:
                deleteWallpaper(true);
                observerResourceHelper();
                updateCustomBackgroundSummary();
                break;
            }
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mPowerWidget) {
            value = mPowerWidget.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_VIEW_WIDGET,
                    value ? 1 : 0);
        } else if (preference == mPowerWidgetHideOnChange) {
            value = mPowerWidgetHideOnChange.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_HIDE_ONCHANGE,
                    value ? 1 : 0);
        } else if (preference == mPowerWidgetHideScrollBar) {
            value = mPowerWidgetHideScrollBar.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_HIDE_SCROLLBAR,
                    value ? 1 : 0);
        } 
	else if (preference == mCustomLabel) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle(R.string.custom_carrier_label_title);
		    alert.setMessage(R.string.custom_carrier_label_explain);

		    // Set an EditText view to get user input
		    final EditText input = new EditText(getActivity());
		    input.setText(mCustomLabelText != null ? mCustomLabelText : "");
		    alert.setView(input);
		    alert.setPositiveButton(getResources().getString(R.string.ok),
		    new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int whichButton) {
				    String value = ((Spannable) input.getText()).toString();
					    Settings.System.putString(getActivity().getContentResolver(),
						    Settings.System.CUSTOM_CARRIER_LABEL, value);
			 	    updateCustomLabelTextSummary();
				    Intent i = new Intent();					    
				    i.setAction("com.android.settings.LABEL_CHANGED");
				    getActivity().getApplicationContext().sendBroadcast(i);
			    }
		    });
		    alert.setNegativeButton(getResources().getString(R.string.cancel),
		    new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
					    // Canceled.
			}
		    });

		    alert.show();
	} else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    public static class PowerWidgetChooser extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        public PowerWidgetChooser() {
        }

        private static final String TAG = "PowerWidgetActivity";

        private static final String BUTTONS_CATEGORY = "pref_buttons";
        private static final String BUTTON_MODES_CATEGORY = "pref_buttons_modes";
        private static final String SELECT_BUTTON_KEY_PREFIX = "pref_button_";

        private static final String EXP_BRIGHTNESS_MODE = "pref_brightness_mode";
        private static final String EXP_NETWORK_MODE = "pref_network_mode";
        private static final String EXP_SCREENTIMEOUT_MODE = "pref_screentimeout_mode";
        private static final String EXP_RING_MODE = "pref_ring_mode";
        private static final String EXP_FLASH_MODE = "pref_flash_mode";

        private HashMap<CheckBoxPreference, String> mCheckBoxPrefs = new HashMap<CheckBoxPreference, String>();

        MultiSelectListPreference mBrightnessMode;
        ListPreference mNetworkMode;
        ListPreference mScreenTimeoutMode;
        MultiSelectListPreference mRingMode;
        ListPreference mFlashMode;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            addPreferencesFromResource(R.xml.power_widget);

            PreferenceScreen prefSet = getPreferenceScreen();
            PackageManager pm = getPackageManager();

            if (getActivity().getApplicationContext() == null) {
                return;
            }

            mBrightnessMode = (MultiSelectListPreference) prefSet
                    .findPreference(EXP_BRIGHTNESS_MODE);
            String storedBrightnessMode = Settings.System.getString(getActivity()
                    .getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_BRIGHTNESS_MODE);
            if (storedBrightnessMode != null) {
                String[] brightnessModeArray = TextUtils.split(storedBrightnessMode, SEPARATOR);
                mBrightnessMode.setValues(new HashSet<String>(Arrays.asList(brightnessModeArray)));
                updateSummary(storedBrightnessMode, mBrightnessMode, R.string.pref_brightness_mode_summary);
            }
            mBrightnessMode.setOnPreferenceChangeListener(this);
            mNetworkMode = (ListPreference) prefSet.findPreference(EXP_NETWORK_MODE);
            mNetworkMode.setOnPreferenceChangeListener(this);
            mScreenTimeoutMode = (ListPreference) prefSet.findPreference(EXP_SCREENTIMEOUT_MODE);
            mScreenTimeoutMode.setOnPreferenceChangeListener(this);
            mRingMode = (MultiSelectListPreference) prefSet.findPreference(EXP_RING_MODE);
            String storedRingMode = Settings.System.getString(getActivity()
                    .getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_RING_MODE);
            if (storedRingMode != null) {
                String[] ringModeArray = TextUtils.split(storedRingMode, SEPARATOR);
                mRingMode.setValues(new HashSet<String>(Arrays.asList(ringModeArray)));
                updateSummary(storedRingMode, mRingMode, R.string.pref_ring_mode_summary);
            }
            mRingMode.setOnPreferenceChangeListener(this);
            mFlashMode = (ListPreference) prefSet.findPreference(EXP_FLASH_MODE);
            mFlashMode.setOnPreferenceChangeListener(this);

            // TODO: set the default values of the items

            // Update the summary text
            mNetworkMode.setSummary(mNetworkMode.getEntry());
            mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntry());
            mFlashMode.setSummary(mFlashMode.getEntry());

            // Add the available buttons to the list
            PreferenceCategory prefButtons = (PreferenceCategory) prefSet
                    .findPreference(BUTTONS_CATEGORY);

            // Add the available mode buttons, incase they need to be removed later
            PreferenceCategory prefButtonsModes = (PreferenceCategory) prefSet
                    .findPreference(BUTTON_MODES_CATEGORY);

            // empty our preference category and set it to order as added
            prefButtons.removeAll();
            prefButtons.setOrderingAsAdded(false);

            // emtpy our checkbox map
            mCheckBoxPrefs.clear();

            // get our list of buttons
            ArrayList<String> buttonList = PowerWidgetUtil.getButtonListFromString(PowerWidgetUtil
                    .getCurrentButtons(getActivity().getApplicationContext()));

            // Don't show WiMAX option if not supported
            boolean isWimaxEnabled = WimaxHelper.isWimaxSupported(getActivity());
            if (!isWimaxEnabled) {
                PowerWidgetUtil.BUTTONS.remove(PowerWidgetUtil.BUTTON_WIMAX);
            }

            // Don't show mobile data options if not supported
            boolean isMobileData = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            if (!isMobileData) {
                PowerWidgetUtil.BUTTONS.remove(PowerWidgetUtil.BUTTON_MOBILEDATA);
                PowerWidgetUtil.BUTTONS.remove(PowerWidgetUtil.BUTTON_NETWORKMODE);
                PowerWidgetUtil.BUTTONS.remove(PowerWidgetUtil.BUTTON_WIFIAP);
                prefButtonsModes.removePreference(mNetworkMode);
            }

            // fill that checkbox map!
            for (PowerWidgetUtil.ButtonInfo button : PowerWidgetUtil.BUTTONS.values()) {
                // create a checkbox
                CheckBoxPreference cb = new CheckBoxPreference(getActivity()
                        .getApplicationContext());

                // set a dynamic key based on button id
                cb.setKey(SELECT_BUTTON_KEY_PREFIX + button.getId());

                // set vanity info
                cb.setTitle(button.getTitleResId());

                // set our checked state
                if (buttonList.contains(button.getId())) {
                    cb.setChecked(true);
                } else {
                    cb.setChecked(false);
                }

                // add to our prefs set
                mCheckBoxPrefs.put(cb, button.getId());

                // specific checks for availability on some platforms
                if (PowerWidgetUtil.BUTTON_FLASHLIGHT.equals(button.getId()) &&
                        !getResources().getBoolean(R.bool.has_led_flash)) {
                    // disable flashlight if it's not supported
                    cb.setEnabled(false);
                    mFlashMode.setEnabled(false);
                } else if (PowerWidgetUtil.BUTTON_NETWORKMODE.equals(button.getId())) {
                    // some phones run on networks not supported by this button,
                    // so disable it
                    int network_state = -99;

                    try {
                        network_state = Settings.Global.getInt(getActivity()
                                .getApplicationContext().getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE);
                    } catch (Settings.SettingNotFoundException e) {
                        Log.e(TAG, "Unable to retrieve PREFERRED_NETWORK_MODE", e);
                    }

                    switch (network_state) {
                    // list of supported network modes
                        case Phone.NT_MODE_WCDMA_PREF:
                        case Phone.NT_MODE_WCDMA_ONLY:
                        case Phone.NT_MODE_GSM_UMTS:
                        case Phone.NT_MODE_GSM_ONLY:
                            break;
                        default:
                            cb.setEnabled(false);
                            break;
                    }
                }

                // add to the category
                prefButtons.addPreference(cb);
            }
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            // we only modify the button list if it was one of our checks that
            // was clicked
            boolean buttonWasModified = false;
            ArrayList<String> buttonList = new ArrayList<String>();
            for (Map.Entry<CheckBoxPreference, String> entry : mCheckBoxPrefs.entrySet()) {
                if (entry.getKey().isChecked()) {
                    buttonList.add(entry.getValue());
                }

                if (preference == entry.getKey()) {
                    buttonWasModified = true;
                }
            }

            if (buttonWasModified) {
                // now we do some wizardry and reset the button list
                PowerWidgetUtil.saveCurrentButtons(getActivity().getApplicationContext(),
                        PowerWidgetUtil.mergeInNewButtonString(
                                PowerWidgetUtil.getCurrentButtons(getActivity()
                                        .getApplicationContext()), PowerWidgetUtil
                                        .getButtonStringFromList(buttonList)));
                return true;
		}

            return false;
        }

        private class MultiSelectListPreferenceComparator implements Comparator<String> {
            private MultiSelectListPreference pref;

            MultiSelectListPreferenceComparator(MultiSelectListPreference p) {
                pref = p;
            }

            @Override
            public int compare(String lhs, String rhs) {
                return Integer.compare(pref.findIndexOfValue(lhs),
                        pref.findIndexOfValue(rhs));
            }
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mBrightnessMode) {
                ArrayList<String> arrValue = new ArrayList<String>((Set<String>) newValue);
                Collections.sort(arrValue, new MultiSelectListPreferenceComparator(mBrightnessMode));
                Settings.System.putString(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.EXPANDED_BRIGHTNESS_MODE, TextUtils.join(SEPARATOR, arrValue));
                updateSummary(TextUtils.join(SEPARATOR, arrValue),
                        mBrightnessMode, R.string.pref_brightness_mode_summary);
            } else if (preference == mNetworkMode) {
                int value = Integer.valueOf((String) newValue);
                int index = mNetworkMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.EXPANDED_NETWORK_MODE, value);
                mNetworkMode.setSummary(mNetworkMode.getEntries()[index]);
            } else if (preference == mScreenTimeoutMode) {
                int value = Integer.valueOf((String) newValue);
                int index = mScreenTimeoutMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.EXPANDED_SCREENTIMEOUT_MODE, value);
                mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntries()[index]);
            } else if (preference == mRingMode) {
                ArrayList<String> arrValue = new ArrayList<String>((Set<String>) newValue);
                Collections.sort(arrValue, new MultiSelectListPreferenceComparator(mRingMode));
                Settings.System.putString(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.EXPANDED_RING_MODE, TextUtils.join(SEPARATOR, arrValue));
                updateSummary(TextUtils.join(SEPARATOR, arrValue), mRingMode, R.string.pref_ring_mode_summary);
            } else if (preference == mFlashMode) {
                int value = Integer.valueOf((String) newValue);
                int index = mFlashMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.EXPANDED_FLASH_MODE, value);
                mFlashMode.setSummary(mFlashMode.getEntries()[index]);
            }
            return true;
        }

        private void updateSummary(String val, MultiSelectListPreference pref, int defSummary) {
            // Update summary message with current values
            final String[] values = parseStoredValue(val);
            if (values != null) {
                final int length = values.length;
                final CharSequence[] entries = pref.getEntries();
                StringBuilder summary = new StringBuilder();
                for (int i = 0; i < (length); i++) {
                    CharSequence entry = entries[Integer.parseInt(values[i])];
                    if ((length - i) > 2) {
                        summary.append(entry).append(", ");
                    } else if ((length - i) == 2) {
                        summary.append(entry).append(" & ");
                    } else if ((length - i) == 1) {
                        summary.append(entry);
                    }
                }
                pref.setSummary(summary);
            } else {
                pref.setSummary(defSummary);
            }
        }

        public static String[] parseStoredValue(CharSequence val) {
            if (TextUtils.isEmpty(val)) {
                return null;
            } else {
                return val.toString().split(SEPARATOR);
            }
        }

    }

    public static class PowerWidgetOrder extends ListFragment
    {
        private static final String TAG = "PowerWidgetOrderActivity";

        private ListView mButtonList;
        private ButtonAdapter mButtonAdapter;
        View mContentView = null;
        Context mContext;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mContentView = inflater.inflate(R.layout.order_power_widget_buttons_activity, null);
            return mContentView;
        }

        /** Called when the activity is first created. */
        // @Override
        // public void onCreate(Bundle icicle)
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mContext = getActivity().getApplicationContext();

            mButtonList = getListView();
            ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
            mButtonAdapter = new ButtonAdapter(mContext);
            setListAdapter(mButtonAdapter);
        }

        @Override
        public void onDestroy() {
            ((TouchInterceptor) mButtonList).setDropListener(null);
            setListAdapter(null);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            // reload our buttons and invalidate the views for redraw
            mButtonAdapter.reloadButtons();
            mButtonList.invalidateViews();
        }

        private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> buttons = PowerWidgetUtil.getButtonListFromString(
                        PowerWidgetUtil.getCurrentButtons(mContext));

                // move the button
                if (from < buttons.size()) {
                    String button = buttons.remove(from);

                    if (to <= buttons.size()) {
                        buttons.add(to, button);

                        // save our buttons
                        PowerWidgetUtil.saveCurrentButtons(mContext,
                                PowerWidgetUtil.getButtonStringFromList(buttons));

                        // tell our adapter/listview to reload
                        mButtonAdapter.reloadButtons();
                        mButtonList.invalidateViews();
                    }
                }
            }
        };

        private class ButtonAdapter extends BaseAdapter {
            private Context mContext;
            private Resources mSystemUIResources = null;
            private LayoutInflater mInflater;
            private ArrayList<PowerWidgetUtil.ButtonInfo> mButtons;

            public ButtonAdapter(Context c) {
                mContext = c;
                mInflater = LayoutInflater.from(mContext);

                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    try {
                        mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                    } catch (Exception e) {
                        mSystemUIResources = null;
                        Log.e(TAG, "Could not load SystemUI resources", e);
                    }
                }

                reloadButtons();
            }

            public void reloadButtons() {
                ArrayList<String> buttons = PowerWidgetUtil.getButtonListFromString(
                        PowerWidgetUtil.getCurrentButtons(mContext));

                mButtons = new ArrayList<PowerWidgetUtil.ButtonInfo>();
                for (String button : buttons) {
                    if (PowerWidgetUtil.BUTTONS.containsKey(button)) {
                        mButtons.add(PowerWidgetUtil.BUTTONS.get(button));
                    }
                }
            }

            public int getCount() {
                return mButtons.size();
            }

            public Object getItem(int position) {
                return mButtons.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                final View v;
                if (convertView == null) {
                    v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                } else {
                    v = convertView;
                }

                PowerWidgetUtil.ButtonInfo button = mButtons.get(position);

                final TextView name = (TextView) v.findViewById(R.id.name);
                final ImageView icon = (ImageView) v.findViewById(R.id.icon);

                name.setText(button.getTitleResId());

                // assume no icon first
                icon.setVisibility(View.GONE);

                // attempt to load the icon for this button
                if (mSystemUIResources != null) {
                    int resId = mSystemUIResources.getIdentifier(button.getIcon(), null, null);
                    if (resId > 0) {
                        try {
                            Drawable d = mSystemUIResources.getDrawable(resId);
                            icon.setVisibility(View.VISIBLE);
                            icon.setImageDrawable(d);
                        } catch (Exception e) {
                            Log.e(TAG, "Error retrieving icon drawable", e);
                        }
                    }
                }

                return v;
            }
        }
    }

}
