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

package net.indiespot.continuations.test.game4;

import net.indiespot.continuations.*;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game1.*;
import net.indiespot.dependencies.CircularArrayList;
import net.indiespot.dependencies.Vec2;
import de.matthiasmann.continuations.*;

public class Soldier extends Human {
	public Commander commander;

	public Soldier() {
		this.name = "soldier";
		this.displayLabels = false;
	}

	@Override
	public void ai() throws SuspendExecution {
		while (true) {
			if (commander == null) {
				this.task = "bored!";
				VirtualThread.sleep(1000);
				this.survive();
				continue;
			}

			Vec2 requestMoveTo = moveToRequests.peekFirst();
			if (requestMoveTo == null) {
				this.task = "idling...";
				VirtualThread.sleep(100);

				if (this.isThirsty()) {
					// desert from the army!
					Commander commander = this.commander;
					if (commander != null) {
						commander.removeSoldier(this);
					}
					{
						this.survive();
					}
					// join the ranks again
					if (commander != null && commander.isAlive()) {
						commander.addSoldier(this);
					}
				}

				Vec2 pos = new Vec2(this.position);
				pos.x = Misc.randomBase(pos.x, 0.25f);
				pos.y = Misc.randomBase(pos.y, 0.25f);
				this.moveTo(pos);
				continue;
			}

			this.task = "moving to requested location";
			this.moveTo(requestMoveTo);
			moveToRequests.removeFirst();
		}
	}

	@Override
	public void onRemove() {
		if (commander != null) {
			commander.removeSoldier(this);
		}

		super.onRemove();
	}

	public final CircularArrayList<Vec2> moveToRequests = new CircularArrayList<>();

	public void requestMoveTo(Vec2 pos) {
		moveToRequests.addLast(pos);
	}
}
