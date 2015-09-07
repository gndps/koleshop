package com.kolshop.kolshopbackend.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Hash {

	/**
	 * Hash password.
	 * 
	 * @param password
	 *            the password
	 * @return the string
	 */
	public static String hashPassword(String password) {
		String hashPassword = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(password.getBytes());
			BigInteger hash = new BigInteger(1, md5.digest());

			hashPassword = hash.toString(16);
			// hashPassword =password;

		} catch (NoSuchAlgorithmException exception) {
			exception.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashPassword;
	}

}
