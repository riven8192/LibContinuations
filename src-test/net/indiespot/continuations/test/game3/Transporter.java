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

package net.indiespot.continuations.test.game3;

import static net.indiespot.continuations.test.game.ResourceType.*;

import java.awt.*;
import java.util.List;

import craterstudio.math.Vec2;

import net.indiespot.continuations.*;
import net.indiespot.continuations.test.*;
import net.indiespot.continuations.test.game.Game;
import net.indiespot.continuations.test.game.GameItem;
import net.indiespot.continuations.test.game.Misc;
import net.indiespot.continuations.test.game.ResourceHolder;
import net.indiespot.continuations.test.game.ResourceType;
import net.indiespot.continuations.test.game1.*;
import de.matthiasmann.continuations.*;

public class Transporter extends Human {

	public Transporter() {
		this.name = "transporter";
		this.color = Color.RED;

		this.holdingResources.register(LOG, new ResourceHolder(10));
		this.holdingResources.register(TOOL, new ResourceHolder(10));
		this.holdingResources.register(PLANK, new ResourceHolder(20));
	}

	@Override
	public void ai() throws SuspendExecution {
		while (true) {
			VirtualThread.sleep(1000);

			Job job = null;
			for (ResourceType type : this.holdingResources.types()) {
				Job got = this.findSupplyDemand(type);
				if (got != null && (job == null || got.score > job.score)) {
					job = got;
				}
			}

			if (job == null) {
				// wander around for a bit
				Vec2 pos = new Vec2();
				pos.x = Misc.randomBase(this.position.x, 32);
				pos.y = Misc.randomBase(this.position.y, 32);
				this.task = "wandering";
				this.moveTo(pos);

				this.survive();
				continue;
			}

			System.out.println(this.name + " found supply/demand for: " + job.transfer + "x " + job.type + " (" + job.src + " -> " + job.dst + "), score: " + job.score);

			this.task = "picking up " + job.type + " supply from " + job.src;
			this.moveTo(job.src.position);
			if (job.src.transfer(job.type, this) == 0) {
				// took nothing
				if (this.getSource(job.type) == null) {
					// has nothing
					continue;
				}
			}
			this.survive();

			this.task = "bringing " + job.type + " to " + job.dst;
			this.moveTo(job.dst.position);
			this.transfer(job.type, job.dst);
			this.survive();
		}
	}

