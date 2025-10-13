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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.perf4j.LoggingStopWatch;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Various graphics utility methods.
 * @author Eric Bergman-Terrell
 */
public class GraphicsUtils {
	/**
	 * Returns the dimensions that will be used to render the specified string.
	 * @param text text to render
	 * @return dimensions of text
	 */
	public static Point getTextExtent(String text) {
		GC gc = null;
		
		Point result = null;
		
		try {
			gc = new GC(Display.getCurrent());
			
			result = gc.textExtent(text);
		}
		finally {
			if (gc != null) {
				gc.dispose();
			}
		}
		
		return result;
	}

	/**
	 * Scale the originalImage to have width and height no larger than maxResolution, preserving aspect ratio.
	 * @param maxResolution maximum width and height
	 * @param originalImage image to scale
	 * @return scaled image
	 */
	public static Image resize(int maxResolution, Image originalImage) {
		final double xScale = (double) originalImage.getBounds().width / (double) maxResolution;
		final double yScale = (double) originalImage.getBounds().height / (double) maxResolution;

		final double scale = Math.max(xScale, yScale);
		
		final int scaleWidth = (int) ((double) originalImage.getBounds().width / scale); 
		final int scaleHeight = (int) ((double) originalImage.getBounds().height / scale); 
		
		return resize(new Rectangle(0, 0, scaleWidth, scaleHeight), originalImage, null);
	}
	
	/**
	 * Scales or copies the originalImage to fit in the specified rect with aspect ratio retained.
	 * Based on http://mea-bloga.blogspot.com/2007/08/resizing-images-using-swt.html
	 * @param rect desired image dimensions
	 * @param originalImage original image
	 * @param scaleDimensions the actual size of visible part of the image. In other words, 
	 * the image returned by this method will have dimensions equal to the specified rect and
	 * will have margins. scaleDimensions will specify the size of the image without the margins.
	 * @param resize if true, image is resized to fill the rectangle. 
	 * @return scaled image containing margins
	 */
	private static Image resizeOrCopy(Rectangle rect, Image originalImage, Point scaleDimensions, boolean resize) {
		Image scaled = null;
		
		if (originalImage != null) {
			LoggingStopWatch stopwatch = new LoggingStopWatch("GraphicsUtils.resizeOrCopy");
			
			final double xScale = (double) originalImage.getBounds().width / (double) rect.width;
			final double yScale = (double) originalImage.getBounds().height / (double) rect.height;
	
			final double scale = Math.max(xScale, yScale);
			
			final int scaleWidth = (int) ((double) originalImage.getBounds().width / scale); 
			final int scaleHeight = (int) ((double) originalImage.getBounds().height / scale); 
			
			if (scaleDimensions != null) {
				scaleDimensions.x = scaleWidth;
				scaleDimensions.y = scaleHeight;
			}

			Globals.getLogger().info("create scaled image");
			
			final LoggingStopWatch stopwatchScale = new LoggingStopWatch("GraphicsUtils.resizeOrCopy: create scaled image");

			scaled = new Image(Display.getDefault(), rect.width, rect.height);
			
			stopwatchScale.stop();
			
			final GC gc = new GC(scaled);
			
			try {
				gc.setAntialias(SWT.ON);
				gc.setInterpolation(SWT.HIGH);

				Globals.getLogger().info(String.format("initial advanced value: %s", gc.getAdvanced()));
				
				gc.setAdvanced(Globals.getPreferenceStore().getBoolean(PreferenceKeys.AdvancedGraphics));
				
				Globals.getLogger().info(String.format("begin drawImage: anti-aliasing: %d interpolation: %d advanced: %s", gc.getAntialias(), gc.getInterpolation(), gc.getAdvanced())); 
				
				LoggingStopWatch stopwatchResize = new LoggingStopWatch("GraphicsUtils.resizeOrCopy: resize");
				
				if (resize) {
					gc.drawImage(originalImage, 0, 0, originalImage.getBounds().width, originalImage.getBounds().height, 0, 0, scaleWidth, scaleHeight);
				}
				else {
					gc.drawImage(originalImage, 0, 0, originalImage.getBounds().width, originalImage.getBounds().height, 0, 0, originalImage.getBounds().width, originalImage.getBounds().height);
				}

				stopwatchResize.stop();
			}
			finally {
				gc.dispose();
			}
			
			stopwatch.stop();
		}
		
		return scaled;
	}

	/**
	 * Scales the originalImage to fit in the specified rect with aspect ratio retained.
	 * @param rect desired image dimensions
	 * @param originalImage original image
	 * @param scaleDimensions the actual size of visible part of the image. In other words, 
	 * the image returned by this method will have dimensions equal to the specified rect and
	 * will have margins. scaleDimensions will specify the size of the image without the margins. 
	 * @return scaled image containing margins
	 */
	public static Image resize(Rectangle rect, Image originalImage, Point scaleDimensions) {
		return resizeOrCopy(rect, originalImage, scaleDimensions, true);
	}

	/**
	 * Copies the originalImage to fit in the specified rect with aspect ratio retained.
	 * @param rect desired image dimensions
	 * @param originalImage original image
	 * @param scaleDimensions the actual size of visible part of the image. In other words, 
	 * the image returned by this method will have dimensions equal to the specified rect and
	 * will have margins. scaleDimensions will specify the size of the image without the margins. 
	 * @return scaled image containing margins
	 */
	public static Image copy(Rectangle rect, Image originalImage, Point scaleDimensions) {
		return resizeOrCopy(rect, originalImage, scaleDimensions, false);
	}

