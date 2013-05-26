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

import java.util.Random;

import net.indiespot.continuations.*;
import de.matthiasmann.continuations.SuspendExecution;

public class VirtualThreadTestHeavyLoad {
	public static void main(String[] args) {
		final VirtualProcessor processor = new VirtualProcessor(1);

		final Random rndm = new Random(2346L);

		final long[] hash = new long[1];

		int virtualThreads = 0;

		long tA = System.nanoTime();

		// game loop
		long now = 0;
		do {
			if (virtualThreads < 2_500_000) {
				for (int i = 0; i < 10_000; i++) {
					virtualThreads += 1;

					new VirtualThread(new VirtualRunnable() {
						@Override
						public void run() throws SuspendExecution {
							while (true) {
								float f = rndm.nextFloat();
								sleep((int) (10 + 1000 * f * f * f));
							}
						}

						private void doh(int sleep) throws SuspendExecution {
							hash[0] *= 37;
							duh(sleep);
						}

						private void duh(int sleep) throws SuspendExecution {
							hash[0] ^= sleep;
							sleep(sleep);
						}
					}).start();
				}
			} else {
				//System.out.println("ok:done");
			}

			long t0 = System.nanoTime();
			int count = processor.tick(now++);
			long t1 = System.nanoTime();

			//System.out.println(count + " tasks took: " + (t1 - t0) / 1_000_000L + "ms, " + (t1 - t0) / Math.max(1, count) + "nanos/switch");

			if (now == 5000) {
				System.out.println(hash[0]);
				break;
			}
		} while (processor.hasPendingTasks());

		long tB = System.nanoTime();
		System.out.println((tB - tA) / 1000000 + "ms");
	}

	static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
