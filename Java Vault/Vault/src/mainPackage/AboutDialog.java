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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

public class AboutDialog extends VaultDialog {
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));
		
		new Label(composite, SWT.NONE).setText(MessageFormat.format("{0} Version {1}", StringLiterals.ProgramName, Version.getVersionNumberText()));
		new Label(composite, SWT.NONE).setText(MessageFormat.format("(C) Copyright {0}, Eric Bergman-Terrell", Version.getCopyrightYear()));

		Link eclipseLink = new Link(composite, SWT.NONE);
		
		eclipseLink.setText("<a>www.EricBT.com</a>");
		eclipseLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(StringLiterals.HomePageURL);
			}
		});
		
		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);

		final Composite eclipseComposite = new Composite(composite, SWT.NONE);
		final RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = rowLayout.marginTop = rowLayout.marginLeft = rowLayout.marginRight = 0;
		eclipseComposite.setLayout(rowLayout);

		Label eclipseLabel = new Label(eclipseComposite, SWT.NONE);
		eclipseLabel.setText(MessageFormat.format("{0} is developed with ", StringLiterals.ProgramName));
		
		eclipseLink = new Link(eclipseComposite, SWT.NONE);
		eclipseLink.setText("<a>Eclipse</a>");
		eclipseLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://www.eclipse.org");
			}
		});
		
		eclipseLabel = new Label(eclipseComposite, SWT.NONE);
		eclipseLabel.setText("and uses the ");
		
		eclipseLink = new Link(eclipseComposite, SWT.NONE);
		eclipseLink.setText("<a>SWT</a> and");
		eclipseLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://www.eclipse.org/swt");
			}
		});
		
		eclipseLink = new Link(eclipseComposite, SWT.NONE);
		eclipseLink.setText("<a>JFace</a> libraries.");
		eclipseLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://wiki.eclipse.org/index.php/JFace");
			}
		});
		
		final Link silkLink = new Link(composite, SWT.NONE);
		silkLink.setText(MessageFormat.format("{0} uses artwork from the <a>Silk</a> icon set.", StringLiterals.ProgramName));
		
		silkLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://www.famfamfam.com/lab/icons/silk/");
			}
		});
		
		final Link heureuseLink = new Link(composite, SWT.NONE);
		heureuseLink.setText(MessageFormat.format("{0} uses Christian d''Heureuse''s <a>Base64Coder</a> class for Base64 encoding and decoding.", StringLiterals.ProgramName));
		
		heureuseLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://www.source-code.biz");
			}
		});
		
		final Link sqliteLink = new Link(composite, SWT.NONE);
		sqliteLink.setText(MessageFormat.format("{0} stores its documents as <a>SQLite</a> databases.", StringLiterals.ProgramName));
		
		sqliteLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://www.sqlite.org");
			}
		});
		
		final Link sqlite4JavaLink = new Link(composite, SWT.NONE);
		sqlite4JavaLink.setText(MessageFormat.format("{0} uses <a>sqlite4java</a> to manipulate its documents.", StringLiterals.ProgramName));
		
		sqlite4JavaLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://bitbucket.org/almworks/sqlite4java");
			}
		});
		
		final Link iTextLink = new Link(composite, SWT.NONE);
		iTextLink.setText(MessageFormat.format("{0} uses <a>iText</a> to create PDF files.", StringLiterals.ProgramName));
		
		iTextLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://itextpdf.com/");
			}
		});

		final Link imagescalrLink = new Link(composite, SWT.NONE);
		imagescalrLink.setText(MessageFormat.format("{0} uses <a>imgscalr</a> to export Photos.", StringLiterals.ProgramName));
		
		imagescalrLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://github.com/thebuzzmedia/imgscalr");
			}
		});
		
		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);

		new Label(composite, SWT.NONE).setText(String.format("Using Java version %s", System.getProperty("java.version")));

		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);

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

		Button licenseTermsButton = new Button(composite, SWT.PUSH);
		licenseTermsButton.setText("&Read License Terms");
		
		licenseTermsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				LicenseTermsDialog licenseTermsDialog = new LicenseTermsDialog(getShell());
				licenseTermsDialog.open();
			}
		});

		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);
		
		final Button feedbackButton = new Button(composite, SWT.PUSH);
		feedbackButton.setText(MessageFormat.format("Send &Feedback about {0}", StringLiterals.ProgramName));
		feedbackButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					EmailUI.emailFeedback();
				}
				catch (Throwable ex) {
					ex.printStackTrace();

					final String message = MessageFormat.format("Cannot email.{0}{0}{1}", PortabilityUtils.getNewLine(),  ex.getMessage());
					final MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON), message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
					messageDialog.open();
				}
			}
		});

		new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);
		
		final Button checkForUpdatesButton = new Button(composite, SWT.PUSH);
		checkForUpdatesButton.setText("Check for &Updates");
		checkForUpdatesButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SoftwareUpdatesDialog softwareUpdatesDialog = new SoftwareUpdatesDialog(Globals.getMainApplicationWindow().getShell());
					softwareUpdatesDialog.open();
				}
				catch (Throwable ex) {
					ex.printStackTrace();
				}
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

		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.DebuggingMode)) {
			new Label(composite, SWT.NONE).setText(StringLiterals.EmptyString);

			final Button gcButton = new Button(composite, SWT.PUSH);
			gcButton.setText("Force Garbage Collection");
			
			gcButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Runtime runtime = Runtime.getRuntime();
					
					Globals.getLogger().info(String.format("Before forced garbage collection, free memory = %d", runtime.freeMemory()));
					
					runtime.gc();
					
					Globals.getLogger().info(String.format("After forced garbage collection, free memory = %d", runtime.freeMemory()));
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_AboutDialog"));
		
		composite.pack();
		
		return composite;
	}

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

	public AboutDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(MessageFormat.format("About {0}", StringLiterals.ProgramName));
	}

}
