/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package test;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.utils.InternalVertexRunner;
import org.junit.Test;

import engine.*;


/**
* Contains a simple unit test for {@link FruchtermanReingoldGraphVis}
*/
public class FruchtermanReingoldGraphVisTest {

	
	
	
	
	
	
	
	
	/**
	* A local integration test on toy data
	*/
	  @Test
	  public void testToyData() throws Exception {

	    // A small graph
	    String[] graph = new String[] {
	      "1,2",
	      "1,3",
	      "1,4",
	      "2,3",
	      "2,4",
	      "3,4"
	    };

	    GiraphConfiguration conf = new GiraphConfiguration();
	    conf.setComputationClass(FruchtermanReingoldGraphVis.class);
	    conf.setEdgeInputFormatClass(CSVEdgeInputFormat.class);
	    conf.setVertexOutputFormatClass(SVGVertexTextOutputFormat.class);
	    
	    
	    // Run internally
	    Iterable<String> results = InternalVertexRunner.run(conf, null ,graph);
	    System.out.println(results);
	   
	  }



 
}
