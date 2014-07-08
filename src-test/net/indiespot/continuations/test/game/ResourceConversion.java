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

import java.util.*;

import net.indiespot.dependencies.IntList;

public class ResourceConversion {
	private final List<ResourceType> consumeTypes = new ArrayList<>();
	private final List<ResourceType> produceTypes = new ArrayList<>();
	private final IntList consumeAmount = new IntList();
	private final IntList produceAmount = new IntList();

	public void consume(ResourceType type, int amount) {
		if (type == null) {
			throw new NullPointerException();
		}
		if (amount <= 0) {
			throw new IllegalArgumentException();
		}

		this.consumeTypes.add(type);
		this.consumeAmount.add(amount);
	}

	public void produce(ResourceType type, int amount) {
		if (type == null) {
			throw new NullPointerException();
		}
		if (amount <= 0) {
			throw new IllegalArgumentException();
		}

		this.produceTypes.add(type);
		this.produceAmount.add(amount);
	}

	public boolean convert(GameItem item) {
		for (int i = 0; i < consumeTypes.size(); i++) {
			ResourceType type = consumeTypes.get(i);
			int take = consumeAmount.get(i);
			if (item.consumingResources.get(type).getFilledSpace() < take) {
				return false;
			}
		}

		for (int i = 0; i < produceTypes.size(); i++) {
			ResourceType type = produceTypes.get(i);
			int make = produceAmount.get(i);
			if (item.producingResources.get(type).getFreeSpace() < make) {
				return false;
			}
		}

		//

		for (int i = 0; i < consumeTypes.size(); i++) {
			ResourceType type = consumeTypes.get(i);
			int take = consumeAmount.get(i);
			if (item.consumingResources.get(type).take(take) != take) {
				throw new IllegalStateException();
			}
		}

		for (int i = 0; i < produceTypes.size(); i++) {
			ResourceType type = produceTypes.get(i);
			int make = produceAmount.get(i);
			if (item.producingResources.get(type).put(make) != make) {
				throw new IllegalStateException();
			}
		}

		return true;
	}
}
