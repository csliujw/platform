package com.platform.bot.utils;

public class Main {
    // 该工具类为将远程发送过来的 bot 代码执行。
    static DynamicCompiler dynamicCompiler = new DynamicCompiler();

    /*
    *
    * */
    public static void main(String[] args) throws Exception {
        String sonCode = "package com.platform.bot.utils;\n" +
                "\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "\n" +
                "class Cell {\n" +
                "        public int x, y;\n" +
                "\n" +
                "        public Cell(int x, int y) {\n" +
                "            this.x = x;\n" +
                "            this.y = y;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "public class BotCode implements com.platform.bot.utils.BotCodeInterface {\n" +
                "\n" +
                "    private boolean check_tail_increasing(int step) {  // 检验当前回合，蛇的长度是否增加\n" +
                "        if (step <= 10) return true;\n" +
                "        return step % 3 == 1;\n" +
                "    }\n" +
                "\n" +
                "    public List<Cell> getCells(int sx, int sy) {\n" +
                "        int steps = 10;\n" +
                "        check_tail_increasing(++steps);\n" +
                "        Cell cell = new Cell(1,2);\n" +
                "        ArrayList<Cell> list = new ArrayList<Cell>();\n" +
                "        list.add(cell);\n" +
                "        return list;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public Integer nextMove(String input) {\n" +
                "\n" +
                "        List<Cell> aCells = getCells(0, 0);\n" +
                "        List<Cell> bCells = getCells(0, 0);\n" +
                "\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        Class<?> sonClass = dynamicCompiler.compileToClass("com.platform.bot.utils.BotCode", sonCode);
        BotCodeInterface o = (BotCodeInterface) sonClass.getDeclaredConstructor().newInstance();
        Integer integer = o.nextMove("1");
        sonClass = null;
        o = null;
        System.out.println(integer);
        System.in.read();
        System.gc();
        System.in.read();
        System.gc();
        System.in.read();
    }
}