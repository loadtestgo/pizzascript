package com.loadtestgo.script.tester.server;

import com.loadtestgo.util.Hex;
import org.pmw.tinylog.Logger;

import java.nio.ByteBuffer;

/**
 * Parse out data from an SSL handshake.  Goal is provide detailed analysis of the SSL
 * connect.
 */
public class SslHandshakeParser {
    public String clientHelloVersion;
    public String clientRequested;
    public String serverResponded;

    public void parse(int streamId, String mode, ByteBuffer buffer) {
        Logger.info("{}:{} {}", streamId, mode, Hex.bytesToHex(buffer));

        int position = buffer.position();

        byte firstByte = buffer.get(position++);
        if ((firstByte & 0x80) != 0) {
            SSLPlaintext header =  new SSLPlaintext();
            header.length = ((firstByte & 0x7f) << 8) + (buffer.get(position++) & 0xFF);
            header.padding = 0;
            parseSSLPlaintext(header, buffer, position);

            Logger.info("{}:{} SSLv2 {} {}",
                    streamId, mode,
                    getVersion(header),
                    HandshakeType.fromValue(header.handshakeMsgType));
        } else if ((firstByte & 0x40) != 0) {
            SSLPlaintext header =  new SSLPlaintext();
            header.length = ((firstByte & 0x3f) << 8) + (buffer.get(position++) & 0xFF);
            header.padding = buffer.get(position++);
            parseSSLPlaintext(header, buffer, position);

            Logger.info("{}:{} SSLv2 {} {}",
                    streamId, mode,
                    getVersion(header),
                    HandshakeType.fromValue(header.handshakeMsgType));
        } else {
            TLSPlaintext header = new TLSPlaintext();

            header.contentType = firstByte;
            header.protocolMajor = buffer.get(position++);
            header.protocolMinor = buffer.get(position++);
            header.length = ((buffer.get(position++) & 0xFF) << 8) + (buffer.get(position++) & 0xFF);

            if (header.contentType == ContentType.HANDSHAKE.value) {
                HandshakeMsg handshakeMsg = new HandshakeMsg();
                handshakeMsg.msgType = buffer.get(position++);
                handshakeMsg.length = ((buffer.get(position++) & 0xFF) << 16) +
                                      ((buffer.get(position++) & 0xFF) << 8) +
                                       (buffer.get(position++) & 0xFF);

                if (handshakeMsg.msgType == HandshakeType.CLIENT_HELLO.value) {
                    ClientHello clientHello = new ClientHello();
                    clientHello.majorVersion = buffer.get(position++);
                    clientHello.minorVersion = buffer.get(position++);
                    clientHello.gmt_unix_time =
                            (((long) (buffer.get(position++) & 0xFF)) << 24) +
                                    ((buffer.get(position++) & 0xFF) << 16) +
                                    ((buffer.get(position++) & 0xFF) << 8) +
                                    (buffer.get(position++) & 0xFF);

                    clientRequested = getVersion(clientHello.majorVersion, clientHello.minorVersion);
                    clientHelloVersion = getVersion(header);

                    for (int i = 0; i < 28; ++i) {
                        clientHello.random_bytes[i] = buffer.get(position++);
                    }

                    clientHello.sessionIdLen = buffer.get(position++) & 0xFF;
                    clientHello.sessionId = new byte[clientHello.sessionIdLen];

                    for (int i = 0; i < clientHello.sessionIdLen; ++i) {
                        clientHello.sessionId[i] = buffer.get(position++);
                    }

                    Logger.info("{}:{} TLS {} {} {} {} {}",
                            streamId, mode,
                            ContentType.fromValue(header.contentType),
                            getVersion(header),
                            HandshakeType.fromValue(handshakeMsg.msgType),
                            getVersion(clientHello.majorVersion, clientHello.minorVersion),
                            Hex.bytesToHex(clientHello.sessionId));
                } else if (handshakeMsg.msgType == HandshakeType.SERVER_HELLO.value) {
                    serverResponded = getVersion(header);
                    Logger.info("{}:{} TLS {} {} {}",
                            streamId, mode,
                            ContentType.fromValue(header.contentType),
                            getVersion(header),
                            HandshakeType.fromValue(handshakeMsg.msgType));
                } else {
                    Logger.info("{}:{} TLS {} {} {}",
                            streamId, mode,
                            ContentType.fromValue(header.contentType),
                            getVersion(header),
                            HandshakeType.fromValue(handshakeMsg.msgType));
                }
            } else {
                Logger.info("{}:{} TLS {} {}",
                        streamId, mode,
                        ContentType.fromValue(header.contentType),
                        getVersion(header));
            }
        }
    }

