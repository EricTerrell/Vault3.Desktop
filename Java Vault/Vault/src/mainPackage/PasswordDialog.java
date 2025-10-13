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
public class PasswordDialog extends VaultDialog {
	@Override
	protected void populateFields() {
		final String text = MessageFormat.format("&Require a password to access {0}",
				Globals.getVaultDocument().getFileName());
		requirePasswordCheckBox.setText(text);

		passwordText.setText(password);
		password2Text.setText(password);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private String password;
	
	public String getPassword() {
		return password;
	}
	
	private Label statusLabel;

	private Button hidePasswordCharsCheckBox, forceUpperCasePasswords;

	private Label passwordLabel, password2Label;
	
	private Text passwordText, password2Text;

	private Color nonErrorBackground, errorBackground;
	
	private char echoChar;
	
	Button requirePasswordCheckBox;
	
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
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}

	@Override
	public boolean close() {
		Globals.getPreferenceStore().setValue(PreferenceKeys.HidePasswordCharacters, hidePasswordCharsCheckBox.getSelection());
		Globals.getPreferenceStore().setValue(PreferenceKeys.ForceUpperCasePasswords, forceUpperCasePasswords.getSelection());

		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		
		requirePasswordCheckBox = new Button(composite, SWT.CHECK);
		requirePasswordCheckBox.setSelection(Globals.getVaultDocument().isEncrypted());
		
		Label spacerLabel = new Label(composite, SWT.NONE);
		spacerLabel.setText(StringLiterals.EmptyString);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		spacerLabel.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		requirePasswordCheckBox.setLayoutData(gridData);

		passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		
		passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		passwordText.setLayoutData(gridData);
		passwordText.addFocusListener(new TextFocusListener());
		
		passwordText.addVerifyListener(e -> {
            e.text = e.text.trim();

            if (forceUpperCasePasswords.getSelection()) {
                e.text = e.text.toUpperCase();
            }
        });
		
		echoChar = passwordText.getEchoChar();
		
		password2Label = new Label(composite, SWT.PASSWORD);
		password2Label.setText("&Enter password again:");
		
		password2Text = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		password2Text.setLayoutData(gridData);
		password2Text.addFocusListener(new TextFocusListener());
		
		password2Text.addVerifyListener(e -> {
            e.text = e.text.trim();

            if (forceUpperCasePasswords.getSelection()) {
                e.text = e.text.toUpperCase();
            }
        });
		
		spacerLabel = new Label(composite, SWT.NONE);
		spacerLabel.setText(StringLiterals.EmptyString);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		spacerLabel.setLayoutData(gridData);

		hidePasswordCharsCheckBox = new Button(composite, SWT.CHECK);
		hidePasswordCharsCheckBox.setText("&Hide password characters");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		hidePasswordCharsCheckBox.setLayoutData(gridData);
		hidePasswordCharsCheckBox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.HidePasswordCharacters));
		
		hidePasswordCharsCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				showHidePasswordCharacters();
			}
		});
		
		showHidePasswordCharacters();

		forceUpperCasePasswords = new Button(composite, SWT.CHECK);
		forceUpperCasePasswords.setText("&Force password to be uppercase");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		forceUpperCasePasswords.setLayoutData(gridData);
		
		forceUpperCasePasswords.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.ForceUpperCasePasswords));
		
		forceUpperCasePasswords.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = passwordText.getText().trim();
				
				if (forceUpperCasePasswords.getSelection()) {
					text = text.toUpperCase();
				}
				
				passwordText.setText(text);
				
				text = password2Text.getText().trim();
				
				if (forceUpperCasePasswords.getSelection()) {
					text = text.toUpperCase();
				}
				
				password2Text.setText(text);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		requirePasswordCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableDisablePasswordControls();
				updateStatusLabel();
			}
		});
		
		enableDisablePasswordControls();

		passwordText.addModifyListener(e -> updateStatusLabel());

		password2Text.addModifyListener(e -> updateStatusLabel());
		
		passwordText.setFocus();
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_PasswordDialog"));
		
		composite.pack();
		
		return composite;
	}

	private void showHidePasswordCharacters() {
		if (hidePasswordCharsCheckBox.getSelection()) {
			passwordText.setEchoChar(echoChar);
			password2Text.setEchoChar(echoChar);
		}
		else {
			passwordText.setEchoChar('\0');
			password2Text.setEchoChar('\0');
		}
	}

	private void enableDisablePasswordControls() {
		boolean enabled = requirePasswordCheckBox.getSelection();
		
		passwordLabel.setEnabled(enabled);
		passwordText.setEnabled(enabled);

		password2Label.setEnabled(enabled);
		password2Text.setEnabled(enabled);
		
		hidePasswordCharsCheckBox.setEnabled(enabled);
		forceUpperCasePasswords.setEnabled(enabled);
	}
	
	private void updateStatusLabel() {
		final Button okButton = getButton(IDialogConstants.OK_ID);

		if (!requirePasswordCheckBox.getSelection()) {
			// Password is not required

			setStatusLabelText(statusLabel, StringLiterals.EmptyString);
			statusLabel.setBackground(nonErrorBackground);
			okButton.setEnabled(true);
		} else {
			// Password is required

			final String[] passwords = new String[]{passwordText.getText(), password2Text.getText()};

			boolean incorrectLength = false;

			for (String password : passwords) {
				if (password.length() < CryptoUtils.getMinPasswordLength()) {
					incorrectLength = true;
					break;
				}
			}

			if (incorrectLength) {
				final String message = MessageFormat.format("Password must contain at least {0} characters.",
						CryptoUtils.getMinPasswordLength());
				setStatusLabelText(statusLabel, message);
				statusLabel.setBackground(errorBackground);
				okButton.setEnabled(false);
			} else {
				if (!passwords[0].equals(passwords[1])) {
					final String message = "Passwords must match.";
					setStatusLabelText(statusLabel, message);
					statusLabel.setBackground(errorBackground);
					okButton.setEnabled(false);
				} else {
					setStatusLabelText(statusLabel, StringLiterals.EmptyString);
					statusLabel.setBackground(nonErrorBackground);
					okButton.setEnabled(true);
				}
			}
		}
	}
	
	@Override
	protected void okPressed() {
		if (requirePasswordCheckBox.getSelection()) {
			password = passwordText.getText().trim();
			
			if (forceUpperCasePasswords.getSelection()) {
				password = password.toUpperCase();
			}
		}
		else {
			password = null;
		}
			
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Password");
	}

	public PasswordDialog(Shell parentShell) {
		super(parentShell);

		this.password = StringLiterals.EmptyString;
	}
}
