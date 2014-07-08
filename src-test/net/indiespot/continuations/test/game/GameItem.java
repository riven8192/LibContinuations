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

import java.awt.*;
import java.util.*;
import java.util.List;

import net.indiespot.continuations.VirtualRunnable;
import net.indiespot.continuations.VirtualThread;
import net.indiespot.dependencies.Vec2;
import de.matthiasmann.continuations.*;

public class GameItem {
	public boolean displayLabels = true;
	public String name = "";
	public String task = null;
	public Color color = Color.BLACK;
	public Vec2 position = new Vec2();
	public float speed = 1.0f;
	public Vec2 travelTarget = null;
	public final ResourceGroup producingResources = new ResourceGroup();
	public final ResourceGroup consumingResources = new ResourceGroup();
	public final ResourceGroup holdingResources = new ResourceGroup();

	private static long id_gen = 1;
	private final long id;

	public GameItem() {
		this.id = id_gen++;
	}

	@Override
	public String toString() {
		String task = this.task;
		if (task == null) {
			task = "";
		} else {
			task = " (" + task + ")";
		}
		return String.valueOf(name) + " #" + this.id + task;
	}

	private List<VirtualThread> spawned;

	protected void spawn(VirtualRunnable task) {
		if (this.spawned == null) {
			this.spawned = new ArrayList<VirtualThread>();
		}

		VirtualThread thread = new VirtualThread(task);
		spawned.add(thread);
		thread.start();
	}

	private Action action;

	public void setAction(Action action) {
		if (this.action != null) {
			throw new IllegalStateException();
		}

		this.action = action;

		if (action != null) {
			Game.NEXT_TICK_ITEMS.add(this);
		}
	}

	public void tick() {
		if (action == null) {
			return;
		}

		if (action.perform()) {
			Game.NEXT_TICK_ITEMS.add(this);
		} else {
			action.end();
			action = null;
		}
	}

	public void onAdd() {
		//
	}

	public void onRemove() {
		//
	}

	private boolean alive = true;

	public boolean isAlive() {
		return alive;
	}

	public void kill() throws SuspendExecution {
		alive = false;
		Game.removeItem(this);

		{
			for (ResourceType type : consumingResources.types()) {
				consumingResources.get(type).drain();
			}
			for (ResourceType type : producingResources.types()) {
				producingResources.get(type).drain();
			}
			for (ResourceType type : holdingResources.types()) {
				holdingResources.get(type).drain();
			}
		}

		if (spawned != null) {
			for (VirtualThread thread : spawned) {
				thread.kill(false);
			}
			spawned.clear();
		}
	}

	public void moveTo(Vec2 pos) throws SuspendExecution {
		if (travelTarget == null) {
			travelTarget = new Vec2();
		}
		travelTarget.load(pos);
		MoveAction.moveTo(this, pos);
	}

	//

	public int transfer(ResourceType type, GameItem that) {
		ResourceHolder src = this.getSource(type);
		ResourceHolder dst = that.getDestination(type);
		if (src == null || dst == null) {
			return 0;
		}

		return ResourceHolder.transfer(src, dst);
	}

	public int transfer(ResourceType type, GameItem that, int max) {
		ResourceHolder src = this.getSource(type);
		ResourceHolder dst = that.getDestination(type);
		if (src == null || dst == null) {
			return 0;
		}

		return ResourceHolder.transfer(src, dst, max);
	}

	public int transferSlowly(ResourceType type, GameItem that, int maxToBeTransfered, int maxPerTransfer, int attemptInterval, int maxAttempts) throws SuspendExecution {
		ResourceHolder src = this.getSource(type);
		ResourceHolder dst = that.getDestination(type);
		if (src == null || dst == null) {
			return 0;
		}

		return ResourceHolder.transferSlowly(//
		   src, dst,//
		   maxToBeTransfered, maxPerTransfer, //
		   attemptInterval, maxAttempts);
	}

	public ResourceHolder getSource(ResourceType type) {
		ResourceHolder src = this.producingResources.getOrNull(type);
		if (src == null || src.isEmpty()) {
			src = this.holdingResources.getOrNull(type);
		}
		return src == null || src.isEmpty() ? null : src;
	}

	public ResourceHolder getDestination(ResourceType type) {
		ResourceHolder dst = this.consumingResources.getOrNull(type);
		if (dst == null || dst.isFull()) {
			dst = this.holdingResources.getOrNull(type);
		}
		return dst == null || dst.isFull() ? null : dst;
	}
}
