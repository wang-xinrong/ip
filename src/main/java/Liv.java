import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

// name of the chat bot
public class Liv {
    // to adhere to the singleton pattern
    private enum State {
        ACTIVE_TALKING,
        /*
            things to be handled in ACTIVE_TALKING state:
            - print words to say
         */
        ACTIVE_LISTENING,
        /*
            things to be handled in ACTIVE_LISTENING state:
            - take in user input
            - process the input
         */
        INACTIVE
    }
    private static Liv instance = null;
    private static HorizontalLine horizontalLine = null;
    private State currentState;
    private static Scanner scanner = null;
    private static LinkedList<Task> tasks = null;

    private static String dataFilePath = "Data/savedTasks.txt";

    private Liv() {
        // initial setup
        horizontalLine = HorizontalLine.getInstance();
        currentState = State.INACTIVE;
        scanner = new Scanner(System.in);
        tasks = new LinkedList<>();
        try {
            loadFromMemory();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadFromMemory() throws FileNotFoundException {
        File file = new File(dataFilePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            loadSingleRowOfData(scanner.nextLine());
        }
    }

    private int getNumOfTasks() {
        return tasks.size();
    }

    private void loadSingleRowOfData(String s) {
        tasks.add(Task.convertDataToTask(s));
    }


    private void saveToMemory() {
        try {
            String dataToWrite = "";
            for (int i = 1; i <= getNumOfTasks(); i++) {
                dataToWrite += tasks.get(i - 1).convertToDataRow();
                if (i < getNumOfTasks()) dataToWrite += System.lineSeparator();
            }
            writeToFile(dataFilePath, dataToWrite);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void writeToFile(String filePath, String textToAdd) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath);
        fileWriter.write(textToAdd);
        fileWriter.close();
    }

    public static Liv getInstance() {
        if (instance == null) {
            instance = new Liv();
        }

        return instance;
    }
    public static void main(String[] args) {
        getInstance().Start();
    }

    private void Start() {
        instance.ToggleActiveState();
        instance.Greet();
        while (IsActive()) {
            // should start the cycle talking
            String userInput = StartListening();
            try{
                ProcessInput(userInput);
            } catch (InputException e) {
                speak(e.getMessage());
            }
        }
    }

    private void Greet() {
        System.out.println("Hello there, Liv here.");
        System.out.println("How may I help you?");
    }

    private void EndSession() {
        // should be called from ACTIVE_LISTENING STATE, exception handling?
        ToggleConversationState();
        System.out.println("Hope you find my service helpful.");
        System.out.println("Till next time!");
        ToggleActiveState();
    }
    private void ToggleConversationState() {

        horizontalLine.printLine();

        if (currentState == State.ACTIVE_TALKING) {
            currentState = State.ACTIVE_LISTENING;
            return;
        }

        if (currentState == State.ACTIVE_LISTENING) {
            currentState = State.ACTIVE_TALKING;
            return;
        }
    }

    private void ToggleActiveState() {

        horizontalLine.printLine();

        if (currentState != State.INACTIVE) {
            currentState = State.INACTIVE;
            return;
        }

        if (currentState == State.INACTIVE) {
            currentState = State.ACTIVE_TALKING;
            return;
        }
    }
    private String StartListening() {
            // should be called from ACTIVE_TALKING STATE
            if (currentState == State.ACTIVE_TALKING) ToggleConversationState();
            return scanner.nextLine();
    }

    private void EndListening() {
        if (currentState == State.ACTIVE_TALKING) ToggleConversationState();
    }

    private boolean IsActive() {
        return currentState != State.INACTIVE;
    }

    private void ProcessInput(String input) throws InputException {

        String[] words = input.split(" ");

        // for multi-word commands
        if (words[0].equals("mark") || words[0].equals("unmark")) {
            if (isInteger(words[1])) {
                boolean isDone = words[0].equals("mark");
                int taskIndex = Integer.parseInt(words[1]);
                speak(setTaskDoneWithIndex(taskIndex, words[0], isDone));
            } else {
                speak("Action failed: task index input is not an integer");
            }
            return;
        }

        if (words[0].equals("delete")) {
            if (isInteger(words[1])) {
                int taskIndex = Integer.parseInt(words[1]);
                Task deletedTask = deleteTask(taskIndex);
                speak("Noted. I've removed this task:"
                        + "\n"
                        + "    "
                        + deletedTask
                        + "\n"
                        + "Now you have " + getNumOfTasks() + " tasks in the list.");//input);
                return;
            } else {
                speak("Action failed: task index input is not an integer");
            }
            return;
        }

        if (words[0].equals("todo")
                || words[0].equals("deadline")
                || words[0].equals("event")) {

            Task newTask = null;
            newTask = Task.createTask(words[0], input);
            addTask(newTask);
            speak("Got it. I've added this task:"
                    + "\n"
                    + "    "
                    + newTask
                    + "\n"
                    + "Now you have " + getNumOfTasks() + " tasks in the list.");//input);
            return;
        }


        if (input.equals("bye")) {
            EndSession();
            saveToMemory();
            return;
        }

        if (input.equals("list")) {
            listTasks();
            return;
        }

        if (input.equals("print tasks")) {
            speak(tasks.toString());
            return;
        }

        throw new CommandNotFoundException(input);
    }

    private void speak(String output) {
        ToggleConversationState();
        System.out.println(output);
        ToggleConversationState();
    }

    private void listTasks() {
        ToggleConversationState();
        System.out.println("Here are the tasks in your list:");
        for (int i = 1; i <= getNumOfTasks(); i++) {
            System.out.println(i + "." + tasks.get(i - 1).toString());
        }
        ToggleConversationState();
    }

    private void addTask(Task task) {
        tasks.add(task);
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // takes in the listed index of the task (1 larger than storage index)
    private String setTaskDoneWithIndex(int index, String isDoneUpdateString, boolean isDone)
            throws TaskIndexOutOfBoundsException {
        try {
            tasks.get(index - 1).setIsDone(isDoneUpdateString, isDone);
            return tasks.get(index - 1).updateIsDoneMessage();
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw new TaskIndexOutOfBoundsException(index);
        }
    }

    private Task deleteTask(int index) throws TaskIndexOutOfBoundsException {
        try {
            Task deletedTask = tasks.remove(index - 1);
            return deletedTask;
        } catch (IndexOutOfBoundsException e) {
            throw new TaskIndexOutOfBoundsException(index);
        }
    }
}
