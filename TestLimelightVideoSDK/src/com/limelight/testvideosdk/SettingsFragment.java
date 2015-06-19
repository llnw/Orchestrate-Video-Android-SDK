package com.limelight.testvideosdk;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import com.limelight.videosdk.LoggerUtil;
import com.limelight.videosdk.utility.Setting;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    // Using this array to map to log level to display a readable text
    private CharSequence mLogEntries[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loading the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        EditTextPreference orgIDEditnPref = (EditTextPreference) findPreference(getResources().getString(R.string.OrgIDEditPrefKey));
        if (orgIDEditnPref != null) {
            String strOrgIDPrefText = orgIDEditnPref.getText();
            if (strOrgIDPrefText != null)/* && !(strOrgIDPrefText.equals(""))) */{
                orgIDEditnPref.setSummary(strOrgIDPrefText);
            } else {
                orgIDEditnPref.setText(getResources().getString(R.string.OrgIDEditPrefDefValue));
                orgIDEditnPref.setSummary(R.string.OrgIDEditPrefDefValue);
            }
        }

        EditTextPreference accessKeyEditnPref = (EditTextPreference) findPreference(getResources()
                .getString(R.string.AccKeyEditPrefKey));
        if (accessKeyEditnPref != null) {
            String strAccessKeyPrefText = accessKeyEditnPref.getText();
            if (strAccessKeyPrefText != null)/* &&!(strAccessKeyPrefText.equals("")))*/{
                accessKeyEditnPref.setSummary(strAccessKeyPrefText);
            } else {
                accessKeyEditnPref.setText(getResources().getString(R.string.AccKeyEditPrefDefValue));
                accessKeyEditnPref.setSummary(getResources().getString(R.string.AccKeyEditPrefDefValue));
            }
        }

        EditTextPreference secretKeyEditnPref = (EditTextPreference) findPreference(getResources()
                .getString(R.string.SecKeyEditPrefKey));
        if (secretKeyEditnPref != null) {
            String strSecretKeyPrefText = secretKeyEditnPref.getText();
            if (strSecretKeyPrefText != null)/*&&!(strSecretKeyPrefText.equals(""))) */{
                secretKeyEditnPref.setSummary(strSecretKeyPrefText);
            } else {
                secretKeyEditnPref.setText(getResources().getString(R.string.SecKeyEditPrefDefValue));
                secretKeyEditnPref.setSummary(getResources().getString(R.string.SecKeyEditPrefDefValue));
            }
        }

        EditTextPreference licProxyEditnPref = (EditTextPreference) findPreference(getResources()
                .getString(R.string.licProxyEditPrefKey));
        if (licProxyEditnPref != null) {
            String strLicProxyPrefText = licProxyEditnPref.getText();
            if (strLicProxyPrefText != null)/* && !(strSecretKeyPrefText.equals("" )))*/{
                licProxyEditnPref.setSummary(strLicProxyPrefText);
            } else {
                licProxyEditnPref.setText(getResources().getString(R.string.licProxyEditPrefDefValue));
                licProxyEditnPref.setSummary(getResources().getString(R.string.licProxyEditPrefDefValue));
            }
        }

        EditTextPreference portalKeyEditnPref = (EditTextPreference) findPreference(getResources()
                .getString(R.string.portalKeyEditPrefKey));
        if (portalKeyEditnPref != null) {
            String strPortalKeyPrefText = portalKeyEditnPref.getText();
            if (strPortalKeyPrefText != null)/* &&!(strSecretKeyPrefText.equals( "")))*/{
                portalKeyEditnPref.setSummary(strPortalKeyPrefText);
            } else {
                portalKeyEditnPref.setText(getResources().getString(R.string.portalKeyEditPrefDefValue));
                portalKeyEditnPref.setSummary(getResources().getString(R.string.portalKeyEditPrefDefValue));
            }
        }

        EditTextPreference urlEditnPref = (EditTextPreference) findPreference(getResources()
                .getString(R.string.urlEditPrefKey));
        if (urlEditnPref != null) {
            String strurlPrefText = urlEditnPref.getText();
            if (strurlPrefText != null)/* && !(strurlPrefText.equals(""))) */{
                urlEditnPref.setSummary(strurlPrefText);
            } else {
                urlEditnPref.setText(getResources().getString(R.string.urlEditPrefDefValue));
                urlEditnPref.setSummary(getResources().getString(R.string.urlEditPrefDefValue));
            }
        }

        ListPreference logListPref = (ListPreference) findPreference(getResources().getString(R.string.logLevelListPrefKey));

        if (logListPref != null) {

            // Fetching the log levels from SDK and displaying
            String[] levelItems = LoggerUtil.getAllLogLevels();
            mLogEntries = new String[levelItems.length];
            CharSequence entryValues[] = new String[levelItems.length];
            int i = 0;
            for (String level : levelItems) {
                mLogEntries[i] = level;
                entryValues[i] = Integer.toString(i);
                i++;
            }
            logListPref.setEntries(mLogEntries);
            logListPref.setEntryValues(entryValues);

            String strLogListPrefText = logListPref.getValue();
            if (strLogListPrefText != null) {

                int logLevelNum = 0;
                try {
                    logLevelNum = Integer.parseInt(strLogListPrefText);
                    logListPref.setSummary(mLogEntries[logLevelNum]);
                } catch (NumberFormatException nfe) {
                    // TODO: Log error info
                }
            } else {
                logListPref.setValue(getResources().getString(
                        R.string.logLevelListPrefDefValue));
            }
        }
        EditTextPreference analyticsEditnPref = (EditTextPreference) findPreference(getResources()
                .getString(R.string.analyticsEditPrefKey));
        if (analyticsEditnPref != null) {
            String analyticsPrefText = analyticsEditnPref.getText();
            if (analyticsPrefText != null){
                analyticsEditnPref.setSummary(analyticsPrefText);
            } else {
                analyticsEditnPref.setText(getResources().getString(R.string.analyticsEditPrefDefValue));
                analyticsEditnPref.setSummary(getResources().getString(R.string.analyticsEditPrefDefValue));
            }
        }
        updateValues();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        //Verifying if the fragment is currently added to its activity or not, getResources crashes when control reaches here and isAdded is false.
        if(isAdded()){
            if (key.equals(getResources().getString(R.string.OrgIDEditPrefKey))) {
                EditTextPreference connectionPref = (EditTextPreference) findPreference(key);
                connectionPref.setSummary(sharedPreferences.getString(key,getResources().getString(R.string.OrgIDEditPrefDefValue)));
            } else if (key.equals(getResources().getString( R.string.AccKeyEditPrefKey))) {
                EditTextPreference connectionPref = (EditTextPreference) findPreference(key);
                connectionPref.setSummary(sharedPreferences.getString(key,getResources().getString(R.string.AccKeyEditPrefDefValue)));
            } else if (key.equals(getResources().getString(R.string.SecKeyEditPrefKey))) {
                EditTextPreference connectionPref = (EditTextPreference) findPreference(key);
                connectionPref.setSummary(sharedPreferences.getString(key,getResources().getString(R.string.SecKeyEditPrefDefValue)));
            } else if (key.equals(getResources().getString(R.string.urlEditPrefKey))) {
                EditTextPreference connectionPref = (EditTextPreference) findPreference(key);
                connectionPref.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.urlEditPrefDefValue)));
            }else if (key.equals(getResources().getString(R.string.licProxyEditPrefKey))) {
                EditTextPreference connectionPref = (EditTextPreference) findPreference(key);
                connectionPref.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.licProxyEditPrefDefValue)));
            }else if (key.equals(getResources().getString(R.string.portalKeyEditPrefKey))) {
                EditTextPreference connectionPref = (EditTextPreference) findPreference(key);
                connectionPref.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.portalKeyEditPrefDefValue)));
            }else if (key.equals(getResources().getString( R.string.logLevelListPrefKey))) {
                ListPreference logListPref1 = (ListPreference) findPreference(key);
                int logLevelNum = 0;
                String strLogListPrefText = sharedPreferences.getString(key,getResources().getString(R.string.logLevelListPrefDefValue));
                logLevelNum = Integer.parseInt(strLogListPrefText);
                logListPref1.setSummary(mLogEntries[logLevelNum]);
            }
            else if (key.equals(getResources().getString(R.string.analyticsEditPrefKey))) {
                EditTextPreference analyticsEditPref = (EditTextPreference) findPreference(key);
                analyticsEditPref.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.analyticsEditPrefDefValue)));
            }
            //update the values in SDK
            updateValues();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        updateValues();
    }
    @Override
    public void onPause() {
        super.onPause();
        updateValues();
    }
    
    private void updateValues(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String apiEndPoint = preferences.getString(getResources().getString(R.string.urlEditPrefKey), null);
        String portalKey = preferences.getString(getResources().getString(R.string.portalKeyEditPrefKey), null);
        String licenseProxy = preferences.getString(getResources().getString(R.string.licProxyEditPrefKey), null);
        Setting.configureLimelightSettings(apiEndPoint, licenseProxy, portalKey);
        String logLevel = preferences.getString(getResources().getString(R.string.logLevelListPrefKey),null);
        if(logLevel != null && !logLevel.isEmpty()){
            int level = Integer.parseInt(logLevel);
            LoggerUtil.setLogLevelByString(mLogEntries[level].toString(),SettingsFragment.this.getActivity());
        }
        String analyticsPrefText = preferences.getString(getResources().getString(R.string.analyticsEditPrefKey), null);
        Setting.SetAnalyticsEndPoint(analyticsPrefText);
    }
}