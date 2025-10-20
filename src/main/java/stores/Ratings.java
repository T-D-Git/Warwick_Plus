package stores;

import java.time.LocalDateTime;
import interfaces.IRatings;
import structures.*;
import utils.CustomUtil;

public class Ratings implements IRatings {
    Stores stores;

    // Each unique rating is identified by (userID,movieID) => RatingRecord
    private CustomHashTable<Long, RatingRecord> ratingKeyTable;

    // Maps each movie ID => array of RatingRecord
    private CustomHashTable<Integer, RatingRecord[]> movieToRatings;

    // Maps each user ID => array of RatingRecord
    private CustomHashTable<Integer, RatingRecord[]> userToRatings;

    // Track actual counts of ratings per movie and user
    private CustomHashTable<Integer, Integer> movieToRatingsSize;
    private CustomHashTable<Integer, Integer> userToRatingsSize;


    // Tracks total number of ratings in the syste
    private int totalRatings;


    /**
     * Inner helper class that stores all the info about a single rating.
     */
    public static class RatingRecord {
        protected int userID, movieID;
        protected float rating;
        protected LocalDateTime timestamp;

        RatingRecord(int userID, int movieID, float rating, LocalDateTime timestamp) {
            this.userID = userID;
            this.movieID = movieID;
            this.rating = rating;
            this.timestamp = timestamp;
        }
    }


    /**
     * The constructor for the Ratings data store. This is where you should
     * initialise your data structures.
     * @param stores An object storing all the different key stores,
     *               including itself
     */
    public Ratings(Stores stores) {
        this.stores = stores;

        // Hash tables are initilised with large capacities as data set is known to be large
        ratingKeyTable = new CustomHashTable<>(100000);
        movieToRatings = new CustomHashTable<>(100000);
        userToRatings = new CustomHashTable<>(100000);
        movieToRatingsSize = new CustomHashTable<>(100000);
        userToRatingsSize = new CustomHashTable<>(100000);
        totalRatings = 0;
    }


    /**
     * Generates a unique long key from userID and movieID to keep lookups efficient.
     */
    private long makeKey(int userID, int movieID) {
        // Shift userID by 20 bits so that userID & movieID won't collide
        // Note: This depends on userIDs typically not exceeding ~1 million
        return ((long) userID << 20) ^ (movieID & 0xFFFFF);
    }


    /**
     * Helper method to append a RatingRecord to an existing array.
     * Returns the new array with the appended element.
     */
    private RatingRecord[] appendRatingRecord(RatingRecord[] existing, RatingRecord record, int currentSize) {
        
        // If the existing array is full, then create an new array and copy over all elements
        if (currentSize == existing.length) {
            RatingRecord[] newArr = new RatingRecord[Math.max(1, existing.length * 2)];
            System.arraycopy(existing, 0, newArr, 0, existing.length);
            existing = newArr;
        }

        // Add the record to the array
        existing[currentSize] = record;
        return existing;
    }




