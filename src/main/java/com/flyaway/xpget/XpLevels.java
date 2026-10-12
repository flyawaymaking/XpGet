package com.flyaway.xpget;

/**
 * Vanilla level curve, see https://minecraft.wiki/w/Experience#Leveling_up
 */
final class XpLevels {
    private XpLevels() {
    }

    /** Experience points needed to reach {@code level} from zero. */
    static long pointsForLevel(int level) {
        long l = level;
        if (level <= 16) {
            return l * l + 6 * l;
        }
        if (level <= 31) {
            return (5 * l * l - 81 * l + 720) / 2;
        }
        return (9 * l * l - 325 * l + 4440) / 2;
    }

    /** Level reached with {@code points} experience points. */
    static int levelForPoints(int points) {
        int level = 0;
        while (pointsForLevel(level + 1) <= points) {
            level++;
        }
        return level;
    }

    /** Lowest level whose experience covers {@code points}. */
    static int levelRequiredFor(int points) {
        int level = 0;
        while (pointsForLevel(level) < points) {
            level++;
        }
        return level;
    }
}
