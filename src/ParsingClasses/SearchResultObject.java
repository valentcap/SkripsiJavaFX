package ParsingClasses;

public class SearchResultObject {
    private String className;
    private String location;

    public SearchResultObject(String cN, String l){
        this.className = cN;
        this.location = l;
    }

    public String getClassName() {
        return className;
    }

    public String getLocation() {
        return location;
    }
}
