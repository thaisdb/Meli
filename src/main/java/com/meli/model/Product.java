package com.meli.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Product {
    private int id;
    private String title;
    private double price;
    private String description;
    private String imageUrl;
    private String brand;
    private int stock;
    @JsonDeserialize(using = TagsDeserializer.class)
    private HashSet<String> tags;

    public Product(int id, String title, double price, String description, String imageUrl, String brand, int stock, HashSet<String> tags) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.stock = stock;
        this.tags = tags != null ? tags : new HashSet<>();
    }
    /*
     * Get unique Id used to identify products
     * @return Id
     */
    public int getId() { return id; }

    /*
     * Set unique Id used to identify products
     */
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public HashSet<String> getTags() { return tags; }
    public void setTags(HashSet<String> tags) { this.tags = tags != null ? tags : new HashSet<>(); }


    // --- Custom Deserializer Class (Refined) ---
    public static class TagsDeserializer extends JsonDeserializer<Set<String>> {
        @Override
        public Set<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken currentToken = p.getCurrentToken();

            if (currentToken == JsonToken.VALUE_STRING) {
                String tagsString = p.getText();
                if (tagsString == null || tagsString.trim().isEmpty()) {
                    return new HashSet<>();
                }
                return Arrays.stream(tagsString.split(";"))
                             .map(String::trim)
                             .filter(tag -> !tag.isEmpty())
                             .collect(Collectors.toCollection(HashSet::new));
            } else if (currentToken == JsonToken.START_ARRAY) {
                // Read array elements directly, allowing for ["tag1", "tag2"] or []
                Set<String> tags = new HashSet<>();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                        tags.add(p.getText().trim());
                    } else {
                        // Handle non-string elements in array if necessary, or skip
                        System.err.println("TagsDeserializer: Unexpected non-string element in tags array: " + p.getText());
                    }
                }
                return tags;
            } else if (currentToken == JsonToken.VALUE_NULL) {
                return new HashSet<>();
            }

            System.err.println("TagsDeserializer: Unexpected JSON token type for tags field: " + currentToken + ". Returning empty set.");
            return new HashSet<>();
        }
    }

    // --- Custom Serializer Class (Remains the same) ---
    public static class TagsSerializer extends JsonSerializer<Set<String>> {
        @Override
        public void serialize(Set<String> tags, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (tags == null || tags.isEmpty()) {
                gen.writeString("");
            } else {
                String tagsString = String.join(" ", tags);
                gen.writeString(tagsString);
            }
        }
    }

}
