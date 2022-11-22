package com.platform.fight.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;
    private String photo;
    private Integer rating;
    private Integer count;

    public User(String username, Integer rating, Integer count, String photo) {
        this.username = username;
        this.rating = rating;
        this.count = count;
        this.photo = photo;
    }
}

