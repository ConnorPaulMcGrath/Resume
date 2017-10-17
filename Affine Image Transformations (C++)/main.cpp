// main.cpp
// Program used to test the functionality of ImageManipulator class
// by transforming, scaling, shearing, and rotating the image "test.gif".
// Author: Connor McGrath
// Fall 2017 CSS 487

#include "Image.h"
#include "ImageManipulator.h"
#include <cstdlib>

// Preconditions:  test.gif exists and is a correctly formatted GIF image
//				   argv's arguments are in the order Sx, Sy, tc, tr, theta, k
// Postconditions: Creates an image output.gif that results from the combination
//				   transformation parameters given in argv.
int main(int argc, char *argv[]){
	float transformArgs[6]; // Indices 0 to 5: Sx, Sy, tc, tr, theta, k 
	Image input("test.gif");
	Image output;

	// Extract argument data for combination transformation
	if (argc == 7) {
		for (int i = 0; i < 6; ++i) {
			transformArgs[i] = atof(argv[i + 1]);
		}
	}

	// Combination transformation test
	
	/* Sample input data for combination transform
	transformArgs[0] = 1.5;
	transformArgs[1] = 1.5;
	transformArgs[2] = 20;
	transformArgs[3] = 40;
	transformArgs[4] = 20;
	transformArgs[5] = .5;*/
	output = ImageManipulator::comboTransform(input, transformArgs);

	// Test for Rotation by 20 degrees
	//output = ImageManipulator::rotateImage(input, 20);

	// Test for Scale by 1.5 in both x and y
	//output = ImageManipulator::scaleImage(input, 1.5, 1.5);

	// Test for Translation by 20 in x and 40 in y
	//output = ImageManipulator::translateImage(input, 20, 40);

	// Test for Shear by 0.5
	//output = ImageManipulator::shearImage(input, 0.5);

	output.writeImage("output.gif");
	return 0;
}
