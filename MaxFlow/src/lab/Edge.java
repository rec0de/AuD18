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
	private final int capacity;
	private int flow;
	private boolean bold = false;

	/**
	 * Constructor
	 * @param from Source vertex of the edge
	 * @param to Destination vertex of the edge
	 * @param distance Length (km) of the edge
	 * @param speedLimit Speed with which the 'car' can travel along this edge
	 */
	public Edge(Vertex from, Vertex to, int capacity) {
		this.from = from;
		this.to = to;
		this.capacity = capacity;
		this.flow = 0;
	}
	
	/**
	 * Get driving time for this edge
	 * @return Driving time (m) for this edge
	 */
	public int flow() {
		return this.flow;
	}
	
	/**
	 * Get length of this edge
	 * @return Length of the edge
	 */
	public int capacity() {
		return this.capacity;
	}
	
	public int freeCapacity() {
		return this.capacity - this.flow;
	}
	
	public int backCapacity() {
		return this.flow;
	}
	
	public void increaseFlow(int inc) {
		this.flow += inc;
		if(this.flow < 0 || this.flow > this.capacity)
			throw new RuntimeException("Edge capacity exceeded: "+this.flow);
	}
	
	public void reset() {
		this.flow = 0;
	}
	
	/**
	 * Embolden the edge if it's capacity was not fully utilized
	 */
	public void emboldenIfFreeCapacity() {
		this.bold = this.freeCapacity() > 0;
	}
	
	/**
	 * Get a dot code representation of this edge
	 * @return Dot code line representing the edge
	 */
	public String toDotCode() {
		return from.getName() + " -> " + to.getName() + " [label=\""+ this.capacity + "-" + this.flow + "\"]" + (this.bold ? "[style=bold];" : ";");
	}
}
