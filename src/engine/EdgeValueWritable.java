package engine;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;


public class EdgeValueWritable implements org.apache.hadoop.io.Writable {
	
	private LongWritable edgeValue = new LongWritable();
	private CoordinatesWritable targetPos = new CoordinatesWritable();
	
	public EdgeValueWritable() {
		
	}
	
	public EdgeValueWritable(LongWritable edgeValue, CoordinatesWritable targetValue) {
		set(edgeValue, targetValue);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		edgeValue.readFields(in);
		targetPos.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		edgeValue.write(out);
		targetPos.write(out);
	}
	
	@Override
	public String toString() {
		return "edge val: " + edgeValue + "; target pos: " + targetPos;
	}
	
	public LongWritable getEdgeValue() {
		return edgeValue;
	}

	public CoordinatesWritable getTargetPos() {
		return targetPos;
	}

	public void set(LongWritable edgeValue, CoordinatesWritable targetPos) {
		this.edgeValue  = edgeValue;
		this.targetPos  = targetPos;
	}

}
