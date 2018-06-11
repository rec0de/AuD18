/**
 * 
 */
package lab;

/**
 * Class representing an edge in a directed graph
 */
public class Edge {
	
	protected final Vertex from;
	protected final Vertex to;
	private final double distance;
	private final double speedLimit;
	private boolean bold = false;

	/**
	 * Constructor
	 * @param from Source vertex of the edge
	 * @param to Destination vertex of the edge
	 * @param distance Length (km) of the edge
	 * @param speedLimit Speed with which the 'car' can travel along this edge
	 */
	public Edge(Vertex from, Vertex to, double distance, double speedLimit) {
		this.from = from;
		this.to = to;
		this.distance = distance;
		this.speedLimit = speedLimit;
	}
	
	/**
	 * Get driving time for this edge
	 * @return Driving time (m) for this edge
	 */
	public double time() {
		return (this.distance / this.speedLimit)*60;
	}
	
	/**
	 * Get length of this edge
	 * @return Length of the edge
	 */
	public double distance() {
		return this.distance;
	}
	
	/**
	 * Reset the bold status
	 */
	public void resetBold() {
		this.bold = false;
	}
	
	/**
	 * Embolden the edge to indicate it is part of the shortest path
	 */
	public void embolden() {
		this.bold = true;
	}
	
	/**
	 * Get a dot code representation of this edge
	 * @return Dot code line representing the edge
	 */
	public String toDotCode() {
		return from.getName() + " -> " + to.getName() + " [label=\""+ this.distance + "," + this.speedLimit + "\"]" + (this.bold ? "[style=bold];" : ";");
	}
}
