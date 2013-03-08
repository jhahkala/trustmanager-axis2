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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.glite.security.SecurityContext;
import org.glite.security.SecurityInfo;
import org.glite.security.SecurityInfoContainer;
import org.glite.security.util.DNHandler;
import java.lang.Exception;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import javax.servlet.ServletRequest;

/**
 * EchoServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
/**
 * Implements a security test and example web service.
 * 
 * @author Joni Hahkala <joni.hahkala@cern.ch>
 */
public class EchoServiceSkeleton {

	/**
	 * The main method of the test web application.
	 * 
	 * @return a string that contains a lot of information that is digged from the user credentials etc.
	 * 
	 * @throws java.rmi.RemoteException in case of problems returns an exception that contains the information so far
	 *             and the information of the error
	 */
	public org.glite.security.trustmanager.axis2.GetAttributesResponseDocument getAttributes()
			throws RemoteExceptionException {

		// System.out.println("Entering getAttributes");

		StringBuffer buf = new StringBuffer();

		buf.append("EchoSecurityService\n\n");

		buf.append("Hello, this is the EchoService web service.\n");
		buf.append("This web service prints out all the security related info available from the client.\n");

		try {
			MessageContext messageContext = MessageContext.getCurrentMessageContext();
			if (messageContext == null) {
				throw new Exception("No MessageContext found, method probably not called inside a web service");
			}

			ServletRequest req = (ServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

			if (req == null) {
				buf.append("SOAP Authorization: MC_HTTP_SERVLETREQUEST is null");
			} else {
				SecurityContext sc = new SecurityContext();
				SecurityContext.setCurrentContext(sc);

				X509Certificate[] cert = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");

				/* Client certificate found. */
				sc.setClientCertChain(cert);

				// get and store the IP address of the other party
				String remote = req.getRemoteAddr();
				sc.setRemoteAddr(remote);

				// trigger the initialization of the certificate stuff in request.
				req.getAttribute("javax.servlet.request.key_size");

				// get the session id
				String sslId = (String) req.getAttribute("javax.servlet.request.ssl_session");
				sc.setSessionId(sslId);

				buf.append("Connection from \"" + remote + "\" by " + sc.getClientName());
				// /////////////////////////////
			}

			SecurityInfo secInfo = SecurityInfoContainer.getSecurityInfo();
			X509Certificate[] cert = secInfo.getClientCertChain();
//			VOMSValidator validator = ((SecurityContext) secInfo).getVOMSValidator();
//			validator.validate();

			buf.append("You're connecting from: " + secInfo.getRemoteAddr() + "\n");
			buf.append("The session ID for this connection is: " + secInfo.getSessionId() + "\n");
			buf.append("Your DN is: " + secInfo.getClientName() + "\n");
			buf.append("Issued by: " + secInfo.getIssuerName() + "\n");
			buf.append("You authenticated with a certificate "
					+ DNHandler.getSubject(secInfo.getClientCertChain()[0]).getRFC2253() + "\n");
			buf.append("Your final certificate subject is: " + DNHandler.getSubject(cert[0]) + "\n");

			boolean proxy = false;
			try {
				DNHandler.getSubject(cert[0]).withoutLastCN(true);
				proxy = true;
			} catch (Exception e) {
				proxy = false;
			}
			buf.append("Your end cert is: " + (proxy ? "proxy certificate" : "end-user certificate") + "\n\n");

			buf.append("Your cert is: \n");
			buf.append(secInfo.getClientCert().toString());
		} catch (Exception e) {
			buf.append("Error while handling the certificate chain:\n");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			e.printStackTrace();
			buf.append(sw.toString());
			throw new RemoteExceptionException(buf.toString());
		}

		buf.append("\nFinished\n");

		GetAttributesResponseDocument doc = GetAttributesResponseDocument.Factory.newInstance();
		GetAttributesResponseDocument.GetAttributesResponse res = GetAttributesResponseDocument.GetAttributesResponse.Factory
				.newInstance();
		res.setReturn(buf.toString());
		System.out.println(buf.toString());
		doc.setGetAttributesResponse(res);

		return doc;

		// return "hello";

	}
}
