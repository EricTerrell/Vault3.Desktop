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

import java.io.File;

public class PhotoUtils {
	public static String getPhotoPath(String photoPath) {
		String resultPath = null;

		if (StringUtils.isURL(photoPath)) {
			resultPath = photoPath.trim();
		}
		else {
			if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.LoadPhotosFromOriginalLocations)) {
				resultPath = photoPath;
			}
			else
			{
				String substitutePhotoFolder = Globals.getPreferenceStore().getString(PreferenceKeys.SubstitutePhotoFolder);
				final String fileSeparator = System.getProperty("file.separator");
				
				while (substitutePhotoFolder.endsWith(fileSeparator)) {
					substitutePhotoFolder = substitutePhotoFolder.substring(0, substitutePhotoFolder.length() - 1);
				}

				final String[] pathSegments = photoPath.split("\\\\|/");
				
				for (int firstSegment = 0; firstSegment < pathSegments.length; firstSegment++) {
					final StringBuilder currentPath = new StringBuilder(substitutePhotoFolder);
					
					for (int segment = firstSegment; segment < pathSegments.length; segment++) {
						currentPath.append(fileSeparator);
						currentPath.append(pathSegments[segment]);
					}
					
					final File file = new File(currentPath.toString());
					
					if (file.exists()) {
						resultPath = currentPath.toString();
						break;
					}
				}
			}
		}
		
		return resultPath;
	}

	private static boolean isPhotoFileType(String fileType) {
		boolean isPhotoFileType = false;
		
		if (fileType != null) {
			fileType = fileType.toLowerCase();
			
			isPhotoFileType = fileType.equals("bmp") || fileType.equals("jpg") || fileType.equals("gif") || fileType.equals("png") || fileType.equals("tif") || fileType.equals("wmf");
		}
		
		return isPhotoFileType;
	}
	
	/**
	 * Return true if the specified File points to a digital photo (as opposed to, for example, a video file).
	 * @param file file path
	 * @return true if the path points to a photo
	 */
	public static boolean isPhotoFile(File file) {
		final String fileType = FileUtils.getFileType(file);

		return isPhotoFileType(fileType);
	}
	
	/**
	 * Return true if the specified file path points to a digital photo (as opposed to, for example, a video file).
	 * @param path file path
	 * @return true if the path points to a photo
	 */
	public static boolean isPhotoFile(String path) {
		final String fileType = FileUtils.getFileType(path);
		
		return isPhotoFileType(fileType);
	}
}
