// ImageManipulator.h
// Provides declarations for the ImageManipulator class, which provides
// functionality for the following Image transformations:
// translation, scaling, shearing, and rotation.
// The class also provides a function to handle any combination of 
// the four aforementioned transformations, comboTransform.
// Author: Connor Paul McGrath
// Fall 2017 CSS 487 Program 1

#ifndef _IMAGE_MANIPULATOR_H
#define _IMAGE_MANIPULATOR_H

#include "Image.h"

class ImageManipulator
{
public:
	// comboTransform returns a transformation of an input image
	// using translation, scaling, shearing, and rotation.
	// Preconditions:	toTransform is a valid Image and is a correctly 
	//					formatted GIF image.
	//					args's arguments are in the order Sx, Sy, tc, tr, theta, k
	// Postconditions:	Creates an image output.gif that results from the combination
	//					transformation using the parameters given in args.
	static Image comboTransform(const Image& toTransform, const float args[6]);

	// scaleImage returns a scaling transformation of the input image,
	// with the scaling proportions determined by the values scaleX and scaleY.
	// Preconditions:	toScale is a valid Image and is a correctly 
	//					formatted GIF image
	// Postconditions:	Creates an image output.gif that results from the scaling
	//					transformation using parameters scaleX and scaleY.
	static Image scaleImage(const Image& toScale, const float scaleX,
		const float scaleY);

	// shearImage returns a shearing transformation of the input image,
	// using shearCoeff to determine the amount to shear the image.
	// Preconditions:	toShear is a valid Image and is a correctly 
	//					formatted GIF image
	// Postconditions:	Creates an image output.gif that results from the shearing
	//					transformation using parameter shearCoeff.
	static Image shearImage(const Image& toShear, const float shearCoeff);

	// rotateImage returns a rotational transformation of the input image,
	// using thetaDegrees to determine the amount to rotate the image.
	// Preconditions:	toRotate is a valid Image and is a correctly 
	//					formatted GIF image
	//					theta is a rotational measurement given in degrees
	// Postconditions:	Creates an image output.gif that results from the rotation
	//					transformation using parameter thetaDegrees.
	static Image rotateImage(const Image& toRotate, const float thetaDegrees);

	// translateImage returns a translation of the input image,
	// shifting the image by changeInCol in the x direction and
	// by changeInRow in the y direction.
	// Preconditions:	toTranslate is a valid Image and is a correctly 
	//					formatted GIF image
	// Postconditions:	Creates an image output.gif that results from the scaling
	//					transformation using the parameters changeInCol and changeInRow.
	static Image translateImage(const Image& toTranslate, const int changeInCol,
		const int changeInRow);

private:
	struct matrix; // Used to contain data for 2x2 matrices and 2x1 vectors.
	
	// Disallow instantiation of the static class.
	ImageManipulator();
	~ImageManipulator();

	// transformImage is a helper function for the public transformation functions
	// that performs transformation calculations using the provided source image,
	// inverse matrices and coefficients for the scaling, shearing, and rotation factors,
	// and translation offset values tc (column translation) and tr (row translation).
	// Preconditions:	source is a valid Image and is a correctly formatted GIF image.
	//					inverseTransforms[] contains the inverse matrices for scaling, 
	//					shearing, and rotation, in that order.
	//					inverseCoeffs[] contains the coefficients for the scaling, 
	//					shearing, and rotation matrices, in that order.
	// Postconditions:	Creates an image output.gif that results from the
	//					translation, scaling, shearing, and rotation 
	//					transformations using the functions parameters.
	static Image ImageManipulator::transformImage(const Image& source, 
		const matrix inverseMatrices[3], const double inverseCoeffs[3],
		const int tc, const int tr);

	// interpPixelVal is a helper function for transformImage that calculates
	// the interpolated pixel value for a (px, py) coordinate using the four
	// neighboring pixels.
	// Preconditions:	source is a valid Image and is a correctly formatted GIF image.
	//					origColBound and origRowBound are the last valid column and row
	//					indices for the source image, respectively.
	// Postconditions:	Returns the interpolated pixel color value for the given (px, py)
	//					coordinate. 
	//					Returns black when interpolation requires an invalid pixel.
	static pixel interpPixelVal(const Image& source, const float px, const float py,
		const int origColBound, const int origRowBound);

	// multiplyMatrices is a helper function used for calculating the product of
	// a 2x2 matrix and a (px, py) vector.
	// Preconditions:	twoByTwo's four members (a, b, c, d) have been initialized
	//					with the appropriate values for its respective transformation.
	//					vector's two members (a, b) represent a point (px, py), where
	//					(px, py) represents a (column, row) pair.
	// Postconditions:	Returns the vector resulting from the 2x2 matrix being
	//					multiplied by a 2x1 vector.
	static matrix multiplyMatrices(matrix twoByTwo, matrix vector);
};

#endif
