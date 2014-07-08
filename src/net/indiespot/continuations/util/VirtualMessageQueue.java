package net.indiespot.continuations.util;

import net.indiespot.dependencies.CircularArrayList;
import de.matthiasmann.continuations.SuspendExecution;

public class VirtualMessageQueue {
	private final VirtualCondition messageQueueNotEmpty;
	private final CircularArrayList<VirtualMessage> messageQueue;

	public VirtualMessageQueue() {
		messageQueue = new CircularArrayList<>(1);
		messageQueueNotEmpty = new VirtualCondition();
	}

	public VirtualMessage pollMessage() {
		return messageQueue.pollFirst();
	}

	public VirtualMessage awaitMessage() throws SuspendExecution {
		while (messageQueue.isEmpty()) {
			messageQueueNotEmpty.await();
		}
		return messageQueue.removeFirst();
	}

	public void passMessage(Object message) {
		messageQueue.addLast(new VirtualMessage(message));
		messageQueueNotEmpty.signalAll();
	}
}
