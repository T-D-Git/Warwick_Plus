package stores;

import structures.*;

import interfaces.ICredits;

import utils.CustomUtil;

public class Credits implements ICredits{
    Stores stores;

    // HashTables for storing credits data
    private CustomHashTable<Integer, CastCredit[]> castTable;
    private CustomHashTable<Integer, CrewCredit[]> crewTable;

    // Reverse lookups (from cast/crew to films they participated in)
    private CustomHashTable<Integer, int[]> castToFilms;
    private CustomHashTable<Integer, int[]> crewToFilms;

    // Store unique cast/crew persons
    private CustomHashTable<Integer, Person> uniqueCast;
    private CustomHashTable<Integer, Person> uniqueCrew;

    // Number of films stored
    private int filmCount;

    /**
     * The constructor for the Credits data store. This is where you should
     * initialise your data structures.
     * 
     * @param stores An object storing all the different key stores, 
     *               including itself
     */
    public Credits (Stores stores) {
        this.stores = stores;

 
        // Initialise tables with sensible initial capacities
        this.castTable = new CustomHashTable<>(100000);   // larger initial size for films
        this.crewTable = new CustomHashTable<>(100000);

        this.castToFilms = new CustomHashTable<>(200000); // cast members to films
        this.crewToFilms = new CustomHashTable<>(200000);

        this.uniqueCast = new CustomHashTable<>(50000); // unique cast persons
        this.uniqueCrew = new CustomHashTable<>(50000); // unique crew persons

        this.filmCount = 0; // No films added yet
    }

    /**
     * Adds data about the people who worked on a given film. The movie ID should be
     * unique
     * 
     * @param cast An array of all cast members that starred in the given film
     * @param crew An array of all crew members that worked on a given film
     * @param id   The (unique) movie ID
     * @return TRUE if the data able to be added, FALSE otherwise
     */
    @Override
    public boolean add(CastCredit[] cast, CrewCredit[] crew, int id) {
        // Check if the credits for this movie already exist.
        if (castTable.containsKey(id) || crewTable.containsKey(id)) {
            return false;  // Avoid duplicate movie entries.
        }

        // Store cast and crew arrays directly
        castTable.put(id, cast);
        crewTable.put(id, crew);

        // Link cast members with their films.
        for (CastCredit c : cast) {

            // Ensure each unique cast member is stored exactly once.
            uniqueCast.putIfAbsent(c.getID(), new Person(c.getID(), c.getName(), c.getProfilePath()));

            // Create initial empty array if not already present.
            castToFilms.putIfAbsent(c.getID(), new int[0]);

            int[] filmList = castToFilms.get(c.getID());

            // Check if this film ID is already present
            boolean alreadyAdded = false;
            for (int film : filmList) {
                if (film == id) {
                    alreadyAdded = true;
                    break;
                }
            }

            // Only append if not already present
            if (!alreadyAdded) {
                castToFilms.put(c.getID(), appendFilmID(filmList, id));
            }
        }

        // Link crew members with their films.
        for (CrewCredit c : crew) {

            // Ensure each unique crew member is stored exactly once.
            uniqueCrew.putIfAbsent(c.getID(), new Person(c.getID(), c.getName(), c.getProfilePath()));

            // Create initial empty array if not already present.
            crewToFilms.putIfAbsent(c.getID(), new int[0]);


            int[] filmList = crewToFilms.get(c.getID());

            boolean alreadyAdded = false;
            for (int film : filmList) {
                if (film == id) {
                    alreadyAdded = true;
                    break;
                }
            }

            if (!alreadyAdded) {
                crewToFilms.put(c.getID(), appendFilmID(filmList, id));
            }

        }

        // Successfully increment film count.
        filmCount++;
        return true;
    }


    /**
     * Helper method to efficiently append a film ID to an existing int array.
     * Avoids unnecessary overhead from dynamic structures.
     *
     * @param existing The existing array of film IDs.
     * @param filmID   The new film ID to append.
     * @return A new array containing all previous IDs plus the new ID.
     */
    private int[] appendFilmID(int[] existing, int filmID) {
        int[] newArray = new int[existing.length + 1];
        System.arraycopy(existing, 0, newArray, 0, existing.length);
        newArray[existing.length] = filmID;
        return newArray;
    }

