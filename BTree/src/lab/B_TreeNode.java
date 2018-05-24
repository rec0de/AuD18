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
	private boolean isRoot = false;
	private final int t;

    /**
	* The constructor
	* 
	* @param t minimum degree of the B-tree
	*/
    public B_TreeNode(int t) {
    	keys = new ArrayList<Entry>(t*2-1);
    	pointers = new B_TreeNode[t*2];
    	this.t = t;
    }
    
    public B_TreeNode(int t, boolean isRoot) {
    	keys = new ArrayList<Entry>(t*2-1);
    	pointers = new B_TreeNode[t*2];
    	this.t = t;
    	this.isRoot = isRoot;
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
    
    public int getIndexOf(Entry entry) {
    	for(int i = 0; i < this.currentLoad; i++) {
    		if(entry.compareTo(this.getEntry(i)) == 0)
    			return i;
    	}
    	return -1;
    }
    
    public boolean insert(Entry entry) {
    	if(isLeaf)
    		return getIndexOf(entry) > 0 ? false : insertValue(entry);
    	
    	if(this.isFull())
    		throw new RuntimeException("Insert expects non-full root node");
    	
    	B_TreeNode pointer = getPointerTowards(entry);
    	
    	if(pointer.isFull()) {
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
    	
    	this.setCurrentLoad(this.currentLoad + 1);
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
    	
    	if(pointers[i] == null)
    		throw new RuntimeException("Non-leaf node with capacity "+currentLoad+" does not have pointer "+i);
    	
    	return pointers[i];
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
    	this.setCurrentLoad(this.currentLoad + 1);
    	while(pointers[i] != child) {
    		this.setPointer(i+1, pointers[i]);
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
    	if(this.currentLoad < this.t)
    		throw new RuntimeException("Delete invariant not met: Node has < t values");
    	
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
    		else if(pointers[index].canBeSmaller()) {
    			Entry deleted = this.getEntry(index);
    			this.keys.set(index, pointers[index].delete(pointers[index].getMax()));
    			System.out.println("Setting key to "+this.getEntry(index));
    			return deleted;
    		}
    		// Case 2b, right child has at least t values
    		else if(pointers[index+1].canBeSmaller()) {
    			Entry deleted = this.getEntry(index);
    			this.keys.set(index, pointers[index+1].delete(pointers[index+1].getMin()));
    			return deleted;
    		}
    		// Case 2c, merge left and right child
    		else {
    			Entry deleted = this.getEntry(index);
    			B_TreeNode leftChild = pointers[index];
    			B_TreeNode rightChild = pointers[index+1];
    			for(int i = 0; i < rightChild.getCurrentLoad(); i++) {
    				leftChild.setCurrentLoad(leftChild.getCurrentLoad()+1);
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
    			this.setCurrentLoad(this.currentLoad - 1);
    			if(this.currentLoad == 0)
    				throw new RuntimeException("Merging root");
    			return deleted;
    		}
    	}
    	// entry is not in node _and_ can't be in tree
    	else if(isLeaf) {
    		return null;
    	}
    	// entry is not in node
    	else {
    		System.out.println("Entry is not in node "+this.toString());
    		int nextRootIndex = 0;
        	while(nextRootIndex < this.currentLoad && entry.compareTo(keys.get(nextRootIndex)) > 0) {
        		nextRootIndex += 1;
        	}
        	
        	B_TreeNode nextRoot = this.getPointer(nextRootIndex);
    		// nextRoot has at least t values, we can recursively delete directly
    		if(nextRoot.canBeSmaller()) {
    			System.out.println("Node has enough values, continuing");
    			return nextRoot.delete(entry);
    		}
    		// Case 3a 1: Left sibling has at least t values
    		else if(nextRootIndex > 1 && pointers[nextRootIndex-1].canBeSmaller()) {
    			System.out.println("Rotating left");
    			nextRoot.setCurrentLoad(this.t);
    			nextRoot.setEntry(t-1, this.keys.get(nextRootIndex-1));
    			this.keys.set(nextRootIndex-1, pointers[nextRootIndex-1].delete(pointers[nextRootIndex-1].getEntry(pointers[nextRootIndex-1].getCurrentLoad()-1)));
    			return nextRoot.delete(entry);
    		}
    		// Case 3a 2: Right sibling has at least t values
    		else if(nextRootIndex <= this.currentLoad && pointers[nextRootIndex+1].canBeSmaller()) {
    			System.out.println("Rotating right");
    			nextRoot.setCurrentLoad(this.t);
    			nextRoot.setEntry(t-1, this.keys.get(nextRootIndex));
    			this.keys.set(nextRootIndex, pointers[nextRootIndex+1].delete(pointers[nextRootIndex+1].getEntry(0)));
    			return nextRoot.delete(entry);
    		}
    		// Case 3b: Merge with left sibling
    		else if(nextRootIndex > 1){
    			System.out.println("Merging with left sibling");
    			B_TreeNode leftSibling = pointers[nextRootIndex-1];
    			leftSibling.setCurrentLoad(leftSibling.getCurrentLoad()+1);
    			leftSibling.setEntry(leftSibling.getCurrentLoad()-1, this.getEntry(nextRootIndex-1));
    			for(int i = 0; i < nextRoot.getCurrentLoad(); i++) {
    				leftSibling.setCurrentLoad(leftSibling.getCurrentLoad()+1);
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
    			this.setCurrentLoad(this.currentLoad - 1);
    			
    			if(this.currentLoad == 0)
    				throw new RuntimeException("Merging root");

    			return leftSibling.delete(entry);
    		}
    		// Case 3b: Merge with right sibling
    		else {
    			System.out.println("Merging with right sibling");
    			B_TreeNode rightSibling = pointers[nextRootIndex+1];
    			nextRoot.setCurrentLoad(nextRoot.getCurrentLoad()+1);
    			nextRoot.setEntry(nextRoot.getCurrentLoad()-1, this.getEntry(nextRootIndex));
    			for(int i = 0; i < rightSibling.getCurrentLoad(); i++) {
    				nextRoot.setCurrentLoad(nextRoot.getCurrentLoad()+1);
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
    			this.setCurrentLoad(this.currentLoad - 1);
    			
    			if(this.currentLoad == 0)
    				throw new RuntimeException("Merging root");

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
    	
    	return pointers[0].getMin();
    }
    
    private Entry getMax() {
    	if(isLeaf)
    		return this.getEntry(this.currentLoad-1);
    	
    	return pointers[this.currentLoad].getMax();
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
    
    public B_TreeNode getPointer(int i) {
    	if(isLeaf)
    		throw new RuntimeException("Trying to get pointer of leaf");
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to get non-existent pointer");
    	if(this.pointers[i] == null)
    		throw new RuntimeException("Pointer "+i+" should exist but doesn't. Load: "+this.currentLoad);
    	return this.pointers[i];
    }
    
    public void setPointer(int i, B_TreeNode pointer) {
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to set non-existent pointer");
    	if(isLeaf)
    		throw new RuntimeException("Trying to set pointer on leaf");
    	this.pointers[i] = pointer;
    }
    
    public int getCurrentLoad() {
    	return this.currentLoad;
    }
    
    public void setCurrentLoad(int i) {
    	if(i > 2*this.t - 1)
    		throw new RuntimeException("Trying to increase node load past limit");
    	this.currentLoad = i;
    }
    
    public void makeNonLeaf() {
    	this.isLeaf = false;
    }
    
    ////////////////////////////////
    // Attributes                 //
    ////////////////////////////////
    
    public boolean isFull() {
    	return this.currentLoad >= this.t * 2 - 1;
    }
    
    public boolean canBeSmaller() {
    	return this.currentLoad >= this.t;
    }
    
    public boolean isLeaf() {
    	return this.isLeaf;
    }
    
    ////////////////////////////////
    // Serializing                //
    ////////////////////////////////
    
    public String toString() {
    	String res = "";
    	for(int i = 0; i < this.currentLoad; i++) {
    		res += keys.get(i).getKey() + ", ";
    	}
    	return "["+res+"]";
    }
    
    public int getDotCode(int index, boolean isRoot, ArrayList<String> list){
    	String name = (isRoot ? "root" : "node"+index);
    	String selfNode = name+"[label=\"<f0>*";
    	for(int i = 0; i < this.currentLoad*2; i += 2) {
    		selfNode += "|<f"+i+">"+keys.get(i/2).getKey()+"|<f"+(i+1)+">*";
    	}
    	selfNode += "\"];";
    	list.add(selfNode);
    	
    	if(isLeaf)
    		return index;
    	
    	for(int i = 0; i <= this.currentLoad; i++) {
    		list.add(name+":f"+(i*2)+"->node"+(index+1));
    		index = pointers[i].getDotCode(index+1, false, list);
    	}
    	
    	return index;
    }
}