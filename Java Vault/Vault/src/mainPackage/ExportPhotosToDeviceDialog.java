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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class ExportPhotosToDeviceDialog extends VaultDialog {
	@Override
	protected boolean isResizable() {
		return true;
	}

	private static String previousDestinationFolder;
	
	private Button totalPhotosCheckBox, deleteFolderContentsCheckBox, shuffleCheckBox;
	
	private Text totalPhotosText, widthText, heightText, photosPerFolderText;

	private Label statusLabel, destinationFolderLabel;
	
	private Color nonErrorBackground, errorBackground;
	
	private Button okButton;

	@Override
	protected void okPressed() {
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosDeleteFolderContents, deleteFolderContentsCheckBox.getSelection());
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosDestFolder, destinationFolderLabel.getText());
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosHeight, heightText.getText());
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosWidth, widthText.getText());
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosPhotosPerFolder, photosPerFolderText.getText());
		
		String totalPhotos = totalPhotosCheckBox.getSelection() ? totalPhotosText.getText() : StringLiterals.EmptyString;
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosTotalPhotos, totalPhotos);
		
		Globals.getPreferenceStore().setValue(PreferenceKeys.ExportPhotosShuffle, shuffleCheckBox.getSelection());
		
		super.okPressed();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		
	    statusLabel = createStatusLabel(parent);
	
		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		return result;
	}

	private boolean validInteger(String text) {
		boolean valid = false;
		
		int value = 0;
		
		try {
			value = Integer.parseInt(text);
			valid = value > 0;
		}
		catch (NumberFormatException ignored) {
		}
		
		return valid;
	}
	
	private void enableDisableOKButton() {
		String errorMessage = null;
		
		if (destinationFolderLabel.getText().isEmpty()) {
			errorMessage = "Please specify destination folder";
		}
		else if (!validInteger(widthText.getText())) {
			errorMessage = "Invalid Width";
		}
		else if (!validInteger(heightText.getText())) {
			errorMessage = "Invalid Height";
		}
		else if (totalPhotosCheckBox.getSelection() && !validInteger(totalPhotosText.getText())) {
			errorMessage = "Invalid Total Photos";
		}
		else if (!validInteger(photosPerFolderText.getText())) {
			errorMessage = "Invalid Photos per Folder";
		}
		
		if (errorMessage != null) {
			statusLabel.setBackground(errorBackground);
			setStatusLabelText(statusLabel, errorMessage);
			okButton.setEnabled(false);
		}
		else {
			statusLabel.setBackground(nonErrorBackground);
			setStatusLabelText(statusLabel, StringLiterals.EmptyString);
			okButton.setEnabled(true);
		}
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Export Photos to Device");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
		
		okButton.setEnabled(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(4, false));

		final Composite destinationFolderComposite = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 0;
		destinationFolderComposite.setLayout(gridLayout);
		
		GridData gridData = new GridData(SWT.FILL);
		gridData.horizontalSpan = 4;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		destinationFolderComposite.setLayoutData(gridData);
		
		final Button destinationFolderBrowseButton = new Button(destinationFolderComposite, SWT.PUSH);
		destinationFolderBrowseButton.setText("&Destination Folder...");

		// Spacer
		new Label(destinationFolderComposite, SWT.NONE);
		
		destinationFolderLabel = new Label(destinationFolderComposite, SWT.NONE);

		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		destinationFolderLabel.setLayoutData(gridData);
		
		destinationFolderBrowseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setText("Specify Destination Folder");
				directoryDialog.setMessage("Folder:");
				directoryDialog.setFilterPath(previousDestinationFolder);
				
				final String destinationFolder = directoryDialog.open();
				
				if (destinationFolder != null) {
					destinationFolderLabel.setText(destinationFolder);
					previousDestinationFolder = destinationFolder;
					
					enableDisableOKButton();
				}
			}
		});
		
		final Composite deleteFolderContentsComposite = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		deleteFolderContentsComposite.setLayout(gridLayout);
		
		gridData = new GridData(SWT.FILL);
		gridData.horizontalSpan = 4;
		deleteFolderContentsComposite.setLayoutData(gridData);
		
		deleteFolderContentsCheckBox = new Button(deleteFolderContentsComposite, SWT.CHECK);
		deleteFolderContentsCheckBox.setText("De&lete Folder Contents");
		
		deleteFolderContentsCheckBox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.ExportPhotosDeleteFolderContents));
		
		final Label widthLabel = new Label(composite, SWT.NONE);
		widthLabel.setText("&Width:");
		
		widthText = new Text(composite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		widthText.setLayoutData(gridData);
		
		widthText.addFocusListener(new TextFocusListener());
		
		widthText.addModifyListener(e -> enableDisableOKButton());
		
		final Label heightLabel = new Label(composite, SWT.NONE);
		heightLabel.setText("&Height:");
		
		heightText = new Text(composite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		heightText.setLayoutData(gridData);

		heightText.addFocusListener(new TextFocusListener());
		
		heightText.addModifyListener(e -> enableDisableOKButton());
		
		totalPhotosCheckBox = new Button(composite, SWT.CHECK);
		totalPhotosCheckBox.setText("&Total Photos:");
		
		totalPhotosCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableOKButton();
			}
		});
		
		totalPhotosText = new Text(composite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		totalPhotosText.setLayoutData(gridData);
		
		totalPhotosText.addFocusListener(new TextFocusListener());
		
		totalPhotosText.addModifyListener(e -> enableDisableOKButton());
		
		totalPhotosCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				totalPhotosText.setEnabled(totalPhotosCheckBox.getSelection());
			}
		});
		
		final Label photosPerFolderLabel = new Label(composite, SWT.NONE);
		photosPerFolderLabel.setText("&Photos per Folder:");
		
		photosPerFolderText = new Text(composite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		photosPerFolderText.setLayoutData(gridData);

		photosPerFolderText.addFocusListener(new TextFocusListener());
		
		photosPerFolderText.addModifyListener(e -> enableDisableOKButton());
		
		final Composite shuffleComposite = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		shuffleComposite.setLayout(gridLayout);
		
		gridData = new GridData(SWT.FILL);
		gridData.horizontalSpan = 4;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		shuffleComposite.setLayoutData(gridData);
		
		shuffleCheckBox = new Button(shuffleComposite, SWT.CHECK);
		shuffleCheckBox.setText("&Shuffle");
		
		shuffleCheckBox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.ExportPhotosShuffle));

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_ExportPhotosToDeviceDialog"));
		
		composite.pack();

		return composite;
	}

	@Override
	protected void populateFields() {
		destinationFolderLabel.setText(Globals.getPreferenceStore().getString(PreferenceKeys.ExportPhotosDestFolder));

		widthText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.ExportPhotosWidth));
		heightText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.ExportPhotosHeight));

		totalPhotosText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.ExportPhotosTotalPhotos));
		
		photosPerFolderText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.ExportPhotosPhotosPerFolder));

		totalPhotosCheckBox.setSelection(validInteger(totalPhotosText.getText()));

		totalPhotosText.setEnabled(totalPhotosCheckBox.getSelection());
		
		enableDisableOKButton();
	}

	/**
	 * @param parent
	 */
	public ExportPhotosToDeviceDialog(Shell parent) {
		super(parent);
	}
}
