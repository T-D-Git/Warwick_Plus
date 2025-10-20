package structures;

@SuppressWarnings("unchecked")
public class DynamicArray<T> {
    private static final int DEFAULT_CAPACITY = 10;

    private T[] array;
    private int size;

    /**
     * Constructs a DynamicArray with the default capacity.
     */
    public DynamicArray() {
        array = (T[]) new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    /**
     * Constructs a DynamicArray with a user-defined initial capacity.
     *
     * @param initialCapacity The desired initial capacity of the array
     */
    public DynamicArray(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than zero.");
        }
        array = (T[]) new Object[initialCapacity];
        size = 0;
    }

    public void add(T item) {
        if (size == array.length) {
            resize();
        }
        array[size++] = item;
    }

    private void resize() {
        T[] newArray = (T[]) new Object[array.length * 2];
        for (int i = 0; i < size; i++) {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return array[index];
    }

    public int size() {
        return size;
    }

    /**
     * Converts the DynamicArray to a regular array of the correct type.
     *
     * @param target Array of the target type to copy elements into (should be length 0)
     * @return Array containing all elements
     */
    public T[] toArray(T[] providedArray)
    {
        if (providedArray.length < size) {
            // Create a new array of type T and length size
            providedArray = (T[]) java.lang.reflect.Array.newInstance(
                providedArray.getClass().getComponentType(), size);
        }
        for (int i = 0; i < size; i++) {
            providedArray[i] = array[i];
        }
        return providedArray;
    }

}
