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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class GoToWebsitesDialog extends VaultDialog {
	@Override
	protected void populateFields() {
		for (String url : urls) {
			String decodedUrl = url;
			
			try {
				decodedUrl = URLDecoder.decode(url, GoToWebsites.encoding);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			list.add(decodedUrl);
		}
		
		list.select(0);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private org.eclipse.swt.widgets.List list;

	private final List<String> urls;
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLOSE_ID) {
			close();
		}
		else {
			super.buttonPressed(buttonId);
		}
	}

	private void displaySelectedWebsite() {
		String[] selectedItems = list.getSelection();
		
		if (selectedItems.length > 0) {
			GoToWebsites.launch(selectedItems[0], getShell());
			close();
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, true));

		final Composite labelComposite = new Composite(composite, SWT.NONE);
		
		labelComposite.setLayout(new GridLayout(2, true));
		
		Label label = new Label(labelComposite, SWT.NONE);
		label.setText("&URL:");

		final Image image = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);
		
		final Label imageLabel = new Label(labelComposite, SWT.NONE);
		imageLabel.setImage(image);
		
		imageLabel.setToolTipText(StringLiterals.DisplayWebsiteToolTip);
		
		list = new org.eclipse.swt.widgets.List(composite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.heightHint = list.getItemHeight() * 5;
		list.setLayoutData(gridData);
		
		list.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				displaySelectedWebsite();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});

		list.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == ' ') {
					displaySelectedWebsite();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_GoToWebsitesDialog"));
		
		composite.pack();
		
		return composite;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Browse URLs");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		final int goToWebsiteButtonID = 1000;
		final Button goToWebsiteButton = createButton(parent, goToWebsiteButtonID, "&Browse URL", true);
		
		goToWebsiteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final String[] selectedItems = list.getSelection();

				GoToWebsites.launch(selectedItems[0], getShell());
				close();
			}
		});
				
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	/**
	 * @param parentShell
	 */
	protected GoToWebsitesDialog(Shell parentShell, List<String> urls) {
		super(parentShell);
		
		this.urls = urls;
	}
}
