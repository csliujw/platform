package com.platform.fight.controller;

import com.alibaba.fastjson.JSONObject;
import com.platform.fight.service.interfaces.IRankListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranklist")
public class RankListController {

    @Autowired
    private IRankListService rankListService;

    @GetMapping("/list/{page}")
    public JSONObject getList(@PathVariable("page") Integer page) {
        return rankListService.selectAll(page);
    }
}
