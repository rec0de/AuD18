/**
 * 
 */
package lab;

/**
 * Exception signaling that a given graph has infinite maximum flow because at
 * least one source and destination are equal
 */
public class InfiniteFlowException extends Exception {
	
	public InfiniteFlowException(String string) {
		super(string);
	}

	private static final long serialVersionUID = -2738022894329680602L;

}
