package com.flyaway.xpget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpLevelsTest {
    /** Vanilla Player#getXpNeededForNextLevel. */
    private static int pointsToNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }

    @Test
    void curveMatchesVanillaLevelUps() {
        long total = 0;
        for (int level = 0; level <= 1000; level++) {
            assertEquals(total, XpLevels.pointsForLevel(level), "level " + level);
            total += pointsToNextLevel(level);
        }
    }

    @Test
    void levelForPointsStaysInsideTheReachedLevel() {
        assertEquals(0, XpLevels.levelForPoints(0));
        assertEquals(0, XpLevels.levelForPoints(6));
        assertEquals(1, XpLevels.levelForPoints(7));
        assertEquals(30, XpLevels.levelForPoints(1395));
        assertEquals(29, XpLevels.levelForPoints(1394));
    }

    @Test
    void levelRequiredCoversTheCost() {
        assertEquals(0, XpLevels.levelRequiredFor(0));
        assertEquals(1, XpLevels.levelRequiredFor(7));
        assertEquals(2, XpLevels.levelRequiredFor(8));
        // 64 bottles at 7 points: level 18 holds 441 points, level 19 holds 493.
        assertEquals(19, XpLevels.levelRequiredFor(448));
    }
}
