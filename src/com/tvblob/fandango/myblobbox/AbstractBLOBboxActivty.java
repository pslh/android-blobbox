package com.tvblob.fandango.myblobbox;

import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import org.alexd.jsonrpc.JSONRPCException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.tvblob.fandango.argo.ArgoClient;
import com.tvblob.fandango.argo.ArgoException;

/**
 * Abstract base for {@link Activity} classes which use the BLOBbox 
 * 
 * @author Paul Henshaw
 * @created Sep 8, 2011
 * @cvsid $Id$
 */
public abstract class AbstractBLOBboxActivty extends Activity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	// Debugging 
	protected static final String TAG = "BLOBbox";
	protected static final boolean DEBUG = true;

	private static final int OPERATION_OK = 0;
	private static final int OPERATION_FAILED = 1;
	private static final int UNSUPPORTED_INTENT = 2;

	private Handler handler = null;

	/**
	 * Subclasses must implement this method to perform the appropriate 
	 * operation for intent (probably using client).  E.g. a Play activity
	 * might call client.play... 
	 * 
	 * @param text_content
	 * @param intent
	 * @param client
	 * @throws JSONRPCException
	 * @throws ArgoException 
	 */
	protected abstract void performOperation(final Intent intent,
			final ArgoClient client) throws ArgoUnsupportedIntentException,
			ArgoException;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Progress dialog: handler must check operation status and then finish
		handler = new Handler() {

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			public void handleMessage(final Message msg) {
				switch (msg.what) {
				case OPERATION_OK:
					Toast.makeText(getApplicationContext(),
							getString(R.string.msg_completed_ok),
							Toast.LENGTH_LONG).show();
					break;

				case OPERATION_FAILED:
					Toast.makeText(getApplicationContext(),
							getString(R.string.msg_errore_toast),
							Toast.LENGTH_LONG).show();

					break;

				case UNSUPPORTED_INTENT:
					Toast.makeText(getApplicationContext(),
							getString(R.string.msg_unsupported_intent),
							Toast.LENGTH_LONG).show();

					break;

				default:
					Toast.makeText(
							getApplicationContext(),
							getString(R.string.msg_unexpected_op_code) + " "
									+ msg.what, Toast.LENGTH_LONG).show();

				}
				finish();
			}
		};

		// Android intent handling
		final Intent intent = getIntent();

		final ProgressDialog pd = ProgressDialog.show(this,
				getString(R.string.msg_title_progressdialog),
				getString(R.string.msg_content_progressdialog), true);

		new Thread() {

			@Override
			public void run() {

				try {
					setupClientAndPerformOperation(intent);
				} catch (final Exception exception) {
					exception.printStackTrace();
					if (DEBUG) {
						Log.e(TAG, "onCreate.run: " + exception.toString());
					}
				} finally {
					pd.dismiss();
				}

			}
		}.start();
	}

	/**
	 * PACKAGE PRIVATE - used by inner class
	 * IMPORTANT - this code runs in a separate thread and may NOT use the
	 * UI, use {@link Handler#sendEmptyMessage(int)} to communicate operation
	 * status
	 * 
	 * @param intent
	 */
	void setupClientAndPerformOperation(final Intent intent) {
		if (DEBUG) {
			Log.i(TAG, "action: " + intent.getAction());
			Log.i(TAG, "type: " + intent.getType());
		}

		try {
			final ArgoClient client = new ArgoClient(getIP(), getUserName(),
					getPassword());
			performOperation(intent, client);
			handler.sendEmptyMessage(OPERATION_OK);
		} catch (final ArgoUnsupportedIntentException exception) {
			exception.printStackTrace();
			handler.sendEmptyMessage(UNSUPPORTED_INTENT);
			if (DEBUG) {
				Log.e(TAG, exception.toString());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			handler.sendEmptyMessage(OPERATION_FAILED);
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
		}
	}

	/**
	 * The password saved in preferences
	 * 
	 * @return String
	 */
	protected String getPassword() {
		return PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getString(IPreferenceConstants.PASSWORD_PREF, "");
	}

	/**
	 * The username saved in preferences
	 * 
	 * @param prefs
	 * @return String
	 */
	protected String getUserName() {
		return PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getString(IPreferenceConstants.USERNAME_PREF, "");
	}

	/**
	 * The BLOBbox IP saved in preferences
	 * 
	 * @return Stirng
	 */
	protected String getIP() {
		return PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getString(IPreferenceConstants.IP_PREF, "");
	}

	/**
	 * The URL for the given intent, handles both direct and "send to" 
	 * situations.
	 * 
	 * @param intent
	 * @param extras
	 * @return String
	 */
	protected String getURL(final Intent intent) {

		final String dataString = intent.getDataString();

		if (dataString == null) {
			final Bundle extras = intent.getExtras();
			if (extras == null) {
				if (DEBUG) {
					Log.i(TAG, "getURL: no data and no extras in intent "
							+ intent.toString());
					return null;
				}
			}
			return extras.getString(Intent.EXTRA_TEXT);
		}
		return dataString;

		//		return extras == null ? dataString : extras
		//				.getString(Intent.EXTRA_TEXT);
	}

	/**
	 * @param intent
	 * @return
	 * @throws ArgoUnsupportedIntentException
	 */
	protected String getNonNullURL(final Intent intent)
			throws ArgoUnsupportedIntentException {
		final String url = getURL(intent);
		if (url == null) {
			throw new ArgoUnsupportedIntentException(intent);
		}
		return url;
	}

	/**
	 * @param intent
	 * @param url
	 * @return String
	 */
	public static String getMimeType(final Intent intent, final String url) {
		/*
		 * If no EXTRAs we can trust the mime type in Intent
		 */
		if (intent.getExtras() == null) {
			if (DEBUG) {
				Log.i(TAG, "No Extras, using intent type " + intent.getType());
			}
			return intent.getType();
		}

		/*
		 * Intent type is probably not reliable, type MimeTypeMap
		 */
		final String mimeTypeFromExtension = MimeTypeMap.getSingleton()
				.getMimeTypeFromExtension(
						MimeTypeMap.getFileExtensionFromUrl(url));
		if (mimeTypeFromExtension != null) {
			if (DEBUG) {
				Log.i(TAG, "mime from map " + mimeTypeFromExtension);
			}
			return mimeTypeFromExtension;
		}

		/*
		 * Try URLConnection.guessContentTypeFromName
		 */
		final String guessed = URLConnection.guessContentTypeFromName(url);
		if (guessed != null) {
			if (DEBUG) {
				Log.i(TAG, "guessed type = " + guessed);
			}
			return guessed;
		}

		/*
		 * No luck, check for .torrent, and otherwise use intent
		 */
		final String lastguess = url.endsWith(".torrent") ? "application/x-bittorrent"
				: intent.getType();
		if (DEBUG) {
			Log.i(TAG, "couldn't guess, using mime type = " + lastguess);
		}
		return lastguess;
	}

	/**
	 * Return a URL to pass to BLOBbox browser bloblet in order to play the
	 * given YouTube url.  Returns null if url is NOT a youtube watch link.
	 * 
	 * @param url
	 * @return String
	 */
	public static String getYouTubeURL(final String url) {
		/*
		 * YouTube link
		 *  http://www.youtube.com/watch?v=25AuClGJB0M&feature=topvideos_music
		 *  Browser link
		 *   http://tvportal.tvblob.com/apps/youtube/app.php?vid=0IP6e9WbaiU
		 */

		try {
			final URL theURL = new URL(url);

			if (DEBUG) {
				Log.i(TAG, "host = " + theURL.getHost());
			}

			if (!"www.youtube.com".equals(theURL.getHost())
					&& !"m.youtube.com".equals(theURL.getHost())) {
				return null;
			}
			if (DEBUG) {
				Log.i(TAG, "path = " + theURL.getPath());
			}

			if (!"/watch".equals(theURL.getPath())) {
				return null;
			}

			if (DEBUG) {
				Log.i(TAG, "query = " + theURL.getQuery());
			}

			final StringTokenizer tokenizer = new StringTokenizer(
					theURL.getQuery(), "&=");
			while (tokenizer.hasMoreTokens()) {
				final String name = tokenizer.nextToken();
				final String value = tokenizer.hasMoreTokens() ? tokenizer
						.nextToken() : "";

				if (DEBUG) {
					Log.i(TAG, "QUERY: " + name + "==" + value);
				}

				if ("v".equals(name)) {
					return "http://tvportal.tvblob.com/apps/youtube/app.php?vid="
							+ value;
				}
			}

			if (DEBUG) {
				Log.i(TAG, "!!! failed to find v in query");
			}

			return null;

		} catch (final Exception exception) {
			return null;
		}
	}
}
