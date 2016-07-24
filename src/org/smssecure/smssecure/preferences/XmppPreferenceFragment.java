package org.smssecure.smssecure.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.smssecure.smssecure.ApplicationPreferencesActivity;
import org.smssecure.smssecure.components.SwitchPreferenceCompat;
import org.smssecure.smssecure.R;
import org.smssecure.smssecure.service.XmppService;
import org.smssecure.smssecure.util.SilencePreferences;
import org.smssecure.smssecure.util.XmppUtil;
import org.smssecure.smssecure.XmppRegisterActivity;

public class XmppPreferenceFragment extends PreferenceFragment {

  private static final String TAG = XmppPreferenceFragment.class.getSimpleName();

  private static final int REGISTERING_ACTIVITY_RESULT_CODE = 666;

  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    addPreferencesFromResource(R.xml.preferences_xmpp);

    setXmppUiSettings();

    findPreference(SilencePreferences.XMPP_ENABLED_PREF)
        .setOnPreferenceChangeListener(new RegisterXmppListener());

  }

  private void setXmppUiSettings() {
    if (SilencePreferences.isXmppRegistered(getActivity())) {
      findPreference(SilencePreferences.XMPP_STATUS)
          .setSummary(R.string.preferences__xmpp_status_registered);
    } else {
      findPreference(SilencePreferences.XMPP_STATUS)
          .setSummary(R.string.preferences__xmpp_status_unregistered);
      ((SwitchPreferenceCompat) findPreference(SilencePreferences.XMPP_ENABLED_PREF))
          .setChecked(false);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity)getActivity()).getSupportActionBar().setTitle(R.string.preferences__xmpp);
  }

  private class RegisterXmppListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      final Context context = (Context) getActivity();

      if (!SilencePreferences.isXmppRegistered(context)) {
        startActivityForResult(new Intent(getActivity(), XmppRegisterActivity.class), REGISTERING_ACTIVITY_RESULT_CODE);
      } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.preferences__xmpp_unregistering_xmpp);
        builder.setMessage(R.string.preferences__xmpp_unregistering_from_xmpp_will_delete_the_account_on_the_server);
        builder.setIconAttribute(R.attr.dialog_alert_icon);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            ((SwitchPreferenceCompat) findPreference(SilencePreferences.XMPP_ENABLED_PREF)).setChecked(true);
          }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            XmppUtil.sendNullXmppMessage(context);
            XmppService.getInstance().deleteAccount();
            findPreference(SilencePreferences.XMPP_STATUS).setSummary(R.string.preferences__xmpp_status_unregistered);
            new Thread() {
              @Override
              public void run() {
                Looper.prepare();
                Toast.makeText(context.getApplicationContext(),
                               context.getString(R.string.preferences__xmpp_status_unregistered),
                               Toast.LENGTH_LONG).show();
                Looper.loop();
              }
            }.start();
          }
        });
        builder.show();
      }
      return true;
    }
  }

  public static CharSequence getSummary(Context context) {
    if (SilencePreferences.isXmppRegistered(context)) {
      return context.getString(R.string.preferences__xmpp_status_registered);
    } else {
      return context.getString(R.string.preferences__xmpp_status_unregistered);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REGISTERING_ACTIVITY_RESULT_CODE) {
      setXmppUiSettings();
    }
  }
}
