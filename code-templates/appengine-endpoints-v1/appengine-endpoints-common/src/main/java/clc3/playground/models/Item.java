package clc3.playground.models;

import java.util.ArrayList;
import java.util.List;

public class Item {
    public static final String NAME = "name";
    public static final String AVAILABLE = "available";
    public static final String PUBLICURL = "publicurl";
    public static final String TAGS = "tags";

    private String id;
    private String name;
    private Boolean available;
    private String publicUrl;
    private List<String> tags;


    public Item(){
        // initialize with empty values
        this("", "", false, "",new ArrayList<>());
    }

    public Item(String id) {
        this(id, "", false, "", new ArrayList<>());
    }

    public Item(String id, String name, boolean available, String publicUrl, List<String> tags) {
        this.id = id;
        this.name = name;
        this.available = available;
        this.publicUrl = publicUrl;
        this.tags = tags;

        if(tags == null){
            throw new IllegalArgumentException("Tags must not be null");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag){
        if(!tags.contains(tag)){
            tags.add(tag);
        }
    }
}
