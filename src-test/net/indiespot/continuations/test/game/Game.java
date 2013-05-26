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
import java.util.*;
import java.util.List;

import javax.swing.*;


import craterstudio.func.Filter;
import craterstudio.math.Vec2;

import net.indiespot.continuations.VirtualProcessor;
import net.indiespot.continuations.test.game1.Human;



public class Game {
	public static long TIME = now();
	public static final float TICK_DURATION = 0.01f; // 100Hz

	public static final List<GameItem> CURR_TICK_ITEMS = new ArrayList<GameItem>();
	public static final List<GameItem> NEXT_TICK_ITEMS = new ArrayList<GameItem>();
	public static final List<GameItem> ALL_ITEMS = new ArrayList<GameItem>();

	public static VirtualProcessor PROCESSOR;

	public static long now() {
		return System.nanoTime() / 1_000_000L;
	}

	public static void addItem(GameItem item) {
		ALL_ITEMS.add(item);
		item.onAdd();
	}

	public static void removeItem(GameItem item) {
		ALL_ITEMS.remove(item);
		item.onRemove();
	}

	public static GameCanvas init(final Runnable init) throws Exception {

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				PROCESSOR = new VirtualProcessor();

				init.run();
			}
		});

		final GameCanvas canvas = new GameCanvas();

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {

				canvas.setPreferredSize(new Dimension(1024, 768));

				JFrame holder = new JFrame();
				holder.getContentPane().add(canvas);
				holder.setResizable(false);
				holder.pack();
				holder.setLocationRelativeTo(null);
				holder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				holder.setVisible(true);
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								tick();
							}
						});

						canvas.repaint();

						Thread.sleep(10);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}).start();

		return canvas;
	}

	@SuppressWarnings("unchecked")
	public static <T extends GameItem> List<T> findByType(Class<T> type, Filter<T> filter) {
		List<T> found = new ArrayList<>();
		for (GameItem item : ALL_ITEMS) {
			if (type.isAssignableFrom(item.getClass())) {
				T t = (T) item;
				if (filter == null || filter.accept(t)) {
					found.add(t);
				}
			}
		}
		return found;
	}

	public static List<GameItem> findProducers(ResourceType type, boolean alsoEmpty, Filter<GameItem> filter) {
		List<GameItem> found = new ArrayList<>();
		for (GameItem item : ALL_ITEMS) {
			if (item instanceof Human) {
				continue;
			}
			if (!item.producingResources.types().contains(type)) {
				continue;
			}
			if (!alsoEmpty && item.producingResources.get(type).isEmpty()) {
				continue;
			}

			if (filter == null || filter.accept(item)) {
				found.add(item);
			}
		}
		return found;
	}

	public static List<GameItem> findConsumers(ResourceType type, boolean alsoFull, Filter<GameItem> filter) {
		List<GameItem> found = new ArrayList<>();
		for (GameItem item : ALL_ITEMS) {
			if (item instanceof Human) {
				continue;
			}
			if (!item.consumingResources.types().contains(type)) {
				continue;
			}
			if (!alsoFull && item.consumingResources.get(type).isFull()) {
				continue;
			}

			if (filter == null || filter.accept(item)) {
				found.add(item);
			}
		}
		return found;
	}

	public static List<GameItem> findHoldersWithSpace(ResourceType type, Filter<GameItem> filter) {
		return findHolders(type, true, false, filter);
	}

	public static List<GameItem> findHoldersWithProducts(ResourceType type, Filter<GameItem> filter) {
		return findHolders(type, false, true, filter);
	}

	public static List<GameItem> findHolders(ResourceType type, boolean alsoEmpty, boolean alsoFull, Filter<GameItem> filter) {
		List<GameItem> found = new ArrayList<>();
		for (GameItem item : ALL_ITEMS) {
			if (item instanceof Human) {
				continue;
			}
			if (!item.holdingResources.types().contains(type)) {
				continue;
			}
			if (!alsoEmpty && item.holdingResources.get(type).isEmpty()) {
				continue;
			}
			if (!alsoFull && item.holdingResources.get(type).isFull()) {
				continue;
			}

			if (filter == null || filter.accept(item)) {
				found.add(item);
			}
		}
		return found;
	}

	public static <T extends GameItem> T nearest(List<T> items, Vec2 location) {
		Vec2 tmp = new Vec2();

		T nearest = null;
		for (T item : items) {
			if (nearest == null) {
				nearest = item;
			} else {
				float len1 = tmp.load(nearest.position).sub(location).squaredLength();
				float len2 = tmp.load(item.position).sub(location).squaredLength();
				if (len2 < len1) {
					nearest = item;
				}
			}
		}
		return nearest;
	}

	public static <T extends GameItem> T nearest(Class<T> type, Filter<T> filter, Vec2 location) {
		return nearest(findByType(type, filter), location);
	}

	public static GameItem nearestProducer(ResourceType type, boolean alsoEmpty, Filter<GameItem> filter, Vec2 location) {
		return nearest(findProducers(type, alsoEmpty, filter), location);
	}

	public static GameItem nearestConsumer(ResourceType type, boolean alsoFull, Filter<GameItem> filter, Vec2 location) {
		return nearest(findConsumers(type, alsoFull, filter), location);
	}

	public static GameItem nearestHolderWithSpace(ResourceType type, Filter<GameItem> filter, Vec2 location) {
		return nearest(findHoldersWithSpace(type, filter), location);
	}

	public static GameItem nearestHolderWithProducts(ResourceType type, Filter<GameItem> filter, Vec2 location) {
		return nearest(findHoldersWithProducts(type, filter), location);
	}

	public static void tick() {
		Game.TIME = now();
		int ticked = Game.PROCESSOR.tick(Game.TIME);
		if (ticked > 0) {
			//	System.out.println("ticked: " + ticked);
		}

		for (GameItem item : CURR_TICK_ITEMS) {
			item.tick();
		}
		CURR_TICK_ITEMS.clear();
		CURR_TICK_ITEMS.addAll(NEXT_TICK_ITEMS);
		NEXT_TICK_ITEMS.clear();
	}
}
