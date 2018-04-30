package lab;

import frame.SortArray;

public class QuickSortA extends QuickSort {

	/**
	 * Quicksort algorithm implementation to sort a SorrtArray by choosing the
	 * pivot as the first (leftmost) element in the list
	 * 
	 * @param records List of elements to be sorted as a SortArray
	 * @param left The index of the left bound for the algorithm
	 * @param right The index of the right bound for the algorithm
	 * @return Returns the sorted list as SortArray
	 */
	@Override
	public void Quicksort(SortArray records, int left, int right) {
		// Lists of length 1 or smaller are trivially sorted
		if(right - left < 1)
			return;
		
		// Use the leftmost element as the pivot
		SortingItem pivot = records.getElementAt(left);
		// Pass the value of the pivot as the leftValue to partition to save an array read
		int p = partition(pivot, records, left, right, pivot);
		
		// Sort the two sub-lists recursively (p being the position of the partition)
		Quicksort(records, left, p);
		Quicksort(records, p+1, right);
	}

}
