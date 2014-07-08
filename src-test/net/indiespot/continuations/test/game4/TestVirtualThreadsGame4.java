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

import java.awt.event.*;

import net.indiespot.continuations.test.game.Game;
import net.indiespot.continuations.test.game.GameCanvas;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game.ResourceType;
import net.indiespot.continuations.test.game1.*;
import net.indiespot.dependencies.Vec2;

public class TestVirtualThreadsGame4 {

	static Commander commander;

	public static void main(String[] args) throws Exception {
		GameCanvas canvas = Game.init(new Runnable() {
			@Override
			public void run() {
				// add water
				for (int i = 0; i < 7; i++) {
					Well water = new Well();
					water.position.x = Misc.random(32, 1024 - 32);
					water.position.y = Misc.random(32, 768 - 32);
					Game.addItem(water);
				}

				// add commander
				{
					commander = new Commander();
					commander.speed = Misc.random(30, 35); // fat bastard
					commander.position.x = 1024 / 2;
					commander.position.y = 768 / 2;
					commander.consumingResources.get(ResourceType.WATER).grow(50);
					commander.consumingResources.get(ResourceType.WATER).fill();
					Game.addItem(commander);
				}

				// add soldiers
				for (int i = 0; i < 23; i++) {
					Soldier soldier = new Soldier();
					soldier.speed = Misc.random(50, 60); // slim bastards
					// soldier.consumingResources.get(ResourceType.WATER).grow(50);
					soldier.consumingResources.get(ResourceType.WATER).fill();
					soldier.position.x = Misc.randomBase(1024 / 2, 256);
					soldier.position.y = Misc.randomBase(768 / 2, 256);
					Game.addItem(soldier);

					commander.addSoldier(soldier);
				}
			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Vec2 p = new Vec2(e.getX(), e.getY());

				commander.goToQueue.addLast(p);
				commander.goToForwards.addLast(Boolean.valueOf(e.getButton() == 1));
			}
		});
	}
}
