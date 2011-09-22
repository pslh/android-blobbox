package com.tvblob.fandango.myblobbox;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import org.alexd.jsonrpc.JSONRPCException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.tvblob.fandango.argo.ArgoAuthenticationException;
import com.tvblob.fandango.argo.ArgoClient;
import com.tvblob.fandango.argo.ArgoCommunicationException;
import com.tvblob.fandango.argo.ArgoException;
import com.tvblob.fandango.argo.IncompatibleRemoteDeviceException;
import com.tvblob.fandango.argo.IncompatibleSoftwareVersionException;

/**
 * Abstract base for {@link Activity} classes which use the BLOBbox 
 * 
 * @author Paul Henshaw
 * @created Sep 8, 2011
 * @cvsid $Id$
 */
public abstract class AbstractBLOBboxActivty extends Activity {
	private static final String YOUTUBE_PLAY_URL = "http://tvportal.tvblob.com/apps/youtube/app.php?vid=";

	// Debugging 
	protected static final String TAG = "BLOBbox";
	protected static final boolean DEBUG = true;

	private static final int OPERATION_OK = 0;
	private static final int OPERATION_FAILED = 1;
	private static final int UNSUPPORTED_INTENT = 2;
	private static final int VERSION_FAIL = 3;

	private static final int AUTH_FAIL = 4;
	private static final int REMOTE_FAIL = 5;
	private static final int COMMS_FAIL = 6;

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
				final Context context = getApplicationContext();

				switch (msg.what) {
				case OPERATION_OK:
					showOK(context);
					break;

				case OPERATION_FAILED:
					showOpFailedError(context);

					break;

				case UNSUPPORTED_INTENT:
					showUnsupportedIntentError(context);

					break;

				case VERSION_FAIL:
					showVersionError(context);

					break;

				case AUTH_FAIL:
					showError(getAuthError(getIP()));
					break;

				case COMMS_FAIL:
					showError(getCommsError(getIP()));

					break;

				case REMOTE_FAIL:
					showError(getRemoteError(getIP()));

					break;

				default:
					showUnexpectedCodeError(msg, context);

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
		} catch (final IncompatibleRemoteDeviceException exception) {
			handler.sendEmptyMessage(REMOTE_FAIL);
		} catch (final IncompatibleSoftwareVersionException exception) {
			handler.sendEmptyMessage(VERSION_FAIL);
		} catch (final ArgoCommunicationException exception) {
			handler.sendEmptyMessage(COMMS_FAIL);
		} catch (final ArgoAuthenticationException exception) {
			handler.sendEmptyMessage(AUTH_FAIL);
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
	 * Return the YouTube video id of the film referred to by url.
	 * Returns null if url is NOT a youtube watch link.
	 * 
	 * @param url
	 * @return String
	 */
	public static String getYouTubeVideoID(final String url) {
		/*
		 * YouTube link
		 *  http://www.youtube.com/watch?v=25AuClGJB0M&feature=topvideos_music
		 *  Browser link
		 *   http://tvportal.tvblob.com/apps/youtube/app.php?vid=0IP6e9WbaiU
		 */

		try {
			if (!isYouTubeHost(url)) {
				return null;
			}

			final String query = extractQuery(url);
			if (query == null) {
				return null;
			}
			final String videoID = findVideoIDInQuery(query);

			if (videoID == null) {
				return null;
			}
			return videoID;
		} catch (final Exception exception) {
			return null;
		}
	}

	/**
	 * True iff url host in the youtube domain
	 * 
	 * @param url
	 * @return boolean
	 * @throws MalformedURLException 
	 */
	public static boolean isYouTubeHost(final String url)
			throws MalformedURLException {
		return new URL(url).getHost().endsWith(".youtube.com");
	}

	/**
	 * URL to play YouTube video or null if NOT a youtube url
	 * 
	 * @param url
	 * @return String
	 */
	public static String getYouTubeURL(final String url) {
		final String id = getYouTubeVideoID(url);
		return id == null ? null : YOUTUBE_PLAY_URL + id;
	}

	/**
	 * Extract the query part of the url NOTE we do NOT use URL.getQuery() 
	 * since this does not handle cases such as /index?url=...#/watch=?v=...
	 * 
	 * @param url
	 * @return String
	 */
	public static String extractQuery(final String url) {
		final int index = url.lastIndexOf('?');
		if (index < 0 || index == url.length()) {
			return null;
		}
		return url.substring(index + 1);
	}

	/**
	 * The video id in the url query or null if not found
	 * 
	 * @param query
	 * @return String
	 */
	public static String findVideoIDInQuery(final String query) {
		if (DEBUG) {
			Log.i(TAG, "findVideoIDInQuery: query = " + query);
		}

		final StringTokenizer tokenizer = new StringTokenizer(query, "&=");
		while (tokenizer.hasMoreTokens()) {
			final String name = tokenizer.nextToken();
			final String value = tokenizer.hasMoreTokens() ? tokenizer
					.nextToken() : "";

			if (DEBUG) {
				Log.i(TAG, "findVideoIDInQuery QUERY: " + name + "==" + value);
			}

			if ("v".equals(name)) {
				return value;
			}
		}
		return null;
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
	 * @param msg
	 * @param context
	 */
	void showUnexpectedCodeError(final Message msg, final Context context) {
		Toast.makeText(context,
				getString(R.string.msg_unexpected_op_code) + " " + msg.what,
				Toast.LENGTH_LONG).show();
	}

	/**
	 * @param context
	 */
	void showOpFailedError(final Context context) {
		Toast.makeText(context, getString(R.string.msg_errore_toast),
				Toast.LENGTH_LONG).show();
	}

	/**
	 * @param context
	 */
	void showUnsupportedIntentError(final Context context) {
		Toast.makeText(context, getString(R.string.msg_unsupported_intent),
				Toast.LENGTH_LONG).show();
	}

	/**
	 * @param context
	 */
	void showVersionError(final Context context) {
		Toast.makeText(context,
				String.format(getString(R.string.prefs_error_sw_ver), getIP()),
				Toast.LENGTH_LONG).show();
	}

	/**
	 * @param context
	 */
	void showOK(final Context context) {
		Toast.makeText(context, getString(R.string.msg_completed_ok),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	String getRemoteError(final String ipAddress) {
		return String.format(getString(R.string.prefs_error_not_blobbox),
				ipAddress);
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	String getCommsError(final String ipAddress) {
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

}
