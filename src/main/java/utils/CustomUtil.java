package utils;

/**
 * A utility class containing a generic QuickSort algorithm that can sort
 * any type of object array using a custom comparator.
 */
public class CustomUtil {

    /**
     * A minimal custom comparator interface that objects can use
     * to determine order without using Java's built-in Comparator.
     *
     * @param <T> The type of objects that will be compared
     */
    public interface CustomComparator<T> {
        /**
         * Compares two objects for order.
         *
         * @param a The first object
         * @param b The second object
         * @return A negative value if 'a' is smaller than 'b';
         *         zero if they are equal; a positive value if 'a' is greater than 'b'.
         */
        int compare(T a, T b);
    }

    /**
     * Sorts the entire array using QuickSort, according to the given comparator.
     *
     * @param <T>  The array type
     * @param arr  The array to sort in-place
     * @param comp The custom comparator used to compare two elements
     */
    public static <T> void quickSort(T[] arr, CustomComparator<T> comp) {
        quickSort(arr, 0, arr.length - 1, comp);
    }

    /**
     * An internal recursive QuickSort method that sorts the sub-array arr[left..right].
     *
     * @param arr   The array to sort
     * @param left  The leftmost index of the sub-array to be sorted
     * @param right The rightmost index of the sub-array to be sorted
     * @param comp  The custom comparator used to compare elements
     */
    private static <T> void quickSort(T[] arr, int left, int right, CustomComparator<T> comp) {
        if (left < right) {
            // Partition the array around a pivot, select as arr[right]
            int pivotIndex = partition(arr, left, right, comp);

            // Recursively sort the left part (before the pivot)
            quickSort(arr, left, pivotIndex - 1, comp);
            // Recursively sort the right part (after the pivot)
            quickSort(arr, pivotIndex + 1, right, comp);
        }
    }

    /**
     * Partitions the sub-array arr[left..right] around a pivot (chosen as arr[right]).
     * Elements smaller than or equal to the pivot are moved to the left side;
     * elements greater are moved to the right side.
     *
     * @param arr   The array
     * @param left  The left index of the sub-array
     * @param right The right index of the sub-array
     * @param comp  The custom comparator for comparisons
     * @return The final index of the pivot after partitioning
     */
    private static <T> int partition(T[] arr, int left, int right, CustomComparator<T> comp) {
        // The pivot is the rightmost element
        T pivot = arr[right];

        // i will track the boundary of elements <= pivot
        int i = left - 1;

        // Move elements into correct positions relative to pivot
        for (int j = left; j < right; j++) {
            // If arr[j] <= pivot, move it into the left side
            if (comp.compare(arr[j], pivot) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }

        // Place the pivot right after the last <= pivot element
        swap(arr, i + 1, right);
        return i + 1;
    }

    /**
     * Swaps the elements at indices i and j in the array.
     *
     * @param arr The array where the swap occurs
     * @param i   The first index
     * @param j   The second index
     */
    private static <T> void swap(T[] arr, int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
