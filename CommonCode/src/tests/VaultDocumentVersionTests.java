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

package tests;

import org.junit.Assert;
import org.junit.Test;

import commonCode.VaultDocumentVersion;

public class VaultDocumentVersionTests {

	@Test
	public void testConstructor() {
		VaultDocumentVersion vaultDocumentVersion = new VaultDocumentVersion();
		Assert.assertEquals("1.3", "1.0", vaultDocumentVersion.toString());
		Assert.assertEquals(1, vaultDocumentVersion.getMajorVersion());
		Assert.assertEquals(0, vaultDocumentVersion.getMinorVersion());
		
		vaultDocumentVersion = new VaultDocumentVersion(1, 3);
		Assert.assertEquals("1.3", "1.3", vaultDocumentVersion.toString());
		Assert.assertEquals(1, vaultDocumentVersion.getMajorVersion());
		Assert.assertEquals(3, vaultDocumentVersion.getMinorVersion());
		
		vaultDocumentVersion = new VaultDocumentVersion(2, 7);
		Assert.assertEquals("2.7", vaultDocumentVersion.toString());
		Assert.assertEquals(2, vaultDocumentVersion.getMajorVersion());
		Assert.assertEquals(7, vaultDocumentVersion.getMinorVersion());
	}

	@Test
	public void testGreaterThan() {
		VaultDocumentVersion vaultDocumentVersion_1_1 = new VaultDocumentVersion(1, 1);
		VaultDocumentVersion vaultDocumentVersion_1_2 = new VaultDocumentVersion(1, 2);
		
		Assert.assertTrue(vaultDocumentVersion_1_2.compareTo(vaultDocumentVersion_1_1) > 0);
	}

	@Test
	public void testLessThan() {
		VaultDocumentVersion vaultDocumentVersion_1_1 = new VaultDocumentVersion(1, 1);
		VaultDocumentVersion vaultDocumentVersion_1_2 = new VaultDocumentVersion(1, 2);
		
		Assert.assertTrue(vaultDocumentVersion_1_1.compareTo(vaultDocumentVersion_1_2) < 0);
	}
	
	@Test
	public void testEqual() {
		VaultDocumentVersion vaultDocumentVersion_1_1  = new VaultDocumentVersion(1, 1);
		VaultDocumentVersion vaultDocumentVersion_1_1b = new VaultDocumentVersion(1, 1);

		Assert.assertEquals(0, vaultDocumentVersion_1_1b.compareTo(vaultDocumentVersion_1_1));
	}
	
	@Test
	public void testGetLatest() {
		VaultDocumentVersion latest = VaultDocumentVersion.getLatestVaultDocumentVersion();
		Assert.assertEquals(1, latest.getMajorVersion());
		Assert.assertEquals(3, latest.getMinorVersion());
	}
}
