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

import java.util.Dictionary;
import java.util.Enumeration;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class ImportFolderDialog extends VaultDialog implements ISelectionChangedListener {
	@Override
	protected void populateFields() {
		fileTypesViewer.setComparator(new ViewerComparator());
		
		final Enumeration<String> keysEnum = uniqueFileTypes.keys();
		
		final IDialogSettings fileTypeSettings = getDialogSettings().getSection(fileTypesSection);

		while (keysEnum.hasMoreElements()) {
			final String fileType = keysEnum.nextElement();
			
			fileTypesViewer.add(fileType);
			
			if (fileTypeSettings != null) {
				final boolean fileTypeSelected = fileTypeSettings.getBoolean(fileType);
				
				if (fileTypeSelected) {
					fileTypesViewer.setChecked(fileType, true);
				}
			}
		}
		
		enableDisableFileTypesViewer();
	}

	private static final String fileTypesSection = "FileTypes";
	
	private Button importAllCheckBox, okButton, unselectAllFileTypesButton;

	private Label statusLabel;

	private Color nonErrorBackground, errorBackground;
	
	private CheckboxTableViewer fileTypesViewer;
	
	private final Dictionary<String, Boolean> uniqueFileTypes;

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}

	@Override
	public boolean close() {
		Globals.getPreferenceStore().setValue(PreferenceKeys.ImportAllFileTypes, importAllCheckBox.getSelection());
		
		// Update uniqueFileTypes dictionary with user selection(s).
		if (!importAllCheckBox.getSelection()) {
			TableItem[] tableItems = fileTypesViewer.getTable().getItems();
			
			try
			{
				Globals.setBusyCursor();
				
				getDialogSettings().addNewSection(fileTypesSection);

				for (TableItem tableItem : tableItems) {
					boolean checked = tableItem.getChecked();
					String fileType = (String) tableItem.getData();
					
					uniqueFileTypes.put(fileType, checked ? Boolean.TRUE : Boolean.FALSE);
					
					IDialogSettings fileTypesSettings = getDialogSettings().getSection(fileTypesSection);
					
					fileTypesSettings.put(fileType, checked);
				}
			}
			finally {
				Globals.setPreviousCursor();
			}
		}

		return super.close();
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);
		
	    statusLabel = createStatusLabel(parent);

		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		importAllCheckBox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.ImportAllFileTypes));

		return result;
	}

	private void updateErrorStatus() {
		boolean error = false;
		String errorMessage = null;
		
		if (!importAllCheckBox.getSelection()) {
			if (fileTypesViewer.getCheckedElements().length == 0) {
				error = true;
				errorMessage = "Please select one or more file types";
			}
		}
		
		if (error) {
			setStatusLabelText(statusLabel, errorMessage);
			okButton.setEnabled(false);
			statusLabel.setBackground(errorBackground);
		}
		else {
			setStatusLabelText(statusLabel, StringLiterals.EmptyString);
			okButton.setEnabled(true);
			statusLabel.setBackground(nonErrorBackground);
		}

		unselectAllFileTypesButton.setEnabled(fileTypesViewer.getCheckedElements().length > 0);
	}

	private void enableDisableFileTypesViewer() {
		fileTypesViewer.getControl().setEnabled(!importAllCheckBox.getSelection());
		updateErrorStatus();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		importAllCheckBox = new Button(composite, SWT.CHECK);
		importAllCheckBox.setText("Import &All File Types");
		
		importAllCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisableFileTypesViewer();
			}
		});
		
		// Spacer
		new Label(composite, SWT.NONE);
		
		final Label fileTypesLabel = new Label(composite, SWT.NONE);
		fileTypesLabel.setText("&Select File Types to Import:");

		fileTypesViewer = CheckboxTableViewer.newCheckList(composite, SWT.V_SCROLL);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		fileTypesViewer.getControl().setLayoutData(gridData);
		
		fileTypesViewer.addSelectionChangedListener(this);
		
		unselectAllFileTypesButton = new Button(composite, SWT.PUSH);
		unselectAllFileTypesButton.setEnabled(false);
		unselectAllFileTypesButton.setText("&Un-select all File Types");
		
		unselectAllFileTypesButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] tableItems = fileTypesViewer.getTable().getItems();
				
				for (TableItem tableItem : tableItems) {
					tableItem.setChecked(false);
				}

				enableDisableFileTypesViewer();
			}
		});
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_ImportFolderDialog"));
		
		composite.pack();
		
		return composite;
	}

	/**
	 * @param parent
	 */
	public ImportFolderDialog(Shell parent, Dictionary<String, Boolean> uniqueFileTypes) {
		super(parent);
		
		this.uniqueFileTypes = uniqueFileTypes;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Import Text Files, Photos, and Videos From Disk Folder");
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateErrorStatus();
	}
}
