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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

// Based on the WrappingPrinter class in "The Definitive Guide to SWT and JFace", pp. 512 - 515.

public class VaultPrinter {
	public static class PageRange {
		private final int startPage;
		
		public int getStartPage() {
			return startPage;
		}

		private final int endPage;

		public int getEndPage() {
			return endPage;
		}

		public PageRange(int startPage, int endPage) {
			this.startPage = startPage;
			this.endPage = endPage;
		}
	}
	
	private final List<TextWithFont> textToPrint;

	private final Printer printer;

	private Rectangle bounds;

	private int xPos, yPos, pageNumber;

	private GC gc;

	private CodePointString buf;

	private final String fileName;
    private String printDateTime;

	private Font defaultFont;
	
	private final Point margins;
	
	private PageRange pageRange;

	public void setPageRange(PageRange pageRange) {
		this.pageRange = pageRange;
	}

	/**
	 * @author Eric Bergman-Terrell
	 *
	 * This class associates a segment of text with the font that will be used to render it.
	 */
	public static class TextWithFont {
		private CodePointString text;
		private final String fontString;
        private String photoPath;
		
		public TextWithFont(String text, String fontString, String photoPath) {
			this.text = new CodePointString(text);
			this.fontString = fontString;
			
			if (photoPath != null) {
				this.photoPath = PhotoUtils.getPhotoPath(photoPath);
			}
		}
	}
	
	public VaultPrinter(Printer printer, List<TextWithFont> textToPrint, String fileName) {
		this.printer = printer;
		this.textToPrint = textToPrint;
		this.fileName = fileName;

		// Remove carriage returns without line feeds.
		for (TextWithFont textWithFont : textToPrint) {
			textWithFont.text.remove('\r');
		}
		
		// Specify 1/2" margins.
		margins = new Point(printer.getDPI().x / 2, printer.getDPI().y / 2);
	}
	
	/**
	 * Break up any tokens that are wider than the entire page. Tokens are broken on a character, not char, boundary.
	 */
	private void breakUpTokensThatAreWiderThanThePage(TextWithFont textWithFont) {
		CodePointString token = new CodePointString();

		final List<Integer> breakUpPoints = new ArrayList<>();
		
		for (int j = 0; j < textWithFont.text.length(); j++) {
			final int ch = textWithFont.text.codePointAt(j);
			
			if (Character.isWhitespace(ch)) {
				token = new CodePointString();
			}
			else {
				token = token.append(ch);
			
				if (gc.stringExtent(token.toString()).x > bounds.width) {
					breakUpPoints.add(j);
					token = new CodePointString(ch);
				}
			}
		}
		
		if (!breakUpPoints.isEmpty()) {
			final CodePointString text = new CodePointString(textWithFont.text);
			
			for (int j = breakUpPoints.size() - 1; j >= 0; j--) {
				text.insertAt(breakUpPoints.get(j), ' ');
			}
			
			textWithFont.text = new CodePointString(text);
		}
	}
	
	private boolean canPrintCurrentPage() {
		return (pageRange == null || pageNumber >= pageRange.getStartPage() && pageNumber <= pageRange.getEndPage());
	}
	
	public void print() {
		// Start the print job.
		pageNumber = 0;

		final Calendar calendar = Calendar.getInstance();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");

		final Date now = new Date();
		printDateTime = SimpleDateFormat.getDateTimeInstance().format(now);

		final String printJobText = MessageFormat.format(
				"{0} Print Job {1}",
				StringLiterals.ProgramName,
				dateFormat.format(calendar.getTime()));
		
		if (printer.startJob(printJobText)) {
			// Determine print area, with margins
			bounds = computePrintArea(printer);
			xPos = bounds.x;
			yPos = bounds.y;
			
			gc = null;
			
			try {
				// Create the GC
				gc = new GC(printer);

				defaultFont = FontUtils.getDefaultFont(gc);
				
				// Determine tab width--use three spaces for tabs.
				final int tabWidth = gc.stringExtent("   ").x;
				
				// Print the text
				startPage();

				for (int i = 0; i < textToPrint.size(); i++) {
					final TextWithFont textWithFont = textToPrint.get(i);
					
					if (textWithFont.photoPath != null) {
						textWithFont.text = textWithFont.text.append('\n');
					}

					// If this is the last item, and it's blank, quit.
					if (i == textToPrint.size() - 1 && textWithFont.text.toString().replace("\n", StringLiterals.EmptyString).trim().isEmpty()) {
						break;
					}

					final Font previousFont = gc.getFont();

					if (textWithFont.fontString != null && !textWithFont.fontString.isEmpty()) {
						Font font = FontUtils.stringToFont(gc.getDevice(), textWithFont.fontString);
						gc.setFont(font);
					}
					else {
						gc.setFont(defaultFont);
					}

					if (previousFont != defaultFont) {
						previousFont.dispose();
					}
					
					breakUpTokensThatAreWiderThanThePage(textWithFont);
					
					buf = new CodePointString();

					for (int j = 0; j < textWithFont.text.length(); j++) {
						// Get the next character
						int c = textWithFont.text.codePointAt(j);
						
						// Check for newline
						if (c == '\n') {
							printBuffer();
							printNewline();
						}
						// Check for tab
						else if (c == '\t') {
							xPos += tabWidth;
						}
						else {
							buf = buf.append(c);
							// Check for space
							if (Character.isWhitespace(c)) {
								printBuffer();
							}
						}
					}
					
					// Print out the last token if there is one.
					if (buf.length() > 0 && !buf.toString().trim().isEmpty()) {
						printBuffer();
					}
					
					printPhoto(textWithFont);
				}
				
				endPage();
				printer.endJob();
			}
			finally {
				if (gc != null) {
					final Font font = gc.getFont();
					
					if (font != defaultFont) {
						font.dispose();
					}
					
					gc.dispose();
				}
				
				defaultFont.dispose();
			}
		}
		
		printer.dispose();
	}

