package com.tvblob.fandango.base;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Convenience methods for manipulating Strings.
 * 
 * <code> 
 * @author Paul Henshaw
 * @created Jun 16, 2006
 * @cvsid $Id$ 
 * </code>
 */
public final class StringUtilities {

	/**
	 * No public constructor: this class is not intended to be instantiated or
	 * subclassed.
	 */
	private StringUtilities() {
		// Intentionally Empty
	}

	/**
	 * Returns true if string is valid HEX number,
	 * i.e ABCDEF0123456789
	 * @param string
	 * @return boolean
	 */
	public static boolean isValidHexString(final String string) {
		final int len = string.length();
		for (int i = 0; i < len; i++) {
			if (Character.digit(string.charAt(i), 16) == -1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Given a versionString return an array of integers corresponding to the
	 * numeric values found in the string. Non-numeric version components and
	 * any numeric components following non-numeric components are ignored.
	 * 
	 * Examples:
	 * <ul>
	 * <li>getVersionNumbers("1.17.6") returns [1,17,6]
	 * <li>getVersionNumbers("1.38.1-vweb0606-02") returns [1,38,1]
	 * <li>getVersionNumbers("1.16.5.2.3") returns [1,16,5,2,3]
	 * <li>getVersionNumbers("1.16.x.5.2.3") returns [1,16]
	 * <li>getVersionNumbers("") returns []
	 * <li>getVersionNumbers("foo") returns []
	 * <li>getVersionNumbers("x.1.2.3") returns []
	 * </ul>
	 * 
	 * @param versionString
	 * @return int[]
	 */
	public static int[] getVersionNumbers(final String versionString) {
		final List<Integer> versionIntegers = new LinkedList<Integer>();
		final StringTokenizer tokenizer = new StringTokenizer(versionString,
				"._-");
		while (tokenizer.hasMoreTokens()) {
			final String versionComponentString = tokenizer.nextToken();

			final int intVal = getIntValueFromVersionComponentString(versionComponentString);
			if (intVal < 0) {
				break;
			}
			versionIntegers.add(Integer.valueOf(intVal));
		}

		final int[] versions = new int[versionIntegers.size()];
		int index = 0;
		for (final Iterator<Integer> iterator = versionIntegers.iterator(); iterator
				.hasNext();) {
			versions[index++] = (iterator.next()).intValue();
		}
		return versions;
	}

	/**
	 * @param versionComponentString
	 * @return int
	 */
	private static int getIntValueFromVersionComponentString(
			final String versionComponentString) {
		try {
			return Integer.parseInt(versionComponentString);
		} catch (final NumberFormatException exception) {
			// Non-numeric component found, try mixed string
			return getIntValueFromMixedString(versionComponentString);
		}
	}

	/**
	 * @param versionComponentString
	 * @return int
	 */
	private static int getIntValueFromMixedString(
			final String versionComponentString) {
		if (Character.isDigit(versionComponentString.charAt(0))) {
			/*
			 * Starts with a digit but also contains non-digit chars, attempt to
			 * handle strings such as "sm-core-1.43.2b".
			 */
			int index = 1;
			while (index < versionComponentString.length()
					&& Character.isDigit(versionComponentString.charAt(index))) {
				index++;
			}
			return Integer.parseInt(versionComponentString.substring(0, index));
		}
		return -1;
	}

	/**
	 * Obtain a compound value for the given version string, consider at most
	 * maxComponent version components.
	 * 
	 * Version components may have values from 0 - 255.
	 * 
	 * getCompoundVersionNumber("1.2.3.4",3) is equivalent to
	 * getCompoundVersionNumber("1.2.3", 3);
	 * 
	 * @param versionString
	 * @param maxComponents
	 * @return long
	 */
	public static int getCompoundVersionNumber(final String versionString,
			final int maxComponents) {
		int value = 0;
		int count = 0;
		final StringTokenizer tokenizer = new StringTokenizer(versionString,
				"._-");
		while (tokenizer.hasMoreTokens() && count < maxComponents) {
			final String versionComponentString = tokenizer.nextToken();

			final int intVal = getIntValueFromVersionComponentString(versionComponentString);
			if (intVal < 0) {
				break;
			}

			// Update count and value only if conversion is successful
			value = value << 8 | intVal;
			count++;
		}
		if (count < maxComponents) {
			return value << 8 * (maxComponents - count);
		}
		return value;
	}

	/**
	 * Convenience method equivalent to getCompoundVersionNumber(versionString,
	 * 4) See {@link #getCompoundVersionNumber(String, int)}
	 * 
	 * @param versionString
	 * @return int
	 */
	public static int getCompoundVersionNumber(final String versionString) {
		return getCompoundVersionNumber(versionString, 4);
	}

	/**
	 * Obtain a String representation of the byte array: each byte is 
	 * represented as a pair of hex digits. 
	 * 
	 * @param bytes
	 * @return String
	 */
	public static String bytesToHexString(final byte[] bytes) {
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			final int theInt = 0xff & bytes[i];

			// Use Character.forDigit rather than Integer.toString 
			// so as to avoid creating lots of little strings and so 
			// perform 0 padding for theInt < 0xa
			buffer.append(Character.forDigit(theInt >> 4, 16));
			buffer.append(Character.forDigit(theInt & 0xf, 16));
		}
		return buffer.toString();
	}

	/**
	 * Construct a byte array from a string produced using {@link bytesToHexString}
	 * 
	 * @param hexString
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(final String hexString) {
		final byte[] bytes = new byte[hexString.length() / 2];

		int charIndex = 0;
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (Character.getNumericValue(hexString
					.charAt(charIndex++)) << 4 | Character
					.getNumericValue(hexString.charAt(charIndex++)));
		}
		return bytes;
	}
}
