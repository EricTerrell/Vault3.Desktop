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

import commonCode.Base64Utils;
import commonCode.DocumentMetadata;
import commonCode.VaultDocumentVersion;
import commonCode.VaultException;
import fonts.FontList;
import org.eclipse.swt.graphics.RGB;
import org.perf4j.LoggingStopWatch;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

public class VaultDocumentDatabasePersistence implements VaultDocumentPersistence {
    private final static String DOCUMENT_VERSION = "DocumentVersion";

    @Override
    public VaultDocument load(String filePath, StringWrapper password) throws Exception {
        VaultDocument vaultDocument;

        final String dbURL = getDBURL(filePath);

        Globals.getLogger().info(String.format("About to open database: \"%s\"", dbURL));

        try (final Connection db = DriverManager.getConnection(dbURL)) {
            vaultDocument = loadFromDatabase(db, filePath, password);
        }

        vaultDocument.setVaultDocumentVersion(VaultDocumentVersion.getLatestVaultDocumentVersion());

        if (password != null) {
            vaultDocument.setPassword(password.getValue());
        }

        vaultDocument.setIsModified(false);
        vaultDocument.setDocumentMetadata(new DocumentMetadata(filePath));

        vaultDocument.setFilePath(filePath);

        return vaultDocument;
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

    private static String getDBURL(String filePath) {
        return String.format("jdbc:sqlite:%s", filePath);
    }

    private VaultDocumentVersion getOriginalDocumentVersion(Connection db) throws SQLException {
        final String dbVaultDocumentVersionString = getVaultDocumentInfo(db, DOCUMENT_VERSION);

        return new VaultDocumentVersion(dbVaultDocumentVersionString);
    }

    private HashSet<String> getColumnNames(ResultSet resultSet) throws SQLException {
        final HashSet<String> columnNames = new HashSet<>();
        final ResultSetMetaData metadata = resultSet.getMetaData();

        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            columnNames.add(metadata.getColumnName(i));
        }

        return columnNames;
    }

    private VaultDocument loadFromDatabase(Connection db, String filePath, StringWrapper password) throws Exception {
        final LoggingStopWatch stopwatch = new LoggingStopWatch("VaultDocument.loadFromDatabase");

        VaultDocument vaultDocument = new VaultDocument();

        Globals.getLogger().info("Starting loadFromDatabase");

        final Map<Integer, ArrayList<OutlineItemWithIDs>> map = new HashMap<>();

        try {
            final VaultDocumentVersion vaultDocumentVersion = verifyVaultDocumentVersion(db);

            final boolean isEncrypted = databaseIsEncrypted(db);

            if (isEncrypted) {
                boolean decrypted = false;

                if (password.getValue() != null) {
                    try {
                        final String cipherText = getVaultDocumentInfo(db, StringLiterals.CipherText);
                        final String saltString = getVaultDocumentInfo(db,StringLiterals.Salt);
                        final String ivString = getVaultDocumentInfo(db,StringLiterals.IV);

                        byte[] salt = null, iv = null;

                        if (saltString != null && ivString != null) {
                            salt = Base64Utils.decode(saltString);
                            iv = Base64Utils.decode(ivString);
                        }

                        final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password.getValue(),
                                vaultDocumentVersion, salt, iv);

                        CryptoUtils.decryptString(decryptionCipher, cipherText);
                        decrypted = true;
                        vaultDocument.setPassword(password.getValue());
                    }
                    catch (Throwable ex) {
                        Globals.getPasswordCache().remove(filePath);
                        ex.printStackTrace();
                    }
                }

                if (!decrypted) {
                    CryptoGUIUtils.promptUserForPasswordAndDecrypt(db, filePath, password, vaultDocumentVersion);
                    vaultDocument.setPassword(password.getValue());
                }
            }

            Globals.setBusyCursor();

            final PreparedStatement statement = db.prepareStatement("SELECT * FROM OutlineItem");
            final ResultSet resultSet = statement.executeQuery();

            final HashSet<String> columnNames = getColumnNames(resultSet);

            while (resultSet.next()) {
                final OutlineItemWithIDs outlineItem = new OutlineItemWithIDs();

                outlineItem.setID(resultSet.getInt("ID"));

                if (isEncrypted) {
                    {
                        byte[] salt = null;
                        byte[] iv = null;

                        if (columnNames.contains("TitleSalt") && columnNames.contains("TitleIV")) {
                            final String saltString = resultSet.getString("TitleSalt");
                            final String ivString = resultSet.getString("TitleIV");

                            salt = Base64Utils.decode(saltString);
                            iv = Base64Utils.decode(ivString);
                        }

                        final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password.getValue(),
                                vaultDocumentVersion, salt, iv);

                        outlineItem.setTitle(CryptoUtils.decryptString(decryptionCipher,
                                resultSet.getString("Title")));
                    }

                    {
                        byte[] salt = null;
                        byte[] iv = null;

                        if (columnNames.contains("TextSalt") && columnNames.contains("TextIV")) {
                            final String saltString = resultSet.getString("TextSalt");
                            final String ivString = resultSet.getString("TextIV");

                            salt = Base64Utils.decode(saltString);
                            iv = Base64Utils.decode(ivString);
                        }

                        final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password.getValue(),
                                vaultDocumentVersion, salt, iv);

                        outlineItem.setText(CryptoUtils.decryptString(decryptionCipher,
                                resultSet.getString("Text")));
                    }
                } else {
                    outlineItem.setTitle(resultSet.getString("Title"));
                    outlineItem.setText(resultSet.getString("Text"));
                }

