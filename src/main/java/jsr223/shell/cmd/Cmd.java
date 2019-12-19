package jsr223.shell.cmd;

import jsr223.shell.Shell;
import org.apache.commons.exec.CommandLine;

public class Cmd implements Shell {

    @Override
    public CommandLine createByCommand(String command) {
        return new CommandLine("cmd").addArgument("/c").addArgument(command);
    }

    @Override
    public String getInstalledVersionCommand() {
        return "echo|set /p=%CmdExtVersion%";
    }

    @Override
    public String getMajorVersionCommand() {
        return getInstalledVersionCommand();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "echo " + toDisplay;
    }

    @Override
    public String getProgram(String... statements) {
        String program = "";
        for (String statement : statements) {
            program += statement + "\n";
        }
        return program;
    }

}