	private Job findSupplyDemand(ResourceType type) {

		final int minTransferAmount = 4;
		final float producerConsumerFactor = 0.9f;
		final float producerHolderFactor = 0.7f;
		final float holderConsumerFactor = 0.7f;
		final float holderHolderFactor = 0.01f;
		final float producerFullBonusFactor = 2.0f;
		final float consumerEmptyBonusFactor = 3.0f;

		GameItem bestProducer = null;
		GameItem bestConsumer = null;
		float bestScore = Integer.MIN_VALUE;
		int bestTransfer = -1;

		// move products directly between producers and consumers
		List<GameItem> producers = Game.findProducers(type, false, null);
		List<GameItem> consumers = Game.findConsumers(type, false, null);
		for (GameItem src : producers) {
			for (GameItem dst : consumers) {
				if (src == dst) {
					continue;
				}

				float distance = Vec2.distance(this.position, src.position);
				distance += Vec2.distance(src.position, dst.position);

				int take = src.producingResources.get(type).getFilledSpace();
				int free = dst.consumingResources.get(type).getFreeSpace();
				int carry = this.holdingResources.get(type).getFreeSpace();

				int transfer = Math.min(take, free);
				if (Math.min(carry, transfer) < minTransferAmount) {
					continue;
				}

				float score = score(carry, transfer, distance, producerConsumerFactor);
				if (src.producingResources.get(type).isFull()) {
					score *= producerFullBonusFactor;
				}
				if (dst.consumingResources.get(type).isEmpty()) {
					score *= consumerEmptyBonusFactor;
				}

				if (score > bestScore) {
					bestProducer = src;
					bestConsumer = dst;
					bestScore = score;
					bestTransfer = transfer;
				}
			}
		}

		// move products between producers and holders
		List<GameItem> consumableHolders = Game.findHoldersWithSpace(type, null);
		for (GameItem src : producers) {
			for (GameItem dst : consumableHolders) {
				if (src == dst) {
					continue;
				}

				float distance = Vec2.distance(this.position, src.position);
				distance += Vec2.distance(src.position, dst.position);

				int take = src.producingResources.get(type).getFilledSpace();
				int free = dst.holdingResources.get(type).getFreeSpace();
				int carry = this.holdingResources.get(type).getFreeSpace();

				int transfer = Math.min(take, free);
				if (Math.min(carry, transfer) < minTransferAmount) {
					continue;
				}

				float score = score(carry, transfer, distance, producerHolderFactor);
				if (src.producingResources.get(type).isFull()) {
					score *= producerFullBonusFactor;
				}

				if (score > bestScore) {
					bestProducer = src;
					bestConsumer = dst;
					bestScore = score;
					bestTransfer = transfer;
				}
			}
		}

		// move products between holders and consumers
		List<GameItem> produceableHolder = Game.findHoldersWithProducts(type, null);
		for (GameItem src : produceableHolder) {
			for (GameItem dst : consumers) {
				if (src == dst) {
					continue;
				}

				float distance = Vec2.distance(this.position, src.position);
				distance += Vec2.distance(src.position, dst.position);

				int take = src.holdingResources.get(type).getFilledSpace();
				int free = dst.consumingResources.get(type).getFreeSpace();
				int carry = this.holdingResources.get(type).getFreeSpace();

				int transfer = Math.min(take, free);
				if (Math.min(carry, transfer) < minTransferAmount) {
					continue;
				}

				float score = score(carry, transfer, distance, holderConsumerFactor);
				if (dst.consumingResources.get(type).isEmpty()) {
					score *= consumerEmptyBonusFactor;
				}

				if (score > bestScore) {
					bestProducer = src;
					bestConsumer = dst;
					bestScore = score;
					bestTransfer = transfer;
				}
			}
		}

		// move products between full-holders and empty-holders
		for (GameItem src : produceableHolder) {
			for (GameItem dst : consumableHolders) {
				if (src == dst) {
					continue;
				}

				// move if full
				if (!src.holdingResources.get(type).isFull()) {
					// only move stuff if there is a big discrepancy (src nearly full, dst nearly empty)
					if (src.holdingResources.get(type).getRatio() < 0.75f //
							|| dst.holdingResources.get(type).getRatio() > 0.25f) {
						continue;
					}
				}

				float distance = Vec2.distance(this.position, src.position);
				distance += Vec2.distance(src.position, dst.position);

				int take = src.holdingResources.get(type).getFilledSpace();
				int free = dst.holdingResources.get(type).getFreeSpace();
				int carry = this.holdingResources.get(type).getFreeSpace();

				int transfer = Math.min(take, free);
				if (Math.min(carry, transfer) < minTransferAmount) {
					continue;
				}

				// the more the better
				float score = score(carry, transfer, distance, holderHolderFactor);
				if (score > bestScore) {
					bestProducer = src;
					bestConsumer = dst;
					bestScore = score;
					bestTransfer = transfer;
				}
			}
		}

		if (bestScore == Integer.MIN_VALUE) {
			return null;
		}

		return new Job(type, bestProducer, bestConsumer, bestScore, bestTransfer);
	}

	static float score(int carry, int transfer, float distance, float bonusFactor) {
		// don't weigh desired transfer too heavily
		transfer = Math.min(carry * 3, transfer);

		float f1 = (float) Math.pow(transfer, 0.75f); // transfer isn't everything
		float f2 = (float) Math.pow(distance, 0.75f); // distance isn't everything
		return (f1 / f2 * bonusFactor);
	}

	static class Job {
		public final ResourceType type;
		public final GameItem src, dst;
		public final float score;
		public final int transfer;

		public Job(ResourceType type, GameItem src, GameItem dst, float score, int transfer) {
			this.type = type;
			this.src = src;
			this.dst = dst;
			this.score = score;
			this.transfer = transfer;
		}
	}
}
