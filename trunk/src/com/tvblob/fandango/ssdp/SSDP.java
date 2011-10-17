package com.tvblob.fandango.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple Service Discovery Protocol (SSDP) utility methods 
 * 
 * <code>
 * @author Paul Henshaw
 * @created Oct 5, 2011
 * @cvsid $Id $
 * </code>
 */
public final class SSDP {
	private static final int TIMEOUT_S = 2; // Timeout in seconds
	private static final int PORT = 1900;
	private static final String MCAST_IP = "239.255.255.250";
	private static final int REPLY_BUF_SIZE = 1000;

	private static final String SEARCH = "M-SEARCH * HTTP/1.1\r\n"
			+ "HOST:239.255.255.250:1900\r\n" + "MAN:\"ssdp:discover\"\r\n"
			//			+ "ST:ssdp:all\r\n" +
			+ "ST:urn:schemas-upnp-org:device:MediaRenderer:1\r\n" + // only want media renderers v1
			"MX:" + TIMEOUT_S + "\r\n" + // max wait time == socket timeout
			"\r\n";

	/**
	 * No public constructor, use static methods
	 */
	private SSDP() {
		throw new UnsupportedOperationException(getClass().getName()
				+ " may not be instantiated");
	}

	/**
	 * Scan for Blobbox devices
	 * 
	 * @return List of {@link UPnPDevice} 
	 * @throws IOException
	 */
	public static List<UPnPDevice> findBlobboxDevices() throws IOException {
		final DatagramSocket msocket = new DatagramSocket(PORT);

		// Wait only TIMEOUT seconds when receiving reply packets
		msocket.setSoTimeout(TIMEOUT_S * 1000);
		msocket.send(createSearchMessage());

		return awaitReplies(msocket);
	}

	/**
	 * @return
	 * @throws UnknownHostException
	 */
	private static DatagramPacket createSearchMessage()
			throws UnknownHostException {
		return new DatagramPacket(SEARCH.getBytes(), SEARCH.length(),
				InetAddress.getByName(MCAST_IP), PORT);
	}

	/**
	 * @param msocket
	 * @return List<String>
	 * @throws IOException
	 */
	private static List<UPnPDevice> awaitReplies(final DatagramSocket msocket)
			throws IOException {

		final List<UPnPDevice> boxes = new LinkedList<UPnPDevice>();
		try {
			while (true) {
				/*
				 * We must use new buffer and packets - reusing objects can
				 * cause corruption if there are multiple devices in LAN.
				 */
				final byte[] buf = new byte[REPLY_BUF_SIZE];
				final DatagramPacket reply = new DatagramPacket(buf, buf.length);
				msocket.receive(reply);
				final UPnPDevice server = new UPnPDevice(reply);
				if (server.isBlobbox()) {
					boxes.add(server);
				}
			}
		} catch (final SocketTimeoutException exception) {
			// Ok, no more replies
		}
		return boxes;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			final List<UPnPDevice> boxes = SSDP.findBlobboxDevices();
			System.out.println("Found " + boxes.size() + " boxes, " + boxes);
		} catch (final IOException exception) {
			System.err.println(exception);
		}
	}
}