	private int getLineHeight(GC gc) {
		// Determine line height
		return gc.getFontMetrics().getHeight();
	}

	private void startPage() {
		yPos = bounds.y;

		pageNumber++;
		
		if (canPrintCurrentPage()) {
			printer.startPage();
		}
	}
	
	private void endPage() {
		printPageHeader();
		printPageNumber();
		
		if (canPrintCurrentPage()) {
			printer.endPage();
		}
	}
	
	/**
	 * Prints the contents of the buffer
	 */
	private void printBuffer() {
		// Get the width of the rendered buffer
		final int width = gc.stringExtent(buf.toString()).x;
		
		// Determine if it fits
		if (xPos + width > bounds.x + bounds.width) {
			// Doesn't fit--wrap
			printNewline();
		}
		
		if (canPrintCurrentPage()) {
			// Print the buffer
			gc.drawString(buf.toString(), xPos, yPos, false);
		}
		
		xPos += width;
		buf.clear();
	}

	/**
	 * prints a newline
	 */
	private void printNewline() {
		// Reset x and y locations to next line
		xPos = bounds.x;
		yPos += getLineHeight(gc);
		
		// Have we gone to the next page?
		if (yPos > bounds.y + bounds.height) {
			endPage();
			startPage();
		}
	}
	
	private Rectangle computePrintArea(Printer printer) {
		// Get the printable area.
		final Rectangle rect = printer.getClientArea();
		
		// Compute the trim
		final Rectangle trim = printer.computeTrim(0, 0, 0, 0);
		
		// Calculate the printable area.
		final int left = Math.max(trim.x + margins.x, rect.x);
		final int right = Math.min((rect.width + trim.x + trim.width) - margins.x, rect.width);
		final int top = Math.max(trim.y + margins.y, rect.y);
		final int bottom = Math.min((rect.height + trim.y + trim.height) - margins.y, rect.height);

		return new Rectangle(left, top, right - left, bottom - top);
	}

	private boolean printPhoto(TextWithFont textWithFont) {
		boolean printedPhoto = false;
		
		// Want at least 1/2 of the page to print the photo.
		final float percentPageSizeRequired = 0.50f;
		
		// If the current item has a photo...
		if (textWithFont.photoPath != null) {
			// Force a page break if there is not sufficient space on the current page.
			int remainingVerticalSpace = bounds.height - yPos;
			
			if (remainingVerticalSpace < bounds.height * percentPageSizeRequired) {
				endPage();
				startPage();
			}
			else {
				// Advance to start of next line.
				xPos = bounds.x;
				yPos += getLineHeight(gc);
			}
			
			printedPhoto = true;
			
			Image originalImage = null, scaledImage = null, printerImage = null;
			
			try {
				// Render photograph.
				final Rectangle rectangle = new Rectangle(xPos, yPos, bounds.width, bounds.height - yPos + margins.y);

				originalImage = GraphicsUtils.loadImage(textWithFont.photoPath);

				final Point scaleDimensions = new Point(0, 0);
				scaledImage = GraphicsUtils.resize(rectangle, originalImage, scaleDimensions);
				
				printerImage = new Image(printer, scaledImage.getImageData());

				if (canPrintCurrentPage()) {
					gc.drawImage(printerImage, rectangle.x, rectangle.y);
				}
				
				yPos += scaleDimensions.y;
			}
			finally {
				if (originalImage != null) {
					originalImage.dispose();
					originalImage = null;
				}

				if (scaledImage != null) {
					scaledImage.dispose();
					scaledImage = null;
				}

				if (printerImage != null) {
					printerImage.dispose();
					printerImage = null;
				}
			}
		}
		
		return printedPhoto;
	}
	
	private void printPageHeader() {
		final Rectangle printArea = computePrintArea(printer);

		final Font oldFont = gc.getFont();
		gc.setFont(defaultFont);

		final int x = printArea.x;
		final int y = printArea.y - margins.y + getLineHeight(gc);

		if (canPrintCurrentPage()) {
			// Draw left portion of header.
			gc.drawString(fileName, x, y);

			// Draw right portion of header.
			gc.drawString(printDateTime, x + printArea.width - gc.textExtent(printDateTime).x, y);
		}

		if (!oldFont.isDisposed()) {
			gc.setFont(oldFont);
		}
	}
	
	private void printPageNumber() {
		final String text = MessageFormat.format("- {0} -", pageNumber);

		final Rectangle printArea = computePrintArea(printer);

		final Font oldFont = gc.getFont();
		gc.setFont(defaultFont);

		final int x = (printArea.width - gc.textExtent(text).x) / 2 + printArea.x;
		final int y = printArea.y + printArea.height + (margins.y - getLineHeight(gc)) / 2;

		if (canPrintCurrentPage()) {
			gc.drawString(text, x, y);
		}
		
		gc.setFont(oldFont);
	}
}
