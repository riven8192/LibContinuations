package net.indiespot.dependencies;

import java.util.Arrays;

public class IntList {
	private int[] array;
	private int size;

	public IntList() {
		this.array = new int[16];
	}

	public void clear() {
		this.size = 0;
	}

	public boolean isEmpty() {
		return this.size == 0;
	}

	public int size() {
		return this.size;
	}

	public int removeLast() {
		return this.array[--this.size];
	}

	public void add(int b) {
		if(this.size == this.array.length) {
			this.array = Arrays.copyOf(this.array, this.array.length * 2);
		}
		this.array[this.size++] = b;
	}

	public int get(int index) {
		if(index >= this.size) {
			throw new IndexOutOfBoundsException();
		}
		return this.array[index];
	}

	public int[] toArray() {
		return Arrays.copyOf(this.array, this.size);
	}

	public void fillArray(int[] dst, int off, int len) {
		if(len != this.size) {
			throw new IllegalStateException();
		}
		System.arraycopy(this.array, 0, dst, off, len);
	}
}