                outlineItem.setParentID(resultSet.getInt("ParentID"));
                outlineItem.setSortOrder(resultSet.getInt("SortOrder"));

                final RGB rgb = new RGB(resultSet.getInt("Red"), resultSet.getInt("Green"), resultSet.getInt("Blue"));
                outlineItem.setRGB(rgb);

                outlineItem.setPhotoPath(resultSet.getString("PhotoPath"));
                outlineItem.setAllowScaling(resultSet.getInt("AllowScaling") == 1);

                if (columnNames.contains("FontList")) {
                    String fontListText = null;

                    try {
                        fontListText = resultSet.getString("FontList");

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

            statement.close();
        }
        finally {
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

            rootOutlineItem = map.get(0).getFirst();
        }
        finally {
            Globals.setPreviousCursor();
        }

        stopwatch.stop();

        final VaultDocumentVersion originalVersion = getOriginalDocumentVersion(db);
        vaultDocument.setVaultDocumentOriginalVersion(originalVersion);

        vaultDocument.setContent(rootOutlineItem);

        return vaultDocument;
    }

    /**
     * Throw an exception if the database version is higher than the code can cope with.
     * @param db
     * @throws Exception
     */
    private static VaultDocumentVersion verifyVaultDocumentVersion(Connection db) throws Exception {
        final VaultDocumentVersion codeVaultDocumentVersion = VaultDocumentVersion.getLatestVaultDocumentVersion();

        final String dbVaultDocumentVersionString = getVaultDocumentInfo(db, DOCUMENT_VERSION);
        final VaultDocumentVersion dbVaultDocumentVersion = new VaultDocumentVersion(dbVaultDocumentVersionString);

        if (dbVaultDocumentVersion.compareTo(codeVaultDocumentVersion) > 0) {
            throw new VaultException("Database version is too high", VaultException.ExceptionCode.DatabaseVersionTooHigh);
        }

        return dbVaultDocumentVersion;
    }

