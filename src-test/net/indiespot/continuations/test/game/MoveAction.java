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

import craterstudio.math.EasyMath;
import craterstudio.math.Vec2;
import net.indiespot.continuations.VirtualThread;
import de.matthiasmann.continuations.*;

public class MoveAction implements Action {
	private final Vec2 pos, src, dst;
	private final long departAt, arriveAt;
	private final VirtualThread toResume;

	private MoveAction(Vec2 pos, long duration, Vec2 src, Vec2 dst) {
		if (duration < 0) {
			throw new IllegalStateException();
		}

		this.pos = pos;
		this.departAt = Game.TIME;
		this.arriveAt = Game.TIME + duration;
		this.src = src;
		this.dst = dst;

		this.toResume = VirtualThread.peekCurrentThread();
	}

	@Override
	public boolean perform() {
		float ratio = EasyMath.invLerpWithCap(Game.TIME, departAt, arriveAt);
		pos.x = EasyMath.lerp(src.x, dst.x, ratio);
		pos.y = EasyMath.lerp(src.y, dst.y, ratio);
		return Game.TIME <= arriveAt;
	}

	@Override
	public void end() {
		this.toResume.resume();
	}

	public static void moveTo(GameItem item, Vec2 dst) throws SuspendExecution {
		Vec2 src = new Vec2(item.position);

		float dx = src.x - dst.x;
		float dy = src.y - dst.y;
		float dist2 = dx * dx + dy * dy;
		if (dist2 == 0.0f) {
			return;
		}

		float distance = (float) Math.sqrt(dist2);
		float duration = distance / item.speed;
		long durationMillis = (long) (duration * 1000L);

		item.setAction(new MoveAction(item.position, durationMillis, src, dst));

		// VirtualThread.sleep(durationMillis);
		VirtualThread.suspend(); // resume at end of MoveAction
	}
}
