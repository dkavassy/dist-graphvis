package engine;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class CoordinatesWritable implements org.apache.hadoop.io.Writable {
	
	private double x = 0;
	private double y = 0;
	
	public CoordinatesWritable() {
		
	}
	
	public CoordinatesWritable(double x, double y) {
		set(x, y);
	}

	public void set(double x2, double y2) {
		this.x = x2;
		this.y = y2;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
	}
	
	@Override
	public String toString() {
		return "x: " + x + "; y: " + y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y);
	}

	public CoordinatesWritable add(CoordinatesWritable other) {
		return new CoordinatesWritable(getX() + other.getX(), getY() + other.getY());
	}
	
	public CoordinatesWritable subtract(CoordinatesWritable other) {
		return new CoordinatesWritable(getX() - other.getX(), getY() - other.getY());
	}
	//fake multiply
	public CoordinatesWritable multiply(CoordinatesWritable other) {
		return new CoordinatesWritable(getX() * other.getX(), getY() * other.getY());
	}
	 
	public CoordinatesWritable min(double t) {
		return new CoordinatesWritable(Math.min(getX(), t),Math.min(getY(), t));
	}

	@Override
	public boolean equals(Object obj) {
		CoordinatesWritable other = (CoordinatesWritable) obj;
		if(other.getX()==x && other.getY()== y){
			return true;
		}
		
		return false;
	}
	
	
}
