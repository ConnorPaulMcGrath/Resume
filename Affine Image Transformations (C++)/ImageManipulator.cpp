// ImageManipulator.cpp
// Provides definitions for the ImageManipulator class, which provides
// functionality for the following Image transformations:
// translation, scaling, shearing, and rotation.
// The class also provides a function to handle any combination of 
// the four aforementioned transformations, comboTransform.
// Author: Connor Paul McGrath
// Fall 2017 CSS 487 Program 1

#include "ImageManipulator.h"
#define _USE_MATH_DEFINES // gives access to M_PI
#include "math.h" // sin, cos, M_PI

// Matrix struct used to represent both 2x2 matrices and 2x1 vectors.
struct ImageManipulator::matrix {
	double a = 0, b = 0, c = 0, d = 0;
};

//----------------- Public methods -------------------------------

// comboTransform returns a transformation of an input image
// using translation, scaling, shearing, and rotation.
// Preconditions:	toTransform is a valid Image and is a correctly 
//					formatted GIF image.
//					args's arguments are in the order Sx, Sy, tc, tr, theta, k
// Postconditions:	Creates an image output.gif that results from the combination
//					transformation using the parameters given in args.
Image ImageManipulator::comboTransform(const Image& toTransform,
const float args[6]) {
	// Sx, Sy, tc, tr, theta, k 
	// Prepare transformImage arguments
	matrix transformMatrices[3];
	// Scalar transform
	transformMatrices[0].a = args[1];
	transformMatrices[0].b = 0;
	transformMatrices[0].c = 0;
	transformMatrices[0].d = args[0];
	// Shear transform
	transformMatrices[1].a = 1;
	transformMatrices[1].b = -1 * args[5];
	transformMatrices[1].c = 0;
	transformMatrices[1].d = 1;
	// Rotation transform
	// Convert degree input to radians for math lib trig functions
	const double radians = args[4] * M_PI / 180;
	transformMatrices[2].a = cos(radians);
	transformMatrices[2].b = sin(radians);
	transformMatrices[2].c = -1 * sin(radians);
	transformMatrices[2].d = cos(radians);

	double inverseCoeffs[3];
	// Scalar coefficient
	inverseCoeffs[0] = 1 / ((transformMatrices[0].a * transformMatrices[0].d) 
		- (transformMatrices[0].b * transformMatrices[0].c));
	// Shear coefficient
	inverseCoeffs[1] = 1 / ((transformMatrices[1].a * transformMatrices[1].d) 
		- (transformMatrices[1].b * transformMatrices[1].c));
	// Rotation coefficient
	inverseCoeffs[2] = 1 / ((transformMatrices[2].a * transformMatrices[2].d) 
		- (transformMatrices[2].b * transformMatrices[2].c));

	// Perform combination transformation
	return transformImage(toTransform, transformMatrices, inverseCoeffs,
		args[2], args[3]);
}

// scaleImage returns a scaling transformation of the input image,
// with the scaling proportions determined by the values scaleX and scaleY.
// Preconditions:	toScale is a valid Image and is a correctly 
//					formatted GIF image
// Postconditions:	Creates an image output.gif that results from the scaling
//					transformation using parameters scaleX and scaleY.
Image ImageManipulator::scaleImage(const Image& toScale, const float scaleX,
const float scaleY) {
	// Create inverse of scalar matrix where a = Sx, b = 0, c = 0, d = Sy
	matrix scalarInverse;
	scalarInverse.a = scaleY;
	scalarInverse.b = 0;
	scalarInverse.c = 0;
	scalarInverse.d = scaleX;
	double inverseCoefficient = 
		1 / ((scalarInverse.a * scalarInverse.d) - (scalarInverse.b * scalarInverse.c));
	
	// Create identity matrix for shear and scalar components of transformation.
	matrix identity;
	identity.a = identity.d = 1;
	identity.b = identity.c = 0;

	// Prepare transformImage arguments
	matrix transformMatrices[3];
	transformMatrices[0] = scalarInverse; // Scalar transform
	transformMatrices[1] = identity; // Shear transform
	transformMatrices[2] = identity; // Rotation transform
	double inverseCoeffs[3];
	inverseCoeffs[0] = inverseCoefficient; // Scalar coefficient
	inverseCoeffs[1] = 1; // Shear coefficient
	inverseCoeffs[2] = 1; // Rotation coefficient

	// Perform scaling transformation
	return transformImage(toScale, transformMatrices, inverseCoeffs, 0, 0);
}

