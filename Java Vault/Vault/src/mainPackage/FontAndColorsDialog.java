/*
  Vault 3
  (C) Copyright 2026, Eric Bergman-Terrell
  
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class FontAndColorsDialog extends VaultDialog {
	@Override
	protected boolean isResizable() {
		return true;
	}

	private final String whichFont;

	private String fontString;

	public String getFontString() { return fontString; }

	private Canvas foregroundColorCanvas, backgroundColorCanvas;

	private FontData[] fontData;

	private final Color originalForegroundColor, originalBackgroundColor;

	private Color foregroundColor, backgroundColor;

	public Color getForegroundColor() { return foregroundColor; }
	public Color getBackgroundColor() { return backgroundColor; }

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Update Font");
	}

	/**
	 * Dialog box that allows the user to pick a font, foreground and background colors
	 * @param parentShell parent shell
	 * @param whichFont text that identifies the font. For example, "Outline Font"
	 * @param fontString string encoding of font
	 * @param originalForegroundColor starting foreground color
	 * @param originalBackgroundColor starting background color. If null, no UI will be rendered for picking the
	 *                                background color.
	 */
	public FontAndColorsDialog(Shell parentShell, String whichFont, String fontString, Color originalForegroundColor,
							   Color originalBackgroundColor) {
		super(parentShell);

		this.whichFont = whichFont;
		this.fontString = fontString;
		this.fontData = FontUtils.stringToFontList(fontString);
		this.originalForegroundColor = originalForegroundColor;
		this.originalBackgroundColor = originalBackgroundColor;
	}

	private Button okButton;

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
		
		okButton.setEnabled(true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			foregroundColor = foregroundColorCanvas.getBackground();

			if (originalBackgroundColor != null) {
				backgroundColor = backgroundColorCanvas.getBackground();
			}
		}

		setReturnCode(buttonId);
		close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));

		final Composite contentComposite = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 0;

		contentComposite.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		contentComposite.setLayoutData(gridData);

		final Label whichFontLabel = new Label(contentComposite, SWT.NONE);
		whichFontLabel.setText(whichFont);
		whichFontLabel.setLayoutData(gridData);

		final Label fontDescriptionLabel = new Label(contentComposite, SWT.NONE);
		fontDescriptionLabel.setText(FontUtils.stringToDescription(FontUtils.fontListToString(fontData)));
		fontDescriptionLabel.setLayoutData(gridData);

		final Button fontButton = new Button(contentComposite, SWT.NONE);
		fontButton.setText("&Font...");

		fontButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				final FontDialog fontDialog = new FontDialog(getShell());
				fontDialog.setFontList(fontData);
				fontDialog.setEffectsVisible(false);

				final FontData fontData = fontDialog.open();

				if (fontData != null) {
					FontAndColorsDialog.this.fontData = fontDialog.getFontList();

					fontDescriptionLabel.setText(FontUtils.stringToDescription(FontUtils.fontListToString(fontDialog.getFontList())));
					fontString = FontUtils.fontListToString(fontDialog.getFontList());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});

		final Composite colorsComposite = new Composite(contentComposite, SWT.NONE);

		final int spacingHeight = GraphicsUtils.getTextExtent("|").y;

		GridLayout twoColumnGridLayout = new GridLayout(2, false);
		twoColumnGridLayout.horizontalSpacing = 5;
		twoColumnGridLayout.marginWidth = 0;
		twoColumnGridLayout.marginHeight = spacingHeight;
		colorsComposite.setLayout(twoColumnGridLayout);

		final Button foregroundColorButton = new Button(colorsComposite, SWT.NONE);
		foregroundColorButton.setText("Fo&reground Color...");

		gridData = new GridData();
		gridData.heightHint = gridData.widthHint = GraphicsUtils.getTextExtent(foregroundColorButton.getText()).y;

		foregroundColorCanvas = new Canvas(colorsComposite, SWT.BORDER);
		foregroundColorCanvas.setLayoutData(gridData);
		foregroundColorCanvas.setForeground(originalForegroundColor);
		foregroundColorCanvas.setBackground(originalForegroundColor);

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				updateColor("Foreground Color", foregroundColorCanvas);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {}
		});

		if (originalBackgroundColor != null) {
			final Button backgroundColorButton = new Button(colorsComposite, SWT.NONE);
			backgroundColorButton.setText("&Background Color...");

			backgroundColorCanvas = new Canvas(colorsComposite, SWT.BORDER);
			backgroundColorCanvas.setLayoutData(gridData);
			backgroundColorCanvas.setBackground(originalBackgroundColor);

			backgroundColorButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent selectionEvent) {
					updateColor("Background Color", backgroundColorCanvas);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent selectionEvent) {}
			});
		}

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_UpdateFontDialog"));

		composite.pack();

		return composite;
	}

	private void updateColor(String dialogTitle, Canvas colorCanvas) {
		final ColorDialog colorDialog = new ColorDialog(getShell());
		colorDialog.setText(dialogTitle);
		colorDialog.setRGB(colorCanvas.getBackground().getRGB());

		final RGB newColor = colorDialog.open();

		if (newColor != null) {
			colorCanvas.setBackground(new Color(newColor.red, newColor.green, newColor.blue));
		}
	}
}