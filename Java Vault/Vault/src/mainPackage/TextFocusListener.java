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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

/**
 * @author Eric Bergman-Terrell
 * TextFocusListener is used to ensure that a Text control is selected when it gets focus. In other words, it's used to ensure
 * that at Text control works like Windows edit controls.
 */
public class TextFocusListener implements FocusListener {
	@Override
	public void focusGained(FocusEvent e) {
		final Text text = (Text) e.widget;
		text.selectAll();
	}

	@Override
	public void focusLost(FocusEvent e) {
		final Text text = (Text) e.widget;
		text.setSelection(0, 0);
	}
}
