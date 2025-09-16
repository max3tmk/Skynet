import java.util.*;

public class Skynet {

    enum Part {
        HEAD, TORSO, HAND, FEET
    }

    static class PartStorage {
        private final Queue<Part> parts = new LinkedList<>();
        private final int MAX_DAILY_PRODUCTION = 10;
        private final int MAX_CARRY = 5;
        private volatile boolean productionFinished = false;

        public synchronized void produceParts() {
            Random rand = new Random();
            int count = rand.nextInt(MAX_DAILY_PRODUCTION) + 1;
            List<Part> newParts = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Part part = Part.values()[rand.nextInt(Part.values().length)];
                parts.add(part);
                newParts.add(part);
            }
            System.out.println("[DAY] Factory produced " + count + " parts: " + newParts +
                    " | Total in storage: " + parts.size());
            notifyAll();
        }

        public synchronized void finishProduction() {
            productionFinished = true;
            notifyAll();
        }

        public synchronized List<Part> takeParts(String factionName) {
            while (parts.isEmpty() && !productionFinished) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new ArrayList<>();
                }
            }

            if (parts.isEmpty()) {
                return new ArrayList<>();
            }

            List<Part> taken = new ArrayList<>();
            for (int i = 0; i < MAX_CARRY && !parts.isEmpty(); i++) {
                taken.add(parts.poll());
            }
            System.out.println("[NIGHT] " + factionName + " took: " + taken +
                    " | Remaining in storage: " + parts.size());
            return taken;
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
                    System.out.println("[NIGHT] " + name + " built " +
                            (robotsBuilt - robotsBefore) + " new robots. Total: " + robotsBuilt);
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
        PartStorage factoryStorage = new PartStorage();

        Faction world = new Faction("World", factoryStorage);
        Faction wednesday = new Faction("Wednesday", factoryStorage);

        Thread tWorld = new Thread(world);
        Thread tWednesday = new Thread(wednesday);

        tWorld.start();
        tWednesday.start();

        for (int day = 1; day <= 100; day++) {
            System.out.println("\n--- DAY " + day + " ---");
            factoryStorage.produceParts();
        }

        factoryStorage.finishProduction();

        tWorld.join();
        tWednesday.join();

        System.out.println("\n--- FINAL RESULTS AFTER 100 DAYS ---");
        System.out.println(world.getName() + " built: " + world.getRobotsBuilt() + " robots");
        System.out.println(wednesday.getName() + " built: " + wednesday.getRobotsBuilt() + " robots");
        System.out.println();

        if (world.getRobotsBuilt() > wednesday.getRobotsBuilt()) {
            System.out.println("WINNER: " + world.getName());
        } else if (wednesday.getRobotsBuilt() > world.getRobotsBuilt()) {
            System.out.println("WINNER: " + wednesday.getName());
        } else {
            System.out.println("DRAW!");
        }
    }
}