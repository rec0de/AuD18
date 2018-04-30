package lab;

import frame.SortArray;

public class QuickSortB extends QuickSort {

	/**
	 * Quicksort algorithm implementation to sort a SorrtArray by choosing the
	 * pivot as the median of the elements at positions (left,middle,right)
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
		
		// Get the left and right bounding values (for median calculation)
		SortingItem leftValue = records.getElementAt(left);
		SortingItem rightValue = records.getElementAt(right);
		
		// Calculate the median pivot of left, right and middle
		SortingItem pivot = getPivot(left, right, records, leftValue, rightValue);
		
		// Pass the already looked-up left and right values to partition to save two reads
		int p = partition(pivot, records, left, right, leftValue, rightValue);
		
		// Sort the two sub-lists recursively (p being the position of the partition)
		Quicksort(records, left, p);
		Quicksort(records, p+1, right);
	}
	
	/**
	 * Method to get the median pivot out of the leftmost, rightmost and middle element of an array
	 * @param left Leftmost index
	 * @param right Rightmost index
	 * @param records The Array to work on
	 * @param l The value of the array at the left index
	 * @param r The value of the array at the right index
	 * @return The value of the chosen median pivot
	 */
	private SortingItem getPivot(int left, int right, SortArray records, SortingItem l, SortingItem r) {
		// Calculate the index of the middle element (rounded down)
		int mIndex = (int) Math.floor(left + (right - left) / 2);
		
		// If the array has only two items, the left item is the middle item so we don't have to look it up again
		SortingItem m = (mIndex == left) ? l : records.getElementAt(mIndex);
		
		SortingItem t = null;
		
		// The median element is the middle element of the sorted three element list of left, middle and right
		// 'Sort' the three elements by doing two swaps if necessary, then return the middle one
		if(l.compareTo(m) > 0) { // (if l is larger than m, l and m need to be swapped)
			t = l;
			l = m;
			m = t;
		}
		if(r.compareTo(m) < 0) { // (if r is smaller than the new m, r and m need to be swapped)
			t = r;
			r = m;
			m = t;
		}
		
		return m; // return the new m
	}

}
