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

import java.net.URI;
import java.awt.Desktop;

public class EmailUI {
	public static boolean canEmailFeedback() {
		return true;
	}

	public static void emailFeedback() {
		try {
			Desktop.getDesktop().mail(new URI("mailto:Vault3@EricBT.com?subject=Vault%203%20Feedback"));
		} catch (Exception ex) {
			Globals.getLogger().info(String.format("EmailUI.emailFeedback: Exception %s", ex));
		}
	}
}
