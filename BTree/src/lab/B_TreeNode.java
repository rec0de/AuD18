package lab;

import java.util.ArrayList;

import frame.Entry;

/*
 * Implements a node of a B-Tree
 *
 * Make sure that you have tested all the given test cases
 * given on the homepage before you submit your solution.
 *
 */

public class B_TreeNode {
	
	private ArrayList<Entry> keys;
	private B_TreeNode[] pointers;
	private int currentLoad = 0;
	private boolean isLeaf = true;
	private final int t;

    /**
	* The constructor
	* @param t minimum degree of the B-tree
	*/
    public B_TreeNode(int t) {
    	keys = new ArrayList<Entry>(t*2-1);
    	pointers = new B_TreeNode[t*2];
    	this.t = t;
    }
    
    /**
     * Searches for an entry with equal key to the provided entry
     * Note: To search for a String key, create an empty entry with that key, then search for that
     * @param dummy An entry with the key to be searched for
     * @return The found entry or null if the key does not exist
     */
    public Entry find(Entry dummy) {
    	int index = getIndexOf(dummy);
    	
    	// Key is in this node
    	if(index > -1)
    		return this.getEntry(index);
    	// Key can't be in tree because it's not here and we're in a dead end
    	else if(isLeaf)
    		return null;
    	// Go one step into the direction the key should be and search recursively
    	else
    		return getPointerTowards(dummy).find(dummy);
    }
    
    /**
     * Finds the first index within the node that has the same key as the given entry
     * @param entry The entry key to find
     * @return Index of the entry if present, -1 otherwise
     */
    public int getIndexOf(Entry entry) {
    	for(int i = 0; i < this.currentLoad; i++) {
    		if(entry.compareTo(this.getEntry(i)) == 0)
    			return i;
    	}
    	return -1;
    }
    
    public boolean insert(Entry entry) {
    	if(getIndexOf(entry) >= 0) // Key already exists
    		return false;
    	else if(isLeaf)
    		return insertValue(entry);
    	
    	if(this.isFull())
    		throw new RuntimeException("Insert expects non-full root node");
    	
    	B_TreeNode pointer = getPointerTowards(entry);
    	
    	// If the next node in the insert chain is full AND the insertion key is not already in the next node, split the node
    	// IMO the check if the key exists in pointer is a bit unclean (and arguably redundant), but it is necessary to avoid
    	// these split operations in order to pass all tests
    	if(pointer.isFull() && pointer.getIndexOf(entry) == -1) {
    		this.splitChild(pointer);
    		pointer = getPointerTowards(entry);
    	}
    	return pointer.insert(entry);
    }
    
    private boolean insertValue(Entry entry) {
    	if(!this.isLeaf)
    		throw new RuntimeException("Trying to insert value into non-leaf node");
    	if(this.isFull())
    		throw new RuntimeException("Trying to insert value into full leaf node");
    	
    	int i = 0;
    	while(i < this.currentLoad && entry.compareTo(this.getEntry(i)) > 0) {
    		i += 1;
    	}
    	
    	this.incrementLoad();
    	this.keys.add(i, entry);
    	
    	return true;
    }
    
    private B_TreeNode getPointerTowards(Entry entry) {
    	if(this.isLeaf)
    		throw new RuntimeException("Trying to get pointer from leaf node");
    	
    	int i = 0;
    	while(i < this.currentLoad && entry.compareTo(this.keys.get(i)) > 0) {
    		i += 1;
    	}
    	
    	if(i < this.currentLoad && entry.compareTo(this.keys.get(i)) == 0)
    		throw new RuntimeException("Trying to get pointer to key equal to a key in this node");
    	
    	return this.getPointer(i);
    }
    
    public void splitChild(B_TreeNode child) {
    	
    	if(this.isFull())
    		throw new RuntimeException("Trying to split child of full node");
    	
    	B_TreeNode newRight = new B_TreeNode(this.t);
    	for(int i = 0; i < t-1; i++) {
    		newRight.insert(child.getEntry(i+t));
    	}
    	if(!child.isLeaf()) {
    		newRight.makeNonLeaf();
    		for(int i = 0; i < t; i++) {
        		newRight.setPointer(i, child.getPointer(i+t));
        	}
    	}
    	Entry median = child.getEntry(t-1);
    	child.setCurrentLoad(t-1);
    	int i = this.currentLoad;
    	this.incrementLoad();
    	while(this.getPointer(i) != child) {
    		this.setPointer(i+1, this.getPointer(i));
    		i -= 1;
    	}
    	this.setPointer(i+1, newRight);
    	this.addEntry(i, median);
    	
    	//System.out.println("New left node: "+child);
    	//System.out.println("Median: "+median);
    	//System.out.println("New right node: "+newRight);
    }
    