    /**
     * Remove a given films data from the data structure
     * 
     * @param id The movie ID
     * @return TRUE if the data was removed, FALSE otherwise
     */
    @Override
    public boolean remove(int id) {
        boolean removed = castTable.remove(id) && crewTable.remove(id);
        if (removed) filmCount--;
        return removed;
    }

    /**
     * Gets all the cast members for a given film
     * 
     * @param filmID The movie ID
     * @return An array of CastCredit objects, one for each member of cast that is 
     *         in the given film. The cast members should be in "order" order. If
     *         there is no cast members attached to a film, or the film cannot be 
     *         found in Credits, then return an empty array
     */
    @Override
    public CastCredit[] getFilmCast(int filmID) {
        CastCredit[] cast = castTable.get(filmID);
        if (cast == null) return new CastCredit[0]; // Return empty array if no cast found

        // Use CustomUtil's quickSort to sort cast by 'order' ascending
        CustomUtil.quickSort(cast, new CustomUtil.CustomComparator<CastCredit>() {
            @Override
            public int compare(CastCredit a, CastCredit b) {
                return Integer.compare(a.getOrder(), b.getOrder());
            }
        });

        return cast;
    }

    /**
     * Gets all the crew members for a given film
     * 
     * @param filmID The movie ID
     * @return An array of CrewCredit objects, one for each member of crew that is
     *         in the given film. The crew members should be in "id" order (not "elementID"). If there 
     *         is no crew members attached to a film, or the film cannot be found in Credits, 
     *         then return an empty array
     */
    @Override
    public CrewCredit[] getFilmCrew(int filmID) {
        CrewCredit[] crew = crewTable.get(filmID);
        if (crew == null) return new CrewCredit[0]; // Return empty array if no crew found

        // Use CustomUtil's quickSort to sort crew by ID ascending
        CustomUtil.quickSort(crew, new CustomUtil.CustomComparator<CrewCredit>() {
            @Override
            public int compare(CrewCredit a, CrewCredit b) {
                // Sort ascending by ID
                return Integer.compare(a.getID(), b.getID());
            }
        });

        return crew;
    }

    /**
     * Gets the number of cast that worked on a given film
     * 
     * @param filmID The movie ID
     * @return The number of cast member that worked on a given film. If the film
     *         cannot be found in Credits, then return -1
     */
    @Override
    public int sizeOfCast(int filmID) {
        CastCredit[] cast = castTable.get(filmID);
        if (cast == null) return -1; // Indicates film not found
        return cast.length;
    }

    /**
     * Gets the number of crew that worked on a given film
     * 
     * @param filmID The movie ID
     * @return The number of crew member that worked on a given film. If the film
     *         cannot be found in Credits, then return -1
     */
    @Override
    public int sizeOfCrew(int filmID) {
        CrewCredit[] crew = crewTable.get(filmID);
        if (crew == null) return -1; // Indicates film not found
        return crew.length;
    }

    /**
     * Gets a list of all unique cast members present in the data structure
     * 
     * @return An array of all unique cast members as Person objects. If there are 
     *         no cast members, then return an empty array
     */
    @Override
    public Person[] getUniqueCast() {
        // If the table is empty, return an empty array
        if (uniqueCast.size() == 0) {
            return new Person[0];
        }

        // Convert all values in the hash table to an array
        return uniqueCast.hashTableValuesToArray(new Person[uniqueCast.size()]);
    }

    /**
     * Gets a list of all unique crew members present in the data structure
     * 
     * @return An array of all unique crew members as Person objects. If there are
     *         no crew members, then return an empty array
     */
    @Override
    public Person[] getUniqueCrew() {
        // If the table is empty, return an empty array
        if (uniqueCrew.size() == 0) {
            return new Person[0];
        }

        // Convert all values in the hash table to an array
        return uniqueCrew.hashTableValuesToArray(new Person[uniqueCrew.size()]);
    }

