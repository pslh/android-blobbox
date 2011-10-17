package com.tvblob.fandango.myblobbox;

/**
 * PACKAGE PRIVATE 
 * 
 * Constants for use in multiple classes.
 * 
 * This class is not intended to be instantiated or subclassed.
 * 
 * @author Paul Henshaw
 * @created Sep 26, 2011
 * @cvsid $Id$
 */
final class Constants {

	// Debugging 
	static final boolean DEBUG = true;
	static final String TAG = "Blobbox Remote";

	/**
	 * PACKAGE PRIVATE operation result codes
	 */
	static final int OPERATION_OK = 0;
	static final int OPERATION_FAILED = 1;
	static final int UNSUPPORTED_INTENT = 2;
	static final int UNSUPPORTED_VERSION = 3;
	static final int AUTHENTICATION_FAILED = 4;
	static final int INCOMPATIBLE_DEVICE = 5;
	static final int COMMUNICATIONS_FAILED = 6;
	static final int NO_DEVICE_FOUND = 7;

	/**
	 * Name of IP address preference
	 */
	static final String IP_PREF = "blobbox_ip";

	/**
	 * Name of username preference
	 */
	static final String USERNAME_PREF = "blobbox_username";

	/**
	 * Name of password preference
	 */
	static final String PASSWORD_PREF = "blobbox_password";

	/**
	 * No public constructor - use static members
	 */
	private Constants() {
		// Intentionally empty
	}
}
