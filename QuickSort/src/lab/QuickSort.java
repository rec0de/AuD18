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
		
		SortingItem leftValue = records.getElementAt(leftBound);
		SortingItem rightValue = records.getElementAt(rightBound);
		
		return partition(pivot, records, leftBound, rightBound, leftValue, rightValue);
	}
	
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound, SortingItem leftValue) {
		
		if(leftBound == rightBound)
			return leftBound;

		SortingItem rightValue = records.getElementAt(rightBound);
		
		return partition(pivot, records, leftBound, rightBound, leftValue, rightValue);
	}
	
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound, SortingItem leftValue, SortingItem rightValue) {
		
		if(leftBound == rightBound)
			return leftBound;
		
		int leftIndex = leftBound;
		int rightIndex = rightBound;
		SortingItem temp;
		
		while(true){
			while(leftIndex < rightBound && leftValue.compareTo(pivot) < 0) {
				leftIndex += 1;
				leftValue = records.getElementAt(leftIndex);
			}
			while(rightIndex > leftBound && rightValue.compareTo(pivot) > 0) {
				rightIndex -= 1;
				rightValue = records.getElementAt(rightIndex);
			}
			if(leftIndex >= rightIndex) {
				return rightIndex;
			}
			
			records.setElementAt(leftIndex, rightValue);
			records.setElementAt(rightIndex, leftValue);
			temp = rightValue;
			rightValue = leftValue;
			leftValue = temp;
		}
	}
}
