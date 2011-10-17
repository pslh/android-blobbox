package com.tvblob.fandango.myblobbox;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tvblob.fandango.argo.ArgoClient;
import com.tvblob.fandango.argo.ArgoException;

/**
 * {@link Activity} to request that the BLOBbox play a url
 * 
 * @author Rodolfo Saccani, Paul Henshaw
 * @created Sep 6, 2011
 * @cvsid $Id$
 */
public class PlaybackMedia extends AbstractBLOBboxActivity {

	/* (non-Javadoc)
	 * @see com.rsaccani.android.blobbox.AbstractBLOBboxActivty#performOperation(android.widget.TextView, android.content.Intent, org.alexd.jsonrpc.JSONRPCHttpClient)
	 */
	protected void performOperation(final Intent intent, final ArgoClient client)
			throws ArgoException {

		// Attempt to send HOME key, just in case we are already playing
		client.sendHOMEKeyIgnoreErrors();

		final String url = getNonNullURL(intent);
		final String youTubeURL = getYouTubeURL(url);
		if (youTubeURL == null) {
			// NOT YouTube - open in player
			if (Constants.DEBUG) {
				Log.i(Constants.TAG, "performOperation: calling playURI " + url);
			}
			client.playURI(url, getString(R.string.play_title),
					String.format(getString(R.string.play_info), url));
		} else {
			// YouTube - open in browser
			if (Constants.DEBUG) {
				Log.i(Constants.TAG, "performOperation: YouTube URI: "
						+ youTubeURL);
			}
			client.browseURL(youTubeURL);
		}
	}

}
