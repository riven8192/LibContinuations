package net.indiespot.continuations.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.indiespot.continuations.VirtualProcessor;
import net.indiespot.continuations.VirtualRunnable;
import net.indiespot.continuations.VirtualThread;
import de.matthiasmann.continuations.SuspendExecution;

public class VirtualThreadNIO {
	@SuppressWarnings("serial")
	public static void main(String[] args) {

		final VirtualProcessor processor = new VirtualProcessor();

		// create virtual threads (or green threads, if you will)

		new VirtualThread(new VirtualRunnable() {
			@Override
			public void run() throws SuspendExecution {
				try {
					ServerSocketChannel ssc = ServerSocketChannel.open();
					ssc.configureBlocking(false);
					ssc.bind(new InetSocketAddress("127.0.0.1", 8282), 50);
					System.out.println("listening: " + ssc);

					handleServerSocket(ssc);
				}
				catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		}).start();

		long lastSecond = now();
		int runsLastSecond = 0;
		do {
			runsLastSecond += processor.tick(now());

			if(now() > lastSecond + 1000) {
				System.out.println("VirtualProcessor: " + runsLastSecond + " runs / sec");

				runsLastSecond = 0;
				lastSecond += 1000L;
			}

			try {
				Thread.sleep(1);
			}
			catch (InterruptedException exc) {
				// ignore
			}
		}
		while (processor.hasPendingTasks());
	}

	@SuppressWarnings("serial")
	static void handleServerSocket(ServerSocketChannel serverSocket) throws SuspendExecution, IOException {

		while (true) {
			SocketChannel sc = accept(serverSocket);
			System.out.println("accepted: " + sc);

			sc.configureBlocking(false);

			final SocketChannel socket = sc;
			new VirtualThread(new VirtualRunnable() {
				@Override
				public void run() throws SuspendExecution {
					handleSocket(socket);
				}
			}).start();
		}
	}

	static void handleSocket(SocketChannel socket) throws SuspendExecution {
		ByteBuffer bb = ByteBuffer.allocateDirect(64 * 1024);

		SocketAddress id = null;
		byte[] httpHeaderEnd = Text.ascii("\r\n\r\n");

		try {
			id = socket.getRemoteAddress();

			while (true) {
				int got = read(socket, bb, 5000);
				if(got == -1) {
					break;
				}

				// ends with "\r\n\r\n"
				if(bb.position() < httpHeaderEnd.length) {
					continue;
				}
				for(int i = 0; i < httpHeaderEnd.length; i++) {
					if(bb.get(bb.position() - httpHeaderEnd.length + i) != '\r') {
						continue;
					}
				}

				// bb.flip();
				// byte[] data = new byte[bb.remaining()];
				// bb.get(data);
				// System.out.print(Text.ascii(data));

				bb.clear();
				bb.put(Text.ascii("HTTP/1.1 200 OK\r\n"));
				bb.put(Text.ascii("Content-Type: text.plain\r\n"));
				bb.put(Text.ascii("Transfer-Encoding: chunked\r\n"));
				bb.put(Text.ascii("\r\n"));
				bb.put(Text.ascii("4\r\n"));
				bb.put(Text.ascii("w00t\r\n"));
				bb.put(Text.ascii("0\r\n"));
				bb.put(Text.ascii("\r\n"));
				bb.flip();
				write(socket, bb);
				bb.clear();
			}
		}
		catch (IOException exc) {
			System.err.println("i/o error: " + exc.getClass().getName() + ": " + exc.getMessage());
		}
		finally {
			System.out.println("diconnected: " + id);

			try {
				socket.close(); // TODO: this is blocking
			}
			catch (IOException exc) {
				// ignore
			}
		}
	}

	static SocketChannel accept(ServerSocketChannel serverSocket) throws SuspendExecution, IOException {
		for(int sleep = 0; true; sleep = incSleep(sleep)) {
			SocketChannel sc = serverSocket.accept();
			if(sc != null) {
				return sc;
			}

			VirtualThread.sleep(sleep);
		}
	}

	static int read(SocketChannel socket, ByteBuffer bb, int timeout) throws SuspendExecution, IOException {
		if(!bb.hasRemaining()) {
			throw new IllegalStateException();
		}

		int duration = 0;
		for(int sleep = 0; true; sleep = incSleep(sleep)) {
			int got = socket.read(bb);
			if(got != 0) {
				return got;
			}

			if(duration + sleep > timeout) {
				sleep = timeout - duration;
			}

			VirtualThread.sleep(sleep);
			duration += sleep;

			if(duration >= timeout) {
				throw new SocketTimeoutException();
			}
		}
	}

	static void write(SocketChannel socket, ByteBuffer bb) throws SuspendExecution, IOException {
		if(!bb.hasRemaining()) {
			throw new IllegalStateException();
		}

		for(int sleep = 0; bb.hasRemaining(); sleep = incSleep(sleep)) {
			int got = socket.write(bb);
			if(got == 0) {
				VirtualThread.sleep(sleep);
			}
			else {
				sleep = 0;
			}
		}
	}

	private static int incSleep(int sleep) {
		return Math.min(25 + (int) (sleep * 1.25f), 1000);
	}

	private static class Text {
		public static byte[] ascii(String s) {
			char[] c = s.toCharArray();
			byte[] b = new byte[c.length];
			for(int i = 0; i < c.length; i++)
				b[i] = (byte) c[i];
			return b;
		}
	}

	static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
