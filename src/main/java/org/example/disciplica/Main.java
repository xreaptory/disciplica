package org.example.disciplica;

import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("Disciplica starting...");
        Scanner scanner = new Scanner(System.in);

        User user = new User("Simon");

        // Create some initial habits
        user.addHabit(new Habit("Exercise", "Go to the gym for 30 minutes"));
        user.addHabit(new Habit("Read", "Read a book for 20 minutes"));
        user.addHabit(new Habit("Meditate", "Meditate for 10 minutes"));
        user.addHabit(new Habit("Sleep", "Go to bed before 10 PM"));
        user.addHabit(new Habit("Study", "Study for 1 hour"));

        boolean running = true;
        while (running) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Show all habits");
            System.out.println("2. Complete a habit");
            System.out.println("3. Show stats");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    System.out.println("\n--- All Habits ---");
                    user.printHabits();
                    break;
                case "2":
                    System.out.println("\n--- Complete a Habit ---");
                    ArrayList<Habit> habits = user.getHabits();
                    if (habits.isEmpty()) {
                        System.out.println("No habits found.");
                    } else {
                        for (int i = 0; i < habits.size(); i++) {
                            Habit h = habits.get(i);
                            String status = h.isCompleted() ? "[DONE]" : "[ ]";
                            System.out.println((i + 1) + ". " + status + " " + h.getName());
                        }
                        System.out.print("Enter habit number to complete: ");
                        try {
                            int choice = Integer.parseInt(scanner.nextLine());
                            if (choice > 0 && choice <= habits.size()) {
                                Habit selectedHabit = habits.get(choice - 1);
                                if (user.completeHabit(selectedHabit)) {
                                    System.out.println("Completed habit: " + selectedHabit.getName());
                                    System.out.println("You gained some experience!");
                                } else {
                                    System.out.println("Could not complete habit. It might be already completed for today.");
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