    /**
     * Get all the cast members that have the given string within their name
     * 
     * @param castName The string that needs to be found
     * @return An array of unique Person objects of all cast members that have the 
     *         requested string in their name. If there are no matches, return an 
     *         empty array
     */
    @Override
    public Person[] findCast(String castName) {
        // Convert the input to lowercase to ensure case-insensitive matching
        castName = castName.toLowerCase();

        // Temporary array to store matching Person objects
        Person[] temp = new Person[uniqueCast.size()];
        int count = 0;

        // Iterate over the internal table of unique cast members
        for (var entry : uniqueCast.getTable()) {
            // Check for valid entry and whether the name contains the search string
            if (entry != null && !entry.isDeleted() && entry.getValue().getName().toLowerCase().contains(castName)) {
                temp[count++] = entry.getValue(); // Add match to result array
            }
        }

        // Return a trimmed array containing only the matched entries
        return java.util.Arrays.copyOf(temp, count);
    }

    /**
     * Get all the crew members that have the given string within their name
     * 
     * @param crewName The string that needs to be found
     * @return An array of unique Person objects of all crew members that have the 
     *         requested string in their name. If there are no matches, return an 
     *         empty array
     */
    @Override
    public Person[] findCrew(String crewName) {
        // Convert the search string to lowercase for case-insensitive matching
        crewName = crewName.toLowerCase();

        // Temporary array to collect matching Person objects
        Person[] temp = new Person[uniqueCrew.size()];
        int count = 0;

        // Iterate over the internal table of unique crew members
        for (var entry : uniqueCrew.getTable()) {
            // Check if entry is valid and name contains the search string
            if (entry != null && !entry.isDeleted() && entry.getValue().getName().toLowerCase().contains(crewName)) {
                temp[count++] = entry.getValue(); // Add match to the result array
            }
        }

        // Return a trimmed array with only matched results
        return java.util.Arrays.copyOf(temp, count);
    }

    /**
     * Gets the Person object corresponding to the cast ID
     * 
     * @param castID The cast ID of the person to be found
     * @return The Person object corresponding to the cast ID provided. 
     *         If a person cannot be found, then return null
     */
    @Override
    public Person getCast(int castID) {
        return uniqueCast.get(castID);
    }

    /**
     * Gets the Person object corresponding to the crew ID
     * 
     * @param crewID The crew ID of the person to be found
     * @return The Person object corresponding to the crew ID provided. 
     *         If a person cannot be found, then return null
     */
    @Override
    public Person getCrew(int crewID){
        return uniqueCrew.get(crewID);
    }

    
    /**
     * Get an array of film IDs where the cast member has starred in
     * 
     * @param castID The cast ID of the person
     * @return An array of all the films the member of cast has starred
     *         in. If there are no films attached to the cast member, 
     *         then return an empty array
     */
    @Override
    public int[] getCastFilms(int castID) {
        int[] films = castToFilms.get(castID);
        if (films == null) return new int[0];
        return films;
    }

    /**
     * Get an array of film IDs where the crew member has starred in
     * 
     * @param crewID The crew ID of the person
     * @return An array of all the films the member of crew has starred
     *         in. If there are no films attached to the crew member, 
     *         then return an empty array
     */
    @Override
    public int[] getCrewFilms(int crewID) {
        int[] films = crewToFilms.get(crewID);
        if (films == null) return new int[0];
        return films;
    }

    /**
     * Get the films that this cast member stars in (in the top 3 cast
     * members/top 3 billing). This is determined by the order field in
     * the CastCredit class
     * 
     * @param castID The cast ID of the cast member to be searched for
     * @return An array of film IDs where the the cast member stars in.
     *         If there are no films where the cast member has starred in,
     *         or the cast member does not exist, return an empty array
     */
    @Override
    public int[] getCastStarsInFilms(int castID) {
        // Get the list of film IDs the cast member has been involved in
        int[] films = castToFilms.get(castID);
        if (films == null) return new int[0];

        // Temporary array to hold film IDs that meet the criteria
        int[] temp = new int[films.length];
        int count = 0;

        // Go through each film the cast member has worked on
        for (int filmID : films) {
            CastCredit[] castCredits = castTable.get(filmID);
            if (castCredits == null) continue;

            // Check all credits for this film and this cast member
            boolean hasTopBilling = false;

            for (CastCredit credit : castCredits) {
                // If it's the right cast member and they have top 3 billing (order < 3)
                if (credit.getID() == castID && credit.getOrder() <= 3) {
                    hasTopBilling = true;
                    break; // One match is enough — no need to keep checking
                }
            }

            // If they had top billing in this film, include the film ID in result
            if (hasTopBilling) {
                temp[count++] = filmID;
            }
        }

        // Return a trimmed array of only the valid film IDs
        return java.util.Arrays.copyOf(temp, count);
    }
    
