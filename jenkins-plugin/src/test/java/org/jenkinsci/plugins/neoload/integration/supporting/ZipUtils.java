/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jenkinsci.plugins.neoload.integration.supporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** From http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/ */
public class ZipUtils {

	/** Unzip the file to the specified directory.
	 * @param zipFile
	 * @param outputFolder
	 * @return a list of the files that were unzipped
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<File> unzip(final String zipFile, final String outputFolder) throws FileNotFoundException, IOException {
		final List<File> unzippedFiles = new ArrayList<File>();
		ZipInputStream zis = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try { // read the zip file
			fis = new FileInputStream(zipFile);
			zis = new ZipInputStream(fis);
			new File(outputFolder).mkdirs(); // create output directory
			ZipEntry zipEntry = zis.getNextEntry();

			// read every file in the zip file
			while (zipEntry != null) {
				// ignore directories
				if (!zipEntry.isDirectory()) {
					final File newFile = new File(outputFolder + File.separator + zipEntry.getName());
					new File(newFile.getParent()).mkdirs(); // create directories if they don't exist.
					newFile.delete(); // overwrite any existing file.

					// write the file contents
					fos = null;
					try {
						fos = new FileOutputStream(newFile);
						final byte[] buffer = new byte[1024];
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					} finally {
						if (fos != null) {
							fos.close();
						}
					}
					unzippedFiles.add(newFile);
				}
				zipEntry = zis.getNextEntry(); // next entry in the zip file
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (zis != null) {
				zis.close();
			}
		}
		// done

		return unzippedFiles;
	}
}