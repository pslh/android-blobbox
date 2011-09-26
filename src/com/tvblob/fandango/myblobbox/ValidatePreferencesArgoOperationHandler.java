package com.tvblob.fandango.myblobbox;

import android.app.Activity;
import android.os.Message;

/**
 * {@link AbstractArgoOperationHandler} for preference validation operation
 * 
 * @author Paul Henshaw
 * @created Sep 26, 2011
 * @cvsid $Id$
 */
final class ValidatePreferencesArgoOperationHandler extends
		AbstractArgoOperationHandler {

	/**
	 * PACKAGE PRIVATE
	 * 
	 * Construct handler with ipAddress and activity
	 */
	ValidatePreferencesArgoOperationHandler(final String ipAddress,
			final Activity activity) {
		super(ipAddress, activity);
	}

	/* (non-Javadoc)
	 * @see android.os.Handler#handleMessage(android.os.Message)
	 */
	public void handleMessage(final Message msg) {
		showUserMessage(msg);
	}

	/* (non-Javadoc)
	 * @see com.tvblob.fandango.myblobbox.AbstractArgoOperationHandler#getOKMessage()
	 */
	protected String getOKMessage() {
		return String.format(getActivity().getString(R.string.prefs_ok),
				getIPAddress());
	}
}