package net.indiespot.continuations.test.basics;

import de.matthiasmann.continuations.SuspendExecution;
import net.indiespot.continuations.*;
import net.indiespot.continuations.util.VirtualExchanger;

public class VirtualThreadTestExchanger {

	public static void main(String[] args) {
		final VirtualProcessor processor = new VirtualProcessor();

		final VirtualExchanger<String> ex = new VirtualExchanger<>();

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				String got = ex.exchange("object1a");
				System.out.println("thread1 got: " + got);
				
				got = ex.exchange("object1b");
				System.out.println("thread1 got: " + got);
			}
		}).start();
		System.out.println("start thread1");

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				String got = ex.exchange("object2a");
				System.out.println("thread2 got: " + got);
				
				got = ex.exchange("object2b");
				System.out.println("thread2 got: " + got);
			}
		}).start();
		System.out.println("start thread2");

		// game loop
		do {
			processor.tick(now());

			try {
				Thread.sleep(1);
			} catch (InterruptedException exc) {
				// ignore
			}
		} while (processor.hasPendingTasks());

		System.out.println("stopped");
	}

	static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
