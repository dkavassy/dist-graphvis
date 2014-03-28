/*
 * @(\#) CoordinatesWritableTest.java 1.1 10 March 20
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
package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.CoordinatesWritable;
import engine.FruchtermanReingoldGraphVis;


/** 
 * Contains unit tests for {@link CoordinatesWritable} 
 * <p> 
 * @author Shan Huang 
 * @version 1.1 Initial development. 
 */ 
public class CoordinatesWritableTest {
	
	CoordinatesWritable c = new CoordinatesWritable(12.0,34.0);;
	

	/**
	 * Test toString() method
	 */
	@Test
	public void testToString() {
		
		assertEquals("x: 12.0; y: 34.0",c.toString());
	}

	/**
	 * Test length() method, calculate the length of a CoordinatesWritable example
	 * result should be the length of this example
	 */
	@Test
	public void testLength() {
		
		assertEquals(Math.sqrt(12.0*12.0+34.0*34.0),c.length(),0);
	}

	/**
	 * Test add() method which adds two CoordinatesWritable together
	 * The return value should be a new CoordinatesWritable whose x and y are respectively
	 * the sum of x and y of two given CoordinatesWritables
	 */
	@Test
	public void testAdd() {
		CoordinatesWritable other = new CoordinatesWritable(45.0,78.0);
		assertEquals(12.0+45.0,c.add(other).getX(),0);
		assertEquals(34.0+78.0,c.add(other).getY(),0);
	}

	/**
	 * Test subtract() method which subtract one CoordinatesWritable with another
	 * The return value should be a new CoordinatesWritable whose x and y are respectively
	 * the result of subtracting x and y of the  given CoordinatesWritables
	 */
	@Test
	public void testSubtract() {
		CoordinatesWritable other = new CoordinatesWritable(45.0,78.0);
		assertEquals(12.0-45.0,c.subtract(other).getX(),0);
		assertEquals(34.0-78.0,c.subtract(other).getY(),0);
	}

	/**
	 * Test multiply() method with double parameter
	 * The return value should be a new CoordinatesWritable whose x and y are respectively
	 * the result of multiplying x and y of the given CoordinatesWritable to the given double
	 */
	@Test
	public void testMultiplyDouble() {
		double scalar = 39.7;
		assertEquals(12.0*39.7,c.multiply(scalar).getX(),0);
		assertEquals(34.0*39.7,c.multiply(scalar).getY(),0);
	}

	/**
	 * Test multiply() method with CoordinatesWritable parameter
	 * The return value should be a new CoordinatesWritable whose x and y are respectively
	 * the result of multiplying x and y of the two given CoordinatesWritables
	 */
	@Test
	public void testMultiplyCoordinatesWritable() {
		CoordinatesWritable other = new CoordinatesWritable(45.0,78.0);
		assertEquals(12.0*45.0,c.multiply(other).getX(),0);
		assertEquals(34.0*78.0,c.multiply(other).getY(),0);
	}

	/**
	 * Test divide() method which divides a CoordinatesWritable with a double
	 * The return value should be a new CoordinatesWritable whose x and y are respectively
	 * the result of dividing x and y of the given CoordinatesWritable with the given double
	 */
	@Test
	public void testDivide() {
		double scalar = 39.7;
		assertEquals(12.0/39.7,c.divide(scalar).getX(),0);
		assertEquals(34.0/39.7,c.divide(scalar).getY(),0);
	}

	/**
	 * Test min() method which compares the x and y coordinates to a double and return a
	 * new CoordinatesWritable whose x and y are the minor ones.
	 */
	@Test
	public void testMin() {
		double t = 30.7;
		assertEquals(12.0,c.min(t).getX(),0);
		assertEquals(30.7,c.min(t).getY(),0);
	}

	/**
	 * Test equals() method which compares two CoordinatesWritables
	 * Two CoordinatesWritables are the same only when their x and y are all the same
	 */
	@Test
	public void testEqualsObject() {
		CoordinatesWritable other1 = new CoordinatesWritable(12.0,34.0);
		CoordinatesWritable other2 = new CoordinatesWritable(12.0,13.0);
		CoordinatesWritable other3 = new CoordinatesWritable(13.0,34.0);
		CoordinatesWritable other4 = new CoordinatesWritable(34.0,67.0);
		
		assertTrue(c.equals(other1));
		assertTrue(!c.equals(other2));
		assertTrue(!c.equals(other3));
		assertTrue(!c.equals(other4));
	}

}
