package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.CoordinatesWritable;

public class CoordinatesWritableTest {
	
	CoordinatesWritable c = new CoordinatesWritable(12.0,34.0);;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToString() {
		
		assertEquals("x: 12.0; y: 34.0",c.toString());
	}

	@Test
	public void testLength() {
		
		assertEquals(Math.sqrt(12.0*12.0+34.0*34.0),c.length(),0);
	}

	@Test
	public void testAdd() {
		CoordinatesWritable other = new CoordinatesWritable(45.0,78.0);
		assertEquals(12.0+45.0,c.add(other).getX(),0);
		assertEquals(34.0+78.0,c.add(other).getY(),0);
	}

	@Test
	public void testSubtract() {
		CoordinatesWritable other = new CoordinatesWritable(45.0,78.0);
		assertEquals(12.0-45.0,c.subtract(other).getX(),0);
		assertEquals(34.0-78.0,c.subtract(other).getY(),0);
	}

	@Test
	public void testMultiplyDouble() {
		double scalar = 39.7;
		assertEquals(12.0*39.7,c.multiply(scalar).getX(),0);
		assertEquals(34.0*39.7,c.multiply(scalar).getY(),0);
	}

	@Test
	public void testMultiplyCoordinatesWritable() {
		CoordinatesWritable other = new CoordinatesWritable(45.0,78.0);
		assertEquals(12.0*45.0,c.multiply(other).getX(),0);
		assertEquals(34.0*78.0,c.multiply(other).getY(),0);
	}

	@Test
	public void testDivide() {
		double scalar = 39.7;
		assertEquals(12.0/39.7,c.divide(scalar).getX(),0);
		assertEquals(34.0/39.7,c.divide(scalar).getY(),0);
	}

	@Test
	public void testMin() {
		double t = 30.7;
		assertEquals(12.0,c.min(t).getX(),0);
		assertEquals(30.7,c.min(t).getY(),0);
	}

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
