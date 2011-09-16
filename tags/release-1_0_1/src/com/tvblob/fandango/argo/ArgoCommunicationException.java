package com.tvblob.fandango.argo;

/**
 * 
 * 
 * @author Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class ArgoCommunicationException extends ArgoException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param ipAddress
	 */
	public ArgoCommunicationException(final String ipAddress) {
		super(createMessage(ipAddress));
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArgoCommunicationException(final String ipAddress,
			final Throwable cause) {
		super(createMessage(ipAddress), cause);
	}

	/**
	 * @param ipAddress
	 * @return
	 */
	protected static String createMessage(final String ipAddress) {
		return "Failed to communicate with BLOBbox at IP address: "
				+ ipAddress
				+ ". Please check IP Adress and Remote access settings on BLOBbox.";
	}
}
