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

import java.io.File;
import java.io.IOException;

public class DocumentMetadata {
	private long length;
	
	public long getLength() {
		return length;
	}

	private long lastModified;
	
	public long getLastModified() {
		return lastModified;
	}
	
	private String canonicalPath;
	
	public String getCanonicalPath() {
		return canonicalPath;
	}

	public DocumentMetadata(String filePath) {
		File file = new File(filePath);
		
		length = file.length();
		lastModified = file.lastModified();

		try {
			canonicalPath = file.getCanonicalPath();
		}
		catch (IOException ex) {
			/* empty */
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;

		if (obj instanceof DocumentMetadata) {
			DocumentMetadata other = (DocumentMetadata) obj;
			
			equals = length == other.length && lastModified == other.lastModified && canonicalPath.equals(other.canonicalPath);
		}
		
		return equals;
	}
}
