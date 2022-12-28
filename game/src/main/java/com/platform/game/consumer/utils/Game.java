package com.platform.game.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.platform.fight.pojo.Bot;
import com.platform.fight.pojo.Record;
import com.platform.fight.pojo.User;
import com.platform.game.consumer.WebSocketServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

// 游戏类，负责地图的生成，游戏输赢的判断，地图信息的编码等
public class Game {
    final private Integer rows;
    final private Integer cols;
    final private Integer innerWallsCount;
    final private int[][] gameMap;
    final private static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    private final Player playerBlue, playerRed;
    private Integer nextStepBlue, nextStepRed;

    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing"; // playing finished
    private String loser = ""; // ALL 平局； Blue 输； Red 输
    private static final String addBotUrl = "http://127.0.0.1:8082/bot/add/";
    public MapUtil mapUtil = new MapUtil();


    public Game(Integer rows, Integer cols, Integer innerWallsCount, Integer blue, Bot blueBot, Integer red, Bot redBot) {
        this.rows = rows;
        this.cols = cols;
        this.innerWallsCount = innerWallsCount;
        this.gameMap = new int[rows][cols];
        int blueBotId = -1, redBotId = -1;
        String blueBotCode = null, redBotCode = null;
        if (blueBot != null) {
            blueBotId = blueBot.getId();
            blueBotCode = blueBot.getContent();
        }
        if (redBot != null) {
            redBotId = redBot.getId();
            redBotCode = redBot.getContent();
        }
        this.playerBlue = new Player(blue, rows - 2, 1, blueBotId, blueBotCode, new ArrayList<>());
        this.playerRed = new Player(red, 1, cols - 2, redBotId, redBotCode, new ArrayList<>());
    }

    public Player getPlayerBlue() {
        return playerBlue;
    }

    public Player getPlayerRed() {
        return playerRed;
    }

