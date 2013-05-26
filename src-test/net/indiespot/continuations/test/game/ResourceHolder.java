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

package net.indiespot.continuations.test.game;

import net.indiespot.continuations.VirtualThread;
import de.matthiasmann.continuations.*;

public class ResourceHolder {
	private int holding;
	private int capacity;

	public ResourceHolder(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException();
		}
		this.capacity = capacity;
	}

	public ResourceHolder(int holding, int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException();
		}
		if (holding < 0) {
			throw new IllegalArgumentException();
		}
		if (holding > capacity) {
			throw new IllegalArgumentException();
		}
		this.holding = holding;
		this.capacity = capacity;
	}

	public void grow(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException();
		}
		this.capacity += amount;
	}

	public void shrink(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException();
		}
		if (this.capacity - amount < this.holding) {
			throw new IllegalStateException();
		}
		this.capacity -= amount;
	}

	public boolean isEmpty() {
		return this.holding == 0;
	}

	public float getRatio() {
		return (float) this.holding / this.capacity;
	}

	public boolean isFull() {
		return this.holding == this.capacity;
	}

	public int getFilledSpace() {
		return this.holding;
	}

	public int getFreeSpace() {
		return this.capacity - this.holding;
	}

	public int drain() {
		return this.take(this.getFilledSpace());
	}

	public int fill() {
		return this.put(this.getFreeSpace());
	}

	public int put(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException();
		}
		int transfer = Math.min(amount, this.getFreeSpace());
		this.holding += transfer;
		return transfer;
	}

	public int take(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException();
		}
		int transfer = Math.min(amount, this.holding);
		this.holding -= transfer;
		return transfer;
	}

	@Override
	public String toString() {
		return this.holding + "/" + this.capacity;
	}

	//

	public static int transfer(ResourceHolder src, ResourceHolder dst) {
		return transfer(src, dst, Integer.MAX_VALUE);
	}

	public static int transfer(ResourceHolder src, ResourceHolder dst, int max) {
		if (max < 0) {
			throw new IllegalArgumentException("cannot transfer less than zero resources: " + max);
		}

		int transfer = Math.min(src.getFilledSpace(), dst.getFreeSpace());

		transfer = Math.min(max, transfer);

		if (src.take(transfer) != transfer) {
			throw new IllegalStateException();
		}

		if (dst.put(transfer) != transfer) {
			throw new IllegalStateException();
		}

		return transfer;
	}

	public static int transferSlowly(ResourceHolder src, ResourceHolder dst, int maxToBeTransfered, int maxPerTransfer, int attemptInterval, int maxAttempts) throws SuspendExecution {

		int rem = maxToBeTransfered;

		while (true) {
			int move = Math.min(rem, maxPerTransfer);
			if (move <= 0) {
				break;
			}

			int moved = ResourceHolder.transfer(src, dst, move);
			if (moved == 0) {
				break;
			}

			VirtualThread.sleep(attemptInterval);
		}

		return maxToBeTransfered - rem;
	}
}