package engine;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;


public class EdgeValueWritable implements org.apache.hadoop.io.Writable {
	
	private LongWritable           weight = new LongWritable();
	private CoordinatesWritable targetPos = new CoordinatesWritable();
	
	public EdgeValueWritable() {
		super();
	}
	
	public EdgeValueWritable(LongWritable edgeValue, CoordinatesWritable targetValue) {
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
	
	@Override
	public String toString() {
		return "edge weight: " + weight + "; target pos: " + targetPos;
	}
	
	public LongWritable getWeight() {
		return weight;
	}

	public CoordinatesWritable getTargetPos() {
		return targetPos;
	}

	public void set(LongWritable edgeValue, CoordinatesWritable targetPos) {
		this.weight  = edgeValue;
		this.targetPos  = targetPos;
	}

}
