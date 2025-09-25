package com.innowise.skynet.faction;

import com.innowise.skynet.model.Part;
import com.innowise.skynet.storage.PartStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Faction implements Runnable {
    private final String name;
    private final PartStorage storage;
    private final Map<Part, Integer> inventory = new HashMap<>();
    private int robotsBuilt = 0;

    public Faction(String name, PartStorage storage) {
        this.name = name;
        this.storage = storage;
        for (Part p : Part.values()) {
            inventory.put(p, 0);
        }
    }

    @Override
    public void run() {
        for (int day = 1; day <= 100; day++) {
            List<Part> newParts = storage.takeParts(name);

            for (Part part : newParts) {
                inventory.put(part, inventory.get(part) + 1);
            }

            int robotsBefore = robotsBuilt;
            while (canBuildRobot()) {
                buildRobot();
            }
            if (robotsBuilt > robotsBefore) {
                System.out.println(name + " built " +
                        (robotsBuilt - robotsBefore) + " robots. Total: " + robotsBuilt);
            }
        }
    }

    private boolean canBuildRobot() {
        return inventory.get(Part.HEAD) >= 1 &&
                inventory.get(Part.TORSO) >= 1 &&
                inventory.get(Part.HAND) >= 2 &&
                inventory.get(Part.FEET) >= 2;
    }

    private void buildRobot() {
        inventory.put(Part.HEAD, inventory.get(Part.HEAD) - 1);
        inventory.put(Part.TORSO, inventory.get(Part.TORSO) - 1);
        inventory.put(Part.HAND, inventory.get(Part.HAND) - 2);
        inventory.put(Part.FEET, inventory.get(Part.FEET) - 2);
        robotsBuilt++;
    }

    public int getRobotsBuilt() {
        return robotsBuilt;
    }

    public String getName() {
        return name;
    }
}
