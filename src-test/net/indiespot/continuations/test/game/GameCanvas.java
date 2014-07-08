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
import java.awt.event.*;

import javax.swing.*;

import net.indiespot.continuations.test.game1.*;


@SuppressWarnings("serial")
public class GameCanvas extends JPanel {

	boolean renderLabelsForHumans = false;

	@Override
	public void addNotify() {
		super.addNotify();

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != 1) {
					renderLabelsForHumans ^= true;
				}
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (GameItem item : Game.ALL_ITEMS) {

			g.setColor(Color.BLUE);

			int x = (int) item.position.x;
			int y = (int) item.position.y;
			g.setColor(item.color);
			g.fillRect(x - 3, y - 3, 6, 6);

			if (!item.displayLabels) {
				continue;
			}

			if (item.travelTarget != null) {
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine((int) item.position.x, (int) item.position.y, (int) item.travelTarget.x, (int) item.travelTarget.y);
			}

			g.setColor(Color.BLACK);
			if (item.name != null && !item.name.isEmpty()) {
				g.drawString(item.toString(), x + 10, y);

				if (renderLabelsForHumans || !(item instanceof Human)) {

					for (ResourceType type : item.consumingResources.types()) {
						y += 14;
						g.drawString("consuming " + type.name() + ": " + item.consumingResources.get(type), x + 20, y);
					}

					for (ResourceType type : item.producingResources.types()) {
						y += 14;
						g.drawString("producing " + type.name() + ": " + item.producingResources.get(type), x + 20, y);
					}

					for (ResourceType type : item.holdingResources.types()) {
						y += 14;
						g.drawString("holding " + type.name() + ": " + item.holdingResources.get(type), x + 20, y);
					}
				}
			}
		}
	}
}
