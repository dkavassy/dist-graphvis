/*
 * @(\#) GraphMLEdgeInputFormat.java 1.1 10 March 20

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
package graphvis.io;

import graphvis.type.VectorWritable;
import graphvis.type.EdgeValueWritable;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.giraph.utils.IntPair;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/** 
 * A class that converts a GraphML file into Giraph edges. 
 * <p> 
 * @author Shan Huang 
 * @version 1.1 Initial development. 
 */ 
public class GraphMLEdgeInputFormat extends TextEdgeInputFormat<LongWritable, EdgeValueWritable> 
{

	
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
	public EdgeReader<LongWritable, EdgeValueWritable> createEdgeReader
	(
			InputSplit arg0, TaskAttemptContext arg1) throws IOException {
		
		return new GraphMLEdgeReader();
	}

	
	/**
	* Edge reader associated with {@link GraphMLEdgeInputFormat}.
	*/
	public class GraphMLEdgeReader extends
	TextEdgeReaderFromEachLineProcessed<IntPair> 
	{
		private final Pattern separator = Pattern.compile("[\\s]");
		
		
		/** 
		   * Get source id of an edge from IntPair.
		   * @param endpoints contains source and target vertex id of an edge. 
		   * @return a new IntWritable. 
		   * @exception IOException.
		   * @see IntPair 
		   * @see IntWritable 
		   */
		@Override
		protected LongWritable getSourceVertexId(IntPair endpoints)
				throws IOException 
		{
			return new LongWritable(endpoints.getFirst());
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
		protected LongWritable getTargetVertexId(IntPair endpoints)
				throws IOException 
		{
			
			return new LongWritable(endpoints.getSecond());
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
				throws IOException 
		{
			
			return new EdgeValueWritable(new LongWritable(0L), new VectorWritable());
		}

		/** 
		   * Read a record of hadoop, and then separate source id and target id from it.
		   * @param line the record of hadoop. 
		   * @return a new IntPair. 
		   * @exception IOException.
		   * @see IntPair 
		   * @see GraphMLEdgeInputFormat 
		   */
		@Override
		protected IntPair preprocessLine(Text text) throws IOException
		{
			
			String line     = text.toString().split("[<>]")[1];
			String[] tokens = separator.split(line);
			return new IntPair(Integer.parseInt(tokens[2].split("[\"]")[1]),
					Integer.parseInt(tokens[3].split("[\"]")[1]));
		}	
		
	}

}
