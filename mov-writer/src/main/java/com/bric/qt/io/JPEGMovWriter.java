/*
 * @(#)JPEGMovWriter.java
 *
 * $Date: 2014-06-02 02:43:17 -0400 (Mon, 02 Jun 2014) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.qt.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;


/** A MovWriter that encodes frames as a series of JPEG images.
 */
public class JPEGMovWriter extends MovWriter {
	
	private static final float DEFAULT_JPG_QUALITY = .85f;
	
	/** This property is used to determine the JPG image quality.
	 * It is a float between [0, 1], where 1 is a lossless image.
	 * This value should be the key in a key/value pair in the Map
	 * passed to <code>addFrame(..)</code>.
	 */
	public static final String PROPERTY_QUALITY = "jpeg-quality";
	
	float defaultQuality;

	public JPEGMovWriter(File file) throws IOException {
		this(file, DEFAULT_JPG_QUALITY);
	}
	
	/**
	 * 
	 * @param file the destination file to write to.
	 * @param defaultQuality the default JPEG quality (from [0,1]) to use
	 * if a frame is added without otherwise specifying this value.
	 * @throws IOException
	 */
	public JPEGMovWriter(File file,float defaultQuality) throws IOException {
		super(file);
		this.defaultQuality = defaultQuality;
	}

	@Override
	protected VideoSampleDescriptionEntry getVideoSampleDescriptionEntry() {
		return VideoSampleDescriptionEntry.createJPEGDescription( videoTrack.w, videoTrack.h);
	}

	private static boolean printWarning = false;
	
	@Override
	protected void writeFrame(OutputStream out, BufferedImage image,
			Map<String, Object> settings) throws IOException {
		if(image.getType()==BufferedImage.TYPE_INT_ARGB ||
				image.getType()==BufferedImage.TYPE_INT_ARGB_PRE) {
			if(printWarning==false) {
				printWarning = true;
				System.err.println("JPEGMovWriter Warning: a BufferedImage of type TYPE_INT_ARGB may produce unexpected results. The recommended type is TYPE_INT_RGB.");
			}
		}
		float quality;
		if(settings!=null && settings.get(PROPERTY_QUALITY) instanceof Number) {
			quality = ((Number)settings.get(PROPERTY_QUALITY)).floatValue();
		} else if(settings!=null && settings.get(PROPERTY_QUALITY) instanceof String) {
			quality = Float.parseFloat((String)settings.get(PROPERTY_QUALITY));
		} else {
			quality = defaultQuality;
		}
		
		MemoryCacheImageOutputStream iOut = new MemoryCacheImageOutputStream(out);
		ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
		ImageWriteParam iwParam = iw.getDefaultWriteParam();
		iwParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwParam.setCompressionQuality(quality);
		iw.setOutput(iOut);
		IIOImage img = new IIOImage(image, null, null);
		iw.write(null, img, iwParam);
	}
}
