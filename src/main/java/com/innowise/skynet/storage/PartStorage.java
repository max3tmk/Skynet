package com.innowise.skynet.storage;

import com.innowise.skynet.model.Part;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class PartStorage {
    private final Queue<Part> parts = new LinkedList<>();
    private final int MAX_DAILY_PRODUCTION = 10;
    private final int MAX_CARRY = 5;

    private final Semaphore factionsTurn = new Semaphore(0);
    private final Semaphore factoryTurn = new Semaphore(0);

    public void produceAndPassTo() {
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
