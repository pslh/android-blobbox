package com.tvblob.fandango.myblobbox;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tvblob.fandango.argo.ArgoAuthenticationException;
import com.tvblob.fandango.argo.ArgoClient;
import com.tvblob.fandango.argo.ArgoCommunicationException;
import com.tvblob.fandango.argo.IncompatibleRemoteDeviceException;
import com.tvblob.fandango.argo.IncompatibleSoftwareVersionException;
import com.tvblob.fandango.ssdp.SSDP;
import com.tvblob.fandango.ssdp.UPnPDevice;

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

	private Handler handler = null;

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

		final String ip = PreferenceManager.getDefaultSharedPreferences(
				getBaseContext()).getString(Constants.IP_PREF, "");

		// IP address field is empty, let's prompt the user for more information about BLOBbox
		if (ip.length() == 0) {
			guessBoxIP();
			//				showWhatIsDialog();
		}
	}

	/**
	 * 
	 */
	void showWhatIsDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.what_is_blobbox)
				.setPositiveButton(R.string.tellme,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int whichButton) {
								// User wants to know more about BLOBbox
								final Uri blobboxUrl = Uri.parse(BLOBBOX_URL);
								final Intent launchBrowser = new Intent(
										Intent.ACTION_VIEW, blobboxUrl);
								startActivity(launchBrowser);
							}
						})
				.setNegativeButton(R.string.no_thanks,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int whichButton) {
								// User doesn't want to be bothered, do nothing
							}
						}).show();
	}

	/**
	 * @param scanHandler
	 */
	private void guessBoxIP() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.msg_title_progressdialog),
				getString(R.string.scanning), true);

		final Handler scanHandler = new Handler() {
			
			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			public void handleMessage(final Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case Constants.OPERATION_OK:
					break;

				case Constants.NO_DEVICE_FOUND:
					// Intentionally fall through
				case Constants.COMMUNICATIONS_FAILED:
					showWhatIsDialog();
					break;

				default:
					Log.e(Constants.TAG,
							"guessBoxIP.handleMessage: unnexpected code: "
									+ msg.what);
					showWhatIsDialog();
					break;
				}
			}
		};

		new Thread() {
			public void run() {
				scanForDevices(dialog, scanHandler);
			}
		}.start();
	}

	/**
	 * @param dialog
	 * @param scanHandler
	 */
	void scanForDevices(final ProgressDialog dialog, final Handler scanHandler) {
		try {
			final List<UPnPDevice> boxes = SSDP.findBlobboxDevices();
			System.out.println("BOXES=" + boxes);
			Log.d(Constants.TAG, "BOXES=" + boxes);
			if (boxes.size() == 0) {
				scanHandler.sendEmptyMessage(Constants.NO_DEVICE_FOUND);
			} else {
				/*
				 * TODO check for > 1 box, display list or something
				 * For now, just use first in list - most users will
				 * have only one BLOBbox.
				 */
				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				final Editor edit = prefs.edit();
				edit.putString(Constants.IP_PREF, boxes.iterator().next()
						.getHost());
				edit.commit();
				scanHandler.sendEmptyMessage(Constants.OPERATION_OK);
			}
		} catch (final IOException exception) {
			Log.w(Constants.TAG, "Failed to scan for BLOBbox devices");
			exception.printStackTrace();
			scanHandler.sendEmptyMessage(Constants.COMMUNICATIONS_FAILED);
		} finally {
			dialog.dismiss();
		}
	}

	/* Options menu: Only the "Info" item */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(getString(R.string.info_button));
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {

		case 0:
			final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					this);
			alertDialog.setTitle(getString(R.string.info_title));
			alertDialog.setMessage(String.format(getString(R.string.info_text),
					getVersion()));
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

	/**
	 * @return
	 * @throws NameNotFoundException
	 */
	protected String getVersion() {
		try {
			return getPackageInfo().versionName;
		} catch (final NameNotFoundException exception) {
			if (Constants.DEBUG) {
				Log.e(Constants.TAG, "getVersion", exception);
			}
			return "unknown";
		}
	}

	/**
	 * @return
	 * @throws NameNotFoundException
	 */
	protected PackageInfo getPackageInfo() throws NameNotFoundException {
		return getPackageManager().getPackageInfo(getPackageName(), 0);
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Get the custom preference
		final Preference blobbox_ip = findPreference(Constants.IP_PREF);

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

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	public void onSharedPreferenceChanged(
			final SharedPreferences sharedPreferences, final String key) {
		final String ipAddress = sharedPreferences.getString(Constants.IP_PREF,
				null);

		final String userName = sharedPreferences.getString(
				Constants.USERNAME_PREF, null);

		final String password = sharedPreferences.getString(
				Constants.PASSWORD_PREF, null);

		//		handler = new ConfigValidationHandler(ipAddress, getBaseContext());
		handler = new ValidatePreferencesArgoOperationHandler(ipAddress, this);

		final ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.msg_title_progressdialog),
				getString(R.string.msg_content_progressdialog), true);

		new Thread() {
			public void run() {
				try {
					checkPrefs(ipAddress, userName, password);
				} finally {
					dialog.dismiss();
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
			handler.sendEmptyMessage(Constants.OPERATION_OK);
		} catch (final IncompatibleRemoteDeviceException exception) {
			handler.sendEmptyMessage(Constants.INCOMPATIBLE_DEVICE);
		} catch (final IncompatibleSoftwareVersionException exception) {
			handler.sendEmptyMessage(Constants.UNSUPPORTED_VERSION);
		} catch (final ArgoCommunicationException exception) {
			handler.sendEmptyMessage(Constants.COMMUNICATIONS_FAILED);
		} catch (final ArgoAuthenticationException exception) {
			handler.sendEmptyMessage(Constants.AUTHENTICATION_FAILED);
		}
	}

}
