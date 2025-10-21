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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class VaultDialog extends Dialog {
	private final static String PADDING = "    ";

	private DialogSettings dialogSettings;

	protected DialogSettings getDialogSettings() {
		return dialogSettings;
	}
	
	protected String getSettingsFileName()
	{
		return String.format("%s.txt", getClass().getSimpleName());
	}
	
	private String getSettingsFilePath() {
		return String.format("%s%s%s", FileUtils.getConfigRootPath(), PortabilityUtils.getFileSeparator(), getSettingsFileName());
	}

	/**
	 * Load fields with content after the minimum dialog box size calculation has been performed. If there are fields with
	 * content that should be considered for the minimum size calculation, load those fields in createDialogArea. 
	 */
	protected void populateFields() {
	}
	
    protected IDialogSettings getDialogBoundsSettings(){
        return dialogSettings;
    }

    protected int getDialogBoundsStrategy(){
        return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
    }
	
    @Override
	public boolean close() {
		final boolean result = super.close();

        final String settingsFilePath = getSettingsFilePath();

    	try {
            Globals.getLogger().info(String.format("VaultDialog.close: saving DialogSettings to %s", settingsFilePath));

			dialogSettings.save(settingsFilePath);

            logFileContents();
		} catch (IOException e) {
            Globals.getLogger().info(String.format("VaultDialog.close: error saving settings to \"%s\": %s", settingsFilePath, e.getMessage()));
		}
		
		return result;
	}

	protected VaultDialog(Shell parentShell) {
		super(parentShell);

		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL;

		if (isResizable()) {
			shellStyle |= (SWT.RESIZE | SWT.MAX);
		}

		setShellStyle(shellStyle);
	}

	@Override
	public int open() {
		dialogSettings = new DialogSettings("settings");

        final String settingsFilePath = getSettingsFilePath();

		try {
            Globals.getLogger().info(String.format("VaultDialog.open: loading DialogSettings from %s", settingsFilePath));

            logFileContents();

			dialogSettings.load(settingsFilePath);
		} catch (IOException e) {
            Globals.getLogger().info(String.format("VaultDialog.open: error saving settings to \"%s\": %s", settingsFilePath, e.getMessage()));
		}
		
		return super.open();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
		newShell.setImage(icon);
	}

	@Override
	public void create() {
		super.create();

		final Point minimumSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell().setMinimumSize(minimumSize);
		
		populateFields();
	}

	public Label createStatusLabel(Composite parent) {
		final Label statusLabel = new Label(parent, SWT.NONE);

		statusLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		return statusLabel;
	}

	public void setStatusLabelText(Label statusLabel, String text) {
		statusLabel.setText(String.format("%s%s%s", PADDING, text, PADDING));
	}

    private void logFileContents() {
        try {
            Globals.getLogger().info(String.format("File contents:\n%s", FileUtils.readFile(getSettingsFilePath())));
        } catch(Exception ex) {
            Globals.getLogger().info(String.format("Cannot read file: %s", ex.getMessage()));
        }
    }
}
