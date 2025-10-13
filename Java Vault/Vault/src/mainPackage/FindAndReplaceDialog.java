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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FindAndReplaceDialog extends VaultDialog {
	@Override
	protected void populateFields() {
		findWhatText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.FindReplaceFindText));

		if (replaceWithText != null) {
			replaceWithText.setText(Globals.getPreferenceStore().getString(PreferenceKeys.FindReplaceReplaceText));
		}
	}

	@Override
	protected String getSettingsFileName() {
		return allowReplace ? "FindAndReplaceDialog.txt" : "FindDialog.txt";
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private final boolean allowReplace;
    private boolean previousFindDone = false;
	
	private Button wholeWordButton;
	private Button matchCaseButton;
	private Button downButton;
	private Button findButton;
	private Button replaceButton;

	private Text replaceWithText;
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(allowReplace ? "Replace" : "Find");
	}

	private final int findNextButtonID   = 1000;
	private final int replaceButtonID    = 1001;
	private final int replaceAllButtonID = 1002;
	
	private Text findWhatText;

	private FindReplaceDocumentAdapter findReplaceDocumentAdapter;
	
	public void UpdateFindReplaceDocumentAdapter() {
		findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(Globals.getVaultTextViewer().getDocument());
		previousFindDone = false;
	}

	private void savePreferences() {
		Globals.getPreferenceStore().setValue(PreferenceKeys.FindReplaceFindText, findWhatText.getText());
		
		if (replaceWithText != null) {
			Globals.getPreferenceStore().setValue(PreferenceKeys.FindReplaceReplaceText, replaceWithText.getText());
		}
		
		Globals.getPreferenceStore().setValue(PreferenceKeys.FindReplaceMatchCase, matchCaseButton.getSelection());
		Globals.getPreferenceStore().setValue(PreferenceKeys.FindReplaceMatchWholeWord, wholeWordButton.getSelection());
	}
	
	private void processFindButtonClick() {
		savePreferences();
		
		IRegion iRegion = null;
		try {
			int offset = Globals.getVaultTextViewer().getTextWidget().getCaretOffset();
			boolean forward = downButton.getSelection();
			
			if (!forward) {
				offset -= findWhatText.getText().length();
			}
			
			iRegion = findReplaceDocumentAdapter.find(offset, findWhatText.getText(), forward, matchCaseButton.getSelection(), wholeWordButton.getSelection(), false);
			
			previousFindDone = true;
		} catch (BadLocationException ignored) {
		}
		
		if (iRegion != null) {
			Globals.getVaultTextViewer().getTextWidget().setFocus();
			Globals.getVaultTextViewer().getTextWidget().setSelection(iRegion.getOffset(), iRegion.getOffset() + iRegion.getLength());
			Globals.getVaultTextViewer().getTextWidget().showSelection();
		}
	}
	
	private boolean processReplaceButtonClick() {
		savePreferences();
		
		boolean replaced = false;
		
		try {
			if (previousFindDone) {
				final IRegion iRegion = findReplaceDocumentAdapter.replace(replaceWithText.getText(), false);
				previousFindDone = true;
				
				replaced = iRegion != null;
			}
		} catch (BadLocationException | IllegalStateException ignored) {
		}

		processFindButtonClick();
		
		return replaced;
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == replaceButtonID) {
			processReplaceButtonClick();
		}
		else if (buttonId == replaceAllButtonID) {
			processReplaceButtonClick();
			
			while (processReplaceButtonClick()) {
			}
		}
		else if (buttonId == findNextButtonID) {
			processFindButtonClick();
		}
		else if (buttonId == IDialogConstants.CLOSE_ID) {
			close();
		}
		else {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		findButton = createButton(parent, findNextButtonID, "&Find Next", !allowReplace);
		findButton.setEnabled(false);
		
		if (allowReplace) {
			replaceButton = createButton(parent, replaceButtonID, "&Replace", true);
			replaceButton.setEnabled(false);

			final Button replaceAllButton = createButton(parent, replaceAllButtonID, "Replace &All", false);
			replaceAllButton.setEnabled(true);
		}
		
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	protected FindAndReplaceDialog(Shell parentShell, boolean allowReplace) {
		super(parentShell);

		findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(Globals.getVaultTextViewer().getDocument());

		this.allowReplace = allowReplace;

		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE | SWT.MAX);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		final Composite findComposite = new Composite(composite, SWT.NONE);
		findComposite.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		findComposite.setLayoutData(gridData);
		
		final Label findWhatLabel = new Label(findComposite, SWT.NONE);
		findWhatLabel.setText("Fi&nd What:");
		
		findWhatText = new Text(findComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		findWhatText.setLayoutData(gridData);
		
		findWhatText.addModifyListener(e -> {
            findButton.setEnabled(!findWhatText.getText().isEmpty());

            if (replaceButton != null) {
                boolean enabled = !findWhatText.getText().isEmpty();
                replaceButton.setEnabled(enabled);
            }
        });

		findWhatText.addFocusListener(new TextFocusListener());
		
		if (allowReplace) {
			final Label replaceWithLabel = new Label(findComposite, SWT.NONE);
			replaceWithLabel.setText("R&eplace With:");
			
			replaceWithText = new Text(findComposite, SWT.BORDER);
			gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = SWT.FILL;
			replaceWithText.setLayoutData(gridData);
			
			replaceWithText.addFocusListener(new TextFocusListener());
		}

		final Composite matchAndDirectionComposite = new Composite(composite, SWT.NONE);
		matchAndDirectionComposite.setLayout(new GridLayout(2, false));
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		matchAndDirectionComposite.setLayoutData(gridData);
		
		final Composite checkBoxesComposite = new Composite(matchAndDirectionComposite, SWT.NONE);
		checkBoxesComposite.setLayout(new FillLayout(SWT.VERTICAL));
		
		wholeWordButton = new Button(checkBoxesComposite, SWT.CHECK);
		wholeWordButton.setText("Match &Whole Word Only");
		wholeWordButton.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.FindReplaceMatchWholeWord));
		
		matchCaseButton = new Button(checkBoxesComposite, SWT.CHECK);
		matchCaseButton.setText("&Match Case");
		matchCaseButton.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.FindReplaceMatchCase));
		
		final Group directionGroup = new Group(matchAndDirectionComposite, SWT.NONE);
		directionGroup.setText("Direction");
		directionGroup.setLayout(new RowLayout(SWT.VERTICAL));

		Button upButton = new Button(directionGroup, SWT.RADIO);
		upButton.setText("&Up");
		
		downButton = new Button(directionGroup, SWT.RADIO);
		downButton.setText("&Down");
		
		downButton.setSelection(true);

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest(allowReplace ? "Dialogs_ReplaceDialog" : "Dialogs_FindDialog"));
		
		composite.pack();
		
		return composite;
	}
}
