package net.indiespot.dependencies;

import java.io.Serializable;
import java.util.*;

public class CircularArrayList<T>  implements Serializable {
	private static final long serialVersionUID = -377269371668702946L;

	private T[] backing;
	private int offsetIndex;
	private int size;

	public CircularArrayList() {
		this(10);
	}

	@SuppressWarnings("unchecked")
	public CircularArrayList(int initialCapacity) {
		backing = (T[]) new Object[Math.max(1, initialCapacity)];
	}

	// common operations

	public int size() {
		return this.size;
	}

	public boolean isEmpty() {
		return this.size == 0;
	}

	public void clear() {
		for (int i = 0; i < size; i++) {
			this.set(i, null);
		}
		size = 0;
	}

	public T get(int index) {
		return backing[this.realIndexOf(index)];
	}

	public T set(int index, T value) {
		int realIndex = this.realIndexOf(index);
		T got = backing[realIndex];
		backing[realIndex] = value;
		return got;
	}

	// add

	public void addFirst(T value) {
		if (size == backing.length) {
			this.grow();
		}
		offsetIndex = wrapNearZero(offsetIndex - 1);
		backing[realIndexOf(0)] = value;
		size++;
	}

	public void addLast(T value) {
		if (size == backing.length) {
			this.grow();
		}
		backing[readIndexOfEnd()] = value;
		size++;
	}

	// remove

	public T removeFirst() throws NoSuchElementException {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}

		int index = this.realIndexOf(0);
		T value = backing[index];
		backing[index] = null;

		size--;

		offsetIndex = wrapPos(offsetIndex + 1);
		return value;
	}

	public T removeLast() throws NoSuchElementException {
		if (this.size == 0) {
			throw new NoSuchElementException();
		}
		size--;

		int index = this.readIndexOfEnd();
		T value = backing[index];
		backing[index] = null;

		return value;
	}

	//

	public boolean contains(T item) {
		return this.indexOf(item) != -1;
	}

	public int indexOf(T item) {
		for (int i = 0; i < this.size; i++) {
			if (backing[this.realIndexOf(i)] == item) {
				return i;
			}
		}
		return -1;
	}

	public boolean remove(T item) {
		int index = this.indexOf(item);
		if (index == -1) {
			return false;
		}

		// shift one slot to the left
		for (int k = index; k < this.size - 1; k++) {
			backing[this.realIndexOf(k)] = backing[this.realIndexOf(k + 1)];
		}
		backing[this.realIndexOf(this.size - 1)] = null;

		this.size--;
		return true;
	}

	// peek

	public T peekFirst() {
		return this.isEmpty() ? null : this.get(0);
	}

	public T peekLast() {
		return this.isEmpty() ? null : this.get(this.size - 1);
	}

	// poll

	public T pollFirst() {
		return this.isEmpty() ? null : this.removeFirst();
	}

	public T pollLast() {
		return this.isEmpty() ? null : this.removeLast();
	}

	//

	private void grow() {
		T[] newArray = Arrays.copyOf(backing, backing.length << 1);
		for (int i = 0; i < size; i++) {
			newArray[i] = backing[wrapPos(offsetIndex + i)];
		}

		offsetIndex = 0;
		backing = newArray;
	}

	private final int realIndexOf(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		return wrapPos(offsetIndex + index);
	}

	private final int readIndexOfEnd() {
		return wrapPos(offsetIndex + size);
	}

	private final int wrapPos(int value) {
		return value % backing.length;
	}

	private final int wrapNearZero(int value) {
		return (value + backing.length) % backing.length;
	}

	// private final int wrapAny(int value) {
	// return ((value % backing.length) + backing.length) % backing.length;
	// }
}