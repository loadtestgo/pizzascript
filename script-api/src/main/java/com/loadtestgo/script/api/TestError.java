package com.loadtestgo.script.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class TestError {
    public String message;
    public int line;
    public int column;
    public List<StackElement> stackTrace;
    public String file;
    public ErrorType type;

    /**
     * Hash the error in a way that gives us a unique id
     */
    @JsonIgnore
    public byte[] getHash() {
         return calcHash(message, stackTrace, file, line);
    }

    static public byte[] calcHash(String errorMessage, List<StackElement> stackTrace, String file, int line) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(errorMessage.getBytes());
            if (stackTrace != null) {
                for (StackElement stackElement : stackTrace) {
                    if (stackElement.file != null) {
                        messageDigest.update(stackElement.file.getBytes());
                    }
                    messageDigest.update((byte) (stackElement.line >>> 24));
                    messageDigest.update((byte) (stackElement.line >>> 16));
                    messageDigest.update((byte) (stackElement.line >>> 8));
                    messageDigest.update((byte) stackElement.line);
                }
            } else {
                if (file != null) {
                    messageDigest.update(file.getBytes());
                }
                messageDigest.update((byte)(line >>> 24));
                messageDigest.update((byte)(line >>> 16));
                messageDigest.update((byte)(line >>> 8));
                messageDigest.update((byte)line);
            }
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s", type.name(), message);
    }
}
