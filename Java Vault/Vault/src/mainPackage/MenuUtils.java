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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Utilities that display status messages as menu items are highlighted.
 * 
 * @author Eric Bergman-Terrell
 *
 */
public class MenuUtils {
	/**
	 * Ensure that when each menu in the menuManager is highlighted, the status bar displays help text, and if the user
	 * presses F1, on-line help is displayed.
	 * @param menuManager
	 */
	public static void armAllMenuItems(MenuManager menuManager) {
		armAllMenuItems(menuManager.getMenu());
		
		for (IContributionItem iContributionItem : menuManager.getItems()) {
			if (iContributionItem instanceof MenuManager) {
				final MenuManager menuManager2 = (MenuManager) iContributionItem;
				
				menuManager2.getMenu().addMenuListener(new MenuListener() {
					@Override
					public void menuHidden(MenuEvent e) {
						Globals.getMainApplicationWindow().setStatusLineMessage(StringLiterals.EmptyString);
					}
	
					@Override
					public void menuShown(MenuEvent e) {
						Globals.getMainApplicationWindow().setStatusLineMessage(StringLiterals.EmptyString);
					}
				});
			}
		}
	}

	/**
	 * Ensure that when menu is highlighted, the status bar displays help text, and if the user
	 * presses F1, on-line help is displayed.
	 * @param menu
	 */
	private static void armAllMenuItems(Menu menu) {
		for (MenuItem menuItem : menu.getItems()) {
				menuItem.addArmListener(MenuUtils::displayMenuItemStatusBarText);

				menuItem.addHelpListener(HelpUtils::ProcessHelpRequest);
				
				if (menuItem.getMenu() != null) {
					armAllMenuItems(menuItem.getMenu());
				}
		}
	}

	/**
	 * Display a help message when a menu item is highlighted.
	 * @param armEvent event from widgetArmed event.
	 */
	private static void displayMenuItemStatusBarText(ArmEvent armEvent) {
		MenuItem menuItem = (MenuItem) armEvent.getSource();
		
		if (menuItem.getData() instanceof ActionContributionItem) {
			final ActionContributionItem contrib = (ActionContributionItem) menuItem.getData();
			
			final IAction iAction = contrib.getAction();
			
			if (iAction.getDescription() != null) {
				Globals.getMainApplicationWindow().setStatusLineMessage(iAction.getDescription());
			}
			else {
				Globals.getMainApplicationWindow().setStatusLineMessage(StringLiterals.EmptyString);
			}
		}
	}
}
