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

import commonCode.VaultDocumentVersion;
import mainPackage.*;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SearchParametersSerializationDeserializationTests extends BaseTests {
	private static final String searchText = "this is some wonderful and long search text";
	
	@Test
	public void roundTrip() {
		final VaultDocument vaultDocument = new VaultDocument();
		vaultDocument.setVaultDocumentVersion(VaultDocumentVersion.getLatestVaultDocumentVersion());
		vaultDocument.setPassword("PASSWORD");

		Globals.setVaultDocument(vaultDocument);

		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setSearchText(searchText);
		
		final List<SearchParameters> searchParametersList = new ArrayList<>();
		
		for (int i = 0; i < 50; i++) {
			searchParametersList.add(searchParameters);
		}

		final byte[] salt = CryptoUtils.createSalt();
		final byte[] iv = CryptoUtils.createIV();

		final String serializedText = SearchParameters.serialize(searchParametersList, salt, iv);
		
		System.out.printf("Length: %d Text: %s%n", serializedText.length(), serializedText);

		Assert.assertNotNull(serializedText);
		
		final List<SearchParameters> deserializedList = SearchParameters.deserialize(serializedText, salt, iv);
		
		Assert.assertEquals(searchParametersList.size(), deserializedList.size());
	}
}
