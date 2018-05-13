package lab;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import frame.Entry;

/*
 * Implements a Hash-Table structure as introduced in the 
 * lecture to store the information read by the RFID 
 * readers in the library.
 *	
 * Make sure that you have tested all the given test cases
 * given on the homepage before you submit your solution.
 *
 */

public class HashTable {
	
	private int capacity;
	private int hashFunction; // 0: division, 1: folding, 2: mid_square
	private int probingMode; // 0: linear, 1: quadratic
	
	private Entry[] data;
	private int valueCount;

	/**
	 * The constructor
	 * 
	 * @param initialCapacity
	 *            represents the initial size of the Hash Table.
	 * @param hashFunction
	 *            can have the following values: division folding mid_square
	 * @param collisionResolution
	 *            can have the following values: linear_probing quadratic_probing
	 * 
	 *            The Hash-Table itself should be implemented as an array of entries
	 *            (Entry[] in Java) and no other implementation will be accepted.
	 *            When the load factor exceeds 75%, the capacity of the Hash-Table
	 *            should be increased as described in the method rehash below. We
	 *            assume a bucket factor of 1.
	 */
	public HashTable(int k, String hashFunction, String collisionResolution) {
		this.data = new Entry[k];
		this.hashFunction = hashFunction.equals("mid_square") ? 2 : hashFunction.equals("folding") ? 1 : 0;
		this.probingMode = collisionResolution.equals("quadratic_probing") ? 1 : 0;
		this.valueCount = 0;
		this.capacity = k;
	}

