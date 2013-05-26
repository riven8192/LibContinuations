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

import de.matthiasmann.continuations.*;

public class ResourceGroup {
	private final Map<ResourceType, ResourceHolder> mapping;

	public ResourceGroup() {
		this.mapping = new HashMap<ResourceType, ResourceHolder>();
	}

	public ResourceHolder register(ResourceType type, ResourceHolder holder) {
		if (this.mapping.containsKey(type)) {
			throw new IllegalStateException();
		}
		this.mapping.put(type, holder);
		return holder;
	}

	public ResourceHolder get(ResourceType type) {
		ResourceHolder holder = this.mapping.get(type);
		if (holder == null) {
			throw new NoSuchElementException("type: " + type.name());
		}
		return holder;
	}

	public ResourceHolder getOrNull(ResourceType type) {
		return this.mapping.get(type);
	}

	public Set<ResourceType> types() {
		return this.mapping.keySet();
	}

	@Override
	public String toString() {
		return this.mapping.toString();
	}

	//

	public static int transfer(ResourceType type, ResourceGroup src, ResourceGroup dst) {
		return ResourceHolder.transfer(src.get(type), dst.get(type));
	}

	public static int transfer(ResourceType type, ResourceGroup src, ResourceGroup dst, int max) {
		return ResourceHolder.transfer(src.get(type), dst.get(type), max);
	}

	public static int transferSlowly(ResourceType type, ResourceGroup src, ResourceGroup dst, int maxToBeTransfered, int maxPerTransfer, int attemptInterval, int maxAttempts) throws SuspendExecution {
		return ResourceHolder.transferSlowly(src.get(type), dst.get(type), maxToBeTransfered, maxPerTransfer, attemptInterval, maxAttempts);
	}
}