package model;
public abstract class LibraryItem {

    private final String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;
    private String shelfLocation;

    protected LibraryItem(String isbn,String title,String author,int totalCopies,int availableCopies,String shelfLocation){
        if (isbn == null || isbn.isBlank())
            throw new IllegalArgumentException("isbn required");
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title required");
        if (author == null || author.isBlank())
            throw new IllegalArgumentException("author required");
        if (totalCopies < 0)
            throw new IllegalArgumentException("totalCopies cannot be negative");
        if (availableCopies < 0 || availableCopies > totalCopies)
            throw new IllegalArgumentException(
                "availableCopies must be between 0 and totalCopies");
        
        this.isbn=isbn.trim();
        this.title=title.trim();
        this.author=author.trim();
        this.totalCopies=totalCopies;
        this.availableCopies=availableCopies;
        setShelfLocation(isbn);
    }

    public String getIsbn(){
        return isbn;
    }
    public String getTitle(){
        return title;
    }
    public String getAuthor(){
        return author;
    }
    public int getTotalCopies(){
        return totalCopies;
    }
    public int getAvailableCopies(){
        return availableCopies;
    }
    public void setShelfLocation(String isbn){
        this.shelfLocation= "floor-1/aisle-"+(Math.abs(isbn.hashCode()%10+1));
    }
    public String getShelfLocation(){
        return shelfLocation;
    }
    public boolean hasAvailableCopy(){
        return availableCopies>0;
    }

    public void checkOutCopies(){
        if (availableCopies==0){
            throw new IllegalStateException("no copies available");
        }
        availableCopies--;
    }

    public void checkInCopies(){
        if (availableCopies>=totalCopies){
            throw new IllegalStateException("All copies already in library");
        }
        availableCopies++;
    }
    
   
    public abstract boolean isIssuable();
    public int getLoanPeriod(int userLoanDays){
        return userLoanDays;

    };
    public abstract String getType();
    public int hashCode() { return isbn.hashCode(); }

}