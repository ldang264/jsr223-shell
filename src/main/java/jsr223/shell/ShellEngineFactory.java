package jsr223.shell;

import jsr223.shell.bash.Bash;
import jsr223.shell.cmd.Cmd;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ShellEngineFactory implements ScriptEngineFactory {

    private static ShellHandler utilShellHandler;
    private static Shell utilShell;
    private static boolean isWin;
    private static final Map<String, Object> parameters = new HashMap<String, Object>();

    static {
        isWin = System.getProperty("os.name").toLowerCase().startsWith("win");
        utilShellHandler = new ShellHandler(createShell());
        parameters.put(ScriptEngine.NAME, "shell");
        parameters.put(ScriptEngine.ENGINE, "Shell interpreter");
        parameters.put(ScriptEngine.ENGINE_VERSION, utilShellHandler.getInstalledVersion());
        parameters.put(ScriptEngine.LANGUAGE, "Shell");
        parameters.put(ScriptEngine.LANGUAGE_VERSION, utilShellHandler.getMajorVersion());
    }

    @Override
    public String getEngineName() {
        return getParameter(ScriptEngine.NAME).toString();
    }

    @Override
    public String getEngineVersion() {
        return getParameter(ScriptEngine.ENGINE_VERSION).toString();
    }

    @Override
    public List<String> getExtensions() {
        return asList("sh", "bash", "bat");
    }

    @Override
    public List<String> getMimeTypes() {
        return asList(
                "application/x-sh",
                "application/x-bash",
                "application/x-cmd",
                "application/x-bat",
                "application/bat",
                "application/x-msdos-program",
                "application/textedit",
                "application/octet-stream");
    }

    @Override
    public List<String> getNames() {
        return asList(
                "shell",
                "bash", "sh", "Bash",
                "cmd", "bat", "Cmd", "Bat");
    }

    @Override
    public String getLanguageName() {
        return getParameter(ScriptEngine.LANGUAGE).toString();
    }

    @Override
    public String getLanguageVersion() {
        return getParameter(ScriptEngine.LANGUAGE_VERSION).toString();
    }

    @Override
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        String methodCall = m + " ";
        for (String arg : args) {
            methodCall += arg + " ";
        }
        return methodCall;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return utilShell.getOutputStatement(toDisplay);
    }

    @Override
    public String getProgram(String... statements) {
        return utilShell.getProgram(statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new ShellEngine(createShell());
    }

    private static Shell createShell() {
        if(isWin){
            return new Cmd();
        }
        return new Bash();
    }
}
