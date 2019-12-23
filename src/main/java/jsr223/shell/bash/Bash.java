package jsr223.shell.bash;

import jsr223.shell.Shell;
import org.apache.commons.exec.CommandLine;

import java.io.File;

public class Bash implements Shell {

    @Override
    public CommandLine createByFile(File file) {
        return new CommandLine("bash").addArgument(file.getAbsolutePath());
    }

    @Override
    public CommandLine createByCommand(String command) {
        return new CommandLine("bash").addArgument("-c").addArgument(command);
    }

    @Override
    public String getInstalledVersionCommand() {
        return "echo -n $BASH_VERSION";
    }

    @Override
    public String getMajorVersionCommand() {
        return "echo -n $BASH_VERSINFO";
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "echo -n " + toDisplay;
    }

    @Override
    public String getProgram(String... statements) {
        String program = "#!/bin/bash\n";
        for (String statement : statements) {
            program += statement + "\n";
        }
        return program;
    }

}
