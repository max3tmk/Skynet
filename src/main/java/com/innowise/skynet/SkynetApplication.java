package com.innowise.skynet;

import com.innowise.skynet.faction.Faction;
import com.innowise.skynet.storage.PartStorage;

public class SkynetApplication {
    public static void main(String[] args) throws InterruptedException {
        PartStorage storage = new PartStorage();

        Faction world = new Faction("World", storage);
        Faction wednesday = new Faction("Wednesday", storage);

        Thread t1 = new Thread(world, "World");
        Thread t2 = new Thread(wednesday, "Wednesday");

        t1.start();
        t2.start();

        for (int round = 1; round <= 100; round++) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("ROUND " + round + " — NIGHT STARTS");

            storage.produceAndPassTo();

            storage.waitForFactions();

            System.out.println("ROUND " + round + " COMPLETED");
        }

        t1.join();
        t2.join();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("FINAL RESULTS AFTER 100 ROUNDS");
        System.out.println("World built: " + world.getRobotsBuilt() + " robots");
        System.out.println("Wednesday built: " + wednesday.getRobotsBuilt() + " robots");

        if (world.getRobotsBuilt() > wednesday.getRobotsBuilt()) {
            System.out.println("WINNER: " + world.getName());
        } else if (wednesday.getRobotsBuilt() > world.getRobotsBuilt()) {
            System.out.println("WINNER: " + wednesday.getName());
        } else {
            System.out.println("DRAW!");
        }
    }
}
