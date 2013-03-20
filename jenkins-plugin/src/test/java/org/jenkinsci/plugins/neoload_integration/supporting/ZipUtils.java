package org.jenkinsci.plugins.neoload_integration.supporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** From http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/ */
public class ZipUtils {
	
	/** Unzip the file to the specified directory.
	 * @param zipFile
	 * @param outputFolder
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void unzip(String zipFile, String outputFolder) throws FileNotFoundException, IOException {
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) { // read the zip file
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
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
				}
				zipEntry = zis.getNextEntry(); // next entry in the zip file
			}
		}
		// done
	}
}