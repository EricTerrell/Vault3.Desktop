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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InsertUrlDialog extends VaultDialog {
	@Override
	protected boolean isResizable() {
		return true;
	}

	private Button okButton;
	private Combo protocolCombo;
	private Text urlText;
	private Label statusLabel;
	private Color nonErrorBackground, errorBackground;
	private String url;
	
	public String getUrl() { 
		return url; 
	}

	private void enableDisableOKButton() {
		final boolean enabled = protocolCombo.getSelectionIndex() >= 0 && !urlText.getText().trim().isEmpty();
		
	    if (!enabled) {
	    	setStatusLabelText(statusLabel, "Please enter URL.");
	        statusLabel.setBackground(errorBackground);
	    }
	    else {
	    	setStatusLabelText(statusLabel, StringLiterals.EmptyString);
	    	statusLabel.setBackground(nonErrorBackground);
	    }
		
		okButton.setEnabled(enabled);
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);
		
	    statusLabel = createStatusLabel(parent);
	
		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		enableDisableOKButton();
		
		urlText.setFocus();
		
		return result;
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
		composite.setLayout(new GridLayout(1, true));

		GridData gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		composite.setLayoutData(gridData);

		final Composite urlComposite = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginWidth = 0;
		urlComposite.setLayout(gridLayout);
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 0;
		gridData.horizontalIndent = 0;
		urlComposite.setLayoutData(gridData);
		
		final Label urlLabel = new Label(urlComposite, SWT.NONE);
		urlLabel.setText("&URL:");
		
		protocolCombo = new Combo(urlComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		protocolCombo.add("http://");
		protocolCombo.add("https://");
		protocolCombo.add("file:///");
		
		protocolCombo.select(0);
		
		urlText = new Text(urlComposite, SWT.BORDER);

		urlText.addModifyListener(e -> enableDisableOKButton());
		
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		urlText.setLayoutData(gridData);

		final Button browseButton = new Button(urlComposite, SWT.PUSH);
		browseButton.setText("&Browse");
		browseButton.setEnabled(false);
		
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setFilterNames(new String[] { "All Files" });
				fileDialog.setFilterExtensions(new String[] { StringLiterals.Wildcard });
				fileDialog.setText("Specify File");
				
				boolean finished = false;
				
				do {
					final String filePath = fileDialog.open();
					
					if (filePath != null && new File(filePath).exists()) {
						urlText.setText(filePath);
						
						finished = true;
					}
					else if (filePath == null) { 
						finished = true;
					}
				} while (!finished);
				
				enableDisableOKButton();
			}
		});
		
		final Composite hintsComposite = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = SWT.FILL;
		gridLayout.verticalSpacing = SWT.FILL;
		
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		hintsComposite.setLayoutData(gridData);
		
		hintsComposite.setLayout(gridLayout);
		final Text hintsText = new Text(hintsComposite, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		hintsText.setText("Hints:\r\n\r\nInsert an http:// or https:// URL to browse to a website using the default web browser.\r\n\r\nInsert a file:/// URL to load a file into the default application for that file. For example, a file URL of \"file:///c:\\documents\\memo.doc\" will load the memo.doc file into Word.\r\n\r\nOnce you've inserted a URL, when you right-click the URL and select Browse URL, the web page or file will be displayed.\r\n\r\nYou can also browse URLs by selecting Edit / Browse URLs (Ctrl+U).");
		
		final GC gc = new GC(hintsText.getDisplay());
		
		final int height = gc.getFontMetrics().getHeight() * 7;
		final int width = height * 4;
		
		gc.dispose();
		
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = height;
		gridData.minimumHeight = height;
		gridData.widthHint = width;
		gridData.minimumWidth = width;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		hintsText.setLayoutData(gridData);
		
		protocolCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButton.setEnabled(protocolCombo.getSelectionIndex() == 2);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_InsertUrlDialog"));
		
		composite.pack();
		
		return composite;
	}

	protected InsertUrlDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText("Insert URL");
	}

	@Override
	protected void okPressed() {
		url = urlText.getText();
		
		try {
			url = URLEncoder.encode(url, GoToWebsites.encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		url = String.format("%s%s", protocolCombo.getItem(protocolCombo.getSelectionIndex()), url);
		
		super.okPressed();
	}

}
