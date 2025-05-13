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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import commonCode.Base64Coder;

import javax.crypto.Cipher;

public class SearchParameters implements Serializable {
	private static final long serialVersionUID = -3767549351231644670L;

	private String searchText;
	
	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	private boolean searchSelected;
	
	public boolean getSearchSelected() {
		return searchSelected;
	}

	public void setSearchSelected(boolean searchSelected) {
		this.searchSelected = searchSelected;
	}

	private boolean matchCase;
	
	public boolean getMatchCase() {
		return matchCase;
	}

	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}

	private boolean fullWord;
	
	public boolean getFullWord() {
		return fullWord;
	}

	public void setFullWord(boolean fullWord) {
		this.fullWord = fullWord;
	}

	private boolean matchAll;
	
	public boolean getMatchAll() {
		return matchAll;
	}

	public void setMatchAll(boolean matchAll) {
		this.matchAll = matchAll;
	}

	private Search.SearchMode searchMode;

	public Search.SearchMode getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(Search.SearchMode searchMode) {
		this.searchMode = searchMode;
	}
	
	public static String serialize(List<SearchParameters> searchParametersList, byte[] salt, byte[] iv) {
		String serializedText = StringLiterals.EmptyString;
		
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
	
			objectOutputStream.writeObject(searchParametersList);
			objectOutputStream.flush();
			
			byteArrayOutputStream.flush();

			byte[] serializedBytes = byteArrayOutputStream.toByteArray();

			if (Globals.getVaultDocument().isEncrypted()) {
				final Cipher cipher = CryptoUtils.createEncryptionCipher(Globals.getVaultDocument().getPassword(),
						Globals.getVaultDocument().getVaultDocumentVersion(), salt, iv);

				serializedBytes = CryptoUtils.encrypt(cipher, serializedBytes);
			}
			
			final char[] results = Base64Coder.encode(serializedBytes);
			
			byteArrayOutputStream.close();
			objectOutputStream.close();
			
			serializedText = new String(results);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return serializedText;
	}
	
	@SuppressWarnings("unchecked")
	public static List<SearchParameters> deserialize(String serializedText, byte[] salt,  byte[] iv) {
		List<SearchParameters> result = new ArrayList<>();

		try
		{
			byte[] serializedBytes = Base64Coder.decode(serializedText);
			
			if (Globals.getVaultDocument().isEncrypted()) {
				final Cipher cipher = CryptoUtils.createDecryptionCipher(
						Globals.getVaultDocument().getPassword(),
						Globals.getVaultDocument().getVaultDocumentVersion(),
						salt,
						iv);

				serializedBytes = CryptoUtils.decrypt(cipher, serializedBytes);
			}
			
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
			final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			
			result = (List<SearchParameters>) objectInputStream.readObject();
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return result;
	}

	@Override
	public String toString() {
		return searchText;
	}
}
