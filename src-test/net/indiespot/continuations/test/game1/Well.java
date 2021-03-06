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

import net.indiespot.continuations.VirtualRunnable;
import net.indiespot.continuations.VirtualThread;
import net.indiespot.continuations.test.game.GameItem;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game.ResourceHolder;
import de.matthiasmann.continuations.*;

public class Well extends GameItem {

	@SuppressWarnings("serial")
	public Well() {
		this.name = "well";
		this.color = Color.BLUE;

		this.producingResources.register(WATER, new ResourceHolder(100, 150));

		this.spawn(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				ai();
			}
		});
	}

	void ai() throws SuspendExecution {
		while (true) {
			VirtualThread.sleep(750);
			this.producingResources.get(WATER).put(Misc.random(1, 3));
		}
	}
}