	/**
	 * Scales the image to the specified dimensions and writes it to the specified path. The image will have
	 * black margins.
	 * @param imagePath file path of original image
	 * @param destinationPath file path where scaled image will be written
	 * @param deviceDimensions dimensions of device for which the image is being scaled
	 */
	public static void exportPhotoToDevice(String imagePath, String destinationPath, Point deviceDimensions) {
		final LoggingStopWatch stopwatch = new LoggingStopWatch("GraphicsUtils.exportPhotoToDevice");

		BufferedImage originalImage = null;
		BufferedImage scaledImage = null;
		
		try {
			if (StringUtils.isURL(imagePath)) {
				final URL imageURL = new URI(imagePath).toURL();
				
				originalImage = ImageIO.read(imageURL);
			}
			else {
				originalImage = ImageIO.read(new File(imagePath));
			}
			
			Globals.getLogger().info(String.format("exportPhotoToDevice: imagePath: %s destinationPath: %s %d %d", imagePath, destinationPath, deviceDimensions.x, deviceDimensions.y));

			final Scalr.Mode mode = getMode(deviceDimensions, originalImage);

			scaledImage = Scalr.resize(originalImage, Method.ULTRA_QUALITY, mode, deviceDimensions.x, deviceDimensions.y, Scalr.OP_ANTIALIAS);

			Globals.getLogger().info(String.format("scaled image %d %d", scaledImage.getWidth(), scaledImage.getHeight()));
	
			originalImage.flush();
			originalImage = null;
			
			ImageIO.write(scaledImage, "jpeg", new File(destinationPath));
			
			scaledImage.flush();
			scaledImage = null;
		}
		catch (Exception ex) {
			Globals.getLogger().info(String.format("exportPhotoToDevice: Exception %s", ex.getMessage()));
		}
		finally {
			if (originalImage != null) {
				originalImage.flush();
			}
			
			if (scaledImage != null) {
				scaledImage.flush();
			}
		}

		stopwatch.stop();
	}

	private static Scalr.Mode getMode(Point deviceDimensions, BufferedImage originalImage) {
		final double deviceAspectRatio = (double) deviceDimensions.x / (double) deviceDimensions.y;
		final double originalImageAspectRatio = (double) originalImage.getWidth() / (double) originalImage.getHeight();

		return deviceAspectRatio > originalImageAspectRatio ? Scalr.Mode.FIT_TO_HEIGHT : Scalr.Mode.FIT_TO_WIDTH;
	}

	/**
	 * Return graphics file type names
	 * @return
	 * Returns an array of file type descriptions that Vault 3 can display.
	 */
	public static String[] getFilterNames() {
		return new String[] { "JPEG Files", "GIF Files", "Windows Bitmaps", "Portable Network Graphics", "TIFF Files", "Windows Metafiles", "All Files" };
	}
	
	/**
	 * Return graphics file types
	 * @return
	 * Returns an array of file types that Vault 3 can display.
	 */
	public static String[] getFilterExtensions() {
		return new String[] { "*.jpg;*.JPG;*.jpeg;*.JPEG", "*.gif;*.GIF", "*.bmp;*.BMP", "*.png;*.PNG", ".tif;*.TIF", "*.wmf;*.WMF", StringLiterals.Wildcard };
	}
	
	static Image loadImage(String imagePathOrURL) {
		Image image = null;
		
		if (StringUtils.isURL(imagePathOrURL)) {
			InputStream inputStream = null;
			
			try {
				URL url = new URI(imagePathOrURL.trim()).toURL();
				
				inputStream = url.openStream();
			
				image = new Image(Display.getCurrent(), inputStream);
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				
				Globals.getLogger().info(String.format("GraphicsUtils.loadImage \"%s\" %s", imagePathOrURL, ex.getMessage()));
			}
			finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					}
					catch (Throwable ex) {
						ex.printStackTrace();
						Globals.getLogger().info(String.format("GraphicsUtils.loadImage \"%s\" %s", imagePathOrURL, ex.getMessage()));
					}
				}
			}
		}
		else {
			image = new Image(Display.getCurrent(), imagePathOrURL);
		}
		
		return image;
	}

	// https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java
	static void rotate(String imagePath, float angle) {
		try (FileInputStream inputStream = new FileInputStream(imagePath)) {
			final BufferedImage originalImage = ImageIO.read(inputStream);

			final double rads = Math.toRadians(angle);
			final double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));

			final int w = originalImage.getWidth();
			final int h = originalImage.getHeight();

			final int newWidth = (int) Math.floor(w * cos + h * sin);
			final int newHeight = (int) Math.floor(h * cos + w * sin);

			final BufferedImage rotated = new BufferedImage(newWidth, newHeight, originalImage.getType());

			Graphics2D g2d = null;

			try {
				g2d = rotated.createGraphics();

				final AffineTransform transform = new AffineTransform();
				transform.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);

				final int x = w / 2;
				final int y = h / 2;

				transform.rotate(rads, x, y);
				g2d.setTransform(transform);
				g2d.drawImage(originalImage, 0, 0, null);
			} finally {
				if (g2d != null) {
					g2d.dispose();
				}
			}

			try (FileOutputStream outputStream = new FileOutputStream(imagePath)) {
				final String formatName = imagePath.substring(imagePath
						.lastIndexOf(".") + 1);

				ImageIO.write(rotated, formatName, outputStream);
			} catch (Exception ex) {
				ex.printStackTrace();
				Globals.getLogger().info(String.format("GraphicsUtils.rotate %f \"%s\" %s",
						angle, imagePath, ex.getMessage()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Globals.getLogger().info(String.format("GraphicsUtils.rotate %f \"%s\" %s",
					angle, imagePath, ex.getMessage()));
		}
	}
}
