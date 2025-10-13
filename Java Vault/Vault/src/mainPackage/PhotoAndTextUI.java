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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class PhotoAndTextUI extends Composite implements ISelectionChangedListener {
	private final PhotoUI photoUI;

	private final SashForm sashForm;

	public SashForm getSashForm() {
		return sashForm;
	}

	public PhotoUI getPhotoUI() {
		return photoUI;
	}

	private final int maxWeight;

	private int[] weights = new int[2];
	
	private final Composite textComposite;
	
	public void setBackground(Color color) {
		textComposite.setBackground(color);
	}
	
	public boolean isPhotoVisible() {
		return photoUI.isVisible();
	}
	
	PhotoAndTextUI(Composite parent) {
		super(parent, SWT.BORDER);

		this.setLayout(new FillLayout());

		final Composite composite = new Composite(this, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setSashWidth(Globals.getPreferenceStore().getInt(PreferenceKeys.SashWidth));

		sashForm.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				SashFormUtils.processSashFormKeypressedEvent(sashForm, keyEvent, Globals.getMainApplicationWindow().getPreviousFocusedControl(), false);
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		GridData gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.verticalIndent = 0;
		gridData.horizontalIndent = 0;
		sashForm.setLayoutData(gridData);
		
		photoUI = new PhotoUI(sashForm);

		photoUI.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				getWeights();
			}
		});

		textComposite = new Composite(sashForm, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = Globals.getPreferenceStore().getInt(PreferenceKeys.TextMarginHeight);
		fillLayout.marginWidth = Globals.getPreferenceStore().getInt(PreferenceKeys.TextMarginWidth);
		textComposite.setLayout(fillLayout);

		final VaultTextViewer vaultTextViewer = new VaultTextViewer(textComposite, SWT.MULTI | SWT.V_SCROLL | SWT.HORIZONTAL | SWT.WRAP);
		vaultTextViewer.getControl().setVisible(false);

		Globals.setVaultTextViewer(vaultTextViewer);
		
		vaultTextViewer.getControl().addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				getWeights();
			}
		});
		
		sashForm.setWeights(0, 100);

		final int[] currentWeights = sashForm.getWeights();

		System.arraycopy(currentWeights, 0, weights, 0, currentWeights.length);
		
		maxWeight = weights[weights.length - 1];
		
		sashForm.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				getWeights();
			}
		});
		
		// composite.pack();
	}

	private void getWeights() {
		if (!photoUI.photoIsInvisible()) {
			final int[] currentWeights = sashForm.getWeights();

			System.arraycopy(currentWeights, 0, weights, 0, currentWeights.length);
			
			Globals.getPreferenceStore().setValue(PreferenceKeys.HorizontalSplitterWeight0, weights[0]);
			Globals.getPreferenceStore().setValue(PreferenceKeys.HorizontalSplitterWeight1, weights[1]);
		}
	}

	public void selectionChanged() {
		if (photoUI.photoIsInvisible()) {
			photoUI.setVisible(false);
			sashForm.setWeights(0, 100);
		}
		else {
			photoUI.setVisible(true);
			
			if (weights[1] == maxWeight) {
				weights = new int[] { Globals.getPreferenceStore().getInt(PreferenceKeys.HorizontalSplitterWeight0), 
									  Globals.getPreferenceStore().getInt(PreferenceKeys.HorizontalSplitterWeight1) };
			}
			
			sashForm.setWeights(weights);
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged();
	}
}
