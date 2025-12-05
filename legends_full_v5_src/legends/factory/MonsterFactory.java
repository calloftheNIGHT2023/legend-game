package legends.factory;

import legends.model.Monster;

public interface MonsterFactory {
    Monster create(String name, int level);
}
