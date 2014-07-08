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

package net.indiespot.continuations.test.game2;

import static net.indiespot.continuations.test.game.ResourceType.*;
import net.indiespot.continuations.VirtualThread;
import net.indiespot.continuations.test.game.Game;
import net.indiespot.continuations.test.game.GameItem;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game.ResourceConversion;
import net.indiespot.continuations.test.game.ResourceHolder;
import net.indiespot.continuations.test.game1.*;
import net.indiespot.dependencies.Vec2;
import de.matthiasmann.continuations.*;

public class Lumberjack extends Human {

	final ResourceConversion makeLogs;

	public Lumberjack() {
		this.name = "lumberjack";

		this.consumingResources.register(TREE, new ResourceHolder(Misc.random(5, 8)));
		this.producingResources.register(LOG, new ResourceHolder(Misc.random(5, 8)));

		this.makeLogs = new ResourceConversion() {
			{
				this.consume(TREE, 2);
				this.produce(LOG, 1);
			}
		};
	}

	@Override
	public void ai() throws SuspendExecution {

		GameItem lastTree = null;

		while (true) {
			this.survive();

			lastTree = this.gatherWood(lastTree);

			this.dumpWood();

			VirtualThread.sleep(1000);
		}
	}

	public int chop(GameItem tree) throws SuspendExecution {

		int transfer = tree.transfer(TREE, this, 1);
		if (tree.producingResources.get(TREE).isEmpty()) {
			tree.kill();
		}

		return transfer;
	}

	private GameItem gatherWood(GameItem tree) throws SuspendExecution {
		while (!this.producingResources.get(LOG).isFull()) {
			this.survive();

			this.task = "looking for a tree";
			if (tree == null || !tree.isAlive()) {
				tree = this.findNearestTree();
				if (tree == null) {
					break;
				}
			}

			this.task = ((Math.random() < 0.1) ? 't' : 'w') + "alking to tree";
			this.moveTo(tree.position);

			this.task = "chopping tree";
			while (this.chop(tree) > 0) {
				VirtualThread.sleep(Misc.random(500, 1500));
				this.survive();
			}

			this.task = "chopping tree into logs";
			do {
				VirtualThread.sleep(Misc.random(500, 1500));
				this.survive();
			} while (this.makeLogs.convert(this));
		}
		return tree;
	}

	private void dumpWood() throws SuspendExecution {
		while (!this.producingResources.get(LOG).isEmpty()) {
			this.task = "looking for storage";
			GameItem storage = this.findNearestStorageWithFreeSpace();
			if (storage == null) {
				break;
			}

			this.task = "dragging logs to " + storage;
			this.moveTo(storage.position);

			this.survive();

			this.task = "dumping logs at " + storage;
			/* int dumped = */this.transfer(LOG, storage);

			VirtualThread.sleep(1000);
		}
	}

	private GameItem findNearestTree() throws SuspendExecution {
		return Game.nearestProducer(TREE, false, null, this.position);
	}

	private GameItem findNearestStorageWithFreeSpace() throws SuspendExecution {
		GameItem item1 = Game.nearestHolderWithSpace(LOG, null, this.position);
		GameItem item2 = Game.nearestConsumer(LOG, false, null, this.position);

		if (item1 != null && item2 != null) {
			float dist1 = Vec2.distanceSquared(item1.position, this.position);
			float dist2 = Vec2.distanceSquared(item2.position, this.position);
			return dist1 < dist2 ? item1 : item2;
		}

		return (item1 != null) ? item1 : item2;
	}
}
