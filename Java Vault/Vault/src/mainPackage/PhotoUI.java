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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class PhotoUI extends Composite implements ISelectionChangedListener {
	private boolean menusArmed;
	
	private Image originalImage, resizedImage;

	private final Canvas canvas;

	private PhotoUIActions.CopyPictureToClipboardAction copyPictureToClipboardAction = new PhotoUIActions.CopyPictureToClipboardAction();
	private PhotoUIActions.CopyPictureFileAction copyPictureFileAction = new PhotoUIActions.CopyPictureFileAction();
	private PhotoUIActions.CopyPictureFilePathAction copyPictureFilePathAction = new PhotoUIActions.CopyPictureFilePathAction();
	
	private boolean disableDraw = false;
	
	private boolean getDisableDraw() {
		return disableDraw;
	}
	
	public void setDisableDraw(boolean value) {
		disableDraw = value;
	}
	
	private boolean allowScaling = true;

	public void setAllowScaling(boolean value) {
		allowScaling = value;
	}
	
	public boolean photoIsInvisible() {
		return originalImage == null && resizedImage == null;
	}
	
	private String copyPictureFilePreviousFolder;
	
	private static String currentImagePath; 
	
	public static String getCurrentImagePath() {
		return currentImagePath;
	}
	
	private ArrayList<IPhotoListener> listeners;
	
	public void setImages(String imagePath) {
		if (originalImage != null) {
			originalImage.dispose();
			originalImage = null;
		}
		
		if (resizedImage != null) {
			// The canvas background image is probably set to resizedImage, so null it out before disposing
			// resizedImage.
			canvas.setBackgroundImage(null);

			resizedImage.dispose();
			resizedImage = null;
		}

		currentImagePath = imagePath != null ? PhotoUtils.getPhotoPath(imagePath) : null;
		
		Globals.getLogger().info(String.format("setImages: currentImagePath: %s", currentImagePath));
		
		if (imagePath != null) {
			imagePath = PhotoUtils.getPhotoPath(imagePath);
			
			if (!getDisableDraw()) {
				try {
					if (imagePath != null) {
						if (!StringUtils.isURL(imagePath)) {
							originalImage = new Image(Display.getCurrent(), imagePath);
						}
						else {
							originalImage = GraphicsUtils.loadImage(imagePath);
						}
					}
					else {
						// Display an invisible graphic if the specified photo could not be loaded.
						originalImage = new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/transparent.png")); 
						
						Globals.getLogger().info(String.format("PhotoUI.setImages: cannot load file %s", imagePath));
					}
				}
				catch (Throwable ex) {
					// Display an invisible graphic if the specified graphic could not be loaded.
					originalImage = new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/transparent.png")); 
					
					Globals.getLogger().info(String.format("PhotoUI.setImages: cannot load file %s - %s", imagePath, ex.getMessage()));
				}
			}
		}

		canvas.redraw();

		listeners.forEach(IPhotoListener::getNotification);
	}
	
	PhotoUI(Composite parent) {
		super(parent, SWT.NONE);

		listeners = new ArrayList<>();
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);

		final Composite composite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		
		composite.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		composite.setLayoutData(gridData);

		canvas = new Canvas(composite, SWT.BORDER_DOT);
		gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		canvas.setLayoutData(gridData);

		canvas.addPaintListener(e -> {
			Globals.getLogger().info("PhotoUI constructor paint listener");

            final Rectangle rect = canvas.getClientArea();

            if (resizedImage == null || !resizedImage.getBounds().equals(rect)) {
                if (resizedImage != null) {
                    resizedImage.dispose();
                    resizedImage = null;
                }

                if (allowScaling) {
                    resizedImage = GraphicsUtils.resize(rect, originalImage, null);
                }
                else {
                    resizedImage = GraphicsUtils.copy(rect, originalImage, null);
                }
            }

            if (resizedImage != null) {
                canvas.setBackgroundImage(resizedImage);
            }
        });
		
		composite.pack();
		
		createContextMenu(composite);
	}
	
	private void createContextMenu(Composite composite) {
		final MenuManager menuManager = new MenuManager();
		menuManager.add(copyPictureToClipboardAction);
		menuManager.add(new Separator());
		menuManager.add(copyPictureFileAction);
		menuManager.add(copyPictureFilePathAction);
		
		canvas.setMenu(menuManager.createContextMenu(composite));
		
		canvas.getMenu().addMenuListener(new MenuListener() {
			@Override
			public void menuHidden(MenuEvent e) {
				Globals.getMainApplicationWindow().setStatusLineMessage(StringLiterals.EmptyString);
			}

			@Override
			public void menuShown(MenuEvent e) {
				if (!menusArmed) {
					MenuUtils.armAllMenuItems(menuManager);
					menusArmed = true;
				}

				// Move the cursor over a bit to avoid selecting the first menu item on Ubuntu.
				final Point cursorLocation = e.display.getCursorLocation();
				
				Display.getCurrent().setCursorLocation(cursorLocation.x + 1, cursorLocation.y + 1);
			}
		});
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
    	final IStructuredSelection selection = (IStructuredSelection) event.getSelection();

    	if (selection.size() != 1) {
    		setImages(null);
    	}
    	else {
			final OutlineItem outlineItem = (OutlineItem) selection.getFirstElement();
        	allowScaling = outlineItem.getAllowScaling();
        	
        	setImages(outlineItem.getPhotoPath());
    	}
	}
	
	public void copyPictureFile() throws IOException {
		if (currentImagePath != null) {
			Globals.getLogger().info(String.format("copyPictureFile: %s", currentImagePath));

			final File selectedItemPhotoFile = new File(currentImagePath);

			final FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
			fileDialog.setFilterExtensions(GraphicsUtils.getFilterExtensions());
			fileDialog.setFilterNames(GraphicsUtils.getFilterNames());
			fileDialog.setFilterPath(copyPictureFilePreviousFolder);
			fileDialog.setOverwrite(true);
			fileDialog.setFileName(selectedItemPhotoFile.getName());
			
			fileDialog.setText("Copy Picture File");

			final String destFilePath = fileDialog.open();
			
			if (destFilePath != null) {
				FileUtils.copyFile(currentImagePath, destFilePath);
				
				copyPictureFilePreviousFolder = fileDialog.getFilterPath();
			}
		}
	}
	
	public void copyPictureToClipboard() {
		Clipboard clipboard = null;
		Image image = null;
		
		try {
			clipboard = new Clipboard(Display.getCurrent());

			final ImageTransfer imageTransfer = ImageTransfer.getInstance();
			
			image = new Image(Display.getCurrent(), currentImagePath); 
			
			clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[] { imageTransfer});
		}
		finally {
			if (clipboard != null) {
				clipboard.dispose();
			}
			
			if (image != null) {
				image.dispose();
			}
		}
	}

	public void addListener(IPhotoListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(IPhotoListener listener) {
		listeners.remove(listener);
	}
}