    private static String getVersion(TLSPlaintext header) {
        return getVersion(header.protocolMajor, header.protocolMinor);
    }

    private static String getVersion(SSLPlaintext header) {
        return getVersion(header.protocolMajor, header.protocolMinor);
    }

    private static String getVersion(byte protocolMajor, byte protocolMinor) {
        if (protocolMajor == 3) {
            if (protocolMinor == 0) {
                return "SSLv3";
            } else if (protocolMinor == 1) {
                return "TLSv1.0";
            } else if (protocolMinor == 2) {
                return "TLSv1.1";
            } else if (protocolMinor == 3) {
                return "TLSv1.2";
            }
        }
        return String.format("%d.%d", protocolMajor, protocolMinor);
    }

    private void parseSSLPlaintext(SSLPlaintext header, ByteBuffer buffer, int position) {
        header.handshakeMsgType = buffer.get(position++);
        header.protocolMajor = buffer.get(position++);
        header.protocolMinor = buffer.get(position++);

        if (header.handshakeMsgType == HandshakeType.CLIENT_HELLO.value) {
            clientHelloVersion = "SSLv2";
            clientRequested = getVersion(header);
        } else if (header.handshakeMsgType == HandshakeType.SERVER_HELLO.value) {
            serverResponded = getVersion(header);
        }
    }

    static class SSLPlaintext {
        public SSLPlaintext() {
        }

        byte handshakeMsgType;
        byte protocolMajor;
        byte protocolMinor;

        int length;
        int padding;
    }

    static class TLSPlaintext {
        public TLSPlaintext() {
        }

        byte contentType;
        byte protocolMajor;
        byte protocolMinor;

        int length;
    }

    enum ContentType {
        CHANGE_CIPHER_SPEC(20),
        ALERT(21),
        HANDSHAKE(22),
        APPLICATION_DATA(23);

        private final int value;
        private ContentType(final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }

        public static ContentType fromValue(byte value) {
            for (ContentType type : ContentType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum HandshakeType {
        HELLO_REQUEST(0),
        CLIENT_HELLO(1),
        SERVER_HELLO(2),
        CERTIFICATE(11),
        SERVER_KEY_EXCHANGE(12),
        CERTIFICATE_REQUEST(13),
        SERVER_HELLO_DONE(14),
        CERTIFICATE_VERIFY(15),
        CLIENT_KEY_EXCHANGE(16),
        FINISHED(20);

        private final int value;
        private HandshakeType(final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }

        public static HandshakeType fromValue(byte value) {
            for (HandshakeType type : HandshakeType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }
    }


    public static class HandshakeMsg {
        byte msgType;
        int length;
    }

    public static class ClientHello {
        // Protocol Version
        byte majorVersion;
        byte minorVersion;

        // Random
        long gmt_unix_time;
        byte[] random_bytes = new byte[28];

        // SessionID
        int sessionIdLen;
        byte[] sessionId;

        // CipherSuite cipher_suites<2..2^16-1>;

        // CompressionMethod compression_methods<1..2^8-1>;
    }

    enum AlertLevel {
        WARNING(1),
        FATAL(2);

        private final int value;
        private AlertLevel(final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }
    }

    enum AlertDescription {
        CLOSE_NOTIFY(0),
        UNEXPECTED_MESSAGE(10),
        BAD_RECORD_MAC(20),
        DECRYPTION_FAILED(21),
        RECORD_OVERFLOW(22),
        DECOMPRESSION_FAILURE(30),
        HANDSHAKE_FAILURE(40),
        NO_CERTIFICATE_RESERVED (41),
        BAD_CERTIFICATE(42),
        UNSUPPORTED_CERTIFICATE(43),
        CERTIFICATE_REVOKED(44),
        CERTIFICATE_EXPIRED(45),
        CERTIFICATE_UNKNOWN(46),
        ILLEGAL_PARAMETER(47),
        UNKNOWN_CA(48),
        ACCESS_DENIED(49),
        DECODE_ERROR(50),
        DECRYPT_ERROR(51),
        EXPORT_RESTRICTION_RESERVED(60),
        PROTOCOL_VERSION(70),
        INSUFFICIENT_SECURITY(71),
        INTERNAL_ERROR(80),
        USER_CANCELED(90),
        NO_RENEGOTIATION(100);

        private final int value;
        private AlertDescription(final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }
    }

    public class Alert {
        AlertLevel level;
        AlertDescription description;
    }
}
