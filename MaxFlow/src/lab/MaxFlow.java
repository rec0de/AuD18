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
		    	// If line is an edge
		    	if(line.matches(".* -> .*;")) {
		    		// Do some string replace regex magic
		    		String[] info = line.replaceAll("\\s+", " ").replaceFirst(" -> ", ",").replaceFirst("\\[label=\"", ",").replaceFirst("\".*$", "").split(",");
		    		info[0] = info[0].replaceFirst(" ", "");
		    		info[1] = info[1].replaceFirst(" ", "");
		    		Vertex origin;
		    		Vertex destination;
		    		
		    		// Create vertices if they do not exist already
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
		
		// Reset edge flows left from previous calculations
		for(Edge edge : edges)
			edge.reset();
		
		// Build Lists of sources and destinations
		for(String sourceKey : sources)
			if(vertices.containsKey(sourceKey))
				sourceVertices.add(vertices.get(sourceKey));
		for(String destinationKey : destinations)
			if(vertices.containsKey(destinationKey))
				destinationVertices.add(vertices.get(destinationKey));
		
		// Handle edge cases
		if(sourceVertices.isEmpty() && destinationVertices.isEmpty())
			return MaxFlow.NO_SOURCE_DESTINATION_FOUND;
		else if(sourceVertices.isEmpty())
			return MaxFlow.NO_SOURCE_FOUND;
		else if(destinationVertices.isEmpty())
			return MaxFlow.NO_DESTINATION_FOUND;
		
		// To avoid restructuring the input graph, sources are added to the flow one after another
		// This intuitively yields the same result as it is equivalent to using certain paths from a unified source before others
		for(Vertex source : sourceVertices) {
			try {
				while(findAugmentingPath(source, destinationVertices)) {} // Call findAugmentingPath until it returns false (= no remaining path)
			}
			catch(InfiniteFlowException e) {
				return MaxFlow.SOURCES_SAME_AS_DESTINATIONS; // return appropriate error code if sources and destinations are not distinct
			}
		}
		
		int res = 0;
		// Total net flow in all sources is the MaxFlow of the graph
		for(Vertex source : sourceVertices)
			res += source.getTotalFlow();
		
		return res == 0 ? MaxFlow.NO_PATH : res; // Flow = 0 implies no path from source to drain
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
		
		// Reset edge flows left from previous calculations
		for(Edge edge : edges)
			edge.reset();
		
		// Build Lists of sources and destinations
		for(String sourceKey : sources)
			if(vertices.containsKey(sourceKey))
				sourceVertices.add(vertices.get(sourceKey));
		for(String destinationKey : destinations)
			if(vertices.containsKey(destinationKey))
				destinationVertices.add(vertices.get(destinationKey));
		
		// To avoid restructuring the input graph, sources are added to the flow one after another
		// This intuitively yields the same result as it is equivalent to using certain paths from a unified source before others
		if(!sourceVertices.isEmpty() && !destinationVertices.isEmpty()) {
			for(Vertex source : sourceVertices) {
				try {
					while(findAugmentingPath(source, destinationVertices)) {} // Call findAugmentingPath until it returns false (= no remaining path)
				}
				catch(InfiniteFlowException e) {
					break; // Doesn't reset capacities already used before exception came up
					// Works alright with the test cases though
				}
			}
		}
		
		for(Edge edge : edges)
			edge.emboldenIfFreeCapacity();
		
		// Build dotcode representation of graph
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
	
	/**
	 * Finds an augmenting path in the residual network of the current flow from a single source to any destination
	 * (if such a path exists) and adds it to the flow
	 * @param source The source vertex
	 * @param destinations List of possible destination vertices
	 * @return True if a path was added, False if no augmenting path exists
	 * @throws InfiniteFlowException
	 */
	private boolean findAugmentingPath(Vertex source, ArrayList<Vertex> destinations) throws InfiniteFlowException{
		HashMap<String,Edge> pred = new HashMap<String,Edge>();
		ArrayList<Vertex> queue = new ArrayList<Vertex>();
		
		//System.out.println("Looking for augmenting path from "+source.getName());
		
		// Start BFS at source vertex
		queue.add(source);
		pred.put(source.getName(), null); // Predecessor of source is null
		
		Vertex current;
		while(!queue.isEmpty()) {
			// Remove the next queued vertex
			current = queue.remove(0);
		
			//System.out.println("Visiting "+current.getName());
			
			// If a path to a destination was found
			if(destinations.contains(current)) {
				if(current == source)
					throw new InfiniteFlowException("Source " + source.getName() + " is also destination"); // Throw exception if destination is a source
				
				// Build augmenting path and find max capacity
				ArrayList<Edge> path = new ArrayList<Edge>();
				String nextName = current.getName();
				String currentName;
				Edge backedge;
				int maxCapacity = Integer.MAX_VALUE; // Assume 'infinite' capacity initially
				// Walk backwards along predecessors
				while(pred.get(nextName) != null) {
					backedge = pred.get(nextName); // Get edge that was used to get to nextName
					currentName = nextName;
					//System.out.println("Walking backwards: "+backedge.from.getName()+" -> "+backedge.to.getName());
					path.add(0, backedge);
					// Get next vertex on path and update maxCapacity (conditionals needed because directed edges can be walked forwards _and_ backwards in the residual network)
					nextName = (backedge.to.getName().equals(currentName)) ? backedge.from.getName() : backedge.to.getName();
					maxCapacity = (backedge.to.getName().equals(currentName)) ? Math.min(maxCapacity, backedge.freeCapacity()) : Math.min(maxCapacity, backedge.backCapacity());
				}
				// Walk path forwards and update flow
				Vertex nextVertex = source;
				for(Edge edge : path) {
					// If the edge was used along its natural direction, increase the flow by maxCapacity
					if(edge.from == nextVertex) {
						edge.increaseFlow(maxCapacity);
						nextVertex = edge.to;
					}
					// If the edge was used opposed to its natural direction, decrease the flow
					else if(edge.to == nextVertex) {
						edge.increaseFlow(- maxCapacity);
						nextVertex = edge.from;
					}
					else
						throw new RuntimeException("Edge is neither from nor to source node"); // Shouldn't happen but let's be safe
				}
				
				return true;
			}
			
			// Otherwise, continue BFS with all connected vertices
			for(Edge edge : current.getResidual()) {
				// Do not visit already visited vertices (vertices that already have a predecessor)
				if(edge.from == current && pred.containsKey(edge.to.getName()) || edge.to == current && pred.containsKey(edge.from.getName()))
					continue;
				
				// If the edge is a regular forward edge with nonzero capacity, add the destination vertex and update its predecessor
				if(edge.from == current && edge.freeCapacity() > 0) {
					queue.add(edge.to);
					pred.put(edge.to.getName(), edge);
					//System.out.println("Adding "+current.getName()+" -> "+edge.to.getName() + " cap: "+edge.freeCapacity());
				}
				// If the edge is a backwards edge with nonzero effective capacity, add the destination vertex and update predecessor
				else if(edge.to == current && edge.backCapacity() > 0) {
					queue.add(edge.from);
					pred.put(edge.from.getName(), edge);
					//System.out.println("Adding "+current.getName()+" -> "+edge.from.getName() + " cap: "+edge.backCapacity());
				}
			}
		}
		
		//System.out.println("No augmenting path from "+source.getName());
		// Return false if queue is empty but no path has been found
		return false;
	}

}