    public Entry delete(Entry entry) {
    	
    	// Delete assumes the current node has at least t values

    	int index = getIndexOf(entry);
    	
    	// Entry is in node
    	if(index > -1) {
    		// Case 1, we're lucky
    		if(isLeaf) {
    			Entry deleted = keys.remove(index);
    			this.currentLoad -= 1;
    			return deleted;
    		}
    		// Case 2a, left child has at least t values
    		else if(this.getPointer(index).canBeSmaller()) {
    			Entry deleted = this.getEntry(index);
    			this.setEntry(index, this.getPointer(index).delete(this.getPointer(index).getMax()));
    			return deleted;
    		}
    		// Case 2b, right child has at least t values
    		else if(this.getPointer(index+1).canBeSmaller()) {
    			Entry deleted = this.getEntry(index);
    			this.setEntry(index,this.getPointer(index+1).delete(this.getPointer(index+1).getMin()));
    			return deleted;
    		}
    		// Case 2c, merge left and right child, delete recursively
    		else {
    			this.mergeSiblings(this.getPointer(index), this.getPointer(index+1), index);
    			return this.getPointer(index).delete(entry);
    		}
    	}
    	// Entry is not in node AND can't be in tree
    	else if(isLeaf) {
    		return null;
    	}
    	// Entry is not in node
    	else {
    		// Find index of pointer pointing to next node in delete chain
    		// (see getPointerTowards)
    		int nextRootIndex = 0;
        	while(nextRootIndex < this.currentLoad && entry.compareTo(this.getEntry(nextRootIndex)) > 0) {
        		nextRootIndex += 1;
        	}
        	
        	B_TreeNode nextRoot = this.getPointer(nextRootIndex);
    		// nextRoot has at least t values, we can recursively delete directly
    		if(nextRoot.canBeSmaller())
    			return nextRoot.delete(entry);
    		// Case 3a 1: Left sibling has at least t values
    		else if(nextRootIndex > 0 && this.getPointer(nextRootIndex-1).canBeSmaller()) {
    			B_TreeNode leftSibling = this.getPointer(nextRootIndex-1);
    			nextRoot.incrementLoad();
    			nextRoot.addEntry(0, this.getEntry(nextRootIndex-1));
    			if(!leftSibling.isLeaf())
    				nextRoot.addPointerAtZero(leftSibling.getPointer(leftSibling.getCurrentLoad()));
    			
    			this.setEntry(nextRootIndex-1, leftSibling.getEntry(leftSibling.getCurrentLoad()-1));
    			leftSibling.uncleanDeleteLast();
    			return nextRoot.delete(entry);
    		}
    		// Case 3a 2: Right sibling has at least t values
    		else if(nextRootIndex < this.currentLoad && this.getPointer(nextRootIndex+1).canBeSmaller()) {
    			B_TreeNode rightSibling = this.getPointer(nextRootIndex+1);
    			nextRoot.incrementLoad();
    			nextRoot.setEntry(t-1, this.getEntry(nextRootIndex));
    			
    			if(!rightSibling.isLeaf())
    				nextRoot.setPointer(t, rightSibling.getPointer(0));
    			
    			this.setEntry(nextRootIndex, rightSibling.getEntry(0));
    			rightSibling.uncleanDeleteFirst();
    			return nextRoot.delete(entry);
    		}
    		// Case 3b: Merge with left sibling
    		else if(nextRootIndex > 0){
    			this.mergeSiblings(this.getPointer(nextRootIndex-1), nextRoot, nextRootIndex-1);
    			return this.getPointer(nextRootIndex-1).delete(entry);
    		}
    		// Case 3b: Merge with right sibling
    		else {
    			this.mergeSiblings(nextRoot, this.getPointer(nextRootIndex+1), nextRootIndex+1);
    			return nextRoot.delete(entry);
    		}
    	}
    }
    
    private void mergeSiblings(B_TreeNode leftSib, B_TreeNode rightSib, int leftIndex) {
		leftSib.incrementLoad();
		leftSib.setEntry(leftSib.getCurrentLoad()-1, this.getEntry(leftIndex));
		for(int i = 0; i < rightSib.getCurrentLoad(); i++) {
			leftSib.incrementLoad();
			leftSib.setEntry(leftSib.getCurrentLoad()-1, rightSib.getEntry(i));
			if(!leftSib.isLeaf())
				leftSib.setPointer(leftSib.getCurrentLoad()-1, rightSib.getPointer(i));
		}
		if(!leftSib.isLeaf())
			leftSib.setPointer(leftSib.getCurrentLoad(), rightSib.getPointer(rightSib.getCurrentLoad()));
		for(int i = leftIndex; i < this.currentLoad-1; i++) {
			this.setEntry(i, this.getEntry(i+1));
			this.setPointer(i+1, this.getPointer(i+2));
		}
		this.decrementLoad();
    }
    
