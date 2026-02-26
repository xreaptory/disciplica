package org.example.disciplica;

public class Main {
        public static void main(String[] args) {
            System.out.println("Disciplica starting...");
            Habit habit1 = new Habit("Exercise", "Go to the gym for 30 minutes", false);
            Habit habit2 = new Habit("Read", "Read a book for 20 minutes", false);
            Habit habit3 = new Habit("Meditate", "Meditate for 10 minutes", false);
            habit1.print();
            habit2.print();
            habit3.print();
        }
}