// shearImage returns a shearing transformation of the input image,
// using shearCoeff to determine the amount to shear the image.
// Preconditions:	toShear is a valid Image and is a correctly 
//					formatted GIF image
// Postconditions:	Creates an image output.gif that results from the shearing
//					transformation using parameter shearCoeff.
Image ImageManipulator::shearImage(const Image& toShear, const float shearCoeff) {
	// Create inverse of shearing matrix 
	// where a = 1, b = shearCoeff, c = 0, d = 1
	matrix shearInverse;
	shearInverse.a = 1;
	shearInverse.b = -1 * shearCoeff;
	shearInverse.c = 0;
	shearInverse.d = 1;
	double inverseCoefficient = 
		1 / ((shearInverse.a * shearInverse.d) - (shearInverse.b * shearInverse.c));

	// Create identity matrix for shear and scalar components of transformation.
	matrix identity;
	identity.a = identity.d = 1;
	identity.b = identity.c = 0;

	// Prepare transformImage arguments
	matrix transformMatrices[3];
	transformMatrices[0] = identity; // Scalar transform
	transformMatrices[1] = shearInverse; // Shear transform
	transformMatrices[2] = identity; // Rotation transform
	double inverseCoeffs[3];
	inverseCoeffs[0] = 1; // Scalar coefficient
	inverseCoeffs[1] = inverseCoefficient; // Shear coefficient
	inverseCoeffs[2] = 1; // Rotation coefficient

	// Perform shearing transformation
	return transformImage(toShear, transformMatrices, inverseCoeffs, 0, 0);
}

// rotateImage returns a rotational transformation of the input image,
// using thetaDegrees to determine the amount to rotate the image.
// Preconditions:	toRotate is a valid Image and is a correctly 
//					formatted GIF image
//					theta is a rotational measurement given in degrees
// Postconditions:	Creates an image output.gif that results from the rotation
//					transformation using parameter thetaDegrees.
Image ImageManipulator::rotateImage(const Image& toRotate, 
const float thetaDegrees) {
	// Convert degree input to radians for math lib trig functions
	const double radians = thetaDegrees * M_PI / 180;
	
	// Create inverse of rotation matrix where a = cos, b = -sin, c = sin, d = cos.
	matrix rotationInverse;
	rotationInverse.a = cos(radians);
	rotationInverse.b = sin(radians);
	rotationInverse.c = -1 * sin(radians);
	rotationInverse.d = cos(radians);
	double inverseCoefficient = 1 / ((rotationInverse.a *rotationInverse.d) 
		- (rotationInverse.b * rotationInverse.c));

	// Create identity matrix for shear and scalar components of transformation.
	matrix identity;
	identity.a = identity.d = 1;
	identity.b = identity.c = 0;

	// Prepare transformImage arguments
	matrix transformMatrices[3];
	transformMatrices[0] = identity; // Scalar transform
	transformMatrices[1] = identity; // Shear transform
	transformMatrices[2] = rotationInverse; // Rotation transform
	double inverseCoeffs[3];
	inverseCoeffs[0] = 1; // Scalar coefficient
	inverseCoeffs[1] = 1; // Shear coefficient
	inverseCoeffs[2] = inverseCoefficient; // Rotation coefficient

	// Perform rotation
	return transformImage(toRotate, transformMatrices, inverseCoeffs, 0, 0);
}

