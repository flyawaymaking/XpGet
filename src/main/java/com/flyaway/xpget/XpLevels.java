package com.flyaway.xpget;

/**
 * Vanilla level curve, see <a href="https://minecraft.wiki/w/Experience#Leveling_up">...</a>
 */
final class XpLevels {
    static long pointsForLevel(int level) {
        if (level <= 16) {
            return (long) level * (long) level + 6 * (long) level;
        }
        if (level <= 31) {
            return (5 * (long) level * (long) level - 81 * (long) level + 720) / 2;
        }
        return (9 * (long) level * (long) level - 325 * (long) level + 4440) / 2;
    }

    static int levelForPoints(int points) {
        int level = 0;
        while (pointsForLevel(level + 1) <= points) {
            level++;
        }
        return level;
    }

    static int levelRequiredFor(int points) {
        int level = 0;
        while (pointsForLevel(level) < points) {
            level++;
        }
        return level;
    }
}
