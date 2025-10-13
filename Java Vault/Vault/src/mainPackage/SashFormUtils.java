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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;

public class SashFormUtils {
	public static void processSashFormKeypressedEvent(SashForm sashForm, KeyEvent e, Control previousFocusedControl, boolean vertical) {
		final int minMargin = 10;

		final int[] weights = sashForm.getWeights();
		int pixelsToMove = Globals.getPreferenceStore().getInt(PreferenceKeys.SplitMovePixels);
		
		if ((vertical && e.keyCode == SWT.ARROW_RIGHT) || (!vertical && e.keyCode == SWT.ARROW_DOWN)) {
			if ((weights[0] > 0 && weights[1] > 0) && (weights[1] - pixelsToMove >= minMargin)) {
				weights[0] += pixelsToMove;
				weights[1] -= pixelsToMove;
				
				sashForm.setWeights(weights);
			}
		}
		else if ((vertical && e.keyCode == SWT.ARROW_LEFT) || (!vertical && e.keyCode == SWT.ARROW_UP)) {
			if ((weights[0] > 0 && weights[1] > 0) && (weights[0] - pixelsToMove >= minMargin)) {
				weights[0] -= pixelsToMove;
				weights[1] += pixelsToMove;
				
				sashForm.setWeights(weights);
			}
		}
		else if (e.keyCode == SWT.ESC) {
			sashForm.setCapture(false);
			
			if (previousFocusedControl != null) {
				previousFocusedControl.forceFocus();
			}

			Globals.getMainApplicationWindow().setStatusLineMessage(StringLiterals.EmptyString);
		}
	}
}
