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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

public class SlideshowDialog extends VaultDialog {
	@Override
	protected boolean isResizable() {
		return true;
	}

	private Runnable slideshowRunnable;

	private boolean timerIsTicking = false;

	@Override
	public boolean close() {
    	boolean result = super.close();

    	cancelTimer();
		
		return result;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	private final List<OutlineItem> allPhotos, selectedPhotos, photosInOriginalOrder;
	private List<OutlineItem> photos;

	private int currentPhotoIndex = 0;
	
	private PhotoUI photoUI;

	private void setCurrentPhotoIndex(int newValue) {
		currentPhotoIndex = newValue;
		
		if (currentPhotoIndex == 0 && Globals.getPreferenceStore().getBoolean(PreferenceKeys.RandomSlideshow)) {
			randomizePhotos();
		}
	}
	
	private void setImage() {
		photoUI.setImages(photos.get(currentPhotoIndex).getPhotoPath());
		getShell().setText(String.format("Slideshow - %s", photos.get(currentPhotoIndex).getTitle()));
	}

	private void firstButtonAction() {
		setCurrentPhotoIndex(0);
		setImage();
	}
	
	private void lastButtonAction() {
		setCurrentPhotoIndex(photos.size() - 1);
		setImage();
	}

	private void nextOrPreviousButtonAction(int increment) {
		int newIndex = currentPhotoIndex + increment;
		
		if (newIndex >= photos.size() || newIndex < 0) {
			if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.SlideshowContinuousLoop)) {
				if (increment == 1) {
					newIndex = 0;
				}
				else {
					newIndex = photos.size() - 1;
				}
			}
			else {
				cancelTimer();
				
				if (newIndex >= photos.size()) {
					newIndex = photos.size() - 1;
				}
				else {
					newIndex = 0;
				}
			}
		}
		
		setCurrentPhotoIndex(newIndex);
		
