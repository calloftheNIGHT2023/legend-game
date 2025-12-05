package legends;

import java.util.List;

import legends.io.IO;
import legends.ItemLibrary.Category;

/**
 * Simple in-game catalog (图鉴) UI. Can be opened from the map to browse
 * weapons, armors, accessories, and consumables with background descriptions.
 */
public class Catalog {

    public static void open(legends.party.Party party, IO io) {
        while (true) {
            io.println("--- Catalog ---");
            io.println("1) Weapons");
            io.println("2) Armors");
            io.println("3) Accessories");
            io.println("4) Consumables");
            io.println("5) Close");
            io.print("Choice: ");
            String line = io.readLine();
            io.clear();
            if (line == null) return;
            switch (line) {
                case "1": showCategory(Category.WEAPON, io); break;
                case "2": showCategory(Category.ARMOR, io); break;
                case "3": showCategory(Category.ACCESSORY, io); break;
                case "4": showCategory(Category.CONSUMABLE, io); break;
                case "5": return;
                default: break;
            }
        }
    }

    private static void showCategory(Category c, IO io) {
        List<ItemLibrary.Entry> list = ItemLibrary.entriesFor(c);
        if (list.isEmpty()) {
            io.println("(no entries)");
            return;
        }
        while (true) {
            io.println("--- " + c.name() + " ---");
            for (int i = 0; i < list.size(); i++) {
                ItemLibrary.Entry e = list.get(i);
                io.println(String.format("%d) %s", i + 1, e.name));
            }
            io.println("0) Back");
            io.print("Select item for details: ");
            String s = io.readLine();
            io.clear();
            if (s == null) return;
            int idx = -1;
            try { idx = Integer.parseInt(s); } catch (NumberFormatException ex) { idx = -1; }
            if (idx == 0) return;
            if (idx < 1 || idx > list.size()) continue;
            ItemLibrary.Entry e = list.get(idx - 1);
            showEntryDetails(e, io);
        }
    }

    private static void showEntryDetails(ItemLibrary.Entry e, IO io) {
        io.println("--- " + e.name + " ---");
        io.println("Description: " + e.desc);
        io.println("Info: " + sampleToString(e.sample));
        // mark as discovered/unlocked when the player views the entry
        legends.CatalogState.unlock(e.name);
        io.println("(Press Enter to continue)");
        io.readLine();
        io.clear();
    }

    private static String sampleToString(Object sample) {
        if (sample == null) return "";
        return sample.toString();
    }
}
