/*
 * Copyright (c) 2012, Enhanced Four
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Enhanced Four' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.indiespot.continuations.test.basics;

import net.indiespot.continuations.*;
import de.matthiasmann.continuations.SuspendExecution;

public class VirtualThreadTestThreadLocal {
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		final VirtualProcessor processor = new VirtualProcessor();

		final VirtualThreadLocal<String> threadLocal = new VirtualThreadLocal<String>() {
			protected String initialValue() {
				return "initial";
			}
		};

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				System.out.println("thread1.get=" + threadLocal.get());
				VirtualThread.yield();
				threadLocal.set("thread1");
				VirtualThread.yield();
				System.out.println("thread1.get=" + threadLocal.get());
			}
		}).start();

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				System.out.println("thread2.get=" + threadLocal.get());
				threadLocal.set("thread2");
				System.out.println("thread2.get=" + threadLocal.get());
			}
		}).start();

		// game loop
		do {
			processor.tick(now());

			try {
				Thread.sleep(1);
			} catch (InterruptedException exc) {
				// ignore
			}
		} while (processor.hasPendingTasks());
	}

	static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
