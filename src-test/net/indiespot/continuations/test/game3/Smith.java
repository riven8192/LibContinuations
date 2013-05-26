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

import net.indiespot.continuations.*;
import net.indiespot.continuations.test.*;
import net.indiespot.continuations.test.game.GameItem;
import net.indiespot.continuations.test.game.ResourceConversion;
import net.indiespot.continuations.test.game.ResourceHolder;
import de.matthiasmann.continuations.*;

public class Smith extends GameItem {

	public Smith() {
		this.name = "smith";
		this.color = Color.MAGENTA;

		this.consumingResources.register(IRON, new ResourceHolder(100, 100));
		this.consumingResources.register(LOG, new ResourceHolder(100));
		this.producingResources.register(TOOL, new ResourceHolder(25));

		final ResourceConversion makeTools = new ResourceConversion() {
			{
				this.consume(IRON, 1);
				this.consume(LOG, 1);
				this.produce(TOOL, 2);
			}
		};

		this.spawn(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				while (true) {
					VirtualThread.sleep(1250);

					if (makeTools.convert(Smith.this)) {
						System.out.println("smith made tools");
						VirtualThread.sleep(5000);
					}
				}
			}
		});
	}
}
