package com.disciplica;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.IntStream;

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

    public static void main(String[] args) {
        logger.info("Disciplica starting...");
        System.out.println("Disciplica starting...");
        Scanner scanner = new Scanner(System.in);

        // Initialize file-based task repository for persistence
        FileTaskRepository taskRepository = new FileTaskRepository();
        
        User user = new User("Simon");

        // Load existing tasks from file or create default tasks if first run
        try {
            List<AbstractTask> savedTasks = taskRepository.load();
            if (!savedTasks.isEmpty()) {
                logger.info("Loading {} saved task(s) from previous session", savedTasks.size());
                System.out.println("Welcome back! Loading " + savedTasks.size() + " saved task(s)...");
                savedTasks.forEach(task -> {
                    try {
                        user.addTask(task);
                    } catch (InvalidHabitException ex) {
                        logger.warn("Skipping invalid saved task: {}", task.getName(), ex);
                    }
                });
            } else {
                // First run - create some initial tasks
                logger.info("First run detected - creating default tasks");
                System.out.println("Welcome! Setting up your initial tasks...");
                user.addTask(new DailyHabit("Exercise", "Go to the gym for 30 minutes", 10));
                user.addTask(new DailyHabit("Read", "Read a book for 20 minutes", 5));
                user.addTask(new WeeklyHabit("Clean Room", "Tidy up the room", 20));
                user.addTask(new OneTimeTask("Buy Groceries", "Get milk and eggs", 5));
                user.addTask(new DailyHabit("Study", "Study for 1 hour", 15));
                
                // Save the initial tasks
                taskRepository.saveAll(user.getTasks());
                logger.info("Initial tasks created and saved");
            }
        } catch (IOException e) {
            logger.error("Failed to load tasks from file", e);
            System.out.println("Warning: Could not load saved tasks. Starting fresh.");
            // Create default tasks if loading failed
            try {
                user.addTask(new DailyHabit("Exercise", "Go to the gym for 30 minutes", 10));
                user.addTask(new DailyHabit("Read", "Read a book for 20 minutes", 5));
                user.addTask(new WeeklyHabit("Clean Room", "Tidy up the room", 20));
                user.addTask(new OneTimeTask("Buy Groceries", "Get milk and eggs", 5));
                user.addTask(new DailyHabit("Study", "Study for 1 hour", 15));
                taskRepository.saveAll(user.getTasks());
                logger.info("Default tasks created after load failure");
            } catch (InvalidHabitException | IOException ex) {
                logger.error("Failed to create default tasks", ex);
            }
        } catch (InvalidHabitException e) {
            logger.error("Failed to initialize tasks", e);
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
                        IntStream.range(0, tasks.size()).forEach(i -> {
                            AbstractTask t = tasks.get(i);
                            String status = t.isCompleted() ? "[DONE]" : "[ ]";
                            System.out.println((i + 1) + ". " + status + " " + t.getName() + " (" + t.getClass().getSimpleName() + ")");
                        });
                        System.out.print("Enter task number to complete: ");
                        try {
                            int choice = Integer.parseInt(scanner.nextLine());
                            if (choice > 0 && choice <= tasks.size()) {
                                AbstractTask selectedTask = tasks.get(choice - 1);
                                try {
                                    if (user.completeTask(selectedTask)) {
                                        System.out.println("Completed task: " + selectedTask.getName());
                                        System.out.println("You gained " + selectedTask.calculatePoints() + " experience points!");
                                        
                                        // Auto-save after task completion
                                        try {
                                            taskRepository.saveAll(user.getTasks());
                                            logger.debug("Tasks auto-saved after completion");
                                        } catch (IOException ioEx) {
                                            logger.warn("Failed to auto-save tasks after completion", ioEx);
                                        }
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

                    List<AbstractTask> completedHabits = user.getCompletedTasks();
                    List<String> habitNames = user.getTaskNames();
                    int totalPotentialExperience = user.getTotalExperienceFromTasks();
                    List<AbstractTask> sortedByStreak = user.getTasksSortedByStreak();

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
                    System.out.println("Saving tasks...");
                    logger.info("Saving tasks before exit");
                    try {
                        taskRepository.saveAll(user.getTasks());
                        System.out.println("Tasks saved successfully!");
                        logger.info("Tasks saved: {} total", user.getTasks().size());
                    } catch (IOException e) {
                        System.out.println("Warning: Failed to save tasks: " + e.getMessage());
                        logger.error("Failed to save tasks on exit", e);
                    }
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
