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

/**
 * 
 */
package mainPackage;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class ViewActions {
	private final static int minSashWeight =  1;
	private final static int maxSashWeight = 99;
	
	public static class SwitchBetweenOutlineAndTextAction extends Action {
		@Override
		public String getDescription() {
			return "Switch between outline and text /photo windows";
		}

		public SwitchBetweenOutlineAndTextAction() {
			super("S&witch Between Outline and Text / Photo");
			setAccelerator(SWT.F5);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			if (Globals.getVaultTreeViewer().getControl().isFocusControl()) {
				Globals.getVaultTextViewer().getControl().forceFocus();
			}
			else if (Globals.getVaultTextViewer().getControl().isFocusControl()) {
				Globals.getVaultTreeViewer().getControl().forceFocus();
			}
			else {
				Globals.getVaultTreeViewer().getControl().forceFocus();
			}
		}
	}
	
	public static class MoveVerticalSplitAction extends Action {
		@Override
		public String getDescription() {
			return "Move the divider between the ouline and text windows";
		}

		public MoveVerticalSplitAction() {
			super("Move Vertical S&plit", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/move_vertical_split.png"))));
			setAccelerator(SWT.F6);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			if (Globals.getVaultTreeViewer().getControl().isFocusControl()) {
				Globals.getMainApplicationWindow().setPreviousFocusedControl(Globals.getVaultTreeViewer().getControl());
			}
			else if (Globals.getVaultTextViewer().getControl().isFocusControl()) {
				Globals.getMainApplicationWindow().setPreviousFocusedControl(Globals.getVaultTextViewer().getControl());
			}
			else {
				Globals.getMainApplicationWindow().setPreviousFocusedControl(null);
			}
			
			Globals.getMainApplicationWindow().setStatusLineMessage("Move the splitter with the left and right arrow keys. Press Esc to quit.");
			Globals.getMainApplicationWindow().getSashForm().forceFocus();
			Globals.getMainApplicationWindow().getSashForm().setCapture(true);
		}
	}
	
	public static class MoveHorizontalSplitAction extends Action implements ISelectionChangedListener {
		@Override
		public String getDescription() {
			return "Move the divider between the photograph and the text";
		}

		public MoveHorizontalSplitAction() {
			super("Move Horizontal S&plit", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/move_horizontal_split.png"))));
			setId(HelpUtils.helpIDFromClass(this));
		}

		private void setEnabled() {
			setEnabled(Globals.getMainApplicationWindow().getPhotoAndTextUI().isPhotoVisible());
		}
		
		public void run() {
			if (Globals.getVaultTreeViewer().getControl().isFocusControl()) {
				Globals.getMainApplicationWindow().setPreviousFocusedControl(Globals.getVaultTreeViewer().getControl());
			}
			else if (Globals.getVaultTextViewer().getControl().isFocusControl()) {
				Globals.getMainApplicationWindow().setPreviousFocusedControl(Globals.getVaultTextViewer().getControl());
			}
			else {
				Globals.getMainApplicationWindow().setPreviousFocusedControl(null);
			}

			Globals.getMainApplicationWindow().setStatusLineMessage("Move the splitter with the up and down arrow keys. Press Esc to quit.");
			Globals.getMainApplicationWindow().getPhotoAndTextUI().getSashForm().forceFocus();
			Globals.getMainApplicationWindow().getPhotoAndTextUI().getSashForm().setCapture(true);
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			setEnabled();
		}
	}
	
	public static class MaximizeOutlineAction extends Action {
		@Override
		public String getDescription() {
			return "Make the outline window as large as possible";
		}

		public MaximizeOutlineAction() {
			super("Maximize &Outline", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/maximize_outline.png"))));
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Globals.getMainApplicationWindow().getSashForm().setWeights(maxSashWeight, minSashWeight);
		}
	}

	public static class MaximizeTextAndPhotosAction extends Action {
		@Override
		public String getDescription() {
			return "Make the text window as large as possible";
		}

		public MaximizeTextAndPhotosAction() {
			super("Maximize &Text / Photo", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/maximize_textphotos.png"))));
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Globals.getMainApplicationWindow().getSashForm().setWeights(minSashWeight, maxSashWeight);
		}
	}

	public static class ViewOutlineAndTextPhotos extends Action {
		@Override
		public String getDescription() {
			return "Display both the outline and text / photo windows";
		}

		public ViewOutlineAndTextPhotos() {
			super("&Display Outline and Text / Photo", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/view_outline_and_textphotos.png"))));
		}
		
		public void run() {
			Globals.getMainApplicationWindow().getSashForm().setWeights(50, 50);
		}
	}

	public static class SlideShowAction extends Action implements ISelectionChangedListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Display photographs in a slide show";
		}

		public SlideShowAction() {
			super("S&lide Show...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/slideshow.png"))));
			setAccelerator(SWT.F4);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		private void setEnabled() {
			setEnabled(PhotoProcessing.canSlideshow());
		}
		
		@Override
		public void run() {
			// Need to save changes to current item, in case this affects the exclusions.
			Globals.getVaultTextViewer().saveChanges();

			PhotoProcessing.slideshow(Globals.getMainApplicationWindow().getShell());
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