    public static String getVaultDocumentInfo(Connection db, String name) throws SQLException {
        final LoggingStopWatch stopwatch = new LoggingStopWatch("VaultDocument.getVaultDocumentInfo");

        String value = null;

        final PreparedStatement statement = db.prepareStatement("SELECT Value FROM VaultDocumentInfo WHERE Name = ?");
        statement.setString(1, name);

        if (statement.execute()) {
            value = statement.getResultSet().getString(1);
        }

        statement.close();

        stopwatch.stop();

        return value;
    }

    private static boolean databaseIsEncrypted(Connection db) throws SQLException {
        final String value = getVaultDocumentInfo(db, "Encrypted");

        return value.equals("1");
    }

    @Override
    public void store(VaultDocument vaultDocument, String filePath) throws Throwable {
        VaultDocument.saveOldFileWithBakType(filePath);

        saveAsSQLiteFile(vaultDocument, filePath);

        DocumentMetadata documentMetadata = new DocumentMetadata(filePath);
        vaultDocument.setDocumentMetadata(documentMetadata);
    }

    /**
     * Save the current Vault document as an SQLite database. Document version is always the latest version.
     * @param filePath path of document
     * @throws Throwable
     */
    private void saveAsSQLiteFile(VaultDocument vaultDocument, String filePath) throws Throwable {
        if (!vaultDocument.warnAboutDocVersionUpgrade()) {
            throw new VaultException("User chose to not upgrade document version.");
        }

        // Don't want to keep warning the user every time a save is done.
        vaultDocument.setVaultDocumentOriginalVersion(vaultDocument.getVaultDocumentVersion());

        final String tempSaveFilePath = filePath + ".$$$";

        final File tempSaveFile = new File(tempSaveFilePath);

        if (tempSaveFile.exists()) {
            FileUtils.deleteFile(tempSaveFilePath);
        }

        final String dbURL = String.format("jdbc:sqlite:%s", tempSaveFile);

        Globals.getLogger().info(String.format("About to open database: \"%s\"", tempSaveFile));

        final boolean isEncrypted = vaultDocument.getPassword() != null;

        try (final Connection db = DriverManager.getConnection(dbURL)) {
            final Statement statement = db.createStatement();

            statement.execute("BEGIN TRANSACTION");

            String createTable = "CREATE TABLE \"android_metadata\" (\"locale\" TEXT DEFAULT 'en_US')";
            statement.execute(createTable);

            statement.execute("INSERT INTO \"android_metadata\" VALUES ('en_US')");

            createTable =
                    "CREATE TABLE VaultDocumentInfo(" +
                            "Name TEXT NOT NULL PRIMARY KEY, Value TEXT NOT NULL)";
            statement.execute(createTable);

            final String maxVersion = VaultDocumentVersion.getLatestVaultDocumentVersion().toString();

            statement.execute(String.format(
                    "INSERT INTO VaultDocumentInfo(Name, Value) VALUES('DocumentVersion', '%s')", maxVersion));
            statement.execute(String.format(
                    "INSERT INTO VaultDocumentInfo(Name, Value) VALUES('Encrypted', '%d')",
                    vaultDocument.getPassword() == null ? 0 : 1));

            if (isEncrypted) {
                // Save cipherText. Later, when user tries to open a document, the password can be verified by
                // attempting to decrypt the ciphertext.

                final PreparedStatement insertStatement =
                        db.prepareStatement("INSERT INTO VaultDocumentInfo(Name, Value) VALUES(?, ?)");

                final byte[] salt = CryptoUtils.createSalt();
                final byte[] iv = CryptoUtils.createIV();

                final Cipher encryptionCipher =
                        CryptoUtils.createEncryptionCipher(
                                vaultDocument.getPassword(),
                                vaultDocument.getVaultDocumentVersion(),
                                salt,
                                iv);

                final String cipherText = CryptoUtils.encryptString(encryptionCipher, getRandomPlainText());

                insertStatement.setString(1, StringLiterals.CipherText);
                insertStatement.setString(2, cipherText);
                insertStatement.execute();

                insertStatement.setString(1, StringLiterals.Salt);
                insertStatement.setString(2, Base64Utils.encodeToString(salt));
                insertStatement.execute();

                insertStatement.setString(1, StringLiterals.IV);
                insertStatement.setString(2, Base64Utils.encodeToString(iv));
                insertStatement.execute();

                insertStatement.close();
            }

            createTable =
                    "CREATE TABLE OutlineItem(" +
                            "ID INTEGER PRIMARY KEY, ParentID INTEGER, Title TEXT, TitleSalt TEXT, TitleIV Text, Text TEXT, TextSalt TEXT, TextIV TEXT, FontList TEXT," +
                            "Red INTEGER DEFAULT 0, Green INTEGER DEFAULT 0, Blue INTEGER DEFAULT 0," +
                            "PhotoPath TEXT, AllowScaling INTEGER DEFAULT 1, SortOrder INTEGER" +
                            ")";

            statement.execute(createTable);

            OutlineItem rootOutlineItem = vaultDocument.getContent();

            String title = StringLiterals.EmptyString,
                    titleSalt = StringLiterals.EmptyString,
                    titleIV = StringLiterals.EmptyString,
                    text = StringLiterals.EmptyString,
                    textSalt = StringLiterals.EmptyString,
                    textIV = StringLiterals.EmptyString;

            if (isEncrypted) {
                {
                    final byte[] salt = CryptoUtils.createSalt();
                    titleSalt = Base64Utils.encodeToString(salt);

                    final byte[] iv = CryptoUtils.createIV();
                    titleIV = Base64Utils.encodeToString(iv);

                    final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
                            vaultDocument.getPassword(),
                            vaultDocument.getVaultDocumentVersion(),
                            salt,
                            iv);

                    title = CryptoUtils.encryptString(encryptionCipher, title);
                }

                {
                    final byte[] salt = CryptoUtils.createSalt();
                    textSalt = Base64Utils.encodeToString(salt);

                    final byte[] iv = CryptoUtils.createIV();
                    textIV = Base64Utils.encodeToString(iv);

                    final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
                            vaultDocument.getPassword(),
                            vaultDocument.getVaultDocumentVersion(),
                            salt,
                            iv);

                    text = CryptoUtils.encryptString(encryptionCipher, text);
                }
            }

