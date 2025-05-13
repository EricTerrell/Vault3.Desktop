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
import org.eclipse.swt.widgets.Text;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class PasswordPromptDialog extends VaultDialog {
	@Override
	protected void populateFields() {
		promptLabel.setText(MessageFormat.format("A password is required to access {0}.", fileName));
	}

	private Text passwordText;
	
	private String password;
	
	private Label statusLabel, promptLabel;

	private Color nonErrorBackground, errorBackground;

	private final String fileName;
	
	private Button hidePasswordCharsCheckBox, okButton, forceUpperCasePasswords;
	
	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);

		statusLabel = createStatusLabel(parent);

		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		updateStatusLabel();
		
		return result;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
		
		okButton.setEnabled(false);
	}

    @Override
	public boolean close() {
    	Globals.getPreferenceStore().setValue(PreferenceKeys.HidePasswordCharacters, hidePasswordCharsCheckBox.getSelection());
    	Globals.getPreferenceStore().setValue(PreferenceKeys.ForceUpperCasePasswords, forceUpperCasePasswords.getSelection());

		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(MessageFormat.format("{0} - Password Required", StringLiterals.ProgramName));
	}

	@Override
	protected void okPressed() {
		password = passwordText.getText().trim();
		
		if (forceUpperCasePasswords.getSelection()) {
			password = password.toUpperCase();
		}
		
		super.okPressed();
	}

	private void updateStatusLabel() {
		final Button okButton = getButton(IDialogConstants.OK_ID);
		
		boolean incorrectLength = passwordText.getText().length() < CryptoUtils.getMinPasswordLength();
		
		if (incorrectLength) {
			final String message = MessageFormat.format("Password must contain at least {0} characters.", CryptoUtils.getMinPasswordLength());
			setStatusLabelText(statusLabel, message);
			statusLabel.setBackground(errorBackground);
			okButton.setEnabled(false);
		}
		else {
      	  setStatusLabelText(statusLabel, StringLiterals.EmptyString);
      	  statusLabel.setBackground(nonErrorBackground);
      	  okButton.setEnabled(true);
		}
	}
	
	public String getPassword() {
		return password;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		
		promptLabel = new Label(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL);
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		promptLabel.setLayoutData(gridData);
		
		Label spacerLabel = new Label(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		spacerLabel.setLayoutData(gridData);

		final Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		
		passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		passwordText.setLayoutData(gridData);
		
		passwordText.addVerifyListener(e -> {
            e.text = e.text.trim();

            if (forceUpperCasePasswords.getSelection()) {
                e.text = e.text.toUpperCase();
            }
        });
	
		passwordText.addModifyListener(e -> updateStatusLabel());
		
		passwordText.addModifyListener(e -> {
            boolean enabled = passwordText.getText().length() >= CryptoUtils.getMinPasswordLength();

            okButton.setEnabled(enabled);
        });
		
		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);
		
		hidePasswordCharsCheckBox = new Button(composite, SWT.CHECK);
		hidePasswordCharsCheckBox.setText("&Hide password characters");
		gridData = new GridData(SWT.FILL);
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		hidePasswordCharsCheckBox.setLayoutData(gridData);
		
		hidePasswordCharsCheckBox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.HidePasswordCharacters));

		final char echoChar = passwordText.getEchoChar();
		
		hidePasswordCharsCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				showHidePasswordChars(echoChar);
			}
		});
	
		showHidePasswordChars(echoChar);
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_PasswordPromptDialog"));

		forceUpperCasePasswords = new Button(composite, SWT.CHECK);
		forceUpperCasePasswords.setText("&Force password to be uppercase");
		gridData = new GridData(SWT.FILL);
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		forceUpperCasePasswords.setLayoutData(gridData);
		
		forceUpperCasePasswords.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.ForceUpperCasePasswords));
		
		forceUpperCasePasswords.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				passwordText.setText(passwordText.getText().trim().toUpperCase());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		composite.pack();
		
		return composite;
	}
	
	private void showHidePasswordChars(char echoChar) {
		if (hidePasswordCharsCheckBox.getSelection()) {
			passwordText.setEchoChar(echoChar);
		}
		else {
			passwordText.setEchoChar('\0');
		}
	}
	
	/**
	 * @param parentShell
	 */
	public PasswordPromptDialog(Shell parentShell, String fileName) {
		super(parentShell);
		
		this.fileName = fileName;
		
		// Ensure that dialog is visible (on top of all other windows).
		setShellStyle(getShellStyle() | SWT.ON_TOP);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}




