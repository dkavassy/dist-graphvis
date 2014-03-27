/*
 * @(\#) SomeClass.java 1.1 10 February 14
 *
 * Copyright (\copyright) 2014 University of York & British Telecommunications plc
 * This Software is granted under the MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package engine;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** 
 * A class that represents coordinates of a vector. 
 * This class contain x and y coordinates. 
 * <p> 
 * @author Daniel Kavassy 
 * @author Shan Huang 
 * @version 1.1 Initial development. 
 */ 
public class CoordinatesWritable implements org.apache.hadoop.io.Writable {
	
	private double x = 0.0;
	private double y = 0.0;
	
	public CoordinatesWritable() {
		super();
	}
	
	/** 
	 * Constructor of CoordinatesWritable
	 * @param x the x axis value. 
	 * @param y the y axis value.
	 * @see CoordinatesWritable 
	 */
	public CoordinatesWritable(double x, double y) {
		super();
		set(x, y);
	}

	/** 
	 * Set new coordinates value.
	 * @param x the x axis value. 
	 * @param y the y axis value. 
	 */
	public void set(double x2, double y2) {
		this.x = x2;
		this.y = y2;
	}

	
	@Override
	public void readFields(DataInput in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
	}

	@Override
	public String toString() {
		return "x: " + x + "; y: " + y;
	}
	
	/** 
	 * Return x
	 * @return a double 
	 */
	public double getX() {
		return x;
	}
	
	/** 
	 * Return y
	 * @return a double 
	 */
	public double getY() {
		return y;
	}

	/** 
	 * Calculate the length of this vector. 
	 * @return a double which is the length of this vector. 
	 * @see CoordinatesWritable 
	 */
	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	/** 
	 * Calculate the sum of two vectors.
	 * @param other the CoordinatesWritable added to this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see CoordinatesWritable 
	 */
	public CoordinatesWritable add(CoordinatesWritable other) {
		return new CoordinatesWritable(getX() + other.getX(), getY()
				+ other.getY());
	}

	/** 
	 * Calculate the difference between two vectors.
	 * @param other CoordinatesWritable subtracted from this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see CoordinatesWritable 
	 */
	public CoordinatesWritable subtract(CoordinatesWritable other) {
		return new CoordinatesWritable(getX() - other.getX(), getY()
				- other.getY());
	}

	public CoordinatesWritable multiply(double scalar) {
		return new CoordinatesWritable(getX() * scalar, getY() * scalar);
	}

	/** 
	 * multiply this vector to another.
	 * @param other CoordinatesWritable multiplied to this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see CoordinatesWritable 
	 */
	public CoordinatesWritable multiply(CoordinatesWritable other) {
		return new CoordinatesWritable(getX() * other.getX(), getY()
				* other.getY());
	}
	
	public CoordinatesWritable divide(double scalar) {
		return new CoordinatesWritable(getX() / scalar, getY() / scalar);
	}
	 
	/** 
	 * Calculate the minor one between this vector and a number. 
	 * Respectively compare x and y to the parameter and take the minor one.
	 * @param a double that will be compared with this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see CoordinatesWritable 
	 */
	public CoordinatesWritable min(double t) {
		return new CoordinatesWritable(Math.min(getX(), t), Math.min(getY(), t));
	}

	/** 
	 * Compare two vectors, if their x and y are all the same, then they are equal.
	 * @param another CoordinatesWritable. 
	 * @return boolean. 
	 * @see CoordinatesWritable 
	 */
	@Override
	public boolean equals(Object obj) {
		CoordinatesWritable other = (CoordinatesWritable) obj;
		if (other.getX() == x && other.getY() == y) {
			return true;
		}

		return false;
	}

}
