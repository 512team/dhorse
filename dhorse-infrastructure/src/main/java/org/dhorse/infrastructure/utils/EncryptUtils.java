package org.dhorse.infrastructure.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptUtils {

	private static final Logger logger = LoggerFactory.getLogger(EncryptUtils.class);
	
	/**
	 * Splits a byte array into two.
	 *
	 * @param src byte array to split
	 * @param n   location to split the array
	 * @return a two dimensional array of the split
	 */
	private static byte[][] split(byte[] src, int n) {
		byte[] l;
		byte[] r;

		if (src.length <= n) {
			l = src;
			r = new byte[0];

		} else {
			l = new byte[n];
			r = new byte[src.length - n];
			System.arraycopy(src, 0, l, 0, n);
			System.arraycopy(src, n, r, 0, r.length);
		}

		byte[][] lr = { l, r };

		return lr;

	}

	/**
	 * Validates if a plaintext password matches a hashed version.
	 *
	 * @param digest   digested version
	 * @param password plaintext password
	 * @return if the two match
	 */
	public static boolean verifyPassword(String digest, String password) {
		String alg = null;
		int size = 0;

		if (digest.regionMatches(true, 0, "{SHA}", 0, 5)) {
			digest = digest.substring(5);
			alg = "SHA1";
			size = 20;

		} else if (digest.regionMatches(true, 0, "{SSHA}", 0, 6)) {
			digest = digest.substring(6);
			alg = "SHA1";
			size = 20;

		} else if (digest.regionMatches(true, 0, "{MD5}", 0, 5)) {
			digest = digest.substring(5);
			alg = "MD5";
			size = 16;

		} else if (digest.regionMatches(true, 0, "{SMD5}", 0, 6)) {
			digest = digest.substring(6);
			alg = "MD5";
			size = 16;

		}

		try {
			MessageDigest mDigest = MessageDigest.getInstance(alg);

			if (mDigest == null) {
				return false;
			}
			Decoder decoder = Base64.getDecoder();

			byte[] decodeBase64 = decoder.decode(digest);
			byte[][] hs = split(decodeBase64, size);
			byte[] hash = hs[0];
			byte[] salt = hs[1];

			mDigest.reset();
			mDigest.update(password.getBytes());
			mDigest.update(salt);

			byte[] pwhash = mDigest.digest();

			return MessageDigest.isEqual(hash, pwhash);

		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to verify password", e);
			return false;
		}
	}
}
