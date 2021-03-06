/*
 * @(\#) VertexValueWritable.java 1.1 10 March 20

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
 * A class that can be used as the value of a vertex. 
 * This class contains the position and displacement of a vertex. 
 * <p> 
 * @author Daniel Kavassy 
 * @version 1.1 Initial development. 
 */ 
public class VertexValueWritable implements org.apache.hadoop.io.Writable
{
	
	private VectorWritable pos  = new VectorWritable();
	private VectorWritable disp = new VectorWritable();
	
	/** 
	 * Default constructor.
	 */
	public VertexValueWritable()
	{
		super();
	}
	
	/** 
	 * Constructor.
	 */
	public VertexValueWritable(VectorWritable pos, VectorWritable disp)
	{
		super();
		set(pos, disp);
	}

	@Override
	public void readFields(DataInput in) throws IOException 
	{
		pos.readFields(in);
		disp.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException 
	{
		pos.write(out);
		disp.write(out);
	}
	

	/** 
	 * Return the position of the vertex
	 * @return a CoordinatesWritable 
	 */
	public VectorWritable getPos() 
	{
		return pos;
	}
	
	/** 
	 * Return the displacement of the vertex
	 * @return a CoordinatesWritable 
	 */
	public VectorWritable getDisp() 
	{
		return disp;
	}

	/** 
	 * Set new vertex value.
	 * @param pos the position. 
	 * @param disp  the displacement. 
	 */
	public void set(VectorWritable pos, VectorWritable disp) 
	{
		this.pos  = pos;
		this.disp = disp;
	}

}
