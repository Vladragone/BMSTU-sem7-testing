package com.example.game.dto;

public class LocationResponseDTO {
    private Long id;
    private Double lat;
    private Double lng;
    private Long groupId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
}
