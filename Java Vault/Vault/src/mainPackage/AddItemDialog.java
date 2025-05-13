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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;

public class AddItemDialog extends VaultDialog {
	@Override
	protected String getSettingsFileName() {
		String settingsFileName;
		
		if (this.mode == Mode.Add && showAboveAndBelowRadioButtons) {
			settingsFileName = "AddItemDialog2.txt";
		}
		else if (this.mode == Mode.Add) {
			settingsFileName = "AddItemDialog.txt";
		}
		else {
			settingsFileName = "EditItemDialog.txt";
		}

		return settingsFileName;
	}

	@Override
	protected void populateFields() {
		if (outlineItem != null) {
			titleWidget.setText(outlineItem.getTitle());
			titleWidget.selectAll();

			textWidget.setText(outlineItem.getText());

			if (outlineItem.getPhotoPath() != null) {
				photoFileText.setText(outlineItem.getPhotoPath());
			}
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public enum Mode { Add, Edit }
	
	private final Mode mode;
	
	private final OutlineItem outlineItem;
	
	private String title;
	
	private static String previousPhotoFilterPath;
	
	private Label statusLabel;
	
	private Color nonErrorBackground, errorBackground;
	
	private Text titleWidget, textWidget, photoFileText;

	private Button belowRadioButton;
	private Button detachPhotoFileButton;
	private Button okButton;
	private Button allowScalingCheckBox;
	
	public String getTitle() {
		return title;
	}

	private String text;
	
	public String getText() {
		return text;
	}

	private String photoPath;
	
	public String getPhotoPath() {
		return (photoPath != null && !photoPath.trim().isEmpty()) ? photoPath : null;
	}
	
	private boolean addBelow;
	
	public boolean getAddBelow() {
		return addBelow;
	}

	private boolean allowScaling;
	
	public boolean getAllowScaling() {
		return allowScaling;
	}
	
	private boolean showAboveAndBelowRadioButtons;
	
	public AddItemDialog(Shell parentShell, boolean showAboveAndBelowRadioButtons, Mode mode, OutlineItem outlineItem) {
		super(parentShell);
		
		this.mode = mode;
		this.outlineItem = outlineItem;
		
		this.showAboveAndBelowRadioButtons = showAboveAndBelowRadioButtons;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
		
		okButton.setEnabled(false);
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

		final Composite titleAndTextComposite = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		titleAndTextComposite.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		titleAndTextComposite.setLayoutData(gridData);
		
		final Label titleLabel = new Label(titleAndTextComposite, SWT.NONE);
		titleLabel.setText("&Title:");

		titleWidget = new Text(titleAndTextComposite, SWT.BORDER);
		titleWidget.addFocusListener(new TextFocusListener());
		
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		titleWidget.setLayoutData(gridData);

		titleWidget.addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                e.doit = true;
            }
        });

	    titleWidget.addModifyListener(event -> enableDisableOKButton());

	    final Label textLabel = new Label(titleAndTextComposite, SWT.NONE);
		textLabel.setText("Te&xt:");
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		textLabel.setLayoutData(gridData);

		textWidget = new Text(titleAndTextComposite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		textWidget.addFocusListener(new TextFocusListener());
		
		textWidget.addModifyListener(e -> enableDisableOKButton());
		
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		textWidget.setLayoutData(gridData);
		
		final Group photoFileGroup = new Group(composite, SWT.NONE);
		photoFileGroup.setLayout(new GridLayout(2, false));
		photoFileGroup.setText("&Photo File Path or URL:");
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.NONE;
		photoFileGroup.setLayoutData(gridData);
		
		photoFileText = new Text(photoFileGroup, SWT.BORDER);
		photoFileText.addFocusListener(new TextFocusListener());
		
		photoFileText.addModifyListener(e -> enableDisableOKButton());
		
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.CENTER;
		photoFileText.setLayoutData(gridData);
		
		final Button browseButton = new Button(photoFileGroup, SWT.PUSH);
		browseButton.setText("B&rowse...");
		
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setText("Select Photo File");
				fileDialog.setFilterExtensions(GraphicsUtils.getFilterExtensions());
				fileDialog.setFilterNames(GraphicsUtils.getFilterNames());
				fileDialog.setFilterPath(previousPhotoFilterPath);
				
				boolean finished = false;
				
				do {
					final String filePath = fileDialog.open();
					
					if (filePath != null && new File(filePath).exists()) {
						photoPath = filePath;
						
						photoFileText.setText(filePath);
						
						previousPhotoFilterPath = fileDialog.getFilterPath();
						
						finished = true;
					}
					else if (filePath == null) {
						finished = true;
					}
				} while (!finished);
			}
		});
		
		detachPhotoFileButton = new Button(photoFileGroup, SWT.PUSH);
		detachPhotoFileButton.setText("&Detach Photo File");
		
		gridData = new GridData(SWT.FILL);
		gridData.horizontalSpan = 2;
		detachPhotoFileButton.setLayoutData(gridData);
		
		detachPhotoFileButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				photoFileText.setText(StringLiterals.EmptyString);
			}
		});
		
		photoFileText.addModifyListener(e -> {
            enableDisableDetachButton();
            enableDisableAllowScalingCheckBox();
        });
		
		enableDisableDetachButton();
		
		allowScalingCheckBox = new Button(photoFileGroup, SWT.CHECK);
		allowScalingCheckBox.setText("&Allow Scaling");

		gridData = new GridData(SWT.FILL);
		gridData.horizontalSpan = 2;
		allowScalingCheckBox.setLayoutData(gridData);
		
		if (outlineItem != null) {
			allowScalingCheckBox.setSelection(outlineItem.getAllowScaling());
		}
		else {
			allowScalingCheckBox.setSelection(true);
		}
		
		enableDisableAllowScalingCheckBox();
		
		if (showAboveAndBelowRadioButtons) {
			final Group group = new Group(composite, SWT.NONE);
			group.setText("Add new item:");
			group.setLayout(new RowLayout(SWT.VERTICAL));

			Button aboveRadioButton = new Button(group, SWT.RADIO);
			aboveRadioButton.setText("&Above Current Item");
			
			belowRadioButton = new Button(group, SWT.RADIO);
			belowRadioButton.setText("&Below Current Item");
			belowRadioButton.setSelection(true);
		}
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest(mode == Mode.Add ? "Dialogs_AddItemDialog" : "Dialogs_EditItemDialog"));
		
		composite.pack();

		return composite;
	}

	private void enableDisableDetachButton() {
		detachPhotoFileButton.setEnabled(!photoFileText.getText().trim().isEmpty());
	}
	
	private void enableDisableAllowScalingCheckBox() {
		allowScalingCheckBox.setEnabled(!photoFileText.getText().trim().isEmpty());
	}
	
	private void enableDisableOKButton() {
		final String title = titleWidget.getText().trim();
		boolean enabled = !title.isEmpty();
	          
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

		newShell.setText(mode == Mode.Add ? "Add" : "Edit");
	}

	@Override
	protected void okPressed() {
		title = titleWidget.getText();
		text = textWidget.getText();
		photoPath = photoFileText.getText().trim();
		
		if (showAboveAndBelowRadioButtons) {
			addBelow = belowRadioButton.getSelection();
		}

		allowScaling = allowScalingCheckBox.getSelection();
		
		super.okPressed();
	}
}
