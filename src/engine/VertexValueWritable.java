package engine;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class VertexValueWritable implements org.apache.hadoop.io.Writable {
	
	private CoordinatesWritable pos  = new CoordinatesWritable();
	private CoordinatesWritable disp = new CoordinatesWritable();
	
	public VertexValueWritable() {
		super();
	}
	
	public VertexValueWritable(CoordinatesWritable pos, CoordinatesWritable disp) {
		super();
		set(pos, disp);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		pos.readFields(in);
		disp.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		pos.write(out);
		disp.write(out);
	}
	
	@Override
	public String toString() {
		return "pos: " + pos;
	}

	public CoordinatesWritable getPos() {
		return pos;
	}

	public CoordinatesWritable getDisp() {
		return disp;
	}

	public void set(CoordinatesWritable pos, CoordinatesWritable disp) {
		this.pos  = pos;
		this.disp = disp;
	}

}
