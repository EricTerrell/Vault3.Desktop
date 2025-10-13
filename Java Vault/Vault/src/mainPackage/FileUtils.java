/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package mainPackage;

import java.io.*;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class FileUtils {
	/**
	 * Returns a UTF-8 byte order mark (BOM)
	 * @return Byte order mark
	 */
	public static byte[] getUTF8BOM() {
		return new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
	}
	
	/**
	 * Returns a UTF-16 Little Endian byte order mark (BOM)
	 * @return Byte order mark
	 */
	private static byte[] getUTF16LittleEndianBOM() {
		return new byte[] { (byte) 0xff, (byte) 0xfe };
	}
	
	/**
	 * Returns a UTF-16 Big Endian byte order mark (BOM)
	 * @return Byte order mark
	 */
	private static byte[] getUTF16BigEndianBOM() {
		return new byte[] { (byte) 0xfe, (byte) 0xff };
	}
	
	/***
	 * Determines the encoding of the specified file. If a UTF16 Byte Order Mark (BOM) is found an encoding of "UTF16" is returned.
	 * If a UTF8 BOM is found an encoding of "UTF8" is returned. Otherwise the default encoding is returned.
	 * @param filePath file path
	 * @return "UTF8", "UTF16", or default encoding.
	 */
	/**
	 * @param filePath path of file
	 * @return encoding of specified file
	 */
	private static String getEncoding(String filePath) {
		String encoding = System.getProperty("file.encoding");

		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
			final byte[] buffer = new byte[3];
			final int length = fileInputStream.read(buffer);

			if (length >= 2) {
				final byte[] utf16LittleEndianBOM = getUTF16LittleEndianBOM();
				final byte[] utf16BigEndianBOM = getUTF16BigEndianBOM();

				if ((buffer[0] == utf16LittleEndianBOM[0] && buffer[1] == utf16LittleEndianBOM[1]) /* UTF-16, little endian */ ||
						(buffer[0] == utf16BigEndianBOM[0] && buffer[1] == utf16BigEndianBOM[1]) /* UTF-16, big endian */) {
					encoding = "UTF16";
				}
			}

			if (length >= 3) {
				byte[] utf8BOM = getUTF8BOM();

				if (buffer[0] == utf8BOM[0] && buffer[1] == utf8BOM[1] && buffer[2] == utf8BOM[2]) /* UTF-8 */ {
					encoding = "UTF8";
				}
			}
		} catch (IOException ioException) {
			Globals.getLogger().info(String.format("FileUtils.getEncoding - exception %s filepath: %s",
					ioException.getMessage(), filePath));
			ioException.printStackTrace();
		}

		return encoding;
	}
	
	/***
	 * Returns the text of the specified file. If a Unicode Byte Order Mark (BOM) is found, the file is read with the corresponding encoding.
	 * Otherwise the file is read using the default encoding.
	 * @param filePath file path
	 * @return text of file
	 * @throws IOException
	 */
	public static String readFile(String filePath) throws IOException {
		final String encoding = getEncoding(filePath);
		
		final StringBuilder text = new StringBuilder();

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding))) {
			char[] buffer = new char[1024 * 16];
			int length;
			
			while ((length = bufferedReader.read(buffer)) != -1) {
				text.append(buffer, 0, length);
			}
		}

		return text.toString();
	}

    public static String readFile(InputStream inputStream) throws IOException {
        final StringBuilder text = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            char[] buffer = new char[1024 * 16];
            int length;

            while ((length = bufferedReader.read(buffer)) != -1) {
                text.append(buffer, 0, length);
            }
        }

        return text.toString();
    }
	
	/**
	 * Copies the specified file
	 * @param srcPath path of file to be copied
	 * @param destPath path to where file will be copied
	 * @throws IOException
	 */
	public static void copyFile(String srcPath, String destPath) throws IOException {
		try (FileInputStream inputStream = new FileInputStream(srcPath); FileOutputStream outputStream = new FileOutputStream(destPath)) {
	        final byte[] buffer = new byte[1024];
	        int length;
	        
	        while ((length = inputStream.read(buffer)) > 0) {
	            outputStream.write(buffer, 0, length);
	        }

	        outputStream.flush();
		}
	}
	
	/**
	 * Delete the specified file
	 * @param filePath path of file to be deleted
	 */
	public static void deleteFile(String filePath) {
		final File file = new File(filePath);
		
		if (!file.exists()) {
			final String errorMessage = MessageFormat.format("Cannot delete file {0} - file does not exist.", filePath);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (file.isDirectory()) {
			final String errorMessage = MessageFormat.format("Cannot delete file {0} - path refers to a folder.", filePath);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (!file.canWrite()) {
			final String errorMessage = MessageFormat.format("Cannot delete file {0} - file is write-protected.", filePath);
			throw new IllegalArgumentException(errorMessage);
		}
		
		boolean success = file.delete();
		
		if (!success) {
			final String errorMessage = MessageFormat.format("Cannot delete file {0} - deletion failed.", filePath);
			throw new IllegalArgumentException(errorMessage);
		}
	}
	
	/**
	 * Renames the specified file.
	 * @param srcFilePath path of file to rename
	 * @param destFilePath new name of file
	 */
	public static void renameFile(String srcFilePath, String destFilePath) {
		final File srcFile = new File(srcFilePath);
		final File destFile = new File(destFilePath);
		
		if (!srcFile.exists()) {
			final String errorMessage = MessageFormat.format("Cannot rename file {0} - file does not exist.", srcFilePath);
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (srcFile.isDirectory()) {
			final String errorMessage = MessageFormat.format("Cannot rename file {0} - path refers to a folder.", srcFilePath);
			throw new IllegalArgumentException(errorMessage);
		}
		
		boolean success = srcFile.renameTo(destFile);
		
		if (!success) {
			final String errorMessage = MessageFormat.format("Cannot rename file {0} to {1} - deletion failed.", srcFilePath, destFilePath);
			throw new IllegalArgumentException(errorMessage);
		}
	}
	
	/**
	 * Returns the folder containing the Vault 3 .jar file.
	 * @return path to folder containing the Vault 3 .jar file.
	 */
	public static String getRootPath() {
		String rootPath;
		
		try {
			rootPath = URLDecoder.decode(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			
			final String osName = System.getProperty("os.name");
			
			if (osName.toUpperCase().contains("WINDOWS")) {
				rootPath = rootPath.substring(1);
			}
			
			rootPath = new File(rootPath).getParent();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

			// If an error occurred, default to current directory.
			rootPath = System.getProperty("user.dir");
		}

		Globals.getLogger().info(MessageFormat.format("getRootPath: {0}", rootPath));
		
		return rootPath;
	}
	
	/**
	 * Returns the path to the folder where the Vault 3.properties configuration file, and other configuration files, are stored.
	 * @return path of configuration file folder
	 */
	public static String getConfigRootPath() {
		String configRootPath = System.getProperty(ConfigValueKeys.ConfigurationRootPath);
		
		if (configRootPath == null || configRootPath.isEmpty()) {
			configRootPath = getRootPath();
		}
		else {
			File directory = new File(configRootPath);
			
			if (!directory.exists()) {
				boolean result = directory.mkdir();
				
				Globals.getLogger().info(String.format("getConfigRootPath: Creating config folder %s result: %s", configRootPath, result));
			}
		}
		
		Globals.getLogger().info(String.format("getConfigRootPath: %s", configRootPath));
		
		return configRootPath;
	}
	
	public static String getFileType(File file) {
		final String fileName = file.getName();
		
		return getFileType(fileName);
	}
	
	public static String getFileType(String fileName) {
		String fileType = null;
		
		int index = fileName.lastIndexOf('.');
		
		if (index >= 0) {
			fileType = fileName.substring(index + 1);
		}
		
		return fileType;
	}
	
	private static void getUniqueFileTypes(String rootFolderPath, Dictionary<String, Boolean> uniqueFileTypes) {
		final File rootFolder = new File(rootFolderPath);
		
		if (rootFolder.isDirectory()) {
			final File[] list = rootFolder.listFiles();
			
			if (list != null && list.length > 0) {
				for (File child : list) {
					if (child.isFile()) {
						String fileType = getFileType(child);

						if (fileType == null) {
							fileType = StringLiterals.EmptyString;
						}

						uniqueFileTypes.put(fileType.toLowerCase(), Boolean.TRUE);
					}
					else if (child.isDirectory()) {
						getUniqueFileTypes(child.getAbsolutePath(), uniqueFileTypes);
					}
				}
			}
		}
	}
	
	public static Dictionary<String, Boolean> getUniqueFileTypes(String rootFolderPath) {
		final Dictionary<String, Boolean> uniqueFileTypes = new Hashtable<>();
		
		getUniqueFileTypes(rootFolderPath, uniqueFileTypes);
		
		return uniqueFileTypes;
	}
	
	/**
	 * Deletes all files and folders in the specified folder.
	 * @param folder
	 */
	public static void deleteFolderContents(File folder) {
		Globals.getLogger().info(String.format("deleteFolderContents: %s", folder.getAbsolutePath()));
		
		if (folder.isDirectory()) {
			final String[] children = folder.list();
			
			for (String child : children) {
				deleteFolderContents(new File(folder, child));
			}
		}
		
		folder.delete();
	}
}
