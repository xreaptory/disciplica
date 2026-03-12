package com.disciplica;

import java.util.Scanner;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void processCompletable(Completable completable) {
        logger.info("Processing completable item: {}", completable.getClass().getSimpleName());
        if (completable.complete()) {
            System.out.println("Completed successfully. Reward earned: " + completable.getReward());
            logger.info("Completable item completed successfully");
        } else {
            System.out.println("Item was already completed.");
            logger.info("Completable item was already completed");
        }
    }

    private static List<AbstractTask> getCompletedHabits(User user) {
        return user.getTasks().stream()
            .filter(AbstractTask::isCompleted)
            .collect(Collectors.toList());
    }

    private static List<String> getHabitNames(User user) {
        return user.getTasks().stream()
            .map(AbstractTask::getName)
            .collect(Collectors.toList());
    }

    private static int getTotalExperienceFromTasks(User user) {
        return user.getTasks().stream()
            .reduce(0, (sum, task) -> sum + task.calculatePoints(), Integer::sum);
    }

    private static List<AbstractTask> getHabitsSortedByStreak(User user) {
        return user.getTasks().stream()
            .sorted(Comparator.<AbstractTask, Integer>comparing(AbstractTask::getStreak).reversed())
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        logger.info("Disciplica starting...");
        System.out.println("Disciplica starting...");
        Scanner scanner = new Scanner(System.in);

        User user = new User("Simon");

        // Create some initial tasks
        try {
            user.addTask(new DailyHabit("Exercise", "Go to the gym for 30 minutes", 10));
            user.addTask(new DailyHabit("Read", "Read a book for 20 minutes", 5));
            user.addTask(new WeeklyHabit("Clean Room", "Tidy up the room", 20));
            user.addTask(new OneTimeTask("Buy Groceries", "Get milk and eggs", 5));
            user.addTask(new DailyHabit("Study", "Study for 1 hour", 15));
        } catch (InvalidHabitException e) {
            logger.error("Failed to initialize default tasks", e);
            System.out.println("Error initializing tasks: " + e.getMessage());
        }

        // Demonstrate interface-based processing with another implementation.
        processCompletable(new Habit("Drink Water", "Drink 2L of water"));

        boolean running = true;
        while (running) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Show all tasks");
            System.out.println("2. Complete a task");
            System.out.println("3. Show stats");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            logger.debug("User input: {}", input);

            switch (input) {
                case "1":
                    System.out.println("\n--- All Tasks ---");
                    user.printTasks();
                    break;
                case "2":
                    System.out.println("\n--- Complete a Task ---");
                    ArrayList<AbstractTask> tasks = user.getTasks();
                    if (tasks.isEmpty()) {
                        System.out.println("No tasks found.");
                    } else {
                        for (int i = 0; i < tasks.size(); i++) {
                            final int index = i;
                            AbstractTask t = tasks.get(i);
                            String status = t.isCompleted() ? "[DONE]" : "[ ]";
                            System.out.println((index + 1) + ". " + status + " " + t.getName() + " (" + t.getClass().getSimpleName() + ")");
                        }
                        System.out.print("Enter task number to complete: ");
                        try {
                            int choice = Integer.parseInt(scanner.nextLine());
                            if (choice > 0 && choice <= tasks.size()) {
                                AbstractTask selectedTask = tasks.get(choice - 1);
                                try {
                                    if (user.completeTask(selectedTask)) {
                                        System.out.println("Completed task: " + selectedTask.getName());
                                        System.out.println("You gained " + selectedTask.calculatePoints() + " experience points!");
                                    } else {
                                        System.out.println("Could not complete task. It might be already completed.");
                                    }
                                } catch (HabitNotFoundException e) {
                                     logger.error("Error completing task", e);
                                     System.out.println("Error: " + e.getMessage());
                                }
                            } else {
                                System.out.println("Invalid choice.");
                                logger.warn("Invalid task number selected: {}", choice);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            logger.warn("Invalid number format entered", e);
                        }
                    }
                    break;
                case "3":
                    System.out.println("\n--- User Stats ---");
                    user.printUser();

                    List<AbstractTask> completedHabits = getCompletedHabits(user);
                    List<String> habitNames = getHabitNames(user);
                    int totalPotentialExperience = getTotalExperienceFromTasks(user);
                    List<AbstractTask> sortedByStreak = getHabitsSortedByStreak(user);

                    System.out.println("Completed habits: " + completedHabits.size());
                    completedHabits.forEach(task ->
                        System.out.println("  - " + task.getName() + " [streak=" + ((Trackable) task).getStreak() + "]")
                    );

                    System.out.println("All habit names: " + habitNames);
                    System.out.println("Total potential experience points (all tasks): " + totalPotentialExperience);

                    System.out.println("Habits sorted by streak:");
                    sortedByStreak.forEach(task ->
                        System.out.println("  - " + task.getName() + " (streak=" + ((Trackable) task).getStreak() + ")")
                    );
                    break;
                case "4":
                    System.out.println("Exiting...");
                    logger.info("Application exiting");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    logger.warn("Invalid menu option selected: {}", input);
            }
        }
        scanner.close();
    }
}
