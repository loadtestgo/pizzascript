package com.loadtestgo.script.engine.internal.browsers.chrome;

import com.loadtestgo.util.Os;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generate Chrome extension ids for expanded directory extensions.
 *
 * Chrome normally generates the extension id from the .pem file, but
 * we don't have a .pem file for our expanded directory extension.
 *
 * This code should match Chrome 'id_util::GenerateIdForPath'
 */
public class ChromeExtensionId {
    /**
     * Generate an extension id given the path to the extension.
     *
     * @param path the path to the extension as provide to the chrome executable on
     *             launch.
     * @return the id of the extension
     */
    public static String GenerateIdForPath(String path) throws NoSuchAlgorithmException,
            DigestException, UnsupportedEncodingException {
        String newPath = MaybeNormalizePath(path);
        return GenerateId(newPath);
    }

    // Converts a normal hexadecimal string into the alphabet used by extensions.
    // We use the characters 'a'-'p' instead of '0'-'f' to avoid ever having a
    // completely numeric host, since some software interprets that as an IP
    // address.
    static void ConvertHexadecimalToIDAlphabet(byte[] id) throws UnsupportedEncodingException {
        for (int i = 0; i < id.length; ++i) {
            int c = id[i];
            int val = 0;
            if (c >= '0' && c <= '9') {
                val = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                val = (c - 'a') + 10;
            } else if (c >= 'A' && c <= 'F') {
                val = (c - 'A') + 10;
            }
            id[i] = (byte)('a' + val);
        }
    }

    // First 16 bytes of SHA256 hashed public key.
    static int kIdSize = 16;

    static void hash(byte[] input, byte[] output) throws NoSuchAlgorithmException, DigestException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] bytes = sha256.digest(input);
        System.arraycopy(bytes, 0, output, 0, output.length);
    }

    static String GenerateId(String input) throws DigestException,
            NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] hash = new byte[kIdSize];
        byte[] bytes = input.getBytes("UTF-8");
        hash(bytes, hash);
        byte[] output = hexEncode(hash).getBytes("UTF-8");
        ConvertHexadecimalToIDAlphabet(output);
        return new String(output);
    }

    static String hexEncode(byte[] val) {
        return String.format("%032x", new BigInteger(1, val));
    }

    static String MaybeNormalizePath(String path) {
        // Normalize any drive letter to upper-case. We do this for consistency with
        // net_utils::FilePathToFileURL(), which does the same thing, to make string
        // comparisons simpler.
        if (Os.isWin()) {
            char[] output = path.toCharArray();

            if (path.length() >= 2 && path.charAt(0) >= 'a' && path.charAt(0) <= 'z' &&
                    path.charAt(1) == ':') {
                output[0] += ('A' - 'a');
            }

            return new String(output);
        } else {
            return path;
        }
    }
}
