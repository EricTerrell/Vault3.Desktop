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

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class HelpUtils {
	private static String getHomePageURL() {
		final String homePageFilePath = MessageFormat.format("{0}{1}Help{1}Help.html", FileUtils.getRootPath(), PortabilityUtils.getFileSeparator());

		return String.format("file:///%s", new File(homePageFilePath).getAbsolutePath());
	}

	private static String getTopicURL(String helpTopicId) {
		final String topicFilePath = MessageFormat.format("{0}{1}Help{1}{2}.html", FileUtils.getRootPath(), PortabilityUtils.getFileSeparator(), helpTopicId);

		return String.format("file:///%s", new File(topicFilePath).getAbsolutePath());
	}
	
	public static void ProcessHelpRequest() {
		Program.launch(getHomePageURL());
	}
	
	public static void ProcessHelpRequest(String helpTopicId) {
		Program.launch(getTopicURL(helpTopicId));
	}
	
	private static void ProcessHelpRequest(IAction iAction) {
		String helpTopicId = iAction.getId();
		ProcessHelpRequest(helpTopicId);
	}
	
	public static void ProcessHelpRequest(HelpEvent event) {
		final MenuItem menuItem = (MenuItem) event.getSource();
		
		ActionContributionItem actionContributionItem = (ActionContributionItem) menuItem.getData();
		
		IAction iAction = actionContributionItem.getAction();

		ProcessHelpRequest(iAction);
	}

	public static String helpIDFromClass(Action action) {
		String id = action.getClass().getName();
		
		int index = id.indexOf('.');
		
		if (index >= 0 && index + 1 < id.length()) {
			id = id.substring(index + 1);
		}
		
		id = id.replace('$', '_');
		
		return id;
	}
}
