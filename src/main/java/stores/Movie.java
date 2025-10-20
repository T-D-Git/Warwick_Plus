package stores;

import java.time.LocalDate;
import structures.DynamicArray;


/**
 * Represents a Movie with all its associated metadata including title, release date,
 * languages, genres, production details, and rating statistics.
 * 
 * This class is used as a core data model to store and retrieve information
 * about individual films within the store.
 * 
 * It includes utility methods for retrieving individual fields, as well as 
 * setting or appending data relevant to voting, production companies, and more.
 */
public class Movie {
    private int id;
    private String title;
    private String originalTitle;
    private String overview;
    private String tagline;
    private String status;
    private Genre[] genres;
    private LocalDate release;
    private long budget;
    private long revenue;
    private String[] languages;
    private String originalLanguage;
    private double runtime;
    private String homepage;
    private boolean adult;
    private boolean video;
    private String poster;
    private double voteAverage;
    private int voteCount;
    private String imdbID;
    private double popularity;
    private DynamicArray<Company> productionCompanies;
    private DynamicArray<String> productionCountries;
    private int collectionID;

    /**
     * Constructs a new Movie object with the given properties.
     * 
     * @param id               The unique ID for the film
     * @param title            The English title of the film
     * @param originalTitle    The original language title of the film
     * @param overview         An overview of the film
     * @param tagline          The tagline for the film (empty string if there is no
     *                         tagline)
     * @param status           Current status of the film
     * @param genres           An array of Genre objects related to the film
     * @param release          The release date for the film
     * @param budget           The budget of the film in US Dollars
     * @param revenue          The revenue of the film in US Dollars
     * @param languages        An array of ISO 639 language codes for the film
     * @param originalLanguage An ISO 639 language code for the original language of
     *                         the film
     * @param runtime          The runtime of the film in minutes
     * @param homepage         The URL to the homepage of the film
     * @param adult            Whether the film is an adult film
     * @param video            Whether the film is a "direct-to-video" film
     * @param poster           The unique part of the URL of the poster (empty if
     *                         the URL is not known)
     */
    public Movie(int id, String title, String originalTitle, String overview, 
                 String tagline, String status, Genre[] genres, LocalDate release,
                 long budget, long revenue, String[] languages, String originalLanguage,
                 double runtime, String homepage, boolean adult, boolean video, String poster) {
        
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.tagline = tagline;
        this.status = status;
        this.genres = genres;
        this.release = release;
        this.budget = budget;
        this.revenue = revenue;
        this.languages = languages;
        this.originalLanguage = originalLanguage;
        this.runtime = runtime;
        this.homepage = homepage;
        this.adult = adult;
        this.video = video;
        this.poster = poster;
        this.voteAverage = 0.0;
        this.voteCount = 0;
        this.imdbID = "";
        this.popularity = 0.0;
        this.productionCompanies = new DynamicArray<>();
        this.productionCountries = new DynamicArray<>();
        this.collectionID = -1;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public String getTagline() {
        return tagline;
    }

    public String getStatus() {
        return status;
    }

    public Genre[] getGenres() {
        return genres;
    }

    public LocalDate getRelease() {
        return release;
    }

    public long getBudget() {
        return budget;
    }

    public long getRevenue() {
        return revenue;
    }

    public String[] getLanguages() {
        return languages;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public double getRuntime() {
        return runtime;
    }

    public String getHomepage() {
        return homepage;
    }

    public boolean getAdult() {
        return adult;
    }

    public boolean getVideo() {
        return video;
    }

    public String getPoster() {
        return poster;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public String getImdbID() {
        return imdbID;
    }

    public double getPopularity() {
        return popularity;
    }

    public DynamicArray<Company> getProductionCompanies() {
        return productionCompanies;
    }

    public DynamicArray<String> getProductionCountries() {
        return productionCountries;
    }

    public int getCollectionID() {
        return collectionID;
    }




    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public void setCollectionID(int collectionID) {
        this.collectionID = collectionID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }


    

    public void addProductionCompany(Company company) {
        this.productionCompanies.add(company);
    }

    public void addProductionCountry(String country) {
        this.productionCountries.add(country);
    }
}
