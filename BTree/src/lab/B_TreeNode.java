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
    
    public Entry find(String searchKey) {
    	Entry dummy = new Entry(searchKey, "", "");
    	
    	//System.out.println("Searching for "+searchKey+" in "+this);
    	
    	int index = getIndexOf(dummy);
    	
    	if(index > -1) {
    		//System.out.println("Is here!");
    		return this.getEntry(index);
    	}
    	else if(isLeaf) {
    		//System.out.println("Is not in leaf -> is not in tree");
    		return null;
    	}
    	else {
    		//System.out.println("Pointing to "+getPointerTowards(dummy));
    		return getPointerTowards(dummy).find(searchKey);
    	}
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
    	if(getIndexOf(entry) >= 0)
    		return false;
    	else if(isLeaf)
    		return insertValue(entry);
    	
    	if(this.isFull())
    		throw new RuntimeException("Insert expects non-full root node");
    	
    	B_TreeNode pointer = getPointerTowards(entry);
    	
    	if(pointer.isFull() && pointer.getIndexOf(entry) == -1) {
    		//System.out.println("Splitting while inserting "+entry.getKey());
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
    	
    	//System.out.println("SPLIT "+child);
    	
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
    	this.keys.add(i, median);
    	
    	//System.out.println("New left node: "+child);
    	//System.out.println("Median: "+median);
    	//System.out.println("New right node: "+newRight);
    }
    
    public Entry delete(Entry entry) {
    	
    	// Delete assumes the current node has at least t values
    	//if(this.currentLoad < this.t)
    	//	throw new RuntimeException("Delete invariant not met: Node has < t values");
    	
    	System.out.println("Deleting "+entry.getKey());
    	
    	int index = getIndexOf(entry);
    	
    	// entry is in node
    	if(index > -1) {
    		System.out.println("Entry is in node "+this.toString());
    		// Case 1, we're lucky
    		if(isLeaf) {
    			System.out.println("Entry is in leaf");
    			Entry deleted = keys.remove(index);
    			this.currentLoad -= 1;
    			return deleted;
    		}
    		// Case 2a, left child has at least t values
    		else if(this.getPointer(index).canBeSmaller()) {
    			Entry deleted = this.getEntry(index);
    			this.keys.set(index, this.getPointer(index).delete(this.getPointer(index).getMax()));
    			System.out.println("Setting key to "+this.getEntry(index));
    			return deleted;
    		}
    		// Case 2b, right child has at least t values
    		else if(this.getPointer(index+1).canBeSmaller()) {
    			Entry deleted = this.getEntry(index);
    			this.setEntry(index,this.getPointer(index+1).delete(this.getPointer(index+1).getMin()));
    			System.out.println("Setting key to "+this.getEntry(index));
    			return deleted;
    		}
    		// Case 2c, merge left and right child
    		else {
    			Entry deleted = this.getEntry(index);
    			B_TreeNode leftChild = this.getPointer(index);
    			B_TreeNode rightChild = this.getPointer(index+1);
    			for(int i = 0; i < rightChild.getCurrentLoad(); i++) {
    				leftChild.incrementLoad();
    				leftChild.setEntry(leftChild.getCurrentLoad()-1, rightChild.getEntry(i));
    				if(!rightChild.isLeaf())
    					leftChild.setPointer(leftChild.getCurrentLoad()-1, rightChild.getPointer(i));
    			}
    			if(!rightChild.isLeaf())
    				leftChild.setPointer(leftChild.getCurrentLoad(), rightChild.getPointer(rightChild.getCurrentLoad()));
    			for(int i = index; i < this.currentLoad-1; i++) {
    				this.setEntry(i, this.getEntry(i+1));
    				this.setPointer(i+1, this.getPointer(i+2));
    			}
    			this.decrementLoad();
    			if(this.currentLoad == 0)
    				throw new RuntimeException("Merging root");
    			return deleted;
    		}
    	}
    	// entry is not in node _and_ can't be in tree
    	else if(isLeaf) {
    		System.out.println("Entry is not in tree");
    		return null;
    	}
    	// entry is not in node
    	else {
    		System.out.println("Entry is not in node "+this.toString());
    		int nextRootIndex = 0;
        	while(nextRootIndex < this.currentLoad && entry.compareTo(this.getEntry(nextRootIndex)) > 0) {
        		nextRootIndex += 1;
        	}
        	
        	B_TreeNode nextRoot = this.getPointer(nextRootIndex);
    		// nextRoot has at least t values, we can recursively delete directly
    		if(nextRoot.canBeSmaller()) {
    			System.out.println("Node has enough values, continuing");
    			return nextRoot.delete(entry);
    		}
    		// Case 3a 1: Left sibling has at least t values
    		else if(nextRootIndex > 0 && this.getPointer(nextRootIndex-1).canBeSmaller()) {
    			System.out.println("Rotating left");
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
    			System.out.println("Rotating right");
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
    			System.out.println("Merging with left sibling");
    			B_TreeNode leftSibling = this.getPointer(nextRootIndex-1);
    			
    			leftSibling.incrementLoad();
    			leftSibling.setEntry(leftSibling.getCurrentLoad()-1, this.getEntry(nextRootIndex-1));
    			for(int i = 0; i < nextRoot.getCurrentLoad(); i++) {
    				leftSibling.incrementLoad();
    				leftSibling.setEntry(leftSibling.getCurrentLoad()-1, nextRoot.getEntry(i));
    				if(!leftSibling.isLeaf())
    					leftSibling.setPointer(leftSibling.getCurrentLoad()-1, nextRoot.getPointer(i));
    			}
    			if(!leftSibling.isLeaf())
    				leftSibling.setPointer(leftSibling.getCurrentLoad(), nextRoot.getPointer(nextRoot.getCurrentLoad()));
    			for(int i = nextRootIndex-1; i < this.currentLoad-1; i++) {
    				this.setEntry(i, this.getEntry(i+1));
    				this.setPointer(i+1, this.getPointer(i+2));
    			}
    			this.decrementLoad();

    			return leftSibling.delete(entry);
    		}
    		// Case 3b: Merge with right sibling
    		else {
    			System.out.println("Merging with right sibling");
    			B_TreeNode rightSibling = this.getPointer(nextRootIndex+1);
    			nextRoot.incrementLoad();
    			nextRoot.setEntry(nextRoot.getCurrentLoad()-1, this.getEntry(nextRootIndex));
    			for(int i = 0; i < rightSibling.getCurrentLoad(); i++) {
    				nextRoot.incrementLoad();
    				nextRoot.setEntry(nextRoot.getCurrentLoad()-1, rightSibling.getEntry(i));
    				if(!nextRoot.isLeaf())
    					nextRoot.setPointer(nextRoot.getCurrentLoad()-1, rightSibling.getPointer(i));
    			}
    			if(!nextRoot.isLeaf())
    				nextRoot.setPointer(nextRoot.getCurrentLoad(), rightSibling.getPointer(rightSibling.getCurrentLoad()));
    			for(int i = nextRootIndex; i < this.currentLoad-1; i++) {
    				this.setEntry(i, this.getEntry(i+1));
    				this.setPointer(i+1, this.getPointer(i+2));
    			}
    			this.decrementLoad();

    			return nextRoot.delete(entry);
    		}
    	}
    }
    
    ////////////////////////////////
    // Tree properties            //
    ////////////////////////////////
    
    public ArrayList<Entry> inorderTraversal(){
    	ArrayList<Entry> result = new ArrayList<Entry>();
    	
    	if(pointers[0] != null) {
    		result.addAll(pointers[0].inorderTraversal());
    	}
    	
    	for(int i = 0; i < this.currentLoad; i++) {
    		result.add(keys.get(i));
    		if(pointers[i+1] != null) {
    			result.addAll(pointers[i+1].inorderTraversal());
    		}
    	}
    	
    	return result;
    }
    
    public int getHeight() {
    	if(isLeaf)
    		return 0;
    	
    	int maxHeight = 0;
    	int cHeight;
    	for(int i = 0; i <= this.currentLoad; i++) {
    		if(pointers[i] != null) {
    			cHeight = pointers[i].getHeight();
    			if(cHeight > maxHeight)
    				maxHeight = cHeight;
    		}
    	}
    	return maxHeight + 1;
    }
    
    public int getSize() {
    	int res = this.currentLoad;
    	for(int i = 0; i <= this.currentLoad; i++) {
    		if(pointers[i] != null)
    			res += pointers[i].getSize();
    	}
    	return res;
    }
    
    private Entry getMin() {
    	if(isLeaf)
    		return this.getEntry(0);
    	
    	return this.getPointer(0).getMin();
    }
    
    private Entry getMax() {
    	if(isLeaf)
    		return this.getEntry(this.currentLoad-1);
    	
    	return this.getPointer(this.currentLoad).getMax();
    }
    
    
    ////////////////////////////////
    // Getters & Setters          //
    ////////////////////////////////
    
    public Entry getEntry(int i) {
    	if(i > this.currentLoad - 1)
    		throw new RuntimeException("Trying to get non-existent entry");
    	Entry res = this.keys.get(i);
    	if(res == null)
    		throw new RuntimeException("Value "+i+" should exist but doesn't. Load: "+this.currentLoad);
    	return res;
    }
    
    public void setEntry(int i, Entry value) {
    	if(i > this.currentLoad- 1)
    		throw new RuntimeException("Trying to set non-existent entry");
    	if(this.keys.size() <= i)
    		keys.add(value);
    	else
    		keys.set(i, value);
    }
    
    protected void addEntry(int i, Entry value) {
    	if(i > this.currentLoad - 1)
    		throw new RuntimeException("Trying to add non-existent entry");
    	keys.add(i, value);
    }
    
    public B_TreeNode getPointer(int i) {
    	if(isLeaf)
    		throw new RuntimeException("Trying to get pointer of leaf");
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to get non-existent pointer");
    	if(this.pointers[i] == null)
    		throw new RuntimeException("Pointer "+i+" should exist but doesn't. Load: "+this.currentLoad+" Node: "+this.toString());
    	return this.pointers[i];
    }
    
    public void setPointer(int i, B_TreeNode pointer) {
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to set non-existent pointer");
    	if(isLeaf)
    		throw new RuntimeException("Trying to set pointer on leaf");
    	this.pointers[i] = pointer;
    }
    
    public void addPointerAtZero(B_TreeNode pointer) {
    	if(isLeaf)
    		throw new RuntimeException("Trying to add pointer on leaf");
    	
    	for(int i = currentLoad-1; i >= 0; i--) {
    		this.pointers[i+1] = this.pointers[i];
    	}
    	this.pointers[0] = pointer;
    }
    
    public int getCurrentLoad() {
    	return this.currentLoad;
    }
    
    public void setCurrentLoad(int i) {
    	if(i > 2*this.t - 1)
    		throw new RuntimeException("Trying to increase node load past limit");
    	this.currentLoad = i;
    }
    
    public void incrementLoad() {
    	this.setCurrentLoad(this.currentLoad+1);
    }
    
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