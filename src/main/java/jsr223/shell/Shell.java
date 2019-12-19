package jsr223.shell;

import org.apache.commons.exec.CommandLine;

import javax.script.ScriptEngineFactory;

public interface Shell {

    CommandLine createByCommand(String command);

    String getInstalledVersionCommand();

    String getMajorVersionCommand();

    String getOutputStatement(String toDisplay);

    String getProgram(String... statements);

}
