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
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PrintScopeDialog extends VaultDialog {
	@Override
	protected boolean isResizable() {
		return true;
	}

	private Label statusLabel;

	private Color nonErrorBackground, errorBackground;

	private Button okButton;
	
	private Text pageRangeText;
	
	private int startPage = 1;
	
	public int getStartPage() { return startPage; }
	
	private int endPage = 1;
	
	public int getEndPage () { return endPage; }
	
	private int scope = PrinterData.SELECTION;
	
	public int getScope() { return scope; }
	
	public PrintScopeDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);
		
	    statusLabel = createStatusLabel(parent);
	
		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		enableDisableOKButton();
		
		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));

		final Group pageRangeGroup = new Group(composite, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.makeColumnsEqualWidth = false;
		pageRangeGroup.setLayout(gridLayout);

		pageRangeGroup.setText("&Scope");

		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;

		final Button allRadioButton = new Button(pageRangeGroup, SWT.RADIO);
		allRadioButton.setText("&All");
		allRadioButton.setLayoutData(gridData);

		final Button selectionRadioButton = new Button(pageRangeGroup, SWT.RADIO);
		selectionRadioButton.setText("S&election");
		selectionRadioButton.setLayoutData(gridData);

		final Button pagesRadioButton = new Button(pageRangeGroup, SWT.RADIO);
		pagesRadioButton.setText("&Pages:");
		
		pageRangeText = new Text(pageRangeGroup, SWT.BORDER);
		pageRangeText.setText("1");
		pageRangeText.setEnabled(false);

		final Label pageRangeLabel = new Label(pageRangeGroup, SWT.NONE);
		pageRangeLabel.setText("Enter a single page number or a single page range. For example, 5-12");
		pageRangeLabel.setLayoutData(gridData);

		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		pageRangeText.setLayoutData(gridData);
		
		pageRangeText.addModifyListener(modifyEvent -> validatePageRange());
		
		allRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				PrintScopeDialog.this.pageRangeText.setEnabled(false);
				scope = PrinterData.ALL_PAGES;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});
		
		selectionRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				PrintScopeDialog.this.pageRangeText.setEnabled(false);
				scope = PrinterData.SELECTION;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});
		
		pagesRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				PrintScopeDialog.this.pageRangeText.setEnabled(true);
				scope = PrinterData.PAGE_RANGE;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});
		
		selectionRadioButton.setSelection(true);
		
		composite.pack();

		return composite;
	}
	
	private void validatePageRange() {
		startPage = endPage = -1;

		final String text = pageRangeText.getText();

		final String[] pageNumberStrings = text.split("-");

		final String invalidPageRange = "Invalid page range";
		
		try {
			if (pageNumberStrings.length == 1) {
				startPage = endPage = Integer.parseInt(pageNumberStrings[0]);
				
				if (startPage > 0) {
					setStatusLabelText(statusLabel, StringLiterals.EmptyString);
					statusLabel.setBackground(nonErrorBackground);
				}
				else {
					startPage = endPage = -1;
					
					setStatusLabelText(statusLabel, invalidPageRange);
					statusLabel.setBackground(errorBackground);
				}
			}
			else if (pageNumberStrings.length == 2) {
				startPage = Integer.parseInt(pageNumberStrings[0]);
				endPage = Integer.parseInt(pageNumberStrings[1]);

				if (startPage > 0 && endPage >= startPage) {
					setStatusLabelText(statusLabel, StringLiterals.EmptyString);
					statusLabel.setBackground(nonErrorBackground);
				}
				else {
					startPage = endPage = -1;
					
					setStatusLabelText(statusLabel, invalidPageRange);
					statusLabel.setBackground(errorBackground);
				}
			}
			else {
				setStatusLabelText(statusLabel, invalidPageRange);
				statusLabel.setBackground(errorBackground);
			}
		} catch (NumberFormatException ex) {
			startPage = endPage = -1;
			
			setStatusLabelText(statusLabel, invalidPageRange);
			statusLabel.setBackground(errorBackground);
		}
		
		okButton.setEnabled(statusLabel.getBackground().equals(nonErrorBackground));
	}

	private void enableDisableOKButton() {
		boolean enabled = true;
	          
      	okButton.setEnabled(enabled);
	          
	    if (!enabled) {
	    	setStatusLabelText(statusLabel, "Title must be non-blank.");
	        statusLabel.setBackground(errorBackground);
	    }
	    else {
	    	setStatusLabelText(statusLabel, StringLiterals.EmptyString);
	    	statusLabel.setBackground(nonErrorBackground);
	    }
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Print Scope");
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}
}
