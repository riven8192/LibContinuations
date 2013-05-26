package net.indiespot.continuations.test.basics;

import static net.indiespot.continuations.VirtualThread.sleep;
import de.matthiasmann.continuations.SuspendExecution;
import net.indiespot.continuations.*;
import net.indiespot.continuations.util.VirtualCondition;
import net.indiespot.continuations.util.VirtualLock;

public class VirtualThreadTestBoundedBuffer {

	static class BoundedBuffer {
		final VirtualLock lock = new VirtualLock();
		final VirtualCondition notFull = lock.newCondition();
		final VirtualCondition notEmpty = lock.newCondition();

		final Object[] items;
		public int putptr, takeptr, count;

		public BoundedBuffer(int cap) {
			items = new Object[cap];
		}

		public void put(Object x) throws SuspendExecution {
			lock.lock();
			try {
				while (count == items.length) {
					notFull.await();
				}

				items[putptr] = x;
				if (++putptr == items.length) {
					putptr = 0;
				}
				++count;

				notEmpty.signal();
			} finally {
				lock.unlock();
			}
		}

		public Object take() throws SuspendExecution {
			lock.lock();
			try {
				while (count == 0) {
					notEmpty.await();
				}

				Object x = items[takeptr];
				if (++takeptr == items.length) {
					takeptr = 0;
				}
				--count;

				notFull.signal();
				return x;
			} finally {
				lock.unlock();
			}
		}
	}

	public static void main(String[] args) {
		final VirtualProcessor processor = new VirtualProcessor();

		final BoundedBuffer bb = new BoundedBuffer(10);

		final int slowDownFactor = 10;

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				for (int i = 0; true; i++) {
					bb.put(Integer.valueOf(i));

					// varying production rate
					sleep(slowDownFactor * (5 + (i / 10) % 15));
				}
			}
		}).start();

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				while (true) {
					System.out.println("size=" + bb.count);
					bb.take();

					// constant consumption rate
					sleep(slowDownFactor * 10);
				}
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