    /**
     * Get Person objects for cast members who have appeared in the most
     * films. If the cast member has multiple roles within the film, then
     * they would get a credit per role played. For example, if a cast
     * member performed as 2 roles in the same film, then this would count
     * as 2 credits. The list should be ordered by the highest to lowest number of credits.
     * 
     * @param numResults The maximum number of elements that should be returned
     * @return An array of Person objects corresponding to the cast members
     *         with the most credits, ordered by the highest number of credits.
     *         If there are less cast members that the number required, then the
     *         list should be the same number of cast members found.
     */
    @Override
    public Person[] getMostCastCredits(int numResults) {
        int totalCast = uniqueCast.size();
        if (totalCast == 0) return new Person[0];

        // Create an array to hold [castID, creditCount]
        int[][] castCredits = new int[totalCast][2];
        int count = 0;

        // Populate the array up to 'count'
        for (var entry : uniqueCast.getTable()) {
            if (entry != null && !entry.isDeleted()) {
                int castID = entry.getKey();
                int credits = getNumCastCredits(castID);

                castCredits[count][0] = castID;
                castCredits[count][1] = credits;
                count++;
            }
        }

        // Create a smaller subarray of exactly 'count' elements
        // so we don't sort unused indices.
        int[][] relevant = new int[count][2];
        for (int i = 0; i < count; i++) {
            relevant[i][0] = castCredits[i][0];
            relevant[i][1] = castCredits[i][1];
        }

        // Use the CustomUtil's quickSort to sort relevant in-place
        // Define a CustomComparator<int[]> that sorts by [1] descending.
        CustomUtil.quickSort(relevant, new CustomUtil.CustomComparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                // [1] is the creditCount
                // Sort descending, so b[1] - a[1] or use Integer.compare(b[1], a[1])
                return Integer.compare(b[1], a[1]);
            }
        });

        // Now relevant is sorted descending by the second column
        int resultCount = Math.min(numResults, count);
        Person[] result = new Person[resultCount];

        // Fill the top 'resultCount' elements
        for (int i = 0; i < resultCount; i++) {
            int castID = relevant[i][0];
            result[i] = uniqueCast.get(castID);
        }

        return result;
    }

    /**
     * Get the number of credits for a given cast member. If the cast member has
     * multiple roles within the film, then they would get a credit per role
     * played. For example, if a cast member performed as 2 roles in the same film,
     * then this would count as 2 credits.
     * 
     * @param castID A cast ID representing the cast member to be found
     * @return The number of credits the given cast member has. If the cast member
     *         cannot be found, return -1
     */
    @Override
    public int getNumCastCredits(int castID) {
        // If the cast member isn't registered, return -1
        if (uniqueCast.get(castID) == null) return -1;

        int[] films = castToFilms.get(castID);
        if (films == null) return 0; // No films linked to this cast ID

        int totalCredits = 0;

        // Go through each film they've worked in
        for (int filmID : films) {
            CastCredit[] credits = castTable.get(filmID);
            if (credits != null) {
                for (CastCredit credit : credits) {
                    // Count every role the person played (same castID)
                    if (credit.getID() == castID) {
                        totalCredits++;
                    }
                }
            }
        }

        return totalCredits;
    }

    /**
     * Gets the number of films stored in this data structure
     * 
     * @return The number of films in the data structure
     */
    @Override
    public int size() {
        return filmCount;
    }
}
