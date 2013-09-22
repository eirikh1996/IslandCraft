package com.github.hoqhuuep.islandcraft.common.chat;

import com.github.hoqhuuep.islandcraft.common.api.ICPlayer;

/**
 * @author Daniel Simmons
 * @see <a
 *      href="https://github.com/hoqhuuep/IslandCraft/wiki/Chat#private-message">IslandCraft
 *      wiki</a>
 */
public final class PrivateMessage {
    /**
     * To be called when a player tries to send a private message.
     *
     * @param from
     * @param to
     * @param message
     */
    public static void onPrivateMessage(final ICPlayer from, final ICPlayer to, final String message) {
        if (null == to) {
            from.message("m-error");
            return;
        }
        to.message("m", from.getName(), to.getName(), message);
    }

    private PrivateMessage() {
        // Utility class
    }
}
