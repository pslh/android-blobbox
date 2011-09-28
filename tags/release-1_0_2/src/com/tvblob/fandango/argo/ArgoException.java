package com.tvblob.fandango.argo;

/**
 * Base class for Argo related exceptions
 * 
 * @author Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class ArgoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public ArgoException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArgoException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
