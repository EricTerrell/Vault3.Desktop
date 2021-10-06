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

package mainPackage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.perf4j.LoggingStopWatch;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import commonCode.Base64Coder;
import commonCode.DocumentMetadata;
import commonCode.VaultDocumentVersion;
import commonCode.VaultException;

import fonts.FontList;

public class VaultDocument {
	private final static String XML_ENCODING = "utf-8";
	
	private final static String VAULT_FILE_TYPE = ".vl3";

	private OutlineItem content;

	public OutlineItem getContent() {
		return content;
	}

	private String filePath;
	
	public String getFilePath() {
		return filePath;
	}
	
	private String defaultFilePath;
	
	public boolean hasDefaultFilePath() {
		return filePathsEqual(filePath, defaultFilePath);
	}

	private boolean filePathsEqual(String filePath, String defaultFilePath) {
		return filePath != null && defaultFilePath != null && filePath.equals(defaultFilePath);
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
		
		String caption = String.format("%s - %s", new File(filePath).getName(), StringLiterals.ProgramName);
		
		Globals.getMainApplicationWindow().setText(caption);
	}
	
	public String getFileName() {
		return new File(filePath).getName();
	}

	private String password;
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		boolean passwordChanged = false;
		
		if (password == null) {
			passwordChanged = this.password != null;
		}
		else {
			passwordChanged = !password.equals(this.password);
		}
		
		this.password = password;
		
