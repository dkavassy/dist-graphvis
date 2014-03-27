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
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.giraph.utils.IntPair;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/** 
 * A class that converts a CSV file into Giraph edges. 
 * <p> 
 * @author Shan Huang 
 * @version 1.1 Initial development. 
 */ 
public class CSVEdgeInputFormat extends TextEdgeInputFormat<IntWritable, EdgeValueWritable>{

	
	/**
	* Create an edge reader for a given split. The framework will call
	* {@link EdgeReader#initialize(InputSplit, TaskAttemptContext)} before
	* the split is used.
	*
	* @param split the split to be read
	* @param context the information about the task
	* @return a new record reader
	* @throws IOException
	*/
	@Override
	public EdgeReader<IntWritable, EdgeValueWritable> createEdgeReader(InputSplit arg0, TaskAttemptContext arg1) 
			throws IOException {
		return new CSVEdgeReader();
	}

	
	/**
	* Edge reader associated with {@link CSVEdgeInputFormat}.
	*/
	  public class CSVEdgeReader extends TextEdgeReaderFromEachLineProcessed<IntPair> {
		  
		  private final Pattern separator = Pattern.compile("[,]");
		
	   /** 
	   * Read a record of hadoop, and then separate source id and target id from it.
	   * @param line the record of hadoop. 
	   * @return a new IntPair. 
	   * @exception IOException.
	   * @see IntPair 
	   * @see CSVEdgeInputFormat 
	   */
	    @Override
	    protected IntPair preprocessLine(Text line) 
	    		throws IOException {
	      String[] tokens = separator.split(line.toString());
	      return new IntPair(Integer.valueOf(tokens[0]),
	          Integer.valueOf(tokens[1]));
	    }

	    /** 
		   * Get source id of an edge from IntPair.
		   * @param endpoints contains source and target vertex id of an edge. 
		   * @return a new IntWritable. 
		   * @exception IOException.
		   * @see IntPair 
		   * @see IntWritable 
		   */
	    @Override
	    protected IntWritable getSourceVertexId(IntPair endpoints)
	      throws IOException {
	      return new IntWritable(endpoints.getFirst());
	    }

	    /** 
		   * Get target id of an edge from IntPair.
		   * @param endpoints contains source and target vertex id of an edge. 
		   * @return a new IntWritable. 
		   * @exception IOException.
		   * @see IntPair 
		   * @see IntWritable 
		   */
	    @Override
	    protected IntWritable getTargetVertexId(IntPair endpoints)
	    		throws IOException {
	      return new IntWritable(endpoints.getSecond());
	    }

	    /** 
		   * Get edge value of an edge. Will get default number in this case.
		   * @param endpoints contains source and target vertex id of an edge. 
		   * @return a new EdgeValueWritable. 
		   * @exception IOException.
		   * @see IntPair 
		   * @see EdgeValueWritable 
		   */
	    @Override
	    protected EdgeValueWritable getValue(IntPair endpoints) 
	    		throws IOException {
	      return new EdgeValueWritable(new LongWritable(0L), new CoordinatesWritable());
	    }
	  }
}
