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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.text.MessageFormat;

public class DeletePhotoFileDialog extends VaultDialog {
	private final String photoFilePath;
	private Button alsoDeleteOutlineItemButton;
	private boolean alsoDeleteOutlineItem;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite twoColumnComposite = (Composite) super.createDialogArea(parent);
		twoColumnComposite.setLayout(new GridLayout(2, false));

		new Label(twoColumnComposite, SWT.NONE).setImage(Display.getDefault().getSystemImage(SWT.ICON_QUESTION));
		new Label(twoColumnComposite, SWT.NONE).setText(MessageFormat.format("Delete {0}?", photoFilePath));

		Composite oneColumnComposite = new Composite(parent, SWT.NONE);
		oneColumnComposite.setLayout(new GridLayout(1, false));

		alsoDeleteOutlineItemButton = new Button(oneColumnComposite, SWT.CHECK);
		alsoDeleteOutlineItemButton.setText("&Also delete outline item");
		alsoDeleteOutlineItemButton.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.AlsoDeleteOutlineItem));

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_DeletePhotoFileDialog"));

		parent.pack();

		return parent;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);

		alsoDeleteOutlineItem = alsoDeleteOutlineItemButton.getSelection();

		close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button yesButton = createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
		createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);

		yesButton.forceFocus();

		yesButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PhotoProcessing.deletePictureFile(Globals.getMainApplicationWindow().getShell());

				Globals.getPreferenceStore().setValue(PreferenceKeys.AlsoDeleteOutlineItem, alsoDeleteOutlineItem);

				if (alsoDeleteOutlineItem) {
					Globals.getVaultTreeViewer().removeSelectedItems(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public DeletePhotoFileDialog(Shell parentShell, String photoFilePath) {
		super(parentShell);

		this.photoFilePath = photoFilePath;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText("Delete Photo File");
	}

}
