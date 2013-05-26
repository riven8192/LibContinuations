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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import craterstudio.data.CircularArrayList;
import craterstudio.math.FastMath;
import craterstudio.math.Vec2;

import net.indiespot.continuations.*;
import net.indiespot.continuations.test.game1.*;
import de.matthiasmann.continuations.*;

public class Commander extends Human {
	public Commander() {
		this.name = "commander";
		this.color = Color.RED;
	}

	public void addSoldier(Soldier soldier) {
		if (soldier.commander != null) {
			throw new IllegalStateException();
		}
		if (this.soldiers.contains(soldier)) {
			throw new IllegalStateException();
		}

		soldier.commander = this;
		this.soldiers.add(soldier);
	}

	public void removeSoldier(Soldier soldier) {
		if (soldier.commander != this) {
			throw new IllegalStateException();
		}
		if (!this.soldiers.contains(soldier)) {
			throw new IllegalStateException();
		}

		soldier.commander = null;
		this.soldiers.remove(soldier);
	}

	public final List<Soldier> soldiers = new CopyOnWriteArrayList<>();
	public final CircularArrayList<Vec2> goToQueue = new CircularArrayList<>();
	public final CircularArrayList<Boolean> goToForwards = new CircularArrayList<>();

	@Override
	public void ai() throws SuspendExecution {
		final float spacing = 32.0f;

		while (true) {
			Vec2 goTo = goToQueue.pollFirst();
			if (goTo == null) {
				this.task = "idling";
				VirtualThread.sleep(1000);
				this.survive();
				continue;
			}
			boolean isForward = goToForwards.removeFirst().booleanValue();

			this.task = "calling...";
			this.requestLayout(this.position, spacing, goTo, isForward);
			VirtualThread.sleep(1000);

			this.task = "waiting...";
			this.waitForSoldiersArrived();

			this.task = "line up platoon...";
			this.requestLayout(goTo, spacing, new Vec2(goTo).sub(this.position).add(goTo), isForward);

			this.task = "marching!";
			this.moveTo(goTo);
		}
	}

	@Override
	public void onRemove() {
		for (Soldier soldier : soldiers) {
			this.removeSoldier(soldier);
		}

		super.onRemove();
	}

	private void requestLayout(Vec2 origin, float spacing, Vec2 target, boolean isForward) {
		List<Vec2> positions = this.calcLayout(origin, spacing, target, isForward);
		if (positions == null) {
			return;
		}

		for (int i = 0; i < soldiers.size(); i++) {
			soldiers.get(i).requestMoveTo(positions.get(i));
		}
	}

	private List<Vec2> calcLayout(Vec2 origin, float spacing, Vec2 target, boolean isForward) {
		int count = this.soldiers.size();
		if (count == 0) {
			return null;
		}

		int w = (int) Math.ceil(Math.sqrt(count) * 0.50f);
		int h = (int) Math.ceil(count / (float) w);

		float angle = FastMath.atan2Deg(origin.y - target.y, origin.x - target.x);
		if (!isForward) {
			angle += 180.0f;
		}

		float sin = FastMath.sinDeg(angle);
		float cos = FastMath.cosDeg(angle);

		List<Vec2> layout = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int x = i % w;
			int y = i / w;

			float px = (x - w * 0.5f + 0.5f) * spacing;
			float py = (y - h * 0.5f + 0.5f) * spacing;
			px -= 48;
			float px2 = cos * px - sin * py;
			float py2 = sin * px + cos * py;

			layout.add(new Vec2(px2, py2).add(origin));
		}

		return layout;
	}

	private void waitForSoldiersArrived() throws SuspendExecution {
		System.out.println(this + " waiting for soldiers");
		for (Soldier soldier : soldiers) {
			while (soldier.isAlive() && !soldier.moveToRequests.isEmpty()) {
				VirtualThread.sleep(250);
				this.survive();
			}
		}
	}
}
