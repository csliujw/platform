package com.platform.game.utils;

import com.platform.fight.pojo.Bot;

import java.util.Date;

// 判断是否是机器人，等一系列操作
public class MachinePlayerUtils {
    public static Bot judgeIsMachinePlayer(Integer bodId) {
        Bot machineBot = null;
        if (bodId < -1) {
            machineBot = new Bot(bodId, -1, "", "", easyBot, new Date(), new Date());
        }
        return machineBot;
    }


    private static final String easyBot = "package com.platform.bot.service;\n" +
            "\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "\n" +
            "class Cell {\n" +
            "    public int x, y;\n" +
            "\n" +
            "    public Cell(int x, int y) {\n" +
            "        this.x = x;\n" +
            "        this.y = y;\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "public class BotCode implements com.platform.bot.utils.BotCodeInterface {\n" +
            "    private boolean check_tail_increasing(int step) {  // 检验当前回合，蛇的长度是否增加\n" +
            "        if (step <= 10) return true;\n" +
            "        return step % 3 == 1;\n" +
            "    }\n" +
            "\n" +
            "    public List<Cell> getCells(int sx, int sy, String steps) {\n" +
            "        steps = steps.substring(1, steps.length() - 1);\n" +
            "        List<Cell> res = new ArrayList<>();\n" +
            "\n" +
            "        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};\n" +
            "        int x = sx, y = sy;\n" +
            "        int step = 0;\n" +
            "        res.add(new Cell(x, y));\n" +
            "        for (int i = 0; i < steps.length(); i ++ ) {\n" +
            "            int d = steps.charAt(i) - '0';\n" +
            "            x += dx[d];\n" +
            "            y += dy[d];\n" +
            "            res.add(new Cell(x, y));\n" +
            "            if (!check_tail_increasing( ++ step)) {\n" +
            "                res.remove(0);\n" +
            "            }\n" +
            "        }\n" +
            "        return res;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public Integer nextMove(String input) {\n" +
            "        String[] strs = input.split(\"#\");\n" +
            "        int[][] g = new int[13][14];\n" +
            "        for (int i = 0, k = 0; i < 13; i ++ ) {\n" +
            "            for (int j = 0; j < 14; j ++, k ++ ) {\n" +
            "                if (strs[0].charAt(k) == '1') {\n" +
            "                    g[i][j] = 1;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);\n" +
            "        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);\n" +
            "\n" +
            "        List<Cell> aCells = getCells(aSx, aSy, strs[3]);\n" +
            "        List<Cell> bCells = getCells(bSx, bSy, strs[6]);\n" +
            "\n" +
            "        for (Cell c: aCells) g[c.x][c.y] = 1;\n" +
            "        for (Cell c: bCells) g[c.x][c.y] = 1;\n" +
            "\n" +
            "        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};\n" +
            "        for (int i = 0; i < 4; i ++ ) {\n" +
            "            int x = aCells.get(aCells.size() - 1).x + dx[i];\n" +
            "            int y = aCells.get(aCells.size() - 1).y + dy[i];\n" +
            "            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {\n" +
            "                return i;\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        return 0;\n" +
            "    }\n" +
            "}";
}
