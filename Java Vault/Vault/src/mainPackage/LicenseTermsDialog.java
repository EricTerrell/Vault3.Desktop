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

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class LicenseTermsDialog extends VaultDialog {
	@Override
	protected boolean isResizable() {
		return true;
	}

	private Button acceptTermsRadioButton;
	
	@Override
	public boolean close() {
		final boolean acceptedTerms = acceptTermsRadioButton.getSelection();
		
    	Globals.getPreferenceStore().setValue(PreferenceKeys.AcceptLicenseTerms, acceptedTerms);
    	
    	boolean result = super.close();

    	if (!acceptedTerms) {
			final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
			
			final String message = MessageFormat.format("You have rejected the licensing terms for {0}.{1}{1}Please uninstall {0} and stop using it immediately.", StringLiterals.ProgramName, PortabilityUtils.getNewLine());
			
			MessageDialog messageDialog = new MessageDialog(getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
			messageDialog.open();
    	}
    	
		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));
		
		final Text editableText = new Text(composite, SWT.NONE);
		final Color backColor = editableText.getBackground();
		final Color foreColor = editableText.getForeground();
		editableText.setVisible(false);

		final Label licenseLabel = new Label(composite, SWT.NONE);
		licenseLabel.setText("License &Terms:");
		
		final Text licenseText = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		licenseText.setBackground(backColor);
		licenseText.setForeground(foreColor);
		
		final GC gc = new GC(licenseText.getDisplay());
		
		final int minLines = 10;
		final int height = minLines * gc.getFontMetrics().getHeight();
		
		gc.dispose();
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = height;
		gridData.minimumHeight = height;
		gridData.verticalAlignment = SWT.FILL;
		licenseText.setLayoutData(gridData);
		
		String licenseTermsText;
		try {
			licenseTermsText = FileUtils.readFile(MainApplicationWindow.class.getResourceAsStream("/resources/LicenseTerms.txt"));
			licenseText.setText(licenseTermsText);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final Group decisionGroup = new Group(composite, SWT.NONE);
		decisionGroup.setLayout(new GridLayout(1, true));
		decisionGroup.setText("&Your Decision:");
		
		Button rejectTermsRadioButton = new Button(decisionGroup, SWT.RADIO);
		rejectTermsRadioButton.setText("I &reject these terms");
		
		acceptTermsRadioButton = new Button(decisionGroup, SWT.RADIO);
		acceptTermsRadioButton.setText("I &accept these terms");

    	boolean accepted = Globals.getPreferenceStore().getBoolean(PreferenceKeys.AcceptLicenseTerms);
    	
    	rejectTermsRadioButton.setSelection(!accepted);
    	acceptTermsRadioButton.setSelection(accepted);
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_LicenseTermsDialog"));
    	
    	composite.pack();
    	
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			close();
		}
	}

	public LicenseTermsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(MessageFormat.format("{0} License Terms", StringLiterals.ProgramName));
	}
}
