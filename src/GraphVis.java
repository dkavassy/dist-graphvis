import java.io.IOException;

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;

public class GraphVis extends BasicComputation<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable, MessageWritable> {

	@Override
	public void compute(
			Vertex<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable> vertex,
			Iterable<MessageWritable> messages) throws IOException {
		 
		// Set T in init!
		
		 if (getSuperstep() % 4 == 0) {
			 
			 // Generate random position, only at the beginning!
			 if (getSuperstep() == 0)
			 {
				 CoordinatesWritable pos  = new CoordinatesWritable( (long) (Math.random()*1000), (long) (Math.random()*1000) );
				 CoordinatesWritable disp = new CoordinatesWritable();
				 
				 CoordinatesPairWritable coords = new CoordinatesPairWritable(pos, disp);
				 
			     vertex.setValue(coords);
			 }
			 
			 // We assume that vertices are numbered 1..n where n is the number of vertices
			 for (int i=1;i <= getTotalNumVertices(); i++)
			 {
				 //FIXME: exclude self
				 sendMessage(new IntWritable(i),
						 new MessageWritable(vertex.getId(),
								 vertex.getValue().getPos()));
			 }
			 
			 
		 }vertex.voteToHalt();
	}
}