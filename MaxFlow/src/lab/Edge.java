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
	 * @param capacity Maximum number of cars that can travel on this edge per hour
	 */
	public Edge(Vertex from, Vertex to, int capacity) {
		this.from = from;
		this.to = to;
		this.capacity = capacity;
		this.flow = 0;
	}
	
	/**
	 * Get current flow along this edge
	 * @return Current flow in the direction of the edge
	 */
	public int flow() {
		return this.flow;
	}
	
	/**
	 * Get maximum flow capacity of this edge
	 * @return Capacity of the edge
	 */
	public int capacity() {
		return this.capacity;
	}
	
	/**
	 * Get residual (unused) capacity of the edge
	 * @return Free capacity of the edge
	 */
	public int freeCapacity() {
		return this.capacity - this.flow;
	}
	
	/**
	 * Get capacity of the edge running in the opposite direction in the residual network (alias for flow)
	 * @return Capacity of the back-edge in the residual network
	 */
	public int backCapacity() {
		return this.flow;
	}
	
	/**
	 * Increase the flow along this edge (route additional cars over this street)
	 * @param inc
	 */
	public void increaseFlow(int inc) {
		this.flow += inc;
		if(this.flow < 0 || this.flow > this.capacity)
			throw new RuntimeException("Edge capacity exceeded: "+this.flow);
	}
	
	/**
	 * Reset the edge (e.g. for different flow calculations in the same graph)
	 */
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
