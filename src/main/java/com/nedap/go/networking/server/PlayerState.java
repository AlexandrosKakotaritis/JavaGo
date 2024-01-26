package com.nedap.go.networking.server;

/**
 * A player can be in 4 distinct states. Useful for ignoring
 * incoming messages not appropriate for each state
 */
public enum PlayerState {
    FRESH, PREGAME, IN_QUEUE, IN_GAME
}
