/**
 * CBIR.java provides GUI interface and methods for computing the distance
 * between the images contained in the images folder. Reads information from
 * the files created by readImage.java.
 * 
 * Existing code modified by Connor McGrath
 * Spring 2017 CSS 490B
 */

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import org.apache.commons.math3.stat.descriptive.*; //stdev, mean

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.*;

public class CBIR extends JFrame{

	private JPanel photoIconPanel;
	private JLabel photoIconLabel;
	private JLabel photographLabel = new JLabel();  //container to hold a large
	private JPanel [] imageButton; //container for image button and image number, order = order of button[]
	private JLabel [] imageLabel; //contains image numbers, order correlates to button[] order
	private JCheckBox [] imageRelBox; //used to mark images as relevant
	private JButton [] button; //creates an array of JButtons for image displays
	private int [] buttonOrder = new int [101]; //creates an array to keep up with the image order
	private double [] imageSize = new double[101]; //keeps up with the image sizes
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;
	//private BorderLayout borderLayoutPageNav;
	private JPanel panelPrimary; //holds old panel structure for images and sorting mechanisms
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;
	private JPanel pageNavPanel;
	//private JTextField pageNumberText;
	private JLabel pageNumberText;
	private int [][] intensityMatrix = new int [100][25];
	private int [][] colorCodeMatrix = new int [100][64];
	private double [][] intenColorCodeMatrix = new double [101][89]; //normalized, row [100] contains weights
	//private double [][] rfWeightedMatrix = new double [101][89]; // row [100] contains RF weights
	private boolean [] relevantImage = new boolean [100]; //stores relevance of images
	private JCheckBox [] imgRelevanceCheckBox = new JCheckBox[100]; //relevance check boxes for each image
	private int [] resolutions = new int [100];
	private int relevantCount = 0;
	private boolean firstRFIteration = true;
	private boolean relevanceEnabled = false;
	private Map <Double , LinkedList<Integer>> map;
	int picNo = 0;
	int imageCount = 1; //keeps up with the number of images displayed since the first page.
	int pageNo = 1;


