package com.platform.bot.utils;

public interface BotCodeInterface {
    static class Cell {
        public int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    Integer nextMove(String input);
}
