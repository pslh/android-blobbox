package com.tvblob.fandango.argo;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.alexd.jsonrpc.JSONRPCException;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.util.Log;

import com.tvblob.fandango.base.BLOBboxEncryptionUtils;
import com.tvblob.fandango.base.StringUtilities;

/**
 * Support for JSON/RPC Argo communications with BLOBbox 
 * 
 * @author Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class ArgoClient {

	private static final int TIMEOUT_MS = 10000;

	private static final int MIN_VERSION_NUM = StringUtilities
			.getCompoundVersionNumber("1.66.2");

	private static final int MIN_PH_VERSION_NUM = StringUtilities
			.getCompoundVersionNumber("1.64.10");

	private static final String TAG = "ArgoClient";
	private static final boolean DEBUG = true;

	private final JabsorbRPCClient client;

	/**
	 * 
	 * 
	 * @param ipAddress of BLOBbox
	 * @throws ArgoCommunicationException 
	 * @throws IncompatibleRemoteDeviceException 
	 * @throws IncompatibleSoftwareVersionException 
	 * @throws ArgoAuthenticationException 
	 */
	public ArgoClient(final String ipAddress, final String userName,
			final String password) throws ArgoCommunicationException,
			IncompatibleRemoteDeviceException,
			IncompatibleSoftwareVersionException, ArgoAuthenticationException {
		this.client = initClient(ipAddress);

		if (DEBUG) {
			Log.i(TAG, "I: " + getJSONRPCURL(ipAddress));
			Log.i(TAG, "username: " + userName);
			Log.i(TAG, "password: " + password);
		}

		validateClient(ipAddress, client);
		try {
			login(client, userName, password, ipAddress);
		} catch (final NoSuchAlgorithmException exception) {
			throw new IllegalStateException("Unable to generate SHA1 digest",
					exception);
		} catch (final UnsupportedEncodingException exception) {
			throw new IllegalStateException("No UTF-8 support!", exception);
		} catch (final JSONRPCException exception) {
			throw new ArgoAuthenticationException(ipAddress);
		}

	}

	public void sendHOMEKeyIgnoreErrors() {
		try {
			sendRemoteControlKey("HOME");
		} catch (final Exception exception) {
			// IGNORE any errors for HOME key
		}
	}

	/**
	 * @param key
	 * @throws ArgoException
	 */
	public void sendRemoteControlKey(final String key) throws ArgoException {
		try {
			client.call("keyinput.remoteControlKeyPressed", key);
		} catch (final JSONRPCException exception) {
			throw new ArgoException("Failed to send key " + key, exception);
		}
	}

	/**
	 * @param ipAddress2
	 * @param userName2
	 * @param password2
	 * @param ipAddress2 
	 * @throws JSONRPCException 
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	private static void login(final JabsorbRPCClient client,
			final String userName2, final String password2,
			final String ipAddress2) throws JSONRPCException,
			NoSuchAlgorithmException, UnsupportedEncodingException {
		final String auth_user_name = client
				.callString("authenticationManager.getAuthorizedUserName");

		if (auth_user_name == null || auth_user_name.equals("null")) {
			// we need to log in
			// Authentication with blobbox
			if (DEBUG) {
				Log.i(TAG, "Trying authentication...");
			}

			// get the challenge
			if (DEBUG) {
				Log.i(TAG, "getLoginChallenge...");
			}
			final String challenge = client.callString(
					"authenticationManager.getLoginChallenge",
					new Object[] { userName2 });

			final String digest = BLOBboxEncryptionUtils
					.getEncryptedMessageDigestAsString(challenge + ':'
							+ userName2 + ':' + password2);

			client.callString("authenticationManager.login",
					new Object[] { digest });

			// Need to repeat setup - list of methods has changed
			client.performPost(getArgoURL(ipAddress2));

			if (DEBUG) {
				Log.i(TAG, "challenge: " + challenge.toString());
			}
			final String auth_user_name2 = client
					.callString("authenticationManager.getAuthorizedUserName");

			// newly logged in
			if (DEBUG) {
				Log.i(TAG, "Logged in as " + auth_user_name2);
			}
		} else {
			// already logged in
			if (DEBUG) {
				Log.i(TAG, "Already Logged in as " + auth_user_name + " "
						+ auth_user_name.getClass().getName());
			}
		}

	}

	/**
	 * @param ipAddress
	 * @param client
	 * @return
	 * @throws IncompatibleRemoteDeviceException
	 * @throws IncompatibleSoftwareVersionException
	 * @throws ArgoCommunicationException
	 */
	protected static void validateClient(final String ipAddress,
			final JabsorbRPCClient client)
			throws IncompatibleRemoteDeviceException,
			IncompatibleSoftwareVersionException, ArgoCommunicationException {
		try {
			final HttpResponse response = client
					.performPost(getArgoURL(ipAddress));
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 204) {
				throw new IncompatibleRemoteDeviceException(ipAddress);
			}

			final String version = client
					.callString("version.getVersionString");

			if (!isSupportedVersion(version)) {
				// Older SMOJ Version does not support JSON play API  
				throw new IncompatibleSoftwareVersionException(version);
			}
		} catch (final JSONRPCException exception1) {
			throw new ArgoCommunicationException(ipAddress);
		} catch (final IllegalArgumentException exception) {
			throw new ArgoCommunicationException(ipAddress, exception);
		}
	}

	/**
	 * Throw an exception if we are unable to communicate with an supported 
	 * BLOBbox at ipAddress 
	 * 
	 * @param ipAddress
	 * @throws IncompatibleRemoteDeviceException
	 * @throws IncompatibleSoftwareVersionException
	 * @throws ArgoCommunicationException
	 */
	public static void ensureValid(final String ipAddress)
			throws IncompatibleRemoteDeviceException,
			IncompatibleSoftwareVersionException, ArgoCommunicationException {
		final JabsorbRPCClient client = initClient(ipAddress);
		validateClient(ipAddress, client);
	}

	/**
	 * @param ipAddress
	 * @return
	 */
	protected static JabsorbRPCClient initClient(final String ipAddress) {
		final JabsorbRPCClient client = new JabsorbRPCClient(
				getJSONRPCURL(ipAddress));

		client.setConnectionTimeout(TIMEOUT_MS);
		client.setSoTimeout(TIMEOUT_MS);
		return client;
	}

	/**
	 * True iff SMOJ version support remote play API
	 * 
	 * @param version
	 * @return boolean
	 */
	private static boolean isSupportedVersion(final String version) {
		final int versionNum = StringUtilities
				.getCompoundVersionNumber(version);
		return versionNum >= MIN_VERSION_NUM
				|| versionNum >= MIN_PH_VERSION_NUM && version.contains(".PH.");
	}

	/**
	 * @return String
	 */
	protected static String getArgoURL(final String ip) {
		return "http://" + ip + "/argo?silent=true";
	}

	/**
	 * @return String
	 */
	protected static String getJSONRPCURL(final String ip) {
		return "http://" + ip + "/jabsorb/JSON-RPC";
	}

	/**
	 * @param method
	 * @param params
	 * @return
	 * @throws ArgoException
	 */
	public Object call(final String method, final Object... params)
			throws ArgoException {
		try {
			return client.call(method, params);
		} catch (final JSONRPCException exception) {
			throw new ArgoException("Failed to call " + method, exception);
		}
	}

	/**
	 * play given URI in BLOBbox player bloblet
	 * 
	 * @param uri
	 * @param title
	 * @param description
	 * @throws ArgoException
	 */
	public void playURI(final String uri, final String title,
			final String description) throws ArgoException {
		try {
			client.call(
					"action.playURI",
					new Object[] { uri, JSONObject.quote(title),
							JSONObject.quote(description) });
		} catch (final JSONRPCException exception) {
			throw new ArgoException("Failed to call action.playURI " + uri,
					exception);
		}

	}

	/**
	 * Open URL in BLOBbox browser
	 * 
	 * @param url
	 * @throws ArgoException 
	 */
	public void browseURL(final String url) throws ArgoException {
		try {
			client.call("action.browserURL", url);
		} catch (final JSONRPCException exception) {
			throw new ArgoException("Failed to call action.browserURL " + url,
					exception);
		}
	}

	/**
	 * @param url
	 * @param mimetype
	 * @throws ArgoException 
	 */
	public void addDownload(final String url, final String mimetype)
			throws ArgoException {

		try {

			if (DEBUG) {
				Log.i(TAG, "calling downloads.addDownload(" + url + ","
						+ mimetype + ")");
			}

			final Object result = client.call("downloads.addDownload", url,
					mimetype, url, JSONObject.quote("Android download " + url));

			if (DEBUG) {
				Log.i(TAG, "result=" + result.toString());
			}
		} catch (final JSONRPCException exception) {
			throw new ArgoException("Failed to call downloads.addDownload "
					+ url, exception);
		}

	}
}
