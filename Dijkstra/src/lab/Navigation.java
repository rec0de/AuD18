package lab;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * The class Navigation finds the shortest (and/or) path between points on a map
 * using the Dijkstra algorithm
 */
public class Navigation {
	/**
	 * Return codes: -1 if the source is not on the map -2 if the destination is
	 * not on the map -3 if both source and destination points are not on the
	 * map -4 if no path can be found between source and destination
	 */
	public static final int SOURCE_NOT_FOUND = -1;
	public static final int DESTINATION_NOT_FOUND = -2;
	public static final int SOURCE_DESTINATION_NOT_FOUND = -3;
	public static final int NO_PATH = -4;
	
	private HashMap<String, Vertex> vertices;
	private LinkedList<Vertex> vertexList;
	private ArrayList<Edge> edges;

	/**
	 * The constructor takes a filename as input, it reads that file and fill
	 * the nodes and edges Lists with corresponding node and edge objects
	 * 
	 * @param filename Name of the file containing the input map
	 */
	public Navigation(String filename) {
		
		this.vertices = new HashMap<String, Vertex>();
		this.vertexList = new LinkedList<Vertex>();
		this.edges = new ArrayList<Edge>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    ArrayList<String> edges = new ArrayList<String>();
		    
		    // Read all lines
		    while ((line = br.readLine()) != null) {
		    	// Process vertices first, save edges for later
		    	if(line.matches(".* -> .*;")) {
		    		edges.add(line);
		    	}
		    	else if(line.matches(".*\\[label.*\\];")) {
		    		String[] info = line.replaceFirst("^.*\\[label=\"", "").replaceFirst("\".*$", "").split(",");
		    		Vertex vertex = new Vertex(info[0], Double.parseDouble(info[1]));
		    		// Add vertices to a hash map for fast lookup AND a list for fast enumeration while exporting dot code
		    		// Memory overhead should be acceptable
		    		this.vertices.put(info[0], vertex);
		    		this.vertexList.add(vertex);
		    		//System.out.println("Parse vertex "+info[0]+" ("+info[1]+")");
		    	}	
		    }
		    
		    // Parse edges
		    for(String edgeString: edges) {
		    	String[] info = edgeString.replaceFirst(" -> ", ",").replaceFirst(" \\[label=\"", ",").replaceFirst("\".*$", "").split(",");
		    	Vertex origin = this.vertices.get(info[0]);
		    	Vertex destination = this.vertices.get(info[1]);
		    	Edge edge = new Edge(origin, destination, Double.parseDouble(info[2]), Double.parseDouble(info[3]));
		    	// Add edge to source and destination vertex
		    	origin.addOutgoing(edge);
		    	destination.addIncoming(edge);
		    	this.edges.add(edge);
		    	//System.out.println("Parse edge "+info[0]+"->"+info[1]);
		    }
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("IO Exception");
		}
	}

	/**
	 * This methods finds the shortest route (distance) between points A and B
	 * on the map given in the constructor.
	 * 
	 * If a route is found the return value is an object of type
	 * ArrayList<String>, where every element is a String representing one line
	 * in the map. The output map is identical to the input map, apart from that
	 * all edges on the shortest route are marked "bold". It is also possible to
	 * output a map where all shortest paths starting in A are marked bold.
	 * 
	 * The order of the edges as they appear in the output may differ from the
	 * input.
	 * 
	 * @param A Source
	 * @param B Destination
	 * @return returns a map as described above if A or B is not on the map or
	 *         if there is no path between them the original map is to be
	 *         returned.
	 */
	public ArrayList<String> findShortestRoute(String A, String B) {
		Vertex a = this.vertices.get(A);
		Vertex b = this.vertices.get(B);
		
		if(a != null && b != null)
			this.dijkstra(a, b, false, true); // false, true for distance (not time) based navigation and path highlighting
		
		// Path is already marked, return dot code
		return this.toDotCode();
	}

	/**
	 * This methods finds the fastest route (in time) between points A and B on
	 * the map given in the constructor.
	 * 
	 * If a route is found the return value is an object of type
	 * ArrayList<String>, where every element is a String representing one line
	 * in the map. The output map is identical to the input map, apart from that
	 * all edges on the shortest route are marked "bold". It is also possible to
	 * output a map where all shortest paths starting in A are marked bold.
	 * 
	 * The order of the edges as they appear in the output may differ from the
	 * input.
	 * 
	 * @param A Source
	 * @param B Destination
	 * @return returns a map as described above if A or B is not on the map or
	 *         if there is no path between them the original map is to be
	 *         returned.
	 */
	public ArrayList<String> findFastestRoute(String A, String B) {
		Vertex a = this.vertices.get(A);
		Vertex b = this.vertices.get(B);
		
		if(a != null && b != null) 
			this.dijkstra(a, b, true, true); // true, true for time based navigation and path highlighting
		
		// Path is already marked, return dot code
		return this.toDotCode();
	}

	/**
	 * Finds the shortest distance in kilometers between A and B using the
	 * Dijkstra algorithm.
	 * 
	 * @param A The start point A
	 * @param B The destination point B
	 * @return the shortest distance in kilometers rounded upwards.
	 *         SOURCE_NOT_FOUND if point A is not on the map
	 *         DESTINATION_NOT_FOUND if point B is not on the map
	 *         SOURCE_DESTINATION_NOT_FOUND if point A and point B are not on
	 *         the map NO_PATH if no path can be found between point A and point
	 *         B
	 */
	public int findShortestDistance(String pointA, String pointB) {
		Vertex source = this.vertices.get(pointA);
		Vertex destination = this.vertices.get(pointB);
		
		// Error / edge case handling
		if(source == null && destination == null)
			return Navigation.SOURCE_DESTINATION_NOT_FOUND;
		else if(source == null)
			return Navigation.SOURCE_NOT_FOUND;
		else if(destination == null)
			return Navigation.DESTINATION_NOT_FOUND;
		
		double dist = this.dijkstra(source, destination, false, false); // false, false for distance based navigation and no path highlighting
		
		// If no path exists, dijkstra will return infinity
		if(dist == Double.POSITIVE_INFINITY)
			return Navigation.NO_PATH;
		else
			return (int) Math.ceil(dist); // Otherwise, round up
	}

	/**
	 * Find the fastest route between A and B using the Dijkstra algorithm.
	 * 
	 * @param A Source
	 * @param B Destination
	 * @return the fastest time in minutes rounded upwards. SOURCE_NOT_FOUND if
	 *         point A is not on the map DESTINATION_NOT_FOUND if point B is not
	 *         on the map SOURCE_DESTINATION_NOT_FOUND if point A and point B
	 *         are not on the map NO_PATH if no path can be found between point
	 *         A and point B
	 */
	public int findFastestTime(String pointA, String pointB) {
		
		Vertex source = this.vertices.get(pointA);
		Vertex destination = this.vertices.get(pointB);
		
		// Error / edge case handling
		if(source == null && destination == null)
			return Navigation.SOURCE_DESTINATION_NOT_FOUND;
		else if(source == null)
			return Navigation.SOURCE_NOT_FOUND;
		else if(destination == null)
			return Navigation.DESTINATION_NOT_FOUND;
		
		double time = this.dijkstra(source, destination, true, false); // true, false for time based navigation and no path highlighting
		
		// If no path exists, dijkstra will return infinity
		if(time == Double.POSITIVE_INFINITY)
			return Navigation.NO_PATH;
		else
			return (int) Math.ceil(time); // Otherwise, round up
	}
	
	/**
	 * 
	 * @param origin Source vertex
	 * @param destination Destination vertex
	 * @param optimizeTime True if a route with minimal time should be found, false for minimal distance
	 * @param embolden True if the shortest path should be highlighted in the dot code, false otherwise
	 * @return Distance / Time of the shortest path from Origin to Destination
	 */
	public double dijkstra(Vertex origin, Vertex destination, boolean optimizeTime, boolean embolden) {
		HashMap<String, Double> distance = new HashMap<String, Double>();
		HashMap<String, Vertex> prev = new HashMap<String, Vertex>();
		
		// If the path should be marked, reset previous markings now
		if(embolden) {
			for(Edge edge: this.edges)
				edge.resetBold();
		}
		
		// Enqueue all vertices
		Queue<Vertex> queue = new LinkedList<Vertex>();
		queue.addAll(this.vertexList);
		
		// Initialize the distance of the start vertex to 0 or, if time is to be optimized, -waitTime
		// This is necessary as the waitTime of the source vertex is not counted towards total driving time
		// (bit hacky, but gets the job done)
		distance.put(origin.getName(), optimizeTime ? -origin.waitTime() : 0.0);
		
		while(queue.size() > 0) {
			// Get minimum vertex by current distance estimate
			// Treat null as positive infinity to avoid initializing the entire map to infinity
			Vertex min = Collections.min(queue, (Vertex a, Vertex b) -> (distance.get(a.getName()) == null ? 1 : distance.get(b.getName()) == null ? -1 : distance.get(a.getName()).compareTo(distance.get(b.getName()))));
			queue.remove(min);
			
			//System.out.println("Expanding vertex "+min.getName());
			
			for(Edge outgoing: min.getOutgoing()) {
				// If we reached the destination, we found the shortest path
				// If the minimum distance is null (think infinity), all vertices still in the queue are unreachable and we can abort the search
				if(min == destination || distance.get(min.getName()) == null)
					break;
				
				// Relax distance if the new distance estimate is better than the previous
				double newDist = distance.get(min.getName()).doubleValue() + (optimizeTime ? outgoing.time() + min.waitTime() : outgoing.distance());
				if(distance.get(outgoing.to.getName()) == null || distance.get(outgoing.to.getName()).doubleValue() > newDist) {
					//System.out.println("Found new shortest path to "+outgoing.to.getName());
					distance.put(outgoing.to.getName(), newDist);
					prev.put(outgoing.to.getName(), min); // Set shortest path predecessor
				}
			}
		}
		
		// If there is no path, return infinity
		if(distance.get(destination.getName()) == null)
			return Double.POSITIVE_INFINITY;
		
		// If the shortest path should be marked, walk backwards from the destination vertex and mark all encountered edges as bold
		if(embolden) {
			Vertex current = destination;
			while(prev.get(current.getName()) != null) {
				prev.get(current.getName()).getEdgeTo(current.getName()).embolden();
				current = prev.get(current.getName());
			}
		}
		
		// Return distance / time of shortest path
		return distance.get(destination.getName());
	}
	
	/**
	 * Get a dot code representation of the graph
	 * @return List of dot code lines representing the graph
	 */
	public ArrayList<String> toDotCode(){
		ArrayList<String> res = new ArrayList<String>();
		res.add("digraph {");
		// Edge definitions
		for(Edge edge: this.edges) {
			res.add(edge.toDotCode());
		}
		// Vertex definitions
		for(Vertex vertex: this.vertexList) {
			res.add(vertex.toDotCode());
		}
		res.add("}");
		//System.out.println(res.stream().map(Object::toString).collect(Collectors.joining("\n")));
		return res;
	}

}
