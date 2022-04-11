/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
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

import java.text.MessageFormat;

/**
 * @author Eric Bergman-Terrell
 *
 * Version History:
 * 
 * Software Version Document Version Notes
 * 0.00 - 0.38      1.0
 * 0.38 - 0.41		1.1
 * 0.42             1.2
 * 0.43             1.2				 Made saved searches document-specific, and encrypted (when the corresponding document is encrypted)
 * 0.44             1.2				 Performance improvements to document loading (no longer get NullPointerExceptions on null font strings). 
 *                                   Bug Fix: busy cursor is now displayed during document load process.
 * 0.45             1.2              Bug Fix: On Mac, printing always printed from the current position to the end of the document
 * 0.46				1.2				 Bug Fix: In some cases when documents are opened, the first outline item is not automatically selected.
 * 									 Bug Fix: When the first outline item is automatically selected after opening a document, text is not
 * 									 rendered in the item's font.
 * 									 When importing from the file system, folders and files are now sorted alphabetically.
 * 									 Bug Fix: Some keyboard accelerators were broken on Windows and Linux.
 * 0.47				1.2				 Added pdf file export.
 * 0.48				1.2				 Photo export is now multithreaded.
 * 									 Bug Fix: when user exports photos, and specifies a target folder that does not exist, and specifies that the
 * 									 folder should be deleted, a null pointer exception is no longer thrown.
 * 0.49				1.2				 Photos can now be specified by URL rather than just filename.
 * 0.50				1.2				 Vault 3 works better with file sync programs.
 * 0.51				1.2				 SMTP port can now be specified in email settings.
 * 0.52				1.2				 SSL can now be specified for SMTP email authentication.
 * 0.53				1.2				 Fixed case-insensitive searches in Russian and other languages.
 * 0.54 - 0.55		1.2				 Users can now specify the text background.
 * 0.56				1.2				 Rebuilt with latest Eclipse libraries.
 * 0.57				1.2				 Rebuilt with latest Eclipse libraries.
 * 0.58				1.2				 Unsuccessful attempt at bug fix: Settings sometimes discarded?
 * 0.59				1.2				 Bug Fix: Settings sometimes discarded
 * 0.60             1.2              Project can now be built with IDEA, in addition to Eclipse.
 * 0.61             1.2              Bug Fixes for OSX: 
 * 										1) Application name displays in menu rather than "SWT"
 * 										2) Application menu is available when the program starts.
 * 										3) Application does not interfere with system shutdown
 * 									 Using latest Eclipse libraries.
 * 0.62				1.2				 Bug Fix: when user clicks Ctrl+O and cancels the file open, document is no longer
 *                                   re-loaded.
 * 0.63				1.2				 Added the ability to automatically delete the current outline item when a photo file is deleted.
 * 0.64				1.2				 Bug Fix: in previous versions, when exporting photos to a device, the height or width of the exported images
 * 									 sometimes exceeded the specified device width or height.
 * 0.65				1.2				 Rebuilt with latest Eclipse libraries. Removed 32-bit versions.
 * 									 Bug Fix: Check for Updates was broken as a result of ericbt.com changing to https.
 * 0.66				1.2				 Bug Fix: File / Save As did not prompt user to overwrite file.
 * 0.67				1.2				 Rebuilt with latest libraries. Hopefully will fix failure to launch on Mac OSX "Big Sur".
 * 0.68				1.2				 Can now import files with no file types.
 * 0.69				1.2				 Bug Fix: If Vault 3 is maximized when closed, it will be maximized when re-launched.
 * 0.70				1.2				 Bug Fixes:
 * 										1) In previous versions (0.67-0.69), the app's window would be drawn, hidden, and re-drawn, when the app was launched.
 * 									    2) In previous versions, on Linux, the password prompt dialog did not include a title bar and could not be moved.
 * 0.71				1.2				 Additional fixes allowing Vault 3 to save and restore the maximized window state.
 * 0.72				1.2				 Rotate Photos Right / Left
 * 0.73				1.2				 Preparations for improved encryption/decription
 * 0.74				1.2				 Bug Fix: vertical slider position was not properly restored after being moved.
 * 0.75				1.2				 Bug Fix: For Mac OSX, "cannot load library: java.lang.UnsatisfiedLinkError: no sqlite4java-osx-x86_64 in java.library.path" error.
 */
public class Version {
	private static final float versionNumber = 0.75f;
	private static final int copyrightYear = 2022;

	public static float getVersionNumber() {
		return versionNumber;
	}
	
	public static String getVersionNumberText() {
		return MessageFormat.format("{0,number,0.00}", versionNumber);
	}

	public static String getCopyrightYear() {
		return String.valueOf(copyrightYear);
	}
}
