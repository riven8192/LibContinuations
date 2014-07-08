package net.indiespot.continuations;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

class VirtualThreadQueue extends AbstractQueue<VirtualThread> {
	private final VirtualProcessor proc;
	private final Comparator<VirtualThread> comp;
	private final PriorityQueue<VirtualThread> head;
	private ArrayList<VirtualThread>[] buckets;
	private int bucketsMask;
	private ArrayList<VirtualThread> tail, tailNext;

	private long bucketDuration;
	private long headTime;
	private int size;

	@SuppressWarnings("unchecked")
	public VirtualThreadQueue(VirtualProcessor proc, long bucketDuration) {
		if (bucketDuration <= 0) {
			throw new IllegalArgumentException();
		}
		this.proc = proc;
		this.bucketDuration = bucketDuration;
		this.comp = new VirtualProcessor.VirtualThreadComparator();

		headTime = proc.getCurrentTime();

		head = new PriorityQueue<>(11, comp);
		buckets = new ArrayList[1 << 3]; // must be POT
		bucketsMask = buckets.length - 1; // will be bit-mask
		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = new ArrayList<>(4);
		}
		tail = new ArrayList<>();
		tailNext = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	private void addBuckets() {
		ArrayList<VirtualThread>[] curr = buckets;
		ArrayList<VirtualThread>[] next = new ArrayList[curr.length << 1];
		for (int i = 0; i < curr.length; i++) {
			next[i] = curr[reindex(i)];
		}
		bucketsMask = next.length - 1;
		bucketOffset = 0;
		for (int i = curr.length; i < next.length; i++) {
			next[i] = new ArrayList<>(4);
		}
		buckets = next;

		// redistribute tail elements over new buckets (and tail)
		List<VirtualThread> copy = tail;
		tail = new ArrayList<>();
		this.addAll(copy);
	}

	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public VirtualThread peek() {
		this.sync();

		return head.peek();
	}

	@Override
	public VirtualThread poll() {
		this.sync();

		VirtualThread got = head.poll();
		if (got != null)
			size--;
		return got;
	}

	private int bucketOffset;

	private int reindex(int index) {
		return (bucketOffset + index) & bucketsMask;
	}

	@Override
	public boolean offer(VirtualThread thread) {
		this.sync();

		// insert VirtualThread into head, bucket or tail
		int bucketIndex = (int) ((thread.wakeUpAt - headTime) / bucketDuration);
		if (bucketIndex <= 0) {
			head.add(thread);
		} else {
			bucketIndex -= 1;

			if (bucketIndex < buckets.length) {
				buckets[reindex(bucketIndex)].add(thread);
			} else {
				tail.add(thread);

				while (this.isTailSplitNeeded()) {
					this.addBuckets();
				}
			}
		}

		size++;

		return true;
	}

	@Override
	public boolean remove(Object obj) {
		VirtualThread thread = (VirtualThread) obj;

		this.sync();

		// remove VirtualThread from head, bucket or tail
		int bucketIndex = (int) ((thread.wakeUpAt - headTime) / bucketDuration);
		if (bucketIndex <= 0) {
			boolean got = head.remove(thread);
			if (got) {
				size--;
			}
			return got;
		}

		bucketIndex -= 1;
		if (bucketIndex < buckets.length) {
			boolean got = buckets[reindex(bucketIndex)].remove(thread);
			if (got) {
				size--;
			}
			return got;
		}

		boolean got = tail.remove(thread);
		if (got) {
			size--;
		}
		return got;
	}

	protected boolean isTailSplitNeeded() {
		if (buckets.length > 10_000) {
			// prevent extreme growth
			return false;
		}
		if (tail.size() < 256) {
			// no need to split yet
			return false;
		}
		return true;
	}

	private long sync() {
		long procTime = proc.getCurrentTime();

		while (procTime >= headTime + bucketDuration) {
			headTime += bucketDuration;

			// move first bucket into 'head'
			ArrayList<VirtualThread> first = buckets[reindex(0)];
			first.trimToSize(); // shrink to typical size
			head.addAll(first);
			first.clear();

			ArrayList<VirtualThread> last = first;
			bucketOffset++;

			if (tail.isEmpty()) {
				continue;
			}

			// split 'tail' into new tail and last bucket
			long tailThreshold = headTime + bucketDuration * (buckets.length + 1);
			tail.trimToSize(); // shrink to typical size

			for (VirtualThread elem : tail) {
				if (elem.wakeUpAt < tailThreshold) {
					last.add(elem);
				} else {
					tailNext.add(elem);
				}
			}
			tail.clear();

			// swap
			ArrayList<VirtualThread> temp = tail;
			tail = tailNext;
			tailNext = temp;
		}

		return procTime;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<VirtualThread> iterator() {
		throw new UnsupportedOperationException();
	}
}
