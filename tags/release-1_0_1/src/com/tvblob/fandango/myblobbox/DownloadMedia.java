package com.tvblob.fandango.myblobbox;

import android.content.Intent;

import com.tvblob.fandango.argo.ArgoClient;
import com.tvblob.fandango.argo.ArgoException;

/**
 * 
 * 
 * @author Paul Henshaw, Rodolfo Saccani
 * @created Sep 8, 2011
 * @cvsid $Id$
 */
public class DownloadMedia extends AbstractBLOBboxActivty {

	/* (non-Javadoc)
	 * @see com.rsaccani.android.blobbox.AbstractBLOBboxActivty#performOperation(android.widget.TextView, android.content.Intent, org.alexd.jsonrpc.JSONRPCHttpClient)
	 */
	protected void performOperation(final Intent intent, final ArgoClient client)
			throws ArgoException {
		final String url = getNonNullURL(intent);
		client.addDownload(url, getMimeType(intent, url));
	}
}
