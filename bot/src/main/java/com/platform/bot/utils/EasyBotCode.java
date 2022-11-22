package com.platform.bot.utils;


import java.util.ArrayList;
import java.util.List;

class Cell {
    public int x, y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

public class EasyBotCode implements com.platform.bot.utils.BotCodeInterface {
    // 检查当前回合，蛇的长度是否会增加
    private boolean check_tail_increasing(int step) {
        if (step <= 7) return true;
        return step % 3 == 1;
    }

    public List<Cell> getCells(int sx, int sy, String steps) {
        steps = steps.substring(1, steps.length() - 1);
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i++) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing(++step)) {
                res.remove(0);
            }
        }
        return res;
    }

    @Override
    public Integer nextMove(String input) {
        // 地图#my.sx#my.sy#(my操作)#you.sx#you.sy#(you操作)
        String[] strs = input.split("#");
        int[][] gameMap = new int[13][14];
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (strs[0].charAt(k) == '1') {
                    gameMap[i][j] = 1;
                }
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);

        for (Cell c : aCells) gameMap[c.x][c.y] = 1;
        for (Cell c : bCells) gameMap[c.x][c.y] = 1;

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        for (int i = 0; i < 4; i++) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            // 预判三步
            if (checkedThree(x, y, gameMap) || checkedTwo(x, y, gameMap) || checkedOne(x, y, gameMap)) return i;
        }
        return 0;
    }

    public boolean checkedOne(int x, int y, int[][] gameMap) {
        if (x >= 0 && x < 13 && y >= 0 && y < 14 && gameMap[x][y] == 0) {
            return true;
        }
        return false;
    }

    public boolean checkedTwo(int x, int y, int[][] gameMap) {
        if (checkedOne(x, y, gameMap) &&
                (
                        checkedOne(x + 1, y, gameMap) ||
                                checkedOne(x - 1, y, gameMap) ||
                                checkedOne(x, y + 1, gameMap) ||
                                checkedOne(x, y - 1, gameMap)
                )) {
            return true;
        }
        return false;
    }

    public boolean checkedThree(int x, int y, int[][] gameMap) {
        if (checkedTwo(x, y, gameMap) &&
                (
                        checkedTwo(x + 1, y, gameMap) ||
                                checkedTwo(x - 1, y, gameMap) ||
                                checkedTwo(x, y + 1, gameMap) ||
                                checkedTwo(x, y - 1, gameMap)
                )) {
            return true;
        }
        return false;
    }

}
