/*
 * @(#)MovWriter
 *
 * $Date: 2015-03-11 01:41:06 -0400 (Wed, 11 Mar 2015) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.bric.image.ImageSize;
import com.bric.io.MeasuredOutputStream;

/**
 * This writes a QuickTime MOV file as a series of images,
 * and interleaves optional PCM (uncompressed) audio.
 * <P>This abstract class does not actually encode
 * the image data; subclasses decide how to do this. The only
 * two current subclasses use either JPG or PNG
 * compression. By modern standards: this results in a very poorly
 * compressed image file, but it is free of painful legal
 * implications that come with the MPEG-4 standard. (And
 * it is worlds easier to implement.)
 * <P>This actually writes to a movie file in 2 passes:
 * the first pass writes all the video and audio data to a 
 * <code>FileOutputStream</code>. When <code>close()</code> is called, 
 * the movie structure is added and a <code>RandomAccessFile</code> is
 * used to correctly set the size headers.
 *
 */
public abstract class MovWriter {
	
	/** This is used to indicate that a frame requested an unacceptable duration, such as 0 or -1. */
	public static class InvalidDurationException extends IllegalArgumentException {
		public InvalidDurationException(String msg) {
			super(msg);
		}
	}

	/**
	 * This is used to indicate that the file used to create a frame was too small.
	 * <p>(Usually this is a missing file that is 0K.)
	 */
	public static class InvalidFileLengthException extends IllegalArgumentException {
		private static final long serialVersionUID = 1L;
		
		public InvalidFileLengthException(String msg) {
			super(msg);
		}
	}
	
	public static final long DEFAULT_TIME_SCALE = 1000;
	
	private static class VideoSample {
		final int duration;
		final long fileLength;
		final long dataStart;
		public VideoSample(int duration,long dataStart,long fileLength) throws InvalidDurationException, InvalidFileLengthException {
			if(duration<=0) throw new InvalidDurationException("duration ("+duration+") must be greater than zero.");
			if(fileLength<=0) throw new InvalidFileLengthException("file length ("+fileLength+") must be greater than zero.");
			this.duration = duration;
			this.fileLength = fileLength;
			this.dataStart = dataStart;
		}
	}
	
	class VideoTrack {
		List<VideoSample> samples = new Vector<VideoSample>();
		protected int w = -1, h = -1;
		long totalDuration;
		TimeToSampleAtom stts = new TimeToSampleAtom();
		SampleSizeAtom stsz = new SampleSizeAtom();
		SampleToChunkAtom stsc = new SampleToChunkAtom();
		ChunkOffsetAtom stco = new ChunkOffsetAtom();
		
		void writeToMoovRoot(ParentAtom moovRoot) {
			ParentAtom trakAtom = new ParentAtom("trak");
			moovRoot.add(trakAtom);
			TrackHeaderAtom trackHeader = new TrackHeaderAtom(1, totalDuration, w, h);
			trackHeader.volume = 0;
			trakAtom.add(trackHeader);
			ParentAtom mdiaAtom = new ParentAtom("mdia");
			trakAtom.add(mdiaAtom);
			MediaHeaderAtom mediaHeader = new MediaHeaderAtom(DEFAULT_TIME_SCALE, totalDuration);
			mdiaAtom.add(mediaHeader);
			HandlerReferenceAtom handlerRef1 = new HandlerReferenceAtom("mhlr","vide","java");
			mdiaAtom.add(handlerRef1);
			ParentAtom minf = new ParentAtom("minf");
			mdiaAtom.add(minf);
			VideoMediaInformationHeaderAtom vmhd = new VideoMediaInformationHeaderAtom();
			minf.add(vmhd);
			HandlerReferenceAtom handlerRef2 = new HandlerReferenceAtom("dhlr","alis","java");
			minf.add(handlerRef2);
			
			ParentAtom dinf = new ParentAtom("dinf");
			minf.add(dinf);
			DataReferenceAtom dref = new DataReferenceAtom();
			dref.addEntry("alis", 0, 1, new byte[] {});
			dinf.add(dref);
			
			ParentAtom stbl = new ParentAtom("stbl");
			minf.add(stbl);
			
			SampleDescriptionAtom stsd = new SampleDescriptionAtom();
			stsd.addEntry( getVideoSampleDescriptionEntry() );
			stbl.add(stsd);
			
			stbl.add(stts);
			stbl.add(stsc);
			stbl.add(stsz);
			stbl.add(stco);
		}
		
		int samplesInCurrentChunk = 0;
		long durationOfCurrentChunk = 0;
		int currentChunkIndex = 0;
		private void addSample(VideoSample sample) throws IOException {
			samples.add(sample);
			totalDuration += sample.duration;
			stts.addSampleTime(sample.duration);
			stsz.addSampleSize(sample.fileLength);
			
			//now decide if the addition of this sample concluded a chunk of samples:
			samplesInCurrentChunk++;
			durationOfCurrentChunk += sample.duration;
			if(durationOfCurrentChunk>=DEFAULT_TIME_SCALE) {
				closeChunk();
			}
		}
		
