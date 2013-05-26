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

package net.indiespot.continuations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import craterstudio.data.CircularArrayList;

public class VirtualProcessor implements Serializable {

	static final ThreadLocal<VirtualProcessor> LAST_DEFINED = new ThreadLocal<VirtualProcessor>();

	private final Queue<VirtualThread> scheduledThreadsQueue;
	private final ArrayList<VirtualThread> doScheduleNextTick;
	private final CircularArrayList<VirtualThread> doExecuteNextTick;

	public VirtualProcessor() {
		this(0);
	}

	public VirtualProcessor(int queueBucketSlotDuration) {
		this.nativeThread = Thread.currentThread();

		if (queueBucketSlotDuration > 0) {
			this.scheduledThreadsQueue = new VirtualThreadQueue(this, queueBucketSlotDuration);
		} else {
			this.scheduledThreadsQueue = new PriorityQueue<VirtualThread>(11, new VirtualThreadComparator());
		}
		this.doScheduleNextTick = new ArrayList<VirtualThread>();
		this.doExecuteNextTick = new CircularArrayList<VirtualThread>();

		this.uncaughtExceptionHandler = null;

		LAST_DEFINED.set(this);
	}

	//

	private final Thread nativeThread;

	void verifyCurrentThread() {
		if (nativeThread != Thread.currentThread()) {
			throw new IllegalThreadStateException();
		}
	}

	//

	private long currentTickTimestamp;

	public final long getCurrentTime() {
		return this.currentTickTimestamp;
	}

	//

	private VirtualUncaughtExceptionHandler uncaughtExceptionHandler;

	public void setUncaughtExceptionHandler(VirtualUncaughtExceptionHandler uncaughtExceptionHandler) {
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}

	//

	private VirtualThread current;

	/**
	 * Executes the virtual threads that are scheduled, if any. The new threads,
	 * or threads that are resumed or reschedule themselves to be executes will
	 * be executed in the next tick.
	 */

	public int tick(long now) {
		this.verifyCurrentThread();

		this.currentTickTimestamp = now;

		// drain events from previous tick into the current tick:
		this.scheduledThreadsQueue.addAll(this.doScheduleNextTick);
		this.doScheduleNextTick.clear();
		int execNowCountdown = this.doExecuteNextTick.size();
		int executedThreads = 0;

		while (true) {
			// do we have any virtual threads that didn't require scheduling?
			VirtualThread thread;
			if (--execNowCountdown >= 0) {
				thread = this.doExecuteNextTick.removeFirst();
			} else {
				// can we execute the next scheduled thread already?
				thread = this.scheduledThreadsQueue.peek();
				if (thread == null || thread.wakeUpAt > now) {
					break;
				}

				// we should execute it, so pop it off the queue
				thread = this.scheduledThreadsQueue.poll();
			}

			this.current = thread;
			this.current.prepareForExecution();

			VirtualThreadState state;
			try {
				state = thread.step();
			} catch (Throwable uncaught) {
				Throwable t = uncaught;
				do {
					cleanupVirtualStackTrace(t);
				} while ((t = t.getCause()) != null);

				if (this.uncaughtExceptionHandler == null) {
					String msg = VirtualProcessor.class.getSimpleName() + " encountered uncaught exception in " + thread.getName();
					new Throwable(msg, uncaught).printStackTrace();
				} else {
					this.uncaughtExceptionHandler.uncaughtException(thread, uncaught);
				}
				continue;
			} finally {
				this.current = null;
				executedThreads++;
			}

			if (state == VirtualThreadState.SLEEPING) {
				this.doScheduleNextTick.add(thread);
			} else if (state == VirtualThreadState.YIELDED) {
				this.doExecuteNextTick.addLast(thread);
			}
		}

		return executedThreads;
	}

	public boolean hasPendingTasks() {
		return !scheduledThreadsQueue.isEmpty() || !doScheduleNextTick.isEmpty() || !doExecuteNextTick.isEmpty();
	}

	boolean unschedule(VirtualThread thread) {
		if (this.current == thread) {
			throw new IllegalStateException("virtual thread cannot unschedule itself, use stop()");
		}
		if (this.doExecuteNextTick.remove(thread)) {
			return true;
		}
		if (this.doScheduleNextTick.remove(thread)) {
			return true;
		}
		if (this.scheduledThreadsQueue.remove(thread)) {
			return true;
		}
		throw new IllegalStateException("virtual thread was not scheduled");
	}

	void schedule(VirtualThread thread) {
		if (thread.wakeUpAt == VirtualThread.WAKEUP_IMMEDIATELY) {
			this.doExecuteNextTick.addLast(thread);
		} else {
			this.doScheduleNextTick.add(thread);
		}
	}

	static class VirtualThreadComparator implements Comparator<VirtualThread>, Serializable {
		@Override
		public int compare(VirtualThread o1, VirtualThread o2) {
			return o1.wakeUpAt < o2.wakeUpAt ? -1 : +1;
		}
	}

	private static void cleanupVirtualStackTrace(Throwable problem) {
		StackTraceElement[] trace = problem.getStackTrace();

		int cutoff = 0;
		for (StackTraceElement elem : trace) {
			if (elem.getClassName().equals(VirtualThread.class.getName())) {
				if (elem.getMethodName().equals("run")) {
					break;
				}
			}
			cutoff++;
		}

		trace = Arrays.copyOf(trace, cutoff);
		problem.setStackTrace(trace);
	}
}
