package com.tvblob.fandango.myblobbox;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tvblob.fandango.argo.ArgoAuthenticationException;
import com.tvblob.fandango.argo.ArgoClient;
import com.tvblob.fandango.argo.ArgoCommunicationException;
import com.tvblob.fandango.argo.IncompatibleRemoteDeviceException;
import com.tvblob.fandango.argo.IncompatibleSoftwareVersionException;

/**
 * Preferences for blobbox activities - IP address, username and password 
 * 
 * @author Rodolfo Saccani, Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class Configure extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String BLOBBOX_URL = "http://www.blobbox.tv/?s=fandango";

	private static final int CONFIG_OK = 0;
	private static final int CONFIG_AUTH_FAIL = 1;
	private static final int CONFIG_REMOTE_FAIL = 2;
	private static final int CONFIG_VERSION_FAIL = 3;
	private static final int CONFIG_COMMS_FAIL = 4;

	/**
	 * {@link Handler} to display {@link Toast} following termination of 
	 * preference validation. 
	 */
	private final class ConfigValidationHandler extends Handler {
		private final String ipAddress;

		/**
		 * PACKAGE PRIVATE
		 * 
		 * @param ipAddress
		 */
		ConfigValidationHandler(final String ipAddress) {
			this.ipAddress = ipAddress;
		}

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case CONFIG_OK:
				showMessage(getOKMessage(ipAddress));
				break;

			case CONFIG_AUTH_FAIL:
				showError(getAuthError(ipAddress));
				break;

			case CONFIG_COMMS_FAIL:
				showError(getCommsError(ipAddress));

				break;

			case CONFIG_REMOTE_FAIL:
				showError(getRemoteError(ipAddress));

				break;

			case CONFIG_VERSION_FAIL:
				showError(getVersionError(ipAddress));

				break;

			default:
				showError("Unexpected code: " + msg.what);

			}
		}
	}

	private Handler handler = null;

	public void onResume() {
		super.onResume();

		final String ip = PreferenceManager.getDefaultSharedPreferences(
				getBaseContext()).getString(IPreferenceConstants.IP_PREF, "");

		// IP address field is empty, let's prompt the user for more information about BLOBbox
		if (ip.length() == 0) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.what_is_blobbox)
					.setPositiveButton(R.string.tellme,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {
									// User wants to know more about BLOBbox
									final Uri blobboxUrl = Uri
											.parse(BLOBBOX_URL);
									final Intent launchBrowser = new Intent(
											Intent.ACTION_VIEW, blobboxUrl);
									startActivity(launchBrowser);
								}
							})
					.setNegativeButton(R.string.no_thanks,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int whichButton) {
									// User doesn't want to be bothered, do nothing
								}
							}).show();
		}
	}

	/* Options menu: Only the "Info" item */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(getString(R.string.info_button));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {

		case 0:
			final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					this);
			alertDialog.setTitle(getString(R.string.info_title));
			alertDialog.setMessage(getString(R.string.info_text));
			alertDialog.setNeutralButton(
					getString(R.string.info_dismiss_button),
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							return;
						}
					});
			alertDialog.show();
			break;

		default:
			break;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Get the custom preference
		final Preference blobbox_ip = findPreference(IPreferenceConstants.IP_PREF);

		blobbox_ip
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(final Preference preference) {
						final SharedPreferences customSharedPreference = getSharedPreferences(
								"myCustomSharedPrefs", Context.MODE_PRIVATE);
						final SharedPreferences.Editor editor = customSharedPreference
								.edit();
						editor.commit();
						return true;
					}

				});

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	public void onSharedPreferenceChanged(
			final SharedPreferences sharedPreferences, final String key) {
		final String ipAddress = sharedPreferences.getString(
				IPreferenceConstants.IP_PREF, null);

		final String userName = sharedPreferences.getString(
				IPreferenceConstants.USERNAME_PREF, null);

		final String password = sharedPreferences.getString(
				IPreferenceConstants.PASSWORD_PREF, null);

		handler = new ConfigValidationHandler(ipAddress);

		final ProgressDialog pd = ProgressDialog.show(this,
				getString(R.string.msg_title_progressdialog),
				getString(R.string.msg_content_progressdialog), true);

		new Thread() {
			public void run() {
				try {
					checkPrefs(ipAddress, userName, password);
				} finally {
					pd.dismiss();
				}
			}
		}.start();

	}

	/**
	 * PACKAGE PRIVATE - used by inner class
	 * 
	 * @param ipAddress
	 * @param userName
	 * @param password
	 */
	void checkPrefs(final String ipAddress, final String userName,
			final String password) {
		try {
			new ArgoClient(ipAddress, userName, password);
			handler.sendEmptyMessage(CONFIG_OK);
		} catch (final IncompatibleRemoteDeviceException exception) {
			handler.sendEmptyMessage(CONFIG_REMOTE_FAIL);
		} catch (final IncompatibleSoftwareVersionException exception) {
			handler.sendEmptyMessage(CONFIG_VERSION_FAIL);
		} catch (final ArgoCommunicationException exception) {
			handler.sendEmptyMessage(CONFIG_COMMS_FAIL);
		} catch (final ArgoAuthenticationException exception) {
			handler.sendEmptyMessage(CONFIG_AUTH_FAIL);
		}
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getVersionError(final String ipAddress) {
		return String.format(getString(R.string.prefs_error_sw_ver), ipAddress);
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getRemoteError(final String ipAddress) {
		return String.format(getString(R.string.prefs_error_not_blobbox),
				ipAddress);
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getCommsError(final String ipAddress) {
		return String.format(getString(R.string.prefs_error_not_reachable),
				ipAddress);
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	String getAuthError(final String ipAddress) {
		return String.format(getString(R.string.prefs_error_auth), ipAddress);
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getOKMessage(final String ipAddress) {
		return String.format(getString(R.string.prefs_ok), ipAddress);
	}

	/**
	 * PACKAGE PRIVATE - used by handler
	 * 
	 * @param error
	 */
	void showError(final String error) {
		Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
	}

	/**
	 * PACKAGE PRIVATE - used by handler
	 * 
	 * @param message
	 */
	void showMessage(final String message) {
		Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
	}

}
