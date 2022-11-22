package com.platform.fight.controller;


import com.alibaba.fastjson.JSONObject;
import com.platform.fight.service.interfaces.IGetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private IGetRecordListService getRecordListService;

    @GetMapping("/get/list/{page}")
    JSONObject getList(@PathVariable("page") Integer page) {
        return getRecordListService.getList(page);
    }
}