// translateImage returns a translation of the input image,
// shifting the image by changeInCol in the x direction and
// by changeInRow in the y direction.
// Preconditions:	toTranslate is a valid Image and is a correctly 
//					formatted GIF image
// Postconditions:	Creates an image output.gif that results from the scaling
//					transformation using the parameters changeInCol and changeInRow.
Image ImageManipulator::translateImage(const Image& toTranslate,
const int changeInCol, const int changeInRow) {
	// Create identity matrix for shear and scalar components of transformation.
	matrix identity;
	identity.a = identity.d = 1;
	identity.b = identity.c = 0;

	// Prepare transformImage arguments
	matrix transformMatrices[3];
	transformMatrices[0] = identity; // Scalar transform
	transformMatrices[1] = identity; // Shear transform
	transformMatrices[2] = identity; // Rotation transform
	double inverseCoeffs[3];
	inverseCoeffs[0] = 1; // Scalar coefficient
	inverseCoeffs[1] = 1; // Shear coefficient
	inverseCoeffs[2] = 1; // Rotation coefficient

	// Perform translation
	return transformImage(toTranslate, transformMatrices, inverseCoeffs,
		changeInCol, changeInRow);
}

//---------------------- Private methods ---------------------------

// transformImage is a helper function for the public transformation functions
// that performs transformation calculations using the provided source image,
// inverse matrices and coefficients for the scaling, shearing, and rotation factors,
// and translation offset values tc (column translation) and tr (row translation).
// Preconditions:	source is a valid Image and is a correctly formatted GIF image.
//					inverseMatrices[] contains the inverse matrices for scaling, 
//					shearing, and rotation, in that order.
//					inverseCoeffs[] contains the coefficients for the scaling, 
//					shearing, and rotation matrices, in that order.
// Postconditions:	Creates an image output.gif that results from the
//					translation, scaling, shearing, and rotation 
//					transformations using the functions parameters.
Image ImageManipulator::transformImage(const Image& source,
const matrix inverseMatrices[3], const double inverseCoeffs[3],
const int tc, const int tr) {
	const int origColBound = source.getCols() - 1, origRowBound = source.getRows() - 1;
	Image transformedImg(origRowBound + 1, origColBound + 1);
	const int centerCol = transformedImg.getCols() / 2, 
			centerRow = transformedImg.getRows() / 2;
	
	// Iterate through every pixel in the output Image, locating 
	// and mapping an an input pixel, if applicable, to it.
	// Finds the input pixel value by performing the inverse transformations
	// to the point (q.a, q.b) and interpolating if necessary.
	for (int i = 0; i < transformedImg.getRows(); ++i) {
		matrix q; // Output pixel vector
		// Position relative to origin and  translate in y direction
		q.b = i - centerRow - tr;

		for (int j = 0; j < transformedImg.getCols(); ++j) {
			// Position relative to origin and  translate in x direction
			q.a = j - centerCol - tc;
			
			// Get input pixel coordinates
			matrix p = multiplyMatrices(inverseMatrices[0], q); // Scale
			p = multiplyMatrices(inverseMatrices[1], p); // Shear
			p = multiplyMatrices(inverseMatrices[2], p); // Rotate
			p.a = (p.a * inverseCoeffs[0] * inverseCoeffs[1] * inverseCoeffs[2]) 
				  + centerCol;
			p.b = (p.b * inverseCoeffs[0] * inverseCoeffs[1] * inverseCoeffs[2])
				  + centerRow;
			
			// Get input color values via interpolation and set output pixel
			pixel rgb = interpPixelVal(source, p.a, p.b, origColBound, origRowBound);
			transformedImg.setPixel(i, j, rgb.red, rgb.green, rgb.blue);
		}
	}
	return transformedImg;
}

