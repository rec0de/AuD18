package lab;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
/**
 * MaxFlow.java
 */


public class MaxFlow {
	/**
	 * Return codes:
	 * 		-1 no source on the map
	 * 		-2 no destination on the map
	 * 		-3 if both source and destination points are not on the map
	 * 		-4 if no path can be found between source and destination
	 * 	MAXINT if sources identical to destinations
	 */
	public static final int NO_SOURCE_FOUND = -1;
	public static final int NO_DESTINATION_FOUND = -2;
	public static final int NO_SOURCE_DESTINATION_FOUND = -3;
	public static final int NO_PATH = -4;
	public static final int SOURCES_SAME_AS_DESTINATIONS = Integer.MAX_VALUE;
	
	private HashMap<String, Vertex> vertices;
	private ArrayList<Edge> edges;
	
	/**
	 * The constructor, setting the name of the file to parse.
	 * 
	 * @param filename the absolute or relative path and filename of the file
	 */
	public MaxFlow(final String filename) {
		this.vertices = new HashMap<String, Vertex>();
		this.edges = new ArrayList<Edge>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    
		    // Read all lines
		    while ((line = br.readLine()) != null) {
		    	if(line.matches(".* -> .*;")) {
		    		String[] info = line.replaceAll("\\s+", " ").replaceFirst(" -> ", ",").replaceFirst("\\[label=\"", ",").replaceFirst("\".*$", "").split(",");
		    		info[0] = info[0].replaceFirst(" ", "");
		    		info[1] = info[1].replaceFirst(" ", "");
		    		Vertex origin;
		    		Vertex destination;
		    		if(vertices.containsKey(info[0]))
		    			origin = vertices.get(info[0]);
		    		else {
		    			origin = new Vertex(info[0]);
		    			this.vertices.put(info[0], origin);
		    		}
		    		
		    		if(vertices.containsKey(info[1]))
		    			destination = vertices.get(info[1]);
		    		else {
		    			destination = new Vertex(info[1]);
		    			this.vertices.put(info[1], destination);
		    		}
	
			    	Edge edge = new Edge(origin, destination, Integer.parseInt(info[2]));
			    	// Add edge to source and destination vertex
			    	origin.addOutgoing(edge);
			    	destination.addIncoming(edge);
			    	this.edges.add(edge);
			    	//System.out.println("Parse edge "+info[0]+"->"+info[1]+" cap: "+info[2]);
		    	}
		    }
		    
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("IO Exception");
		}	
	}
	
	/**
	 * Calculates the maximum number of cars able to travel the graph specified
	 * in filename.
	 *
	 * @param sources a list of all source nodes
	 * @param destinations a list of all destination nodes
	 * @return 	the maximum number of cars able to travel the graph,
	 * 			NO_SOURCE_FOUND if no source is on the map
	 * 			NO_DESTINATION_FOUND if no destination is on the map
	 * 			NO_SOURCE_DESTINATION_FOUND if both - no source and no destination - are not on the map
	 * 			NO_PATH if no path can be found
	 * 			SOURCES_SAME_AS_DESTINATIONS if sources == destinations 
	 */
	public final int findMaxFlow(final String[] sources, final String[] destinations) {
		ArrayList<Vertex> sourceVertices = new ArrayList<Vertex>();
		ArrayList<Vertex> destinationVertices = new ArrayList<Vertex>();
		
		for(Edge edge : edges)
			edge.reset();
		
		for(String sourceKey : sources)
			if(vertices.containsKey(sourceKey))
				sourceVertices.add(vertices.get(sourceKey));
		for(String destinationKey : destinations)
			if(vertices.containsKey(destinationKey))
				destinationVertices.add(vertices.get(destinationKey));
		
		if(sourceVertices.isEmpty() && destinationVertices.isEmpty())
			return MaxFlow.NO_SOURCE_DESTINATION_FOUND;
		else if(sourceVertices.isEmpty())
			return MaxFlow.NO_SOURCE_FOUND;
		else if(destinationVertices.isEmpty())
			return MaxFlow.NO_DESTINATION_FOUND;
		
		for(Vertex source : sourceVertices) {
			try {
				while(findAugmentingPath(source, destinationVertices)) {}
			}
			catch(InfiniteFlowException e) {
				return MaxFlow.SOURCES_SAME_AS_DESTINATIONS;
			}
		}
		
		int res = 0;
		for(Vertex source : sourceVertices)
			res += source.getTotalFlow();
		
		return res == 0 ? MaxFlow.NO_PATH : res;
	}
	
	/**
	 * Calculates the graph showing the maxFlow.
	 *
	 * @param sources a list of all source nodes
	 * @param destinations a list of all destination nodes
	 * @return a ArrayList of Strings as specified in the task in dot code
	 */
	public final ArrayList<String> findResidualNetwork(final String[] sources,	final String[] destinations) {
		ArrayList<Vertex> sourceVertices = new ArrayList<Vertex>();
		ArrayList<Vertex> destinationVertices = new ArrayList<Vertex>();
		
		for(Edge edge : edges)
			edge.reset();
		
		for(String sourceKey : sources)
			if(vertices.containsKey(sourceKey))
				sourceVertices.add(vertices.get(sourceKey));
		for(String destinationKey : destinations)
			if(vertices.containsKey(destinationKey))
				destinationVertices.add(vertices.get(destinationKey));
		
		if(!sourceVertices.isEmpty() && !destinationVertices.isEmpty()) {
			for(Vertex source : sourceVertices) {
				try {
					while(findAugmentingPath(source, destinationVertices)) {}
				}
				catch(InfiniteFlowException e) {
					break; // Doesn't reset capacities already used before exception came up
				}
			}
		}
		
		for(Edge edge : edges)
			edge.emboldenIfFreeCapacity();
		
		ArrayList<String> res = new ArrayList<String>();
		
		res.add("digraph{");
		
		for(String sourceName : sources)
			res.add(sourceName+" [shape=doublecircle][style=bold];");
		for(String destName : destinations)
			res.add(destName+" [shape=circle][style=bold];");
		for(Edge edge : this.edges)
			res.add(edge.toDotCode());
		
		res.add("}");
		
		return res;
	}
	
	private boolean findAugmentingPath(Vertex source, ArrayList<Vertex> destinations) throws InfiniteFlowException{
		HashMap<String,Edge> pred = new HashMap<String,Edge>();
		ArrayList<Vertex> queue = new ArrayList<Vertex>();
		ArrayList<Edge> res = new ArrayList<Edge>();
		
		System.out.println("Looking for augmenting path from "+source.getName());
		
		queue.add(source);
		pred.put(source.getName(), null);
		
		Vertex current;
		while(!queue.isEmpty()) {
			current = queue.remove(0);
		
			//System.out.println("Visiting "+current.getName());
			
			if(destinations.contains(current)) {
				if(current == source)
					throw new InfiniteFlowException("Source " + source.getName() + " is also destination");
				
				System.out.println("Found augmenting path");
				
				// Build path and return
				String nextName = current.getName();
				String currentName;
				Edge backedge;
				int maxCapacity = Integer.MAX_VALUE;
				while(pred.get(nextName) != null) {
					backedge = pred.get(nextName);
					currentName = nextName;
					//System.out.println("Walking backwards: "+backedge.from.getName()+" -> "+backedge.to.getName());
					res.add(0, backedge);
					nextName = (backedge.to.getName().equals(currentName)) ? backedge.from.getName() : backedge.to.getName();
					maxCapacity = (backedge.to.getName().equals(currentName)) ? Math.min(maxCapacity, backedge.freeCapacity()) : Math.min(maxCapacity, backedge.backCapacity());
				}
				Vertex nextVertex = source;
				System.out.println(maxCapacity);
				for(Edge edge : res) {
					if(edge.from == nextVertex) {
						edge.increaseFlow(maxCapacity);
						nextVertex = edge.to;
					}
					else if(edge.to == nextVertex) {
						edge.increaseFlow(- maxCapacity);
						nextVertex = edge.from;
					}
					else
						throw new RuntimeException("Edge is neither from nor to source node");
				}
				
				return true;
			}
			
			for(Edge edge : current.getResidual()) {
				if(edge.from == current && pred.containsKey(edge.to.getName()) || edge.to == current && pred.containsKey(edge.from.getName()))
					continue;
				
				if(edge.from == current && edge.freeCapacity() > 0) {
					queue.add(edge.to);
					pred.put(edge.to.getName(), edge);
					System.out.println("Adding "+current.getName()+" -> "+edge.to.getName() + " cap: "+edge.freeCapacity());
				}
				else if(edge.to == current && edge.backCapacity() > 0) {
					queue.add(edge.from);
					pred.put(edge.from.getName(), edge);
					System.out.println("Adding "+current.getName()+" -> "+edge.from.getName() + " cap: "+edge.backCapacity());
				}
			}
		}
		
		System.out.println("No augmenting path from "+source.getName());
		
		return false;
	}

}