package jsr223.shell;

import jsr223.shell.util.IOUtil;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;

public class ShellEngine extends AbstractScriptEngine {

    private Shell shell;

    public ShellEngine(Shell shell) {
        this.shell = shell;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        CommandResult commandResult;
        try {
            commandResult = new ShellHandler(shell).run(script, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (commandResult.getExitValue() != 0) {
            throw new ScriptException("Script failed with exit code " + commandResult.getExitValue() + "\nError message:" + commandResult.getErrorMessage());
        }
        return commandResult.getExitValue();
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(IOUtil.toString(reader), context);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new ShellEngineFactory();
    }
}
