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

package commonCode;

import java.util.Objects;

public class VaultDocumentVersion implements Comparable<VaultDocumentVersion> {
	public static final VaultDocumentVersion VERSION_1_0 = new VaultDocumentVersion(1, 0);
	public static final VaultDocumentVersion VERSION_1_1 = new VaultDocumentVersion(1, 1);
	public static final VaultDocumentVersion VERSION_1_2 = new VaultDocumentVersion(1, 2);
	public static final VaultDocumentVersion VERSION_1_3 = new VaultDocumentVersion(1, 3);

	public VaultDocumentVersion() {
		majorVersion = 1;
		minorVersion = 0;
	}
	
	public VaultDocumentVersion(String version) {
		String[] majorMinorVersionString = version.split("\\.");
		
		majorVersion = Integer.parseInt(majorMinorVersionString[0]);
		minorVersion = Integer.parseInt(majorMinorVersionString[1]);
	}

	public VaultDocumentVersion(int majorVersion, int minorVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	private int majorVersion;

	public int getMajorVersion() {
		return majorVersion;
	}

	private int minorVersion;

	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Returns the latest Vault 3 document version.
	 * @return
	 * Latest Vault 3 document version
	 */
	public static VaultDocumentVersion getLatestVaultDocumentVersion() {
		return VERSION_1_3;
	}
	
	@Override
	public String toString() {
		return String.format("%d.%d", majorVersion, minorVersion);
	}

	@Override
	public int compareTo(VaultDocumentVersion otherObject) {
		int result;

		if (getMajorVersion() != otherObject.getMajorVersion()) {
			result = getMajorVersion() - otherObject.getMajorVersion();
		} else {
			result = getMinorVersion() - otherObject.getMinorVersion();
		}

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		final VaultDocumentVersion that = (VaultDocumentVersion) o;

		return majorVersion == that.majorVersion && minorVersion == that.minorVersion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(majorVersion, minorVersion);
	}
}
