package com.tvblob.fandango.myblobbox;

import android.content.Intent;

import com.tvblob.fandango.argo.ArgoException;

/**
 * 
 * 
 * @author Paul Henshaw
 * @created Sep 15, 2011
 * @cvsid $Id$
 */
public class ArgoUnsupportedIntentException extends ArgoException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @param cause
	 */
	public ArgoUnsupportedIntentException(final Intent intent,
			final Throwable cause) {
		super(getMessageFromIntent(intent), cause);
	}

	/**
	 * @param intent
	 * @return
	 */
	private static String getMessageFromIntent(final Intent intent) {
		return "Unable to extract url from intent " + intent.toString();
	}

	/**
	 * @param message
	 */
	public ArgoUnsupportedIntentException(final Intent intent) {
		super(getMessageFromIntent(intent));
	}

}
