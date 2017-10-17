/**
 * readImage.java reads image information from the images folder and
 * writes text files containing each image's intensity and color code
 * histogram information.
 * 
 * Existing code modified by Connor McGrath
 * Spring 2017 CSS 490B
 * Homework 1
 */

import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;

//import CBIR.IconButtonHandler;

import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;

public class readImage
{
	int imageCount = 1;
	int intensityBins [] = new int [25];
	int intensityMatrix [][] = new int[100][25];
	int colorCodeBins [] = new int [64];
	int colorCodeMatrix [][] = new int[100][64];
	int resolutions [] = new int[100];

	/*Each image is retrieved from the file.  The height and width are found for the image and the getIntensity and
	 * getColorCode methods are called.
	 */
	public readImage()
	{
		while(imageCount < 101){ //iterates through all 100 images
			BufferedImage img; //current image

			try //attempt to read next image
			{
				img = ImageIO.read(new File("images/" + imageCount + ".jpg"));
				
				final int height = img.getHeight();
				final int width = img.getWidth();
				final int resolution = height * width;
				
				resolutions[imageCount - 1] = resolution;
				getIntensity(img, height, width);
				getColorCode(img, height, width);
			} 
			catch (IOException e)
			{
				System.out.println("Error occurred when reading the file.");
			}

			++imageCount; //increment image number
		}
		
		writeResolution();
		writeIntensity();
		writeColorCode();

	}

