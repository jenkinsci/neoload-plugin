package org.jenkinsci.plugins.neoload_integration.supporting;

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
	public static List<File> unzip(String zipFile, String outputFolder) throws FileNotFoundException, IOException {
		List<File> unzippedFiles = new ArrayList<File>();
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
						byte[] buffer = new byte[1024];
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