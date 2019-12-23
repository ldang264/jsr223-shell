package jsr223.shell;

import org.apache.commons.exec.CommandLine;

import java.io.File;

public interface Shell {

    CommandLine createByFile(File file);

    CommandLine createByCommand(String command);

    String getInstalledVersionCommand();

    String getMajorVersionCommand();

    String getOutputStatement(String toDisplay);

    String getProgram(String... statements);

}
