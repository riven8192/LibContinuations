package net.indiespot.continuations.test.basics;

import de.matthiasmann.continuations.SuspendExecution;
import net.indiespot.continuations.*;

public class VirtualThreadTestSerializable {

	@SuppressWarnings("serial")
	public static void main(String[] args) {
		final VirtualProcessor processor = new VirtualProcessor();

		final VirtualThread vt = new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				for (int i = 0; true; i++) {
					System.out.println("i=" + i);
					VirtualThread.sleep(101);

					if (i == 13) {
						VirtualThread.suspend();
					}
				}
			}
		});
		vt.start();

		final VirtualThread vt2 = new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {

				while (vt.getState() != VirtualThreadState.SUSPENDED) {
					VirtualThread.sleep(25);
				}

				System.out.println("serializing...");
				byte[] data = vt.serialize();
				System.out.println("deserializing " + data.length + " bytes...");
				VirtualThread vt = VirtualThread.deserialize(processor, data);
				System.out.println("resuming...");
				vt.resume();
			}
		});
		vt2.start();

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
