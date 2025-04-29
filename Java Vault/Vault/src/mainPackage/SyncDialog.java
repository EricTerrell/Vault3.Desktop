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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SyncDialog extends VaultDialog {
	private static final int OPEN_ID = 1000;
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));
		
		final String text = Globals.getVaultDocument().getIsModified() ?
								"The current document was updated after you opened it. Open updated document and discard your changes?" 
							: 
								"The current document was updated after you opened it. Open updated document?";
		
		new Label(composite, SWT.NONE).setText(text);

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_SyncDialog"));
		
		composite.pack();
		
		return composite;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch(buttonId) {
		case OPEN_ID: {
			final String filePath = Globals.getVaultDocument().getFilePath();
			
			try {
				VaultDocumentIO.fileOpen(getShell(), filePath);
				Globals.getMainApplicationWindow().getSearchUI().reset();
				Globals.getVaultTreeViewer().selectFirstItem();
			}
			catch (Throwable ex) {
				final boolean processedException = DatabaseVersionTooHigh.displayMessaging(ex, filePath);

				if (!processedException) {
					String message = MessageFormat.format("Cannot open file {2}.{0}{0}{1}", PortabilityUtils.getNewLine(),  ex.getMessage(), filePath);
					MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON), message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
					messageDialog.open();
				}

				ex.printStackTrace();
			}
		}
		break;
		}
		
		setReturnCode(buttonId);
		close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, OPEN_ID, "&Open Updated Document", false);
		final Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true);

		cancelButton.forceFocus();
	}

	public SyncDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText("Sync");
	}

}
