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

import commonCode.Base64Coder;
import mainPackage.Search.SearchMode;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class SearchUI extends Composite {
    private final String searchResultsLabelText = "Search Results:";

    private final Label searchResultsLabel;

    private final ListViewer listViewer;

    private final ComboViewer searchComboViewer;

    private Search.SearchResults searchResults;

    private final Button
            clearButton, allWordsRadioButton, atLeastOneWordRadioButton, matchWholeWordsOnlyCheckBox,
            searchAllRadioButton, searchSelectedRadioButton, matchCaseCheckBox, searchTitlesRadioButton,
            searchTitlesAndTextRadioButton;

    private void selectAllSearchText() {
        searchComboViewer.getCombo().setSelection(new Point(0, searchComboViewer.getCombo().getText().length()));
    }

    private void updateGuiForNewSearch() {
        final StructuredSelection structuredSelection = (StructuredSelection) searchComboViewer.getSelection();

        if (!structuredSelection.isEmpty()) {
            final SearchParameters searchParameters = (SearchParameters) structuredSelection.getFirstElement();

            searchComboViewer.getCombo().setText(searchParameters.getSearchText());
            selectAllSearchText();

            searchTitlesRadioButton.setSelection(searchParameters.getSearchMode() == SearchMode.titles);
            searchTitlesAndTextRadioButton.setSelection(searchParameters.getSearchMode() == SearchMode.titlesAndText);

            searchSelectedRadioButton.setSelection(searchParameters.getSearchSelected());
            searchAllRadioButton.setSelection(!searchParameters.getSearchSelected());

            matchCaseCheckBox.setSelection(searchParameters.getMatchCase());
            matchWholeWordsOnlyCheckBox.setSelection(searchParameters.getFullWord());

            allWordsRadioButton.setSelection(searchParameters.getMatchAll());
            atLeastOneWordRadioButton.setSelection(!searchParameters.getMatchAll());
        }
    }

    SearchUI(Composite parent) {
        super(parent, SWT.NONE);

        this.setLayout(new FillLayout());

        final Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        final Composite searchForComposite = new Composite(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.marginWidth = 0;
        searchForComposite.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalSpan = 2;
        searchForComposite.setLayoutData(gridData);

        final Label searchForLabel = new Label(searchForComposite, SWT.NONE);
        searchForLabel.setText("Se&arch For:");

        searchComboViewer = new ComboViewer(searchForComposite, SWT.NONE);
        gridData = new GridData(SWT.FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        searchComboViewer.getCombo().setLayoutData(gridData);

        final Image image = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_LIGHTBULB);

        final Label imageLabel = new Label(searchForComposite, SWT.NONE);
        imageLabel.setImage(image);

        imageLabel.setToolTipText(StringLiterals.SearchTextToolTip);

        final Button clearSearchHistoryButton = new Button(searchForComposite, SWT.PUSH);
        clearSearchHistoryButton.setText("Clear (&X)");

        clearSearchHistoryButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                searchComboViewer.getCombo().removeAll();
                searchComboViewer.getCombo().setText(StringLiterals.EmptyString);
                Globals.getPreferenceStore().setValue(getSavedSearchesKey(), StringLiterals.EmptyString);
                Globals.getPreferenceStore().setValue(getSavedSearchesSaltKey(), StringLiterals.EmptyString);
                Globals.getPreferenceStore().setValue(getSavedSearchesIVKey(), StringLiterals.EmptyString);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        final Group searchScopeGroup = new Group(composite, SWT.NONE);
        searchScopeGroup.setText("Search");
        searchScopeGroup.setLayout(new RowLayout(SWT.VERTICAL));

        searchAllRadioButton = new Button(searchScopeGroup, SWT.RADIO);
        searchAllRadioButton.setText("All Outline Ite&ms");

        searchSelectedRadioButton = new Button(searchScopeGroup, SWT.RADIO);
        searchSelectedRadioButton.setText("Selected O&utline Items");

        searchAllRadioButton.setSelection(true);
        searchSelectedRadioButton.setSelection(false);

        final Group searchTargetGroup = new Group(composite, SWT.NONE);
        searchTargetGroup.setText("Search");
        searchTargetGroup.setLayout(new RowLayout(SWT.VERTICAL));

        searchTitlesRadioButton = new Button(searchTargetGroup, SWT.RADIO);
        searchTitlesRadioButton.setText("&Titles");

        searchTitlesAndTextRadioButton = new Button(searchTargetGroup, SWT.RADIO);
        searchTitlesAndTextRadioButton.setText("Titles and Te&xt");

        searchTitlesRadioButton.setSelection(false);
        searchTitlesAndTextRadioButton.setSelection(true);

        final Group matchGroup = new Group(composite, SWT.NONE);
        matchGroup.setText("Match");
        matchGroup.setLayout(new RowLayout(SWT.VERTICAL));

        matchWholeWordsOnlyCheckBox = new Button(matchGroup, SWT.CHECK);
        matchWholeWordsOnlyCheckBox.setText("Whole Wor&d(s) Only");
        matchWholeWordsOnlyCheckBox.setSelection(false);

        matchCaseCheckBox = new Button(matchGroup, SWT.CHECK);
        matchCaseCheckBox.setText("&Case");
        matchCaseCheckBox.setSelection(false);

        final Group multipleWordSearchOptionsGroup = new Group(composite, SWT.NONE);
        multipleWordSearchOptionsGroup.setText("Must Find");
        multipleWordSearchOptionsGroup.setLayout(new RowLayout(SWT.VERTICAL));

        allWordsRadioButton = new Button(multipleWordSearchOptionsGroup, SWT.RADIO);
        allWordsRadioButton.setText("All &Words and Phrases");

        atLeastOneWordRadioButton = new Button(multipleWordSearchOptionsGroup, SWT.RADIO);
        atLeastOneWordRadioButton.setText("At &Least One Word or Phrase");

        allWordsRadioButton.setSelection(false);
        atLeastOneWordRadioButton.setSelection(true);

        final Composite searchClearComposite = new Composite(composite, SWT.NONE);
        searchClearComposite.setLayout(new GridLayout(2, false));

        final Button searchButton = new Button(searchClearComposite, SWT.PUSH);
        searchButton.setText("Search (&Y)");
        searchButton.setEnabled(false);

        getShell().setDefaultButton(searchButton);

        clearButton = new Button(searchClearComposite, SWT.PUSH);
        clearButton.setText("C&lear");
        clearButton.setEnabled(false);

        clearButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                clearSearch();
            }
        });

        searchComboViewer.getCombo().addModifyListener(e -> searchButton.setEnabled(!searchComboViewer.getCombo().getText().trim().isEmpty()));

        selectAllSearchText();

        searchComboViewer.getCombo().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateGuiForNewSearch();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        searchComboViewer.setContentProvider(new SearchParametersContentProvider());
        searchComboViewer.setLabelProvider(new ToStringLabelProvider());

        searchResultsLabel = new Label(composite, SWT.NONE);
        searchResultsLabel.setText(searchResultsLabelText);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = SWT.FILL;
        searchResultsLabel.setLayoutData(gridData);

        final Composite searchResultsComposite = new Composite(composite, SWT.NONE);
        gridData = new GridData(SWT.FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalSpan = 2;
        searchResultsComposite.setLayoutData(gridData);

        searchResultsComposite.setLayout(new FillLayout());

        listViewer = new ListViewer(searchResultsComposite);
        listViewer.setContentProvider(new SearchResultsContentProvider());
        listViewer.setLabelProvider(new ToStringLabelProvider());

        searchButton.addSelectionListener(new SelectionAdapter() {
            private SearchParameters getSearchParameters() {
                SearchMode searchMode = searchTitlesRadioButton.getSelection() ? SearchMode.titles : SearchMode.titlesAndText;

                SearchParameters searchParameters = new SearchParameters();
                searchParameters.setSearchText(searchComboViewer.getCombo().getText());
                searchParameters.setSearchSelected(searchSelectedRadioButton.getSelection());
                searchParameters.setMatchCase(matchCaseCheckBox.getSelection());
                searchParameters.setFullWord(matchWholeWordsOnlyCheckBox.getSelection());
                searchParameters.setMatchAll(allWordsRadioButton.getSelection());
                searchParameters.setSearchMode(searchMode);

                return searchParameters;
            }

            private void saveSearchParameters(SearchParameters searchParameters) {
                for (int i = searchComboViewer.getCombo().getItemCount() - 1; i >= 0; i--) {
                    final SearchParameters currentSearchParameters = (SearchParameters) searchComboViewer.getElementAt(i);

                    if (currentSearchParameters.getSearchText().trim().equals(searchParameters.getSearchText().trim())) {
                        searchComboViewer.remove(currentSearchParameters);
                    }
                }

                searchComboViewer.insert(searchParameters, 0);

                // Trim the list down to the specified maximum size.
                final int maxSavedSearches = Globals.getPreferenceStore().getInt(PreferenceKeys.MaxSavedSearches);

                while (searchComboViewer.getCombo().getItemCount() > maxSavedSearches) {
                    searchComboViewer.getCombo().remove(searchComboViewer.getCombo().getItemCount() - 1);
                }

                searchComboViewer.getCombo().setText(searchParameters.getSearchText());

                final ArrayList<SearchParameters> searchParametersList = new ArrayList<>();

                for (int i = 0; i < searchComboViewer.getCombo().getItemCount(); i++) {
                    SearchParameters currentSearchParameters = (SearchParameters) searchComboViewer.getElementAt(i);

                    searchParametersList.add(currentSearchParameters);
                }

                searchParametersList.trimToSize();

                final byte[] salt = CryptoUtils.createSalt();
                final byte[] iv = CryptoUtils.createIV();

                final String serializedText = SearchParameters.serialize(searchParametersList, salt, iv);

                Globals.getPreferenceStore().setValue(getSavedSearchesKey(), serializedText);
                Globals.getPreferenceStore().setValue(getSavedSearchesSaltKey(), new String(Base64Coder.encode(salt)));
                Globals.getPreferenceStore().setValue(getSavedSearchesIVKey(), new String(Base64Coder.encode(iv)));
            }

            public void widgetSelected(SelectionEvent e) {
                Globals.setBusyCursor();

                try {
                    Globals.getVaultTextViewer().saveChanges();
                    SearchParameters searchParameters = getSearchParameters();

                    searchResults = Search.DoSearch(searchParameters.getSearchText(), searchParameters.getSearchSelected(), searchParameters.getMatchCase(), searchParameters.getFullWord(), searchParameters.getMatchAll(), searchParameters.getSearchMode());

                    Globals.getVaultTextViewer().setSearchPatterns(searchResults.getPatterns(), !searchResults.getResults().isEmpty());

                    final String text = MessageFormat.format(
                            "Search Results ({0}) (&Z):", searchResults.getResults().size());
                    searchResultsLabel.setText(text);

                    listViewer.setInput(searchResults.getResults());
                    Globals.getVaultTextViewer().highlightSearchHits();

                    if (!searchResults.getResults().isEmpty()) {
                        listViewer.setSelection(new StructuredSelection(searchResults.getResults().get(0)));
                        listViewer.getList().forceFocus();
                        clearButton.setEnabled(true);
                        saveSearchParameters(searchParameters);
                    }
                } finally {
                    Globals.setPreviousCursor();
                }
            }
        });

        listViewer.addSelectionChangedListener(event -> {
            final ISelection selection = event.getSelection();
            Globals.getVaultTreeViewer().setSelection(selection, true);

            final int index = listViewer.getList().getSelectionIndex();
            final int count = listViewer.getList().getItemCount();

            (Globals.getMainApplicationWindow().getAction(SearchActions.NextSearchItemAction.class)).setEnabled(index < count - 1 && count > 0);
            (Globals.getMainApplicationWindow().getAction(SearchActions.PreviousSearchItemAction.class)).setEnabled(index > 0 && count > 0);
        });

        composite.pack();
    }

    private static String getKey(String prefix) {
        return String.format("SavedSearches_%s_%s", prefix, Globals.getVaultDocument().getFileName().trim().toLowerCase());
    }

    /**
     * Return a property file key for saved searches associated with the current document.
     *
     * @return saved search property key
     */
    private static String getSavedSearchesKey() {
        return getKey("searches");
    }

    /**
     * Return a property file key for the salt corresponding to saved searches associated with the current document.
     *
     * @return saved search property key
     */
    private static String getSavedSearchesSaltKey() {
        return getKey("salt");
    }

    /**
     * Return a property file key for the iv corresponding to saved searches associated with the current document.
     *
     * @return saved search property key
     */
    private static String getSavedSearchesIVKey() {
        return getKey("iv");
    }

    private void loadSavedSearches() {
        final String serializedText = Globals.getPreferenceStore().getString(getSavedSearchesKey());
        final String saltString = Globals.getPreferenceStore().getString(getSavedSearchesSaltKey());
        final String ivString = Globals.getPreferenceStore().getString(getSavedSearchesIVKey());

        final byte[] salt = Base64Coder.decode(saltString);
        final byte[] iv = Base64Coder.decode(ivString);

        List<SearchParameters> searchParametersList = SearchParameters.deserialize(serializedText, salt, iv);

        final int maxSavedSearches = Globals.getPreferenceStore().getInt(PreferenceKeys.MaxSavedSearches);
        searchComboViewer.getCombo().setVisibleItemCount(maxSavedSearches);

        searchComboViewer.setInput(searchParametersList);

        if (searchComboViewer.getCombo().getItemCount() > 0) {
            searchComboViewer.getCombo().select(0);
            updateGuiForNewSearch();
        }
    }

    public void prepareToSearch() {
        selectAllSearchText();
        searchComboViewer.getCombo().setSelection(new Point(0, searchComboViewer.getCombo().getText().length()));
        searchComboViewer.getCombo().setFocus();
    }

    private boolean canGoToNextSearchTopic() {
        return listViewer.getList().getSelectionIndex() >= 0 && listViewer.getList().getSelectionIndex() < listViewer.getList().getItemCount() - 1;
    }

    public void goToNextSearchTopic() {
        if (canGoToNextSearchTopic()) {
            Globals.getVaultTextViewer().getTextWidget().forceFocus();
            listViewer.setSelection(new StructuredSelection(searchResults.getResults().get(listViewer.getList().getSelectionIndex() + 1)));
        }
    }

    private boolean canGoToPreviousSearchTopic() {
        return listViewer.getList().getSelectionIndex() > 0 && listViewer.getList().getItemCount() > 0;
    }

    public void goToPreviousSearchTopic() {
        if (canGoToPreviousSearchTopic()) {
            Globals.getVaultTextViewer().getTextWidget().forceFocus();
            listViewer.setSelection(new StructuredSelection(searchResults.getResults().get(listViewer.getList().getSelectionIndex() - 1)));
        }
    }

    public void reset() {
        searchResultsLabel.setText(searchResultsLabelText);
        listViewer.setInput(new ArrayList<OutlineItem>());

        loadSavedSearches();
    }

    public void clearSearch() {
        searchResults.getResults().clear();
        searchResultsLabel.setText(searchResultsLabelText);

        listViewer.setInput(null);
        Globals.getVaultTextViewer().setSearchPatterns(null, false);
        Globals.getVaultTextViewer().highlightSearchHits();

        clearButton.setEnabled(false);
        (Globals.getMainApplicationWindow().getAction(SearchActions.ClearSearchAction.class)).setEnabled(false);
    }
}
