package com.tvblob.fandango.argo;

/**
 * 
 * 
 * @author Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class IncompatibleRemoteDeviceException extends ArgoException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public IncompatibleRemoteDeviceException(final String ipAddress) {
		super(createMessage(ipAddress));
	}

	/**
	 * @param ipAddress
	 * @return
	 */
	private static String createMessage(final String ipAddress) {
		return "WARNING: IP "
				+ ipAddress
				+ " is not a BLOBbox. "
				+ "Please check IP Adress and Remote access settings on BLOBbox.";
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IncompatibleRemoteDeviceException(final String message,
			final Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
