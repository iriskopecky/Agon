package com.iriskopecky;

import java.util.ArrayList;

public record Scan(
        BoardView[][] scanMap,
        int playerX, //the position of the player character in scanMap
        int playerY, //the position of the player character in scanMap
        int scanDist //the view distance used to generate this scan
) {
    public record BoardView(
            int portents,
            ArrayList<TerasView> presentTerata,
            int posX,
            int posY
    ) {

    }
}
