package structures;

import structures.*;

public class CustomHashTable<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double MAX_LOAD_FACTOR = 0.7;

    private Entry<K, V>[] table;
    private int size;

    @SuppressWarnings("unchecked")
    public CustomHashTable() {
        table = new Entry[DEFAULT_CAPACITY];
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public CustomHashTable(int initialCapacity) {
        table = new Entry[initialCapacity];
        size = 0;
    }

    public static class Entry<K, V> {
        protected K key;
        protected V value;
        protected boolean isDeleted; // Marks removed entries

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.isDeleted = false;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public boolean isDeleted() {
            return isDeleted;
        }        
    }

    /**
     * Primary hash function used to compute the initial index for a key.
     * 
     * @param key The key to hash
     * @return An index within the bounds of the hash table
     */
    private int hash1(K key) {
        return Math.abs(key.hashCode()) % table.length;
    }

    /**
     * Secondary hash function used for double hashing to compute step size.
     * 
     * @param key The key to hash
     * @return A step size used in double hashing
     */
    private int hash2(K key) {
        return 1 + (Math.abs(key.hashCode()) % (table.length - 2));
    }

    /**
     * Resizes the internal hash table when the load factor threshold is exceeded.
     */
    private void rehash() {
        // Save reference to the current table
        Entry<K, V>[] oldTable = table;

        // Allocate a new table with double the capacity
        table = new Entry[table.length * 2];
        size = 0;  // Reset size; it will be updated during re-insertion

        // Re-insert all valid (non-deleted) entries into the new table
        for (Entry<K, V> entry : oldTable) {
            if (entry != null && !entry.isDeleted) {
                put(entry.key, entry.value);  // Use put() to respect new hash indices
            }
        }
    }


    /**
     * Inserts the specified value with the given key
     * Does not overwrite existing entries.
     *
     * @param key   The key to insert.
     * @param value The value associated with the key.
     */
    public void put(K key, V value) {
    // Check if inserting a new entry exceeds the max load factor (0.5)
    if ((double) size / table.length >= MAX_LOAD_FACTOR) {
        rehash(); // Double table size and re-insert existing entries
    }

    // Calculate initial index position using first hash function
    int index = hash1(key);

    // Calculate step size using second hash function (for double hashing)
    int stepSize = hash2(key);

    // Keep track of the first deleted slot encountered during probing
    int firstDeletedIndex = -1;

    // Probe until an empty slot is found
    while (table[index] != null) {
        // Case 1: Slot is occupied but entry is marked as deleted
        if (table[index].isDeleted) {
            // Store this slot if a deleted slot hasn't yet been encountered
            if (firstDeletedIndex == -1) {
                firstDeletedIndex = index;
            }
        }
        // Case 2: Slot is occupied and keys match (updating existing key)
        else if (table[index].key.equals(key)) {
            table[index].value = value;  // Update the value for the existing key
            return; // Done inserting, exit method
        }

        // Move index forward by stepSize (computed from hash2) for next probe
        index = (index + stepSize) % table.length;
    }

    // If deleted slot found earlier, insert the new entry at that location
    if (firstDeletedIndex != -1) {
        table[firstDeletedIndex] = new Entry<>(key, value);
    }
    // Else, insert the new entry into the first empty slot found
    else {
        table[index] = new Entry<>(key, value);
    }

    // Increase the size (number of entries) after successful insertion
    size++;
    }


    /**
     * Inserts the specified key-value pair if the key isn't already present.
     *
     * @param key The key to insert.
     * @param value The value to associate with the key.
     * @return TRUE if insertion succeeded; FALSE if key already exists.
     */
    public boolean putIfAbsent(K key, V value) {
        // If the key already exists, no insertion is done.
        if (containsKey(key)) {
            return false;
        }

        // Otherwise, insert the new key-value pair.
        put(key, value);
        return true;
    }


    /**
     * Converts all non-deleted values in the hash table to an array of type V.
     *
     * @param array An array of type V with a length equal to the number of valid entries in the table.
     *              This array will be populated and returned.
     * @return An array containing all values currently stored in the hash table.
     */
    public V[] hashTableValuesToArray(V[] array) {
        int index = 0;

        // Iterate over hash table
        for (Entry<K, V> entry : table) {
            if (entry != null && !entry.isDeleted) {
                // Add each existing element to the array
                array[index++] = entry.value;
            }
        }
        return array;
    }



    /**
     * Retrieves the value associated with the specified key from the hash table.
     *
     * @param key The key whose associated value is to be returned
     * @return The value mapped to the key, or null if the key is not found or has been removed
     */
    public V get(K key) {
        // Compute the initial index using the primary hash function
        int index = hash1(key);

        // Compute the secondary hash (step size) for double hashing
        int stepSize = hash2(key);

        // Probe the table using double hashing until a null slot is encountered
        while (table[index] != null) {
            // If the key matches and is not marked as deleted, return the associated value
            if (!table[index].isDeleted && table[index].key.equals(key)) {
                return table[index].value;
            }

            // Move to the next index using the step size
            index = (index + stepSize) % table.length;
        }

        // Key was not found in the table
        return null;
    }

    /**
     * Removes the entry associated with the specified key from the hash table.
     *
     * @param key The key whose mapping is to be removed from the table
     * @return true if the entry was successfully removed; false if the key was not found
     */
    public boolean remove(K key) {
        // Compute the primary hash index
        int index = hash1(key);

        // Compute the secondary hash (step size) for probing
        int stepSize = hash2(key);

        // Probe until the find the key is found or an empty slot is found
        while (table[index] != null) {
            // If this slot matches the key and is not marked as deleted
            if (!table[index].isDeleted && table[index].key.equals(key)) {
                // Mark it as deleted (tombstone)
                table[index].isDeleted = true;
                size--; // Decrease active element count
                return true;
            }

            // Continue probing using double hashing
            index = (index + stepSize) % table.length;
        }

        // Key not found in the table
        return false;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return table.length;
    }

    public Entry<K, V>[] getTable() {
        return table;
    }

}
