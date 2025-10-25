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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.MessageFormat;
import java.util.List;

import javax.crypto.Cipher;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import commonCode.Base64Utils;
import commonCode.DocumentMetadata;
import commonCode.VaultDocumentVersion;
import commonCode.VaultException;
import org.eclipse.swt.graphics.RGB;

public class VaultDocumentXMLPersistence implements VaultDocumentPersistence {
    private final static String XML_ENCODING = "utf-8";

    /**
	 * Read the specified Vault 3 file. Prompt the user for the password and decrypt the file if necessary.
	 * @param filePath path of Vault 3 file
	 * @param password the password entered by the user
	 * @return an OutlineItem containing the entire Vault 3 file's contents
	 * @throws Exception
	 */
    @Override
    public VaultDocument load(String filePath, StringWrapper password) throws Throwable {
        var vaultDocument = parseVault3File(filePath, password);

        vaultDocument.setVaultDocumentOriginalVersion(vaultDocument.getVaultDocumentVersion());

        vaultDocument.setVaultDocumentVersion(VaultDocumentVersion.getLatestVaultDocumentVersion());

        if (password != null) {
            vaultDocument.setPassword(password.getValue());
        }

        vaultDocument.setIsModified(false);
        vaultDocument.setDocumentMetadata(new DocumentMetadata(filePath));

        vaultDocument.setFilePath(filePath);

        return vaultDocument;
    }

    private static VaultDocument parseVault3File(String filePath, StringWrapper password) throws Exception {
        var vaultDocument = new VaultDocument();

        OutlineItem outlineItem = null;

        Globals.getLogger().info("Starting SAX parsing");

        try {
            Globals.setBusyCursor();

            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            final SAXParser saxParser = saxParserFactory.newSAXParser();

            final NativeDefaultHandler nativeDefaultHandler = new NativeDefaultHandler();

            // If the first argument to parse is a filename with embedded spaces, an exception will be thrown. The solution is to
            // use a FileInputStream instead of a file path.
            try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                saxParser.parse(fileInputStream, nativeDefaultHandler);
            }

            Globals.getLogger().info(String.format("Finished SAX parsing Pass 1. Parsed %s Version: %d.%d",
                    filePath, nativeDefaultHandler.getMajorVersion(), nativeDefaultHandler.getMinorVersion()));

            final VaultDocumentVersion vaultDocumentVersion = new VaultDocumentVersion(nativeDefaultHandler.getMajorVersion(), nativeDefaultHandler.getMinorVersion());
            final VaultDocumentVersion codeVaultDocumentVersion = VaultDocumentVersion.getLatestVaultDocumentVersion();

            if (vaultDocumentVersion.compareTo(codeVaultDocumentVersion) > 0) {
                throw new VaultException("Database version is too high", VaultException.ExceptionCode.DatabaseVersionTooHigh);
            }

            if (nativeDefaultHandler.getIsEncrypted()) {
                byte[] plainText = null;
                byte[] cipherText = nativeDefaultHandler.getCipherText();

                final byte[] salt = nativeDefaultHandler.getSalt();
                final byte[] iv = nativeDefaultHandler.getIV();

                boolean decrypted = false;

                if (password.getValue() != null) {
                    try {
                        final Cipher cipher = CryptoUtils.createDecryptionCipher(password.getValue(),
                                vaultDocumentVersion, salt, iv);

                        plainText = CryptoUtils.decrypt(cipher, cipherText);
                        decrypted = true;
                    }
                    catch (Throwable ex) {
                        Globals.getPasswordCache().remove(filePath);
                        ex.printStackTrace();
                    }
                }

                if (!decrypted) {
                    plainText = CryptoGUIUtils.promptUserForPasswordAndDecrypt(filePath, cipherText, salt, iv, password, vaultDocumentVersion);
                }

                final String unicodeCharset = "UTF-8";

                final Charset charSet = Charset.forName(unicodeCharset);
                final CharsetDecoder charsetDecoder = charSet.newDecoder();

                final ByteBuffer input = ByteBuffer.wrap(plainText);
                final CharBuffer decodedBuffer = charsetDecoder.decode(input);
                final String clearTextString = decodedBuffer.toString();

                Globals.getLogger().info("Starting parse pass 2");

                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(clearTextString.getBytes(unicodeCharset))) {
                    saxParser.parse(inputStream, nativeDefaultHandler);

                    Globals.getLogger().info("finished pass 2");
                }
            }

