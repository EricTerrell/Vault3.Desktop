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
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import commonCode.IPlatform.PlatformEnum;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class TextActions {
	public static class InsertDateAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Insert date at current position";
		}

		public InsertDateAction() {
			super("&Insert Date", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/calendar.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canInsertDate());
		}
		
		public void run() {
			Globals.getVaultTextViewer().insertDate();
		}

		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}
	
	public static class InsertTimeAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Insert time at current position";
		}

		public InsertTimeAction() {
			super("Insert Ti&me", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/clock.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().insertTime();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canInsertTime());
		}

		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}
	
	public static class InsertDateAndTimeAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Insert date and time at current position";
		}

		public InsertDateAndTimeAction() {
			super("Insert &Date and Time");
			setAccelerator(SWT.F3);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().insertDateAndTime();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canInsertDateAndTime());
		}

		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}
	
	public static class InsertUrlAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Insert a web or file URL at the current position";
		}

		public InsertUrlAction() {
			super("I&nsert URL...");
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canInsertUrl());
		}
		
		public void run() {
			Globals.getVaultTextViewer().insertUrl();
		}

		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}

	public static class DisplayUrlAction extends Action {
		@Override
		public String getDescription() {
			return "Display the specified web or file URL";
		}

		public DisplayUrlAction() {
			super("&Browse URL");
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().displayUrl();
		}
	}

	public static class UndoAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Undo the last action";
		}

		public UndoAction() {
			super("&Undo", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/undo.png"))));
			setAccelerator(SWT.MOD1 | 'Z');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().undo();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canUndo());
		}

		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}

	public static class CutAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Cut selected text and put it in the clipboard";
		}

		public CutAction() {
			super("Cu&t", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/cut.png"))));
			setAccelerator(Globals.getPlatform() != PlatformEnum.MacOSX ? SWT.CTRL | 'X' : SWT.COMMAND | 'X');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().cut();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canCut());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}

	public static class CopyAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Put selected text in the clipboard";
		}

		public CopyAction() {
			super("Cop&y", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/copy.png"))));
			setAccelerator(Globals.getPlatform() != PlatformEnum.MacOSX ? SWT.CTRL | 'C' : SWT.COMMAND | 'C');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().copy();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canCopy());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}

	public static class PasteAction extends Action {
		@Override
		public String getDescription() {
			return "Insert clipboard contents into current text position";
		}

		public PasteAction() {
			super("&Paste", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/paste.png"))));
			setAccelerator(Globals.getPlatform() != PlatformEnum.MacOSX ? SWT.CTRL | 'V' : SWT.COMMAND | 'V');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().paste();
		}

		public void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canPaste());
		}
	}

	public static class SelectAllAction extends Action implements ISelectionChangedListener, ITextListener {
		@Override
		public String getDescription() {
			return "Select all text";
		}

		public SelectAllAction() {
			super("Select &All");
			setAccelerator(Globals.getPlatform() != PlatformEnum.MacOSX ? SWT.CTRL | 'A' : SWT.COMMAND | 'A');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().selectAll();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canSelectAll());
		}

		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}
	}

	public static class InsertTextFileAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Insert text file at cursor position";
		}

		public InsertTextFileAction() {
			super("Insert Te&xt File...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/insert_text_file.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}

		public void run() {
			Globals.getVaultTextViewer().insertTextFile(Globals.getMainApplicationWindow().getShell());
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canInsertTextFile());
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}

	public static class SetFontAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Selected a new font for the current outline item's text";
		}

		public SetFontAction() {
			super("Set F&ont...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/font.png"))));
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canSetFont());
		}

		@Override
		public void run() {
			Globals.getVaultTextViewer().setFont();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}
	}

	public static class GoToWebsitesAction extends Action {
		@Override
		public String getDescription() {
			return "Visit web site(s) in the current outline item";
		}

		public GoToWebsitesAction() {
			super("Browse &URLs...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/web.png"))));
			setAccelerator(SWT.MOD1 | 'U');
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			GoToWebsites.run(Globals.getMainApplicationWindow().getShell());
		}
	}
	
	public static class FindAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Find the specified text";
		}

		public FindAction() {
			super("&Find...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/find.png"))));
			setAccelerator(SWT.MOD1 | 'F');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Globals.getVaultTextViewer().find(Globals.getMainApplicationWindow().getShell());
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canFind());
		}
		
		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}
	
	public static class ReplaceAction extends Action implements ITextListener {
		@Override
		public String getDescription() {
			return "Replace the specified text with different text";
		}

		public ReplaceAction() {
			super("&Replace...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/text_replace.png"))));
			setAccelerator(SWT.MOD1 | 'R');
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Globals.getVaultTextViewer().replace(Globals.getMainApplicationWindow().getShell());
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTextViewer().canReplace());
		}
		
		@Override
		public void textChanged(TextEvent event) {
			Globals.getVaultTextViewer().getTextWidget().getDisplay().asyncExec(this::setEnabled);
		}
	}
}