    /**
     * Adds a rating to the data structure. The rating is made unique by its user ID
     * and its movie ID
     * 
     * @param userID    The user ID
     * @param movieID   The movie ID
     * @param rating    The rating gave to the film by this user (between 0 and 5
     *                  inclusive)
     * @param timestamp The time at which the rating was made
     * @return TRUE if the data able to be added, FALSE otherwise
     */
    @Override
    public boolean add(int userid, int movieid, float rating, LocalDateTime timestamp) {
        // Step 1: Create a unique key for the (userID, movieID) pair
        long key = makeKey(userid, movieid);

        // Step 2: If a rating already exists for this user-movie pair, do not add again
        if (ratingKeyTable.containsKey(key)) return false;

        // Step 3: Construct a new RatingRecord to hold the rating details
        RatingRecord record = new RatingRecord(userid, movieid, rating, timestamp);

        // Step 4: Add the new rating to the primary ratingKeyTable for quick lookup
        ratingKeyTable.put(key, record);

        // Step 5: Update the movieToRatings mapping
        // Ensure the movie has an entry; if not, initialise an array of size 100 and set size to 0
        movieToRatings.putIfAbsent(movieid, new RatingRecord[100]);
        movieToRatingsSize.putIfAbsent(movieid, 0);

        // Retrieve the current size and array for this movie
        int movieSize = movieToRatingsSize.get(movieid);
        RatingRecord[] movieArr = movieToRatings.get(movieid);

        // Append the new rating to the array
        movieArr = appendRatingRecord(movieArr, record, movieSize);

        // Update the data structure with the new array and increment size
        movieToRatings.put(movieid, movieArr);
        movieToRatingsSize.put(movieid, movieSize + 1);

        // Step 6: Update the userToRatings mapping in the same way
        userToRatings.putIfAbsent(userid, new RatingRecord[100]);
        userToRatingsSize.putIfAbsent(userid, 0);

        int userSize = userToRatingsSize.get(userid);
        RatingRecord[] userArr = userToRatings.get(userid);
        userArr = appendRatingRecord(userArr, record, userSize);
        userToRatings.put(userid, userArr);
        userToRatingsSize.put(userid, userSize + 1);

        // Step 7: Increment the global rating count
        totalRatings++;

        // Successfully added the rating
        return true;
    }


    /**
     * Removes a given rating, using the user ID and the movie ID as the unique
     * identifier
     * 
     * @param userID  The user ID
     * @param movieID The movie ID
     * @return TRUE if the data was removed successfully, FALSE otherwise
     */
    @Override
    public boolean remove(int userid, int movieid) {
        long key = makeKey(userid, movieid);

        // 1) Check if rating record exists
        RatingRecord record = ratingKeyTable.get(key);
        if (record == null) return false; // Not found => can't remove

        // 2) Remove from ratingKeyTable
        ratingKeyTable.remove(key);
        totalRatings--;

        // Decrement sizes
        movieToRatingsSize.put(record.movieID, movieToRatingsSize.get(record.movieID) - 1);
        userToRatingsSize.put(record.userID, userToRatingsSize.get(record.userID) - 1);

        return true;
    }



    /**
     * Sets a rating for a given user ID and movie ID. Therefore, should the given
     * user have already rated the given movie, the new data should overwrite the
     * existing rating. However, if the given user has not already rated the given
     * movie, then this rating should be added to the data structure
     * 
     * @param userID    The user ID
     * @param movieID   The movie ID
     * @param rating    The new rating to be given to the film by this user (between
     *                  0 and 5 inclusive)
     * @param timestamp The time at which the new rating was made
     * @return TRUE if the data able to be added/updated, FALSE otherwise
     */
    @Override
    public boolean set(int userid, int movieid, float rating, LocalDateTime timestamp) {
        long key = makeKey(userid, movieid);
        RatingRecord existing = ratingKeyTable.get(key);
        if (existing != null) {
            // Overwrite rating/timestamp
            existing.rating = rating;
            existing.timestamp = timestamp;
            return true;
        }
        return add(userid, movieid, rating, timestamp);
    }


    /**
     * Get all the ratings for a given film
     * 
     * @param movieID The movie ID
     * @return An array of ratings. If there are no ratings or the film cannot be
     *         found in Ratings, then return an empty array
     */
    @Override
    public float[] getMovieRatings(int movieid) {
        // Retrieve the array of RatingRecords associated with the movie
        RatingRecord[] arr = movieToRatings.get(movieid);
        
        // Retrieve the actual number of ratings for the movie
        Integer size = movieToRatingsSize.get(movieid);

        // If either is null, return an empty result
        if (arr == null || size == null) return new float[0];

        // Copy the ratings from the array up to the current size
        float[] res = new float[size];
        for (int i = 0; i < size; i++) {
            res[i] = arr[i].rating;
        }

        return res;
    }


