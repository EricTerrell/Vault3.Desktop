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

import java.awt.font.TextAttribute;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import fonts.IFont;

public class PDFExport {
	private final static int DEFAULT_SIZE = 12;
	private final static String textExportPreviousFolder = System.getProperty("user.home");
	private Boolean defaultFontIsVariableWidth;
	private Font titleFont;
	
	public void pdfFileExport(Shell shell) {
		Globals.getVaultTextViewer().saveChanges();
		
		final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFilterNames(new String[] { "PDF Files", "All Files" });
		fileDialog.setFilterExtensions(new String[] { StringLiterals.PDFFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
		fileDialog.setFilterPath(textExportPreviousFolder);
		
		fileDialog.setText("Export");
		
		final String filePath = fileDialog.open();
		
		if (filePath != null) {
			try {
				Globals.setBusyCursor();

				final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();

		        Document document = null;
		
		        try {
		        	document = new Document();
			        PdfWriter.getInstance(document, new FileOutputStream(filePath));
			        
			        document.open();

			        // TOC
					for (OutlineItem outlineItem : selectedItems) {
						exportOutlineItem(document, outlineItem, true, 0);
					}

					document.newPage();
					
					// BODY
					for (OutlineItem outlineItem : selectedItems) {
						exportOutlineItem(document, outlineItem, false, 0);
					}
		        }
		        finally {
		        	if (document != null) {
		        		document.close();
		        	}
		        }
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				Globals.getLogger().info(String.format("PDFExport.pdfFileExport: Exception %s", ex));

				final String message = MessageFormat.format("Cannot export PDF file {0}.{1}{1}{2}", filePath, PortabilityUtils.getNewLine(),  ex.getMessage());
				final MessageDialog messageDialog = new MessageDialog(shell, StringLiterals.ProgramName, Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON), message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();
			}
			finally {
				Globals.setPreviousCursor();
			}
		}
	}
	
	private void exportOutlineItem(Document document, OutlineItem outlineItem, boolean tocOnly, int nestingDepth) throws DocumentException {
		if (!tocOnly || outlineItem.hasChildren()) {
			final Chunk chunk = new Chunk(outlineItem.getTitle(), getTitleFont());
	
			if (!tocOnly && outlineItem.getParent() != null) {
				chunk.setLocalDestination(outlineItem.getUuid().toString());
			}
			
			if (tocOnly && outlineItem.hasChildren()) {
				chunk.setLocalGoto(outlineItem.getUuid().toString());
				chunk.setUnderline(0.1f, -2f);
			}
			
			final Paragraph titleParagraph = new Paragraph(chunk);

			if (tocOnly) {
				titleParagraph.setIndentationLeft(nestingDepth * 15);
			}
			
			document.add(titleParagraph);
			
			if (!tocOnly) {
				document.add(Chunk.NEWLINE);
			}
			
			if (!tocOnly) {
				final Font textFont = getTextFont(outlineItem);
				
				if (!outlineItem.getText().trim().isEmpty()) {
					Paragraph textParagraph = new Paragraph(outlineItem.getText(), textFont);
					document.add(textParagraph);
					document.add(Chunk.NEWLINE);
				}
			}
					
			for (OutlineItem childOutlineItem : outlineItem.getChildren()) {
				exportOutlineItem(document, childOutlineItem, tocOnly, nestingDepth + 1);
			}
		}
	}

	private Font getTitleFont() {
		if (titleFont == null) {
			titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, DEFAULT_SIZE);
		}
		
		return titleFont;
	}
	
	private Font getTextFont(OutlineItem outlineItem) {
		Font textFont = null;
		boolean variableWidth = true;

		if (outlineItem.getFontList() == null)
		{
			if (defaultFontIsVariableWidth == null) {
				GC gc = null;
				org.eclipse.swt.graphics.Font defaultFont = null;
				org.eclipse.swt.graphics.Font previousFont = null;
				
				try {
					gc = new GC(Display.getCurrent());
	
					defaultFont = FontUtils.getDefaultFont(gc);
					previousFont = gc.getFont();
					
					gc.setFont(defaultFont);
					
					variableWidth = gc.stringExtent("W").x != gc.stringExtent("i").x;
					defaultFontIsVariableWidth = variableWidth;
					
					gc.setFont(previousFont);
				}
				finally {
					if (defaultFont != null && defaultFont != previousFont) {
						defaultFont.dispose();
					}
					
					if (gc != null) {
						gc.dispose();
					}
				}
			}
			else {
				variableWidth = defaultFontIsVariableWidth;
			}
		}
		else {
			final IFont iFont = outlineItem.getFontList().getFont();
		
			if (iFont != null) {
				final java.awt.Font font = new java.awt.Font(iFont.getName(), java.awt.Font.PLAIN, DEFAULT_SIZE);

				final Map<TextAttribute, ?> attributeMap = font.getAttributes();
			    variableWidth = attributeMap.containsValue("Monospaced");
			}
		}

		textFont = FontFactory.getFont(variableWidth ? FontFactory.HELVETICA : FontFactory.COURIER, DEFAULT_SIZE);

		return textFont;
	}
}
