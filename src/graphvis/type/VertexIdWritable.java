package graphvis.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

public class VertexIdWritable implements Writable {

	private LongWritable index = new LongWritable();
	private LongWritable id = new LongWritable();
	
	@Override
	public void readFields(DataInput in) throws IOException {
		index.readFields(in);
		id.readFields(in);

	}

	@Override
	public void write(DataOutput out) throws IOException {
		index.write(out);
		id.write(out);

	}

	@Override
	public String toString() {
		return "VertexIdWritable [index=" + index + ", id=" + id + "]";
	}

	public LongWritable getIndex() {
		return index;
	}

	public void setIndex(LongWritable index) {
		this.index = index;
	}

	public LongWritable getId() {
		return id;
	}

	public void setId(LongWritable id) {
		this.id = id;
	}
	
	

}
