package com.example.game.dto;

public class GameErrorResponseDTO {
    private Long id;
    private String name;

    public GameErrorResponseDTO() {}

    public GameErrorResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
