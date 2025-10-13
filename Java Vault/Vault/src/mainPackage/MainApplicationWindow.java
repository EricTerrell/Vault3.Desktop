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
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import commonCode.DocumentMetadata;

public class MainApplicationWindow extends ApplicationWindow {
	private boolean checkForModificationsWaiting;
	
	private synchronized boolean isCheckForModificationsWaiting() { return checkForModificationsWaiting; }
	
	private synchronized void setCheckForModificationsWaiting(boolean checkForModificationsWaiting) { 
		this.checkForModificationsWaiting = checkForModificationsWaiting; 
	}
	
	private DocumentMetadata previousDocumentMetadata;
	
	private String commandLineArgumentFilePath;
	
	private ISelectionChangedListener[] selectionChangedListeners;

	/**
	 * Fire the selection changed event to every listener listening for tree selection changes. This is necessary when a call is made,
	 * such as TreeViewer().selectAll(), that doesn't fire the event.
	 */
	public void fireAllSelectionChangedListeners() {
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			ISelection selection = Globals.getVaultTreeViewer().getSelection();
			listener.selectionChanged(new SelectionChangedEvent(Globals.getVaultTreeViewer(), selection));
		}
	}
	
	private static int alreadyRunningMessageBoxNestingDepth = 0;
	
	private static SingletonApplication singletonApplication;
	
	private Control previousFocusedControl;
	
	public Control getPreviousFocusedControl() {
		return previousFocusedControl;
	}
	
	public void setPreviousFocusedControl(Control control) {
		previousFocusedControl = control;
	}

	public void setStatusLineMessage(String message) {
		getStatusLineManager().setMessage(message);
	}
	
	private MenuManager menuManager, fileMenuManager, editMenuManager;
	
	private final Point minimumSize;
	
	private final Runnable autoSaveRunnable;
	
	private final Runnable checkForModificationsRunnable;
	
	private PhotoAndTextUI photoAndTextUI;

	public PhotoAndTextUI getPhotoAndTextUI() {
		return photoAndTextUI;
	}
	
	private final ArrayList<IDocumentLoadUnloadListener> documentLoadUnloadListeners = new ArrayList<>();

	private VaultTreeViewer vaultTreeViewer;

	private final Hashtable<Class<?>, Action> actions = new Hashtable<>();
	
	public Action getAction(Class<?> actionClass) {
		return actions.get(actionClass);
	}
	
	// File actions:
	private final FileActions.ExitAction exitAction = new FileActions.ExitAction();
	private final FileActions.NewAction newAction = new FileActions.NewAction();
	private final FileActions.OpenAction openAction = new FileActions.OpenAction();
	private final FileActions.SaveAction saveAction = new FileActions.SaveAction();
	private final FileActions.SaveAsAction saveAsAction = new FileActions.SaveAsAction();
	private final FileActions.PasswordAction passwordAction = new FileActions.PasswordAction();
	private final FileActions.PrintAction printAction = new FileActions.PrintAction();
	private final FileActions.ImportFromXMLFileAction importFromXMLFileAction = new FileActions.ImportFromXMLFileAction();
	private final FileActions.ImportFromVault3FileAction importFromVault3XMLFileAction = new FileActions.ImportFromVault3FileAction();
	private final FileActions.ImportFromFileSystemAction importFromFileSystemAction = new FileActions.ImportFromFileSystemAction();
	
	private final FileActions.PDFExportAction pdfExportAction = new FileActions.PDFExportAction();
	private final FileActions.XMLExportAction xmlExportAction = new FileActions.XMLExportAction();
	private final FileActions.TextExportAction textExportAction = new FileActions.TextExportAction();
	private final FileActions.ExportPhotosToDeviceAction exportPhotosToDeviceAction = new FileActions.ExportPhotosToDeviceAction();
	private final FileActions.CopyPictureFileAction copyPictureFileAction = new FileActions.CopyPictureFileAction();
	private final FileActions.DeletePictureFileAction deletePictureFileAction = new FileActions.DeletePictureFileAction();
	private final FileActions.RenamePictureFileAction renamePictureFileAction = new FileActions.RenamePictureFileAction();
	private final FileActions.EditPictureFileAction editPictureFileAction = new FileActions.EditPictureFileAction();
	private final FileActions.RotateLeftFileAction rotateLeftFileAction = new FileActions.RotateLeftFileAction();
	private final FileActions.RotateRightFileAction rotateRightFileAction = new FileActions.RotateRightFileAction();

	// Outline actions:
	private final OutlineActions.AddAction addAction = new OutlineActions.AddAction();
	private final OutlineActions.ImportPicturesAction importPicturesAction = new OutlineActions.ImportPicturesAction();
	private final OutlineActions.RemoveAction removeAction = new OutlineActions.RemoveAction();
	private final OutlineActions.MoveUpAction moveUpAction = new OutlineActions.MoveUpAction();
	private final OutlineActions.MoveDownAction moveDownAction = new OutlineActions.MoveDownAction();
	private final OutlineActions.IndentAction indentAction = new OutlineActions.IndentAction();
	private final OutlineActions.UnindentAction unindentAction = new OutlineActions.UnindentAction();
	private final OutlineActions.MoveAction moveAction = new OutlineActions.MoveAction();
	private final OutlineActions.SelectAllAction outlineSelectAllAction = new OutlineActions.SelectAllAction();
	private final OutlineActions.CutAction cutOutlineItemAction = new OutlineActions.CutAction();
	private final OutlineActions.CopyAction copyOutlineItemAction = new OutlineActions.CopyAction();
	private final OutlineActions.PasteAction pasteOutlineItemAction = new OutlineActions.PasteAction();
	private final OutlineActions.ExpandAction expandAction = new OutlineActions.ExpandAction();
	private final OutlineActions.ExpandAllAction expandAllAction = new OutlineActions.ExpandAllAction();
	private final OutlineActions.CollapseAction collapseAction = new OutlineActions.CollapseAction();
	private final OutlineActions.CollapseAllAction collapseAllAction = new OutlineActions.CollapseAllAction();
	private final OutlineActions.EditAction editOutlineAction = new OutlineActions.EditAction();
	private final OutlineActions.SortAction sortAction = new OutlineActions.SortAction();
	private final OutlineActions.SetFontAction outlineSetFontAction = new OutlineActions.SetFontAction();

	// Edit actions:
	private final TextActions.UndoAction undoAction = new TextActions.UndoAction();
	private final TextActions.CutAction cutTextAction = new TextActions.CutAction();
	private final TextActions.CopyAction copyTextAction = new TextActions.CopyAction();
	private final TextActions.PasteAction pasteTextAction = new TextActions.PasteAction();
	private final TextActions.SelectAllAction selectAllTextAction = new TextActions.SelectAllAction();
	private final TextActions.InsertDateAction insertDateAction = new TextActions.InsertDateAction();
	private final TextActions.InsertTimeAction insertTimeAction = new TextActions.InsertTimeAction();
	private final TextActions.InsertDateAndTimeAction insertDateAndTimeAction = new TextActions.InsertDateAndTimeAction();
	private final TextActions.InsertTextFileAction insertTextFileAction = new TextActions.InsertTextFileAction();
	private final TextActions.SetFontAction setFontAction = new TextActions.SetFontAction();
	private final TextActions.GoToWebsitesAction goToWebsitesAction = new TextActions.GoToWebsitesAction();
	private final TextActions.FindAction findAction = new TextActions.FindAction();
	private final TextActions.ReplaceAction replaceAction = new TextActions.ReplaceAction();
	private final TextActions.InsertUrlAction insertUrlAction = new TextActions.InsertUrlAction();
	private final TextActions.DisplayUrlAction displayUrlAction = new TextActions.DisplayUrlAction();

	// Photo Actions
	private final PhotoUIActions.CopyPictureToClipboardAction copyPictureToClipboardAction = new PhotoUIActions.CopyPictureToClipboardAction();
	private final PhotoUIActions.CopyPictureFileAction copyPictureFileActionPhotoMenu = new PhotoUIActions.CopyPictureFileAction();
	private final PhotoUIActions.CopyPictureFilePathAction copyPictureFilePathAction = new PhotoUIActions.CopyPictureFilePathAction();
	
	// View actions:
	private final ViewActions.SwitchBetweenOutlineAndTextAction switchBetweenOutlineAndTextAction = new ViewActions.SwitchBetweenOutlineAndTextAction();
	private final ViewActions.MaximizeOutlineAction maximizeOutlineAction = new ViewActions.MaximizeOutlineAction();
	private final ViewActions.MaximizeTextAndPhotosAction maximizeTextAndPhotosAction = new ViewActions.MaximizeTextAndPhotosAction();
	private final ViewActions.MoveVerticalSplitAction moveVerticalSplitAction = new ViewActions.MoveVerticalSplitAction();
	private final ViewActions.MoveHorizontalSplitAction moveHorizontalSplitAction = new ViewActions.MoveHorizontalSplitAction();
	private final ViewActions.ViewOutlineAndTextPhotos viewOutlineAndTextPhotos = new ViewActions.ViewOutlineAndTextPhotos();
	private final ViewActions.SlideShowAction slideShowAction = new ViewActions.SlideShowAction();
	
	// Search Actions:
	private final SearchActions.SearchAction searchAction = new SearchActions.SearchAction();
	private final SearchActions.NextSearchHitAction nextSearchHitAction = new SearchActions.NextSearchHitAction();
	private final SearchActions.PreviousSearchHitAction previousSearchHitAction = new SearchActions.PreviousSearchHitAction();
	private final SearchActions.NextSearchItemAction nextSearchItemAction = new SearchActions.NextSearchItemAction();
	private final SearchActions.PreviousSearchItemAction previousSearchItemAction = new SearchActions.PreviousSearchItemAction();
	private final SearchActions.ClearSearchAction clearSearchAction = new SearchActions.ClearSearchAction();
	
	// Options Actions:
	private final OptionsActions.SettingsAction settingsAction = new OptionsActions.SettingsAction();
	
	// Help actions:
	private final HelpActions.AboutAction aboutAction = new HelpActions.AboutAction();
	private final HelpActions.HelpTopicsAction helpTopicsAction = new HelpActions.HelpTopicsAction();
	private final HelpActions.VisitOurWebsiteAction visitOurWebsiteAction = new HelpActions.VisitOurWebsiteAction();
	private final HelpActions.SoftwareUpdatesAction softwareUpdatesAction = new HelpActions.SoftwareUpdatesAction();
	private final HelpActions.SupportAction supportAction = new HelpActions.SupportAction();
	private final HelpActions.SendFeedbackAction sendFeedbackAction = new HelpActions.SendFeedbackAction();
	private final HelpActions.GettingStartedAction gettingStartedAction = new HelpActions.GettingStartedAction();
	private final HelpActions.Vault3ForAndroidAction vault3ForAndroidAction = new HelpActions.Vault3ForAndroidAction();
	
	private TabFolder navigateAndSearchTabFolder;
	
	public TabFolder getNavigateAndSearchTabFolder() {
		return navigateAndSearchTabFolder;
	}

	private SearchUI searchUI;
	
	public SearchUI getSearchUI() {
		return searchUI;
	}
	
	private SashForm sashForm;
	
	public SashForm getSashForm() {
		return sashForm;
	}
	
	public MainApplicationWindow() {
		super(null);

		addActions();
		
		minimumSize = new Point(Globals.getPreferenceStore().getInt(PreferenceKeys.MainWindowMinimumWidth), 
				                Globals.getPreferenceStore().getInt(PreferenceKeys.MainWindowMinimumHeight));
		
		addMenuBar();
		addCoolBar(SWT.NONE);
		addStatusLine();

		Globals.setMainApplicationWindow(this);
		Globals.initializeImageRegistry();

		VaultDocument vaultDocument = new VaultDocument();
		Globals.setVaultDocument(vaultDocument);

		autoSaveRunnable = this::autoSave;

		checkForModificationsRunnable = this::checkForModifications;

		// Allow user to save changes when the operating system is being shut down.
		Display.getCurrent().addListener(SWT.Close, event -> {
            Globals.getLogger().info("handleEvent SWT.Close");

            boolean cancelled = saveCurrentDocument();

            Globals.getLogger().info(String.format("Cancelled: %b", cancelled));

            if (cancelled) {
                event.doit = false;
            }
            else {
                Globals.getLogger().info("System.exit(0)");

                // Avoid handing OS X.
                System.exit(0);
            }
        });
	}

	private void addActions() {
		final Action[] actionsArray = new Action[]
		{ 
			addAction, aboutAction, copyPictureFileAction, deletePictureFileAction, renamePictureFileAction,
			editPictureFileAction, rotateLeftFileAction, rotateRightFileAction, importPicturesAction,
			removeAction, moveUpAction, moveDownAction, indentAction, unindentAction, moveAction,
			outlineSelectAllAction, cutOutlineItemAction, copyOutlineItemAction, pasteOutlineItemAction, expandAction,
			expandAllAction, collapseAction, collapseAllAction, editOutlineAction, sortAction, outlineSetFontAction,
			undoAction, cutTextAction, copyTextAction, pasteTextAction, selectAllTextAction, insertDateAction,
			insertTimeAction, insertDateAndTimeAction, insertTextFileAction, setFontAction, goToWebsitesAction,
			findAction, replaceAction, insertUrlAction, displayUrlAction, nextSearchHitAction, previousSearchHitAction,
			nextSearchItemAction, previousSearchItemAction, clearSearchAction, softwareUpdatesAction, settingsAction
		};
		
		for (Action action : actionsArray) {
			actions.put(action.getClass(), action);
		}
	}
	
	private synchronized void autoSave() {
		Globals.getLogger().info("autoSave");

		// If Sync dialog is displayed, do nothing.
		if (!isCheckForModificationsWaiting()) {
			checkForModifications(false);
	
			// See if auto save was canceled after the timer was started.
			final int autoSaveMinutes = Globals.getPreferenceStore().getInt(PreferenceKeys.AutoSaveMinutes);
	
			if (autoSaveMinutes > 0) {
				if (Globals.getVaultDocument().getIsModified()) {
					Globals.getLogger().info("Saving changes to file.");
					
					try {
						VaultDocumentIO.fileSave(getShell());
					}
					catch (Throwable ex) {
						ex.printStackTrace();
					}
					
					getStatusLineManager().setMessage(MessageFormat.format("Automatically saving {0}.", Globals.getVaultDocument().getFileName()));
					Display.getCurrent().timerExec(Globals.getPreferenceStore().getInt(PreferenceKeys.StatusBarMessageDuration) * 1000, () -> getStatusLineManager().setMessage(StringLiterals.EmptyString));
				}
				else {
					Globals.getLogger().info("No changes to save.");
				}
	
				// Start another timer tick.
				startAutoSaveTimer();
			}
			else {
				Globals.getLogger().info("autoSave was cancelled.");
			}
		}
	}

	private void checkForModifications() {
		checkForModifications(true);
	}
	
	private synchronized void checkForModifications(boolean anotherTick) {
		Globals.getLogger().info("checkForModifications");
		
		if (!isCheckForModificationsWaiting()) {
			setCheckForModificationsWaiting(true);
			
			final String filePath = Globals.getVaultDocument().getFilePath();
	
			if (new File(filePath).exists()) {
				DocumentMetadata currentMetadata = new DocumentMetadata(filePath);
	
				boolean metadataChanged = Globals.getVaultDocument().getDocumentMetadata() != null && !currentMetadata.equals(Globals.getVaultDocument().getDocumentMetadata());
	
				Globals.getLogger().info(String.format("MainApplicationWindow.checkForModifications: metadataChanged: %s %s", filePath, metadataChanged));
	
				if (metadataChanged && !currentMetadata.equals(previousDocumentMetadata) /* Don't keep asking the user about the same change */) {
					SyncDialog syncDialog = new SyncDialog(getShell());
					syncDialog.open();
					
					previousDocumentMetadata = currentMetadata;
				}
			}
	
			if (anotherTick) {
				// Start another timer tick.
				startCheckForModificationsTimer();
			}
			
			setCheckForModificationsWaiting(false);
		}
	}
	
	public void startCheckForModificationsTimer() {
		final int checkForModificationsMinutes = Globals.getPreferenceStore().getInt(PreferenceKeys.CheckForModificationsMinutes);
		
		Globals.getLogger().info(String.format("startCheckForModificationsTimer: checkForModificationsMinutes = %s", checkForModificationsMinutes));
		
		if (checkForModificationsMinutes > 0) {
			Display.getCurrent().timerExec(checkForModificationsMinutes * 60 * 1000, checkForModificationsRunnable);
		}
		else {
			// Cancel the timer.
			Display.getCurrent().timerExec(-1, checkForModificationsRunnable);
		}
	}
	
	public void startAutoSaveTimer() {
		int autoSaveMinutes = Globals.getPreferenceStore().getInt(PreferenceKeys.AutoSaveMinutes);
		
		Globals.getLogger().info(String.format("startAutoSaveTimer: autoSaveMinutes = %s", autoSaveMinutes));
		
		if (autoSaveMinutes > 0) {
			Display.getCurrent().timerExec(autoSaveMinutes * 60 * 1000, autoSaveRunnable);
		}
		else {
			// Cancel the timer.
			Display.getCurrent().timerExec(-1, autoSaveRunnable);
		}
	}
	
	private void run() {
		Globals.getLogger().info("MainApplicationWindow.run: start");
		
		setBlockOnOpen(true);
		
		open();

		Globals.getLogger().info("MainApplicationWindow.run: after open");

		if (Display.getCurrent() != null && !Display.getCurrent().isDisposed()) {
			Globals.getLogger().info("MainApplicationWindow.run disposing display");

			Display.getCurrent().dispose();
		}

        Globals.getLogger().info("MainApplicationWindow.run finished");
        Globals.getLogger().info("MainApplicationWindow.run: System.exit(0)");
        Globals.closeLogFileHandler();

        // Need this to not interfere with system shutdown on OS X.
		System.exit(0);
	}

	protected StatusLineManager createStatusLineManager() {
		return new StatusLineManager();
	}
	
	public void createFileMenuItems() {
		fileMenuManager.removeAll();
		
		fileMenuManager.add(newAction);
		
		fileMenuManager.add(openAction);
		fileMenuManager.add(saveAction);
		fileMenuManager.add(saveAsAction);
		fileMenuManager.add(new Separator());
		fileMenuManager.add(passwordAction);
		fileMenuManager.add(new Separator());
		fileMenuManager.add(printAction);
		fileMenuManager.add(new Separator());

		final MenuManager importMenu = new MenuManager("&Import");
		importMenu.add(importFromVault3XMLFileAction);
		importMenu.add(importFromXMLFileAction);
		importMenu.add(importFromFileSystemAction);
		
		fileMenuManager.add(importMenu);
		
		final MenuManager exportMenu = new MenuManager("&Export");
		exportMenu.add(pdfExportAction);
		exportMenu.add(textExportAction);
		exportMenu.add(xmlExportAction);
		exportMenu.add(exportPhotosToDeviceAction);

		fileMenuManager.add(exportMenu);

		fileMenuManager.add(new Separator());
		fileMenuManager.add(copyPictureFileAction);
		fileMenuManager.add(deletePictureFileAction);
		fileMenuManager.add(renamePictureFileAction);
		
		fileMenuManager.add(new Separator());
		
		fileMenuManager.add(editPictureFileAction);

		fileMenuManager.add(new Separator());

		fileMenuManager.add(rotateLeftFileAction);
		fileMenuManager.add(rotateRightFileAction);

		fileMenuManager.add(new Separator());
		
		List<MRUFileList.MRUFile> mruFiles = Globals.getMRUFiles().getMRUFiles();
		
		int maxMRUFiles = Globals.getPreferenceStore().getInt(PreferenceKeys.MaxMRUFiles);
		
		if (!mruFiles.isEmpty()) {
			int itemsAdded = 0;
			
			for (int i = 0; i < mruFiles.size() && itemsAdded < maxMRUFiles; i++) {
				itemsAdded++;
				
				MRUFileList.MRUFile mruFile = mruFiles.get(i);

				String ampersand = itemsAdded <= 9 ? "&" : StringLiterals.EmptyString;
				
				String menuText = MessageFormat.format("{0}{1} - {2}", ampersand, itemsAdded, mruFile.getMenuText()); 
				MRUFileActions.MRUFileAction mruFileAction = new MRUFileActions.MRUFileAction(menuText, mruFile.getFilePath());
				fileMenuManager.add(mruFileAction);
			}
			
			fileMenuManager.add(new Separator());
		}
		
		fileMenuManager.add(exitAction);
	}
	
	protected MenuManager createMenuManager() {
		Globals.getMRUFiles().load();
		
		menuManager = new MenuManager();
		
		fileMenuManager = new MenuManager("&File");
		createFileMenuItems();
		
		menuManager.add(fileMenuManager);
		
		final MenuManager outlineMenuManager = new MenuManager("&Outline");
		outlineMenuManager.add(addAction);
		outlineMenuManager.add(editOutlineAction);
		outlineMenuManager.add(importPicturesAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(moveAction);
		outlineMenuManager.add(moveUpAction);
		outlineMenuManager.add(moveDownAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(indentAction);
		outlineMenuManager.add(unindentAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(expandAction);
		outlineMenuManager.add(expandAllAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(collapseAction);
		outlineMenuManager.add(collapseAllAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(sortAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(outlineSelectAllAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(cutOutlineItemAction);
		outlineMenuManager.add(copyOutlineItemAction);
		outlineMenuManager.add(pasteOutlineItemAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(removeAction);
		outlineMenuManager.add(new Separator());
		outlineMenuManager.add(outlineSetFontAction);
		menuManager.add(outlineMenuManager);

		editMenuManager = new MenuManager("&Edit");
		editMenuManager.add(undoAction);
		editMenuManager.add(new Separator());
		editMenuManager.add(cutTextAction);
		editMenuManager.add(copyTextAction);
		editMenuManager.add(pasteTextAction);
		editMenuManager.add(selectAllTextAction);
		editMenuManager.add(new Separator());
		editMenuManager.add(findAction);
		editMenuManager.add(replaceAction);
		editMenuManager.add(new Separator());
		editMenuManager.add(insertDateAction);
		editMenuManager.add(insertTimeAction);
		editMenuManager.add(insertDateAndTimeAction);
		editMenuManager.add(new Separator());
		editMenuManager.add(insertTextFileAction);
		editMenuManager.add(new Separator());
		editMenuManager.add(insertUrlAction);
		editMenuManager.add(displayUrlAction);
		editMenuManager.add(goToWebsitesAction);
		editMenuManager.add(new Separator());
		editMenuManager.add(setFontAction);
		menuManager.add(editMenuManager);
		
		final MenuManager photoMenuManager = new MenuManager("&Photo");
		photoMenuManager.add(copyPictureToClipboardAction);
		photoMenuManager.add(new Separator());
		photoMenuManager.add(copyPictureFileActionPhotoMenu);
		photoMenuManager.add(copyPictureFilePathAction);
		menuManager.add(photoMenuManager);
		
		final MenuManager viewMenuManager = new MenuManager("&View");
		viewMenuManager.add(slideShowAction);
		viewMenuManager.add(new Separator());
		viewMenuManager.add(switchBetweenOutlineAndTextAction);
		viewMenuManager.add(new Separator());
		viewMenuManager.add(maximizeOutlineAction);
		viewMenuManager.add(maximizeTextAndPhotosAction);
		viewMenuManager.add(viewOutlineAndTextPhotos);
		viewMenuManager.add(new Separator());
		viewMenuManager.add(moveVerticalSplitAction);
		viewMenuManager.add(moveHorizontalSplitAction);
		menuManager.add(viewMenuManager);
		
		final MenuManager searchMenuManager = new MenuManager("Sea&rch");
		searchMenuManager.add(searchAction);
		searchMenuManager.add(new Separator());
		searchMenuManager.add(nextSearchHitAction);
		searchMenuManager.add(previousSearchHitAction);
		searchMenuManager.add(new Separator());
		searchMenuManager.add(nextSearchItemAction);
		searchMenuManager.add(previousSearchItemAction);
		searchMenuManager.add(new Separator());
		searchMenuManager.add(clearSearchAction);
		menuManager.add(searchMenuManager);
		
		final MenuManager optionsMenuManager = new MenuManager("Opt&ions");
		optionsMenuManager.add(settingsAction);
		menuManager.add(optionsMenuManager);
		
		final MenuManager helpMenuManager = new MenuManager("&Help");
		helpMenuManager.add(helpTopicsAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(gettingStartedAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(visitOurWebsiteAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(softwareUpdatesAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(supportAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(sendFeedbackAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(vault3ForAndroidAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(aboutAction);
		menuManager.add(helpMenuManager);
		
		return menuManager;
	}
	
	protected ToolBarManager createToolBarManager(int style) {
		final ToolBarManager toolBarManager = new ToolBarManager(style);

		toolBarManager.add(newAction);
		toolBarManager.add(openAction);
		toolBarManager.add(saveAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(printAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(goToWebsitesAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(addAction);
		toolBarManager.add(editOutlineAction);
		toolBarManager.add(importPicturesAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(moveUpAction);
		toolBarManager.add(moveDownAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(indentAction);
		toolBarManager.add(unindentAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(moveAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(cutOutlineItemAction);
		toolBarManager.add(copyOutlineItemAction);
		toolBarManager.add(pasteOutlineItemAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(removeAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(expandAllAction);
		toolBarManager.add(collapseAllAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(sortAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(searchAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(nextSearchHitAction);
		toolBarManager.add(previousSearchHitAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(nextSearchItemAction);
		toolBarManager.add(previousSearchItemAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(slideShowAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(maximizeOutlineAction);
		toolBarManager.add(maximizeTextAndPhotosAction);
		toolBarManager.add(viewOutlineAndTextPhotos);
		toolBarManager.add(new Separator());
		toolBarManager.add(settingsAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(aboutAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(helpTopicsAction);
		
		return toolBarManager;
	}	

	protected CoolBarManager createCoolBarManager(int style) {
		final CoolBarManager coolBarManager = new CoolBarManager(style);

		coolBarManager.add(createToolBarManager(SWT.NONE));

		return coolBarManager;
	}

	public void notifyDocumentLoadUnloadListeners() {
		documentLoadUnloadListeners.forEach(ICustomListener::getNotification);
	}

	private void openFile() {
		// If a file was specified as an argument, open it.
		if (commandLineArgumentFilePath != null) {
			try {
				VaultDocumentIO.fileOpen(getShell(), commandLineArgumentFilePath);
				Globals.getMainApplicationWindow().getSearchUI().reset();
				vaultTreeViewer.selectFirstItem();
			}
			catch (Throwable ex) {
				boolean processedException = DatabaseVersionTooHigh.displayMessaging(ex, commandLineArgumentFilePath);

				if (!processedException) {
					final String message = MessageFormat.format("Cannot open file {2}.{0}{0}{1}",
							PortabilityUtils.getNewLine(),  ex.getMessage(), commandLineArgumentFilePath);

					final MessageDialog messageDialog =
							new MessageDialog(
									Globals.getMainApplicationWindow().getShell(),
									StringLiterals.ProgramName,
									Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON),
									message,
									MessageDialog.ERROR,
									new String[] { "&OK" },
									0);

					messageDialog.open();
				}

				ex.printStackTrace();
			}
		}
		else if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.LoadFileOnStartup)) {
			final String startupFilePath = Globals.getPreferenceStore().getString(PreferenceKeys.StarupFilePath);

			final File file = new File(startupFilePath);

			if (file.exists()) {
				try {
					VaultDocumentIO.fileOpen(getShell(), startupFilePath);
					Globals.getMainApplicationWindow().getSearchUI().reset();
					vaultTreeViewer.selectFirstItem();
				}
				catch (Throwable ex) {
					boolean processedException = DatabaseVersionTooHigh.displayMessaging(ex, startupFilePath);

					if (!processedException) {
						String message = MessageFormat.format("Cannot open file {2}.{0}{0}{1}", PortabilityUtils.getNewLine(),  ex.getMessage(), startupFilePath);
						MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON), message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
						messageDialog.open();
					}

					ex.printStackTrace();
				}
			}
			else {
				final String message = MessageFormat.format("Cannot open file {0}.", startupFilePath);

				final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);

				final MessageDialog messageDialog = new MessageDialog(getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();
			}
		}
		else if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.LoadMostRecentlyUsedFile)) {
			final String startupFilePath = Globals.getPreferenceStore().getString(PreferenceKeys.MostRecentlyUsedFilePath);

			final File file = new File(startupFilePath);

			if (file.exists()) {
				try {
					VaultDocumentIO.fileOpen(getShell(), startupFilePath);
					Globals.getMainApplicationWindow().getSearchUI().reset();
					vaultTreeViewer.selectFirstItem();
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected Control createContents(Composite parent) {
		getCoolBarManager().getControl().setBackground(parent.getBackground());

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		sashForm = new SashForm(composite, SWT.HORIZONTAL);

		sashForm.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				SashFormUtils.processSashFormKeypressedEvent(sashForm, keyEvent, previousFocusedControl, true);
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		navigateAndSearchTabFolder = new TabFolder(sashForm, SWT.NONE);
		
		final TabItem navigateTabItem = new TabItem(navigateAndSearchTabFolder, SWT.NONE);
		navigateTabItem.setText("&Navigate");
		
		searchUI = new SearchUI(navigateAndSearchTabFolder);

		final TabItem searchTabItem = new TabItem(navigateAndSearchTabFolder, SWT.NONE);
		searchTabItem.setText("&Search");
		searchTabItem.setControl(searchUI);
		
		vaultTreeViewer = new VaultTreeViewer(navigateAndSearchTabFolder, SWT.MULTI);
		vaultTreeViewer.setContentProvider(new VaultTreeContentProvider());
		vaultTreeViewer.setLabelProvider(new VaultLabelProvider());
		
		vaultTreeViewer.applyUserPreferences();
		vaultTreeViewer.load(Globals.getVaultDocument().getContent());

		notifyDocumentLoadUnloadListeners();
		
		navigateTabItem.setControl(vaultTreeViewer.getTree());
		
		Globals.setVaultTreeViewer(vaultTreeViewer);
		
		photoAndTextUI = new PhotoAndTextUI(sashForm);
		
		final ITextListener[] textListeners = new ITextListener[] {
				insertDateAction, insertTimeAction, insertDateAndTimeAction, undoAction, selectAllTextAction,
				findAction, replaceAction, insertUrlAction
		};
		
		for (ITextListener textListener : textListeners) {
			Globals.getVaultTextViewer().addTextListener(textListener);
		}

		final ISelectionChangedListener[] textSelectionChangedListeners = new ISelectionChangedListener[] {
				cutTextAction, copyTextAction, selectAllTextAction
		};
		
		for (ISelectionChangedListener textSelectionChangedListener : textSelectionChangedListeners) {
			Globals.getVaultTextViewer().addSelectionChangedListener(textSelectionChangedListener);
		}
		
		selectionChangedListeners = new ISelectionChangedListener[] 
		{ 
				Globals.getVaultTextViewer(), removeAction, indentAction, unindentAction, moveAction, expandAction,
				collapseAction, cutOutlineItemAction, copyOutlineItemAction, pasteOutlineItemAction, moveUpAction,
				moveDownAction, editOutlineAction, sortAction, insertTextFileAction, setFontAction, addAction,
				importPicturesAction, importFromXMLFileAction, importFromFileSystemAction, slideShowAction,
				searchAction, printAction, photoAndTextUI.getPhotoUI(), photoAndTextUI, textExportAction,
				xmlExportAction, pdfExportAction, outlineSelectAllAction, copyPictureFileAction,
				deletePictureFileAction, renamePictureFileAction, editPictureFileAction,
				rotateLeftFileAction, rotateRightFileAction,
				moveHorizontalSplitAction, exportPhotosToDeviceAction, importFromVault3XMLFileAction
		};
		
		for (ISelectionChangedListener selectionChangedListener : selectionChangedListeners) {
			vaultTreeViewer.addSelectionChangedListener(selectionChangedListener);
		}
		
		final ITreeViewerListener[] treeViewerListeners = new ITreeViewerListener[] {
				expandAction, expandAllAction, collapseAction, collapseAllAction
		};
		
		for (ITreeViewerListener treeViewerListener : treeViewerListeners) {
			vaultTreeViewer.addTreeListener(treeViewerListener);
		}
		
		final IDocumentLoadUnloadListener[] documentLoadListeners = new IDocumentLoadUnloadListener[] {
				expandAction, expandAllAction, collapseAction, collapseAllAction, slideShowAction, searchAction, outlineSelectAllAction 
		};
		
		for (IDocumentLoadUnloadListener documentLoadListener : documentLoadListeners) {
			addDocumentLoadListener(documentLoadListener);
		}

		final IPhotoListener[] photoListeners = new IPhotoListener[] {
				copyPictureToClipboardAction, 
				copyPictureFileActionPhotoMenu,
				copyPictureFilePathAction
		};
		
		for (IPhotoListener photoListener : photoListeners) {
			getPhotoAndTextUI().getPhotoUI().addListener(photoListener);
		}

		openFile();

		notifyDocumentLoadUnloadListeners();
		
		startAutoSaveTimer();
		startCheckForModificationsTimer();

		sashForm.setSashWidth(Globals.getPreferenceStore().getInt(PreferenceKeys.SashWidth));
		sashForm.setWeights(Globals.getPreferenceStore().getInt(PreferenceKeys.SashWidthLeft), Globals.getPreferenceStore().getInt(PreferenceKeys.SashWidthRight));

		expandAllAction.setEnabled();
		expandAction.setEnabled();
		collapseAllAction.setEnabled();
		collapseAction.setEnabled();
		printAction.setEnabled();

		// Make accelerators actually work: http://stackoverflow.com/questions/239744/menu-item-accel-key-works-only-after-menu-item-has-been-shown
		menuManager.updateAll(true);

		MenuUtils.armAllMenuItems(menuManager);

		editMenuManager.getMenu().addMenuListener(new MenuListener() {
			@Override
			public void menuHidden(MenuEvent e) {
			}

			@Override
			public void menuShown(MenuEvent e) {
				pasteTextAction.setEnabled();
				displayUrlAction.setEnabled(Globals.getVaultTextViewer().canDisplayUrl());
			}
		});
		
		enableToolBarStatusBarUpdates();
		
		processLicenseTerms();

		SoftwareUpdatesDialog.displayUpdatesDialogIfUpdatesAreAvailable(getShell());
		
		getShell().addListener(SWT.Deiconify, event -> {
            // Work-around for the following issue in Windows 7: When the app has focus on the tree, and is minimized for a few minutes,
            // when it's restored the expand/contract graphics on the tree will be invisible and will remain invisible until focus goes
            // away from the tree and comes back.

            Globals.getMainApplicationWindow().getShell().forceFocus();
        });
		
		FontListInitializer.initialize();
		
    	return null;
	}

	private void processLicenseTerms() {
		if (!Globals.getPreferenceStore().getBoolean(PreferenceKeys.AcceptLicenseTerms)) {
			final LicenseTermsDialog licenseTermsDialog = new LicenseTermsDialog(getShell());
			final int result = licenseTermsDialog.open();
			
			if (!Globals.getPreferenceStore().getBoolean(PreferenceKeys.AcceptLicenseTerms) || result != IDialogConstants.OK_ID) {
				close();
			}
		}
	}
	
	private void enableToolBarStatusBarUpdates() {
		final ToolBar toolBar = (ToolBar) getCoolBarManager().getControl().getItem(0).getControl();

		toolBar.addMouseMoveListener(e -> {
            ToolItem item = toolBar.getItem(new Point(e.x, e.y));

            if (item != null && item.getData() instanceof ActionContributionItem) {
                final ActionContributionItem actionContributionItem = (ActionContributionItem) item.getData();

                getStatusLineManager().setMessage(actionContributionItem.getAction().getDescription());
            }
        });
		
		toolBar.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(MouseEvent e) {
			}

			@Override
			public void mouseExit(MouseEvent e) {
				// Clear status bar message when user moves the mouse away from the toolbar.
	            getStatusLineManager().setMessage(StringLiterals.EmptyString);
			}

			@Override
			public void mouseHover(MouseEvent e) {
			}
		});
	}
	
	private void saveCurrentSizeAndPosition() {
		if (!getShell().getMinimized()) {
			final Rectangle rectangle = getShell().getBounds();
			Globals.getPreferenceStore().setValue(PreferenceKeys.MainWindowX, rectangle.x);
			Globals.getPreferenceStore().setValue(PreferenceKeys.MainWindowY, rectangle.y);
			Globals.getPreferenceStore().setValue(PreferenceKeys.MainWindowWidth, rectangle.width);
			Globals.getPreferenceStore().setValue(PreferenceKeys.MainWindowHeight, rectangle.height);

			Globals.getLogger().info(String.format("Saving window rectangle: x=%d y=%d w=%d h=%d",
					rectangle.x, rectangle.y, rectangle.width, rectangle.height));
		}

		final boolean isMaximized = getShell().getMaximized();

		Globals.getLogger().info(String.format("isMaximized: %b", isMaximized));

		Globals.getPreferenceStore().setValue(PreferenceKeys.IsMainWindowMaximized, isMaximized);
	}

	private void imposePreviousLocationAndSize(Shell shell) {
		Globals.getLogger().info("imposePreviousLocationAndSize");

		final Rectangle rectangle = new Rectangle(Globals.getPreferenceStore().getInt(PreferenceKeys.MainWindowX),
				Globals.getPreferenceStore().getInt(PreferenceKeys.MainWindowY),
				Globals.getPreferenceStore().getInt(PreferenceKeys.MainWindowWidth),
				Globals.getPreferenceStore().getInt(PreferenceKeys.MainWindowHeight));

		if (!(rectangle.x == -1 && rectangle.y == -1 && rectangle.width == -1 && rectangle.height == -1)) {
			rectangle.width = Math.max(rectangle.width, minimumSize.x);
			rectangle.height = Math.max(rectangle.height, minimumSize.y);

			Globals.getLogger().info(String.format("imposing window rectangle: x=%d y=%d w=%d h=%d",
					rectangle.x, rectangle.y, rectangle.width, rectangle.height));

			shell.setLocation(new Point(rectangle.x, rectangle.y));
			shell.setSize(new Point(rectangle.width, rectangle.height));
		}
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		
		shell.setImage(Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON));
		
		shell.setText(StringLiterals.ProgramName);

		shell.setMinimumSize(minimumSize);

		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.IsMainWindowMaximized)) {
			shell.setLocation(0, 0);
			shell.setSize(new Point(-1, -1));

			Globals.getLogger().info("shell.setMaximized(true);");
			shell.setMaximized(true);
		} else {
			imposePreviousLocationAndSize(shell);
		}

		shell.addListener(SWT.Move, event -> saveCurrentSizeAndPosition());
		shell.addListener(SWT.Resize, event -> saveCurrentSizeAndPosition());
	}
	
	public static void main(String[] args) {
		Globals.getLogger().info(String.format("Java version %s", System.getProperty("java.version")));

            Display.setAppName(StringLiterals.ProgramName);

		boolean singleInstance = !Globals.getPreferenceStore().getBoolean(PreferenceKeys.AllowMultipleInstances);

		boolean listeningToSocket = false;
		
		if (singleInstance) {
			singletonApplication = new SingletonApplication();
			listeningToSocket = singletonApplication.listenSocket();
		}
		
		if (!singleInstance || listeningToSocket) {
			MainApplicationWindow mainApplicationWindow = new MainApplicationWindow();
			
			// If a file was specified in the command argument list, retain it for opening.
			if (args.length > 0) {
				try {
					if (new File(args[0]).exists()) {
						mainApplicationWindow.commandLineArgumentFilePath = args[0];
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}

			mainApplicationWindow.run();
		}
		else {
			Globals.getLogger().info("this instance didn't get the socket, shutting down");
		}
	}
	
	public void setText(final String text) {
		// The shell may not exist yet, so defer this call until after the shell is created.

		Display.getCurrent().asyncExec(() -> {
            final Shell shell1 = getShell();

            if (shell1 != null) {
                getShell().setText(text);
            }
        });
	}
	
	private void addDocumentLoadListener(IDocumentLoadUnloadListener listener) {
		documentLoadUnloadListeners.add(listener);
	}
	
	/**
	 * Saves the current document if it has changed, and if the user chooses to save it.
	 * @return true if the save was cancelled
	 */
	public boolean saveCurrentDocument() {
		VaultDocument vaultDocument = Globals.getVaultDocument();
		
		boolean cancelled = false;
		
		if (vaultDocument.getIsModified()) {
			final String prompt = MessageFormat.format("Save changes to {0}?", vaultDocument.getFileName());
			
			final MessageDialog messageDialog = new MessageDialog(
					getShell(),
					StringLiterals.ProgramName,
					Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON),
					prompt,
					MessageDialog.QUESTION,
					new String[] { "&Yes", "&No", "&Cancel" },
					0);
			
			final int choice = messageDialog.open();
			
			cancelled = choice == 2;
			
			if (choice == 0) {
				try {
					Globals.setBusyCursor();
					
					try {
						Globals.getVaultTextViewer().saveChanges();
						
						try {
							vaultDocument.save();
						}
						catch (Throwable ex) {
							final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
							
							final String message = MessageFormat.format(
									"Cannot save {0}.{1}{1}{2}",
									vaultDocument.getFilePath(),
									PortabilityUtils.getNewLine(),
									ex.getMessage());

							final MessageDialog messageDialog2 = new MessageDialog(getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&Close" }, 0);
							messageDialog2.open();
						}
					}
					finally {
						Globals.setPreviousCursor();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		return cancelled;
	}
	
	@Override
	public boolean close() {
		boolean result = true;

		final int[] weights = sashForm.getWeights();
		Globals.getPreferenceStore().setValue(PreferenceKeys.SashWidthLeft, weights[0]);
		Globals.getPreferenceStore().setValue(PreferenceKeys.SashWidthRight, weights[1]);

		boolean cancelled = saveCurrentDocument();
		
		if (!cancelled) {
			if (singletonApplication != null) {
				singletonApplication.releaseSocket();
			}
			
			result = super.close();
		}

		return result;
	}
	
	/**
	 * Ensure that main window is visible and display a "this program is already running" message.
	 */
	public void displayAlreadyRunningMessage() {
		Globals.getLogger().info("Making main window visible");
		
		final Shell shell = getShell();
		
		if (shell.getMinimized()) {
			shell.setMinimized(false);
		}
		
		shell.forceActive();
		shell.forceFocus();

		if (alreadyRunningMessageBoxNestingDepth == 0 && Globals.getPreferenceStore().getBoolean(PreferenceKeys.WarnAboutSingleInstance)) {
			alreadyRunningMessageBoxNestingDepth++;
			
			final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);

			final String message = MessageFormat.format("{0} is already running\r\n\r\nTo suppress this message, select the Options / Settings menu item and go to the Instances tab.", StringLiterals.ProgramName);
			final MessageDialog messageDialog = new MessageDialog(getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.INFORMATION, new String[] { "&OK" }, 0);
			messageDialog.open();

			alreadyRunningMessageBoxNestingDepth--;
		}
		
		vaultTreeViewer.getTree().setFocus();
	}
}
