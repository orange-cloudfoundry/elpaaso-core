/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.mvn.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

/**
 * Mock Implementation for test purpose
 * 
 * 
 * uses ice.com tar utility lib
 * http://www.gjt.org/javadoc/com/ice/tar/TarEntry.html
 * 
 * 
 * @author APOG7416
 * 
 */
public class MockMvnRepoDaoImpl extends MvnRepoDaoImpl {

	private static Logger logger = LoggerFactory.getLogger(MockMvnRepoDaoImpl.class.getName());

	private static final int SSH_PORT = 22;
	private static final int SSH_TIMEOUT = 15000;


	/**
	 * put via SFTP a java String into a file
	 * 
	 * @param distFileName
	 */
	protected final void putStringToFile(String distFileName, InputStream inputStream) {

		Session session = null;
		ChannelSftp channelSftp = null;

		try {
			// Initialisation de la session SSH
			session = openJSchSession();

			// Initialisation du channel de communication
			channelSftp = getSftpChannel(session);
			channelSftp.connect();

			// Put
			logger.debug("sftp putStringToFile: putting file \n" + distFileName);
			channelSftp.put(inputStream, distFileName, ChannelSftp.OVERWRITE);
		} catch (JSchException e) {
			logger.error("sftp putStringToFile: " + e.getMessage());
			throw new TechnicalException(e);
		} catch (SftpException e) {
			logger.error("sftp putStringToFile: " + e.getMessage());
			throw new TechnicalException(e);
		} finally {
			// InputStream close
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new TechnicalException(e);
				}
			}
			// ChannelSftp close
			if ((channelSftp != null) && (channelSftp.isConnected())) {
				channelSftp.disconnect();
			}
			// Session close
			if ((session != null) && (session.isConnected())) {
				session.disconnect();
			}
		}
	}

	/**
	 * getSftpChannel
	 * 
	 * @param session
	 * @return
	 * @throws JSchException
	 * @throws JSchException
	 */
	private ChannelSftp getSftpChannel(Session session) throws JSchException {
		return (ChannelSftp) session.openChannel("sftp");
	}

	/**
	 * Open JSch session
	 * 
	 * @return
	 * @throws JSchException
	 */
	private Session openJSchSession() throws JSchException {
		JSch jsch;
		jsch = new JSch();
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		Session session;
		session = jsch.getSession("root", "10.193.125.214", SSH_PORT);
		session.setPassword("cloud");
		session.setTimeout(SSH_TIMEOUT);
		UserInfo ui = new MyUserInfo();
		session.setUserInfo(ui);
		session.setConfig(config);
		session.connect();
		return session;
	}

	/**
	 * Internal class
	 * 
	 * @author ufba7493
	 * 
	 */
	private static class MyUserInfo implements UserInfo {

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassphrase(String message) {
			return false;
		}

		@Override
		public boolean promptPassword(String message) {
			return true;
		}

		@Override
		public boolean promptYesNo(String message) {
			return false;
		}

		@Override
		public void showMessage(String message) {
			logger.debug(this.getClass().getName() + ": " + message);
		}
	}

	@Override
	public MavenReference resolveUrl(MavenReference mavenReference) {
		MavenReference result = new MavenReference(mavenReference);
		String accessUrl = super.mvnConsumerConfigurer.getPullPrimaryRepository().getUrl();
		try {
			// replace . from groupId with / to match a path on maven repo
			accessUrl += mavenReference.getGroupId().replace('.', '/');

			accessUrl += "/" + mavenReference.getArtifactId() + "/" + mavenReference.getVersion() + "/" + mavenReference.getArtifactId();
			if (mavenReference.getClassifier() != null && mavenReference.getClassifier().length() > 0) {
				accessUrl += "-" + mavenReference.getClassifier();
			}
			accessUrl += "." + mavenReference.getExtension();

			result.setAccessUrl(new URL(accessUrl));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
