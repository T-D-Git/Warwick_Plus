package stores;

import structures.DynamicArray;

/**
 * Represents a movie collection, which groups related films under a single entity.
 * Stores metadata about the collection as well as a dynamic list of film IDs.
 */
public class Collection {
    private int id;                           // Unique identifier for the collection
    private String name;                      // Name of the collection
    private String posterPath;                // Path to the collection's poster image
    private String backdropPath;              // Path to the collection's backdrop image
    private DynamicArray<Integer> filmIDs;    // Dynamic array storing the IDs of films in this collection


    /**
     * Constructs a Collection object with the specified metadata.
     * 
     * @param id            Unique collection ID
     * @param name          Name of the collection
     * @param posterPath    Path to the poster image
     * @param backdropPath  Path to the backdrop image
     */
    public Collection(int id, String name, String posterPath, String backdropPath) {
        this.id = id;
        this.name = name;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.filmIDs = new DynamicArray<>(); // Initialises an empty dynamic array for film IDs
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public DynamicArray<Integer> getFilmIDs() {
        return filmIDs;
    }

    public void addFilmID(int filmID) {
        this.filmIDs.add(filmID);
    }
}
