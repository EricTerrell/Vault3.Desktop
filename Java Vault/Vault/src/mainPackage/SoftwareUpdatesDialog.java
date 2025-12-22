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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SoftwareUpdatesDialog extends VaultDialog {
	private boolean updatesAreAvailable = false;
	
	private boolean cannotRetrieveLatestVersion = false;
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		final Button closeButton = createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
		closeButton.forceFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));
		
		final Label updatesLabel = new Label(composite, SWT.NONE);
		
		if (!cannotRetrieveLatestVersion) {
			String updatesAvailableText = updatesAreAvailable ? MessageFormat.format("An updated version of {0} is available.", StringLiterals.ProgramName) : MessageFormat.format("You are running the latest version of {0}. Please check again in the future.", StringLiterals.ProgramName);
			updatesLabel.setText(updatesAvailableText);
		}
		else
		{
			updatesLabel.setText("Cannot check for updates. Please try again later.");
		}

		final Label spacerLabel = new Label(composite, SWT.NONE);
		spacerLabel.setText(StringLiterals.EmptyString);

		// We want to unconditionally include the label and button, and hide them if necessary, leaving a gap in the dialog,
		// otherwise the dialog may be sized too small and the Donate button will be hidden.
		
		final Label downloadLabel = new Label(composite, SWT.NONE);
		downloadLabel.setText("Click the Download Updates button to visit the download web page.");

		final Button downloadUpdatesButton = new Button(composite, SWT.PUSH);
		downloadUpdatesButton.setText("Download &Updates");

		if (updatesAreAvailable) {
			downloadUpdatesButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					Program.launch("https://www.EricBT.com/Vault3/Download");
				}
			});
		}
		else {
			downloadLabel.setVisible(false);
			downloadUpdatesButton.setVisible(false);
		}

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_CheckForUpdatesDialog"));

		new Label(composite, SWT.NONE);
		
		new Label(composite, SWT.NONE).setText(MessageFormat.format("Click the Donate button to support continued {0} development.", StringLiterals.ProgramName));
		new Label(composite, SWT.NONE);
		
		final Button donateButton = new Button(composite, SWT.PUSH);
		donateButton.setText("&Donate");

		donateButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(StringLiterals.SupportURL);
			}
		});

		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);

		final Button vault3ForAndroidButton = new Button(composite, SWT.PUSH);
		vault3ForAndroidButton.setText("&Vault 3 for Android");

		vault3ForAndroidButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Globals.getPreferenceStore().getString(PreferenceKeys.Vault3ForAndroidURL));
			}
		});

		composite.pack();
		
		return composite;
	}

	private SoftwareUpdatesDialog(Shell parentShell, boolean updatesAreAvailable) {
		super(parentShell);

		this.updatesAreAvailable = updatesAreAvailable;
	}
	
	public SoftwareUpdatesDialog(Shell parentShell) {
		this(parentShell, false);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText("Check for Updates");
		
		if (!updatesAreAvailable) {
			try {
				final float latestVersion = getLatestVersion();
				
				updatesAreAvailable = latestVersion > Version.getVersionNumber();
			}
			catch (Throwable ex) {
				ex.printStackTrace();

				cannotRetrieveLatestVersion = true;

				final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);

				final MessageDialog messageDialog = new MessageDialog(
						getShell(),
						StringLiterals.ProgramName,
						icon,
						"Error checking for updates.",
						MessageDialog.ERROR,
						new String[] { "&OK" },
						0);

				messageDialog.open();
			}
		}
	}
	
	private static float getLatestVersion() throws IOException, InterruptedException {
		float latestVersion;

		try (final HttpClient httpClient = HttpClient.newHttpClient()) {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(String.format("https://www.EricBT.com/versions/vault3.txt?platform=%s&arch=%s",
							StringUtils.removeAllWhitespace(System.getProperty("os.name").toLowerCase()),
							StringUtils.removeAllWhitespace(System.getProperty("os.arch").toLowerCase())
					)))
					.build();

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			Globals.getLogger().info(String.format("version response: %s", response.body()));

			latestVersion = Float.parseFloat(response.body().trim());

			Globals.getPreferenceStore().setValue(PreferenceKeys.LastUpdateCheckDate, new Date().getTime());
		}

		return latestVersion;
	}
	
	public static void displayUpdatesDialogIfUpdatesAreAvailable(Shell parentShell) {
		try {
			Globals.getLogger().info("SoftwareUpdatesDialog.displayUpdatesDialogIfUpdatesAreAvailable");

			final PreferenceStore preferenceStore = Globals.getPreferenceStore();
			
			if (preferenceStore.getBoolean(PreferenceKeys.CheckForUpdatesAutomatically)) {
				final long lastCheckInstant = preferenceStore.getLong(PreferenceKeys.LastUpdateCheckDate);
				final long now = new Date().getTime();

				Globals.getLogger().info(String.format("lastCheckInstant = %d\nnow = %d", lastCheckInstant, now));

				final long sevenDaysInMilliseconds = 1000 * 60 * 60 * 24 * 7;
	
				if ((now - lastCheckInstant) >= sevenDaysInMilliseconds) {
					if (getLatestVersion() > Version.getVersionNumber()) {
						try {
							Globals.getLogger().info("launch SoftwareUpdatesDialog");

							final SoftwareUpdatesDialog softwareUpdatesDialog = new SoftwareUpdatesDialog(parentShell, true);

							softwareUpdatesDialog.open();
						}
						catch (Exception ex) {
							Globals.getLogger().info(String.format("Error: %s", ex.getMessage()));

							ex.printStackTrace();
						}
					}
				}
			}
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
