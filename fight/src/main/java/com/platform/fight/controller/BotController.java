package com.platform.fight.controller;

import com.platform.fight.pojo.Bot;
import com.platform.fight.service.BotServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class BotController {
    private BotServiceImpl botService;

    @GetMapping("/bot/getlist/")
    List<Bot> getList() {
        return botService.getList();
    }

    @PostMapping("/bot/remove/")
    Map<String, String> remove(@RequestParam Map<String, String> data) {
        return botService.remove(data);
    }

    @PostMapping("/bot/update/")
    Map<String, String> update(@RequestParam Map<String, String> data) {
        return botService.update(data);
    }

    @PostMapping("/bot/add/")
    Map<String, String> add(@RequestParam Map<String, String> data) {
        return botService.add(data);
    }
}
