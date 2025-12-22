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
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class SettingsDialog extends VaultDialog {
	private final PreferenceStore preferenceStore;
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText("Settings");
	}

	@Override
	protected void populateFields() {
		photoExclusionText.setText(preferenceStore.getString(PreferenceKeys.PhotoExclusions));
		photoEditingProgramText.setText(preferenceStore.getString(PreferenceKeys.PhotoEditingProgramPath));
		startupFilePathText.setText(preferenceStore.getString(PreferenceKeys.StartupFilePath));

		updateFontDisplay();
		
		substituteFolderLabel.setText(preferenceStore.getString(PreferenceKeys.SubstitutePhotoFolder));
	}

	private int autoSaveIntervalMinutes, checkForModificationsIntervalMinutes, cpuCoresForPhotoExports;

	private Button autoSaveCheckBox, saveWithBakFileTypeCheckBox, loadFileOnStartupButton,
            loadMostRecentlyUsedFileButton, doNotAutomaticallyLoadFileButton,
            loadPhotosFromOriginalLocationsRadioButton, loadPhotosFromSubstituteFolderRadioButton, okButton,
            cachePasswords, allowMultipleInstances, advancedGraphics, slideShowFullScreen, checkForUpdatesCheckBox,
            warnAboutSingleInstance, checkForModificationCheckBox, includeTextInPhotoExports;

    private Spinner includeTextSize;

	private Label substituteFolderLabel, defaultTextFontLabel, statusLabel;
	
	private Text startupFilePathText, photoExclusionText, photoEditingProgramText;
	
	private Canvas textForegroundColorCanvas, textBackgroundColorCanvas;

	private Color nonErrorBackground, errorBackground;
	
	private String previousSubstitutePhotoFolder = null, fontString;

	private RGB textForegroundColor, textBackgroundColor;
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);
		
	    statusLabel = createStatusLabel(parent);
	
		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		enableDisableOKButton();

		return result;
	}

	@Override
	public void create() {
		super.create();

		// enableDisableOKButton is called before all fields have been loaded, since it's used as the argument to
		// addModifyListener in createDialogArea. Consequently, we need to call it a final time after the dialog has
		// been fully created. Otherwise, there will be a spurious warning message.
		enableDisableOKButton();
	}

	private void enableDisableOKButton() {
		final boolean unspecifiedStartupFileError = loadFileOnStartupButton.getSelection() && startupFilePathText.getText().trim().isEmpty();
		
		final boolean unspecifiedSubstitutePhotoFolderError = (loadPhotosFromSubstituteFolderRadioButton.getSelection() && substituteFolderLabel.getText().trim().isEmpty());

		okButton.setEnabled(!unspecifiedStartupFileError && !unspecifiedSubstitutePhotoFolderError);
		
	    if (unspecifiedStartupFileError) {
	    	setStatusLabelText(statusLabel, "Startup File Must Be Specified in Startup File Tab.");
	        statusLabel.setBackground(errorBackground);
	    }
	    else if (unspecifiedSubstitutePhotoFolderError)
	    {
	    	setStatusLabelText(statusLabel, "Substitute Photo Folder Must Be Specified in Substitute Folder Tab.");
	        statusLabel.setBackground(errorBackground);
	    }
	    else {
	    	setStatusLabelText(statusLabel, StringLiterals.EmptyString);
	    	statusLabel.setBackground(nonErrorBackground);
	    }
	}
	
	@Override
	protected void okPressed() {
		preferenceStore.setValue(PreferenceKeys.LoadFileOnStartup,          loadFileOnStartupButton.getSelection()); 
		preferenceStore.setValue(PreferenceKeys.LoadMostRecentlyUsedFile,   loadMostRecentlyUsedFileButton.getSelection()); 
		preferenceStore.setValue(PreferenceKeys.DoNotAutomaticallyLoadFile, doNotAutomaticallyLoadFileButton.getSelection()); 
		preferenceStore.setValue(PreferenceKeys.StartupFilePath,             startupFilePathText.getText());
		
		preferenceStore.setValue(PreferenceKeys.AutoSaveMinutes, autoSaveCheckBox.getSelection() ? autoSaveIntervalMinutes : 0);
		preferenceStore.setValue(PreferenceKeys.SaveOldFileWithBakType, saveWithBakFileTypeCheckBox.getSelection());
		
		preferenceStore.setValue(PreferenceKeys.CheckForModificationsMinutes, checkForModificationCheckBox.getSelection() ? checkForModificationsIntervalMinutes : 0);
		
		preferenceStore.setValue(PreferenceKeys.LoadPhotosFromOriginalLocations, loadPhotosFromOriginalLocationsRadioButton.getSelection());
		preferenceStore.setValue(PreferenceKeys.SubstitutePhotoFolder, substituteFolderLabel.getText());
		preferenceStore.setValue(PreferenceKeys.AdvancedGraphics, advancedGraphics.getSelection());
		preferenceStore.setValue(PreferenceKeys.SlideshowFullScreen, slideShowFullScreen.getSelection());
		preferenceStore.setValue(PreferenceKeys.PhotoExclusions, photoExclusionText.getText());
		
		preferenceStore.setValue(PreferenceKeys.DefaultTextFont, fontString);
		
		preferenceStore.setValue(PreferenceKeys.DefaultTextFontRed, textForegroundColor.red);
		preferenceStore.setValue(PreferenceKeys.DefaultTextFontGreen, textForegroundColor.green);
		preferenceStore.setValue(PreferenceKeys.DefaultTextFontBlue, textForegroundColor.blue);
		
		preferenceStore.setValue(PreferenceKeys.TextBackgroundRed, textBackgroundColor.red);
		preferenceStore.setValue(PreferenceKeys.TextBackgroundGreen, textBackgroundColor.green);
		preferenceStore.setValue(PreferenceKeys.TextBackgroundBlue, textBackgroundColor.blue);
		
		Globals.getVaultTextViewer().refresh();
		
		preferenceStore.setValue(PreferenceKeys.CachePasswords, cachePasswords.getSelection());
		
		preferenceStore.setValue(PreferenceKeys.AllowMultipleInstances, allowMultipleInstances.getSelection());

		if (warnAboutSingleInstance.isEnabled()) {
			preferenceStore.setValue(PreferenceKeys.WarnAboutSingleInstance, warnAboutSingleInstance.getSelection());
		}
		
		preferenceStore.setValue(PreferenceKeys.PhotoEditingProgramPath, photoEditingProgramText.getText());
        preferenceStore.setValue(PreferenceKeys.CPUCoresForPhotoExports, cpuCoresForPhotoExports);
        preferenceStore.setValue(PreferenceKeys.IncludeOutlineTextInExportedPhotos,
                includeTextInPhotoExports.getSelection());
        preferenceStore.setValue(PreferenceKeys.IncludeOutlineTextSize, includeTextSize.getSelection());

		preferenceStore.setValue(PreferenceKeys.CheckForUpdatesAutomatically, checkForUpdatesCheckBox.getSelection());
		
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());

		final TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gridData);
		
		final TabItem startupFileTabItem = new TabItem(tabFolder, SWT.NONE);
		startupFileTabItem.setText("&Startup File");

		final TabItem savingTabItem = new TabItem(tabFolder, SWT.NONE);
		savingTabItem.setText("Sa&ving");
		
		final TabItem syncTabItem = new TabItem(tabFolder, SWT.NONE);
		syncTabItem.setText("S&ync");
		
		final TabItem passwordsTabItem = new TabItem(tabFolder, SWT.NONE);
		passwordsTabItem.setText("P&asswords");

		final TabItem instancesTabItem = new TabItem(tabFolder, SWT.NONE);
		instancesTabItem.setText("&Instances");

		final TabItem fontsAndColorsTabItem = new TabItem(tabFolder, SWT.NONE);
		fontsAndColorsTabItem.setText("Fo&nts && Colors");

		final TabItem photosTabItem = new TabItem(tabFolder, SWT.NONE);
		photosTabItem.setText("&Photos && Slideshows");
		
		final TabItem substituteFolderTabItem = new TabItem(tabFolder, SWT.NONE);
		substituteFolderTabItem.setText("Su&bstitute Folder");
		
		final TabItem updatesTabItem = new TabItem(tabFolder, SWT.NONE);
		updatesTabItem.setText("&Updates");

		final Composite startupComposite = new Composite(tabFolder, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		startupComposite.setLayout(gridLayout);
		
		loadFileOnStartupButton = new Button(startupComposite, SWT.RADIO);
		loadFileOnStartupButton.setText("Open the Fo&llowing File on Startup:");

		loadFileOnStartupButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableOKButton();
			}
		});
		
		startupFilePathText = new Text(startupComposite, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		startupFilePathText.setLayoutData(gridData);
		
		startupFilePathText.addModifyListener(e -> enableDisableOKButton());

		startupFilePathText.addFocusListener(new TextFocusListener());
		
		new Label(startupComposite, SWT.NONE).setText(StringLiterals.EmptyString);

		final Button specifyFileButton = new Button(startupComposite, SWT.NONE);
		specifyFileButton.setText("Specify Startup &File...");
		
		specifyFileButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				final String vault3File = MessageFormat.format("{0} File", StringLiterals.ProgramName);
				fileDialog.setFilterNames(new String[] { vault3File, "All Files" });
				fileDialog.setFilterExtensions(new String[] { StringLiterals.ProgramFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
				fileDialog.setText("Specify File");
				
				boolean finished = false;
				
				do {
					final String filePath = fileDialog.open();
					
					if (filePath != null && new File(filePath).exists()) {
						loadFileOnStartupButton.setSelection(true);
						loadMostRecentlyUsedFileButton.setSelection(false);
						doNotAutomaticallyLoadFileButton.setSelection(false);
						startupFilePathText.setText(filePath);
						
						finished = true;
					}
					else if (filePath == null) { 
						finished = true;
					}
				} while (!finished);
				
				enableDisableOKButton();
			}
		});
		
		new Label(startupComposite, SWT.NONE).setText(StringLiterals.EmptyString);
		
		loadMostRecentlyUsedFileButton = new Button(startupComposite, SWT.RADIO);
		loadMostRecentlyUsedFileButton.setText("Open &Most Recently Opened File on Startup");
		
		loadMostRecentlyUsedFileButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableOKButton();
			}
		});

		doNotAutomaticallyLoadFileButton = new Button(startupComposite, SWT.RADIO);
		doNotAutomaticallyLoadFileButton.setText("Don't &Automatically Open File on Startup");
		
		doNotAutomaticallyLoadFileButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableOKButton();
			}
		});

		loadFileOnStartupButton.setSelection(preferenceStore.getBoolean(PreferenceKeys.LoadFileOnStartup));
		loadMostRecentlyUsedFileButton.setSelection(preferenceStore.getBoolean(PreferenceKeys.LoadMostRecentlyUsedFile));
		doNotAutomaticallyLoadFileButton.setSelection(preferenceStore.getBoolean(PreferenceKeys.DoNotAutomaticallyLoadFile));
		
		if (!loadFileOnStartupButton.getSelection() && !loadMostRecentlyUsedFileButton.getSelection() && !doNotAutomaticallyLoadFileButton.getSelection()) {
			doNotAutomaticallyLoadFileButton.setSelection(true);
		}
		
		final Composite savingComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(3, false);
		savingComposite.setLayout(gridLayout);

		autoSaveCheckBox = new Button(savingComposite, SWT.CHECK);
		autoSaveCheckBox.setText("A&utomatically Save Document Every");
		
		final Scale saveIntervalScale = new Scale(savingComposite, SWT.HORIZONTAL);
		saveIntervalScale.setMinimum(1);
		saveIntervalScale.setMaximum(60);
		
		int minutes = preferenceStore.getInt(PreferenceKeys.AutoSaveMinutes);
		
		autoSaveCheckBox.setSelection(minutes > 0);
		
		if (minutes > 0) {
			saveIntervalScale.setSelection(minutes);
		}

		final String minutesFormat = "{0} Minute(s)";
		
		final Label autoSaveIntervalLabel = new Label(savingComposite, SWT.NONE);
		String text = MessageFormat.format(minutesFormat, saveIntervalScale.getSelection());
		autoSaveIntervalLabel.setText(text);
		
		autoSaveIntervalMinutes = saveIntervalScale.getSelection();

		// Allocate room for 2 digits (plus some extra space to account for different width of digits).
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = GraphicsUtils.getTextExtent(text).x + GraphicsUtils.getTextExtent("0").x * 3;
		gridData.horizontalAlignment = SWT.LEFT;
		autoSaveIntervalLabel.setLayoutData(gridData);

		saveIntervalScale.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final String text = MessageFormat.format(minutesFormat, saveIntervalScale.getSelection());
				autoSaveIntervalLabel.setText(text);
				autoSaveIntervalMinutes = saveIntervalScale.getSelection();
			}
		});
		
		saveWithBakFileTypeCheckBox = new Button(savingComposite, SWT.CHECK);
		saveWithBakFileTypeCheckBox.setText("&Before Saving a File, Save the Old File as <FILENAME>.bak");
		
		saveWithBakFileTypeCheckBox.setSelection(preferenceStore.getBoolean(PreferenceKeys.SaveOldFileWithBakType));
		
		gridData = new GridData();
		gridData.horizontalSpan = gridLayout.numColumns;
		saveWithBakFileTypeCheckBox.setLayoutData(gridData);

		final Composite syncComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(3, false);
		syncComposite.setLayout(gridLayout);

		checkForModificationCheckBox = new Button(syncComposite, SWT.CHECK);
		checkForModificationCheckBox.setText("Check for Document Changes Every");
		
		final Scale checkForModificationsIntervalScale = new Scale(syncComposite, SWT.HORIZONTAL);
		checkForModificationsIntervalScale.setMinimum(1);
		checkForModificationsIntervalScale.setMaximum(60);
		
		minutes = preferenceStore.getInt(PreferenceKeys.CheckForModificationsMinutes);
		
		checkForModificationCheckBox.setSelection(minutes > 0);
		
		if (minutes > 0) {
			checkForModificationsIntervalScale.setSelection(minutes);
		}

		final Label checkForModificationsIntervalLabel = new Label(syncComposite, SWT.NONE);
		text = MessageFormat.format(minutesFormat, checkForModificationsIntervalScale.getSelection());
		checkForModificationsIntervalLabel.setText(text);
		
		checkForModificationsIntervalMinutes = checkForModificationsIntervalScale.getSelection();

		// Allocate room for 2 digits (plus some extra space to account for different width of digits).
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = GraphicsUtils.getTextExtent(text).x + GraphicsUtils.getTextExtent("0").x * 3;
		gridData.horizontalAlignment = SWT.LEFT;
		checkForModificationsIntervalLabel.setLayoutData(gridData);

		checkForModificationsIntervalScale.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = MessageFormat.format(minutesFormat, checkForModificationsIntervalScale.getSelection());
				checkForModificationsIntervalLabel.setText(text);
				checkForModificationsIntervalMinutes = checkForModificationsIntervalScale.getSelection();
			}
		});

		final Composite passwordsComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		passwordsComposite.setLayout(gridLayout);
		
		cachePasswords = new Button(passwordsComposite, SWT.CHECK | SWT.WRAP);
		cachePasswords.setText(MessageFormat.format("Onl&y require a password to be entered the first time a given {0} document is accessed after {0} is launched", StringLiterals.ProgramName));
		cachePasswords.setSelection(preferenceStore.getBoolean(PreferenceKeys.CachePasswords));
		
		final Composite instancesComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		instancesComposite.setLayout(gridLayout);
		
		final Composite twoItemsComposite = new Composite(instancesComposite, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		twoItemsComposite.setLayout(gridLayout);
		
		allowMultipleInstances = new Button(twoItemsComposite, SWT.CHECK);
		allowMultipleInstances.setText(MessageFormat.format("&Allow multiple instances of {0}", StringLiterals.ProgramName));
		allowMultipleInstances.setSelection(preferenceStore.getBoolean(PreferenceKeys.AllowMultipleInstances));
		
		Image image = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);
		
		Label imageLabel = new Label(twoItemsComposite, SWT.NONE);
		imageLabel.setImage(image);
		
		imageLabel.setToolTipText(MessageFormat.format("This setting will take effect after all instances of {0} are shut down.", StringLiterals.ProgramName));
		
		warnAboutSingleInstance = new Button(instancesComposite, SWT.CHECK);
		warnAboutSingleInstance.setText(MessageFormat.format("&Warn when switching to running instance of {0}", StringLiterals.ProgramName));
		warnAboutSingleInstance.setSelection(preferenceStore.getBoolean(PreferenceKeys.WarnAboutSingleInstance));

		allowMultipleInstances.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableWarnAboutSingleInstance();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		enableDisableWarnAboutSingleInstance();

		final Composite fontsAndColorsComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		fontsAndColorsComposite.setLayout(gridLayout);

		defaultTextFontLabel = new Label(fontsAndColorsComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		defaultTextFontLabel.setLayoutData(gridData);

		final Label defaultTextFontColorLabel = new Label(fontsAndColorsComposite, SWT.NONE);
		defaultTextFontColorLabel.setText("Text Foreground Color:");

		textForegroundColorCanvas = new Canvas(fontsAndColorsComposite, SWT.BORDER);
		textForegroundColorCanvas.setBackground(new Color(textForegroundColor));
		
		gridData = new GridData();
		gridData.heightHint = gridData.widthHint = GraphicsUtils.getTextExtent(defaultTextFontColorLabel.getText()).y;
		textForegroundColorCanvas.setLayoutData(gridData);

        final Button specifyForegroundColorButton = new Button(fontsAndColorsComposite, SWT.NONE);
        specifyForegroundColorButton.setText("Specify Text Foreground Co&lor...");

        specifyForegroundColorButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                final ColorDialog colorDialog = new ColorDialog(getShell());
                colorDialog.setText("Text Foreground Color");
                colorDialog.setRGB(textForegroundColor);

                final RGB newColor = colorDialog.open();

                if (newColor != null) {
                    textForegroundColor = newColor;
                    textForegroundColorCanvas.setBackground(new Color(textForegroundColor));
                }
            }
        });

		Label spacerLabel = new Label(fontsAndColorsComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		spacerLabel.setLayoutData(gridData);

		final Button specifyFontButton = new Button(fontsAndColorsComposite, SWT.NONE);
		specifyFontButton.setText("Specify Text &Font...");
		
		specifyFontButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final FontDialog fontDialog = new FontDialog(getShell());

				FontData[] fontList = FontUtils.stringToFontList(preferenceStore.getString(PreferenceKeys.DefaultTextFont));
				
				if (fontList != null) {
					fontDialog.setFontList(fontList);
				}
				
				final int red   = preferenceStore.getInt(PreferenceKeys.DefaultTextFontRed);
				final int green = preferenceStore.getInt(PreferenceKeys.DefaultTextFontGreen);
				final int blue  = preferenceStore.getInt(PreferenceKeys.DefaultTextFontBlue);

				final RGB defaultColor = new RGB(red, green, blue);

				fontDialog.setRGB(defaultColor);
				fontDialog.setText("Specify Font");
				fontDialog.setEffectsVisible(true);

				final FontData fontData = fontDialog.open();
				
				if (fontData != null) {
					fontList = fontDialog.getFontList();
					
					fontString = FontUtils.fontListToString(fontList);

                    // Color will not be available on Linux.
                    if (fontDialog.getRGB() != null) {
                        textForegroundColor = fontDialog.getRGB();
                    }

					updateFontDisplay();
				}
			}
		});

		spacerLabel = new Label(fontsAndColorsComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		spacerLabel.setLayoutData(gridData);

        gridLayout = new GridLayout(2, false);
        fontsAndColorsComposite.setLayout(gridLayout);

        final Label textBackgroundColorLabel = new Label(fontsAndColorsComposite, SWT.NONE);
        textBackgroundColorLabel.setText("Text Background Color:");

        textBackgroundColorCanvas = new Canvas(fontsAndColorsComposite, SWT.BORDER);
        textBackgroundColorCanvas.setBackground(new Color(textBackgroundColor));

        gridData = new GridData();
        gridData.heightHint = gridData.widthHint = GraphicsUtils.getTextExtent(textBackgroundColorLabel.getText()).y;
        textBackgroundColorCanvas.setLayoutData(gridData);

        final Button specifyBackgroundColorButton = new Button(fontsAndColorsComposite, SWT.NONE);
        specifyBackgroundColorButton.setText("Specify &Text Background Color...");

        specifyBackgroundColorButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                final ColorDialog colorDialog = new ColorDialog(getShell());
                colorDialog.setText("Text Background Color");
                colorDialog.setRGB(textBackgroundColor);

                final RGB newColor = colorDialog.open();

                if (newColor != null) {
                    textBackgroundColor = newColor;
                    textBackgroundColorCanvas.setBackground(new Color(textBackgroundColor));
                }
            }
        });

        Composite substituteFolderComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		substituteFolderComposite.setLayout(gridLayout);
		
		loadPhotosFromOriginalLocationsRadioButton = new Button(substituteFolderComposite, SWT.RADIO);
		loadPhotosFromOriginalLocationsRadioButton.setText("&Load Photos and file:/// URLs from Original Locations");
		
		loadPhotosFromSubstituteFolderRadioButton = new Button(substituteFolderComposite, SWT.RADIO);
		loadPhotosFromSubstituteFolderRadioButton.setText("Loa&d Photos and file:/// URLs from Substitute Folder");
		
		loadPhotosFromOriginalLocationsRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableOKButton();
			}
		});
		
		loadPhotosFromSubstituteFolderRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableOKButton();
			}
		});
		
		final boolean loadFromOriginal = preferenceStore.getBoolean(PreferenceKeys.LoadPhotosFromOriginalLocations);
		
		loadPhotosFromOriginalLocationsRadioButton.setSelection(loadFromOriginal);
		loadPhotosFromSubstituteFolderRadioButton.setSelection(!loadFromOriginal);
		
		substituteFolderLabel = new Label(substituteFolderComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		substituteFolderLabel.setLayoutData(gridData);
		
		// Spacer.
		new Label(substituteFolderComposite, SWT.NONE).setText(StringLiterals.EmptyString);
		
		final Button specifyFolderButton = new Button(substituteFolderComposite, SWT.NONE);
		specifyFolderButton.setText("Specify &Folder...");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT;
		specifyFolderButton.setLayoutData(gridData);
		
		specifyFolderButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setText("Specify Substitute Photos Folder");
				directoryDialog.setMessage("Folder:");
				directoryDialog.setFilterPath(previousSubstitutePhotoFolder);
				
				String substituteFolder = directoryDialog.open();
				
				if (substituteFolder != null) {
					substituteFolderLabel.setText(substituteFolder);
					loadPhotosFromOriginalLocationsRadioButton.setSelection(false);
					loadPhotosFromSubstituteFolderRadioButton.setSelection(true);
					previousSubstitutePhotoFolder = substituteFolder;
					substituteFolderLabel.setText(substituteFolder);
					
					enableDisableOKButton();
				}
			}
		});

		final Composite photosComposite = new Composite(tabFolder, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		photosComposite.setLayout(gridLayout);
		
		final Composite advancedGraphicsComposite = new Composite(photosComposite, SWT.NONE);
		advancedGraphicsComposite.setLayout(new GridLayout(2, false));
		
		advancedGraphics = new Button(advancedGraphicsComposite, SWT.CHECK);
		advancedGraphics.setText("&Use Advanced Graphics");
		advancedGraphics.setSelection(preferenceStore.getBoolean(PreferenceKeys.AdvancedGraphics));
		
		image = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);
		
		imageLabel = new Label(advancedGraphicsComposite, SWT.NONE);
		imageLabel.setImage(image);
		
		imageLabel.setToolTipText("Disable advanced graphics if photos are rendered slowly with low quality.");

		final Composite slideShowFullScreenComposite = new Composite(photosComposite, SWT.NONE);
		slideShowFullScreenComposite.setLayout(new GridLayout(2, false));
		
		slideShowFullScreen = new Button(advancedGraphicsComposite, SWT.CHECK);
		slideShowFullScreen.setText("&Run Slideshows in Full Screen Mode");
		slideShowFullScreen.setSelection(preferenceStore.getBoolean(PreferenceKeys.SlideshowFullScreen));
				
		final Composite exclusionsComposite = new Composite(photosComposite, SWT.NONE);
		exclusionsComposite.setLayout(new GridLayout(3, false));
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		exclusionsComposite.setLayoutData(gridData);
		
		final Label exclusionsLabel = new Label(exclusionsComposite, SWT.NONE);
		exclusionsLabel.setText("In Slideshows and Photo Exports, Excl&ude Items Containing:");
		
		photoExclusionText = new Text(exclusionsComposite, SWT.BORDER);
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		photoExclusionText.setLayoutData(gridData);
		
		// Spacer.
		new Label(photosComposite, SWT.NONE).setText(StringLiterals.EmptyString);

		Button specifyPhotoEditingProgramButton = new Button(photosComposite, SWT.NONE);
		specifyPhotoEditingProgramButton.setText("Specify Photo Editing &Program...");
				
		photoEditingProgramText = new Text(photosComposite, SWT.BORDER);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		photoEditingProgramText.setLayoutData(gridData);
		
		specifyPhotoEditingProgramButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setFilterNames(new String[] { "Executable Files", "All Files" });
				fileDialog.setFilterExtensions(new String[] { StringLiterals.ExecutableFileType, StringLiterals.Wildcard });
				fileDialog.setText("Specify Photo Editing Program");
				
				boolean finished = false;
				
				do {
					final String filePath = fileDialog.open();
					
					if (filePath != null && new File(filePath).exists()) {
						photoEditingProgramText.setText(filePath);
						
						finished = true;
					}
					else if (filePath == null) { 
						finished = true;
					}
				} while (!finished);
			}
		});

		imageLabel = new Label(exclusionsComposite, SWT.NONE);
		imageLabel.setImage(image);
		
		imageLabel.setToolTipText(StringLiterals.SearchTextToolTip);

        // Spacer.
        new Label(photosComposite, SWT.NONE).setText(StringLiterals.EmptyString);

        final Composite cpuCoresComposite = new Composite(photosComposite, SWT.NONE);
        gridLayout = new GridLayout(4, false);
        cpuCoresComposite.setLayout(gridLayout);

        new Label(cpuCoresComposite, SWT.NONE).setText("&CPU Cores for Photo Exports:");

        final int cpuCores = Runtime.getRuntime().availableProcessors();

        final Scale cpuCoresScale = new Scale(cpuCoresComposite, SWT.HORIZONTAL);
        cpuCoresScale.setMinimum(1);
        cpuCoresScale.setMaximum(cpuCores * 4);

        cpuCoresScale.setIncrement(1);
        cpuCoresScale.setPageIncrement(4);

        cpuCoresForPhotoExports = preferenceStore.getInt(PreferenceKeys.CPUCoresForPhotoExports);
        cpuCoresScale.setSelection(cpuCoresForPhotoExports);

        final Label cpuCoresLabel = new Label(cpuCoresComposite, SWT.NONE);

        final String cpuCoresFormat = "{0}";
        final String cpuCoresText = MessageFormat.format(cpuCoresFormat, cpuCoresScale.getSelection());
        cpuCoresLabel.setText(cpuCoresText);

        cpuCoresForPhotoExports = cpuCoresScale.getSelection();

        final Image cpuCoresImage = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);

        final Label cpuCoresTooltipLabel = new Label(cpuCoresComposite, SWT.NONE);
        cpuCoresTooltipLabel.setImage(cpuCoresImage);

        final String cpuCoresTooltipText = String.format("This computer has %d cores", cpuCores);
        cpuCoresTooltipLabel.setToolTipText(cpuCoresTooltipText);

        // Allocate room for 5 digits (plus some extra space to account for different width of digits).
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = GraphicsUtils.getTextExtent(cpuCoresText).x + GraphicsUtils.getTextExtent("0").x * 5;
        gridData.horizontalAlignment = SWT.LEFT;
        cpuCoresLabel.setLayoutData(gridData);

        cpuCoresScale.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                final String text = MessageFormat.format(cpuCoresFormat, cpuCoresScale.getSelection());
                cpuCoresLabel.setText(text);
                cpuCoresForPhotoExports = cpuCoresScale.getSelection();
            }
        });

        final Composite includeTextComposite = new Composite(photosComposite, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.horizontalSpacing = 15;
        includeTextComposite.setLayout(gridLayout);

        includeTextInPhotoExports = new Button(includeTextComposite, SWT.CHECK);
        includeTextInPhotoExports.setText("I&nclude outline text in exported photos");

        includeTextInPhotoExports.setSelection(
                preferenceStore.getBoolean(PreferenceKeys.IncludeOutlineTextInExportedPhotos));

        new Label(includeTextComposite, SWT.NONE).setText("&Text Size:");

        includeTextSize = new Spinner(includeTextComposite, SWT.BORDER);
        includeTextSize.setMinimum(8);
        includeTextSize.setMaximum(72);
        includeTextSize.setSelection(Globals.getPreferenceStore().getInt(PreferenceKeys.IncludeOutlineTextSize));
        includeTextSize.setIncrement(1);

        final Composite updatesComposite = new Composite(tabFolder, SWT.NONE);
		updatesComposite.setLayout(new GridLayout(1, false));
		
		checkForUpdatesCheckBox = new Button(updatesComposite, SWT.CHECK);
		checkForUpdatesCheckBox.setText("&Check for updates once a week");
		checkForUpdatesCheckBox.setSelection(preferenceStore.getBoolean(PreferenceKeys.CheckForUpdatesAutomatically));
		
		startupFileTabItem.setControl(startupComposite);
		savingTabItem.setControl(savingComposite);
		syncTabItem.setControl(syncComposite);
		passwordsTabItem.setControl(passwordsComposite);
		instancesTabItem.setControl(instancesComposite);
		fontsAndColorsTabItem.setControl(fontsAndColorsComposite);
		photosTabItem.setControl(photosComposite);
		substituteFolderTabItem.setControl(substituteFolderComposite);
		updatesTabItem.setControl(updatesComposite);
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_SettingsDialog"));
		
		composite.pack();

		return composite;
	}

	private void enableDisableWarnAboutSingleInstance() {
		warnAboutSingleInstance.setEnabled(!allowMultipleInstances.getSelection());
	}
	
	private void updateFontDisplay() {
		final String defaultTextFontLabelText = MessageFormat.format("Default Text Font: {0}", FontUtils.stringToDescription(fontString));
		defaultTextFontLabel.setText(defaultTextFontLabelText);
		
		textForegroundColorCanvas.setBackground(new Color(textForegroundColor));
	}
	
	public SettingsDialog(Shell parentShell) {
		super(parentShell);
		
		preferenceStore = Globals.getPreferenceStore();
		
		textForegroundColor = new RGB(preferenceStore.getInt(PreferenceKeys.DefaultTextFontRed),
                                      preferenceStore.getInt(PreferenceKeys.DefaultTextFontGreen),
                                      preferenceStore.getInt(PreferenceKeys.DefaultTextFontBlue));

		textBackgroundColor = new RGB(preferenceStore.getInt(PreferenceKeys.TextBackgroundRed),
									  preferenceStore.getInt(PreferenceKeys.TextBackgroundGreen),
									  preferenceStore.getInt(PreferenceKeys.TextBackgroundBlue));

		fontString = preferenceStore.getString(PreferenceKeys.DefaultTextFont);
		
		previousSubstitutePhotoFolder = preferenceStore.getString(PreferenceKeys.SubstitutePhotoFolder);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
