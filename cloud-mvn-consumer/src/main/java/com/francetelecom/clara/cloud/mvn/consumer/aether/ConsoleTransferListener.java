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
package com.francetelecom.clara.cloud.mvn.consumer.aether;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.eclipse.aether.transfer.AbstractTransferListener;

public class ConsoleTransferListener extends AbstractTransferListener {

	private PrintStream out;

	private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

	private int lastLength;

	public ConsoleTransferListener(PrintStream out) {
		this.out = (out != null) ? out : System.out;
	}

	@Override
	public void transferInitiated(TransferEvent event) {
		String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

		out.println(message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
	}

	private void pad(StringBuilder buffer, int spaces) {
		String block = "                                        ";
		while (spaces > 0) {
			int n = Math.min(spaces, block.length());
			buffer.append(block, 0, n);
			spaces -= n;
		}
	}

	@Override
	public void transferSucceeded(TransferEvent event) {
		transferCompleted(event);

		TransferResource resource = event.getResource();
		long contentLength = event.getTransferredBytes();
		if (contentLength >= 0) {
			String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
			String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

			String throughput = "";
			long duration = System.currentTimeMillis() - resource.getTransferStartTime();
			if (duration > 0) {
				DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
				double kbPerSec = (contentLength / 1024.0) / (duration / 1000.0);
				throughput = " at " + format.format(kbPerSec) + " KB/sec";
			}

			out.println(type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len + throughput + ")");
		}
	}

	@Override
	public void transferFailed(TransferEvent event) {
		transferCompleted(event);
		// event.getException().printStackTrace(out);
		out.println("Could not find " + transfertEventResourceFormatter(event).toString() + " - Message: " + event.getException().getLocalizedMessage());
	}

	private void transferCompleted(TransferEvent event) {
		downloads.remove(event.getResource());

		StringBuilder buffer = new StringBuilder(64);
		pad(buffer, lastLength);
		buffer.append('\r');
		out.print(buffer);
	}

	public void transferCorrupted(TransferEvent event) {
		event.getException().printStackTrace(out);
	}

	protected long toKB(long bytes) {
		return (bytes + 1023) / 1024;
	}

	private StringBuilder transfertEventResourceFormatter(TransferEvent event) {
		StringBuilder result = new StringBuilder(100);
		TransferResource resource = event.getResource();
		if (resource != null) {
			result.append(resource.getResourceName());
			result.append(" in repository : ");
			result.append(resource.getRepositoryUrl());
		}

		return result;

	}

}
