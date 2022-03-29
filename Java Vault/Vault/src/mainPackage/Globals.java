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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import commonCode.IPlatform.PlatformEnum;

public class Globals {
	private static PlatformEnum platform;
	
	public static PlatformEnum getPlatform() {
		return platform;
	}
	
	private static ImageRegistry imageRegistry;
	
	public static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
	
	private static ColorRegistry colorRegistry;
	
	public static ColorRegistry getColorRegistry() {
		return colorRegistry;
	}
	
	private static RGB whiteColor = new RGB(255, 255, 255);
	
	public static RGB getWhiteColor() {
		return whiteColor;
	}
	
	private static MainApplicationWindow mainApplicationWindow;
	
	public static void setMainApplicationWindow(MainApplicationWindow _mainApplicationWindow) {
		mainApplicationWindow = _mainApplicationWindow;
	}
	
	public static MainApplicationWindow getMainApplicationWindow() {
		return mainApplicationWindow;
	}
	
	private static VaultTextViewer vaultTextViewer;
	
	public static VaultTextViewer getVaultTextViewer() {
		return vaultTextViewer;
	}

	public static void setVaultTextViewer(VaultTextViewer vaultTextViewer) {
		Globals.vaultTextViewer = vaultTextViewer;
	}

	private static VaultTreeViewer vaultTreeViewer;

	public static VaultTreeViewer getVaultTreeViewer() {
		return vaultTreeViewer;
	}

	public static void setVaultTreeViewer(VaultTreeViewer vaultTreeViewer) {
		Globals.vaultTreeViewer = vaultTreeViewer;
	}
	
	private static VaultDocument vaultDocument;

	public static VaultDocument getVaultDocument() {
		return vaultDocument;
	}

	public static void setVaultDocument(VaultDocument vaultDocument) {
		Globals.vaultDocument = vaultDocument;
	}
	
	public static void setBusyCursor() {
		Globals.getMainApplicationWindow().getShell().setCursor(new Cursor(Globals.getMainApplicationWindow().getShell().getDisplay(), SWT.CURSOR_WAIT));
		Globals.getVaultTextViewer().getTextWidget().setCursor(new Cursor(Globals.getVaultTextViewer().getTextWidget().getDisplay(), SWT.CURSOR_WAIT));
	}

	public static void setPreviousCursor() {
		Cursor[] cursors = new Cursor[] { Globals.getMainApplicationWindow().getShell().getCursor(), Globals.getVaultTextViewer().getTextWidget().getShell().getCursor() };
		
		Globals.getMainApplicationWindow().getShell().setCursor(null);
		Globals.getVaultTextViewer().getTextWidget().setCursor(null);

		for (Cursor cursor : cursors) {
			if (cursor != null) {
				cursor.dispose();
			}
		}
	}
	
	private static OutlineItemClipboard clipboard = new OutlineItemClipboard();
	
	public static OutlineItemClipboard getClipboard() {
		return clipboard;
	}
	
	private static Logger logger = Logger.getLogger("mainPackage");

	public static Logger getLogger() {
		return logger;
	}
	
	private static PreferenceStore preferenceStore;
	
	public static PreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	
	private static MRUFileList mruFiles;
	
	public static MRUFileList getMRUFiles() {
		return mruFiles;
	}

	private static FindAndReplaceDialog findAndReplaceDialog;

	public static FindAndReplaceDialog getFindAndReplaceDialog() {
		return findAndReplaceDialog;
	}
	
	public static void setFindAndReplaceDialog(FindAndReplaceDialog dialog) {
		findAndReplaceDialog = dialog;
	}

	private static PasswordCache passwordCache;
	
	public static PasswordCache getPasswordCache() {
		return passwordCache;
	}
	
	public static final String IMAGE_REGISTRY_VAULT_ICON = "vault icon";
	public static final String IMAGE_REGISTRY_LIGHTBULB = "lightbulb";
	
	public static void initializeImageRegistry() {
		imageRegistry = new ImageRegistry(Display.getCurrent());
		
		Image image = new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("artwork/vault3.png"));
		imageRegistry.put(IMAGE_REGISTRY_VAULT_ICON, image);
		
