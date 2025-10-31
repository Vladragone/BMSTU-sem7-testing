package com.example.game.dto;

import com.example.game.model.LocationGroup;

public class LocationGroupRequestDTO {
    private String name;

    public LocationGroupRequestDTO() {}

    public LocationGroupRequestDTO(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocationGroup toEntity() {
        LocationGroup g = new LocationGroup();
        g.setName(this.name);
        return g;
    }
}