    /**
     * Get all the ratings for a given user
     * 
     * @param userID The user ID
     * @return An array of ratings. If there are no ratings or the user cannot be
     *         found in Ratings, then return an empty array
     */
    @Override
    public float[] getUserRatings(int userid) {
        // Retrieve the array of RatingRecords for the specified user
        RatingRecord[] arr = userToRatings.get(userid);

        // Get the number of valid ratings the user has submitted
        Integer size = userToRatingsSize.get(userid);

        // Return an empty array if the user has no ratings or does not exist
        if (arr == null || size == null) return new float[0];

        // Extract the rating values into a float array
        float[] res = new float[size];
        for (int i = 0; i < size; i++) {
            res[i] = arr[i].rating;
        }

        return res;
    }


    /**
     * Get the average rating for a given film
     * 
     * @param movieID The movie ID
     * @return Produces the average rating for a given film. 
     *         If the film cannot be found in Ratings, but does exist in the Movies store, return 0.0f. 
     *         If the film cannot be found in Ratings or Movies stores, return -1.0f.
     */
    @Override
    public float getMovieAverageRating(int movieid) {
        float[] ratings = getMovieRatings(movieid);
        if (ratings.length > 0) { // If ratings exist
            // Compute average of the stored ratings
            float sum = 0;
            for (float r : ratings) sum += r;
            return sum / ratings.length;
        }

        // No rating in Ratings => check if this movie is in Movies
        if (stores.getMovies().getTitle(movieid) != null) {
            // Exists in Movies but no rating => 0.0f
            return 0.0f;
        } else {
            // Not in Movies => -1.0f
            return -1.0f;
        }
    }


    /**
     * Get the average rating for a given user
     * 
     * @param userID The user ID
     * @return Produces the average rating for a given user. If the user cannot be
     *         found in Ratings, or there are no rating, return -1.0f
     */
    @Override
    public float getUserAverageRating(int userid) {
        float[] ratings = getUserRatings(userid);
        if (ratings.length > 0) { // If ratings exist
            // Compute average of the stored ratings
            float sum = 0;
            for (float r : ratings) sum += r;
            return sum / ratings.length;
        }
        return -1.0f;
    }

