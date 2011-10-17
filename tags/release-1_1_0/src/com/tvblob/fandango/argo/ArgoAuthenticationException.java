package com.tvblob.fandango.argo;

/**
 * 
 * 
 * @author Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class ArgoAuthenticationException extends ArgoException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param ipAddress
	 */
	public ArgoAuthenticationException(final String ipAddress) {
		super("Authentication failure, check login and password for box on "
				+ ipAddress);
	}

	/**
	 * @param ipAddress
	 * @param cause
	 */
	public ArgoAuthenticationException(final String ipAddress,
			final Throwable cause) {
		super("Authentication failure, check login and password for box on "
				+ ipAddress, cause);
	}

}
