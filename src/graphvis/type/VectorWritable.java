/*
 * @(\#) CoordinatesWritable.java 1.1 10 March 20
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
package graphvis.type;

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
public class VectorWritable implements org.apache.hadoop.io.Writable 
{
	
	private double x = 0.0;
	private double y = 0.0;
	
	public VectorWritable() 
	{
		super();
	}
	
	/** 
	 * Constructor of CoordinatesWritable
	 * @param x the x axis value. 
	 * @param y the y axis value.
	 * @see VectorWritable 
	 */
	public VectorWritable(double x, double y) 
	{
		super();
		set(x, y);
	}

	/** 
	 * Set new coordinates value.
	 * @param x2 the x axis value. 
	 * @param y2 the y axis value. 
	 */
	public void set(double x2, double y2) 
	{
		this.x = x2;
		this.y = y2;
	}

	
	@Override
	public void readFields(DataInput in) throws IOException 
	{
		x = in.readDouble();
		y = in.readDouble();
	}

	@Override
	public void write(DataOutput out) throws IOException 
	{
		out.writeDouble(x);
		out.writeDouble(y);
	}

	@Override
	public String toString() 
	{
		return "x: " + x + "; y: " + y;
	}
	
	/** 
	 * Return x
	 * @return a double 
	 */
	public double getX() 
	{
		return x;
	}
	
	/** 
	 * Return y
	 * @return a double 
	 */
	public double getY() 
	{
		return y;
	}

	/** 
	 * Calculate the length of this vector. 
	 * @return a double which is the length of this vector. 
	 * @see VectorWritable 
	 */
	public double length() 
	{
		return Math.sqrt(x * x + y * y);
	}

	/** 
	 * Calculate the sum of two vectors.
	 * @param other the CoordinatesWritable added to this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see VectorWritable 
	 */
	public VectorWritable add(VectorWritable other)
	{
		return new VectorWritable(getX() + other.getX(), getY()
				+ other.getY());
	}

	/** 
	 * Calculate the difference between two vectors.
	 * @param other CoordinatesWritable subtracted from this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see VectorWritable 
	 */
	public VectorWritable subtract(VectorWritable other) 
	{
		return new VectorWritable(getX() - other.getX(), getY()
				- other.getY());
	}

	public VectorWritable multiply(double scalar)
	{
		return new VectorWritable(getX() * scalar, getY() * scalar);
	}

	/** 
	 * multiply this vector to another.
	 * @param other CoordinatesWritable multiplied to this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see VectorWritable 
	 */
	public VectorWritable multiply(VectorWritable other) 
	{
		return new VectorWritable(getX() * other.getX(), getY() * other.getY());
	}
	
	public VectorWritable divide(double scalar)
	{
		return new VectorWritable(getX() / scalar, getY() / scalar);
	}
	 
	/** 
	 * Calculate the minor one between this vector and a number. 
	 * Respectively compare x and y to t and take the minor one.
	 * @param t a double that will be compared with this vector. 
	 * @return a new CoordinatesWritable. 
	 * @see VectorWritable 
	 */
	public VectorWritable min(double t)
	{
		return new VectorWritable(Math.min(getX(), t), Math.min(getY(), t));
	}

	/** 
	 * Compare two vectors, if their x and y are all the same, then they are equal.
	 * @param another CoordinatesWritable. 
	 * @return boolean. 
	 * @see VectorWritable 
	 */
	@Override
	public boolean equals(Object another) 
	{
		VectorWritable other = (VectorWritable) another;
		
		if (other.getX() == x && other.getY() == y) 
		{
			return true;
		}

		return false;
	}

}