    /**
     * Gets the top N movies with the most ratings, in order from most to least
     * 
     * @param num The number of movies that should be returned
     * @return A sorted array of movie IDs with the most ratings. The array should be
     *         no larger than num. If there are less than num movies in the store,
     *         then the array should be the same length as the number of movies in Ratings
     */
    @Override
    public int[] getMostRatedMovies(int num) {
        // Gather all entries from movieToRatings
        int[][] movieCounts = new int[movieToRatings.size()][2]; 
        // [i][0] => movieID, [i][1] => ratingCount

        int index = 0;
        var tableArr = movieToRatings.getTable();
        
        // Loop through the table to build movieCounts
        for (int i = 0; i < tableArr.length; i++) {
            var entry = tableArr[i];
            if (entry != null && !entry.isDeleted()) {
                movieCounts[index][0] = entry.getKey();        // store movieID
                movieCounts[index][1] = movieToRatingsSize.get(entry.getKey()); // store ratingCount
                index++;
            }
        }

        // Trim down to the relevant portion (index elements)
        int[][] relevant = new int[index][2];
        for (int i = 0; i < index; i++) {
            relevant[i][0] = movieCounts[i][0];
            relevant[i][1] = movieCounts[i][1];
        }

        // Sort by rating count in descending order using our custom QuickSort
        CustomUtil.quickSort(relevant, new CustomUtil.CustomComparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                // sort by the second element descending
                return Integer.compare(b[1], a[1]);
            }
        });

        // Take the top 'num' movies after sorting
        int limit = Math.min(num, index);
        int[] result = new int[limit];
        for (int i = 0; i < limit; i++) {
            result[i] = relevant[i][0];  // the movieID
        }

        return result;
    }


    /**
     * Gets the top N users with the most ratings, in order from most to least
     * 
     * @param num The number of users that should be returned
     * @return A sorted array of user IDs with the most ratings. The array should be
     *         no larger than num. If there are less than num users in the store,
     *         then the array should be the same length as the number of users in Ratings
     */
    @Override
    public int[] getMostRatedUsers(int num) {
        // Build array => [userID, ratingCount]
        int[][] userCounts = new int[userToRatings.size()][2];

        int index = 0;
        var tableArr = userToRatings.getTable();

        // Loop through the table to populate userCounts
        for (int i = 0; i < tableArr.length; i++) {
            var entry = tableArr[i];
            if (entry != null && !entry.isDeleted()) {
                userCounts[index][0] = entry.getKey();         // userID
                userCounts[index][1] = userToRatingsSize.get(entry.getKey()); // ratingCount
                index++;
            }
        }

        // Trim any unused space down to 'index'
        int[][] relevant = new int[index][2];
        for (int i = 0; i < index; i++) {
            relevant[i][0] = userCounts[i][0];
            relevant[i][1] = userCounts[i][1];
        }

        // Sort descending by rating count using custom QuickSort
        CustomUtil.quickSort(relevant, new CustomUtil.CustomComparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                // Compare the second element (ratingCount) in descending order
                return Integer.compare(b[1], a[1]);
            }
        });

        // Take the top 'num' users after sorting
        int limit = Math.min(num, index);
        int[] result = new int[limit];
        for (int i = 0; i < limit; i++) {
            result[i] = relevant[i][0]; // userID
        }

        return result;
    }



    /**
     * Get the number of ratings that a movie has
     * 
     * @param movieid The movie id to be found
     * @return The number of ratings the specified movie has. 
     *         If the movie exists in the Movies store, but there are no ratings for it, then return 0. 
     *         If the movie does not exist in the Ratings or Movies store, then return -1.
     */
    @Override
    public int getNumRatings(int movieid) {
        Integer size = movieToRatingsSize.get(movieid);
        if (size != null && size > 0) return size;

        // No rating in Ratings for this movie, so check if the movie is in Movies
        if (stores.getMovies().getTitle(movieid) != null) {
            // Movie is known by Movies but has no rating => 0
            return 0;
        } else {
            // Movie not even in Movies => -1
            return -1;
        }
    }


    /**
     * Get the highest average rated film IDs, in order of there average rating
     * (hightst first).
     * 
     * @param numResults The maximum number of results to be returned
     * @return An array of the film IDs with the highest average ratings, highest
     *         first. If there are less than num movies in the store,
     *         then the array should be the same length as the number of movies in Ratings
     */
    @Override
    public int[] getTopAverageRatedMovies(int numResults) {
        // Get the underlying array representation of the movieToRatings hash table
        var tableArr = movieToRatings.getTable();

        // Keeps track of how many valid movies are processed
        int actualMovies = 0;

        // Create an array to hold [movieID, averageRating] pairs
        float[][] avgArr = new float[movieToRatings.size()][2];

        // Step 1: Build up the avgArr using getMovieAverageRating for each valid movieID
        for (int i = 0; i < tableArr.length; i++) {
            var entry = tableArr[i];
            if (entry != null && !entry.isDeleted()) {
                int movieID = entry.getKey();

                float avg = getMovieAverageRating(movieID);

                avgArr[actualMovies][0] = movieID;
                avgArr[actualMovies][1] = avg;
                actualMovies++;
            }
        }

        // Step 2: Trim avgArr down to only valid movies
        float[][] relevant = new float[actualMovies][2];
        System.arraycopy(avgArr, 0, relevant, 0, actualMovies);

        // Step 3: Sort the array in descending order of average rating
        CustomUtil.quickSort(relevant, new CustomUtil.CustomComparator<float[]>() {
            @Override
            public int compare(float[] a, float[] b) {
                return Float.compare(b[1], a[1]);  // sort descending by avgRating
            }
        });

        // Step 4: Create the result array with top movie IDs
        int limit = Math.min(numResults, actualMovies);
        int[] result = new int[limit];
        for (int i = 0; i < limit; i++) {
            result[i] = (int) relevant[i][0];  // movieID
        }

        return result;
    }


    /**
     * Gets the number of ratings in the data structure
     * 
     * @return The number of ratings in the data structure
     */
    @Override
    public int size() {
        return totalRatings;
    }

}
