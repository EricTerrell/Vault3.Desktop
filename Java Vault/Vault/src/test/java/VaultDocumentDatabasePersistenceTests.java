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

package test.java;

import commonCode.VaultDocumentVersion;
import commonCode.VaultException;
import mainPackage.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class VaultDocumentDatabasePersistenceTests extends BaseTests {
    private final StringWrapper password = new StringWrapper("PASSWORD");

    private final String EXAMPLE_FILENAME_DATABASE_UNENCRYPTED = "example.vl3";
    private final String EXAMPLE_FILENAME_XML_UNENCRYPTED = "example.xml";

    private final String EXAMPLE_FILENAME_DATABASE_ENCRYPTED = "example-encrypted.vl3";
    private final String EXAMPLE_FILENAME_XML_ENCRYPTED = "example-encrypted.xml";

    @Test
    public void testLoadDatabaseUnencryptedDocument() throws Exception {
        final String filePath = TestUtils.getResourceFilePath(EXAMPLE_FILENAME_DATABASE_UNENCRYPTED);

        var vaultDocument =
                new VaultDocumentDatabasePersistence().load(filePath, null);

        validateDocument(vaultDocument, filePath, VaultDocumentVersion.VERSION_1_3);
    }

    @Test
    public void testLoadXMLUnencryptedDocument() throws Throwable {
        final String filePath = TestUtils.getResourceFilePath(EXAMPLE_FILENAME_XML_UNENCRYPTED);

        var vaultDocument =
                new VaultDocumentXMLPersistence().load(filePath, null);

        validateDocument(vaultDocument, filePath, VaultDocumentVersion.VERSION_1_0);
    }

    @Test
    public void testLoadDatabaseEncryptedDocument() throws Exception {
        final String filePath = TestUtils.getResourceFilePath(EXAMPLE_FILENAME_DATABASE_ENCRYPTED);

        var vaultDocument =
                new VaultDocumentDatabasePersistence().load(filePath, password);

        validateDocument(vaultDocument, filePath, VaultDocumentVersion.VERSION_1_3);
    }

    @Test
    public void testLoadXMLEncryptedDocument() throws Throwable {
        final String filePath = TestUtils.getResourceFilePath(EXAMPLE_FILENAME_XML_ENCRYPTED);

        var vaultDocument =
                new VaultDocumentXMLPersistence().load(filePath, password);

        validateDocument(vaultDocument, filePath, VaultDocumentVersion.VERSION_1_0);
    }

    private void validateDocument(VaultDocument vaultDocument, String filePath,
                                  VaultDocumentVersion expectedOriginalVersion) throws IOException {
        Assert.assertEquals(
                vaultDocument.getVaultDocumentVersion(), VaultDocumentVersion.getLatestVaultDocumentVersion());
        Assert.assertEquals(
                expectedOriginalVersion, vaultDocument.getVaultDocumentOriginalVersion());

        Assert.assertFalse(vaultDocument.getIsModified());
        Assert.assertEquals(filePath, vaultDocument.getFilePath());

        validateDocumentMetadata(vaultDocument, filePath);

        Assert.assertEquals("Computer", vaultDocument.getContent().getChildren().get(0).getTitle());
        Assert.assertEquals("General computer notes go here.", vaultDocument.getContent().getChildren().get(0).getText());

        Assert.assertEquals("Phonebook", vaultDocument.getContent().getChildren().get(3).getTitle());
        Assert.assertEquals("MCI Calling Card #: 17-4455-4455", vaultDocument.getContent().getChildren().get(3).getText());
    }

    private void validateDocumentMetadata(VaultDocument vaultDocument, String filePath) throws IOException {
        final var fileInfo = new File(filePath);

        Assert.assertEquals(fileInfo.length(), vaultDocument.getDocumentMetadata().getLength());
        Assert.assertEquals(fileInfo.lastModified(), vaultDocument.getDocumentMetadata().getLastModified());
        Assert.assertEquals(fileInfo.getCanonicalPath(), vaultDocument.getDocumentMetadata().getCanonicalPath());
    }

    @Test
    public void testXMLAndDatabaseRoundTrip() throws Throwable {
        final String exampleFilePath = TestUtils.getResourceFilePath(EXAMPLE_FILENAME_DATABASE_UNENCRYPTED);

        final var databaseRoundTripFilePath =
                String.format("%s%sround-trip.vl3",
                        new File(exampleFilePath).getParent(),
                        PortabilityUtils.getFileSeparator());

        var rootOutlineItem = new OutlineItem();

        var childOutlineItem1 = new OutlineItem();
        childOutlineItem1.setTitle(UUID.randomUUID().toString());
        childOutlineItem1.setText(UUID.randomUUID().toString());

        var childOutlineItem2 = new OutlineItem();
        childOutlineItem2.setTitle(UUID.randomUUID().toString());
        childOutlineItem2.setText(UUID.randomUUID().toString());

        rootOutlineItem.addChild(childOutlineItem1);
        rootOutlineItem.addChild(childOutlineItem2);

        var vaultDocument = new VaultDocument();
        vaultDocument.setContent(rootOutlineItem);

        new VaultDocumentDatabasePersistence().store(vaultDocument, databaseRoundTripFilePath);

        vaultDocument = new VaultDocumentDatabasePersistence().load(databaseRoundTripFilePath, null);

        validateRoundTripVaultDocument(vaultDocument, childOutlineItem1, childOutlineItem2, databaseRoundTripFilePath,
                VaultDocumentVersion.VERSION_1_3);

        final var xmlRoundTripFilePath =
                String.format("%s%sround-trip.xml",
                        new File(exampleFilePath).getParent(),
                        PortabilityUtils.getFileSeparator());

        when(vaultTreeViewer.getSelectedItems()).thenReturn(vaultDocument.getContent().getChildren());

        new VaultDocumentXMLPersistence().store(vaultDocument, xmlRoundTripFilePath);

        vaultDocument = new VaultDocumentXMLPersistence().load(xmlRoundTripFilePath, null);

        validateRoundTripVaultDocument(vaultDocument, childOutlineItem1, childOutlineItem2, xmlRoundTripFilePath,
                VaultDocumentVersion.VERSION_1_0);
    }

    private void validateRoundTripVaultDocument(VaultDocument vaultDocument, OutlineItem childOutlineItem1,
                                                OutlineItem childOutlineItem2, String filePath,
                                                VaultDocumentVersion expectedVaultDocumentVersion) throws IOException {
        Assert.assertEquals(
                childOutlineItem1.getTitle(),
                vaultDocument.getContent().getChildren().getFirst().getTitle());
        Assert.assertEquals(childOutlineItem1.getText(), vaultDocument.getContent().getChildren().getFirst().getText());

        Assert.assertEquals(childOutlineItem2.getTitle(), vaultDocument.getContent().getChildren().get(1).getTitle());
        Assert.assertEquals(childOutlineItem2.getText(), vaultDocument.getContent().getChildren().get(1).getText());

        Assert.assertEquals(filePath, vaultDocument.getFilePath());

        Assert.assertEquals(
                VaultDocumentVersion.getLatestVaultDocumentVersion(), vaultDocument.getVaultDocumentVersion());
        Assert.assertEquals(expectedVaultDocumentVersion, vaultDocument.getVaultDocumentOriginalVersion());

        Assert.assertNull(vaultDocument.getPassword());

        Assert.assertFalse(vaultDocument.getIsModified());

        validateDocumentMetadata(vaultDocument, filePath);
    }

    @Test
    public void verifyExceptionThrownWhenFileIsNewerThanExpected() {
        var exception = Assert.assertThrows(VaultException.class, () -> {
            final String filePath = TestUtils.getResourceFilePath("version_1_4.vl3");

            new VaultDocumentDatabasePersistence().load(filePath, null);
        });

        Assert.assertEquals("Database version is too high", exception.getMessage());
    }
}