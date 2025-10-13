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

import java.util.UUID;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class MoveItemsDialog extends VaultDialog implements ISelectionChangedListener, ITreeViewerListener {
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void populateFields() {
		// Use the OutlineItem copy constructor to ensure that this tree viewer doesn't
		// bind to the same object as the main tree viewer.
		vaultTreeViewer.setInput(new OutlineItem(Globals.getVaultDocument().getContent(), null, false));
		vaultTreeViewer.collapseAll();
	}

	private VaultTreeViewer vaultTreeViewer;

	private Label statusLabel;

	private Color nonErrorBackground, errorBackground;
	
	private Button aboveRadioButton, belowRadioButton;

	private Group placeItemsGroup;
	
	private UUID selectedNodeUUID;
	
	public UUID getSelectedNodeUUID() {
		return selectedNodeUUID;
	}
	
	private boolean selectedNodeIsExpanded;
	
	public boolean isSelectedNodeExpanded() {
		return selectedNodeIsExpanded;
	}

	private boolean addAtTop;
	
	public boolean getAddAtTop() {
		return addAtTop;
	}
	
	private boolean firstItemIsSelected() {
		boolean result = false;
		
		if (vaultTreeViewer.getTree().getSelectionCount() == 1) {
			final TreeItem[] selectedItems = vaultTreeViewer.getTree().getSelection();

			result = vaultTreeViewer.getTree().getItems()[0] == selectedItems[0];
		}
		
		return result;
	}
	
	@Override
	public boolean close() {
    	final boolean result = super.close();

		vaultTreeViewer.removeSelectionChangedListener(this);
		vaultTreeViewer.removeTreeListener(this);
		
		vaultTreeViewer.dispose();
    	
		return result;
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);
		
	    statusLabel = createStatusLabel(parent);
	
		nonErrorBackground = statusLabel.getBackground();
		errorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		
		return result;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		selectedNodeUUID = ((OutlineItem) selection.getFirstElement()).getUuid();

		final TreeItem[] treeItems = vaultTreeViewer.getTree().getSelection();
		
		selectedNodeIsExpanded = treeItems[0].getExpanded();
		
		// Do not allow the user to move an item under itself.
		boolean illegalMove = false;
		
		for (OutlineItem nodeToMove : Globals.getVaultTreeViewer().getSelectedItems()) {
			if (nodeToMove.findNode(selectedNodeUUID) != null) {
				illegalMove = true;
				break;
			}
		}

		placeItemsGroup.setEnabled(firstItemIsSelected());
		aboveRadioButton.setEnabled(firstItemIsSelected());
		belowRadioButton.setEnabled(firstItemIsSelected());
		
		final Button okButton = getButton(IDialogConstants.OK_ID);
		
		okButton.setEnabled(selection.getFirstElement() != null && !illegalMove);
		
        if (illegalMove) {
      	  setStatusLabelText(statusLabel, "Cannot move items to selected location.");
      	  statusLabel.setBackground(errorBackground);
        }
        else {
      	  setStatusLabelText(statusLabel, StringLiterals.EmptyString);
      	  statusLabel.setBackground(nonErrorBackground);
        }
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		composite.setLayout(new GridLayout());
		
		final Composite labelComposite = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		labelComposite.setLayout(gridLayout);

		final Label label = new Label(labelComposite, SWT.NONE);
		label.setText("&Select New Location:");

		final Image image = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);
		
		final Label imageLabel = new Label(labelComposite, SWT.NONE);
		imageLabel.setImage(image);

		final String tooltipText = "Press Enter to expand/contract, or use right and left arrow keys.";
		imageLabel.setToolTipText(tooltipText);
		
		vaultTreeViewer = new VaultTreeViewer(composite, SWT.SINGLE, true);
		vaultTreeViewer.setContentProvider(new VaultTreeContentProvider());
		vaultTreeViewer.setLabelProvider(new VaultLabelProvider());
		
		vaultTreeViewer.applyUserPreferences();
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		vaultTreeViewer.getTree().setLayoutData(gridData);

		placeItemsGroup = new Group(composite, SWT.NONE);
		placeItemsGroup.setText("Place Items:");
		placeItemsGroup.setLayout(new RowLayout(SWT.VERTICAL));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		placeItemsGroup.setLayoutData(gridData);
		
		aboveRadioButton = new Button(placeItemsGroup, SWT.RADIO);
		aboveRadioButton.setText("&Above First Item");
		
		belowRadioButton = new Button(placeItemsGroup, SWT.RADIO);
		belowRadioButton.setText("&Below First Item");
		belowRadioButton.setSelection(true);
		
		vaultTreeViewer.addSelectionChangedListener(this);
		vaultTreeViewer.addTreeListener(this);

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_MoveItemsDialog"));
		
		composite.pack();
		
		return composite;
	}

	@Override
	protected void okPressed() {
		addAtTop = firstItemIsSelected() && aboveRadioButton.getSelection();

		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Don't want a default button, otherwise user will get confused - pressing enter will collapse/expand outline items in the tree.
		createButton(parent, IDialogConstants.OK_ID, "&OK", false);
		
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
		
		Button okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Move");
	}

	public MoveItemsDialog(Shell parentShell) {
		super(parentShell);
	}

	private void updateExpansion() {
		Display.getCurrent().asyncExec(() -> {
            TreeItem[] treeItems = vaultTreeViewer.getTree().getSelection();

            selectedNodeIsExpanded = treeItems[0].getExpanded();
        });
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		updateExpansion();
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		updateExpansion();
	}
}
