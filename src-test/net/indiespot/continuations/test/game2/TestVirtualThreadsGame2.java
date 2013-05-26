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

import java.awt.event.*;

import net.indiespot.continuations.test.*;
import net.indiespot.continuations.test.game.Game;
import net.indiespot.continuations.test.game.GameCanvas;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game1.*;

import craterstudio.math.Vec2;


public class TestVirtualThreadsGame2 {
	public static void main(String[] args) throws Exception {
		GameCanvas canvas = Game.init(new Runnable() {
			@Override
			public void run() {
				// add trees
				for (int i = 0; i < 1024 / 4; i++) {
					Tree tree = new Tree();
					tree.position.x = Misc.random(32, 1024 - 32);
					tree.position.y = Misc.random(32, 768 - 32);
					Game.addItem(tree);
				}

				// add storages
				for (int i = 0; i < 7; i++) {
					Storage storage = new Storage();
					storage.position.x = Misc.random(32, 1024 - 32);
					storage.position.y = Misc.random(32, 768 - 32);
					Game.addItem(storage);
				}

				// add water
				for (int i = 0; i < 15; i++) {
					Well water = new Well();
					water.position.x = Misc.random(32, 1024 - 32);
					water.position.y = Misc.random(32, 768 - 32);
					Game.addItem(water);
				}
			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Vec2 p = new Vec2(e.getX(), e.getY());

				// add lumberjack
				Lumberjack lumberjack = new Lumberjack();
				lumberjack.position.load(p);
				Game.addItem(lumberjack);
			}
		});
	}
}
