package com.platform.match.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Player {
    private Integer userId;
    private Integer rating;
    private Integer waitingTime;  // 等待时间
    private Integer botId;
}
