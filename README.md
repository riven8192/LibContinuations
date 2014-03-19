## LibContinuations

LibContinuations is a small library that enables the use of [Green Threads](http://en.wikipedia.org/wiki/Green_threads) in Java.

Conceptually, a green threead is a virtual thread that runs on real (native) threads. A green thread can `sleep` or `yield` and its state is preserved while other threads execute, enabling you to write normal Java code that doesn't suffer from multithreading overhead and related complexity (locks, race conditions, etc). LibContinuations is as close as possible to regular threads, runnables and locks so integrates easily with existing code. There is very little overhead per green thread, allowing many thousands of green threads to be run on a single thread.

## Use case

For example, AI code can be written with your usual Java control-flow:

```java
public void run () throws SuspendExecution {
	while (true) { // this is the AI of the unit, it will go on forever
		Vec2 originalLocation = new Vec2(this.position);
		Water water = this.findWater();
		this.moveTo(water.position);
		this.drink(water);
		this.moveTo(originalLocation); // walk back
	}
}

public void drink (Water water) throws SuspendExecution {
	while (!this.isFull() && water.level > 0) {
		water.level--;
		this.water++;
		sleep(1500); // drinking takes a while
	}
}

public void moveTo (Vec2 end) throws SuspendExecution {
	Vec2 start = new Vec2(this.position);
	float distance = Vec2.distance(start);
	float duration = distance / this.speed;
	for (float traveled = 0; traveled <= duration; traveled += this.speed) {
		float ratio = Math.min(traveled / distance, 1);
		this.position.x = start.x + ratio * (end.x - start.x);
		this.position.y = start.y + ratio * (end.y - start.y);
		yield(); // wake up here in the next game tick
	}
}

```

## Implementation

LibContinuations is written on top of Matthias Mann's [Continuations Library](http://www.matthiasmann.de/content/view/24/26/) which provides the concept of `yield return`, as found in C# and other languages. This works by rewriting bytecode to store and restore the Java stack.

## Adjusting your bytecode

There are two ways to apply the bytecode rewriting:

* At runtime: A Java agent intercepts classloading and adjusts each class as it is loaded. This is convenient while developing.
* Ahead of time: Java code or an Ant task discovers your class files and rewrites them. This is convenient to do before deploying your application.

With either option you still have full debugging functionality in your IDE: step into/over/out and breakpoints work as always.

## Using LibContinuations

`VirtualProcessor` manages all green threads for a single native thread. `VirtualThread` takes a `VirtualRunnable` and behaves just like `java.lang.Thread` and `java.lang.Runnable`. Methods that can be suspended via `VirtualThread.sleep()` or `VirtualThread.yield()` must throw `SuspendExecution`.


```java
import net.indiespot.continuations.*;
import de.matthiasmann.continuations.SuspendExecution;

public class Example {
	public Example () {
		VirtualProcessor processor = new VirtualProcessor();

		new VirtualThread(new VirtualRunnable() {
			public void run () throws SuspendExecution {
				while (true) {
					System.out.println("first task");
					VirtualThread.sleep(1000);
				}
			}
		}).start(processor);

		new VirtualThread(new VirtualRunnable() {
			public void run () throws SuspendExecution {
				while (true) {
					System.out.println("second task");
					VirtualThread.sleep(800);
				}
			}
		}).start(); // the last VirtualProcessor for this thread is used if omitted

		while (true) {
			long now = System.nanoTime() / 1000000l;
			processor.tick(now);
		}
	}

	public static void main (String[] args) throws Exception {
		new Example();
	}
}

```

Before this code can be run, it must be processed ahead of time by the Ant task or Java code, or the Java agent must be used. To use the Java agent, these JVM parameters must be used:


```
-javaagent:continuations-agent.jar -ea
```

To use Ant:

```xml
<taskdef name="continuations"
	classname="de.matthiasmann.continuations.instrument.InstrumentationTask"
	classpath="asm-debug-all-4.2.jar:continuations.jar" />

<target name="post-compile">
	<continuations verbose="true">
		<fileset dir="classes" />
	</continuations>
</target>
```

To use Java:

```java
import de.matthiasmann.continuations.instrument.DBClassWriter;
import de.matthiasmann.continuations.instrument.InstrumentClass;
import de.matthiasmann.continuations.instrument.MethodDatabase;

...

// Collect each class file that may need to be processed.
MethodDatabase db = new MethodDatabase(MethodDatabase.class.getClassLoader());
for (String classFile : Scar.paths("classes", "**.class")) // uses Scar to collect paths
	db.checkClass(new File(classFile));

// Rewrite the class files that need processing.
for (File file : db.getWorkList())
	instrumentClass(db, file);

...


static private void instrumentClass (MethodDatabase db, File file) throws IOException {
	FileInputStream input = new FileInputStream(file);
	ClassReader reader = new ClassReader(input);
	input.close();

	ClassWriter writer = new DBClassWriter(db, reader);
	reader.accept(new InstrumentClass(writer, db, false), ClassReader.SKIP_FRAMES);
	byte[] newClass = writer.toByteArray();

	FileOutputStream output = new FileOutputStream(file);
	output.write(newClass);
	output.close();
}

```
