/*
 * Copyright (c) Members of the EGEE Collaboration. 2004. See
 * http://www.eu-egee.org/partners/ for details on the copyright holders.
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

package org.glite.security.trustmanager.axis2;

import org.glite.security.trustmanager.ContextWrapper;

import org.apache.log4j.Logger;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import java.util.Properties;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.lang.Exception;

import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * AXIS2SocketFactory.java
 * 
 * @author Andrei Krueger, Joni Hahkala Created on November 15, 2007 - based on AXISSocketFactory.java
 */
public class AXIS2SocketFactory implements SecureProtocolSocketFactory {
	private static final Logger LOGGER = Logger.getLogger("org.glite.security.trustmanager.axis2.AXIS2SocketFactory");

	/** Thread local storage for the thread specific client properties. */
	private static ThreadLocal theAXIS2SocketFactoryProperties = new ThreadLocal();

	/**
	 * Gets the threadlocal properties object if it is set, otherwise returns System properties object.
	 * 
	 * @return java.util.Properties with the settings of the current thread.
	 */
	public static Properties getCurrentProperties() {
		Properties thisProperties = (Properties) theAXIS2SocketFactoryProperties.get();

		// if nothing was set, then fall back to global settings
		if (thisProperties == null) {
			thisProperties = System.getProperties();
		}

		return thisProperties;
	}

	/** Clears the thread specific properties object. */
	public static void clearCurrentProperties() {
		theAXIS2SocketFactoryProperties.set(null);
	}

	/**
	 * @param cp the Properties associated with the current thread
	 */
	public static void setCurrentProperties(Properties cp) {
		theAXIS2SocketFactoryProperties.set(cp);
	}

	/** Creates a new instance of AXIS2SocketFactory */
	public AXIS2SocketFactory() {
		// do nothing
	}

	/** Creates a new SSLSocket bound to ContextWrapper **/
	private Socket createSocket() throws IOException {
		SSLSocket socket;
		try {
			ContextWrapper contextWrapper = new ContextWrapper(getCurrentProperties());
			socket = (javax.net.ssl.SSLSocket) contextWrapper.getSocketFactory().createSocket();
			socket.setEnabledProtocols(new String[] { contextWrapper.getContext().getProtocol() });
		} catch (Exception e) {
			LOGGER.fatal("createSocket(): SSL socket creation failed : " + e.getMessage(), e);
			throw new IOException(e.toString());
		}
		return socket;
	}

	/**
	 * Creates a new SSLSocket bound to ContextWrapper and layered over an existing socket. UNIMPLEMENTED
	 **/
	@SuppressWarnings("unused")
	private Socket createSocket(Socket s, boolean autoclose) throws IOException {
		LOGGER.fatal("createSocket(s, ac): function unimplemented");
		throw new IOException("createSocket(s, ac) unimplemented");
	}

	/**
	 * Connect socket to remote host.
	 * 
	 * @param socket the socket to be connected
	 * @param remoteaddr remote host and port
	 * @param localaddr optional local host and port
	 * @param timeout optional timeout, default if used if timeout == 0
	 * 
	 * @return original socket
	 **/
	private final Socket connectSocket(Socket socket, SocketAddress remoteaddr, SocketAddress localaddr, int timeout)
			throws IOException {
		int newTimeout = timeout;
		if (localaddr != null) {
			socket.bind(localaddr);
		}

		// if no timeout is given, see if the property is set and use that
		if (timeout == 0) {
			String timeoutString = getCurrentProperties().getProperty(ContextWrapper.SSL_TIMEOUT_SETTING,
					ContextWrapper.TIMEOUT_DEFAULT);
			newTimeout = Integer.parseInt(timeoutString);
		}

		socket.setSoTimeout(newTimeout);
		socket.connect(remoteaddr, newTimeout);

		return socket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int,
	 * java.net.InetAddress, int, org.apache.commons.httpclient.params.HttpConnectionParams)
	 */
	public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort,
			final HttpConnectionParams params) throws IOException, UnknownHostException {
		Socket socket = createSocket();

		int timeout = params.getConnectionTimeout();
		SocketAddress remoteaddr = new InetSocketAddress(host, port);
		SocketAddress localaddr = new InetSocketAddress(localHost, localPort);

		connectSocket(socket, remoteaddr, localaddr, timeout);
		return socket;
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
	 **/
	public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
			throws IOException, UnknownHostException {
		Socket socket = createSocket();

		SocketAddress remoteaddr = new InetSocketAddress(host, port);
		SocketAddress localaddr = new InetSocketAddress(localHost, localPort);

		connectSocket(socket, remoteaddr, localaddr, 0);
		return socket;
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
	 **/
	public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
		Socket socket = createSocket();

		SocketAddress remoteaddr = new InetSocketAddress(host, port);

		connectSocket(socket, remoteaddr, null, 0);
		return socket;
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
	 **/
	public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose)
			throws IOException, UnknownHostException {
		Socket socket = createSocket(s, autoClose);

		SocketAddress remoteaddr = new InetSocketAddress(host, port);

		connectSocket(socket, remoteaddr, null, 0);
		return socket;
	}

}