    ////////////////////////////////
    // Tree properties            //
    ////////////////////////////////
    
    public ArrayList<Entry> inorderTraversal(){
    	ArrayList<Entry> result = new ArrayList<Entry>();
    	
    	if(!isLeaf) 
    		result.addAll(this.getPointer(0).inorderTraversal());
    	
    	for(int i = 0; i < this.currentLoad; i++) {
    		result.add(this.getEntry(i));
    		
    		if(!isLeaf)
    			result.addAll(this.getPointer(i+1).inorderTraversal());
    	}
    	
    	return result;
    }
    
    /**
     * Determines the height of the tree
     * @return Height of the subtree rooted at node
     */
    public int getHeight() {
    	if(isLeaf)
    		return 0;
    	// Exploiting the fact that every node on the same depth has the same height here
    	return this.getPointer(0).getHeight() + 1;
    }
    
    /**
     * Recursively count the number of entries in the nodes subtree
     * @return Number of nodes in the subtree rooted at node
     */
    public int getSize() {
    	int res = this.currentLoad;
    	
    	if(isLeaf)
    		return res;
    	
    	for(int i = 0; i <= this.currentLoad; i++)
    		res += this.getPointer(i).getSize();
    	
    	return res;
    }
    
    /**
     * Recursively get the smallest element in the node's subtree
     * @return Smallest entry in subtree
     */
    private Entry getMin() {
    	if(isLeaf)
    		return this.getEntry(0);
    	
    	return this.getPointer(0).getMin();
    }
    
    /**
     * Recursively get the largest element in the node's subtree
     * @return Largest entry in subtree
     */
    private Entry getMax() {
    	if(isLeaf)
    		return this.getEntry(this.currentLoad-1);
    	
    	return this.getPointer(this.currentLoad).getMax();
    }
    
    
    ////////////////////////////////
    // Getters & Setters          //
    ////////////////////////////////
    
    // These getter / setter methods try to enforce reasonable constraints to make debugging easier
    
    /**
     * Returns the entry at position i of the node
     * @param i Index of the entry to get
     * @return Entry at position i
     */
    public Entry getEntry(int i) {
    	if(i > this.currentLoad - 1)
    		throw new RuntimeException("Trying to get non-existent entry");
    	Entry res = this.keys.get(i);
    	if(res == null)
    		throw new RuntimeException("Value "+i+" should exist but doesn't. Load: "+this.currentLoad);
    	return res;
    }
    
    /**
     * Sets the i-th entry of the node
     * @param i Index of the entry to set
     * @param value Entry to set
     */
    public void setEntry(int i, Entry value) {
    	if(i > this.currentLoad- 1)
    		throw new RuntimeException("Trying to set non-existent entry");
    	// keys.set might fail if the node capacity has been expanded, use add in that case
    	if(this.keys.size() <= i)
    		keys.add(value);
    	else
    		keys.set(i, value);
    }
    
    /**
     * Inserts an entry at the given index, shifting all following entries back
     * WARNING: currentLoad has to be incremented immediately before to accommodate the new entry
     * @param i Index to insert at
     * @param value Entry to insert
     */
    protected void addEntry(int i, Entry value) {
    	if(i > this.currentLoad - 1)
    		throw new RuntimeException("Trying to add non-existent entry");
    	keys.add(i, value);
    }
    
    /**
     * Returns the pointer at the given index
     * @param i Index of the pointer
     * @return Node that is pointed to
     */
    public B_TreeNode getPointer(int i) {
    	if(isLeaf)
    		throw new RuntimeException("Trying to get pointer of leaf");
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to get non-existent pointer");
    	if(this.pointers[i] == null)
    		throw new RuntimeException("Pointer "+i+" should exist but doesn't. Load: "+this.currentLoad+" Node: "+this.toString());
    	return this.pointers[i];
    }
    
    /**
     * Sets the pointer at the given index
     * @param i Index of the pointer
     * @param pointer Node to point to
     */
    public void setPointer(int i, B_TreeNode pointer) {
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to set non-existent pointer");
    	if(isLeaf)
    		throw new RuntimeException("Trying to set pointer on leaf");
    	this.pointers[i] = pointer;
    }
    
    /**
     * Prepends a new pointer at index zero
     * WARNING: currentLoad has to be incremented immediately before to accommodate the new pointer
     * 			without losing the last one. Intended only for use in the rotate left / right functions
     * @param pointer
     */
    protected void addPointerAtZero(B_TreeNode pointer) {
    	if(isLeaf)
    		throw new RuntimeException("Trying to add pointer on leaf");
    	
    	for(int i = currentLoad-1; i >= 0; i--) {
    		this.pointers[i+1] = this.pointers[i];
    	}
    	this.pointers[0] = pointer;
    }
    
