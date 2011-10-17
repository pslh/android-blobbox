package com.tvblob.fandango.myblobbox;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.tvblob.fandango.argo.ArgoClient;

/**
 * Abstract base class for {@link Handler} used to display result of 
 * {@link ArgoClient} operations. 
 * 
 * @author Paul Henshaw
 * @created Sep 26, 2011
 * @cvsid $Id$
 */
public abstract class AbstractArgoOperationHandler extends Handler {
	private final String ipAddress;
	private final Activity activity;

	/**
	 * @param ipAddress
	 * @param activity
	 */
	protected AbstractArgoOperationHandler(final String ipAddress,
			final Activity activity) {
		super();
		this.ipAddress = ipAddress;
		this.activity = activity;
	}

	/**
	 * Derived classes should override this method to provide a suitable
	 * message to display on operation success.
	 * 
	 * @return String
	 */
	protected abstract String getOKMessage();

	/**
	 * @param msg
	 */
	protected void showUserMessage(final Message msg) {
		switch (msg.what) {
		case Constants.OPERATION_OK:
			showShortMessage(getOKMessage());
			break;

		case Constants.OPERATION_FAILED:
			showLongMessage(getOpFailedError());

			break;

		case Constants.UNSUPPORTED_INTENT:
			showLongMessage(getUnsupportedIntentError());

			break;

		case Constants.UNSUPPORTED_VERSION:
			showLongMessage(getVersionError());

			break;

		case Constants.AUTHENTICATION_FAILED:
			showLongMessage(getAuthError());
			break;

		case Constants.COMMUNICATIONS_FAILED:
			showLongMessage(getCommsError());

			break;

		case Constants.INCOMPATIBLE_DEVICE:
			showLongMessage(getRemoteError());
			break;

		default:
			showLongMessage(getUnexpectedCodeMessage(msg));
		}
	}

	/**
	 * @param msg
	 * @return
	 */
	protected String getUnexpectedCodeMessage(final Message msg) {
		return getActivity().getString(R.string.msg_unexpected_op_code) + " "
				+ msg.what;
	}

	/**
	 * @return
	 */
	protected String getOpFailedError() {
		return getActivity().getString(R.string.msg_errore_toast);
	}

	/**
	 * @return
	 */
	protected String getUnsupportedIntentError() {
		return getActivity().getString(R.string.msg_unsupported_intent);
	}

	/**
	 * @return
	 */
	protected String getVersionError() {
		return String.format(
				getActivity().getString(R.string.prefs_error_sw_ver),
				getIPAddress());
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getRemoteError() {
		return String.format(
				getActivity().getString(R.string.prefs_error_not_blobbox),
				getIPAddress());
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getCommsError() {
		return String.format(
				getActivity().getString(R.string.prefs_error_not_reachable),
				getIPAddress());
	}

	/**
	 * @param ipAddress
	 * @return String
	 */
	protected String getAuthError() {
		return String.format(
				getActivity().getString(R.string.prefs_error_auth),
				getIPAddress());
	}

	// Toast display

	/**
	 * PACKAGE PRIVATE - used by handler
	 * 
	 * @param error
	 */
	protected void showLongMessage(final String error) {
		Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
	}

	/**
	 * PACKAGE PRIVATE - used by handler
	 * 
	 * @param message
	 */
	protected void showShortMessage(final String message) {
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * @return the context
	 */
	private Context getContext() {
		return getActivity().getApplicationContext();
	}

	/**
	 * @return the activity
	 */
	protected Activity getActivity() {
		return activity;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIPAddress() {
		return ipAddress;
	}
}
