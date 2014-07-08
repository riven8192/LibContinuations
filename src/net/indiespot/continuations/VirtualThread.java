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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.atomic.*;

import net.indiespot.continuations.util.VirtualMessage;
import net.indiespot.continuations.util.VirtualMessageQueue;
import de.matthiasmann.continuations.*;
import de.matthiasmann.continuations.Coroutine.State;

@SuppressWarnings("serial")
public class VirtualThread implements CoroutineProto, VirtualRunnable {

	private static final AtomicLong ID_PROVIDER = new AtomicLong();

	private final VirtualRunnable task;
	private final Coroutine coroutine;

	@SuppressWarnings("rawtypes")
	Map locals;

	public VirtualThread(VirtualRunnable task) {
		this(task, null);
	}

	public VirtualThread(VirtualRunnable task, String name) {
		this.task = task;
		this.coroutine = new Coroutine(this, 1);
		this.id = ID_PROVIDER.incrementAndGet();
		this.state = VirtualThreadState.NEW;
		this.name = (name == null) ? this.getClass().getSimpleName() + "#" + this.id : name;
	}

	//

	private VirtualProcessor proc;

	public VirtualProcessor getProcessor() {
		return assertNotNull(this.proc);
	}

	//

	private final String name;

	public String getName() {
		return name;
	}

	//

	private VirtualThreadState state;

	public VirtualThreadState getState() {
		return state;
	}

	//

	public void start() {
		this.start(VirtualProcessor.LAST_DEFINED.get());
	}

	public void start(VirtualProcessor proc) {
		assertNotNull(proc);
		assertNull(this.proc);
		assertTrue(this.state == VirtualThreadState.NEW);

		proc.verifyCurrentThread();

		this.proc = proc;
		this.state = VirtualThreadState.RUNNABLE;
		this.wakeUpAt = WAKEUP_IMMEDIATELY;
		this.proc.schedule(this);
	}

	public void spawn(VirtualRunnable task) {
		new VirtualThread(task).start(this.proc);
	}

	void prepareForExecution() {
		this.state = VirtualThreadState.RUNNABLE;
	}

	public void resume() {
		this.proc.verifyCurrentThread();

		if (this.state != VirtualThreadState.SUSPENDED) {
			throw new IllegalStateException("virtual thread not suspended");
		}

		this.state = VirtualThreadState.RUNNABLE;
		this.wakeUpAt = WAKEUP_IMMEDIATELY;
		this.proc.schedule(this);
	}

	public void wake() {
		this.proc.verifyCurrentThread();

		if (this.state != VirtualThreadState.SLEEPING) {
			throw new IllegalStateException("virtual thread not sleeping");
		}

		proc.unschedule(this);
		this.wakeUpAt = VirtualThread.WAKEUP_IMMEDIATELY;
		proc.schedule(this);
	}

	public void kill() {
		this.kill(true);
	}

	public void kill(boolean excIfNotScheduled) {
		this.proc.verifyCurrentThread();

		switch (this.state) {
			case NEW:
			case SUSPENDED:
				break;

			case TERMINATED:
			case YIELDED:
			case RUNNABLE:
			case SLEEPING:
				if (!this.getProcessor().unschedule(this)) {
					if (excIfNotScheduled) {
						throw new IllegalStateException("thread not scheduled");
					}
				}
				break;
		}

		this.state = VirtualThreadState.TERMINATED;
	}

	static final long WAKEUP_IMMEDIATELY = 0;
	long wakeUpAt;

	@Override
	public final void coExecute() throws SuspendExecution {
		this.run();
	}

	@Override
	public void run() throws SuspendExecution {
		this.task.run();
	}

	VirtualThreadState step() {
		// if (state != VirtualThreadState.RUNNABLE) {
		// throw new IllegalStateException();
		// }

		coroutine.run();

		if (coroutine.getState() == State.FINISHED) {
			state = VirtualThreadState.TERMINATED;
		}
		return state;
	}