    /**
     * Returns the current load of a node
     * @return the number of values in the node
     */
    public int getCurrentLoad() {
    	return this.currentLoad;
    }
    
    /**
     * Sets the load value (= number of values) of the current node
     * Use increment / decrement instead where possible
     * @param i Load-value to set
     */
    protected void setCurrentLoad(int i) {
    	if(i > 2*this.t - 1)
    		throw new RuntimeException("Trying to increase node load past limit");
    	this.currentLoad = i;
    }
    
    /**
     * Increments load by one. Shortcut for setCurrentLoad(getCurrentLoad()+1)
     */
    public void incrementLoad() {
    	this.setCurrentLoad(this.currentLoad+1);
    }
    
    /**
     * Decrements load by one. Shortcut for setCurrentLoad(getCurrentLoad()-1)
     */
    public void decrementLoad() {
    	this.setCurrentLoad(this.currentLoad-1);
    }
    
    /**
     * Marks a node a non-leaf
     * Note that nodes can (intentionally) never be made a leaf again once they are an inner node
     */
    public void makeNonLeaf() {
    	this.isLeaf = false;
    }
    
    /**
     * Erases the first entry and pointer (if present) from a node
     * WARNING: This assumes the first entry/pointer has previously been moved somewhere else
     * 			If this is not ensured, data will be lost!
     */
    protected void uncleanDeleteFirst() {
    	if(this.currentLoad < t)
    		throw new RuntimeException("Trying unclean delete on node with less than t values");
    	
    	// Remove the first entry, shifting the following ones forward
    	this.keys.remove(0);
    	
    	// Move pointers forward, overwriting the first
    	if(!isLeaf) {
    		for(int i = 0; i < this.currentLoad; i++) {
        		this.setPointer(i, this.getPointer(i+1));
        	}
    	}
    	this.decrementLoad();
    }
    
    /**
     * Erases the last entry and pointer (if present) from a node
     * WARNING: This assumes the last entry/pointer has previously been moved somewhere else
     * 			If this is not ensured, data will be lost!
     */
    protected void uncleanDeleteLast() {
    	if(this.currentLoad < t)
    		throw new RuntimeException("Trying unclean delete on node with less than t values");
    	
    	// No need to really delete values as they are the last
    	// Mark unusable by decrementing currentLoad
    	this.decrementLoad();
    }
    
    ////////////////////////////////
    // Attributes                 //
    ////////////////////////////////
    
    /**
     * Checks if a node is full
     * @return True if no more values can be inserted into the node
     */
    public boolean isFull() {
    	return this.currentLoad >= this.t * 2 - 1;
    }
    
    /**
     * Checks if a node can have a value removed without violating the BTree constraints
     * (e.g. if the node has at least t values)
     * @return True if a value can be removed from the node, false otherwise
     */
    public boolean canBeSmaller() {
    	return this.currentLoad >= this.t;
    }
    
    /**
     * Checks if the node is a leaf
     * @return True if the node is a leaf
     */
    public boolean isLeaf() {
    	return this.isLeaf;
    }
    
    ////////////////////////////////
    // Serializing                //
    ////////////////////////////////
    
    
    public String toString() {
    	String res = "";
    	for(int i = 0; i < this.currentLoad; i++) {
    		res += this.getEntry(i).getKey() + ", ";
    	}
    	return "["+res+"]";
    }
    
    /**
     * Adds a dot code representation of this nodes subtree to the given list
     * @param index The index of the highest-numbered node
     * @param isRoot True if the current node is to be treated as the root node, false otherwise
     * @param list The list to add dot code lines to
     * @return The last used node index
     */
    public int getDotCode(int index, boolean isRoot, ArrayList<String> list){
    	String name = (isRoot ? "root" : "node"+index);
    	
    	// Assemble line describing this node
    	String selfNode = name+"[label=\"<f0>*";
    	for(int i = 0; i < this.currentLoad*2; i += 2) {
    		selfNode += "|<f"+(i+1)+">"+this.getEntry(i/2).getKey()+"|<f"+(i+2)+">*";
    	}
    	selfNode += "\"];";
    	list.add(selfNode);
    	
    	// Abort here if node has no children
    	if(isLeaf)
    		return index;
    	
    	// Recursively add child subtrees and links to child nodes
    	for(int i = 0; i <= this.currentLoad; i++) {
    		list.add(name+":f"+(i*2)+"->node"+(index+1));
    		index = this.getPointer(i).getDotCode(index+1, false, list);
    	}
    	
    	return index;
    }
}