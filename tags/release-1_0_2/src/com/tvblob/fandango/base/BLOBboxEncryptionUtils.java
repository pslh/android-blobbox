package com.tvblob.fandango.base;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Static methods providing basic cryptographic functions.  
 * This class is not intended to be subclassed or instantiated.
 * 
 * <code>
 * @author Paul Henshaw
 * @created Nov 14, 2007
 * @cvsid $Id$
 * </code>
 */
public final class BLOBboxEncryptionUtils {
	private static final String SHA_1_ALGORITHM = "SHA-1";
	private static final String UTF_8_ENCODING = "UTF-8";

	/**
	 * No public constructor - use static methods
	 */
	private BLOBboxEncryptionUtils() {
		// intentionally empty
	}

	/**
	 * @param message
	 * @param algorithm
	 * @return String
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException 
	 */
	public static String getEncryptedMessageDigestAsString(
			final String message, final String algorithm)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return StringUtilities.bytesToHexString(getEncryptedMessageDigest(
				getByteArray(message), algorithm));
	}

	/**
	 * @param message
	 * @return String
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException 
	 */
	public static String getEncryptedMessageDigestAsString(final String message)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return getEncryptedMessageDigestAsString(message, SHA_1_ALGORITHM);
	}

	/**
	 * @param byteArray
	 * @return byte[]
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] getEncryptedMessageDigest(final byte[] byteArray)
			throws NoSuchAlgorithmException {
		return getEncryptedMessageDigest(byteArray, SHA_1_ALGORITHM);
	}

	/**
	 * @param byteArray
	 * @param algorithm 
	 * @return byte[]
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] getEncryptedMessageDigest(final byte[] byteArray,
			final String algorithm) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(algorithm).digest(byteArray);
	}

	/**
	 * @param message
	 * @return byte[]
	 * @throws UnsupportedEncodingException 
	 */
	private static byte[] getByteArray(final String message)
			throws UnsupportedEncodingException {
		return message.getBytes(UTF_8_ENCODING);
	}
}
