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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class PasteAboveOrBelowDialog extends VaultDialog {
	private boolean cancelled = true;
	
	public boolean isCancelled() {
		return cancelled;
	}

	private boolean addAbove;
	
	public boolean getAddAbove() {
		return addAbove;
	}

	public PasteAboveOrBelowDialog(Shell parentShell) {
		super(parentShell);
	}

	private Button aboveRadioButton;
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));

		final Group group = new Group(composite, SWT.NONE);
		group.setText("Place Items:");
		group.setLayout(new RowLayout(SWT.VERTICAL));
		
		aboveRadioButton = new Button(group, SWT.RADIO);
		aboveRadioButton.setText("&Above first item");

		final Button belowRadioButton = new Button(group, SWT.RADIO);
		belowRadioButton.setText("&Below first item");
		belowRadioButton.setSelection(true);
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_PasteAboveOrBelowDialog"));
		
		composite.pack();
		
		return composite;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Paste");
	}

	@Override
	protected void okPressed() {
		addAbove = aboveRadioButton.getSelection();
		cancelled = false;

		super.okPressed();
	}
}
