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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ExportPhotosToDevice {
	private final static String PhotoFolderPrefix = "Vault3Photos";
	
	/**
	 * This class holds the arguments that are used when the export process is run by the IRunnableWithProgress object.
	 * @author Eric Bergman-Terrell
	 */
	private static class Arguments {
		public int maxPhotosPerFolder;
		public String destinationFolder;
		public Point deviceDimensions;
		public List<OutlineItem> selectedPhotos;
		public boolean deleteFolderContents;
	}
	
	/**
	 * Delete the contents of the specified folder and then delete the folder.
	 * @param destinationFolder folder to delete
	 * @param progressMonitor allows the process to be cancelled
	 */
	private static void deleteFolderContents(String destinationFolder, IProgressMonitor progressMonitor) {
		File destinationFolderObj = new File(destinationFolder);
		
		String[] children = destinationFolderObj.list();
		
		if (children != null) {
			for (String child : children) {
				if (!progressMonitor.isCanceled() && child.contains(PhotoFolderPrefix)) {
					final File folderToDelete = new File(destinationFolder, child);
					
					Display.getDefault().asyncExec(() -> Globals.getMainApplicationWindow().setStatusLineMessage(MessageFormat.format("Deleting folder {0}", folderToDelete.getAbsolutePath())));
					
					FileUtils.deleteFolderContents(folderToDelete);
				}
			}
		}
	}
	
	/**
	 * Scale and copy the specified photos to the specified folder.
	 * @param shell current shell
	 * @param deviceDimensions width and height of device (e.g. photo frame)
	 * @param destinationFolder folder where photos will be stored
	 * @param maxPhotos maximum number of photos to store in the destination folder.
	 * @param maxPhotosPerFolder maximum number of photos per folder
	 * @param shuffle if true, order of photos is randomized
	 * @param selectedPhotos list of selected photos
	 * @param deleteFolderContents if true, delete folders from destination folder that Vault 3 previously wrote.
	 */
	public static void export(Shell shell, Point deviceDimensions, String destinationFolder, int maxPhotos, int maxPhotosPerFolder, boolean shuffle, List<OutlineItem> selectedPhotos, boolean deleteFolderContents) {
		if (shuffle) {
			selectedPhotos = randomizePhotos(selectedPhotos);
		}

		if (maxPhotos > 0 && selectedPhotos.size() > maxPhotos) {
			selectedPhotos = selectedPhotos.subList(0, maxPhotos);
		}
		
		final Arguments arguments = new Arguments();
		arguments.maxPhotosPerFolder = maxPhotosPerFolder;
		arguments.destinationFolder = destinationFolder;
		arguments.deviceDimensions = deviceDimensions;
		arguments.selectedPhotos = selectedPhotos;
		arguments.deleteFolderContents = deleteFolderContents;
		
		final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(shell);

		int numberOfCores = Runtime.getRuntime().availableProcessors();
		final ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
		
		Globals.getLogger().info(String.format("export: %d cores", numberOfCores));

		try {
			IRunnableWithProgress runnable = progressMonitor -> {
                // If user chose to delete folders, estimate this as about 10% of the work of rendering photos.
                final int deleteFoldersWork = arguments.deleteFolderContents ? arguments.selectedPhotos.size() / 10 : 0;

                progressMonitor.beginTask("Exporting Photos to Device", arguments.selectedPhotos.size() + deleteFoldersWork);

                int fileNumber = 0, folderNumber = 0;

                // Delete folders in destination folder if requested. Only delete the folders that Vault3 created.
                if (arguments.deleteFolderContents) {
                    deleteFolderContents(arguments.destinationFolder, progressMonitor);
                }

                progressMonitor.worked(deleteFoldersWork);

                if (!progressMonitor.isCanceled()) {
                    for (OutlineItem selectedItem : arguments.selectedPhotos) {
                        if (progressMonitor.isCanceled()) {
                            throw new InterruptedException();
                        }
                        else {
                            String imagePath = selectedItem.getPhotoPath();

                            if (imagePath != null && !imagePath.isEmpty()) {
                                imagePath = PhotoUtils.getPhotoPath(imagePath);

                                final String _imagePath = imagePath;

                                fileNumber++;

                                if (fileNumber % arguments.maxPhotosPerFolder == 1) {
                                    folderNumber++;
                                    fileNumber = 1;
                                }

                                final int _fileNumber = fileNumber;
                                final int _folderNumber = folderNumber;

                                try {
                                    executorService.execute(() -> {
                                        if (!progressMonitor.isCanceled()) {
                                            Globals.getLogger().info(String.format("export: fileNumber: %d", _fileNumber));

                                            String destinationSubFolder = String.format("%s%sVault3Photos.%03d", arguments.destinationFolder, PortabilityUtils.getFileSeparator(), _folderNumber);
                                            File destFolder = new File(destinationSubFolder);

                                            if (!destFolder.exists()) {
                                                Globals.getLogger().info(String.format("export: creating folder %s", destinationSubFolder));
                                                destFolder.mkdirs();
                                            }

                                            final String _destinationPath = String.format("%s%s%04d.jpg", destinationSubFolder, PortabilityUtils.getFileSeparator(), _fileNumber);

                                            Globals.getLogger().info(String.format("_imagePath: %s _destinationPath: %s (%d, %d)", _imagePath, _destinationPath,
                                                                                    arguments.deviceDimensions.x, arguments.deviceDimensions.y));

                                            // Need to check the isCancelled property here too, otherwise it may take a long time for a cancellation
                                            // to take effect.
                                            if (!progressMonitor.isCanceled()) {
                                                Display.getDefault().syncExec(() -> Globals.getMainApplicationWindow().setStatusLineMessage(MessageFormat.format("Scaling and copying {0}", _imagePath)));

                                                GraphicsUtils.exportPhotoToDevice(_imagePath, _destinationPath, arguments.deviceDimensions);

                                                progressMonitor.worked(1);
                                            }
                                        }
                                    });
                                }
                                catch (Throwable ex) {
                                    ex.printStackTrace();
                                    Globals.getLogger().info(String.format("exportPhotosToDevice: exception %s", ex.getMessage()));
                                }
                            }
                        }
                    }

                    // Request a shutdown - stop accepting new tasks and shutdown when last task is completed.
                    executorService.shutdown();

                    if (!progressMonitor.isCanceled()) {
                        // Wait for all tasks to complete.
                        executorService.awaitTermination(365, TimeUnit.DAYS);
                    }
                }

                progressMonitor.done();
            };
			
			progressMonitorDialog.run(true, true, runnable);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
		finally {
			if (!executorService.isShutdown()) {
				// If the executor service isn't shut down yet, we need to shut it down, otherwise the JVM will not exit when the 
				// user closes the app.
				executorService.shutdownNow();
			}
		}
		
		Globals.getMainApplicationWindow().setStatusLineMessage(StringLiterals.EmptyString);
	}

	/**
	 * Return a shuffled list of photos.
	 * @param photos list of photos in original order
	 * @return list of photos in random order
	 */
	private static List<OutlineItem> randomizePhotos(List<OutlineItem> photos) {
		List<OutlineItem> remainingPhotos = new ArrayList<>(photos.size());
		remainingPhotos.addAll(photos);

		List<OutlineItem> randomizedPhotos = new ArrayList<>(photos.size());

		Random random = new Random(System.currentTimeMillis());
		
		while (!remainingPhotos.isEmpty()) {
			int index = random.nextInt(remainingPhotos.size());
			
			randomizedPhotos.add(remainingPhotos.get(index));
			remainingPhotos.remove(index);
		}
		
		return randomizedPhotos;
	}
}
