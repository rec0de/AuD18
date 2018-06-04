/**
 * 
 */
package lab;

import java.util.ArrayList;

/**
 * @author Nils Rollshausen
 *
 */
public class Vertex {
	
	private final double avgWaitTime;
	private final String name;
	private ArrayList<Edge> incoming;
	private ArrayList<Edge> outgoing;

	public Vertex(String name, double waitTime) {
		this.name = name;
		this.avgWaitTime = waitTime;
		this.incoming = new ArrayList<Edge>();
		this.outgoing = new ArrayList<Edge>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public double waitTime() {
		return this.avgWaitTime;
	}
	
	public void addIncoming(Edge edge) {
		incoming.add(edge);
	}
	
	public void addOutgoing(Edge edge) {
		outgoing.add(edge);
	}
	
	public ArrayList<Edge> getOutgoing() {
		return outgoing;
	}
	
	public Edge getEdgeTo(String vertex) {
		for(Edge edge: outgoing) {
			if(edge.to.getName().equals(vertex))
				return edge;
		}
		return null;
	}
	
	public String toDotCode() {
		return this.name + "[label=\"" + this.name + ',' + this.avgWaitTime + "\"];";
	}
}
