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

package net.indiespot.continuations.util;

import de.matthiasmann.continuations.instrument.DBClassWriter;
import de.matthiasmann.continuations.instrument.InstrumentClass;
import de.matthiasmann.continuations.instrument.Log;
import de.matthiasmann.continuations.instrument.LogLevel;
import de.matthiasmann.continuations.instrument.MethodDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import craterstudio.func.Callback;
import craterstudio.io.FileUtil;

public class AheadOfTime {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage:");
			System.out.println("\toutputDir, inputDirs...");
			System.exit(1);
		}

		final MethodDatabase db = new MethodDatabase(Thread.currentThread().getContextClassLoader());
		db.setVerbose(false);
		db.setDebug(false);
		db.setAllowMonitors(false);
		db.setAllowBlocking(false);
		db.setLog(new Log() {
			public void log(LogLevel level, String msg, Object... args) {
				System.out.println(level + ": " + String.format(msg, args));
			}

			public void error(String msg, Exception ex) {
				System.out.println("ERROR: " + msg);
				ex.printStackTrace(System.out);
			}
		});

		File outputDir = new File(args[0]);

		List<File> inputDirs = new ArrayList<>();
		for (int i = 1; i < args.length; i++) {
			inputDirs.add(new File(args[i]));
		}

		build(inputDirs, outputDir, db);
	}

	public static void build(List<File> inputDirs, final File outputDir, final MethodDatabase db) {

		for (final File dir : inputDirs) {
			if (!dir.exists() || !dir.isDirectory()) {
				throw new IllegalStateException("directory not found: " + dir.getAbsolutePath());
			}

			System.out.println("input dir: " + dir.getAbsolutePath());

			visitDirectory(dir, new Callback<File>() {
				@Override
				public void callback(File src) {
					System.out.println("checking class file: " + src);

					if (src.getName().endsWith(".class")) {
						db.checkClass(src);
					}
				}
			});
		}

		for (final File dir : inputDirs) {
			visitDirectory(dir, new Callback<File>() {
				@Override
				public void callback(File src) {
					final String relpath = FileUtil.getRelativePath(src, dir);
					final File dst = new File(outputDir, relpath);

					dst.getParentFile().mkdirs();

					if (src.getName().endsWith(".class")) {
						instrumentClass(db, src, dst);
					} else if (!src.isDirectory()) {
						System.out.println("copying file: " + src);
						try {
							FileUtil.copyFile(src, dst);
						} catch (IOException exc) {
							exc.printStackTrace();
						}
					}
				}
			});
		}
	}

	private static void visitDirectory(File dir, Callback<File> callback) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				visitDirectory(file, callback);
			}
			callback.callback(file);
		}
	}

	private static void instrumentClass(MethodDatabase db, File in, File out) {
		db.log(LogLevel.INFO, "Instrumenting class %s", in);

		try {
			ClassReader r;

			FileInputStream fis = new FileInputStream(in);
			try {
				r = new ClassReader(fis);
			} finally {
				fis.close();
			}

			ClassWriter cw = new DBClassWriter(db, r);
			ClassVisitor cv = new CheckClassAdapter(cw);
			InstrumentClass ic = new InstrumentClass(cv, db, false);
			r.accept(ic, ClassReader.SKIP_FRAMES);
			byte[] newClass = cw.toByteArray();

			FileOutputStream fos = new FileOutputStream(out);
			try {
				fos.write(newClass);
			} finally {
				fos.close();
			}
		} catch (IOException ex) {
			throw new BuildException("Instrumenting file " + in, ex);
		}
	}
}
