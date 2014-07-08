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

import java.awt.*;

import net.indiespot.continuations.*;
import net.indiespot.continuations.test.game.Game;
import net.indiespot.continuations.test.game.GameItem;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game.ResourceHolder;
import de.matthiasmann.continuations.*;

public class Tree extends GameItem {

	@SuppressWarnings("serial")
	public Tree() {
		this.name = null;
		this.color = Color.GREEN;

		this.producingResources.register(TREE, new ResourceHolder(Misc.random(10, 40), 50));

		this.spawn(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				ai();
			}
		});
	}

	void ai() throws SuspendExecution {
		while (true) {
			VirtualThread.sleep(Misc.random(5 * 1100, 5 * 2100));

			if (Math.random() < 0.01) {
				if (this.producingResources.get(TREE).getRatio() > 0.50f) {
					Tree tree = new Tree();
					tree.position.x = Misc.randomBase(position.x, 32);
					tree.position.y = Misc.randomBase(position.y, 32);
					// System.out.println("new tree");
					Game.addItem(tree);
				}
			}

			if (Math.random() < 0.1) {
				if (this.producingResources.get(TREE).put(1) == 1) {
					// System.out.println("grew tree");
					color = new Color(0, 255 - (this.producingResources.get(TREE).getFilledSpace() * 2), 0);
				}
			}

			if (Math.random() < 0.1) {
				if (this.producingResources.get(TREE).getRatio() > 0.80f) {
					// System.out.println("killed tree");
					kill();
				}
			}
		}
	}
}