	/**
	 * This method takes as input the name of a file containing a sequence of
	 * entries that should be inserted into the Hash-Table in the order they appear
	 * in the file. You cannot make any assumptions on the order of the entries nor
	 * is it allowed to change the order given in the file. You can assume that the
	 * file is located in the same directory as the executable program. The input
	 * file is similar to the input file for lab 1. The return value is the number
	 * of entries successfully inserted into the Hash-Table.
	 * 
	 * @param filename
	 *            name of the file containing the entries
	 * @return returns the number of entries successfully inserted in the
	 *         Hash-Table.
	 */
	public int loadFromFile(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    String[] parts;
		    int insertionCount = 0;
		    
		    while ((line = br.readLine()) != null) {
		    	parts = line.split(";");
		    	Entry entry = new Entry(parts[0], parts[1], parts[2]);

		    	if(this.insert(entry))
		    		insertionCount += 1;
		    }
		    
		    System.out.println(insertionCount);
		    
		    return insertionCount;
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			return 0;
		} catch (IOException e) {
			System.out.println("IO Exception");
			return 0;
		}
	}

	/**
	 * This method inserts the entry insertEntry into the Hash-Table. Note that you
	 * have to deal with collisions if you want to insert an entry into a slot which
	 * is not empty. This method returns true if the insertion of the entry
	 * insertEntry is successful and false if the key of this entry already exists
	 * in the Hash-Table (the existing key/value pair is left unchanged).
	 * 
	 * @param insertEntry
	 *            entry to insert into the Hash-table
	 * @return returns true if the entry insertEntry is successfully inserted false
	 *         if the entry already exists in the Hash-Table
	 */
	public boolean insert(Entry insertEntry) {
		int address = this.getHashAddr(insertEntry);
		int base = address;
		int i = 1;
		while(this.data[address] != null && !this.data[address].isDeleted()) {
			if(i > this.capacity || this.data[address].getKey().equals(insertEntry.getKey()))
				return false;
			address = this.getProbingAddr(base, i);
			i += 1;
		}
		
		if(this.data[address] == null)
			this.valueCount += 1;
		
		this.data[address] = insertEntry;
		
		// Trigger rehash if load factor > 0.75
		if(((double) this.valueCount) / this.capacity > 0.75)
			this.rehash();
		
		System.out.println("Load: "+Double.toString(((double) this.valueCount) / this.capacity));
		
		return true;
	}

	/**
	 * This method deletes the entry from the Hash-Table, having deleteKey as key
	 * This method returns the entry, having deleteKey as key if the deletion is
	 * successful and null if the key deleteKey is not found in the Hash-Table.
	 * 
	 * @param deleteKey
	 *            key of the entry to delete from the Hash-Table
	 * @return returns the deleted entry if the deletion ends successfully null if
	 *         the entry is not found in the Hash-Table
	 */
	public Entry delete(String deleteKey) {
		int address = this.getHashAddr(deleteKey);
		int base = address;
		int i = 1;
		while(this.data[address] != null && !this.data[address].getKey().equals(deleteKey)) {
			if(i > this.capacity)
				return null;
			address = this.getProbingAddr(base, i);
			i += 1;
		}
		
		if(this.data[address] != null) {
			this.data[address].markDeleted();
			return this.data[address];
		}
		else
			return null;
	}

	/**
	 * This method searches in the Hash-Table for the entry with key searchKey. It
	 * returns the entry, having searchKey as key if such an entry is found, null
	 * otherwise.
	 * 
	 * @param searchKey
	 *            key of the entry to find in the Hash-table
	 * @return returns the entry having searchKey as key if such an entry exists
	 *         null if the entry is not found in the Hash-Table
	 */
	public Entry find(String searchKey) {
		int address = this.getHashAddr(searchKey);
		int base = address;
		int i = 1;
		while(this.data[address] != null && !this.data[address].getKey().equals(searchKey)) {
			if(i > this.capacity)
				return null;
			address = this.getProbingAddr(base, i);
			i += 1;
		}
		
		return this.data[address].isDeleted() ? null : this.data[address];
	}

	/**
	 * This method returns a ArrayList<String> containing the output Hash-Table. The
	 * output should be directly interpretable dot code. Each item in the ArrayList
	 * corresponds to one line of the output Hash-Table. The nodes of the output
	 * Hash-Table should contain the keys of the entries and also the data.
	 * 
	 * @return returns the output Hash-Table in directly interpretable dot code
	 */
	public ArrayList<String> getHashTable() {
		ArrayList<String> dot = new ArrayList<String>();
		dot.add("digraph {");
		dot.add("splines=true;");
		dot.add("nodesep=.01;");
		dot.add("rankdir=LR;");
		dot.add("node[fontsize=8,shape=record,height=.1;]");
		String addressdef = "[fontsize=12,label=\"";
		for(int i = 0; i < this.capacity; i++) {
			addressdef += "<f"+Integer.toString(i)+">"+Integer.toString(i)+(i < this.capacity - 1 ? "|" : "");
		}
		addressdef += "\"];";
		dot.add(addressdef);
		
		int j = 1;
		for(int i = 0; i < this.capacity; i++) {
			if(this.data[i] != null) {
				String insertSequence = getInsertionSequence(this.data[i].getKey());
				String line = "node"+Integer.toString(j)+"[label=\"{<l>" + this.data[i].getKey() + '|' + this.data[i].getData() + insertSequence + "}\"];";
				dot.add(line);
				System.out.println(line);
				j += 1;
			}
		}
		
		j = 1;
		for(int i = 0; i < this.capacity; i++) {
			if(this.data[i] != null) {
				String line = "ht:f"+Integer.toString(i)+"->node" + Integer.toString(j) + ":l;";
				dot.add(line);
				System.out.println(line);
				j += 1;
			}
		}
		
		dot.add("}");
		return dot;
	}
	

	/**
	 * This method increases the capacity of the Hash-Table and reorganizes it, in
	 * order to accommodate and access its entries more efficiently. This method is
	 * called automatically when the load factor exceeds 75%. To increase the size
	 * of the Hash-Table, you multiply the actual capacity by 10 and search for the
	 * closest primary number less than the result of this multiplication. For
	 * example if the actual capacity of the Hash-Table is 101, the capacity will be
	 * increased to 1009, which is the closest primary number less than (101*10).
	 */
	private void rehash() {
		
		System.out.println("REHASH");
		
		int newSize = this.capacity * 10;
		
		while(!isPrime(newSize)) {
			newSize -= 1;
		}
		
		System.out.println(newSize);
		
		int oldSize = this.capacity;
		Entry[] oldData = this.data;
		
		this.data = new Entry[newSize];
		this.capacity = newSize;
		
		for(int i = 0; i < oldSize; i++) {
			if(oldData[i] != null && !oldData[i].isDeleted()) {
				this.insert(oldData[i]);
			}
		}
	}
	
	private long decimalRepresentation(String value) {
		char[] key = value.toCharArray();
		String str = Integer.toString((int) key[0]) + Integer.toString((int) key[1]) + Integer.toString((int) key[2]) + Integer.toString((int) key[3]) + Integer.toString((int) key[4]);
		return Long.parseLong(str);
	}
	
	private int getHashAddr(Entry value) {
		return this.getHashAddr(value.getKey());
	}
	
	private int getHashAddr(String key) {
		if(this.hashFunction == 0)
			return this.divisionHash(key);
		else if(this.hashFunction == 1)
			return this.foldingHash(key);
		else if(this.hashFunction == 2)
			return this.midSquareHash(key);
		else
			return 0;
	}
	
	private int divisionHash(String value) {
		long decimal = decimalRepresentation(value);
		return Math.toIntExact(decimal % this.capacity);
	}
	
	private int midSquareHash(String value) {
		int addrLength = (int) Math.ceil(Math.log10(this.capacity));
		System.out.println("AddrLen: "+addrLength);
		BigInteger dec = BigInteger.valueOf(decimalRepresentation(value));
		BigInteger sqr = dec.pow(2);
		String str = sqr.toString();
		str = str.substring(str.length() - (9+addrLength), str.length() - 9);
		System.out.println(str);
		return Integer.parseInt(str) % this.capacity;
	}
	
	private int foldingHash(String value) {
		int addrLength = (int) Math.ceil(Math.log10(this.capacity));
		String str = Long.toString(decimalRepresentation(value));
		
		// Padd with zeroes to multiple of address length
		while(str.length() % addrLength != 0) {
			str = "0" + str;
		}
		
		int sum = 0;
		boolean reverse = true;
		String part;

		for(int i = 0; i < str.length(); i += addrLength) {
			part = str.substring(str.length() - (i + addrLength), str.length() - i);
			if(reverse)
				part = new StringBuilder(part).reverse().toString();
			sum += Integer.parseInt(part);
			reverse = !reverse;
		}
		
		String res = Integer.toString(sum);
		return Integer.parseInt(res.substring(res.length() - addrLength, res.length())) % this.capacity;
	}
	
	private int getProbingAddr(int base, int i) {
		if(this.probingMode == 0)
			return (base + i) % this.capacity;
		else {
			int nextTry = ((int)(base - Math.ceil(Math.pow(((float) i)/2, 2))*Math.pow(-1, i))) % this.capacity;
			return nextTry < 0 ? nextTry + this.capacity : nextTry;
		}
	}
	
	private String getInsertionSequence(String searchKey) {
		int address = this.getHashAddr(searchKey);
		int base = address;
		int i = 1;
		String sequence = "";
		while(this.data[address] != null && !this.data[address].getKey().equals(searchKey)) {
			sequence += Integer.toString(address) + ", ";
			address = this.getProbingAddr(base, i);
			i += 1;
		}
		
		return sequence.length() > 2 ? "|" + sequence.substring(0, sequence.length() - 2) : "";
	}
	
	private boolean isPrime(int p) {
		int i = 2;
		while(i <= Math.sqrt(p)) {
			if(p % i == 0)
				return false;
			i++;
		}
		return true;
	}
}
