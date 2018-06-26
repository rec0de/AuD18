package lab;

import java.util.ArrayList;

/**
 * Class representing a vertex in a directed graph
 */
public class Vertex {
	
	private final String name;
	private ArrayList<Edge> incoming;
	private ArrayList<Edge> outgoing;
	private ArrayList<Edge> allEdges;

	/**
	 * Constructor
	 * @param name Name of the vertex. Assumed to be per-graph unique
	 */
	public Vertex(String name) {
		this.name = name;
		this.incoming = new ArrayList<Edge>();
		this.outgoing = new ArrayList<Edge>();
		this.allEdges = new ArrayList<Edge>();
	}
	
	/**
	 * Getter method for vertex name
	 * @return Vertex name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Add an incoming edge to this vertex
	 * @param edge Incoming edge
	 */
	public void addIncoming(Edge edge) {
		incoming.add(edge);
		allEdges.add(edge);
	}
	
	/**
	 * Add an outgoing edge to this vertex
	 * @param edge Outgoing edge
	 */
	public void addOutgoing(Edge edge) {
		outgoing.add(edge);
		allEdges.add(edge);
	}
	
	/**
	 * Get a List of all outgoing or incoming edges of this vertex (for analyzing the residual network)
	 * @return List of all edges connecting to or from this vertex
	 */
	public ArrayList<Edge> getResidual() {
		return allEdges;
	}
	
	/**
	 * Calculates the net flow in this vertex. Should be zero for all non-source non-drain vertices
	 * @return Net flow of the vertex
	 */
	public int getTotalFlow() {
		int res = 0;
		for(Edge edge : outgoing)
			res += edge.flow();
		for(Edge edge : incoming)
			res -= edge.flow();
		return res;
	}
	
	/**
	 * Get a dot code representation of this vertex
	 * @return Dot code representing the vertex
	 */
	public String toDotCode() {
		return this.name + "[label=\"" + this.name + "\"];";
	}
}
