/*
 * Copyright (C) 2013 The JellyKang Project
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

package com.android.settings.kang;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

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
import android.os.SystemProperties;
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
import android.view.Display;
import android.view.Window;
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
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AboutKang extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AboutKang";

    private static final String KEY_KANG_VERSION = "kang_version";

    private PreferenceScreen mGitUrl;
    private PreferenceScreen mXdaUrl;
    private PreferenceScreen mGooUrl;
    private PreferenceScreen mCredMainUrl;
    private PreferenceScreen mCred1Url;
    private PreferenceScreen mCred2Url;
    private PreferenceScreen mCred3Url;
    private PreferenceScreen mCred4Url;
    private PreferenceScreen mCred5Url;
    private PreferenceScreen mCred6Url;
    private PreferenceScreen mCred7Url;
    private PreferenceScreen mCred8Url;
    private PreferenceScreen mCred9Url;
    private PreferenceScreen mCred10Url;
    private PreferenceScreen mCred11Url;
    private PreferenceScreen mCred12Url;


    private Activity mActivity;

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	
	addPreferencesFromResource(R.xml.about_kang);

        PreferenceScreen prefSet = getPreferenceScreen();
        mActivity = getActivity();

  	setValueSummary(KEY_KANG_VERSION, "ro.kang.version");

        mGitUrl = (PreferenceScreen) findPreference("github_kang_source");
        mXdaUrl = (PreferenceScreen) findPreference("xda_kang_source");
        mGooUrl = (PreferenceScreen) findPreference("goo_kang_source");
        mCredMainUrl = (PreferenceScreen) findPreference("kang_credmain");
        mCred1Url = (PreferenceScreen) findPreference("kang_cred1");
        mCred2Url = (PreferenceScreen) findPreference("kang_cred2");
        mCred3Url = (PreferenceScreen) findPreference("kang_cred3");
        mCred4Url = (PreferenceScreen) findPreference("kang_cred4");
        mCred5Url = (PreferenceScreen) findPreference("kang_cred5");
        mCred6Url = (PreferenceScreen) findPreference("kang_cred6");
        mCred7Url = (PreferenceScreen) findPreference("kang_cred7");
        mCred8Url = (PreferenceScreen) findPreference("kang_cred8");
        mCred9Url = (PreferenceScreen) findPreference("kang_cred9");
        mCred10Url = (PreferenceScreen) findPreference("kang_cred10");
        mCred11Url = (PreferenceScreen) findPreference("kang_cred11");
        mCred12Url = (PreferenceScreen) findPreference("kang_cred12");

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    	if (preference == mGitUrl) {
            launchUrl("http://github.com/JellyKang");
	    Toast.makeText(getActivity(), "Github Source", Toast.LENGTH_LONG).show();
	    return true;
        } else if (preference == mXdaUrl) {
            launchUrl("http://forum.xda-developers.com/showthread.php?t=2192566");
	    Toast.makeText(getActivity(), "XDA Thread", Toast.LENGTH_LONG).show();
	    return true;
        } else if (preference == mGooUrl) {
            launchUrl("http://goo.im/devs/xmcwildchild22/Roms/Kang/");
	    Toast.makeText(getActivity(), "Goo Hosting", Toast.LENGTH_LONG).show();
	    return true;
        } else if (preference == mCredMainUrl) {
            launchUrl("http://forum.xda-developers.com/member.php?u=4193353");
	    return true;
        } else if (preference == mCred1Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=2878915");
	    return true;
        } else if (preference == mCred2Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=1282428");
	    return true;
        } else if (preference == mCred3Url) {
            launchUrl("http://www.cyanogenmod.org/");
	    return true;
        } else if (preference == mCred4Url) {
            launchUrl("https://www.codeaurora.org/");
	    return true;
        } else if (preference == mCred5Url) {
            launchUrl("https://github.com/team-hydra/");
	    return true;
        } else if (preference == mCred6Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=1617770");
	    return true;
        } else if (preference == mCred7Url) {
            launchUrl("http://evervolv.com/");
	    return true;
        } else if (preference == mCred8Url) {
            launchUrl("https://github.com/liquidsmooth");
	    return true;
        } else if (preference == mCred9Url) {
            launchUrl("https://github.com/carbondev");
	    return true;
        } else if (preference == mCred10Url) {
            launchUrl("http://www.slimroms.net/");
	    return true;
        } else if (preference == mCred11Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=4640360");
	    return true;
        } else if (preference == mCred12Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=4217237");
	    return true;
	}
	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {
            // No recovery
        }
    }
     
}
