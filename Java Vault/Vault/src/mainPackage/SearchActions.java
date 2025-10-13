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
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class SearchActions {
	public static class SearchAction extends Action implements ISelectionChangedListener, IDocumentLoadUnloadListener {
		@Override
		public String getDescription() {
			return "Search for outline items containing specific text";
		}

		public SearchAction() {
			super("&Search", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/search.png"))));
			setAccelerator(SWT.F7);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getMainApplicationWindow().getNavigateAndSearchTabFolder().setSelection(1);
			Globals.getMainApplicationWindow().getSearchUI().prepareToSearch();
		}

		private void setEnabled() {
			setEnabled(Globals.getVaultTreeViewer().canSearch());
		}
		
		@Override
		public void getNotification() {
			setEnabled();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent arg0) {
			setEnabled();
		}
	}

	public static class NextSearchHitAction extends Action {
		@Override
		public String getDescription() {
			return "Move cursor to next search hit in the current outline item";
		}

		public NextSearchHitAction () {
			super("&Next Search Hit", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/next_search_hit.png"))));
			setAccelerator(SWT.F8);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getVaultTextViewer().nextSearchHit();
		}
	}
	
	public static class PreviousSearchHitAction extends Action {
		@Override
		public String getDescription() {
			return "Move cursor to previous search hit in the current outline item";
		}

		public PreviousSearchHitAction () {
			super("&Previous Search Hit", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/prev_search_hit.png"))));
			setAccelerator(SWT.F9);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getVaultTextViewer().previousSearchHit();
		}
	}
	
	public static class NextSearchItemAction extends Action {
		@Override
		public String getDescription() {
			return "Go to next outline item containing searched-for text";
		}

		public NextSearchItemAction() {
			super("Next Search &Item", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/next_search_item.png"))));
			setAccelerator(SWT.F11);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getMainApplicationWindow().getSearchUI().goToNextSearchTopic();
		}
	}
	
	public static class PreviousSearchItemAction extends Action {
		@Override
		public String getDescription() {
			return "Go to previous outline item containing searched-for text";
		}

		public PreviousSearchItemAction() {
			super("Previous Search I&tem", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/prev_search_item.png"))));
			setAccelerator(SWT.F12);
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getMainApplicationWindow().getSearchUI().goToPreviousSearchTopic();
		}
	}
	
	public static class ClearSearchAction extends Action {
		@Override
		public String getDescription() {
			return "Clear search results and highlighting";
		}

		public ClearSearchAction() {
			super("&Clear Search");
			setEnabled(false);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		@Override
		public void run() {
			Globals.getMainApplicationWindow().getSearchUI().clearSearch();
		}
	}
}
