package duke.task;

import java.time.LocalDateTime;

import duke.parser.MissingInputFieldException;
import duke.parser.Parser;
import duke.storage.Storage;
import duke.ui.Ui;


/**
 * Represents an event.
 */
public class Event extends Task {

    public static final String TYPE_STRING = "E";
    public static final String DELIMITER = "event|/from|/to";
    public static final String COMMAND = "event";
    private LocalDateTime eventStartTiming = null;
    private LocalDateTime eventEndTiming = null;

    /**
     * Returns an instance of event created with user input.
     *
     * @param input User specified input to create Event.
     * @throws MissingInputFieldException
     */
    public Event(String input) throws MissingInputFieldException {
        super(TaskType.EVENT);
        setDelimiter(DELIMITER);
        setCommand(COMMAND);
        setUpTask(input);
    }

    @Override
    public String getType() {
        return TYPE_STRING;
    }

    @Override
    public void setUpTask(String input) throws MissingInputFieldException {
        try {
            input = input.trim();
            if (!input.contains(getCommand())) {
                throw new RuntimeException("not todo");
            }
            String[] inputArray = Task.removeEmptyElements(input.split(getDelimiter()));
            description = inputArray[0].trim();
            eventStartTiming = Parser.parseDateAndTime(inputArray[1].trim());
            eventEndTiming = Parser.parseDateAndTime(inputArray[2].trim());
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            throw new MissingInputFieldException(type);
        }
    }

    @Override
    public String toString() {
        return "[" + getType() + "]" + "["
                + getIsDoneStatus() + "] "
                + description + " " + "(from: "
                + Ui.printTime(eventStartTiming) + " to: " + Ui.printTime(eventEndTiming) + ")";
    }

    @Override
    public String convertToDataRow() {
        return super.convertToDataRow() + storageDataStringSplitter + Storage.convertDateTimeForStorage(eventStartTiming)
                + storageDataStringSplitter + Storage.convertDateTimeForStorage(eventEndTiming);
    }
}