	/** intensity method 
	* Note: pixel color channel parsing optimizations are adapted from @author Motasim on StackOverflow. 
	* @author Connor McGrath
	* @see writeIntensity();
	*/ 
	public void getIntensity(BufferedImage image, final int height, final int width){
		byte pixels[] = ((DataBufferByte) image.getRaster().getDataBuffer()).getData(); //get pixel info
		boolean hasAlphaChannel = image.getAlphaRaster() != null; //determine if alpha channel exists
		
		for (int i = 0; i < 25; i++)
			intensityBins[i] = 0; //reset count for intensity bins
		
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//final int alpha = ((int) pixels[pixel] & 0xff); // alpha
				final int blue = ((int) pixels[pixel + 1] & 0xff); // blue
				final int green = ((int) pixels[pixel + 2] & 0xff); // green
				final int red = ((int) pixels[pixel + 3] & 0xff); // red
				
				//calculate pixel intensity
				double intensity = (.299 * red) + (.587 * green) + (.114 * blue);
				
				//place pixel into a histogram bin
				int bin = (int) intensity/10; 
				if (bin == 25) bin = 24; //only 25 available bins (0 to 24)
				
				intensityBins[bin]++; //add pixel to its respective bin
			}
		} 
		else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//final int alpha = -16777216 // 255 alpha
				final int blue = ((int) pixels[pixel] & 0xff); // blue
				final int green = ((int) pixels[pixel + 1] & 0xff); // green
				final int red = ((int) pixels[pixel + 2] & 0xff); // red
				
				//calculate pixel intensity
				double intensity = (.299 * red) + (.587 * green) + (.114 * blue);
				
				//place pixel into a histogram bin
				int bin = (int) intensity/10; 
				if (bin == 25) bin = 24; //only 25 available bins (0 to 24)
				
				intensityBins[bin]++; //add pixel to its respective bin
			}
		}
		
		//transfer intensityBins info to intensityMatrix for current image
		for (int i = 0; i < 25; i++){
			intensityMatrix[imageCount - 1][i] = intensityBins[i];
		}
	}

	/** Color code method 
	* Note: pixel color channel parsing optimizations are adapted from @author Motasim on StackOverflow. 
	* @author Connor McGrath
	* @see writeColorCode();
	*/ 
	public void getColorCode(BufferedImage image, final int height, final int width){
		byte pixels[] = ((DataBufferByte) image.getRaster().getDataBuffer()).getData(); //get pixel info
		boolean hasAlphaChannel = image.getAlphaRaster() != null; //determine if alpha channel exists
		
		for (int i = 0; i < 64; i++)
			colorCodeBins[i] = 0; //reset count for color code bins
		
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//final int alpha = ((int) pixels[pixel] & 0xff); // alpha
				final byte blue = (byte) ( pixels[pixel + 1] & 0xC0 ); // get first two blue bits
				final byte green = (byte) ( pixels[pixel + 2] & 0xC0 ); // get first two green bits
				final byte red = (byte) ( pixels[pixel + 3] & 0xC0 ); // get first two red bits
				
				// get pixel color code by clearing bits 6,7; placing red in bits 4,5 
				// green bits 2,3; blue in bits 0,1. This effectively makes a 6-bit color code (RGB)
				// with two leading 0s.
				int colCode = ((red << 2) + (green << 4) + (blue << 6)) & 0x3f;
				
				// Add pixel to its respective histogram bin. 
				// Each color code value corresponds to a bin number (0 to 63).
				colorCodeBins[colCode]++; //add pixel to its respective bin
			}
		} 
		else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//final int alpha = -16777216 // 255 alpha
				byte blue = (byte) ( pixels[pixel] & 0xC0 ); // get first two blue bits
				byte green = (byte) ( pixels[pixel + 1] & 0xC0 ); // get first two green bits
				byte red = (byte) ( pixels[pixel + 2] & 0xC0 ); // get first two red bits
				
				// get pixel color code by clearing bits 6,7; placing red in bits 4,5 
				// green bits 2,3; blue in bits 0,1. This effectively makes a 6-bit color code (RGB)
				// with two leading 0s.
				red = (byte) ((red >> 2) & 0x30); //move red bits
				green = (byte) ((green >> 4) & 0x0C); //move green bits
				blue = (byte) ((blue >> 6) & 0x03); //move blue bits
				int colCode = (red + green + blue) & 0x3f; //merge RGB bits, clear leading 2 bits
				
				// Add pixel to its respective histogram bin. 
				// Each color code value corresponds to a bin number (0 to 63).
				colorCodeBins[colCode]++; //add pixel to its respective bin
			}
		}
		
		//transfer colorCodeBins info to colorCodeMatrix for current image
		for (int i = 0; i < 64; i++){
			colorCodeMatrix[imageCount - 1][i] = colorCodeBins[i];
		}
	}

	/**
	 * This method writes the contents of the colorCode matrix to a file named colorCodes.txt.
	 * @see getColorCode() 
	 */
	public void writeColorCode(){
		try{
			PrintWriter writer = new PrintWriter("colorCodes.txt", "UTF-8");
			
			for (int i = 0; i < 100; i++){ //iterate through all 100 images
				for (int j = 0; j < 63; j++){ //iterate through first 63 bins
					writer.print(colorCodeMatrix[i][j] + ",");
				}
				writer.print(colorCodeMatrix[i][63]); //no "," after 64th bin
				writer.println(); //new line for next image
			}

			writer.close();
		} catch (IOException e) {
			System.out.println("Error writing to intensity.txt.");
		}
	}

	/**
	 * writeIntensity () writes the contents of the intensity matrix to 
	 * a file called intensity.txt
	 * @see getIntensity()
	 */
	public void writeIntensity(){
		try{
			PrintWriter writer = new PrintWriter("intensity.txt", "UTF-8");
			
			for (int i = 0; i < 100; i++){ //iterate through all 100 images
				for (int j = 0; j < 24; j++){ //iterate through first 24 bins
					writer.print(intensityMatrix[i][j] + ",");
				}
				writer.print(intensityMatrix[i][24]); //no "," after 25th bin
				writer.println(); //new line for next image
			}

			writer.close();
		} catch (IOException e) {
			System.out.println("Error writing to intensity.txt.");
		}
	}
	
	/**
	 * writeResolution () writes the contents of the resolution array to 
	 * a file called resolution.txt
	 */
	public void writeResolution(){
		try{
			PrintWriter writer = new PrintWriter("resolution.txt", "UTF-8");
			
			for (int i = 0; i < 100; i++){
				writer.println(resolutions[i]);
			}

			writer.close();
		} catch (IOException e) {
			System.out.println("Error writing to resolution.txt.");
		}
	}
	
	
	public static void main(String[] args)
	{
		new readImage();
	}

}
