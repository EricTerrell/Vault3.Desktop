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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import commonCode.Base64Utils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.RGB;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import fonts.FontList;

public class NativeDefaultHandler extends DefaultHandler {
    private OutlineItemWithTempText outlineItem;

    private StringBuilder elementText;

    private Stack<OutlineItemWithTempText> stack;

    private ByteArrayOutputStream encryptedBytes;

    public byte[] getCipherText() {
        byte[] result = encryptedBytes.toByteArray();

        // Deallocate the memory in the buffer as soon as possible.
        encryptedBytes = null;

        return result;
    }

    private boolean isEncrypted = false;

    private byte[] salt;
    private byte[] iv;

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getIV() {
        return iv;
    }

    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    private int majorVersion = 1, minorVersion = 0;

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    private boolean isBase64Encoded = false;

    public final static String TEXTELEMENTNAME = "Text";
    public final static String VAULTELEMENTNAME = "Vault";
    public final static String BASE64ENCODEDATTRIBUTENAME = "base64Encoded";
    public final static String ITEMELEMENTNAME = "Item";
    public final static String TITLEELEMENTNAME = "Title";
    public final static String PHOTOELEMENTNAME = "Photo";
    public final static String ALLOWSCALINGATTRIBUTENAME = "AllowScaling";
    public final static String RGBATTRIBUTENAME = "rgb";
    public final static String FONTLISTATTRIBUTENAME = "fontList";
    public final static String PATHATTRIBUTENAME = "path";
    public final static String VERSIONATTRIBUTENAME = "version";
    public final static String ENCRYPTEDITEMS = "EncryptedItems";
    public final static String ENCRYPTEDITEM = "EncryptedItem";

    public final static String SALTATTRIBUTE = "salt";
    public final static String IVATTRIBUTE = "iv";

    public final static String TRUEVALUE = "true";
    public final static String FALSEVALUE = "false";

    public NativeDefaultHandler() {
    }

    public OutlineItem getOutlineItem() {
        return outlineItem.getOutlineItem();
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();

        stack = new Stack<>();
        outlineItem = null;
        elementText = null;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();

        Assert.isTrue(stack.isEmpty());
    }

    private String decodeIfNecessary(String text) throws UnsupportedEncodingException {
        String decodedText;

        if (isBase64Encoded) {
            decodedText = Base64Utils.i18nDecode(text);
        } else {
            decodedText = text;
        }

        return decodedText;
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);

        elementText = new StringBuilder();

        if (name.equals(ITEMELEMENTNAME)) {
            OutlineItemWithTempText parent = stack.peek();

            OutlineItemWithTempText child = new OutlineItemWithTempText();
            child.getOutlineItem().setParent(parent.getOutlineItem());

            parent.getOutlineItem().addChild(child.getOutlineItem());

            stack.push(child);
        }
        switch (name) {
            case TEXTELEMENTNAME: {
                final String rgbString = attributes.getValue(RGBATTRIBUTENAME);

                if (rgbString != null) {
                    final String[] rgbArray = rgbString.split(",");

                    if (rgbArray.length == 3) {
                        final int red = Integer.valueOf(rgbArray[0]);
                        final int green = Integer.valueOf(rgbArray[1]);
                        final int blue = Integer.valueOf(rgbArray[2]);

                        final OutlineItem currentItem = stack.peek().getOutlineItem();
                        currentItem.setRGB(new RGB(red, green, blue));
                    }
                }

                try {
                    final String fontString = decodeIfNecessary(attributes.getValue(FONTLISTATTRIBUTENAME));

                    if (fontString != null) {
                        final OutlineItem currentItem = stack.peek().getOutlineItem();

                        try {
                            final FontList fontList = FontList.deserialize(fontString);
                            currentItem.setFontList(fontList);
                        } catch (Throwable ex) {
                            Globals.getLogger().severe(String.format("NativeDefaultHandler.StartElement: cannot deserialize FontList from %s", fontString));
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    throw new SAXException(ex);
                }
            }
            break;
            case PHOTOELEMENTNAME: {
                try {
                    final String photoPath = decodeIfNecessary(attributes.getValue(PATHATTRIBUTENAME));

                    if (photoPath != null) {
                        final OutlineItem currentItem = stack.peek().getOutlineItem();
                        currentItem.setPhotoPath(photoPath);
                    }
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    throw new SAXException(ex);
                }

                final String allowScaling = attributes.getValue(ALLOWSCALINGATTRIBUTENAME);

                if (allowScaling != null) {
                    final OutlineItem currentItem = stack.peek().getOutlineItem();
                    currentItem.setAllowScaling(!allowScaling.equals(FALSEVALUE));
                }
            }
            break;
            case VAULTELEMENTNAME: {
                final String versionString = attributes.getValue(VERSIONATTRIBUTENAME);

                if (versionString != null) {
                    final String[] majorAndMinorVersions = versionString.split("\\.");

                    if (majorAndMinorVersions.length == 2) {
                        majorVersion = Integer.valueOf(majorAndMinorVersions[0]);
                        minorVersion = Integer.valueOf(majorAndMinorVersions[1]);
                    }
                }

                final String base64EncodingString = attributes.getValue(BASE64ENCODEDATTRIBUTENAME);

                isBase64Encoded = base64EncodingString != null && base64EncodingString.equals(TRUEVALUE);

                outlineItem = new OutlineItemWithTempText();
                stack.push(outlineItem);
            }
            break;
            case ENCRYPTEDITEMS: {
                isEncrypted = true;

                final String saltString = attributes.getValue(SALTATTRIBUTE);

                if (saltString != null) {
                    salt = Base64Utils.decode(saltString);
                }

                final String ivString = attributes.getValue(IVATTRIBUTE);

                if (ivString != null) {
                    iv = Base64Utils.decode(ivString);
                }

                encryptedBytes = new ByteArrayOutputStream();
            }
            break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);

        switch (name) {
            case ITEMELEMENTNAME: {
                final OutlineItemWithTempText outlineItem = stack.pop();

                try {
                    outlineItem.getOutlineItem().setText(decodeIfNecessary(outlineItem.getTempText().toString()));
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    throw new SAXException(ex);
                }
            }
            break;
            case TITLEELEMENTNAME: {
                try {
                    stack.peek().getOutlineItem().setTitle(decodeIfNecessary(elementText.toString()));
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    throw new SAXException(ex);
                }
            }
            break;
            case TEXTELEMENTNAME: {
                stack.peek().getTempText().append(elementText);
            }
            break;
            case VAULTELEMENTNAME: {
                stack.pop();
            }
            break;
            case ENCRYPTEDITEM: {
                final byte[] encryptedBuffer = Base64Utils.decode(elementText.toString());

                try {
                    encryptedBytes.write(encryptedBuffer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new SAXException(ex);
                }
            }
            break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);

        final String currentText = new String(ch, start, length);

        if (!currentText.isEmpty()) {
            elementText.append(currentText);
        }
    }
}
