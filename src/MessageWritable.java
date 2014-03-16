import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;


public class MessageWritable implements org.apache.hadoop.io.Writable {
	
	private IntWritable srcId = new IntWritable();
	private CoordinatesWritable pos = new CoordinatesWritable();
	
	public MessageWritable() {
		
	}
	
	public MessageWritable(IntWritable srcId, CoordinatesWritable pos) {
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
	
	public IntWritable getSrcId() {
		return srcId;
	}
	
	public CoordinatesWritable getPos() {
		return pos;
	}

	public void set(IntWritable srcId, CoordinatesWritable pos) {
		this.srcId = srcId;
		this.pos  = pos;
	}

}