// interpPixelVal is a helper function for transformImage that calculates
// the interpolated pixel value for a (px, py) coordinate using the four
// neighboring pixels.
// Preconditions:	source is a valid Image and is a correctly formatted GIF image.
//					origColBound and origRowBound are the last valid column and row
//					indices for the source image, respectively.
// Postconditions:	Returns the interpolated pixel color value for the given (px, py)
//					coordinate. 
//					Returns black when interpolation requires an invalid pixel.
pixel ImageManipulator::interpPixelVal(const Image& source, const float px,
const float py, const int origColBound, const int origRowBound) {
	pixel rgb;

	// Interpolate the pixel's value if (px, py) is within the
	// source image's bounds: ([0, origColBound], [0, origRowBound]).
	if (px >= 0 && px <= origColBound && py >= 0 && py <= origRowBound) {
		const int r = floor(py), c = floor(px);
		const float dx = px - c; // Beta value
		const float dy = py - r; // Alpha value

		// Calculate weights for the four pixels that neighbor pixel (py, px)
		double weights[4];
		weights[0] = (1 - dy) * (1 - dx); // weight for I(r, c)
		weights[1] = dy * (1 - dx); // weight for I(r + 1, c)
		weights[2] = (1 - dy) * dx; // weight for I(r, c + 1)
		weights[3] = dy * dx; // weight for I(r + 1, c + 1)

		// Get color values for four neighboring pixels. If a pixel's 
		// respective weight is 0, we do not need its color values,
		// and can avoid invalid pixel access along the image's borders
		// by using a placeholder pixel in the calculations.
		pixel unusedPixel; // placeholder pixel
		unusedPixel.red = unusedPixel.green = unusedPixel.blue = 0;
		pixel neighbor[4];
		neighbor[0] = source.getPixel(r, c);
		neighbor[1] = weights[1] != 0 ? source.getPixel(r + 1, c) : unusedPixel;
		neighbor[2] = weights[2] != 0 ? source.getPixel(r, c + 1) : unusedPixel;
		neighbor[3] = weights[3] != 0 ? source.getPixel(r + 1, c + 1) : unusedPixel;

		// Calculate values for red, green, blue channels using 
		// weighted averages and neighboring pixels' color channel values.
		rgb.red = static_cast<byte>(
			weights[0] * neighbor[0].red		// (1 - alpha)(1 - beta)I(r, c)
			+ weights[1] * neighbor[1].red		// (alpha)(1 - beta)I(r + 1, c)
			+ weights[2] * neighbor[2].red		// (1 - alpha)( beta)I(r, c + 1)
			+ weights[3] * neighbor[3].red);	// (alpha)(beta)I(r + 1, c + 1)
		rgb.green = static_cast<byte>(
			weights[0] * neighbor[0].green
			+ weights[1] * neighbor[1].green
			+ weights[2] * neighbor[2].green
			+ weights[3] * neighbor[3].green);
		rgb.blue = static_cast<byte>(
			weights[0] * neighbor[0].blue
			+ weights[1] * neighbor[1].blue
			+ weights[2] * neighbor[2].blue
			+ weights[3] * neighbor[3].blue);
	}
	else { // Return black if an invalid pixel is needed for interpolation.
		rgb.red = rgb.green = rgb.blue = 0;
	}
	return rgb;
}

// multiplyMatrices is a helper function used for calculating the product of
// a 2x2 matrix and a (px, py) vector.
// Preconditions:	twoByTwo's four members (a, b, c, d) have been initialized
//					with the appropriate values for its respective transformation.
//					vector's two members (a, b) represent a point (px, py), where
//					(px, py) represents a (column, row) pair.
// Postconditions:	Returns the vector resulting from the 2x2 matrix being
//					multiplied by a 2x1 vector.
ImageManipulator::matrix ImageManipulator::multiplyMatrices(matrix twoByTwo,
matrix vector) {
	matrix result; 
	result.a = (twoByTwo.a * vector.a) + (twoByTwo.b * vector.b);
	result.b = (twoByTwo.c * vector.a) + (twoByTwo.d * vector.b);
	return result;
}

// Private constructor and destructor. ImageManipulator is a static class.
ImageManipulator::ImageManipulator(){}
ImageManipulator::~ImageManipulator(){}
