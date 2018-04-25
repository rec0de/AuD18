package lab;

import frame.SortArray;

public class QuickSortB extends QuickSort {

	/**
	 * Quicksort algorithm implementation to sort a SorrtArray by choosing the
	 * pivot as the median of the elements at positions (left,middle,right)
	 * 
	 * @param records
	 *            - list of elements to be sorted as a SortArray
	 * @param left
	 *            - the index of the left bound for the algorithm
	 * @param right
	 *            - the index of the right bound for the algorithm
	 * @return Returns the sorted list as SortArray
	 */
	@Override
	public void Quicksort(SortArray records, int left, int right) {
		// implement the Quicksort B algorithm to sort the records
		// (choose the pivot as the median value of the elements at position
		// (left (first),middle,right(last)))
		if(right - left < 1)
			return;
		
		SortingItem leftValue = records.getElementAt(left);
		SortingItem rightValue = records.getElementAt(right);
		
		SortingItem pivot = getPivot(left, right, records, leftValue, rightValue);
		int p = partition(pivot, records, left, right, leftValue, rightValue);
		Quicksort(records, left, p);
		Quicksort(records, p+1, right);
	}
	
	private SortingItem getPivot(int left, int right, SortArray records, SortingItem l, SortingItem r) {
		int mIndex = (int) Math.floor(left + (right - left) / 2);
		
		SortingItem m = (mIndex == left) ? l : records.getElementAt(mIndex);
		SortingItem t = null;
		
		// 'Sort' the three elements by doing two swaps if necessary, then return the middle (= median) one
		
		if(l.compareTo(m) > 0) {
			t = l;
			l = m;
			m = t;
		}
		
		if(r.compareTo(m) < 0) {
			t = r;
			r = m;
			m = t;
		}
		
		return m;
	}

}
