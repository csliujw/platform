package com.platform.match.service.utils.strategy;

import com.platform.match.dto.Player;
import com.platform.match.service.utils.MatcherRule;
import org.springframework.stereotype.Component;

@Component("matcherByRating2")
public class MatcherByRating2 implements MatcherRule {
    @Override
    public boolean matcher(Player blue, Player red) {
        int ratingDelta = Math.abs(blue.getRating() - red.getRating());
        int waitingTime = Math.min(blue.getWaitingTime(), red.getWaitingTime());
        return ratingDelta <= waitingTime * 20;
    }
}
