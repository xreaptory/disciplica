package org.example.disciplica;

public class Main {
        public static void main(String[] args) {
            System.out.println("Disciplica starting...");
            Habit habit1 = new Habit("Exercise", "Go to the gym for 30 minutes");
            Habit habit2 = new Habit("Read", "Read a book for 20 minutes");
            Habit habit3 = new Habit("Meditate", "Meditate for 10 minutes");

            System.out.println("Before: " + habit1.getName() + " - Completed: " + habit1.isCompleted());
            System.out.println("Before: " + habit2.getName() + " - Completed: " + habit2.isCompleted());
            System.out.println("Before: " + habit3.getName() + " - Completed: " + habit3.isCompleted());

            // Test complete
            habit1.complete();
            habit2.complete();

            // Verify state changed
            System.out.println("After: " + habit1.getName() + " - Completed: " + habit1.isCompleted());
            System.out.println("After: " + habit2.getName() + " - Completed: " + habit2.isCompleted());
            System.out.println("After: " + habit3.getName() + " - Completed: " + habit3.isCompleted());
        }
}
