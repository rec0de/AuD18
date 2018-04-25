package lab;

/**
 * 
 * This class represents one entry of the list that has to be sorted.
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

	@Override
	public int compareTo(SortingItem arg0) {
		return this.BookSerialNumber.compareTo(arg0.BookSerialNumber) == 0 ? this.ReaderID.compareTo(arg0.ReaderID): this.BookSerialNumber.compareTo(arg0.BookSerialNumber);
	}

}
