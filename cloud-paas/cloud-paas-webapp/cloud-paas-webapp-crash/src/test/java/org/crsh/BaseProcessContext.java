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
package org.crsh;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
//import org.crsh.text.Chunk;
//import org.crsh.text.Text;
import org.crsh.text.Screenable;
import org.crsh.text.Style;
import org.junit.Assert;

public class BaseProcessContext implements ShellProcessContext {

	public static BaseProcessContext create(Shell shell, String line) {
		return new BaseProcessContext(shell, line);
	}

	public static BaseProcessContext create(ShellProcess process) {
		return new BaseProcessContext(process);
	}

	/** . */
	private final LinkedList<String> output = new LinkedList<String>();

	/** . */
	private final LinkedList<String> input = new LinkedList<String>();

	/** . */
	private ShellResponse response;

	/** . */
	private final CountDownLatch latch;

	/** . */
	private int width;

	/** . */
	private int height;

	/** . */
	private ShellProcess process;

	private BaseProcessContext(ShellProcess process) {
		this.process = process;
		this.latch = new CountDownLatch(1);
		this.response = null;
		this.width = 32;
		this.height = 40;
	}

	private BaseProcessContext(Shell shell, String line) {
		this(shell.createProcess(line));
	}

	public BaseProcessContext cancel() {
		process.cancel();
		return this;
	}

	public BaseProcessContext execute() {
		process.execute(this);
		return this;
	}

	public ShellProcess getProcess() {
		return process;
	}

	public BaseProcessContext addLineInput(String line) {
		input.add(line);
		return this;
	}

	public BaseProcessContext assertLineOutput(String expected) {
		Assert.assertTrue(output.size() > 0);
		String test = output.removeFirst();
		Assert.assertEquals(expected, test);
		return this;
	}

	public BaseProcessContext assertNoOutput() {
		Assert.assertEquals(0, output.size());
		return this;
	}

	public BaseProcessContext assertNoInput() {
		Assert.assertEquals(0, input.size());
		return this;
	}

	public boolean takeAlternateBuffer() {
		return false;
	}

	public boolean releaseAlternateBuffer() {
		return false;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getProperty(String name) {
		return null;
	}

	public String readLine(String msg, boolean echo) {
		output.addLast(msg);
		return input.isEmpty() ? null : input.removeLast();
	}

	// public Class<Chunk> getConsumedType() {
	// return Chunk.class;
	// }
	//
	// public void provide(Chunk element) throws IOException {
	// if (element instanceof Text) {
	// CharSequence seq = ((Text)element).getText();
	// if (seq.length() > 0) {
	// output.add(seq.toString());
	// }
	// }
	// }

	public String getOutput() {
		StringBuilder buffer = new StringBuilder();
		for (String o : output) {
			buffer.append(o);
		}
		return buffer.toString();
	}

	public void flush() {
	}

	public void end(ShellResponse response) {
		this.response = response;
		this.latch.countDown();
	}

	public ShellResponse getResponse() {
		try {
			latch.await(60, TimeUnit.SECONDS);
			return response;
		} catch (InterruptedException e) {
			throw AbstractTestCase.failure(e);
		}
	}

	@Override
	public Screenable append(Style style) throws IOException {
		return this;
	}

	@Override
	public Screenable cls() throws IOException {
		return this;
	}

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		if (csq.length() > 0) {
			output.add(csq.toString());
		}
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		if (end != start) {
			if (start != 0 || end != csq.length()) {
				csq = csq.subSequence(start, end);
			}
			output.add(csq.toString());
		}
		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		output.add(Character.toString(c));
		return this;
	}
}
