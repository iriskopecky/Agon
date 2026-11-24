package com.iriskopecky.agon.game.logic;

import com.iriskopecky.ActionPair;
import com.iriskopecky.Scan;
import com.iriskopecky.TerasView;

import java.util.ArrayList;

public class ViewWrapperUtils {

    /**
     * Generate a Scan for a teras.
     * @param teras The teras to generate the scan for.
     * @param board The current game board.
     * @return The teras's scan.
     */
    public static Scan buildScan(ServerTeras teras, GameBoard board) {
        final int MAX_RADIUS = Math.max(board.getHeight() / 2, board.getWidth() / 2);

        int size = teras.getCurrentViewDist() * 2 + 1;
        size = Math.min(MAX_RADIUS, size); //ensure we don't have a scan larger than the entire board, because that's inefficient and painful.
        Scan.BoardView[][] boardView = new Scan.BoardView[size][size];
        //for each position in the scan, generate the view
        for (int xAdjust = -1 * teras.getCurrentViewDist(), x = 0; xAdjust <= teras.getCurrentViewDist(); xAdjust++, x++) {
            int posX = teras.getPos().getX() + xAdjust;
            for (int yAdjust = -1 * teras.getCurrentViewDist(), y = 0; yAdjust <= teras.getCurrentViewDist(); yAdjust++, y++) {
                int posY = teras.getPos().getY() + yAdjust;
                GameBoard.Position pos = board.getGamePos(posX, posY);
                //make the boardView
                Scan.BoardView bView = new Scan.BoardView(pos.getPortents(),
                        bulkGenTerasViews(pos.getOccupyingTeras()), pos.getCoords().getX(), pos.getCoords().getY());
                boardView[x][y] = bView;
            }
        }

        return new Scan(boardView, teras.getPos().getX(), teras.getPos().getY(), teras.getCurrentViewDist());
    }

    /**
     * Convert a list of serverTeras into a form suitable for player view.
     * @param terata A list of serverterata to convert.
     * @return The list of views.
     */
    public static ArrayList<TerasView> bulkGenTerasViews(ArrayList<ServerTeras> terata) {
        ArrayList<TerasView> output = new ArrayList<>();

        for (ServerTeras teras : terata) {
            output.add(genTerasView(teras));
        }

        return output;
    }

    public static TerasView genTerasView(ServerTeras teras) {
        return new TerasView(teras.getIdentifier());
    }

    /**
     * Check whether the requested actions are permissible for the given teras
     * @param actions The actions requested.
     * @param teras The requesting teras.
     * @return True if the actions should be permitted, false otherwise.
     */
    public static boolean validateActionPair(ActionPair actions, ServerTeras teras, GameBoard board) {
        if (actions == null || actions.primary() == null || actions.secondary() == null
                || (actions.primary() == ActionPair.PrimaryAction.REPLACE_WITH_SECONDARY && actions.primarySubstitute() == null)) {
            return false;
        }

        //ensure melee is within range.
        if (actions.primary() == ActionPair.PrimaryAction.MELEE) {
            GameBoard.Position currentPos = board.getGamePos(teras.getPos().getX(), teras.getPos().getY());
            GameBoard.Position targetPos = board.getGamePos(actions.targetX(), actions.targetY());
            int range = currentPos.getComponentDistance(targetPos, board.getWidth(), board.getHeight());
            if (range > teras.getCurrentMeleeRange()) {
                return false;
            }
        }

        return true;
    }
}
