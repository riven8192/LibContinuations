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

import static net.indiespot.continuations.VirtualThread.*;
import net.indiespot.continuations.*;
import de.matthiasmann.continuations.SuspendExecution;

public class VirtualThreadTestSchedule {
	public static void main(String[] args) {
		final VirtualProcessor processor = new VirtualProcessor();

		// create virtual threads (or green threads, if you will)

		final long started = now();
		final long finalWake = now() + 5000;

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				System.out.println("thread 1: a (" + (now() - started) + "ms)");
				sleep(1000);
				System.out.println("thread 1: b (" + (now() - started) + "ms)");
				sleep(1000);
				System.out.println("thread 1: c (" + (now() - started) + "ms)");
				sleep(1000);
				System.out.println("thread 1: d (" + (now() - started) + "ms)");
				yield();
				System.out.println("thread 1: e (" + (now() - started) + "ms)");
				wakeupAt(finalWake);
				System.out.println("thread 1: f (" + (now() - started) + "ms)");
			}
		}).start();

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				sleep(500);
				System.out.println("thread 2: a (" + (now() - started) + "ms)");
				sleep(2000);
				System.out.println("thread 2: b (" + (now() - started) + "ms)");
			}
		}).start();

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				sleep(1500);
				System.out.println("thread 3: a (" + (now() - started) + "ms)");
				sleep(100);
				System.out.println("thread 3: b (" + (now() - started) + "ms)");
				sleep(100);
				System.out.println("thread 3: c (" + (now() - started) + "ms)");
				sleep(100);
				System.out.println("thread 3: d (" + (now() - started) + "ms)");
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
