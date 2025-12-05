package legends.market;

import java.util.ArrayList;
import java.util.List;

import legends.items.Item;
import legends.items.Weapon;
import legends.items.Armor;
import legends.items.Consumable;
import legends.items.ConsumableStack;
import legends.model.Hero;
import legends.party.Party;

public class Market {
    private final List<Item> items = new ArrayList<>();

    public Market(List<Item> items) {
        this.items.addAll(items);
    }

    public static Market exampleMarket() {
        List<Item> list = new ArrayList<>();
        list.add(new Weapon("Sword", 1, 200, 50, 1));
        list.add(new Weapon("Great Axe", 2, 400, 90, 2));
        list.add(new Weapon("Twinfang Daggers", 2, 360, 70, 1));
        list.add(new Weapon("Stormcaller Spear", 3, 650, 130, 2));
        list.add(new Weapon("Eclipse Bow", 4, 900, 160, 2));
        list.add(new Armor("Leather Armor", 1, 150, 20));
        list.add(new Consumable("Small Health Potion", 1, 50, Consumable.ConsumeType.HEAL, 50, true));
        list.add(new Consumable("Mana Tonic", 1, 40, Consumable.ConsumeType.RESTORE_MP, 30, true));
        list.add(new Consumable("Antidote", 1, 80, Consumable.ConsumeType.ANTIDOTE, 0, true));
        return new Market(list);
    }

    public void enter(Party party, legends.io.IO io) {
        while (true) {
            io.println("--- Market ---");
            io.println("1) Buy");
            io.println("2) Sell");
            io.println("3) Leave");
            io.print("Choice: ");
            String line = io.readLine();
            io.clear();
            if (line == null || line.equals("3")) {
                return; // treat closed input as leaving market
            }

            if (line.equals("2")) {
                handleSell(party, io);
                continue;
            }
            if (!line.equals("1")) {
                continue;
            }

            java.util.List<Hero> heroes = party.getHeroes();
            for (int i = 0; i < heroes.size(); i++) {
                io.println((i + 1) + ") " + heroes.get(i).toString());
            }
            io.print("Choose hero (or 0 to cancel): ");
            int idx;
            try {
                String raw = io.readLine();
                io.clear();
                idx = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                continue;
            }
            if (idx <= 0 || idx > heroes.size()) {
                continue;
            }
            Hero h = heroes.get(idx - 1);

            for (int i = 0; i < items.size(); i++) {
                Item it = items.get(i);
                io.println(String.format("%d) %s  Lv:%d  Price:%d",
                        i + 1, it.getName(), it.getLevel(), it.getPrice()));
            }
            io.print("Choose item to buy (0 cancel): ");
            int itemIdx;
            try {
                String raw = io.readLine();
                io.clear();
                itemIdx = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                continue;
            }
            if (itemIdx <= 0 || itemIdx > items.size()) {
                continue;
            }
            Item it = items.get(itemIdx - 1);

            if (h.getLevel() < it.getLevel()) {
                io.println("Hero level too low.");
                continue;
            }
            if (!h.spendGold(it.getPrice())) {
                io.println("Not enough gold.");
                continue;
            }
            if (it instanceof Weapon) {
                Weapon template = (Weapon) it;
                Weapon purchased = new Weapon(template.getName(), template.getLevel(), template.getPrice(), template.getDamage(), template.getHands());
                party.addWeaponToBackpack(purchased);
                io.println(purchased.getName() + " added to backpack. Open inventory (I) to equip.");
            } else if (it instanceof Armor) {
                Armor template = (Armor) it;
                Armor purchased = new Armor(template.getName(), template.getLevel(), template.getPrice(), template.getReduction());
                party.addArmorToBackpack(purchased);
                io.println(purchased.getName() + " added to backpack. Open inventory (I) to equip.");
            } else if (it instanceof Consumable) {
                h.addConsumable((Consumable) it);
                io.println(h.getName() + " purchased " + it.getName());
            } else {
                io.println("Purchased " + it.getName());
            }
        }
    }

    private void handleSell(Party party, legends.io.IO io) {
        java.util.List<Hero> heroes = party.getHeroes();
        if (heroes.isEmpty()) {
            io.println("No heroes available.");
            return;
        }

        for (int i = 0; i < heroes.size(); i++) {
            io.println((i + 1) + ") " + heroes.get(i).toString());
        }
        io.print("Choose hero (or 0 to cancel): ");
        int idx;
        try {
            String raw = io.readLine();
            io.clear();
            idx = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return;
        }
        if (idx <= 0 || idx > heroes.size()) {
            return;
        }
        Hero h = heroes.get(idx - 1);

        java.util.List<String> labels = new ArrayList<>();
        java.util.List<Integer> payouts = new ArrayList<>();
        java.util.List<Runnable> actions = new ArrayList<>();

        Weapon weapon = h.getEquippedWeapon();
        if (weapon != null) {
            final int payout = weapon.getSellPrice();
            labels.add("Weapon: " + weapon.getName() + " (" + payout + " gold)");
            payouts.add(payout);
            actions.add(() -> {
                h.equipWeapon(null);
                h.gainGold(payout);
            });
        }

        Armor armor = h.getEquippedArmor();
        if (armor != null) {
            final int payout = armor.getSellPrice();
            labels.add("Armor: " + armor.getName() + " (" + payout + " gold)");
            payouts.add(payout);
            actions.add(() -> {
                h.equipArmor(null);
                h.gainGold(payout);
            });
        }

        java.util.List<ConsumableStack> stacks = h.getConsumables();
        for (int i = 0; i < stacks.size(); i++) {
            ConsumableStack stack = stacks.get(i);
            final int stackIndex = i;
            int count = stack.getCount();
            final int payout = count * stack.getConsumable().getSellPrice();
            if (count <= 0 || payout <= 0) {
                continue;
            }
            labels.add("Consumable: " + stack.getConsumable().getName() + " x" + count + " (" + payout + " gold)");
            payouts.add(payout);
            actions.add(() -> {
                ConsumableStack removed = h.removeConsumableStack(stackIndex);
                if (removed != null) {
                    h.gainGold(payout);
                }
            });
        }

        if (labels.isEmpty()) {
            io.println("Nothing to sell.");
            return;
        }

        for (int i = 0; i < labels.size(); i++) {
            io.println((i + 1) + ") " + labels.get(i));
        }
        io.print("Choose item to sell (0 cancel): ");
        int sellIdx;
        try {
            String raw = io.readLine();
            io.clear();
            sellIdx = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return;
        }
        if (sellIdx <= 0 || sellIdx > labels.size()) {
            return;
        }

        int payout = payouts.get(sellIdx - 1);
        actions.get(sellIdx - 1).run();
        io.println("Sold for " + payout + " gold.");
    }
}
