package lab;

/**
 * 
 * This class represents one entry of the list that has to be sorted.
 * 
 * Added Comparable interface to be able to, well, compare items
 * 
 */
public class SortingItem implements Comparable<SortingItem>{

	// DO NOT modify
	public String BookSerialNumber;
	public String ReaderID;
	public String Status;

	// DO NOT modify
	public SortingItem() {

	}

	// DO NOT modify
	public SortingItem(SortingItem otherItem) {
		this.BookSerialNumber = otherItem.BookSerialNumber;
		this.ReaderID = otherItem.ReaderID;
		this.Status = otherItem.Status;
	}

	// Compares self to another SortingItem, returns 0 if equal value, -1 if self is smaller, 1 if self is larger
	@Override
	public int compareTo(SortingItem arg0) {
		// If the BookSerialNumbers are equal, we return the result of the ReaderID comparison, otherwise comparing BookSerialNumbers is sufficient
		// (string comparisons are lexicographical)
		return this.BookSerialNumber.compareTo(arg0.BookSerialNumber) == 0 ? this.ReaderID.compareTo(arg0.ReaderID): this.BookSerialNumber.compareTo(arg0.BookSerialNumber);
	}

}
