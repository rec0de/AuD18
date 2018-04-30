package lab;

import frame.SortArray;

/**
 * Abstract superclass for the Quicksort algorithm.
 * 
 * Note: I got into a small optimization competition with a fellow student, trying
 * to get read and write ops to an absolute minimum. Therefore, there is a fair bit of
 * passing local variables between functions to save a few read/write ops.
 * I do believe this to be well within the rules of the assignment as values are not
 * cached in an array-like structure or passed between recursion levels.
 * All optimizations merely cut down on duplicate / unnecessary reads and writes.
 * 
 * @author NAJI
 * @author Nils Rollshausen
 */
public abstract class QuickSort {

	// DO NOT modify this method
	public abstract void Quicksort(SortArray records, int left, int right);

	/**
	 * Partitions an interval of the SortArray so that elements of the left part are <= pivot
	 * and elements on the right are >= pivot. This variant does not use any pre-cached values for array bounds
	 * @param pivot The value of the pivot element
	 * @param records The SortArray to partition
	 * @param leftBound Left bound index of the interval to partition
	 * @param rightBound Right bound index of the interval to partition
	 * @return index of the partition
	 */
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound) {
		
		if(leftBound == rightBound)
			return leftBound;
		
		// Lookup bound values
		SortingItem leftValue = records.getElementAt(leftBound);
		SortingItem rightValue = records.getElementAt(rightBound);
		
		// Use actual implementation with pre-cached bound values
		return partition(pivot, records, leftBound, rightBound, leftValue, rightValue);
	}
	
	/**
	 * Partitions an interval of the SortArray so that elements of the left part are <= pivot
	 * and elements on the right are >= pivot. As the value of the leftmost array element is
	 * equal to the pivot value when using said element as the pivot, we can avoid reading that
	 * value from the array _again_ by passing it as an additional parameter
	 * @param pivot The value of the pivot element
	 * @param records The SortArray to partition
	 * @param leftBound Left bound index of the interval to partition
	 * @param rightBound Right bound index of the interval to partition
	 * @param leftValue Value of the element at index leftBound
	 * @return index of the partition
	 */
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound, SortingItem leftValue) {
		
		if(leftBound == rightBound)
			return leftBound;

		// Look up the right bound value
		SortingItem rightValue = records.getElementAt(rightBound);
		
		// Use actual implementation with pre-cached bounds
		return partition(pivot, records, leftBound, rightBound, leftValue, rightValue);
	}
	
	/**
	 * Partitions an interval of the SortArray so that elements of the left part are <= pivot
	 * and elements on the right are >= pivot. As the value of the bounding array elements is
	 * already known in some usecases, we can avoid reading these values from the array again
	 * by passing them as additional parameters
	 * @param pivot The value of the pivot element
	 * @param records The SortArray to partition
	 * @param leftBound Left bound index of the interval to partition
	 * @param rightBound Right bound index of the interval to partition
	 * @param leftValue Value of the element at index leftBound
	 * @param rightValue Value of the element at index rightBound
	 * @return index of the partition
	 */
	protected int partition(SortingItem pivot, SortArray records, int leftBound, int rightBound, SortingItem leftValue, SortingItem rightValue) {
		
		int leftIndex = leftBound;
		int rightIndex = rightBound;
		int pivotIndex = leftBound;
		SortingItem temp;
		
		/*
		 * Using the Hoare partitioning scheme here as opposed to the one presented in the lecture
		 * for approximately three times less swap operations compared to the Lomuto scheme
		 * See https://en.wikipedia.org/wiki/Quicksort#Hoare_partition_scheme for details
		 * Essentially, increment / decrement left / right indices until a pair of values that are
		 * the wrong part of the list is found, then swap that pair and continue
		 */
		while(true){
			// As long as the items at left index are smaller or equal than the pivot, they are on the right side
			while(leftIndex < rightBound && leftValue.compareTo(pivot) <= 0) {
				pivotIndex = leftValue.compareTo(pivot) == 0 ? leftIndex : pivotIndex; // 'Search for' the pivot index while traversing list to avoid passing the index as an argument
				leftIndex += 1;
				leftValue = records.getElementAt(leftIndex);
			}
			// Same for items at rightIndex larger or equal to than the pivot
			while(rightIndex > leftBound && rightValue.compareTo(pivot) >= 0) {
				pivotIndex = rightValue.compareTo(pivot) == 0 ? rightIndex : pivotIndex; // 'Search for' the pivot index while traversing list to avoid passing the index as an argument
				rightIndex -= 1;
				rightValue = records.getElementAt(rightIndex);
			}
			// As soon as both indices cross, no more swaps have to be made and the partition is complete
			if(leftIndex >= rightIndex) {
				// Swap the pivot if necessary (values equal to pivot are not swapped while partitioning)
				if((pivotIndex > rightIndex && pivot.compareTo(rightValue) < 0) || (pivotIndex < rightIndex && pivot.compareTo(rightValue) > 0)) {
					records.setElementAt(pivotIndex, rightValue);
					records.setElementAt(rightIndex, pivot);
				}
				return rightIndex;
			}
			
			// If the indices are not equal (or crossed over), swap the two items that are in the wrong spot
			records.setElementAt(leftIndex, rightValue);
			records.setElementAt(rightIndex, leftValue);
			// Also swap the local copies
			temp = rightValue;
			rightValue = leftValue;
			leftValue = temp;
		}
	}
}
