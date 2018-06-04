/**
 * 
 */
package lab;

/**
 * @author Nils Rollshausen
 *
 */
public class Edge {
	
	protected final Vertex from;
	protected final Vertex to;
	private final double distance;
	private final double speedLimit;
	private boolean bold = false;

	public Edge(Vertex from, Vertex to, double distance, double speedLimit) {
		this.from = from;
		this.to = to;
		this.distance = distance;
		this.speedLimit = speedLimit;
	}
	
	public double time() {
		return (this.distance / this.speedLimit)*60;
	}
	
	public double distance() {
		return this.distance;
	}
	
	public void resetBold() {
		this.bold = false;
	}
	
	public void embolden() {
		this.bold = true;
	}
	
	public String toDotCode() {
		return from.getName() + " -> " + to.getName() + " [label=\""+ this.distance + "," + this.speedLimit + "\"]" + (this.bold ? "[style=bold];" : ";");
	}
}
