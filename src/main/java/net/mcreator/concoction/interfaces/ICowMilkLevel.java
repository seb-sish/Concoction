package net.mcreator.concoction.interfaces;

public interface ICowMilkLevel {
    int concoction$getMilkLevel();
    void concoction$setMilkLevel(int level);
    void concoction$incrementMilkLevel();
    void concoction$decrementMilkLevel();

    long concoction$getLastMilkedTime();
    void concoction$setLastMilkedTime(long time);
} 