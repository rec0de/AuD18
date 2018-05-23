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
	* 
	* @param t minimum degree of the B-tree
	*/
    public B_TreeNode(int t) {
    	keys = new ArrayList<Entry>(t*2-1);
    	pointers = new B_TreeNode[t*2];
    	this.t = t;
    }
    
    public Entry find(String searchKey) {
    	Entry dummy = new Entry(searchKey, "", "");
    	
    	System.out.println("Searching for "+searchKey+" in "+this);
    	
    	int index = getIndexOf(dummy);
    	
    	if(index > -1) {
    		System.out.println("Is here!");
    		return keys.get(index);
    	}
    	else if(isLeaf) {
    		System.out.println("Is not in leaf -> is not in tree");
    		return null;
    	}
    	else {
    		System.out.println("Pointing to "+getPointerTowards(dummy));
    		return getPointerTowards(dummy).find(searchKey);
    	}
    }
    
    public int getIndexOf(Entry entry) {
    	for(int i = 0; i < this.currentLoad; i++) {
    		if(entry.compareTo(keys.get(i)) == 0)
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
    	System.out.println(pointer);
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
    	while(i < this.currentLoad && entry.compareTo(keys.get(i)) > 0) {
    		i += 1;
    	}
    	keys.add(i, entry);
    	this.currentLoad += 1;
    	
    	return true;
    }
    
    private B_TreeNode getPointerTowards(Entry entry) {
    	if(this.isLeaf)
    		throw new RuntimeException("Trying to get pointer from leaf node");
    	
    	int i = 0;
    	while(i < this.currentLoad && entry.compareTo(keys.get(i)) > 0) {
    		i += 1;
    	}
    	
    	if(pointers[i] == null)
    		throw new RuntimeException("Non-leaf node with capacity "+currentLoad+" does not have pointer "+i);
    	
    	return pointers[i];
    }
    
    public void splitChild(B_TreeNode child) {
    	
    	if(this.isFull())
    		throw new RuntimeException("Trying to split child of full node");
    	
    	System.out.println("SPLIT "+child);
    	
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
    	while(pointers[i] != child) {
    		pointers[i+1] = pointers[i];		
    		i -= 1;
    	}
    	pointers[i+1] = newRight;
    	keys.add(i, median);
    	currentLoad += 1;
    	
    	System.out.println("New left node: "+child);
    	System.out.println("Median: "+median);
    	System.out.println("New right node: "+newRight);
    }
    
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
    
    public boolean isFull() {
    	return this.currentLoad >= this.t * 2 - 1;
    }
    
    public boolean isLeaf() {
    	return this.isLeaf;
    }
    
    public void makeNonLeaf() {
    	this.isLeaf = false;
    }
    
    public Entry getEntry(int i) {
    	return keys.get(i);
    }
    
    public B_TreeNode getPointer(int i) {
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to get non-existent pointer");
    	return this.pointers[i];
    }
    
    public void setPointer(int i, B_TreeNode pointer) {
    	if(i > this.currentLoad)
    		throw new RuntimeException("Trying to set non-existent pointer");
    	if(isLeaf)
    		throw new RuntimeException("Trying to set pointer on leaf");
    	this.pointers[i] = pointer;
    }
    
    public void setCurrentLoad(int i) {
    	this.currentLoad = i;
    }
    
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