            outlineItem = nativeDefaultHandler.getOutlineItem();
        }
        finally {
            Globals.setPreviousCursor();
        }

        vaultDocument.setContent(outlineItem);

        return vaultDocument;
    }

    @Override
    public void store(VaultDocument vaultDocument, String filePath) throws Throwable {
        saveAsXMLFile(vaultDocument, filePath);
    }

    /**
     * Saves the current document as an XML file. The file is written to a temporary file. If the temporary file is written
     * successfully, it is renamed to the specified filePath
     * @param filePath the document will be saved to this file path
     * @throws Exception
     */
    private void saveAsXMLFile(VaultDocument vaultDocument, String filePath) throws Exception {
        final String tempSaveFilePath = filePath + ".$$$";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        savePlaintextXmlToStream(byteArrayOutputStream);

        final FileOutputStream fileOutputStream = new FileOutputStream(tempSaveFilePath);

        if (!vaultDocument.isEncrypted()) {
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
        }
        else {
            final ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
            encryptCleartextXmlToStream(vaultDocument, byteArrayOutputStream, cipherText);

            // Need to write out the file in small pieces to avoid a java.io.IOException (Insufficient system resources exist to complete the requested service).
            final int maxSegmentLength = 1024 * 4;

            int index = 0;
            byte[] buffer = cipherText.toByteArray();

            while (index < buffer.length) {
                int segmentLength = Math.min(maxSegmentLength, buffer.length - index);

                final byte[] segment = new byte[segmentLength];

                System.arraycopy(buffer, index, segment, 0, segment.length);

                fileOutputStream.write(segment);

                index += segmentLength;
            }

            Globals.getPasswordCache().put(filePath, vaultDocument.getPassword());
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
     * Renders the current document in XML, and writes it to the specified output stream.
     * @param outputStream the XML is written to this stream
     * @throws XMLStreamException
     * @throws IOException
     */
    private void savePlaintextXmlToStream(OutputStream outputStream) throws XMLStreamException, IOException {
        Globals.getLogger().info("start");

        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter = null;

        OutputStreamWriter outputStreamWriter = null;

        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, XML_ENCODING);

            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStreamWriter);

            xmlStreamWriter.writeStartDocument(XML_ENCODING, "1.0");

            xmlStreamWriter.writeStartElement(NativeDefaultHandler.VAULTELEMENTNAME);
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.VERSIONATTRIBUTENAME, VaultDocumentVersion.getLatestVaultDocumentVersion().toString());
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.BASE64ENCODEDATTRIBUTENAME, NativeDefaultHandler.TRUEVALUE);

            final List<OutlineItem> children = Globals.getVaultTreeViewer().getSelectedItems();

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
    private void encryptCleartextXmlToStream(VaultDocument vaultDocument, ByteArrayOutputStream input, OutputStream outputStream) throws Exception {
        Globals.getLogger().info("start");

        final byte[] salt = CryptoUtils.createSalt();
        final byte[] iv = CryptoUtils.createIV();

        final Cipher cipher = CryptoUtils.createEncryptionCipher(
                vaultDocument.getPassword(),
                vaultDocument.getVaultDocumentVersion(),
                salt,
                iv);

        final byte[] cipherText = CryptoUtils.encrypt(cipher, input.toByteArray());

        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter = null;

        final int itemMaxLength = 1024;

        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(new OutputStreamWriter(outputStream, XML_ENCODING));

            xmlStreamWriter.writeStartDocument(XML_ENCODING, "1.0");

            xmlStreamWriter.writeStartElement(NativeDefaultHandler.VAULTELEMENTNAME);
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.VERSIONATTRIBUTENAME, VaultDocumentVersion.getLatestVaultDocumentVersion().toString());

            xmlStreamWriter.writeStartElement(NativeDefaultHandler.ENCRYPTEDITEMS);

            final String saltString = Base64Utils.encodeToString(salt);
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.SALTATTRIBUTE, saltString);

            final String ivString = Base64Utils.encodeToString(iv);
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.IVATTRIBUTE, ivString);

            int index = 0;

            while (index < cipherText.length) {
                xmlStreamWriter.writeStartElement(NativeDefaultHandler.ENCRYPTEDITEM);

                final int segmentLength = Math.min(itemMaxLength, cipherText.length - index);

                final byte[] segment = new byte[segmentLength];

                System.arraycopy(cipherText, index, segment, 0, segment.length);

                final String base64EncodedString = Base64Utils.encodeToString(segment);

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
        xmlStreamWriter.writeCharacters(Base64Utils.i18nEncode(outlineItem.getTitle()));
        xmlStreamWriter.writeEndElement();

        xmlStreamWriter.writeStartElement(NativeDefaultHandler.TEXTELEMENTNAME);

        final RGB rgb = outlineItem.getRGB();

        if (rgb != null) {
            final String rgbString = String.format("%d,%d,%d", rgb.red, rgb.green, rgb.blue);
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.RGBATTRIBUTENAME, rgbString);
        }

        final String fontListString = Base64Utils.i18nEncode(outlineItem.getFontListString());

        if (fontListString != null) {
            xmlStreamWriter.writeAttribute(NativeDefaultHandler.FONTLISTATTRIBUTENAME, fontListString);
        }

        xmlStreamWriter.writeCharacters(Base64Utils.i18nEncode(outlineItem.getText()));
        xmlStreamWriter.writeEndElement();

        final String photoPath = Base64Utils.i18nEncode(outlineItem.getPhotoPath());

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
}