	public static void main(String args[]) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CBIR app = new CBIR();
				app.setVisible(true);
			}
		});
	}



	public CBIR() {
		//The following lines set up the interface including the layout of the buttons and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Connor McGrath 490 Program 1");
		panelPrimary = new JPanel();
		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		buttonPanel = new JPanel();
		pageNavPanel = new JPanel();
		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 10); //whole window's layout
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(3, 3, 10, 0); //change to 4 when add more buttons
		//gridLayoutPageNav = new GridLayout(1, 2, 50, 0);
		//borderLayoutPageNav = new BorderLayout();
		setLayout(new BorderLayout());
		panelPrimary.setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout1);
		panelTop.setLayout(gridLayout3);
		//pageNavPanel.setLayout(gridLayoutPageNav);
		//pageNavPanel.setLayout(borderLayoutPageNav);
		add(panelPrimary, BorderLayout.CENTER); 
		panelPrimary.add(panelTop);
		panelPrimary.add(panelBottom1);
		add(pageNavPanel, BorderLayout.SOUTH);

		//set up selected/query image icon panel
		photoIconPanel = new JPanel(new BorderLayout());
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		photoIconLabel = new JLabel("\tPlease Select an Image");
		photoIconLabel.setFont(new Font("verdana", 1, 12)); 
		photoIconLabel.setForeground(Color.black);
		photoIconPanel.add(photoIconLabel, BorderLayout.WEST);
		photoIconPanel.add(photographLabel, BorderLayout.CENTER);
		
		panelTop.add(photoIconPanel);
		//panelTop.add(photographLabel);
		
		buttonPanel.setLayout(gridLayout4);
		panelTop.add(buttonPanel);
		//Sort by Intensity button
		JButton intensity = new JButton("Sort by Intensity");
		intensity.setFont(new Font("verdana", 1, 14)); 
		buttonPanel.add(intensity);
		buttonPanel.add(new JLabel());// spacing/placeholder
		buttonPanel.add(new JLabel());// spacing/placeholder
		//Sort by Color Code button
		JButton colorCode = new JButton("<html>Sort by<br />Color Code</html>");
		colorCode.setFont(new Font("verdana", 1, 14));
		buttonPanel.add(colorCode);
		buttonPanel.add(new JLabel());// spacing/placeholder
		buttonPanel.add(new JLabel());// spacing/placeholder
		//Sort by Intensity + Color Code w/ Relevance Feedback button
		JButton intenColorCode = new JButton("<html>Sort by Intensity + Color Code<br /><font size=\"3\">(Relevance Feedback)</font></html>");
		intenColorCode.setFont(new Font("verdana", 1, 14));
		buttonPanel.add(intenColorCode);
		JCheckBox relevanceToggle = new JCheckBox("Toggle Relevance", false);
		relevanceToggle.setFont(new Font("verdana", 1, 14));
		buttonPanel.add(relevanceToggle);
		
		// Previous/Next page navigation panel
		pageNavPanel.setPreferredSize(new Dimension(20, 40));
		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		pageNumberText = new JLabel("Page " + pageNo + "/5");
		//pageNumberText.setBackground(Color.black);
		pageNumberText.setFont(new Font("verdana", 1, 12)); //2nd arg Font.BOLD
		pageNumberText.setForeground(Color.black);
		previousPage.setFont(new Font("verdana", 1, 12)); 
		nextPage.setFont(new Font("verdana", 1, 12)); 
		//pageNumberText.setEditable(false);
		//nextPage.setPreferredSize(new Dimension(20, 20));
		pageNavPanel.add(previousPage);
		pageNavPanel.add(pageNumberText);
		pageNavPanel.add(nextPage);

		nextPage.addActionListener(new NextPageHandler());
		previousPage.addActionListener(new PreviousPageHandler());
		intensity.addActionListener(new IntensityHandler());
		colorCode.addActionListener(new ColorCodeHandler());
		intenColorCode.addActionListener(new IntenColorCodeHandler());
		relevanceToggle.addItemListener(new RelevanceToggleHandler());
		pack();
		setSize(1100, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);


		button = new JButton[101];
		imageLabel = new JLabel[101];
		imgRelevanceCheckBox = new JCheckBox[101];
		imageButton = new JPanel[101];
		/*This for loop goes through the images in the database and stores them as icons and adds
		 * the images to JButtons and then to the JButton array
		 */ 
		for (int i = 1; i < 101; i++) {
			ImageIcon icon;
			icon = new ImageIcon(getClass().getResource("images/" + i + ".jpg"));

			if(icon != null){
				//create imageButton
				imageButton[i] = new JPanel();
				imageButton[i].setLayout(new BorderLayout());
				JPanel eastPanel = new JPanel();
				eastPanel.setLayout(new BorderLayout());
				imageButton[i].add(eastPanel, BorderLayout.EAST);
				
				//create image number text & space based on the number of digits in the image number
				if (i >= 1 && i <= 9) imageLabel[i] = new JLabel("(" + i + ")     "); //i is 1 digit
				else if (i < 100) imageLabel[i] = new JLabel("(" + i + ")   "); //i is 2 digits
				else imageLabel[i] = new JLabel("(" + i + ") "); //i is 3 digits
				imageLabel[i].setFont(new Font("verdana", 1, 12)); 
				imageLabel[i].setForeground(Color.black);
				eastPanel.add(imageLabel[i], BorderLayout.CENTER);
				
				//create image relevance check box
				imgRelevanceCheckBox[i - 1] = new JCheckBox("Relevant", false);
				imgRelevanceCheckBox[i - 1].setFont(new Font("verdana", 1, 12));
				imgRelevanceCheckBox[i - 1].addItemListener(new ImageRelevanceHandler(i));
				imgRelevanceCheckBox[i - 1].setVisible(false); //starts hidden
				eastPanel.add(imgRelevanceCheckBox[i - 1], BorderLayout.SOUTH);

				//create button containing image
				button[i] = new JButton(icon);
				imageButton[i].add(button[i], BorderLayout.CENTER);
				//panelBottom1.add(button[i]);
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonOrder[i] = i;
			}
		}
		
        readIntensityFile();
        readColorCodeFile();
        initIntenCCMatrix();
        readResolutionFile();
        
        for (int i = 0; i < 100; ++i){
        	relevantImage[i] = false;
        }
        
		displayFirstPage(); 
	}
	
	/**
	 * placeholder for pageTextPanel updating, also need listener function to accept page input
	 */
	public void updatePageNumber(){
		pageNumberText.setText("Page " + pageNo + "/5");
	}

	/*This method opens the intensity text file containing the intensity matrix with the histogram bin values for each image.
	 * The contents of the matrix are processed and stored in a two dimensional array called intensityMatrix.
	 */
	public void readIntensityFile(){
		Scanner read;
		String line = "";
		int lineNumber = 0;
		
		try{
			read = new Scanner(new File ("intensity.txt"));
			
			while (read.hasNextLine() && (lineNumber < 100)){
				line = read.nextLine();
				String[] tokens = line.split(",");
				
				for (int i = 0; i < tokens.length; i++){
					intensityMatrix[lineNumber][i] = Integer.parseInt(tokens[i]);
				}
				
				++lineNumber;
			}
		}
		catch(FileNotFoundException EE){
			System.out.println("The file intensity.txt does not exist");
		}

	}

	/*This method opens the color code text file containing the color code matrix with the histogram bin values for each image.
	 * The contents of the matrix are processed and stored in a two dimensional array called colorCodeMatrix.
	 */
	private void readColorCodeFile(){
		Scanner read;
		String line = "";
		int lineNumber = 0;
		
		try{
			read = new Scanner(new File ("colorCodes.txt"));

			while (read.hasNextLine() && (lineNumber < 100)){
				line = read.nextLine();
				String[] tokens = line.split(",");

				for (int i = 0; i < tokens.length; i++){
					colorCodeMatrix[lineNumber][i] = Integer.parseInt(tokens[i]);
				}
				
				++lineNumber;
			}
		}
		catch(FileNotFoundException EE){
			System.out.println("The file colorCodes.txt does not exist");
		}


	}
	
	/* This method combines the intensity and color code matrices into the
	 * relFeedbackMatrix used for the relevance feedback algorithm.
	 */
	private void initIntenCCMatrix(){
		//transfer intensity values
		for (int i = 0; i < 100; ++i){
			for (int j = 0; j < 25; ++j)
				intenColorCodeMatrix[i][j] = intensityMatrix[i][j];
		}
		//transfer color code values
		for (int i = 0; i < 100; ++i){
			for (int j = 25; j < 89; ++j)
				intenColorCodeMatrix[i][j] = colorCodeMatrix[i][j - 25];
		}
		//divide each bin by the image's resolution
		for (int i = 0; i < 100; ++i){
			for (int j = 0; j < 89; ++j)
				intenColorCodeMatrix[i][j] /= resolutions[i]; 
		}
		//normalize each feature's values
		for (int i = 0; i < 89; ++i){
			SummaryStatistics featureStats = new SummaryStatistics();
			for (int j = 0; j < 100; ++j){
				featureStats.addValue(intenColorCodeMatrix[j][i]);
			}
			double featureMean = featureStats.getMean();
			double featureStdev = featureStats.getStandardDeviation();
			for (int j = 0; j < 100; ++j){
				double oldVal = intenColorCodeMatrix[j][i];
				intenColorCodeMatrix[j][i] = (oldVal - featureMean)/featureStdev;
			}
		}
		
	}

	/*This method opens the resolution text file containing the intensity matrix with the histogram bin values for each image.
	 * The contents of the matrix are processed and stored in a two dimensional array called intensityMatrix.
	 */
	public void readResolutionFile(){
		Scanner read;
		String line = "";
		int lineNumber = 0;
		
		try{
			read = new Scanner(new File ("resolution.txt"));

			while (read.hasNextLine() && (lineNumber < 100)){
				line = read.nextLine();
				int res = Integer.parseInt(line);
				
				resolutions[lineNumber++] = res;
			}
		}
		catch(FileNotFoundException EE){
			System.out.println("The file resolution.txt does not exist");
		}

	}
	
	/*This method displays the first twenty images in the panelBottom.  The for loop starts at number one and gets the image
	 * number stored in the buttonOrder array and assigns the value to imageButNo.  The button associated with the image is 
	 * then added to panelBottom1.  The for loop continues this process until twenty images are displayed in the panelBottom1
	 */
	private void displayFirstPage(){
		int imageButNo = 0;
		imageCount = 1; //reset image count
		pageNo = 1;
		panelBottom1.removeAll(); 
		for(int i = 1; i < 21; i++){
			//System.out.println(button[i]);
			imageButNo = buttonOrder[i];
			//panelBottom1.add(button[imageButNo]);
			panelBottom1.add(imageButton[imageButNo]);
			imageCount ++;
		}
		
		pageNumberText.setText("Page " + pageNo + "/5");
		panelBottom1.revalidate();  
		panelBottom1.repaint();

	}

	/*This class implements an ActionListener for each iconButton.  When an icon button is clicked, the image on the 
	 * the button is added to the photographLabel and the picNo is set to the image number selected and being displayed.
	 */ 
	private class IconButtonHandler implements ActionListener{
		int pNo = 0;
		ImageIcon iconUsed;

		IconButtonHandler(int i, ImageIcon j){
			pNo = i;
			iconUsed = j;  //sets the icon to the one used in the button
		}

		public void actionPerformed( ActionEvent e){
			photographLabel.setIcon(iconUsed);
			picNo = pNo;
			photoIconLabel.setText("Query Image: (" + picNo + ")");
		}

	}

	/*This class implements an ActionListener for the nextPageButton.  The last image number to be displayed is set to the 
	 * current image count plus 20.  If the endImage number equals 101, then the next page button does not display any new 
	 * images because there are only 100 images to be displayed.  The first picture on the next page is the image located in 
	 * the buttonOrder array at the imageCount
	 */
	private class NextPageHandler implements ActionListener{

		public void actionPerformed( ActionEvent e){
			int imageButNo = 0;
			int endImage = imageCount + 20;
			if(endImage <= 101){
				panelBottom1.removeAll(); 
				for (int i = imageCount; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					//panelBottom1.add(button[imageButNo]);
					panelBottom1.add(imageButton[imageButNo]);
					imageCount++;

				}
				
				++pageNo;
				pageNumberText.setText("Page " + pageNo + "/5");
				panelBottom1.revalidate();  
				panelBottom1.repaint();
			}
		}

	}

	/*This class implements an ActionListener for the previousPageButton.  The last image number to be displayed is set to the 
	 * current image count minus 40.  If the endImage number is less than 1, then the previous page button does not display any new 
	 * images because the starting image is 1.  The first picture on the next page is the image located in 
	 * the buttonOrder array at the imageCount
	 */
	private class PreviousPageHandler implements ActionListener{

		public void actionPerformed( ActionEvent e){
			int imageButNo = 0;
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			if(startImage >= 1){
				panelBottom1.removeAll();
				/*The for loop goes through the buttonOrder array starting with the startImage value
				 * and retrieves the image at that place and then adds the button to the panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					//panelBottom1.add(button[imageButNo]);
					panelBottom1.add(imageButton[imageButNo]);
					imageCount--;

				}
				
				--pageNo;
				pageNumberText.setText("Page " + pageNo + "/5");
				panelBottom1.revalidate();  
				panelBottom1.repaint();
			}
		}

	}


	/*This class implements an ActionListener when the user selects the intensityHandler button.  The image number that the
	 * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
	 * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one.
	 * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
	 * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
	 * The images are then arranged from most similar to the least.
	 */
	private class IntensityHandler implements ActionListener{

		public void actionPerformed( ActionEvent e){
			int pic = (picNo - 1);

			
			if (pic >= 0 && pic < 100){
				imageDistance [] intensityDistances = getDistances(pic, 0);

				if (intensityDistances != null){
					Arrays.sort(intensityDistances); //sort into ascending order 
					//rearrange button order according to distances
					for(int i = 1; i <= 100; i++){
						buttonOrder[i] = intensityDistances[i - 1].imgNumber;
					}

					displayFirstPage();
				}
			}
			
		}
	}

	/*This class implements an ActionListener when the user selects the colorCode button.  The image number that the
	 * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
	 * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one. 
	 * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
	 * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
	 * The images are then arranged from most similar to the least.
	 */ 
	private class ColorCodeHandler implements ActionListener{

		public void actionPerformed( ActionEvent e){
			int pic = (picNo - 1);
			
			if (pic >= 0 && pic < 100){
				imageDistance [] colorCodeDistances = getDistances(pic, 1);

				if (colorCodeDistances != null){
					Arrays.sort(colorCodeDistances); //sort into ascending order 
					//rearrange button order according to distances
					for(int i = 1; i <= 100; i++){
						buttonOrder[i] = colorCodeDistances[i - 1].imgNumber;
					}

					displayFirstPage();
				}
			}
		}
	}
	
	/*This class implements an ActionListener when the user selects the colorCode button.  The image number that the
	 * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
	 * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one. 
	 * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
	 * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
	 * The images are then arranged from most similar to the least.
	 */ 
	private class IntenColorCodeHandler implements ActionListener{

		public void actionPerformed( ActionEvent e){
			int pic = (picNo - 1);
			
			if (relevanceEnabled){ //do nothing if relevance is not enabled
				if (pic >= 0 && pic < 100){
					if (firstRFIteration == false){ //calculate new weights
						calculateRelevanceWeights();
					}
					imageDistance [] iccDistances = getDistances(pic, 2);

					if (iccDistances != null){
						Arrays.sort(iccDistances); //sort into ascending order 
						//rearrange button order according to distances
						for(int i = 1; i <= 100; i++){
							buttonOrder[i] = iccDistances[i - 1].imgNumber;
						}

						displayFirstPage();
					}
					firstRFIteration = false;
				}
			}
		}
	}
	
	/*This class implements an ItemListener when the user selects the Relevance Toggle check box.
	 * Toggling the relevance feature on sets the image relevance to be visible. Checking the
	 * relevance toggle box resets the feature weights stored in rfWeightedMatrix[100].
	 * Resetting the relevance weights can be achieved by unchecking/checking the Relevance Toggle.
	 */ 
	private class RelevanceToggleHandler implements ItemListener{

		public void itemStateChanged( ItemEvent e){
			if (e.getStateChange()  == ItemEvent.SELECTED){ //relevanceToggle checked
				//reset feature weights
				for (int i = 0; i < 89; ++i){
					intenColorCodeMatrix[100][i] = 1/89;
					//rfWeightedMatrix[100][i] = 1/89; 
				}
				// reset feature values (bin count/img size)
				/*for (int i = 0; i < 100; ++i){
					for (int j = 0; j < 89; ++j){
						rfWeightedMatrix[i][j] = intenColorCodeMatrix[i][j] / resolutions[i];
					}
				}*/
				//show image relevance check boxes
				for (int i = 0; i < 100; ++i){
					imgRelevanceCheckBox[i].setVisible(true);
				}
				firstRFIteration = true; //use base weights for first rf iteration
				relevanceEnabled = true;
			}
			else{ // relevanceToggle unchecked
				//hide image relevance check boxes
				for (int i = 0; i < 100; ++i){
					imgRelevanceCheckBox[i].setVisible(false);
				}
				//un-check image relevance check boxes
				for (int i = 0; i < 100; ++i){
					imgRelevanceCheckBox[i].setSelected(false);
				}
				//reset relevance flags
				for (int i = 0; i < 100; ++i){
					relevantImage[i] = false;
				}
				relevanceEnabled = false;
				relevantCount = 0;
			}
		}
	}
	
	/*This class implements an ItemListener that is used for each image's relevance check box.
	 * When an image is marked as relevant, its respective index in relevantImage[] is marked true.
	 * When an image's relevance check box is unchecked, its index in relevantImage[] is set to false.
	 */ 
	private class ImageRelevanceHandler implements ItemListener{
		int pNo = 0;

		ImageRelevanceHandler(int i){
			pNo = i;
		}
		
		public void itemStateChanged( ItemEvent e){
			if (e.getStateChange()  == ItemEvent.SELECTED){ //relevanceToggle checked
				relevantImage[pNo - 1] = true; //flag image as relevant
				++relevantCount;
				//System.out.println("img is RELEVANT: " + pNo);
			}
			else{ // relevanceToggle unchecked
				relevantImage[pNo - 1] = false; //remove relevance flag
				--relevantCount;
				//System.out.println("not rel img:" + pNo);
			}
		}
	}
	
	
	/**
	 * getDistances calculates the Manhattan distances between picNo and all other images
	 * using the histogram data set specified by mode.
	 * @param picNo Image number - 1, for use in an array. Identifies the query image.
	 * @param mode Indicates whether to calculate distances to picNo using the intensity
	 * 		  or color code matrices. Set mode to 0 to calculate intensity distances, set
	 * 		  mode to 1 to calculate color code distances.
	 * @return Unsorted imageDistance array, where each element of the array contains the image
	 * 		   file number and the distance from (picNo + 1) to that image.
	 */
	private imageDistance[] getDistances( int picNo, int mode){
		imageDistance [] distances = new imageDistance[100];
		
		if (mode == 0){ //intensity distances
			for (int i = 0; i < 100; ++i){
				double distSum = 0; //distance between picNo and image i
				
				for (int j = 0; j < 25; ++j){
					// add ((Hi(j)/(Mi * Ni)) - ((Hk(j)/(Mk * Nk)) to the sum 
					double dist = Math.abs(((double)intensityMatrix[picNo][j] / resolutions[picNo]) 
							- ((double)intensityMatrix[i][j] / resolutions[i]));
					distSum += dist;
				}
				
				distances[i] = new imageDistance(i + 1, distSum);
			}
		}
		else if (mode == 1) { //color code distances
			for (int i = 0; i < 100; ++i){
				double distSum = 0; //distance between picNo and image i
				
				for (int j = 0; j < 64; ++j){
					// add ((Hi(j)/(Mi * Ni)) - ((Hk(j)/(Mk * Nk)) to the sum 
					double dist = Math.abs(((double)colorCodeMatrix[picNo][j] / resolutions[picNo]) 
							- ((double)colorCodeMatrix[i][j] / resolutions[i]));
					distSum += dist;
				}
				
				distances[i] = new imageDistance(i + 1, distSum);
			}
		}
		else if (mode == 2) { //intensity + color code distances using RF feature weights
			for (int i = 0; i < 100; ++i){
				double distSum = 0; //distance between picNo and image i
				
				for (int j = 0; j < 89; ++j){
					double dist = Math.abs((intenColorCodeMatrix[picNo][j] ) 
							- (intenColorCodeMatrix[i][j]));
					dist *= intenColorCodeMatrix[100][j]; //multiply by feature's weight
					distSum += dist;
				}
				
				distances[i] = new imageDistance(i + 1, distSum);
			}
		}
		else{ //invalid mode
			distances = null;
		}
		
		return distances;
	}
	
	/* Computed new feature weights using the relevant images to determine
	 * which features are more important. Stores the weights in intenColorCodeMatrix[100].
	 */
	private void calculateRelevanceWeights(){
		double relMatrix [][] = new double[relevantCount + 1][89];
		double means [] = new double[relevantCount];
		double stdevs [] = new double[relevantCount];
		double minStdev = 0;
		int rfmIdx = 0;
		// put relevant image info into relFeatureMatrix
		for (int i = 0; (i < 100) && (rfmIdx < relevantCount); ++i){
			if (relevantImage[i] == true){
				for (int j = 0; j < 89; ++j)
					relMatrix[rfmIdx][j] = intenColorCodeMatrix[i][j];
				++rfmIdx;
			}
		}
		// calculate new feature weights
		for (int i = 0; i < 89; ++i){
			SummaryStatistics featureStats = new SummaryStatistics();
			for (int j = 0; j < relevantCount; ++j){
				featureStats.addValue(relMatrix[j][i]);
			}
			stdevs[i] = featureStats.getStandardDeviation();
			means[i] = featureStats.getMean();
			if (i == 0) 
				minStdev = stdevs[i];
			else if ((stdevs[i] < minStdev) && (stdevs[i] != 0))
				minStdev = stdevs[i];
		}
		// set feature weights
		for (int i = 0; i < 89; i++){
			if (stdevs[i] != 0)
				relMatrix[relevantCount][i] = 1 / stdevs[i];
			else { //handle stdev == 0 case
				if (means[i] != 0){
					stdevs[i] = .5 * minStdev;
					relMatrix[relevantCount][i] = 1 / stdevs[i];
				}
				else{
					relMatrix[relevantCount][i] = 0;
				}
			}
		}
		// normalize new feature weights
		double weightSum = 0;
		for (int i = 0; i < 89; ++i){
				weightSum += relMatrix[relevantCount][i];
		}
		if (weightSum == 0) return;
		for (int i = 0; i < 89; ++i){
			relMatrix[relevantCount][i] /= weightSum;
		}
		//transfer new weights to iCCMatrix
		for (int i = 0; i < 89; ++i){
			intenColorCodeMatrix[100][i] = relMatrix[relevantCount][i];
		}
	}
	
	/**
	 * imageDistance provides a structure that contains an image number and a distance
	 * to that image. Allows the Manhattan distances to be sorted while maintaining
	 * image number information.
	 * @author Connor Paul McGrath
	 *
	 */
	private class imageDistance implements Comparable<imageDistance>{
		int imgNumber; //image file number
		double distanceTo; //distance from query to imgNumber
		
		public imageDistance(int iNo, double d){
			imgNumber = iNo;
			distanceTo = d;
		}
		
		public int compareTo(imageDistance other){
			int retVal = 0;
			
			if (other == null) throw new NullPointerException("NULL imageDistance error");
			
			double diff = this.distanceTo - other.distanceTo;
			
			if (diff > 0) retVal = 1;
			else if (diff < 0) retVal = -1;
			else retVal = 0;
			
			return retVal;
		}
		
		@Override
		public boolean equals(Object other){
			boolean retVal = false;
			if ((other instanceof imageDistance) && (other != null)){
				retVal = ((imageDistance)other).distanceTo == this.distanceTo; 
			}
			return retVal;
		}
	}

} //end CBIR class
