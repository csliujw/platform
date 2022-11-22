package com.platform.match.utils;

import com.platform.match.dto.Player;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// 如果是人机对战，则生成一个机器人。
public class GenerateMachinePlayer {
    private static Random random = new Random();

    public static Player generateMachinePlayer() {
        int id = Math.abs(ThreadLocalRandom.current().nextInt(10000000));
        Player machine = new Player(-id, -100, 0, -id);
        return machine;
    }
}
