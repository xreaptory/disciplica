package org.example.disciplica;

public class Main {
    public static void main(String[] args) {
        System.out.println("Disciplica starting...");
        User user = new User("Simon");
        Habit habit1 = new Habit("Exercise", "Go to the gym for 30 minutes");
        Habit habit2 = new Habit("Read", "Read a book for 20 minutes");
        Habit habit3 = new Habit("Meditate", "Meditate for 10 minutes");
        Habit habit4 = new Habit("Sleep", "Go to bed before 10 PM");
        Habit habit5 = new Habit("Excercise", "Run 2 kilometers");
        Habit habit6 = new Habit("Study", "Study for 1 hour");

        System.out.println("--- Test addHabits ---");
        System.out.println(user.addHabit(habit1));
        System.out.println(user.addHabit(habit2));
        System.out.println(user.addHabit(habit3));
        System.out.println(user.addHabit(habit4));
        System.out.println(user.addHabit(habit5));
        System.out.println(user.addHabit(habit6));

        user.printHabits();
        user.printUser();
        System.out.println("--- Test completeHabits ---");
        user.completeHabit(habit1);
        user.completeHabit(habit2);
        user.completeHabit(habit3);
        user.completeHabit(habit4);
        user.completeHabit(habit5);
        user.completeHabit(habit6);
        user.printUser();
        user.printHabits();





//        System.out.println("Before: " + habit1.getName() + " - Completed: " + habit1.isCompleted());
//        System.out.println("Before: " + habit2.getName() + " - Completed: " + habit2.isCompleted());
//        System.out.println("Before: " + habit3.getName() + " - Completed: " + habit3.isCompleted());
//        System.out.println("Before Streak-"+habit1.getName()+": "+habit1.getStreak());
//        System.out.println("Before Streak-"+habit2.getName()+": "+habit2.getStreak());
//        System.out.println("Before Streak-"+habit3.getName()+": "+habit3.getStreak());
//
//        // Test complete
//        habit1.complete();
//        habit2.complete();
//
//        // Verify state changed
//        System.out.println("After: " + habit1.getName() + " - Completed: " + habit1.isCompleted());
//        System.out.println("After: " + habit2.getName() + " - Completed: " + habit2.isCompleted());
//        System.out.println("After: " + habit3.getName() + " - Completed: " + habit3.isCompleted());
//        System.out.println("After Streak-"+habit1.getName()+": "+habit1.getStreak());
//        System.out.println("After Streak-"+habit2.getName()+": "+habit2.getStreak());
//        System.out.println("After Streak-"+habit3.getName()+": "+habit3.getStreak());
    }
}
