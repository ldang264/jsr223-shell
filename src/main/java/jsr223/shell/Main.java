package jsr223.shell;

import jsr223.shell.bash.Bash;
import jsr223.shell.cmd.Cmd;

import javax.script.ScriptException;

public class Main {

    public static void main(String[] args) throws ScriptException {
        Shell shell = null;
        if ("cmd".equals(args[0])) {
            shell = new Cmd();
        } else if ("bash".equals(args[0])) {
            shell = new Bash();
        } else {
            System.err.println("First argument must be shell name (cmd/bash)");
            System.exit(-1);
        }

        String script = "";
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            script += arg + " ";
        }

        Object returnCode = new ShellEngine(shell).eval(script);
        System.exit((Integer) returnCode);
    }
}
