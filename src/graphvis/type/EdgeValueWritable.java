/*
 * @(\#) EdgeValueWritable.java 1.1 10 March 20

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

import org.apache.hadoop.io.LongWritable;

/** 
 * A class that can be used as the value of an edge. 
 * This class contains edge weight and the target position of an edge. 
 * <p> 
 * @author Daniel Kavassy 
 * @version 1.1 Initial development. 
 */ 
public class EdgeValueWritable 
implements org.apache.hadoop.io.Writable {
	
	private LongWritable           weight = new LongWritable();
	private VectorWritable targetPos = new VectorWritable();
	
	/** 
	 * Default constructor.
	 */
	public EdgeValueWritable() {
		super();
	}
	
	/** 
	 * Constructor.
	 */
	public EdgeValueWritable(LongWritable edgeValue, VectorWritable targetValue) {
		super();
		set(edgeValue, targetValue);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		weight.readFields(in);
		targetPos.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		weight.write(out);
		targetPos.write(out);
	}
	

	/** 
	 * Return the edge weight
	 * @return a LongWritable 
	 */
	public LongWritable getWeight() {
		return weight;
	}

	/** 
	 * Return the target position
	 * @return a CoordinatesWritable 
	 */
	public VectorWritable getTargetPos() {
		return targetPos;
	}

	/** 
	 * Set new edge value.
	 * @param edgeValue the edge weight. 
	 * @param targetPos  the target vertex position. 
	 */
	public void set(LongWritable edgeValue, VectorWritable targetPos) {
		this.weight  = edgeValue;
		this.targetPos  = targetPos;
	}

}
