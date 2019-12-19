package jsr223.shell;

public class CommandResult {
    private int exitValue;

    private String outMessage;

    private String errorMessage;

    public int getExitValue() {
        return exitValue;
    }

    public CommandResult setExitValue(int exitValue) {
        this.exitValue = exitValue;
        return this;
    }

    public String getOutMessage() {
        return outMessage;
    }

    public CommandResult setOutMessage(String outMessage) {
        this.outMessage = outMessage;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public CommandResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
