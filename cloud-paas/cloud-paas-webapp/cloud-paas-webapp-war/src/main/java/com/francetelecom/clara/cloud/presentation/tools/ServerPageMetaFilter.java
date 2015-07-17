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
package com.francetelecom.clara.cloud.presentation.tools;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ServerPageMetaFilter
 * Class that display page meta information in html comment
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 * Sample usage : see ManageStatisticsImplTest
 * @version     : $Revision$
 * @see org.apache.wicket.markup.html.ServerTimeFilter
 */
public class ServerPageMetaFilter implements IResponseFilter {
	private static final Logger log = LoggerFactory.getLogger(ServerPageMetaFilter.class);

    private String retrieveRenderingDateStr() {
         SimpleDateFormat sdf = new SimpleDateFormat();
         sdf.applyPattern("MM/dd/yyyy HH:mm:ss");
         return sdf.format(new Date());
    }

    private String retrieveServerIP() {
        String serverIP = "unknown";
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhe) {
            log.error("unable to retrieve server ip : {}", uhe.getMessage(), uhe);
        }
        return serverIP;
    }

	/**
	 * @see org.apache.wicket.IResponseFilter#filter(AppendingStringBuffer)
	 */
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer) {
        String responseHtmlTagUsed = "</html>";
		int index = responseBuffer.indexOf(responseHtmlTagUsed);
		long timeTaken = System.currentTimeMillis() - RequestCycle.get().getStartTime();
		if (index != -1) {
			AppendingStringBuffer script = new AppendingStringBuffer(125);
			script.append("\n<!--");

            // server time used
			script.append("\n window.pagemeta.serverPageRenderingTime=' ");
			script.append(((double)timeTaken) / 1000);
			script.append("s';\n");

            // server ip
            script.append("\n window.pagemeta.serverIP='");
            script.append(retrieveServerIP());
            script.append("';\n");

            // rendering date
            script.append("\n window.pagemeta.renderingDate='");
            script.append(retrieveRenderingDateStr());
            script.append("';\n");


			script.append(" -->\n");
			responseBuffer.insert(index, script);
		}

		log.debug(timeTaken + "ms server time taken for request " +
			RequestCycle.get().getRequest().getUrl() + " response size: " + responseBuffer.length());
		return responseBuffer;
	}
}
