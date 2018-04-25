package lab;

import frame.SortArray;

/**
 * Abstract superclass for the Quicksort algorithm.
 * 
 * @author NAJI
 */
public abstract class QuickSort {

	// DO NOT modify this method
	public abstract void Quicksort(SortArray records, int left, int right);

	// You may add additional methods here
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound) {
		
		if(leftBound == rightBound)
			return leftBound;
		
		int leftIndex = leftBound;
		int rightIndex = rightBound;
		
		while(true){
			while(leftIndex < rightBound && records.getElementAt(leftIndex).compareTo(pivot) < 0) {
				leftIndex += 1;
			}
			while(rightIndex > leftBound && records.getElementAt(rightIndex).compareTo(pivot) > 0) {
				rightIndex -= 1;
			}
			if(leftIndex >= rightIndex) {
				return rightIndex;
			}
			
			swap(leftIndex, rightIndex, records);
		}
	}
	
	protected void swap(int i1, int i2, SortArray records) {
		SortingItem temp = records.getElementAt(i1);
		records.setElementAt(i1, records.getElementAt(i2));
		records.setElementAt(i2, temp);
	}

}
