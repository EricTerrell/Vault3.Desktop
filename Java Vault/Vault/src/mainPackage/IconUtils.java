/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 */
public class IconUtils {
    public static ImageDescriptor getTransparentImageDescriptor(String imagePath, RGB transparentColor) {
        ImageData iconImageData = new ImageData(MainApplicationWindow.class.getResourceAsStream(imagePath));

        iconImageData.transparentPixel = iconImageData.palette.getPixel(transparentColor);

        Image iconImage = new Image(Display.getCurrent(), iconImageData);

        return ImageDescriptor.createFromImage(iconImage);
    }
}
