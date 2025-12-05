package legends;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks which catalog entries the player has discovered/unlocked.
 */
public final class CatalogState {
    private static final Set<String> unlocked = new HashSet<>();

    private CatalogState() {}

    public static void unlock(String name) {
        if (name == null) return;
        unlocked.add(name);
    }

    public static boolean isUnlocked(String name) {
        if (name == null) return false;
        return unlocked.contains(name);
    }

    public static List<String> getUnlockedList() {
        return Collections.unmodifiableList(new java.util.ArrayList<>(unlocked));
    }

    public static void setUnlockedList(List<String> list) {
        unlocked.clear();
        if (list == null) return;
        for (String s : list) if (s != null) unlocked.add(s);
    }
}
