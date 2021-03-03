/*
  Vault 3
  (C) Copyright 2009, Eric Bergman-Terrell
  
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
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
public class SendEmailFeedbackDialog extends VaultDialog {
	@Override
	protected void populateFields() {
		fromText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.EmailFromAddress));
		subjectText.setText(MessageFormat.format("{0} feedback", StringLiterals.ProgramName));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private Label statusLabel;
	
	private Color nonErrorBackground, errorBackground;

	private Button sendButton;
	
	private Text fromText, subjectText, bodyText;

	protected SendEmailFeedbackDialog(Shell parentShell) {
		super(parentShell);
	}

    @Override
    protected void okPressed() 
    {
		String fromAddress = fromText.getText();
		String subject = subjectText.getText();
    	
    	boolean successful = false;
    	
    	try {
    		getShell().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));

			Email.send(Globals.getPreferenceStore().getString(PreferenceKeys.EmailServerAddress),
					   Globals.getPreferenceStore().getBoolean(PreferenceKeys.EmailAuthentication),
					   Globals.getPreferenceStore().getString(PreferenceKeys.EmailUserName),
					   Globals.getPreferenceStore().getString(PreferenceKeys.EmailPassword),
					   "Vault3@EricBT.com", fromAddress, subject, bodyText.getText());
	    	successful = true;
    	}
    	catch (Throwable ex) {
			String message = MessageFormat.format("Cannot send email: {0}.", ex.getMessage());
			
			Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
			
			MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
			messageDialog.open();
    	}
    	finally {
    		getShell().setCursor(null);
    	}
    	
    	if (successful) {
    		super.okPressed();
    	}
    }
    
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		sendButton = createButton(parent, IDialogConstants.OK_ID, "&Send", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
		
		sendButton.setEnabled(false);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		
	    statusLabel = new Label(parent, SWT.BORDER);
	
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		statusLabel.setLayoutData(gridData);

		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		enableDisableSendButton();
		
		return result;
	}

	private void enableDisableSendButton() {
		if (sendButton != null) {
			boolean enabled = true;
			
			String errorMessage = "";
			
			if (fromText == null || fromText.getText().trim().length() == 0) {
				errorMessage = "Please specify From email address.";
				enabled = false;
			}
			else if (bodyText == null || bodyText.getText().trim().length() == 0) {
				errorMessage = "Please enter Body.";
				enabled = false;
			}
	
			sendButton.setEnabled(enabled);
		          
		    if (!enabled) {
		    	statusLabel.setText(errorMessage);
		        statusLabel.setBackground(errorBackground);
		    }
		    else {
		    	statusLabel.setText("");
		    	statusLabel.setBackground(nonErrorBackground);
		    }
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Send Feedback");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(3, false));

		Label fromLabel = new Label(composite, SWT.NONE);
		fromLabel.setText("&From:");

		fromText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fromText.setLayoutData(gridData);
		
		fromText.addModifyListener(e -> enableDisableSendButton());

		fromText.addFocusListener(new TextFocusListener());
		
		Image image = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);
		Label imageLabel = new Label(composite, SWT.NONE);
		imageLabel.setImage(image);
		imageLabel.setToolTipText("Enter your email address");
		
		Label subjectLabel = new Label(composite, SWT.NONE);
		subjectLabel.setText("S&ubject:");
		
		subjectText = new Text(composite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		subjectText.setLayoutData(gridData);
		subjectText.addFocusListener(new TextFocusListener());
		
		Label separatorLabel = new Label(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separatorLabel.setLayoutData(gridData);
		
		Label bodyLabel = new Label(composite, SWT.NONE);
		bodyLabel.setText("&Body:");
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		bodyLabel.setLayoutData(gridData);
		
		bodyText = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		bodyText.setBackground(subjectText.getBackground());
		bodyText.setForeground(subjectText.getForeground());

		bodyText.addModifyListener(e -> enableDisableSendButton());

		separatorLabel = new Label(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separatorLabel.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		bodyText.setLayoutData(gridData);
		
		composite.pack();
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_SendEmailFeedbackDialog"));
		
		return composite;
	}	
}
