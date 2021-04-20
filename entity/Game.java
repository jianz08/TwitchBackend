package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

//Game.Builder builder = new Game.Builder();
//builder.setId("123");
//builder.setName("Vincent");

//Game game = builder.build();
//Game game = new Game(builder);如果提供public constructor,这个也可以。但一般只需提供一种用法给user，所以constructor设为private
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = Game.Builder.class)
public class Game{
    @JsonProperty("id")
    private final String id;
    @JsonProperty("name")
    private final String name;
    @JsonProperty("box_art_url")
    private final String boxArtUrl;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    private Game(Builder builder) {//private constructor?
        this.id = builder.id;
        this.name = builder.name;
        this.boxArtUrl = builder.boxArtUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("id")
        private String id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("box_art_url")
        private String boxArtUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBoxArtUrl(String boxArtUrl) {
            this.boxArtUrl = boxArtUrl;
            return this;
        }
        public Game build() {
            return new Game(this);
        }
    }

}
