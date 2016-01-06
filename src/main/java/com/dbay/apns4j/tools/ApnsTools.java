/*
 * Copyright 2013 DiscoveryBay Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dbay.apns4j.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateExpiredException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbay.apns4j.DateUtil;
import com.dbay.apns4j.model.Command;
import com.dbay.apns4j.model.FrameItem;

/**
 * @author RamosLi
 * 
 */
public abstract class ApnsTools {
	
	private final static Logger	LOG	= LoggerFactory.getLogger(ApnsTools.class);
	
	public final static byte[] generateData(List<FrameItem> list) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		int frameLength = 0;
		for (FrameItem item : list) {
			// itemId length = 1, itemDataLength = 2
			frameLength += 1 + 2 + item.getItemLength();
		}
		try {
			os.writeByte(Command.SEND_V2);
			os.writeInt(frameLength);
			for (FrameItem item : list) {
				os.writeByte(item.getItemId());
				os.writeShort(item.getItemLength());
				os.write(item.getItemData());
			}
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}
	
	@Deprecated
	public final static byte[] generateData(int id, int expire, byte[] token,
			byte[] payload) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		try {
			os.writeByte(Command.SEND);
			os.writeInt(id);
			os.writeInt(expire);
			os.writeShort(token.length);
			os.write(token);
			os.writeShort(payload.length);
			os.write(payload);
			os.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}
	
	private final static String[]	hexArr	= new String[] { "0", "1", "2",
			"3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
	
	public final static String encodeHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(hexArr[(b >> 4) & 0x0F]);
			sb.append(hexArr[b & 0x0F]);
		}
		return sb.toString();
	}
	
	public final static byte[] decodeHex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((hexCharIndex(hex.charAt(2 * i)) << 4) | hexCharIndex(hex
					.charAt(2 * i + 1)));
		}
		return bytes;
	}
	
	/**
	 * @param hex
	 * @return 0---15
	 */
	private final static int hexCharIndex(char hex) {
		int index = 0;
		if (hex >= '0' && hex <= '9') {
			index = hex - '0';
		} else if (hex >= 'a' && hex <= 'f') {
			index = hex - 'a' + 10;
		} else if (hex >= 'A' && hex <= 'F') {
			index = hex - 'A' + 10;
		} else {
			throw new IllegalArgumentException("Invalid hex char. " + hex);
		}
		return index;
	}
	
	public final static int parse4ByteInt(byte b1, byte b2, byte b3, byte b4) {
		return ((b1 << 24) & 0xFF000000) | ((b2 << 16) & 0x00FF0000)
				| ((b3 << 8) & 0x0000FF00) | (b4 & 0x000000FF);
	}
	
	public final static SocketFactory createSocketFactory(InputStream keyStore,
			String password, String keystoreType, String algorithm,
			String protocol) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException, KeyManagementException,
			CertificateExpiredException {
		
		char[] pwdChars = password.toCharArray();
		KeyStore ks = KeyStore.getInstance(keystoreType);
		ks.load(keyStore, pwdChars);
		
		// 检查证书是否过期
		Enumeration<String> enums = ks.aliases();
		String alias = "";
		if (enums.hasMoreElements()) {
			alias = enums.nextElement();
		}
		if (StringUtils.isNotEmpty(alias)) {
			X509Certificate certificate = (X509Certificate) ks
					.getCertificate(alias);
			if (null != certificate) {
				String type = certificate.getType();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书类型：type=[{}]", type);
				}
				
				int ver = certificate.getVersion();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书版本号：ver=[{}]", ver);
				}
				
				String name = certificate.getSubjectDN().getName();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书标题：subjectDN=[{}]", name);
				}
				
				String serialNumber = certificate.getSerialNumber()
						.toString(16);
				if (LOG.isInfoEnabled()) {
					LOG.info("证书序列号：serialNumber=[{}]", serialNumber);
				}
				
				String issuerDN = certificate.getIssuerDN().getName();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书发行者：issuerDN=[{}]", issuerDN);
				}
				
				String sigAlgName = certificate.getSigAlgName();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书签名算法：sigAlgName=[{}]", sigAlgName);
				}
				
				String publicAlgorithm = certificate.getPublicKey()
						.getAlgorithm();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书公钥算法：publicAlgorithm=[{}]", publicAlgorithm);
				}
				
				Date before = certificate.getNotBefore();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书开始日期：before=[{}]", DateFormatUtils.format(
							before, "yyyy-MM-dd HH:mm:ss"));
				}
				
				Date after = certificate.getNotAfter();
				if (LOG.isInfoEnabled()) {
					LOG.info("证书结束日期：after=[{}]", DateFormatUtils.format(after,
							"yyyy-MM-dd HH:mm:ss"));
				}
				
				// 判断证书是否
				long day = DateUtil.getNumberOfDaysBetween(after, new Date());
				if (day <= 0) {
					if (LOG.isWarnEnabled()) {
						LOG.warn("证书已经过期：[{}]天", Math.abs(day));
					}
					throw new CertificateExpiredException("证书已经过期：["
							+ Math.abs(day) + "]天");
				}
				
				if (LOG.isInfoEnabled()) {
					LOG.info("证书将在[{}]天后过期", day);
				}
			}
		}
		
		KeyManagerFactory kf = KeyManagerFactory.getInstance(algorithm);
		kf.init(ks, pwdChars);
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
		tmf.init((KeyStore) null);
		SSLContext context = SSLContext.getInstance(protocol);
		context.init(kf.getKeyManagers(), tmf.getTrustManagers(), null);
		
		return context.getSocketFactory();
	}
	
	// All data is specified in network order, that is big endian.
	public final static byte[] intToBytes(int num, int resultBytesCount) {
		byte[] ret = new byte[resultBytesCount];
		for (int i = 0; i < resultBytesCount; i++) {
			ret[i] = (byte) ((num >> ((resultBytesCount - 1 - i) * 8)) & 0xFF);
		}
		return ret;
	}
}
