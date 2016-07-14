package com.loadtestgo.script.tester.server;

import org.pmw.tinylog.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SslTransport {
    private SSLSession session;
    private SSLEngine engine;
    private SSLEngineResult engineResult;
    private SSLEngineResult.HandshakeStatus handshakeStatus;
    private boolean initialHandshake;
    private SSLContext sslContext;
    private SocketChannel socketChannel;
    private SslHandshakeParser handshakeParser;
    private int streamId = 0;
    static private AtomicInteger nextStreamId = new AtomicInteger(0);

    ByteBuffer inOutputData;
    ByteBuffer inInputData;
    ByteBuffer outOutputData;

    InputStream outputStream = new SSLInputStream();
    OutputStream inputStream = new SSLOutputStream();

    public InputStream getInputStream() {
        return outputStream;
    }

    public OutputStream getOutputStream() {
        return inputStream;
    }

    public SslTransport(SocketChannel socketChannel,
                        SSLContext sslContext,
                        String host,
                        int port) throws IOException {
        this.streamId = nextStreamId.getAndAdd(1);
        this.socketChannel = socketChannel;
        this.sslContext = sslContext;

        this.engine = createSSLEngine(host, port);

        inOutputData = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        inInputData = ByteBuffer.allocateDirect(session.getPacketBufferSize());

        handshakeParser = new SslHandshakeParser();

        initialHandshake = true;
        performInitialHandshake();

        outOutputData = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        inOutputData.flip();
    }

    private SSLEngine createSSLEngine(String host, int port) throws SSLException {
        SSLEngine engine = sslContext.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        session = engine.getSession();
        engine.beginHandshake();
        handshakeStatus = engine.getHandshakeStatus();

        return engine;
    }

    private void performInitialHandshake() throws IOException {
        ByteBuffer dummy = ByteBuffer.allocate(0);

        while (initialHandshake) {
            switch (handshakeStatus) {
                case FINISHED:
                    initialHandshake = false;
                    return;

                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        task.run();
                    }
                    handshakeStatus = engine.getHandshakeStatus();
                    break;

                case NEED_UNWRAP:
                    boolean forceRead = false;
                    do {
                        inInputData.flip();
                        if (!inInputData.hasRemaining() || forceRead) {
                            inInputData.compact();
                            int read = socketChannel.read(inInputData);
                            inInputData.flip();
                            if (read > 0) {
                                handshakeParser.parse(streamId, "recv", inInputData);
                            }
                        }

                        engineResult = engine.unwrap(inInputData, inOutputData);
                        inInputData.compact();
                        forceRead = engineResult.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW;
                    } while (forceRead);

                    if (engineResult.getStatus() != SSLEngineResult.Status.OK) {
                        throw new IOException(engineResult.getStatus().toString());
                    }
                    handshakeStatus = engineResult.getHandshakeStatus();
                    sendOutput(inOutputData);
                    break;

                case NEED_WRAP:
                    engineResult = engine.wrap(dummy, inOutputData);
                    if (engineResult.getStatus() != SSLEngineResult.Status.OK) {
                        throw new IOException(engineResult.getStatus().toString());
                    }
                    handshakeStatus = engineResult.getHandshakeStatus();
                    sendOutput(inOutputData);
                    break;

                case NOT_HANDSHAKING:
                    // Ignore
                    break;
            }
        }
    }

    private void sendOutput(ByteBuffer outputBuffer) throws IOException {
        outputBuffer.flip();

        int remaining = outputBuffer.remaining();
        while (remaining > 0) {
            handshakeParser.parse(streamId, "send", outputBuffer);
            socketChannel.write(outputBuffer);
            remaining = outputBuffer.remaining();
        }

        outputBuffer.compact();
    }

    public SSLSession getSslSession() {
        return engine.getSession();
    }

    public SslHandshakeParser getHandshakeParser() {
        return handshakeParser;
    }

    class SSLInputStream extends InputStream {
        SSLInputStream() {
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (readData(b, 0, 1) == 1) {
                return (int) b[0];
            } else {
                return -1;
            }
        }

        public int read(byte[] buf, int off, int len) throws IOException {
            return readData(buf, off, len);
        }

        public int readData(byte[] buf, int off, int len) throws IOException {
            while (true) {
                if (inOutputData.hasRemaining()) {
                    int actualLen = Math.min(len, inOutputData.remaining());
                    inOutputData.get(buf, off, actualLen);
                    return actualLen;
                }

                int read = -1;
                do {
                    inInputData.flip();

                    if (!inInputData.hasRemaining() ||
                            engineResult.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {

                        inInputData.compact();

                        read = socketChannel.read(inInputData);

                        if (read == -1) {
                            return -1;
                        }

                        if (read == 0) {
                            return 0;
                        }

                        inInputData.flip();
                    }

                    inOutputData.compact();
                    engineResult = engine.unwrap(inInputData, inOutputData);
                    inOutputData.flip();
                    inInputData.compact();

                } while (engineResult.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW);
            }
        }
    }

    class SSLOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            writeData(new byte[]{(byte) b}, 0, 1);
        }

        public void write(byte[] buf, int off, int len) throws IOException {
            writeData(buf, off, len);
        }

        void writeData(byte[] buf, int off, int len) throws IOException {
            ByteBuffer source = ByteBuffer.wrap(buf, off, len);
            while (source.remaining() > 0) {
                engineResult = engine.wrap(source, outOutputData);
                outOutputData.flip();
                while (outOutputData.remaining() > 0) {
                    socketChannel.write(outOutputData);
                }
                outOutputData.compact();
            }
        }
    }

    public void close() throws IOException {
        try {
            if (!engine.isOutboundDone()) {
                engine.closeOutbound();
            }

            if (engine.isInboundDone()) {
                engine.closeInbound();
            }
        } finally {
            Logger.info("{}:close", streamId);
            socketChannel.close();
        }
    }
}
