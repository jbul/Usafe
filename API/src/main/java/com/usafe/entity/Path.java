package com.usafe.entity;

import org.locationtech.jts.geom.Coordinate;

import javax.persistence.*;

@Entity
public class Path {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Path nextPath;
    private Coordinate coordinate;

    public Path() {}

    public Path(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Path getNextPath() {
        return nextPath;
    }

    public void setNextPath(Path nextPath) {
        this.nextPath = nextPath;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
