package com.disciplica.bootstrap;
import com.disciplica.domain.contract.Completable;
import com.disciplica.domain.contract.Trackable;
import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.AbstractTask;
import com.disciplica.domain.model.DailyHabit;
import com.disciplica.domain.model.Habit;
import com.disciplica.domain.model.OneTimeTask;
import com.disciplica.domain.model.User;
import com.disciplica.domain.model.WeeklyHabit;
import com.disciplica.infrastructure.persistence.FileTaskRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class    Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String MENU_OPTION_SHOW_TASKS = "1";
    private static final String MENU_OPTION_COMPLETE_TASK = "2";
    private static final String MENU_OPTION_SHOW_STATS = "3";
    private static final String MENU_OPTION_EXIT = "4";

    public static void processCompletable(Completable completable) {
        logCompletableStart(completable);
        if (completable.complete()) {
            printCompletableSuccess(completable);
            return;
        }
        printCompletableAlreadyDone();
    }

    public static void main(String[] args) {
        HabitTrackerApp.main(args);
    }

    private static AppContext createContext() {
        Scanner userInput = new Scanner(System.in);
        FileTaskRepository repository = new FileTaskRepository();
        User user = new User("Simon");
        return new AppContext(userInput, user, repository);
    }

    private static void announceStartup() {
        logger.info("Disciplica starting...");
        System.out.println("Disciplica starting...");
    }

    private static void logCompletableStart(Completable completable) {
        String typeName = completable.getClass().getSimpleName();
        logger.info("Processing completable item: {}", typeName);
    }

    private static void printCompletableSuccess(Completable completable) {
        System.out.println("Completed successfully. Reward earned: " + completable.getReward());
        logger.info("Completable item completed successfully");
    }

    private static void printCompletableAlreadyDone() {
        System.out.println("Item was already completed.");
        logger.info("Completable item was already completed");
    }

    private static void initializeUserTasks(User user, FileTaskRepository repository) {
        try {
            List<AbstractTask> savedTasks = repository.load();
            initializeFromLoadedTasks(user, repository, savedTasks);
        } catch (IOException ioException) {
            handleLoadFailure(user, repository, ioException);
        } catch (InvalidHabitException invalidHabitException) {
            reportInitializationError(invalidHabitException);
        }
    }

    private static void initializeFromLoadedTasks(User user, FileTaskRepository repository,
            List<AbstractTask> savedTasks) throws InvalidHabitException, IOException {
        if (savedTasks.isEmpty()) {
            createAndPersistDefaultTasks(user, repository);
            return;
        }
        restoreSavedTasks(user, savedTasks);
    }

    private static void restoreSavedTasks(User user, List<AbstractTask> savedTasks) {
        logger.info("Loading {} saved task(s) from previous session", savedTasks.size());
        System.out.println("Welcome back! Loading " + savedTasks.size() + " saved task(s)...");
        savedTasks.forEach(task -> safelyAddTask(user, task));
    }

    private static void safelyAddTask(User user, AbstractTask task) {
        try {
            user.addTask(task);
        } catch (InvalidHabitException invalidHabitException) {
            logger.warn("Skipping invalid saved task: {}", task.getName(), invalidHabitException);
        }
    }

    private static void handleLoadFailure(User user, FileTaskRepository repository,
            IOException ioException) {
        logger.error("Failed to load tasks from file", ioException);
        System.out.println("Warning: Could not load saved tasks. Starting fresh.");
        try {   
            createAndPersistDefaultTasks(user, repository);
        } catch (InvalidHabitException | IOException defaultTaskException) {
            logger.error("Failed to create default tasks", defaultTaskException);
        }
    }

    private static void reportInitializationError(InvalidHabitException invalidHabitException) {
        logger.error("Failed to initialize tasks", invalidHabitException);
        System.out.println("Error initializing tasks: " + invalidHabitException.getMessage());
    }

    private static void createAndPersistDefaultTasks(User user, FileTaskRepository repository)
            throws InvalidHabitException, IOException {
        logger.info("Creating default tasks");
        System.out.println("Welcome! Setting up your initial tasks...");
        addDefaultTasks(user);
        repository.saveAll(user.getTasks());
        logger.info("Default tasks created and saved");
    }

    private static void addDefaultTasks(User user) throws InvalidHabitException {
        user.addTask(new DailyHabit("Exercise", "Go to the gym for 30 minutes", 10));
        user.addTask(new DailyHabit("Read", "Read a book for 20 minutes", 5));
        user.addTask(new WeeklyHabit("Clean Room", "Tidy up the room", 20));
        user.addTask(new OneTimeTask("Buy Groceries", "Get milk and eggs", 5));
        user.addTask(new DailyHabit("Study", "Study for 1 hour", 15));
    }

    private static void runMenuLoop(AppContext context) {
        boolean keepRunning = true;
        while (keepRunning) {
            showMenu();
            String selectedOption = readMenuOption(context.input());
            keepRunning = handleMenuOption(selectedOption, context);
        }
    }

    private static void showMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Show all tasks");
        System.out.println("2. Complete a task");
        System.out.println("3. Show stats");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
    }

    private static String readMenuOption(Scanner userInput) {
        String selectedOption = userInput.nextLine();
        logger.debug("User input: {}", selectedOption);
        return selectedOption;
    }

    private static boolean handleMenuOption(String selectedOption, AppContext context) {
        return switch (selectedOption) {
            case MENU_OPTION_SHOW_TASKS -> runShowTasks(context.user());
            case MENU_OPTION_COMPLETE_TASK -> runCompleteTask(context);
            case MENU_OPTION_SHOW_STATS -> runShowStats(context.user());
            case MENU_OPTION_EXIT -> saveAndExit(context);
            default -> runInvalidOption(selectedOption);
        };
    }

    private static boolean runShowTasks(User user) {
        showAllTasks(user);
        return true;
    }

    private static boolean runCompleteTask(AppContext context) {
        completeTaskFlow(context);
        return true;
    }

    private static boolean runShowStats(User user) {
        showUserStats(user);
        return true;
    }

    private static boolean runInvalidOption(String selectedOption) {
        reportInvalidMenuOption(selectedOption);
        return true;
    }

    private static void showAllTasks(User user) {
        System.out.println("\n--- All Tasks ---");
        user.printTasks();
    }

    private static void completeTaskFlow(AppContext context) {
        System.out.println("\n--- Complete a Task ---");
        ArrayList<AbstractTask> tasks = context.user().getTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        showTaskSelection(tasks);
        handleTaskSelection(context, tasks);
    }

    private static void showTaskSelection(List<AbstractTask> tasks) {
        IntStream.range(0, tasks.size()).forEach(index -> printTaskLine(tasks, index));
        System.out.print("Enter task number to complete: ");
    }

    private static void printTaskLine(List<AbstractTask> tasks, int index) {
        AbstractTask task = tasks.get(index);
        String statusLabel = task.isCompleted() ? "[DONE]" : "[ ]";
        String taskType = task.getClass().getSimpleName();
        System.out.println((index + 1) + ". " + statusLabel + " " + task.getName() + " (" + taskType + ")");
    }

    private static void handleTaskSelection(AppContext context, List<AbstractTask> tasks) {
        try {
            int selectedTaskNumber = Integer.parseInt(context.input().nextLine());
            completeSelectedTask(selectedTaskNumber, tasks, context);
        } catch (NumberFormatException numberFormatException) {
            System.out.println("Invalid input. Please enter a number.");
            logger.warn("Invalid number format entered", numberFormatException);
        }
    }

    private static void completeSelectedTask(int selectedTaskNumber, List<AbstractTask> tasks,
            AppContext context) {
        if (!isTaskSelectionValid(selectedTaskNumber, tasks.size())) {
            System.out.println("Invalid choice.");
            logger.warn("Invalid task number selected: {}", selectedTaskNumber);
            return;
        }
        AbstractTask selectedTask = tasks.get(selectedTaskNumber - 1);
        attemptTaskCompletion(context, selectedTask);
    }

    private static boolean isTaskSelectionValid(int selectedTaskNumber, int availableTaskCount) {
        return selectedTaskNumber > 0 && selectedTaskNumber <= availableTaskCount;
    }

    private static void attemptTaskCompletion(AppContext context, AbstractTask selectedTask) {
        try {
            completeTaskAndPersist(context, selectedTask);
        } catch (HabitNotFoundException habitNotFoundException) {
            logger.error("Error completing task", habitNotFoundException);
            System.out.println("Error: " + habitNotFoundException.getMessage());
        }
    }

    private static void completeTaskAndPersist(AppContext context, AbstractTask selectedTask)
            throws HabitNotFoundException {
        boolean taskCompleted = context.user().completeTask(selectedTask);
        reportCompletionResult(taskCompleted, selectedTask);
        if (taskCompleted) {
            persistAfterCompletion(context);
        }
    }

    private static void reportCompletionResult(boolean taskCompleted, AbstractTask selectedTask) {
        if (!taskCompleted) {
            System.out.println("Could not complete task. It might be already completed.");
            return;
        }
        System.out.println("Completed task: " + selectedTask.getName());
        System.out.println("You gained " + selectedTask.calculatePoints() + " experience points!");
    }

    private static void persistAfterCompletion(AppContext context) {
        try {
            context.repository().saveAll(context.user().getTasks());
            logger.debug("Tasks auto-saved after completion");
        } catch (IOException ioException) {
            logger.warn("Failed to auto-save tasks after completion", ioException);
        }
    }

    private static void showUserStats(User user) {
        System.out.println("\n--- User Stats ---");
        user.printUser();
        List<AbstractTask> completedTasks = user.getCompletedTasks();
        printCompletedTasks(completedTasks);
        printTaskNameSummary(user);
        printTasksSortedByStreak(user);
    }

    private static void printCompletedTasks(List<AbstractTask> completedTasks) {
        System.out.println("Completed habits: " + completedTasks.size());
        completedTasks.forEach(task -> printTaskWithStreak("[streak=", task, "]"));
    }

    private static void printTaskNameSummary(User user) {
        List<String> taskNames = user.getTaskNames();
        int totalExperience = user.getTotalExperienceFromTasks();
        System.out.println("All habit names: " + taskNames);
        System.out.println("Total potential experience points (all tasks): " + totalExperience);
    }

    private static void printTasksSortedByStreak(User user) {
        List<AbstractTask> tasksByStreak = user.getTasksSortedByStreak();
        System.out.println("Habits sorted by streak:");
        tasksByStreak.forEach(task -> printTaskWithStreak("(streak=", task, ")"));
    }

    private static void printTaskWithStreak(String prefix, AbstractTask task, String suffix) {
        System.out.println("  - " + task.getName() + " " + prefix + ((Trackable) task).getStreak() + suffix);
    }

    private static boolean saveAndExit(AppContext context) {
        System.out.println("Saving tasks...");
        logger.info("Saving tasks before exit");
        saveBeforeExit(context);
        System.out.println("Exiting...");
        logger.info("Application exiting");
        return false;
    }

    private static void saveBeforeExit(AppContext context) {
        try {
            context.repository().saveAll(context.user().getTasks());
            System.out.println("Tasks saved successfully!");
            logger.info("Tasks saved: {} total", context.user().getTasks().size());
        } catch (IOException ioException) {
            System.out.println("Warning: Failed to save tasks: " + ioException.getMessage());
            logger.error("Failed to save tasks on exit", ioException);
        }
    }

    private static void reportInvalidMenuOption(String selectedOption) {
        System.out.println("Invalid option. Please try again.");
        logger.warn("Invalid menu option selected: {}", selectedOption);
    }

    private record AppContext(Scanner input, User user, FileTaskRepository repository) {
    }
}


