import java.util.*;
import java.util.concurrent.Semaphore;

public class Skynet {

    enum Part {
        HEAD, TORSO, HAND, FEET
    }

    static class PartStorage {
        private final Queue<Part> parts = new LinkedList<>();
        private final int MAX_DAILY_PRODUCTION = 10;
        private final int MAX_CARRY = 5;

        private final Semaphore factionsTurn = new Semaphore(0);
        private final Semaphore factoryTurn = new Semaphore(0);

        public void produceAndPassTo(int night) {
            Random rand = new Random();
            int count = rand.nextInt(MAX_DAILY_PRODUCTION) + 1;
            List<Part> newParts = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Part part = Part.values()[rand.nextInt(Part.values().length)];
                parts.add(part);
                newParts.add(part);
            }
            System.out.println("Factory produced " + count + " parts: " + newParts +
                    " | Total in storage: " + parts.size());

            factionsTurn.release(2);
        }

        public List<Part> takeParts(String factionName) {
            try {
                factionsTurn.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }

            List<Part> taken = new ArrayList<>();
            synchronized (parts) {
                for (int i = 0; i < MAX_CARRY && !parts.isEmpty(); i++) {
                    taken.add(parts.poll());
                }
            }

            System.out.println(factionName + " took: " + taken +
                    " | Remaining: " + parts.size());

            factoryTurn.release();

            return taken;
        }

        public void waitForFactions() {
            try {
                factoryTurn.acquire(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Faction implements Runnable {
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

            storage.produceAndPassTo(round);

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