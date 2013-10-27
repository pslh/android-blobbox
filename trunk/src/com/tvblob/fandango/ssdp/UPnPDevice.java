package com.tvblob.fandango.ssdp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * MetaData regarding a UPnP device
 * 
 * <code>
 * 
 * @author Paul Henshaw
 * @created Oct 5, 2011
 * @cvsid $Id $ </code>
 */
public class UPnPDevice {
	private static final String BLOBBOX_PREFIX = "uuid:blobbox-";
	private static final String LOCATION_FIELD = "LOCATION";
	private static final String USN_FIELD = "USN";

	private final Map<String, String> metaData;
	private Document doc;

	/**
	 * Construct {@link UPnPDevice} from SSDP reply packet
	 * 
	 * @param reply
	 */
	public UPnPDevice(final DatagramPacket reply) {
		this(new String(reply.getData()));
	}

	/**
	 * Construct {@link UPnPDevice} from SSDP reply string
	 */
	public UPnPDevice(final String reply) {
		metaData = initMetaData(reply);
		doc = null;
	}

	/**
	 * @param reply
	 * @return Map String name to String value
	 */
	private Map<String, String> initMetaData(final String reply) {
		/*
		 * Example reply:
		 * 
		 * HTTP/1.1 200 OK CACHE-CONTROL: max-age=1800 DATE: Wed, 05 Oct 2011
		 * 12:09:53 GMT EXT: LOCATION: http://192.168.1.94:49152/description.xml
		 * SERVER: Linux/2.6.31-3.2, UPnP/1.0, Portable SDK for UPnP
		 * devices/1.4.6 X-User-Agent: redsonic ST:
		 * urn:schemas-upnp-org:device:MediaRenderer:1 USN: uuid:blobbox-
		 * 1_0-000006244034248144::urn:schemas-upnp-org:device:MediaRenderer:1
		 */

		final BufferedReader reader = new BufferedReader(
				new StringReader(reply));
		String line;
		final Map<String, String> map = new TreeMap<String, String>();
		try {
			while ((line = reader.readLine()) != null) {
				final int index = line.indexOf(':');
				if (index >= 0 && index < line.length()) {
					map.put(line.substring(0, index), line.substring(index + 1)
							.trim());
				}
			}
		} catch (final IOException exception) {
			throw new IllegalArgumentException("Failed to parse" + reply,
					exception);
		}
		return map;
	}

	/**
	 * @param name
	 * @return the value for the given field name or null if not present
	 */
	public String getField(final String name) {
		return metaData.get(name);
	}

	/**
	 * @return true iff USN starts with blobbox prefix
	 */
	public boolean isBlobbox() {
		final String usn = getField(USN_FIELD);
		return usn != null && usn.startsWith(BLOBBOX_PREFIX);
	}

	/**
	 * @return LOCATION field URL
	 */
	public String getLocationURL() {
		return getField(LOCATION_FIELD);
	}

	/**
	 * @return host from LOCATION field or null if not found
	 * 
	 * @throws MalformedURLException
	 */
	public String getHost() {
		final String locationURL = getLocationURL();
		if (locationURL == null) {
			return null;
		}
		try {
			return new URL(locationURL).getHost();
		} catch (final MalformedURLException exception) {
			return null;
		}
	}

	/**
	 * The value of the friendlyName element
	 * 
	 * @return String
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public String getFriendlyName() throws IOException {
		final Document document = getXMLDescription();
		if (document == null) {
			System.err.println("NULL doc! " + metaData);
			return null;
		}

		final NodeList deviceNodeList = document.getDocumentElement()
				.getElementsByTagName("device");

		if (deviceNodeList.getLength() == 0) {
			return null;
		}

		final Element elem = (Element) deviceNodeList.item(0);
		final NodeList nodeList = elem.getElementsByTagName("friendlyName");
		if (nodeList.getLength() == 0) {
			return null;
		}
		Node firstChild = nodeList.item(0).getFirstChild();
		return firstChild == null ? null : firstChild.getNodeValue();
	}

	/**
	 * @return XML document for UPnP description.xml, uses cached document if
	 *         available
	 * @throws IOException
	 */
	public Document getXMLDescription() throws IOException {
		if (this.doc == null) {
			doc = readXMLDocument();
		}
		return doc;
	}

	/**
	 * 
	 * @return Document
	 * @throws IOException
	 */
	private Document readXMLDocument() throws IOException {
		final String url = getLocationURL();
		if (url == null) {
			return null;
		}
		final HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
		} catch (final MalformedURLException badURL) {
			return null;
		}
		final InputStream inputStream = connection.getInputStream();

		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(inputStream);
		} catch (final SAXException exception) {
			exception.printStackTrace();
			return null;
		} catch (final ParserConfigurationException exception) {
			exception.printStackTrace();
			return null;
		} finally {
			try {
				inputStream.close();
			} catch (final IOException exception) {
				// ignore
			}
			connection.disconnect();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (isBlobbox()) {
			return "Blobbox on " + getHost() + " name="
					+ getFriendlyNameIgnoreErrors();
		}
		return metaData.toString();
	}

	/**
	 * @return String
	 */
	private String getFriendlyNameIgnoreErrors() {
		String name;
		try {
			name = getFriendlyName();
		} catch (final IOException exception) {
			exception.printStackTrace();
			name = null;
		}
		return name;
	}
}