            // Insert root OutlineItem.
            final PreparedStatement insertStatement = db.prepareStatement("INSERT INTO OutlineItem(Title, TitleSalt, TitleIV, Text, TextSalt, TextIV, ParentID, SortOrder) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
            insertStatement.setString(1, title);
            insertStatement.setString(2, titleSalt);
            insertStatement.setString(3, titleIV);
            insertStatement.setString(4, text);
            insertStatement.setString(5, textSalt);
            insertStatement.setString(6, textIV);
            insertStatement.setInt(7, 0);
            insertStatement.setInt(8, 0);

            insertStatement.execute();
            insertStatement.close();

            int rootItemID = -1;

            if (statement.execute("SELECT last_insert_rowid()")) {
                rootItemID = statement.getResultSet().getInt(1);
            }

            int sortOrder = 0;

            for (OutlineItem childOutlineItem : rootOutlineItem.getChildren()) {
                addToSQLiteDatabase(vaultDocument, childOutlineItem, rootItemID, sortOrder++, db, isEncrypted);
            }

            final String createIndex = "CREATE INDEX ParentIDIndex ON OutlineItem(ParentID)";

            statement.execute(createIndex);

            statement.execute("COMMIT");

            statement.close();
        }

        final File newFile = new File(filePath);

        // If the destination file exists (for example, if the user has not chosen to save it as a .bak file, that file
        // must be deleted before renaming the temporary file to it.
        if (newFile.exists()) {
            Globals.getLogger().info(String.format("saveAsSQLiteFile: deleting file %s", newFile.getPath()));
            newFile.delete();
        }

        final boolean renamed = tempSaveFile.renameTo(newFile);