		if (passwordChanged) {
			isModified = true;
		}
	}
	
	public boolean isEncrypted() {
		return this.password != null && this.password.length() > 0;
	}

	private VaultDocumentVersion vaultDocumentVersion = new VaultDocumentVersion();

	public VaultDocumentVersion getVaultDocumentVersion() {
		return vaultDocumentVersion;
	}

	public void setVaultDocumentVersion(VaultDocumentVersion vaultDocumentVersion) {
		this.vaultDocumentVersion = vaultDocumentVersion;
	}

	private boolean isModified;
	
	public boolean getIsModified() { 
		return isModified; 
	}
	
	public void setIsModified(boolean isModified) { 
		this.isModified = isModified; 
	}
	
	private DocumentMetadata documentMetadata;
	
	public DocumentMetadata getDocumentMetadata() {
		return documentMetadata;
	}

	public void setDocumentMetadata(DocumentMetadata documentMetadata) {
		this.documentMetadata = documentMetadata;
	}

	public void setContent(OutlineItem content) {
		this.content = content;
	}
	
	/**
	 * Writes the XML for the specified OutlineItem to the XMLStreamWriter
	 * @param outlineItem specified OutlineItem
	 * @param xmlStreamWriter XMLStreamWriter where XML is written
	 * @throws XMLStreamException
	 * @throws UnsupportedEncodingException 
	 */
	private void saveOutlineItem(OutlineItem outlineItem, XMLStreamWriter xmlStreamWriter) throws XMLStreamException, UnsupportedEncodingException {
		xmlStreamWriter.writeStartElement(NativeDefaultHandler.ITEMELEMENTNAME);
		
		xmlStreamWriter.writeStartElement(NativeDefaultHandler.TITLEELEMENTNAME);
		xmlStreamWriter.writeCharacters(Base64Coder.i18nEncode(outlineItem.getTitle()));
		xmlStreamWriter.writeEndElement();
		
		xmlStreamWriter.writeStartElement(NativeDefaultHandler.TEXTELEMENTNAME);
		
		RGB rgb = outlineItem.getRGB();
		
		if (rgb != null) {
			String rgbString = String.format("%d,%d,%d", rgb.red, rgb.green, rgb.blue);
			xmlStreamWriter.writeAttribute(NativeDefaultHandler.RGBATTRIBUTENAME, rgbString);
		}
		
		String fontListString = Base64Coder.i18nEncode(outlineItem.getFontListString());
		
		if (fontListString != null) {
			xmlStreamWriter.writeAttribute(NativeDefaultHandler.FONTLISTATTRIBUTENAME, fontListString);
		}
		
		xmlStreamWriter.writeCharacters(Base64Coder.i18nEncode(outlineItem.getText()));
		xmlStreamWriter.writeEndElement();
		
		String photoPath = Base64Coder.i18nEncode(outlineItem.getPhotoPath());
		
		if (photoPath != null) {
			xmlStreamWriter.writeStartElement(NativeDefaultHandler.PHOTOELEMENTNAME);
			
			xmlStreamWriter.writeAttribute(NativeDefaultHandler.PATHATTRIBUTENAME, photoPath);
			xmlStreamWriter.writeAttribute(NativeDefaultHandler.ALLOWSCALINGATTRIBUTENAME, outlineItem.getAllowScaling() ? NativeDefaultHandler.TRUEVALUE : NativeDefaultHandler.FALSEVALUE);
			
			xmlStreamWriter.writeEndElement();
		}
		
		for (OutlineItem childItem : outlineItem.getChildren()) {
			saveOutlineItem(childItem, xmlStreamWriter);
		}
		
		xmlStreamWriter.writeEndElement();
	}

	/**
	 * Renders the current document in XML, and writes it to the specified output stream.
	 * @param outputStream the XML is written to this stream
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void savePlaintextXmlToStream(OutputStream outputStream) throws XMLStreamException, IOException {
		Globals.getLogger().info("start");
		
		XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter = null;

        OutputStreamWriter outputStreamWriter = null;
        
        try {
        	outputStreamWriter = new OutputStreamWriter(outputStream, XML_ENCODING);
        	
	        xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStreamWriter);		
	        
	        xmlStreamWriter.writeStartDocument(XML_ENCODING, "1.0");
	        
	        xmlStreamWriter.writeStartElement(NativeDefaultHandler.VAULTELEMENTNAME);
	        xmlStreamWriter.writeAttribute(NativeDefaultHandler.VERSIONATTRIBUTENAME, VaultDocumentVersion.getLatestVaultDocumentVersion().toString());
	        xmlStreamWriter.writeAttribute(NativeDefaultHandler.BASE64ENCODEDATTRIBUTENAME, NativeDefaultHandler.TRUEVALUE);
	        
	        List<OutlineItem> children = Globals.getVaultTreeViewer().getSelectedItems();
	        
	        for (OutlineItem child : children) {
	            saveOutlineItem(child, xmlStreamWriter);
	        }
	        
	        xmlStreamWriter.writeEndElement();
	        xmlStreamWriter.writeEndDocument();
	        
	        xmlStreamWriter.flush();
	        outputStream.flush();
        }
        finally {
        	if (xmlStreamWriter != null) {
        		xmlStreamWriter.close();
        	}
        	
        	if (outputStreamWriter != null) {
        		outputStreamWriter.close();
        	}
        	
        	outputStream.close();
        }

		Globals.getLogger().info("end");
	}

	// Encrypt the byte array using the password as a key.
	private void encryptCleartextXmlToStream(ByteArrayOutputStream input, OutputStream outputStream) throws Exception {
		Globals.getLogger().info("start");

		byte[] cipherText = CryptoUtils.encrypt(password, input.toByteArray());
		
		XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlStreamWriter = null;
		
		final int itemMaxLength = 1024;
		
		try {
	        xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(new OutputStreamWriter(outputStream, XML_ENCODING));
	        
	        xmlStreamWriter.writeStartDocument(XML_ENCODING, "1.0");
	        
	        xmlStreamWriter.writeStartElement(NativeDefaultHandler.VAULTELEMENTNAME);
	        xmlStreamWriter.writeAttribute(NativeDefaultHandler.VERSIONATTRIBUTENAME, VaultDocumentVersion.getLatestVaultDocumentVersion().toString());
	        
	        xmlStreamWriter.writeStartElement(NativeDefaultHandler.ENCRYPTEDITEMS);
	        
	        int index = 0;

			// Pre-compute the hex values to save time.
			String[] hexValues = new String[256];
			
			byte b = Byte.MIN_VALUE;
			
			for (int i = 0; i < hexValues.length; i++) {
				hexValues[i] = String.format("%02x", b++);
			}
			
			while (index < cipherText.length) {
	        	xmlStreamWriter.writeStartElement(NativeDefaultHandler.ENCRYPTEDITEM);

	        	int segmentLength = Math.min(itemMaxLength, cipherText.length - index);

	        	byte[] segment = new byte[segmentLength];

				System.arraycopy(cipherText, index, segment, 0, segment.length);
	        	
	        	char[] base64EncodedChars = Base64Coder.encode(segment);
	        	String base64EncodedString = new String(base64EncodedChars);
	        	
	        	xmlStreamWriter.writeCharacters(base64EncodedString);
	        	
	        	xmlStreamWriter.writeEndElement();
	        	
	        	index += segmentLength;
	        }
	        
			xmlStreamWriter.writeEndElement();
	        xmlStreamWriter.writeEndElement();
	        xmlStreamWriter.writeEndDocument();
		}
		finally {
			if (xmlStreamWriter != null) {
				xmlStreamWriter.close();
			}
		}
		
		Globals.getLogger().info("end");
	}
	
	private String getRandomPlainText() {
		SecureRandom secureRandom = new SecureRandom();

		byte[] randomBytes = new byte[100];
		secureRandom.nextBytes(randomBytes);

		return new String(Base64Coder.encode(randomBytes));
	}
	
	/**
	 * Determine if the specified file is an SQLite database
	 * @param filePath file path
	 * @return true if the file is an SQLite database
	 * @throws IOException
	 */
	public static boolean isDatabase(String filePath) throws IOException {
		boolean isDatabase = false;

		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
			// http://www.sqlite.org/fileformat.html
			byte[] sqliteFileMarker = new byte[] { 0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00 };

			byte[] fileContents = new byte[sqliteFileMarker.length];
			
			int bytesRead = (fileInputStream.read(fileContents));
			
			if (bytesRead == fileContents.length) {
				isDatabase = Arrays.equals(sqliteFileMarker, fileContents);
			}
		}

		return isDatabase;
	}
	
	private static class OutlineItemWithIDs extends OutlineItem {
		private int id;
		
		public int getID() {
			return id;
		}

		public void setID(int id) {
			this.id = id;
		}

		private int parentID;

		public int getParentID() {
			return parentID;
		}

		public void setParentID(int parentID) {
			this.parentID = parentID;
		}
		
		private int sortOrder;

		public int getSortOrder() {
			return sortOrder;
		}

		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}
	}
	
	private static class OutlineItemWithIDsComparitor implements Comparator<OutlineItemWithIDs> {
		@Override
		public int compare(OutlineItemWithIDs outlineItem1, OutlineItemWithIDs outlineItem2) {
			return outlineItem1.getSortOrder() - outlineItem2.getSortOrder();
		}
	}
	
	public OutlineItem loadFromDatabase(Shell shell, String filePath, StringWrapper password) throws Exception {
		LoggingStopWatch stopwatch = new LoggingStopWatch("VaultDocument.loadFromDatabase");

		Globals.getLogger().info("Starting loadFromDatabase");
		
		SQLiteConnection db = null;

		Map<Integer, ArrayList<OutlineItemWithIDs>> map = new HashMap<>();

		try {
			db = new SQLiteConnection(new File(filePath));
			db.open();
			
			boolean isEncrypted = databaseIsEncrypted(db);
			
			if (isEncrypted) {
				String cipherText = getVaultDocumentInfo(db, "Ciphertext");
	
				boolean decrypted = false;
				
				if (password.getValue() != null) {
					try {
						Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password.getValue());
						CryptoUtils.decryptString(decryptionCipher, cipherText);
						decrypted = true;
						setPassword(password.getValue());
					}
					catch (Throwable ex) {
						Globals.getPasswordCache().remove(filePath);
						ex.printStackTrace();
					}
				}
	
				if (!decrypted) {
					CryptoGUIUtils.promptUserForPasswordAndDecrypt(filePath, cipherText, password);
					setPassword(password.getValue());
				}
			}

			Globals.setBusyCursor();

			VaultDocumentVersion vaultDocumentVersion = verifyVaultDocumentVersion(db);
			
			boolean dbVersion1_2OrLater = vaultDocumentVersion.compareTo(new VaultDocumentVersion(1, 2)) >= 0;
			
			String columns;
			
			if (dbVersion1_2OrLater) {
				columns = "ID, Title, Text, ParentID, SortOrder, Red, Green, Blue, PhotoPath, AllowScaling, FontList";
			}
			else {
				columns = "ID, Title, Text, ParentID, SortOrder, Red, Green, Blue, PhotoPath, AllowScaling";
			}
				
			String select = String.format("SELECT %s FROM OutlineItem", columns);
			
			SQLiteStatement statement = db.prepare(select);

			Cipher decryptionCipher = null;
			
			if (isEncrypted) {
				decryptionCipher = CryptoUtils.createDecryptionCipher(password.getValue());
			}
			
			while (statement.step()) {
				OutlineItemWithIDs outlineItem = new OutlineItemWithIDs();
				
				outlineItem.setID(statement.columnInt(0));
				
				outlineItem.setTitle(statement.columnString(1));
				outlineItem.setText(statement.columnString(2));
				
				if (isEncrypted) {
					outlineItem.setTitle(CryptoUtils.decryptString(decryptionCipher, outlineItem.getTitle()));
					outlineItem.setText(CryptoUtils.decryptString(decryptionCipher, outlineItem.getText()));
				}
				
				outlineItem.setParentID(statement.columnInt(3));
				outlineItem.setSortOrder(statement.columnInt(4));
				
				RGB rgb = new RGB(statement.columnInt(5), statement.columnInt(6), statement.columnInt(7));
				outlineItem.setRGB(rgb);
				
				outlineItem.setPhotoPath(statement.columnString(8));
				outlineItem.setAllowScaling(statement.columnInt(9) == 1);
				
				if (dbVersion1_2OrLater) {
					String fontListText = null;
					
					try {
						fontListText = statement.columnString(10);
						
						FontList fontList = FontList.deserialize(fontListText);
						outlineItem.setFontList(fontList);
					} catch (Throwable ex) {
						Globals.getLogger().severe(String.format("VaultDocument.loadFromDatabase: cannot deserialize FontList: %s", fontListText));
					}
				}
				
				int parentID = outlineItem.getParentID();
				
				ArrayList<OutlineItemWithIDs> arrayList = map.get(parentID);
				
				if (arrayList == null) {
					arrayList = new ArrayList<>();
					map.put(parentID, arrayList);
				}
				
				arrayList.add(outlineItem);
			}
		}
		finally {
			if (db != null) {
				db.dispose();
			}
			
			Globals.setPreviousCursor();
		}
		
		OutlineItem rootOutlineItem;

		try {
			Globals.setBusyCursor();
			
			for (ArrayList<OutlineItemWithIDs> arrayList : map.values()) {
				for (OutlineItemWithIDs outlineItem : arrayList) {
					// Retrieve all children for the current outline item.
					final ArrayList<OutlineItemWithIDs> children = map.get(outlineItem.getID());
					
					if (children != null) {
						final OutlineItemWithIDs[] sortedChildren = children.toArray(new OutlineItemWithIDs[0]);
						Arrays.sort(sortedChildren, new OutlineItemWithIDsComparitor());
						
						for (OutlineItemWithIDs child : sortedChildren) {
							outlineItem.addChild(child);
						}
					}
				}
			}
	
			rootOutlineItem = map.get(0).get(0);
		}
		finally {
			Globals.setPreviousCursor();
		}
		
		stopwatch.stop();
		
		return rootOutlineItem;
	}

	/**
	 * Throw an exception if the database version is higher than the code can cope with.
	 * @param db
	 * @throws Exception
	 */
	private VaultDocumentVersion verifyVaultDocumentVersion(SQLiteConnection db) throws Exception {
		VaultDocumentVersion codeVaultDocumentVersion = VaultDocumentVersion.getLatestVaultDocumentVersion();
		
		String dbVaultDocumentVersionString = getVaultDocumentInfo(db, "DocumentVersion");
		VaultDocumentVersion dbVaultDocumentVersion = new VaultDocumentVersion(dbVaultDocumentVersionString);
		
		if (dbVaultDocumentVersion.compareTo(codeVaultDocumentVersion) > 0) {
			throw new VaultException("Database version is too high", VaultException.ExceptionCode.DatabaseVersionTooHigh);
		}
		
		return dbVaultDocumentVersion;
	}
	
	private static String getVaultDocumentInfo(SQLiteConnection db, String name) throws SQLiteException {
		LoggingStopWatch stopwatch = new LoggingStopWatch("VaultDocument.getVaultDocumentInfo");

		String value = null;

		SQLiteStatement statement = db.prepare("SELECT Value FROM VaultDocumentInfo WHERE Name = ?");
		statement.bind(1, name);

		if (statement.step()) {
			value = statement.columnString(0);
		}
		
		stopwatch.stop();
		
		return value;
	}

	private static boolean databaseIsEncrypted(SQLiteConnection db) throws SQLiteException {
		String value = getVaultDocumentInfo(db, "Encrypted"); 

		return value.equals("1");
	}
	
	/**
	 * Save the current Vault document as an SQLite database. Document version is "1.1" in this case.
	 * @param filePath path of document
	 * @throws Throwable 
	 */
	public void saveAsSQLiteFile(String filePath) throws Throwable {
		String tempSaveFilePath = filePath + ".$$$";

		File tempSaveFile = new File(tempSaveFilePath);
		
		if (tempSaveFile.exists()) {
			FileUtils.deleteFile(tempSaveFilePath);
		}

		Cipher encryptionCipher = null;
		
		if (getPassword() != null) {
			encryptionCipher = CryptoUtils.createEncryptionCipher(getPassword());
		}
		
		SQLiteConnection db = new SQLiteConnection(tempSaveFile);
		 
		try {
		    db.open(true);

		    db.exec("BEGIN TRANSACTION");

		    String createTable = "CREATE TABLE \"android_metadata\" (\"locale\" TEXT DEFAULT 'en_US')";
		    db.exec(createTable);
		    
		    db.exec("INSERT INTO \"android_metadata\" VALUES ('en_US')");
		    
		    createTable = 
		    	"CREATE TABLE VaultDocumentInfo(" +
		    	"Name TEXT NOT NULL PRIMARY KEY, Value TEXT NOT NULL)";
		    db.exec(createTable);
		    
		    String maxVersion = VaultDocumentVersion.getLatestVaultDocumentVersion().toString();

		    db.exec(String.format("INSERT INTO VaultDocumentInfo(Name, Value) VALUES('DocumentVersion', '%s')", maxVersion));
		    db.exec(String.format("INSERT INTO VaultDocumentInfo(Name, Value) VALUES('Encrypted', '%d')", password == null ? 0 : 1));
		    
		    if (encryptionCipher != null) {
				SQLiteStatement insertStatement = db.prepare("INSERT INTO VaultDocumentInfo(Name, Value) VALUES(?, ?)");
				insertStatement.bind(1, "Ciphertext");
				
				String plainText = getRandomPlainText();
				String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);
				
				insertStatement.bind(2, cipherText);

				insertStatement.step();
				insertStatement.dispose();
		    }

		    createTable = 
		    	"CREATE TABLE OutlineItem(" +
		    	"ID INTEGER PRIMARY KEY, ParentID INTEGER, Title TEXT, Text TEXT, FontList TEXT," +
		    	"Red INTEGER DEFAULT 0, Green INTEGER DEFAULT 0, Blue INTEGER DEFAULT 0," +
		    	"PhotoPath TEXT, AllowScaling INTEGER DEFAULT 1, SortOrder INTEGER" +
		    	")";
		    			    
		    db.exec(createTable);

		    OutlineItem rootOutlineItem = Globals.getVaultDocument().getContent();
		    
		    String title = StringLiterals.EmptyString, text = StringLiterals.EmptyString;
		    
			if (encryptionCipher != null) {
				title = CryptoUtils.encryptString(encryptionCipher, title);
				text = CryptoUtils.encryptString(encryptionCipher, text);
			}
		    
		    // Insert root OutlineItem.
			SQLiteStatement insertStatement = db.prepare("INSERT INTO OutlineItem(Title, Text, ParentID, SortOrder) VALUES(?, ?, ?, ?)");
			insertStatement.bind(1, title);
			insertStatement.bind(2, text);
			insertStatement.bind(3, 0);
			insertStatement.bind(4, 0);

			insertStatement.step();
			insertStatement.dispose();
			
			int rootItemID = -1;
			
	    	SQLiteStatement statement = db.prepare("SELECT last_insert_rowid()");

	    	if (statement.step()) {
	    		rootItemID = statement.columnInt(0);
	    	}
	    	
	    	statement.dispose();

		    int sortOrder = 0;

	    	for (OutlineItem childOutlineItem : rootOutlineItem.getChildren()) {
		    	addToSQLiteDatabase(childOutlineItem, rootItemID, sortOrder++, db, encryptionCipher);
		    }
		    
		    String createIndex = "CREATE INDEX ParentIDIndex ON OutlineItem(ParentID)";
		    
		    db.exec(createIndex);
		    
		    db.exec("COMMIT");
		}
		finally {
			 db.dispose();
		}
		
		File newFile = new File(filePath);
		
		// If the destination file exists (for example, if the user has not chosen to save it as a .bak file, that file
		// must be deleted before renaming the temporary file to it.
		if (newFile.exists()) {
			Globals.getLogger().info(String.format("saveAsSQLiteFile: deleting file %s", newFile.getPath()));
			newFile.delete();
		}
		
		boolean renamed = tempSaveFile.renameTo(newFile);
		
		if (!renamed) {
			String errorMessage = MessageFormat.format("Cannot rename {0} to {1}.", tempSaveFile.getPath(), newFile.getPath());
			throw new VaultException(errorMessage);
		}
		
        setFilePath(filePath);
        isModified = false;
        
        if (isEncrypted()) {
			Globals.getPasswordCache().put(filePath, getPassword());
        }
		
		Globals.getMRUFiles().update(filePath, password);
	}
	
	private void addToSQLiteDatabase(OutlineItem outlineItem, int parentID, int sortOrder, SQLiteConnection db, Cipher encryptionCipher) throws SQLiteException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		SQLiteStatement insertStatement = db.prepare("INSERT INTO OutlineItem(Title, Text, ParentID, SortOrder, FontList, Red, Green, Blue, PhotoPath, AllowScaling) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		String title = outlineItem.getTitle();
		String text = outlineItem.getText();
		
		if (encryptionCipher != null) {
			title = CryptoUtils.encryptString(encryptionCipher, title);
			text = CryptoUtils.encryptString(encryptionCipher, text);
		}
		
		insertStatement.bind(1, title);
		insertStatement.bind(2, text);
		insertStatement.bind(3, parentID);
		insertStatement.bind(4, sortOrder);
		insertStatement.bind(5, outlineItem.getFontListString());

		RGB rgb = new RGB(0, 0, 0);
		
		if (outlineItem.getRGB() != null) {
			rgb = outlineItem.getRGB();
		}
		
		insertStatement.bind(6, rgb.red);
		insertStatement.bind(7, rgb.green);
		insertStatement.bind(8, rgb.blue);
		
		insertStatement.bind(9, outlineItem.getPhotoPath());
		insertStatement.bind(10, outlineItem.getAllowScaling() ? 1 : 0);
		
		insertStatement.step();
		insertStatement.dispose();
		
    	int id = 0;
    	
    	SQLiteStatement statement = db.prepare("SELECT last_insert_rowid()");

    	if (statement.step()) {
    		id = statement.columnInt(0);
    	}
    	
    	statement.dispose();
    	
    	int newSortOrder = 0;
    	
    	for (OutlineItem childOutlineItem : outlineItem.getChildren()) {
    		addToSQLiteDatabase(childOutlineItem, id, newSortOrder++, db, encryptionCipher);
    	}
	}

	/**
	 * Saves the current Vault document with a .bak file type if the user has chosen this option.
	 * @throws VaultException
	 */
	public static void saveOldFileWithBakType(String filePath) throws VaultException {
		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.SaveOldFileWithBakType)) {
			File originalFile = new File(filePath);
			
			if (originalFile.exists()) {
				String bakFilePath = String.format("%s.bak", filePath);
				
				File bakFile = new File(bakFilePath);
				
				// Remove previous .bak file if it exists.
				if (bakFile.exists()) {
					Globals.getLogger().info(String.format("saveOldFileWithBakType: deleting file %s", bakFile.getPath()));
					bakFile.delete();
				}
				
				// Rename file to {filepath}.bak
				Globals.getLogger().info(String.format("saveOldFileWithBakType: renaming file %s to %s", originalFile.getPath(), bakFile.getPath()));
				boolean renamed = originalFile.renameTo(bakFile);
				
				if (!renamed) {
					String errorMessage = MessageFormat.format("Cannot rename {0} to {1}.", originalFile.getAbsolutePath(), bakFile.getAbsolutePath());
					throw new VaultException(errorMessage);
				}
			}
		}
	}
	
	/**
	 * Saves the current document as an XML file. The file is written to a temporary file. If the temporary file is written
	 * successfully, it is renamed to the specified filePath
	 * @param filePath the document will be saved to this file path
	 * @throws Exception
	 */
	public void saveAsXMLFile(String filePath) throws Exception {
		String tempSaveFilePath = filePath + ".$$$";
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		savePlaintextXmlToStream(byteArrayOutputStream);
		
		FileOutputStream fileOutputStream = new FileOutputStream(tempSaveFilePath);

		if (!isEncrypted()) {
			fileOutputStream.write(byteArrayOutputStream.toByteArray());
		}
		else {
			ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
			encryptCleartextXmlToStream(byteArrayOutputStream, cipherText);

			// Need to write out the file in small pieces to avoid a java.io.IOException (Insufficient system resources exist to complete the requested service).
			final int maxSegmentLength = 1024 * 4;
			
	        int index = 0;
	        byte[] buffer = cipherText.toByteArray();

	        while (index < buffer.length) {
	        	int segmentLength = Math.min(maxSegmentLength, buffer.length - index);
	        	
	        	byte[] segment = new byte[segmentLength];

				System.arraycopy(buffer, index, segment, 0, segment.length);
	        	
	        	fileOutputStream.write(segment);
	        	
	        	index += segmentLength;
	        }
	        
	        Globals.getPasswordCache().put(filePath, password);
		}
		
		fileOutputStream.flush();
		fileOutputStream.close();

		File tempSaveFile = new File(tempSaveFilePath);
		File newFile = new File(filePath);
		
		// If the destination file exists (for example, if the user has not chosen to save it as a .bak file, that file
		// must be deleted before renaming the temporary file to it.
		if (newFile.exists()) {
			Globals.getLogger().info(String.format("saveAsXMLFile: deleting file %s", newFile.getPath()));
			newFile.delete();
		}
		
		boolean renamed = tempSaveFile.renameTo(newFile);
		
		if (!renamed) {
			String errorMessage = MessageFormat.format("Cannot rename {0} to {1}.", tempSaveFile.getPath(), newFile.getPath());
			throw new VaultException(errorMessage);
		}
	}
	
	/**
	 * Saves the current document.
	 * @throws Throwable 
	 */
	public void save() throws Throwable {
		saveOldFileWithBakType(filePath);

		saveAsSQLiteFile(filePath);
		
		DocumentMetadata documentMetadata = new DocumentMetadata(filePath);
		Globals.getVaultDocument().setDocumentMetadata(documentMetadata);
	}
	
	public VaultDocument() {
		content = new OutlineItem();
		isModified = false;
		final String vaultDefaultFilename = "Untitled";

		defaultFilePath = String.format("%s%s%s%s", System.getProperty("user.home"), PortabilityUtils.getFileSeparator(), vaultDefaultFilename, VAULT_FILE_TYPE);
		setFilePath(defaultFilePath);
	}
}
