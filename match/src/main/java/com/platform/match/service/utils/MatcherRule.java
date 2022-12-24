package com.platform.match.service.utils;

import com.platform.match.dto.Player;

public interface MatcherRule {
    public abstract boolean matcher(Player blue, Player red);
}
