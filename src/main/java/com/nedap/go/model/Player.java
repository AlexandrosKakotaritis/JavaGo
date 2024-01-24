package com.nedap.go.model;

/**
 * A player of a Go game. The interface contains only the getStone method. If an object represents a
 * player for a Go game, it should implement this interface.
 */
public interface Player {

  Stone getStone();
}