	private final long id;

	public long getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return (int) this.id;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof VirtualThread) && ((VirtualThread) obj).id == this.id;
	}

	//

	private VirtualMessageQueue messageQueue;

	public static VirtualMessage pollMessage() {
		VirtualThread thread = VirtualThread.currentThread();
		return (thread.messageQueue == null) ? null : thread.messageQueue.pollMessage();
	}

	public static VirtualMessage awaitMessage() throws SuspendExecution {
		VirtualThread thread = VirtualThread.currentThread();
		while (thread.messageQueue == null) {
			VirtualThread.yield(); // relatively busy wait
		}
		return thread.messageQueue.awaitMessage();
	}

	public void passMessage(Object message) {
		if (VirtualThread.currentThread() == this) {
			throw new IllegalStateException("cannot pass message to self");
		}
		if (messageQueue == null) {
			messageQueue = new VirtualMessageQueue();
		}
		messageQueue.passMessage(message);
	}

	//

	public static void sleep(long timeout) throws SuspendExecution {
		VirtualThread thread = currentThread();
		thread.wakeUpAt = thread.getProcessor().getCurrentTime() + Math.max(timeout, WAKEUP_IMMEDIATELY);
		thread.state = VirtualThreadState.SLEEPING;
		Coroutine.yield();
	}

	public static void wakeupAt(long at) throws SuspendExecution {
		VirtualThread thread = currentThread();
		thread.wakeUpAt = at;
		thread.state = VirtualThreadState.SLEEPING;
		Coroutine.yield();
	}

	public static void yield() throws SuspendExecution {
		currentThread().state = VirtualThreadState.YIELDED;
		Coroutine.yield();
	}

	public static void suspend() throws SuspendExecution {
		currentThread().state = VirtualThreadState.SUSPENDED;
		Coroutine.yield();
	}

	public static void stop() throws SuspendExecution {
		currentThread().state = VirtualThreadState.TERMINATED;
		Coroutine.yield();
	}

	public byte[] serialize() throws SuspendExecution {
		this.proc.verifyCurrentThread();

		if (state != VirtualThreadState.SUSPENDED) {
			throw new IllegalStateException("virtual thread must be suspended");
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();

			return baos.toByteArray();
		} catch (IOException exc) {
			throw new IllegalStateException(exc);
		}
	}

	public static VirtualThread deserialize(VirtualProcessor proc, byte[] data) {
		proc.verifyCurrentThread();

		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			try {
				VirtualThread thread = (VirtualThread) ois.readObject();
				if (thread.state != VirtualThreadState.SUSPENDED) {
					throw new IllegalStateException("virtual thread must be suspended");
				}
				thread.proc = proc;
				return thread;
			} finally {
				ois.close();
			}
		} catch (IOException exc) {
			throw new IllegalStateException(exc);
		} catch (ClassNotFoundException exc) {
			throw new IllegalStateException(exc);
		}
	}

	//

	public static final VirtualThread currentThread() {
		VirtualThread thread = peekCurrentThread();
		if (thread == null) {
			throw new IllegalStateException("current thread is not a continuation");
		}
		return thread;
	}

	public static final VirtualThread peekCurrentThread() {
		Coroutine coroutine = Coroutine.getActiveCoroutine();
		if (coroutine == null) {
			return null;
		}
		return assertNotNull((VirtualThread) coroutine.getProto());
	}

	public static boolean isCurrentThreadContinuation() {
		return Coroutine.getActiveCoroutine() != null;
	}

	//

	private static <T> T assertNotNull(T value) {
		if (value == null) {
			throw new IllegalArgumentException("argument must not be null");
		}
		return value;
	}

	private static <T> T assertNull(T value) {
		if (value != null) {
			throw new IllegalArgumentException("argument must be null");
		}
		return value;
	}

	private static boolean assertTrue(boolean value) {
		if (!value) {
			throw new IllegalArgumentException("argument must be true");
		}
		return value;
	}
}