		void close() throws IOException {
			closeChunk();
		}
		
		private void closeChunk() throws IOException {
			if(samplesInCurrentChunk>0) {
				stsc.addChunk(currentChunkIndex+1, samplesInCurrentChunk, 1);
				stco.addChunkOffset(samples.get(samples.size()-samplesInCurrentChunk).dataStart);
				
				//reset variables
				currentChunkIndex++;
				samplesInCurrentChunk = 0;
				durationOfCurrentChunk = 0;
			}
		}

		void addFrame(int duration, byte[] imageFile) throws IOException {
			if(w==-1 && h==-1) {
				Dimension d = ImageSize.getSizeUsingImageIO(imageFile);
				if (d != null) {
					w = d.width;
					h = d.height;
				}
			}

			long byteSize = write(out, imageFile);
			VideoSample sample = new VideoSample(duration, out.getBytesWritten()-byteSize, byteSize);
			addSample(sample);
		}

		void validateSize(int width,int height) {
			if(width==-1) throw new IllegalArgumentException("width = "+width);
			if(height==-1) throw new IllegalArgumentException("height = "+height);
			
			if(w==-1 && h==-1) {
				w = width;
				h = height;
			} else {
				if(w!=width || h!=height) {
					throw new IllegalArgumentException("Each frame must have the same dimension.  This frame ("+width+"x"+height+") is not the same dimensions as previous frames ("+w+"x"+h+").");
				}
			}
		}
	}
	
	/** The output stream we write the movie data to. */
	private MeasuredOutputStream out;
	
	/** The file we're writing to. */
	File dest;
	
	/** Whether close() has been called yet. */
	private boolean closed = false;
	
	/** The video track. */
	protected VideoTrack videoTrack = new VideoTrack();
	
