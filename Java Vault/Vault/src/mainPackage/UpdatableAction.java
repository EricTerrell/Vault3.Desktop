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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class UpdatableAction extends Action {
    private final String fileName;

    public UpdatableAction(String text, String fileName) {
        super(text);

        this.fileName = fileName;

        updateImage();
    }

    public void updateImage() {
        Globals.getLogger().info(String.format("updateImage fileName: \"%s\"", fileName));

        var oldImageDescriptor = getImageDescriptor();
        var newImageDescriptor = ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream(getResourcePath(fileName))));

        setImageDescriptor(newImageDescriptor);

        // Update UI
        firePropertyChange(IAction.IMAGE, oldImageDescriptor, newImageDescriptor);

        Globals.getLogger().info("updateImage completed");
    }

    private static String getResourcePath(String filename) {
        final var darkModeFolder = Globals.getPreferenceStore().getBoolean(PreferenceKeys.UseDarkModeIcons) ?
                "/dark_mode" : StringLiterals.EmptyString;

        var result = String.format("/resources%s/%s", darkModeFolder, filename);

        Globals.getLogger().info(String.format("getResourcePath: result: \"%s\"", result));

        return result;
    }
}