		image = new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("artwork/lightbulb.png"));
		imageRegistry.put(IMAGE_REGISTRY_LIGHTBULB, image);
	}
	
	static {
		platform = new Platform().getPlatform();
		
		mruFiles = new MRUFileList();
		passwordCache = new PasswordCache();

		String propertiesFilePath = String.format("%s%s%s.properties", FileUtils.getConfigRootPath(), PortabilityUtils.getFileSeparator(), StringLiterals.ProgramName);
		
		getLogger().info(String.format("Properties file path: %s", propertiesFilePath));
		
		colorRegistry = new ColorRegistry();
		
		preferenceStore = new PreferenceStore(propertiesFilePath);

		try {
			preferenceStore.load();
		} catch (IOException ex) {
			Globals.getLogger().info(MessageFormat.format("Globals.static initializer: cannot load preference file. {0}{1}{2}", ex.getMessage(), PortabilityUtils.getNewLine(), ex.getStackTrace()));
		}
		
		setDefaultPreferenceValues();
		
		preferenceStore.addPropertyChangeListener(new PropertyChangeListener(preferenceStore));
	}
	
	private static void setDefaultPreferenceValues() {
		preferenceStore.setDefault(PreferenceKeys.OutlineFontString, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.TextMarginWidth, 5);
		preferenceStore.setDefault(PreferenceKeys.TextMarginHeight, 5);
		preferenceStore.setDefault(PreferenceKeys.AutoSaveMinutes, 0);
		preferenceStore.setDefault(PreferenceKeys.SaveOldFileWithBakType, true);
		preferenceStore.setDefault(PreferenceKeys.StatusBarMessageDuration, 5);
		preferenceStore.setDefault(PreferenceKeys.SlideshowInterval, 5);
		preferenceStore.setDefault(PreferenceKeys.SlideshowAllItems, true);
		preferenceStore.setDefault(PreferenceKeys.SlideshowContinuousLoop, true);
		preferenceStore.setDefault(PreferenceKeys.SlideshowExclusions, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.MainWindowX, -1);
		preferenceStore.setDefault(PreferenceKeys.MainWindowY, -1);
		preferenceStore.setDefault(PreferenceKeys.MainWindowWidth, -1);
		preferenceStore.setDefault(PreferenceKeys.MainWindowHeight, -1);
		preferenceStore.setDefault(PreferenceKeys.MainWindowMinimumWidth, 400);
		preferenceStore.setDefault(PreferenceKeys.MainWindowMinimumHeight, 300);
		preferenceStore.setDefault(PreferenceKeys.DialogMinimumWidth, 200);
		preferenceStore.setDefault(PreferenceKeys.DialogMinimumHeight, 100);
		preferenceStore.setDefault(PreferenceKeys.SashWidth, 5);
		preferenceStore.setDefault(PreferenceKeys.SashWidthLeft, 50);
		preferenceStore.setDefault(PreferenceKeys.SashWidthRight, 50);
		preferenceStore.setDefault(PreferenceKeys.MaxMRUFiles, 9);
		preferenceStore.setDefault(PreferenceKeys.FindReplaceFindText, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.FindReplaceReplaceText, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.FindReplaceMatchCase, false);
		preferenceStore.setDefault(PreferenceKeys.FindReplaceMatchWholeWord, false);
		preferenceStore.setDefault(PreferenceKeys.HidePasswordCharacters, true);
		preferenceStore.setDefault(PreferenceKeys.SecureRandomAlgorithm, "SHA1PRNG");
		preferenceStore.setDefault(PreferenceKeys.CipherAlgorithm, "AES");
		preferenceStore.setDefault(PreferenceKeys.KeyLength, 128);
		preferenceStore.setDefault(PreferenceKeys.KeyAlgorithm1_1, "AES");
		preferenceStore.setDefault(PreferenceKeys.CipherAlgorithm1_1, "AES");
		preferenceStore.setDefault(PreferenceKeys.KeyLength1_1, 128);
		preferenceStore.setDefault(PreferenceKeys.SplitMovePixels, 10);
		preferenceStore.setDefault(PreferenceKeys.URLRegex, "((www|http|https)(\\W+\\S+[^).,:;?\\]\\} \\r\\n$]+))|(file:///[\\S]+)"); // From Sean Harrop, "Matching urls in free text", regexlib.com
		preferenceStore.setDefault(PreferenceKeys.LoadPhotosFromOriginalLocations, true);
		preferenceStore.setDefault(PreferenceKeys.AcceptLicenseTerms, false);
		preferenceStore.setDefault(PreferenceKeys.SingletonSocketPort, 54321);
		preferenceStore.setDefault(PreferenceKeys.CachePasswords, true);
		preferenceStore.setDefault(PreferenceKeys.AllowMultipleInstances, false);
		preferenceStore.setDefault(PreferenceKeys.EmailFromAddress, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.EmailServerAddress, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.EmailUserName, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.EmailPassword, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.EmailAuthentication, false);
		preferenceStore.setDefault(PreferenceKeys.EmailScaleImagesSelection, 0);
		preferenceStore.setDefault(PreferenceKeys.HorizontalSplitterWeight0, 90);
		preferenceStore.setDefault(PreferenceKeys.HorizontalSplitterWeight1, 10);
		preferenceStore.setDefault(PreferenceKeys.ImportAllFileTypes, false);
		preferenceStore.setDefault(PreferenceKeys.AdvancedGraphics, true);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosDestFolder, StringLiterals.EmptyString);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosDeleteFolderContents, false);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosWidth, 0);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosHeight, 0);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosTotalPhotos, 1500);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosPhotosPerFolder, 100);
		preferenceStore.setDefault(PreferenceKeys.ExportPhotosShuffle, false);
		preferenceStore.setDefault(PreferenceKeys.CheckForUpdatesAutomatically, true);
		preferenceStore.setDefault(PreferenceKeys.LastUpdateCheckDate, 0);
		preferenceStore.setDefault(PreferenceKeys.DebuggingMode, false);
		preferenceStore.setDefault(PreferenceKeys.MaxSavedSearches, 40);
		preferenceStore.setDefault(PreferenceKeys.WarnAboutSingleInstance, true);
		preferenceStore.setDefault(PreferenceKeys.ForceUpperCasePasswords, true);
		preferenceStore.setDefault(PreferenceKeys.Vault3ForAndroidURL, "http://www.ericbt.com/Vault3ForAndroid");
		preferenceStore.setDefault(PreferenceKeys.CheckForModificationsMinutes, 0);
		preferenceStore.setDefault(PreferenceKeys.EmailSMTPPort, 25);
		preferenceStore.setDefault(PreferenceKeys.EmailSSL, false);
		preferenceStore.setDefault(PreferenceKeys.TextBackgroundRed, 255);
		preferenceStore.setDefault(PreferenceKeys.TextBackgroundGreen, 255);
		preferenceStore.setDefault(PreferenceKeys.TextBackgroundBlue, 255);
		preferenceStore.setDefault(PreferenceKeys.AlsoDeleteOutlineItem, true);
	}
}
