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

package net.indiespot.continuations.test.game1;

import static net.indiespot.continuations.test.game.ResourceType.*;

import java.awt.*;

import craterstudio.func.Filter;
import craterstudio.math.Vec2;

import net.indiespot.continuations.VirtualRunnable;
import net.indiespot.continuations.VirtualThread;
import net.indiespot.continuations.test.*;
import net.indiespot.continuations.test.game.Game;
import net.indiespot.continuations.test.game.GameItem;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game.ResourceHolder;

import de.matthiasmann.continuations.*;

public abstract class Human extends GameItem {

	public Human() {
		this.name = "human";
		this.color = Color.DARK_GRAY;

		this.speed = 25f + (float) Math.random() * 25f;

		this.consumingResources.register(WATER, new ResourceHolder(Misc.random(25, 50), Misc.random(50, 100)));

		this.spawn(new VirtualRunnable() {@Override
			public void run() throws SuspendExecution {
				Human.this.ai();
			}
		});

		this.spawn(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				while (true) {
					VirtualThread.sleep(1250);

					if (consumingResources.get(WATER).take(1) == 0) {
						System.out.println(Human.this.toString() + " died of thirst");
						kill();
						throw new IllegalStateException("never happens");
					}
				}
			}
		});
	}

	public abstract void ai() throws SuspendExecution;

	protected void survive() throws SuspendExecution {
		Vec2 goBackTo = new Vec2(this.position);

		boolean didSomethingElse = false;
		while (this.isThirsty()) {
			this.task = "looking for water";
			GameItem water = this.findNearestWater();
			if (water == null) {
				break;
			}

			didSomethingElse = true;

			this.task = "walking to water";
			moveTo(water.position);

			this.task = "drinking water";
			this.drinkWater(water);
		}

		if (didSomethingElse) {
			this.task = "going back...";
		}
		moveTo(goBackTo);
	}

	public boolean isThirsty() {
		return this.consumingResources.get(WATER).getFilledSpace() < 20;
	}

	private int drinkWater(GameItem water) throws SuspendExecution {
		return water.transfer(WATER, this, 50);
	}

	private GameItem findNearestWater() throws SuspendExecution {
		//return Game.nearest(Well.class, new WellFilledWellFilter(), this.position);
		return Game.nearestProducer(WATER, false, new WellFilledWellFilter(), this.position);
	}

	//

	private static class WellFilledWellFilter implements Filter<GameItem> {
		@Override
		public boolean accept(GameItem item) {
			return item.producingResources.get(WATER).getFilledSpace() > 20;
		}
	}
}
