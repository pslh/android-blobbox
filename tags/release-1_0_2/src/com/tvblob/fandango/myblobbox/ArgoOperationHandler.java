package com.tvblob.fandango.myblobbox;

import android.app.Activity;
import android.os.Message;

/**
 * {@link AbstractArgoOperationHandler} for play/download operations
 * 
 * @author Paul Henshaw
 * @created Sep 26, 2011
 * @cvsid $Id$
 */
final class ArgoOperationHandler extends AbstractArgoOperationHandler {

	/**
	 * PACKAGE PRIVATE
	 * 
	 * Construct handler with ipAddress and activity
	 */
	ArgoOperationHandler(final String ipAddress, final Activity activity) {
		super(ipAddress, activity);
	}

	/* (non-Javadoc)
	 * @see android.os.Handler#handleMessage(android.os.Message)
	 */
	public void handleMessage(final Message msg) {
		showUserMessage(msg);
		getActivity().finish();
	}

	/* (non-Javadoc)
	 * @see com.tvblob.fandango.myblobbox.AbstractArgoOperationHandler#getOKMessage()
	 */
	protected String getOKMessage() {
		return getActivity().getString(R.string.msg_completed_ok);
	}
}