    public void setNextStepBlue(Integer nextStepBlue) {
        lock.lock(); // 此处的加锁感觉没有必要。两个人在同一个线程中执行，操作的是不同的变量，不存在线程争用。
        try {
            this.nextStepBlue = nextStepBlue;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepRed(Integer nextStepRed) {
        lock.lock();
        try {
            this.nextStepRed = nextStepRed;
        } finally {
            lock.unlock();
        }
    }

    public void run() {
        for (int i = 0; i < 500; i++) {
            if (nextStep()) {
                mapUtil.judge();
                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepBlue == null && nextStepRed == null) {
                        loser = "all";
                    } else if (nextStepBlue == null) {
                        loser = "Blue"; // Blue 没走，blue 输了
                    } else {
                        loser = "Red";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }

    public String getInput(Player player) {
        // 将当前的局面信息编码成字符串 操作序列是 0 1 2 3 的字符
        Player me, you;
        if (playerBlue.getId().equals(player.getId())) {
            me = playerBlue;
            you = playerRed;
        } else {
            me = playerRed;
            you = playerBlue;
        }
        // 地图#自己的起始坐标x#y#我的操作#对手的起始左边#x#y#对手的操作
        String ans = mapUtil.getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
        return ans;
    }

    // 等待两名玩家的下一步操作,都有下一步操作了，则将操作加入各自的 step 集合
    private boolean nextStep() {
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendBotCode(playerBlue);
        sendBotCode(playerRed);

        for (int i = 0; i < 50; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                lock.lock();
                if (nextStepBlue != null && nextStepRed != null) {
                    playerBlue.getSteps().add(nextStepBlue);
                    playerRed.getSteps().add(nextStepRed);
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    private void sendBotCode(Player player) {
        if (player.getBotId() == -1) return; // 人出馬，無需執行代碼
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", player.getId().toString());
        data.add("botCode", player.getBotCode());
        data.add("input", getInput(player)); // 获取玩家的下一步输入
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }

    private void sendAllMessage(String message) {
        // 如果查询不到该用户，或者用户为机器人（用户id小于等于-1的是机器人）
        if (WebSocketServer.users.get(playerBlue.getId()) != null && playerBlue.getId() >= -1) {
            WebSocketServer.users.get(playerBlue.getId()).sendMessage(message);
        }
        if (WebSocketServer.users.get(playerRed.getId()) != null && playerRed.getId() >= -1) {
            WebSocketServer.users.get(playerRed.getId()).sendMessage(message);
        }
    }

    private void sendMove() {
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("blue_direction", nextStepBlue);
            resp.put("red_direction", nextStepRed);
            sendAllMessage(resp.toJSONString());
            nextStepBlue = nextStepRed = null;
            // 发送移动指令。
        } finally {
            lock.unlock();
        }
    }

    private void sendResult() {  // 向两个Client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        System.out.println("send result1");
        saveToDatabase();
        System.out.println("send result2");

        // 将可能存在的机器人移除 users
        if (playerRed.getId() != null && playerRed.getId() < -1) {
            WebSocketServer.users.remove(playerRed.getId());
        }
        // 将可能存在的机器人移除 users
        if (playerBlue.getId() != null && playerBlue.getId() < -1) {
            WebSocketServer.users.remove(playerBlue.getId());
        }
        sendAllMessage(resp.toJSONString());
    }

    // 更新用户积分和对局总数
    private void updateUserRating(Player one, boolean win) {
        int rating = win ? 5 : -5;
        SqlRunner.db().update("update user set rating = rating + {0},count=count+1 where id = {1}", rating, one.getId());
    }

    private void updateRecord() {
        Record record = new Record(
                null,
                playerBlue.getId(),
                playerBlue.getSx(),
                playerBlue.getSy(),
                playerRed.getId(),
                playerRed.getSx(),
                playerRed.getSy(),
                playerBlue.getStepsString(),
                playerRed.getStepsString(),
                mapUtil.getMapString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void saveToDatabase() {
        updateUserRating(playerBlue, true);

        boolean blueWin = false;
        // 如果有一方是机器人，则只保存对局，不增加对战分数。
        if (playerBlue.getBotId() < -1 || playerRed.getBotId() < -1) {
            // 説明有一方是機器人，不更新天梯分数。只记录对局信息。
            updateRecord();
        } else {
            if ("Red".equals(loser)) {
                blueWin = true;
            }
            updateUserRating(playerBlue, blueWin);
            updateUserRating(playerRed, !blueWin);
            updateRecord();
        }
    }

    /**
     * 地图工具类，用于生成地图，判断地图情况等
     */
    public class MapUtil {
        // 判断输赢
        private void judge() {
            // 身体
            List<Cell> cellsBlue = playerBlue.getCells();
            List<Cell> cellsRed = playerRed.getCells();
            boolean validBlue = checkValid(cellsBlue, cellsRed);
            boolean validRed = checkValid(cellsRed, cellsBlue);
            // 都有效的话，状态还是 playing
            if (!validBlue || !validRed) {
                status = "finished";
                if (!validBlue && !validRed) {
                    loser = "all";
                } else if (!validBlue) { // validBlue 为 false，blue 走的无效，blue 输
                    // validBlue 为 false 则 红赢
                    loser = "Blue";
                } else {
                    loser = "Red";
                }
            }
        }

        public void createMap() {
            for (int i = 0; i < 1000; i++) {
                if (draw()) break;
            }
        }

        // dfs 检查地图的连通性
        private boolean checkConnectivity(int sx, int sy, int tx, int ty) {
            if (sx == tx && sy == ty) return true;
            gameMap[sx][sy] = 1;

            for (int i = 0; i < 4; i++) {
                int x = sx + dx[i], y = sy + dy[i];
                if (x >= 0 && x < Game.this.rows && y >= 0 && y < Game.this.cols && gameMap[x][y] == 0) {
                    if (checkConnectivity(x, y, tx, ty)) {
                        gameMap[sx][sy] = 0;
                        return true;
                    }
                }
            }

            gameMap[sx][sy] = 0;
            return false;
        }

        private boolean draw() {  // 画地图
            for (int i = 0; i < Game.this.rows; i++) {
                for (int j = 0; j < Game.this.cols; j++) {
                    gameMap[i][j] = 0;
                }
            }

            // 四周填充数字1（障碍物）
            for (int r = 0; r < Game.this.rows; r++) {
                gameMap[r][0] = gameMap[r][Game.this.cols - 1] = 1;
            }
            for (int c = 0; c < Game.this.cols; c++) {
                gameMap[0][c] = gameMap[Game.this.rows - 1][c] = 1;
            }

            Random random = new Random();
            for (int i = 0; i < Game.this.innerWallsCount / 2; i++) {
                // 随机生成障碍物，对称。
                for (int j = 0; j < 1000; j++) {
                    int r = random.nextInt(Game.this.rows);
                    int c = random.nextInt(Game.this.cols);

                    if (gameMap[r][c] == 1 || gameMap[Game.this.rows - 1 - r][Game.this.cols - 1 - c] == 1)
                        continue;
                    if (r == Game.this.rows - 2 && c == 1 || r == 1 && c == Game.this.cols - 2)
                        continue;

                    gameMap[r][c] = gameMap[Game.this.rows - 1 - r][Game.this.cols - 1 - c] = 1;
                    break;
                }
            }
            return checkConnectivity(Game.this.rows - 2, 1, 1, Game.this.cols - 2);
        }

        private String getMapString() {
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    res.append(gameMap[i][j]);
                }
            }
            return res.toString();
        }

        public int[][] getGameMap() {
            return gameMap;
        }

        private boolean checkValid(List<Cell> self, List<Cell> opponent) {
            int n = self.size();
            Cell cell = self.get(n - 1);
            // 判断下一步是否撞墙
            if (gameMap[cell.x][cell.y] == 1) {
                return false;//撞墙了
            }
            // 判断是否和自己之前的身体误碰
            for (int i = 0; i < n - 1; i++) {
                if (self.get(i).x == cell.x && self.get(i).y == cell.y) {
                    return false;
                }
            }
            // 判断是否和对手之前的身体误碰
            for (int i = 0; i < n - 1; i++) {
                if (opponent.get(i).x == cell.x && opponent.get(i).y == cell.y) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        UpdateWrapper<User> wrapper = new UpdateWrapper<User>();
        wrapper.eq("id", 1);
        wrapper.setSql("'rating' = 'rating'+1");
        System.out.println(wrapper.getSqlComment());

    }
}

