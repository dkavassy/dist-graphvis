import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class CoordinatesWritable implements org.apache.hadoop.io.Writable {
	
	private long x = 0;
	private long y = 0;
	
	public CoordinatesWritable() {
		
	}
	
	public CoordinatesWritable(long x, long y) {
		set(x, y);
	}

	public void set(long x2, long y2) {
		this.x = x2;
		this.y = y2;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		x = in.readLong();
		y = in.readLong();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(x);
		out.writeLong(y);
	}
	
	@Override
	public String toString() {
		return "x: " + x + "; y: " + y;
	}

	public long getX() {
		return x;
	}

	public long getY() {
		return y;
	}

}
