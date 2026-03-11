package org.example.disciplica;

import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("Disciplica starting...");
        Scanner scanner = new Scanner(System.in);

        User user = new User("Simon");

        // Create some initial tasks
        user.addTask(new DailyHabit("Exercise", "Go to the gym for 30 minutes", 10));
        user.addTask(new DailyHabit("Read", "Read a book for 20 minutes", 5));
        user.addTask(new WeeklyHabit("Clean Room", "Tidy up the room", 20));
        user.addTask(new OneTimeTask("Buy Groceries", "Get milk and eggs", 5));
        user.addTask(new DailyHabit("Study", "Study for 1 hour", 15));

        boolean running = true;
        while (running) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Show all tasks");
            System.out.println("2. Complete a task");
            System.out.println("3. Show stats");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();

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
                            AbstractTask t = tasks.get(i);
                            String status = t.isCompleted() ? "[DONE]" : "[ ]";
                            System.out.println((i + 1) + ". " + status + " " + t.getName() + " (" + t.getClass().getSimpleName() + ")");
                        }
                        System.out.print("Enter task number to complete: ");
                        try {
                            int choice = Integer.parseInt(scanner.nextLine());
                            if (choice > 0 && choice <= tasks.size()) {
                                AbstractTask selectedTask = tasks.get(choice - 1);
                                if (user.completeTask(selectedTask)) {
                                    System.out.println("Completed task: " + selectedTask.getName());
                                    System.out.println("You gained " + selectedTask.calculatePoints() + " experience points!");
                                } else {
                                    System.out.println("Could not complete task. It might be already completed.");
                                }
                            } else {
                                System.out.println("Invalid choice.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                        }
                    }
                    break;
                case "3":
                    System.out.println("\n--- User Stats ---");
                    user.printUser();
                    break;
                case "4":
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }
}
