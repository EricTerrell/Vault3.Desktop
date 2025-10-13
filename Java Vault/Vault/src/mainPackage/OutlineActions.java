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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class OutlineActions {
	public static class CutAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Delete selected outline items, but allow subsequent pasting";
		}

		public CutAction() {
			super("&Cut", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/cut.png"))));
			setToolTipText("Cut outline items");
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canCutSelectedItems());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().cutSelectedItems();
		}
	}

	public static class CopyAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Copy selected outline items for subsequent pasting";
		}

		public CopyAction() {
			super("Cop&y", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/copy.png"))));
			setToolTipText("Copy outline items");
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canCopySelectedItems());
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().copySelectedItems();
		}
	}

	public static class PasteAction extends Action implements ISelectionChangedListener, IClipboardListener {
		@Override
		public String getDescription() {
			return "Insert previously cut outline items in new position";
		}

		public PasteAction() {
			super("Pa&ste", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/paste.png"))));
			setEnabled(false);
			setToolTipText("Paste outline items");
			setId(HelpUtils.helpIDFromClass(this));

			Globals.getClipboard().addListener(this);
		}
		
		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canPasteItems());
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		@Override
		public void getNotification() {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().pasteItems(Globals.getMainApplicationWindow().getShell());
		}
	}

	public static class AddAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Add new outline item";
		}

		public AddAction() {
			super("&Add...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/add.png"))));
			setAccelerator(SWT.ALT | SWT.INSERT);
			setEnabled(true);
			setId(HelpUtils.helpIDFromClass(this));
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(Globals.getVaultTreeViewer().canAddItem());
		}

		public void run() {
			Globals.getVaultTreeViewer().addItem(Globals.getMainApplicationWindow().getShell());
		}
	}
	
	public static class RemoveAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Delete selected outline items";
		}
		public RemoveAction() {
			super("Remo&ve...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/delete.png"))));
			setAccelerator(SWT.ALT | SWT.DEL);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(Globals.getVaultTreeViewer().canRemoveSelectedItems());
		}

		public void run() {
			try {
				setEnabled(false);
				Globals.getVaultTreeViewer().removeSelectedItems(true);
			}
			finally {
				setEnabled(true);
			}
		}
	}
	
	public static class ImportPicturesAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Create new outline items that display the specified graphics files";
		}

		public ImportPicturesAction() {
			super("Impo&rt Photos...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/import_pictures.png"))));
			setAccelerator(SWT.MOD1 | 'I');
			setEnabled(true);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			VaultDocumentImports.importPictures(Globals.getMainApplicationWindow().getShell());
		}
		
		public void setEnabled() {
			setEnabled(VaultDocumentImports.canImportPictures());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}
	}
	
	public static class MoveUpAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Move the selected outline item up";
		}

		public MoveUpAction() {
			super("Move &Up", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/arrow_up.png"))));
			setAccelerator(SWT.ALT | SWT.ARROW_UP);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canMoveUp());
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().moveUp();
			setEnabled();
			((OutlineActions.MoveDownAction) Globals.getMainApplicationWindow().getAction(OutlineActions.MoveDownAction.class)).setEnabled();
		}
	}
	
	public static class MoveDownAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Move the selected outline item down";
		}

		public MoveDownAction() {
			super("Move &Down", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/arrow_down.png"))));
			
			setAccelerator(SWT.ALT | SWT.ARROW_DOWN);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canMoveDown());
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}
		
		public void run() {
			Globals.getVaultTreeViewer().moveDown();
			setEnabled();
			((OutlineActions.MoveUpAction)Globals.getMainApplicationWindow().getAction(OutlineActions.MoveUpAction.class)).setEnabled();
		}
	}
	
	public static class IndentAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Indent selected outline items";
		}

		public IndentAction() {
			super("I&ndent", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/arrow_right.png"))));
			setAccelerator(SWT.ALT | SWT.ARROW_RIGHT);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(Globals.getVaultTreeViewer().canIndentSelectedItems());
		}

		public void run() {
			Globals.getVaultTreeViewer().indentSelectedItems();
		}
	}
	
	public static class UnindentAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Unindent selected outline items";
		}

		public UnindentAction() {
			super("Uninden&t", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/arrow_left.png"))));
			setAccelerator(SWT.ALT | SWT.ARROW_LEFT);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(Globals.getVaultTreeViewer().canUnindentSelectedItems());
		}
		
		public void run() {
			Globals.getVaultTreeViewer().unindentSelectedItems();
		}
	}

	public static class MoveAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Move the selected outline items";
		}

		public MoveAction() {
			super("&Move...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/move.png"))));
			setAccelerator(SWT.MOD1 | 'M');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled(Globals.getVaultTreeViewer().canMoveSelectedItems());
		}

		public void run() {
			Globals.getVaultTreeViewer().moveSelectedItems(Globals.getMainApplicationWindow().getShell());
		}
	}

	public static class ExpandAction extends Action implements ISelectionChangedListener, ITreeViewerListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Open selected outline item and show subordinate items (double-clicking listbox item works too)";
		}

		public ExpandAction() {
			super("&Expand", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/expand.png"))));
			setAccelerator(SWT.MOD1 | SWT.ARROW_UP);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().expandSelectedItems();
			setEnabled();
		}
		
		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canExpandSelectedItems());
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}
	
		@Override
		public void getNotification() {
			setEnabled();
		}
	}

	public static class ExpandAllAction extends Action implements ITreeViewerListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Open all outline items and show their subordinate items";
		}

		public ExpandAllAction() {
			super("E&xpand All", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/expand_all.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Globals.setBusyCursor();

			try {
				Globals.getVaultTreeViewer().expandAll();
				
				// Generate a TreeCollapsed event so that the GUI can update correctly.
				treeCollapsed(null);
				((OutlineActions.CollapseAllAction)Globals.getMainApplicationWindow().getAction(OutlineActions.CollapseAllAction.class)).treeCollapsed(null);
			}
			finally {
				Globals.setPreviousCursor();
			}
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canExpandAllItems());
		}
		
		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void getNotification() {
			setEnabled();
		}
	}

	public static class CollapseAction extends Action implements ISelectionChangedListener, ITreeViewerListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Close selected outline item and hide subordinate items (double-clicking listbox item works too)";
		}

		public CollapseAction() {
			super("Colla&pse", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/collapse.png"))));
			setAccelerator(SWT.MOD1 | SWT.ARROW_DOWN);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().collapseSelectedItems();
			setEnabled();
		}
		
		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canCollapseSelectedItems());
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void getNotification() {
			setEnabled();
		}
	}

	public static class CollapseAllAction extends Action implements ITreeViewerListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Close all outline items and hide their subordinate items";
		}

		public CollapseAllAction() {
			super("Collapse All", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/collapse_all.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Globals.setBusyCursor();

			try {
				Globals.getVaultTreeViewer().collapseAllItems();

				// Generate a TreeCollapsed event so that the GUI can update correctly.
				treeCollapsed(null);
				((OutlineActions.ExpandAllAction)Globals.getMainApplicationWindow().getAction(OutlineActions.ExpandAllAction.class)).treeCollapsed(null);
			}
			finally {
				Globals.setPreviousCursor();
			}
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canCollapseAllItems());
		}
		
		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			Globals.getVaultTreeViewer().getTree().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void getNotification() {
			setEnabled();
		}
	}

	public static class EditAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Change the title, text, and photo file for the selected item";
		}

		public EditAction() {
			super("Ed&it...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/edit.png"))));
			setAccelerator(SWT.F2);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canEditOutlineItem());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTextViewer().saveChanges();
			
			Globals.getVaultTreeViewer().editOutlineItem(Globals.getMainApplicationWindow().getShell());
			
			((FileActions.CopyPictureFileAction) Globals.getMainApplicationWindow().getAction(FileActions.CopyPictureFileAction.class)).setEnabled();
			((FileActions.RenamePictureFileAction)Globals.getMainApplicationWindow().getAction(FileActions.RenamePictureFileAction.class)).setEnabled();
			((FileActions.DeletePictureFileAction)Globals.getMainApplicationWindow().getAction(FileActions.DeletePictureFileAction.class)).setEnabled();
		}
	}

	public static class SortAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Sort selected outline items in lexical order";
		}

		public SortAction() {
			super("&Sort", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/sort.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canSort());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}

		public void run() {
			Globals.getVaultTreeViewer().sort();
		}
	}

	public static class SetFontAction extends Action {
		@Override
		public String getDescription() {
			return "Select a new font for the outline window";
		}

		public SetFontAction() {
			super("Set &Font...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/font.png"))));
			setEnabled(true);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getVaultTreeViewer().setFont();
		}
	}
	
	public static class SelectAllAction extends Action implements ISelectionChangedListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Select all outline items";
		}

		public SelectAllAction() {
			super("Select A&ll");
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canSelectAll());
		}
		
		@Override
		public void run() {
			Globals.getVaultTreeViewer().selectAll();
			Globals.getMainApplicationWindow().fireAllSelectionChangedListeners();
		}
		
		@Override
		public void getNotification() {
			setEnabled();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}
	}
}