		try {
			setImage();
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
	
	private void nextButtonAction() {
		nextOrPreviousButtonAction(+1);
	}
	
	private void previousButtonAction() {
		nextOrPreviousButtonAction(-1);
	}

	private void cancelTimer() {
		Display.getCurrent().timerExec(-1, slideshowRunnable);
		timerIsTicking = false;
	}
	
	private void startTimer(boolean performActionImmediately) {
		timerIsTicking = true;

		if (performActionImmediately) {
			nextButtonAction();
		}
		
		Display.getCurrent().timerExec(Globals.getPreferenceStore().getInt(PreferenceKeys.SlideshowInterval) * 1000, slideshowRunnable);
	}
	
	private void randomizePhotos() {
		photos.clear();

		final ArrayList<OutlineItem> remainingPhotos = new ArrayList<>(photosInOriginalOrder.size());
		remainingPhotos.addAll(photosInOriginalOrder);

		final Random random = new Random(System.currentTimeMillis());
		
		while (!remainingPhotos.isEmpty()) {
			int index = random.nextInt(remainingPhotos.size());
			
			photos.add(remainingPhotos.get(index));
			remainingPhotos.remove(index);
		}
	}
	
	private void unRandomizePhotos() {
		photos.clear();
		photos.addAll(photosInOriginalOrder);
	}

	private void displaySelectedClicked() {
		Globals.getPreferenceStore().setValue(PreferenceKeys.SlideshowAllItems, false);
		photos = selectedPhotos;
		photosInOriginalOrder.clear();
		photosInOriginalOrder.addAll(selectedPhotos);
		setCurrentPhotoIndex(0);
		setImage();
	}
	
	private void displayAllClicked() {
		Globals.getPreferenceStore().setValue(PreferenceKeys.SlideshowAllItems, true);
		photos = allPhotos;
		photosInOriginalOrder.clear();
		photosInOriginalOrder.addAll(allPhotos);
		setCurrentPhotoIndex(0);
		setImage();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final int columns = 11;

		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout(columns, false);
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginBottom = 0;
		gridLayout.marginTop = 0;
		composite.setLayout(gridLayout);

		final Button playButton = new Button(composite, SWT.PUSH);
		playButton.setText("&Play");
		
		playButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				startTimer(true);
			}
		});

		final Button stopButton = new Button(composite, SWT.PUSH);
		stopButton.setText("&Stop");
		
		stopButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelTimer();
			}
		});

		final Button firstButton = new Button(composite, SWT.PUSH);
		firstButton.setText("&First");
		
		firstButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				firstButtonAction();
			}
		});

		final Button previousButton = new Button(composite, SWT.PUSH);
		previousButton.setText("Pre&vious");
		
		previousButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				previousButtonAction();
			}
		});

		final Button nextButton = new Button(composite, SWT.PUSH);
		nextButton.setText("&Next");
		
		nextButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				nextButtonAction();
			}
		});

		final Button lastButton = new Button(composite, SWT.PUSH);
		lastButton.setText("&Last");
		
		lastButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				lastButtonAction();
			}
		});

		final Button closeButton = new Button(composite, SWT.PUSH);
		closeButton.setText("&Close");
		
		closeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		final Composite intervalComposite = new Composite(composite, SWT.NONE);
		intervalComposite.setLayout(new GridLayout(2, false));

		final Scale intervalScale = new Scale(intervalComposite, SWT.HORIZONTAL);
		intervalScale.setMinimum(1);
		intervalScale.setMaximum(60);
		intervalScale.setSelection(Globals.getPreferenceStore().getInt(PreferenceKeys.SlideshowInterval));
		
		final String secondsFormat = "{0} Second(s)";
		
		final Label intervalValueLabel = new Label(intervalComposite, SWT.NONE);
		final String text = MessageFormat.format(secondsFormat, intervalScale.getSelection());
		intervalValueLabel.setText(text);
		
		// Allocate room for 2 digits (plus some extra space to account for different width of digits).
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = GraphicsUtils.getTextExtent(text).x + GraphicsUtils.getTextExtent("0").x * 3;
		gridData.horizontalAlignment = SWT.RIGHT;
		intervalValueLabel.setLayoutData(gridData);
		
		intervalScale.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = MessageFormat.format(secondsFormat, intervalScale.getSelection());
				intervalValueLabel.setText(text);
				Globals.getPreferenceStore().setValue(PreferenceKeys.SlideshowInterval, intervalScale.getSelection());
				
				if (timerIsTicking) {
					cancelTimer();
					startTimer(false);
				}
			}
		});

		final Button randomCheckbox = new Button(composite, SWT.CHECK);
		randomCheckbox.setText("&Random");
		randomCheckbox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.RandomSlideshow));
		
		randomCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean random = randomCheckbox.getSelection();
				
				if (random) {
					randomizePhotos();
				}
				else {
					unRandomizePhotos();
				}
				
				Globals.getPreferenceStore().setValue(PreferenceKeys.RandomSlideshow, random);
			}
		});
		
		final Button loopCheckbox = new Button(composite, SWT.CHECK);
		loopCheckbox.setText("L&oop");
		loopCheckbox.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.SlideshowContinuousLoop));
		
		loopCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Globals.getPreferenceStore().setValue(PreferenceKeys.SlideshowContinuousLoop, loopCheckbox.getSelection());
			}
		});

		final Composite radioButtonComposite = new Composite(composite, SWT.NONE);
		radioButtonComposite.setLayout(new GridLayout(2, false));

		final Button allItemsRadioButton = new Button(radioButtonComposite, SWT.RADIO);
		allItemsRadioButton.setText("&All");
		
		allItemsRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				displayAllClicked();
			}
		});

		final Button selectedItemsRadioButton = new Button(radioButtonComposite, SWT.RADIO);
		selectedItemsRadioButton.setText("&Selected");
		
		selectedItemsRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectedClicked();
			}
		});
		
		allItemsRadioButton.setSelection(Globals.getPreferenceStore().getBoolean(PreferenceKeys.SlideshowAllItems));
		selectedItemsRadioButton.setSelection(!Globals.getPreferenceStore().getBoolean(PreferenceKeys.SlideshowAllItems));
		
		if (allPhotos.isEmpty()) {
			allItemsRadioButton.setSelection(false);
			allItemsRadioButton.setEnabled(false);
			selectedItemsRadioButton.setSelection(true);
		}
		
		if (selectedPhotos.isEmpty()) {
			selectedItemsRadioButton.setSelection(false);
			selectedItemsRadioButton.setEnabled(false);
			allItemsRadioButton.setSelection(true);
		}
		
		photoUI = new PhotoUI(composite);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = columns;
		photoUI.setLayoutData(gridData);

		if (allItemsRadioButton.getSelection() && allItemsRadioButton.getEnabled()) {
			displayAllClicked();
		}
		
		if (selectedItemsRadioButton.getSelection() && selectedItemsRadioButton.getEnabled()) {
			displaySelectedClicked();
		}
		
		if ((allItemsRadioButton.getSelection() && allItemsRadioButton.getEnabled()) || 
			(selectedItemsRadioButton.getSelection() || selectedItemsRadioButton.getEnabled())) {
			setImage();
		}

		parent.addHelpListener(e -> HelpUtils.ProcessHelpRequest("Dialogs_SlideshowDialog"));
		
		composite.pack();
		
		return composite;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText("Slideshow");
		
		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.SlideshowFullScreen)) {
			newShell.setBounds(Display.getCurrent().getBounds());
		}
	}

	public SlideshowDialog(Shell parentShell, List<OutlineItem> allPhotos, List<OutlineItem> selectedPhotos) {
		super(parentShell);

		photos = new ArrayList<>();
		this.allPhotos = allPhotos;
		this.selectedPhotos = selectedPhotos;
		photosInOriginalOrder = new ArrayList<>();
		
		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.RandomSlideshow)) {
			randomizePhotos();
		}
		
		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.SlideshowFullScreen)) {
			setShellStyle(SWT.NO_TRIM);
		}
		else {
			setShellStyle(getShellStyle() | (SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX));
		}

		slideshowRunnable = () -> {
            nextButtonAction();
            Display.getCurrent().timerExec(Globals.getPreferenceStore().getInt(PreferenceKeys.SlideshowInterval) * 1000, slideshowRunnable);
        };
	}
}
