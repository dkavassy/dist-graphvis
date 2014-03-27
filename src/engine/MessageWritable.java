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

import org.apache.hadoop.io.IntWritable;

/** 
 * A class that can be used as the message type. 
 * This class contains message source vertex id and a vector of coordinates. 
 * <p> 
 * @author Daniel Kavassy 
 * @version 1.1 Initial development. 
 */ 
public class MessageWritable implements org.apache.hadoop.io.Writable {
	
	private IntWritable srcId = new IntWritable();
	private CoordinatesWritable pos = new CoordinatesWritable();
	
	/** 
	 * Default constructor.
	 */
	public MessageWritable() {
		super();
	}
	
	/** 
	 * Constructor.
	 */
	public MessageWritable(IntWritable srcId, CoordinatesWritable pos) {
		super();
		set(srcId, pos);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		srcId.readFields(in);
		pos.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		srcId.write(out);
		pos.write(out);
	}
	
	@Override
	public String toString() {
		return "srcId: " + srcId + "; pos: " + pos;
	}
	
	/** 
	 * Return the source id
	 * @return a IntWritable 
	 */
	public IntWritable getSrcId() {
		return srcId;
	}
	
	/** 
	 * Return the vector of coordinates
	 * @return a CoordinatesWritable 
	 */
	public CoordinatesWritable getPos() {
		return pos;
	}

	/** 
	 * Set new message.
	 * @param srcId the source id. 
	 * @param pos the vector. 
	 */
	public void set(IntWritable srcId, CoordinatesWritable pos) {
		this.srcId = srcId;
		this.pos  = pos;
	}

	/** 
	 * Compare two messages, if they come from the same source and their x and y are all the same,
	 * then they are the same message.
	 * @param another MessageWritable. 
	 * @return boolean. 
	 * @see MessageWritable 
	 */
	@Override
	public boolean equals(Object obj) {
		MessageWritable other = (MessageWritable)  obj;
		if(other.getSrcId().equals(srcId)){
			if(other.getPos().equals(pos)){
				return true;
			}
		}
		
		
		
		return false;
		
	}

}
