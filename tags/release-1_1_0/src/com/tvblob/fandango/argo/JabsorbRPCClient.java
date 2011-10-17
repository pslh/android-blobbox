package com.tvblob.fandango.argo;

import java.io.IOException;

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * {@link JSONRPCHttpClient} which also provides a {@link #performPost(String)} 
 * method useful for Jabsorb HTTP Session setup.
 * 
 * @author Paul Henshaw
 * @created Sep 14, 2011
 * @cvsid $Id$
 */
public class JabsorbRPCClient extends JSONRPCHttpClient {

	/**
	 * Construct {@link JSONRPCHttpClient} with uri
	 * 
	 * @param uri
	 */
	public JabsorbRPCClient(final String uri) {
		super(uri);
	}

	/**
	 * Perform a simple post to the given url. This method may be used to 
	 * call a JSP/Servlet which loads objects into the HTTP Session. 
	 * 
	 * @param url
	 * @return 
	 */
	public HttpResponse performPost(final String url) throws JSONRPCException {

		final HttpPost request = new HttpPost(url);
		final HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params,
				getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(params, getSoTimeout());
		HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
		request.setParams(params);
		try {
			return getHTTPClient().execute(request);
		} catch (final ClientProtocolException exception) {
			throw new JSONRPCException("HTTP error", exception);

		} catch (final IOException exception) {
			throw new JSONRPCException("HTTP error", exception);
		}
	}
}
