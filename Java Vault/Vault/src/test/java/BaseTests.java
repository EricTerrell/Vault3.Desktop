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

import mainPackage.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Cursor;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseTests {
    @Mock
    private MainApplicationWindow mainApplicationWindow;

    @Mock
    private Shell shell;

    @Mock
    private Cursor cursor;

    @Mock
    protected VaultTextViewer vaultTextViewer;

    @Mock
    private StyledText textWidget;

    @Mock
    protected VaultTreeViewer vaultTreeViewer;

    @Before
    public void setUp() {
        mainApplicationWindow = mock(MainApplicationWindow.class);
        shell = mock(Shell.class);
        cursor = mock(Cursor.class);
        vaultTextViewer = mock(VaultTextViewer.class);
        textWidget = mock(StyledText.class);
        vaultTreeViewer = mock(VaultTreeViewer.class);

        when(shell.getCursor()).thenReturn(cursor);
        when(mainApplicationWindow.getShell()).thenReturn(shell);
        when(vaultTextViewer.getTextWidget()).thenReturn(textWidget);
        when(textWidget.getShell()).thenReturn(shell);

        Globals.setMainApplicationWindow(mainApplicationWindow);
        Globals.setVaultTextViewer(vaultTextViewer);
        Globals.setVaultTreeViewer(vaultTreeViewer);
    }
}