        if (!renamed) {
            final String errorMessage = MessageFormat.format("Cannot rename {0} to {1}.",
                    tempSaveFile.getPath(), newFile.getPath());
            throw new VaultException(errorMessage);
        }

        vaultDocument.setFilePath(filePath);
        vaultDocument.setIsModified(false);

        if (vaultDocument.isEncrypted()) {
            Globals.getPasswordCache().put(filePath, vaultDocument.getPassword());
        }

        Globals.getMRUFiles().update(filePath, vaultDocument.getPassword());
    }

    private void addToSQLiteDatabase(VaultDocument vaultDocument, OutlineItem outlineItem, int parentID,
                                     int sortOrder, Connection db, boolean isEncrypted)
            throws SQLException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        String title = outlineItem.getTitle();
        String text = outlineItem.getText();

        String titleSalt = StringLiterals.EmptyString,
                titleIV = StringLiterals.EmptyString,
                textSalt = StringLiterals.EmptyString,
                textIV = StringLiterals.EmptyString;

        if (isEncrypted) {
            {
                final byte[] salt = CryptoUtils.createSalt();
                titleSalt = Base64Utils.encodeToString(salt);

                final byte[] iv = CryptoUtils.createIV();
                titleIV = Base64Utils.encodeToString(iv);

                final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
                        vaultDocument.getPassword(),
                        vaultDocument.getVaultDocumentVersion(),
                        salt,
                        iv);

                title = CryptoUtils.encryptString(encryptionCipher, title);
            }

            {
                final byte[] salt = CryptoUtils.createSalt();
                textSalt = Base64Utils.encodeToString(salt);

                final byte[] iv = CryptoUtils.createIV();
                textIV = Base64Utils.encodeToString(iv);

                final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
                        vaultDocument.getPassword(),
                        vaultDocument.getVaultDocumentVersion(),
                        salt,
                        iv);

                text = CryptoUtils.encryptString(encryptionCipher, text);
            }
        }

        final PreparedStatement insertStatement = db.prepareStatement("INSERT INTO OutlineItem(Title, TitleSalt, TitleIV, Text, TextSalt, TextIV, ParentID, SortOrder, FontList, Red, Green, Blue, PhotoPath, AllowScaling) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        insertStatement.setString(1, title);
        insertStatement.setString(2, titleSalt);
        insertStatement.setString(3, titleIV);
        insertStatement.setString(4, text);
        insertStatement.setString(5, textSalt);
        insertStatement.setString(6, textIV);
        insertStatement.setInt(7, parentID);
        insertStatement.setInt(8, sortOrder);
        insertStatement.setString(9, outlineItem.getFontListString());

        RGB rgb = new RGB(0, 0, 0);

        if (outlineItem.getRGB() != null) {
            rgb = outlineItem.getRGB();
        }

        insertStatement.setInt(10, rgb.red);
        insertStatement.setInt(11, rgb.green);
        insertStatement.setInt(12, rgb.blue);

        insertStatement.setString(13, outlineItem.getPhotoPath());
        insertStatement.setInt(14, outlineItem.getAllowScaling() ? 1 : 0);

        insertStatement.execute();
        insertStatement.close();

        int id = 0;

        final PreparedStatement statement = db.prepareStatement("SELECT last_insert_rowid()");

        if (statement.execute()) {
            id = statement.getResultSet().getInt(1);
        }

        statement.close();

        int newSortOrder = 0;

        for (OutlineItem childOutlineItem : outlineItem.getChildren()) {
            addToSQLiteDatabase(vaultDocument, childOutlineItem, id, newSortOrder++, db, isEncrypted);
        }
    }

    private String getRandomPlainText() {
        final SecureRandom secureRandom = new SecureRandom();

        final byte[] randomBytes = new byte[100];
        secureRandom.nextBytes(randomBytes);

        return Base64Utils.encodeToString(randomBytes);
    }
}