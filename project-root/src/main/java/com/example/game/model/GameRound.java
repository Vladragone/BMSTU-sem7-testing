package com.example.game.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_round")
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "guess_lat")
    private Double guessLat;

    @Column(name = "guess_lng")
    private Double guessLng;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    public GameRound() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameSession getSession() {
        return session;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getGuessLat() {
        return guessLat;
    }

    public void setGuessLat(Double guessLat) {
        this.guessLat = guessLat;
    }

    public Double getGuessLng() {
        return guessLng;
    }

    public void setGuessLng(Double guessLng) {
        this.guessLng = guessLng;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }
}
