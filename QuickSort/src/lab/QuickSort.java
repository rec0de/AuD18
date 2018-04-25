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

	/**
	 * Partitions a list with no pre-supplied values
	 * @param pivot Pivot element to compare against
	 * @param records The SortArray to work on
	 * @param leftBound Left bounding index of the subset to partition
	 * @param rightBound Right bounding index of the subset to partition
	 * @return Index of the split position
	 */
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound) {
		
		if(leftBound == rightBound)
			return leftBound;
		
		SortingItem leftValue = records.getElementAt(leftBound);
		SortingItem rightValue = records.getElementAt(rightBound);
		
		return partition(pivot, records, leftBound, rightBound, leftValue, rightValue);
	}
	
	/**
	 * Partitions a list with pre-supplied leftValue. Used by QuickSortA as pivot = leftValue is already known
	 * @param pivot Pivot element to compare against
	 * @param records The SortArray to work on
	 * @param leftBound Left bounding index of the subset to partition
	 * @param rightBound Right bounding index of the subset to partition
	 * @param leftValue SortingItem at position leftBound
	 * @return Index of the split position
	 */
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound, SortingItem leftValue) {
		
		if(leftBound == rightBound)
			return leftBound;

		SortingItem rightValue = records.getElementAt(rightBound);
		
		return partition(pivot, records, leftBound, rightBound, leftValue, rightValue);
	}
	
	/**
	 * Partitions a list with pre-supplied leftValue and rightValue. Used by QuickSortB as leftValue and rightValue are already known
	 * @param pivot Pivot element to compare against
	 * @param records The SortArray to work on
	 * @param leftBound Left bounding index of the subset to partition
	 * @param rightBound Right bounding index of the subset to partition
	 * @param leftValue SortingItem at position leftBound
	 * @param rightValue SortingItem at position rightBound
	 * @return Index of the split position
	 */
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound, SortingItem leftValue, SortingItem rightValue) {
		
		if(leftBound == rightBound)
			return leftBound;
		
		int leftIndex = leftBound;
		int rightIndex = rightBound;
		int pivotIndex = leftBound;
		SortingItem temp;
		
		while(true){
			while(leftIndex < rightBound && leftValue.compareTo(pivot) <= 0) {
				pivotIndex = leftValue.compareTo(pivot) == 0 ? leftIndex : pivotIndex; // 'Search for' the pivot index while traversing list to avoid passing the index as an argument
				leftIndex += 1;
				leftValue = records.getElementAt(leftIndex);
			}
			while(rightIndex > leftBound && rightValue.compareTo(pivot) >= 0) {
				pivotIndex = rightValue.compareTo(pivot) == 0 ? rightIndex : pivotIndex; // 'Search for' the pivot index while traversing list to avoid passing the index as an argument
				rightIndex -= 1;
				rightValue = records.getElementAt(rightIndex);
			}
			if(leftIndex >= rightIndex) {
				// Swap the pivot if necessary (values equal to pivot are not swapped while partitioning)
				if((pivotIndex > rightIndex && pivot.compareTo(rightValue) < 0) || (pivotIndex < rightIndex && pivot.compareTo(rightValue) > 0)) {
					records.setElementAt(pivotIndex, rightValue);
					records.setElementAt(rightIndex, pivot);
				}
				return rightIndex;
			}
			
			// Swap right and left elements and local copies
			records.setElementAt(leftIndex, rightValue);
			records.setElementAt(rightIndex, leftValue);
			temp = rightValue;
			rightValue = leftValue;
			leftValue = temp;
		}
	}
}
