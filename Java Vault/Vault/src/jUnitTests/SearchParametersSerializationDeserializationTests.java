/*
  Vault 3
  (C) Copyright 2021, Eric Bergman-Terrell
  
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

package jUnitTests;

import java.io.IOException;

import org.junit.Assert;
import mainPackage.SearchParameters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class SearchParametersSerializationDeserializationTests {
	private static final String searchText = "this is some wonderful and long search text";
	
	@Test
	public void roundTrip() throws IOException, ClassNotFoundException {
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setSearchText(searchText);
		
		List<SearchParameters> searchParametersList = new ArrayList<>();
		
		for (int i = 0; i < 50; i++) {
			searchParametersList.add(searchParameters);
		}
		
		String serializedText = SearchParameters.serialize(searchParametersList);
		
		System.out.println(String.format("Length: %d Text: %s", serializedText.length(), serializedText));

		Assert.assertNotNull(serializedText);
		
		List<SearchParameters> deserializedList = SearchParameters.deserialize(serializedText);
		
		Assert.assertEquals(searchParametersList.size(), deserializedList.size());
	}
}