	/** Constructs a new <code>MovWriter</code>.
	 * <P>By constructing this object a <code>FileOutputStream</code>
	 * is opened for the destination file.  It remains open until
	 * <code>close()</code> is called or this object is finalized.
	 * @param file the file data is written to.  It is strongly
	 * recommended that this file name end with ".mov" (or ".MOV"), although
	 * this is not required.
	 * @throws IOException
	 */
	public MovWriter(File file) throws IOException {
		dest = file;
		file.createNewFile();
		out = new MeasuredOutputStream(new FileOutputStream(file));
		
		Atom.write32Int(out, 1); //an extended size field
		Atom.write32String(out, "mdat");
		
		//the extended size field: an 8-byte long that will eventually
		//reflect the size of the data atom. We don't know this in the
		//first pass, so write 8 zeroes, and we'll fill this gap in
		//when .close() is called:
		Atom.write32Int(out, 0);
		Atom.write32Int(out, 0);
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	/** This finishes writing the movie file.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		synchronized(this) {
			if(closed)
				return;
			closed = true;
		}

		long mdatSize;
		try {
			videoTrack.close();

			mdatSize = out.getBytesWritten();

			ParentAtom moovRoot = new ParentAtom("moov");

			long totalDuration = videoTrack.totalDuration;
			MovieHeaderAtom movieHeader = new MovieHeaderAtom(DEFAULT_TIME_SCALE, totalDuration);
			moovRoot.add(movieHeader);

			videoTrack.writeToMoovRoot(moovRoot);
			moovRoot.write(out);
		} finally {
			out.close();
		}

		//very last step: we have to rewrite the first
		//4 bytes of this file now that we can conclusively say
		//how big the "mdat" atom is:

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(dest,"rw");
			raf.seek(8);
			byte[] array = new byte[8];
			array[0] = (byte)((mdatSize >> 56) & 0xff);
			array[1] = (byte)((mdatSize >> 48) & 0xff);
			array[2] = (byte)((mdatSize >> 40) & 0xff);
			array[3] = (byte)((mdatSize >> 32) & 0xff);
			array[4] = (byte)((mdatSize >> 24) & 0xff);
			array[5] = (byte)((mdatSize >> 16) & 0xff);
			array[6] = (byte)((mdatSize >> 8) & 0xff);
			array[7] = (byte)(mdatSize & 0xff);
			raf.write(array);
		} finally {
			raf.close();
		}
	}

	/** Adds an image to this animation.
	 * <P>All images must be the same dimensions; if this image is
	 * a different size from previously added images an exception is thrown.
	 * 
	 * @param duration the duration (in seconds) this frame should
	 * show.  (This value is converted to a timescale of DEFAULT_TIME_SCALE.)
	 * @param bi the image to add as a frame.
	 * @param settings an optional map of settings subclasses may use
	 * to encode this data. For example, the JPEGMovWriter may consult
	 * this map to determine the image quality of the JPEG it writes.
	 * @throws IOException
	 */
	public synchronized void addFrame(float duration,BufferedImage bi,Map<String, Object> settings) throws IOException {
		if(closed) throw new IllegalArgumentException("this writer has already been closed");
		int relativeDuration = (int)(duration*DEFAULT_TIME_SCALE+.5);

		videoTrack.validateSize(bi.getWidth(), bi.getHeight());
		long startPosition = out.getBytesWritten();
		writeFrame(out, bi, settings);
		long byteSize = out.getBytesWritten() - startPosition;
		VideoSample sample = new VideoSample(relativeDuration, out.getBytesWritten()-byteSize, byteSize);
		videoTrack.addSample(sample);
	}
	
	protected abstract void writeFrame(OutputStream out,BufferedImage image,Map<String, Object> settings) throws IOException;

	/** Adds an image to this animation.
	 * <P>All images must be the same dimensions; if this image is
	 * a different size from previously added images an exception is thrown.
	 * <P>This method is provided as a convenient way to quickly merge
	 * frames into a movie. It does not, however, type check the images, or
	 * convert images that are not of the correct file type. (For example:
	 * if you add TIFF image files to a MovWriter that expects JPG image files,
	 * then no exception will be thrown. But the new mov file will be unreadable.)
	 * 
	 * @param duration the duration (in seconds) this frame should
	 * show.  (This value is converted to a timescale of DEFAULT_TIME_SCALE.)
	 * @param image the image to add.
	 * @throws IOException
	 */
	public synchronized void addFrame(int duration, byte[] image) throws IOException {
		if(closed) throw new IllegalArgumentException("this writer has already been closed");
		
		videoTrack.addFrame(duration, image);
	}
	
	/** Subclasses must define the VideoSampleDescriptionEntry this writer uses.
	 */
	protected abstract VideoSampleDescriptionEntry getVideoSampleDescriptionEntry();

	/** Write a file to an OutputStream.
	 * 
	 * @param out the stream to write to.
	 * @param file the file to write
	 * @return the number of bytes written.
	 * @throws IOException
	 */
	protected static synchronized long write(OutputStream out,File file) throws IOException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			return write(out, in, false);
		} finally {
			try {
				in.close();
			} catch(IOException e) {}
		}
	}

	protected static synchronized long write(OutputStream out, byte[] bytes) throws IOException {
		out.write(bytes,0,bytes.length);
		return bytes.length;
	}

	/** Write the remainder of an InputStream to an OutputStream.
	 * 
	 * @param out the stream to write to.
	 * @param in the data to write
	 * @param reverseBytePairs whether every two bytes should be switched (to convert
	 * from one endian to another)
	 * @return the number of bytes written.
	 * @throws IOException
	 */
	protected static synchronized long write(OutputStream out,InputStream in,boolean reverseBytePairs) throws IOException {
		byte[] block = new byte[4096];
		
		long written = 0;
		int k = read(in, block, block.length);
		if(reverseBytePairs) reverseBytePairs(block,k);
		while(k!=-1) {
			written += k;
			out.write(block,0,k);
			k = read(in, block, block.length);
			if(reverseBytePairs) reverseBytePairs(block,k);
		}
		return written;
	}


	/** Write up to a certain number of bytes from an InputStream to an OutputStream.
	 * 
	 * @param out the stream to write to.
	 * @param in the data to write
	 * @param maxBytes the maximum number of bytes to write
	 * @param reverseBytePairs whether every two bytes should be switched (to convert
	 * from one endian to another)
	 * @return the number of bytes written.
	 * @throws IOException
	 */
	protected static synchronized long write(OutputStream out,InputStream in,long maxBytes,boolean reverseBytePairs) throws IOException {
		byte[] block = new byte[4096];
		
		long written = 0;
		
		if(maxBytes%2==1)
			maxBytes--;
		
		int k = read(in, block, Math.min(block.length, (int)maxBytes) );
		if(reverseBytePairs) reverseBytePairs(block,k);
		loop : while(k!=-1) {
			written += k;
			out.write(block,0,k);
			k = read(in, block, Math.min(block.length, (int)(maxBytes-written)));
			if(reverseBytePairs) reverseBytePairs(block,k);
			if(written==maxBytes) break loop;
		}
		return written;
	}
	
	/** Reads bytes from an InputStream. This will always return an even number
	 * of bytes.
	 * @param bytesToRead
	 * @return
	 */
	private static int read(InputStream in, byte[] dest, int bytesToRead) throws IOException {
		int read = 0;
		if(bytesToRead%2==1)
			bytesToRead--;
		read = in.read(dest, 0, bytesToRead);
		if(read==-1) return read;
		while( (read%2) == 1) {
			int k = in.read(dest, read, bytesToRead-read);
			if(k==-1) return read;
			read+=k;
		}
		return read;
	}
	
	private static void reverseBytePairs(byte[] data,int length) {
		if(length==-1) return;
		//it is safe to assume length is divisible by 2
		for(int a = 0; a<length-1; a+=2) {
			byte t = data[a];
			data[a] = data[a+1];
			data[a+1] = t;
		}
	}
}
