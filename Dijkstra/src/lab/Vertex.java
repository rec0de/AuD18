package lab;

import java.util.ArrayList;

/**
 * Class representing a vertex ('street crossing' in this context) in a directed graph
 */
public class Vertex {
	
	private final double avgWaitTime;
	private final String name;
	private ArrayList<Edge> incoming;
	private ArrayList<Edge> outgoing;

	/**
	 * Constructor
	 * @param name Name of the vertex. Assumed to be per-graph unique
	 * @param waitTime Average time spent waiting at this vertex
	 */
	public Vertex(String name, double waitTime) {
		this.name = name;
		this.avgWaitTime = waitTime;
		this.incoming = new ArrayList<Edge>();
		this.outgoing = new ArrayList<Edge>();
	}
	
	/**
	 * Getter method for vertex name
	 * @return Vertex name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Getter method for average vertex wait time
	 * @return
	 */
	public double waitTime() {
		return this.avgWaitTime;
	}
	
	/**
	 * Add an incoming edge to this vertex
	 * @param edge Incoming edge
	 */
	public void addIncoming(Edge edge) {
		incoming.add(edge);
	}
	
	/**
	 * Add an outgoing edge to this vertex
	 * @param edge Outgoing edge
	 */
	public void addOutgoing(Edge edge) {
		outgoing.add(edge);
	}
	
	/**
	 * Get a List of all outgoing edges of this vertex
	 * @return
	 */
	public ArrayList<Edge> getOutgoing() {
		return outgoing;
	}
	
	/**
	 * Get the edge from this vertex to the destination vertex (if it exists, otherwise return null)
	 * @param vertex
	 * @return
	 */
	public Edge getEdgeTo(String vertex) {
		for(Edge edge: outgoing) {
			if(edge.to.getName().equals(vertex))
				return edge;
		}
		return null;
	}
	
	/**
	 * Get a dot code representation of this vertex
	 * @return Dot code representing the vertex
	 */
	public String toDotCode() {
		return this.name + "[label=\"" + this.name + ',' + this.avgWaitTime + "\"];";
	}
}
