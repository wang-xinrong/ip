package duke.task;

import java.time.LocalDateTime;

import duke.parser.MissingInputFieldException;
import duke.parser.Parser;
import duke.storage.Storage;
import duke.ui.Ui;

/**
 * Represents a deadline.
 */
public class Deadline extends Task {

    private LocalDateTime by = null;

    public Deadline(String input) throws MissingInputFieldException {
        super(TaskType.DEADLINE);
        delimiter = "deadline|/by";
        command = "deadline";
        setUpTask(input);
    }

    @Override
    public String printType() {
        return "D";
    }

    @Override
    public void setUpTask(String input) throws MissingInputFieldException {
        try {
            input = input.trim();
            if (!input.contains(command)) throw new RuntimeException("not deadline");
            String[] inputArray = Task.removeEmptyElements(input.split(delimiter));
            description = inputArray[0].trim();
            by = Parser.parseDateAndTime(inputArray[1].trim());
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            throw new MissingInputFieldException(type);
        }
    }

    @Override
    public String toString() {
        return "[" + printType() + "]" + "[" + getIsDoneStatus() + "] "
                + description + " " + "(by: " + Ui.printTime(by) + ")";
    }

    @Override
    public String convertToDataRow() {
        return super.convertToDataRow() + 0 + storageDataStringSplitter +
                Storage.convertDateTimeForStorage(by);
    }
}
