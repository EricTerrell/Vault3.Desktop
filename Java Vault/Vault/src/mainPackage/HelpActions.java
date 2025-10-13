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

import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class HelpActions {
	public static class AboutAction extends Action {
		@Override
		public String getDescription() {
			return MessageFormat.format("Display information about {0}", StringLiterals.ProgramName);
		}

		public AboutAction() {
			super("&About...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/question.png"))));
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			try {
				setEnabled(false);
				AboutDialog aboutDialog = new AboutDialog(Globals.getMainApplicationWindow().getShell());
				aboutDialog.open();
			}
			finally {
				setEnabled(true);
			}
		}
	}
	
	public static class HelpTopicsAction extends Action {
		@Override
		public String getDescription() {
			return "Display on-line help";
		}

		public HelpTopicsAction() {
			super("On-Line Help", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/help.png"))));
			setAccelerator(SWT.F1);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			HelpUtils.ProcessHelpRequest();
		}
	}
	
	public static class GettingStartedAction extends Action {
		@Override
		public String getDescription() {
			return "Display the Vault 3 Quick Start guide";
		}

		public GettingStartedAction() {
			super("Getting Started");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			HelpUtils.ProcessHelpRequest("gettingstarted");
		}
	}
	
	public static class VisitOurWebsiteAction extends Action {
		@Override
		public String getDescription() {
			return "Visit www.EricBT.com";
		}

		public VisitOurWebsiteAction() {
			super("&Visit EricBT.com");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Program.launch("http://www.EricBT.com");
		}
	}
	
	public static class SoftwareUpdatesAction extends Action {
		@Override
		public String getDescription() {
			return MessageFormat.format("Check for {0} updates on the www.EricBT.com website", StringLiterals.ProgramName);
		}
		
		public SoftwareUpdatesAction() {
			super("&Check for Updates...");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			SoftwareUpdatesDialog softwareUpdatesDialog = new SoftwareUpdatesDialog(Globals.getMainApplicationWindow().getShell());
			
			softwareUpdatesDialog.open();
		}
	}
	
	public static class SupportAction extends Action {
		@Override
		public String getDescription() {
			return MessageFormat.format("Support {0} development with a donation via PayPal", StringLiterals.ProgramName);
		}
		
		public SupportAction() {
			super("&Donate");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Program.launch(StringLiterals.SupportURL);
		}
	}
	
	public static class SendFeedbackAction extends Action {
		@Override
		public String getDescription() {
			return MessageFormat.format("Send Feedback about {0} via e-mail", StringLiterals.ProgramName);
		}

		public SendFeedbackAction() {
			super(MessageFormat.format("Send &Feedback about {0}...", StringLiterals.ProgramName), ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/email.png"))));
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void setEnabled() {
			setEnabled(EmailUI.canEmailFeedback());
		}
		
		public void run() {
			try {
				EmailUI.emailFeedback();
			}
			catch (Throwable ex) {
				ex.printStackTrace();

				final String message = MessageFormat.format("Cannot email.{0}{0}{1}", PortabilityUtils.getNewLine(),  ex.getMessage());
				MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON), message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();
			}
		}
	}

	public static class Vault3ForAndroidAction extends Action {
		@Override
		public String getDescription() {
			return "Get information about Vault 3 for Android";
		}

		public Vault3ForAndroidAction() {
			super("Va&ult 3 for Android");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Program.launch(Globals.getPreferenceStore().getString(PreferenceKeys.Vault3ForAndroidURL));
		}
	}
}
