package com.group5.htms.util;

public final class RankGroupUtil {
    private RankGroupUtil() {
    }

    public static String fromRankingPoints(Integer rankingPoints) {
        int points = rankingPoints == null ? 0 : rankingPoints;
        if (points >= 800) {
            return "A";
        }
        if (points >= 500) {
            return "B";
        }
        if (points >= 200) {
            return "C";
        }
        return "D";
    }
}