package com.tvblob.fandango.argo;

/**
 * 
 * 
 * @author Paul Henshaw
 * @created Sep 9, 2011
 * @cvsid $Id$
 */
public class IncompatibleSoftwareVersionException extends ArgoException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param version 
	 */
	public IncompatibleSoftwareVersionException(final String version) {
		super("BLOBbox software version " + version
				+ " does not support remote play API");
	}

}
