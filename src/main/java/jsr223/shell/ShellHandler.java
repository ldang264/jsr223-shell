package jsr223.shell;

import jsr223.shell.util.IOUtil;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShellHandler {

    public static final String SHELL = "shell_";

    public static final String UNIQUE_SEQUENCE_NO = "unique_sequence_no_";

    private static String charsetName = System.getProperty(SHELL + "charset_out", System.getProperty("sun.jnu.encoding"));

    private static int timeout = Integer.parseInt(System.getProperty(SHELL + "timeout", "7200000"));

    private static int limit = Integer.parseInt(System.getProperty(SHELL + "stdout_limit", "65536")); // 64K

    private static final String KEY_LANGUAGE = SHELL + "language";

    private static final String KEY_CHARSET_COMMAND = SHELL + "charset_command";

    private Shell shell;

    public ShellHandler(Shell shell) {
        this.shell = shell;
    }

    public String getInstalledVersion() {
        try {
            return runAndGetOutput(shell.getInstalledVersionCommand());
        } catch (Throwable e) {
            return "Could not determine version";
        }
    }

    public String getMajorVersion() {
        try {
            return runAndGetOutput(shell.getMajorVersionCommand());
        } catch (Throwable e) {
            return "Could not determine version";
        }
    }

    public CommandResult run(String command, ScriptContext scriptContext) throws IOException {
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        CommandLine commandLine;
        File temporaryFile = null;
        if (bindings.get(KEY_LANGUAGE) != null && bindings.get(KEY_LANGUAGE).toString().startsWith(".")) {
            String fileExtension = bindings.get(KEY_LANGUAGE).toString();
            temporaryFile = commandAsTemporaryFile(command, fileExtension, (String) bindings.get(KEY_CHARSET_COMMAND));
            commandLine = shell.createByFile(temporaryFile);
        } else {
            commandLine = shell.createByCommand(command);
        }
        Map<String, String> variables = new HashMap<String, String>(System.getenv());
        Map<String, String> bindingVariables = build(bindings);
        variables.putAll(bindingVariables);
        CommandResult commandResult = execute(commandLine, variables);
        IOUtil.pipe(commandResult.getOutMessage(), scriptContext.getWriter());
        IOUtil.pipe(commandResult.getErrorMessage(), scriptContext.getErrorWriter());
        resolveOut(commandResult.getOutMessage(), bindings);
        if (temporaryFile != null) {
            temporaryFile.delete();
        }
        return commandResult;
    }

    private void resolveOut(String outMessage, Bindings bindings) {
        if (outMessage != null) {
            for (final Map.Entry<String, Object> binding : bindings.entrySet()) {
                if (binding.getKey().startsWith(UNIQUE_SEQUENCE_NO)) {
                    System.setProperty(binding.getKey(), outMessage.length() > limit ? outMessage.substring(0, limit) : outMessage);
                }
            }
        }
    }

    private String runAndGetOutput(String command) throws IOException {
        CommandLine commandLine = shell.createByCommand(command);
        CommandResult commandResult = execute(commandLine, null);
        return commandResult.getOutMessage();
    }

    private CommandResult execute(CommandLine commandLine, Map<String, String> environment) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        executor.setWatchdog(watchdog);
        executor.setExitValues(null);
        final StringBuilder outSB = new StringBuilder();
        final StringBuilder errorSB = new StringBuilder();
        OutputStream outOS = null;
        OutputStream errorOS = null;
        try {
            outOS = new MyLogOutputStream(outSB, charsetName);
            errorOS = new MyLogOutputStream(errorSB, charsetName);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outOS, errorOS);
            executor.setStreamHandler(streamHandler);
        } finally {
            IOUtil.closeSilently(outOS);
            IOUtil.closeSilently(errorOS);
        }
        int exitValue = executor.execute(commandLine, environment);
        watchdog.destroyProcess();
        return new CommandResult().setExitValue(exitValue).setOutMessage(outSB.toString()).setErrorMessage(errorSB.toString());
    }

    private Map<String, String> build(Map<String, Object> engineScopeMap) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, Object> binding : engineScopeMap.entrySet()) {
            String bindingKey = binding.getKey();
            Object bindingValue = binding.getValue();

            if (bindingValue instanceof Object[]) {
                addArrayBindingAsEnvironmentVariable(bindingKey, (Object[]) bindingValue, map);
            } else if (bindingValue instanceof Collection) {
                addCollectionBindingAsEnvironmentVariable(bindingKey, (Collection) bindingValue, map);
            } else if (bindingValue instanceof Map) {
                addMapBindingAsEnvironmentVariable(bindingKey, (Map<?, ?>) bindingValue, map);
            } else {
                map.put(bindingKey, toEmpty(binding.getValue()));
            }
        }
        return map;
    }

    private void addMapBindingAsEnvironmentVariable(String bindingKey, Map<?, ?> bindingValue, Map<String, String> environment) {
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) bindingValue).entrySet()) {
            environment.put(bindingKey + "_" + entry.getKey(), (entry.getValue() == null ? "" : toEmpty(entry.getValue())));
        }
    }

    private void addCollectionBindingAsEnvironmentVariable(String bindingKey, Collection bindingValue, Map<String, String> environment) {
        Object[] bindingValueAsArray = bindingValue.toArray();
        addArrayBindingAsEnvironmentVariable(bindingKey, bindingValueAsArray, environment);
    }

    private void addArrayBindingAsEnvironmentVariable(String bindingKey, Object[] bindingValue, Map<String, String> environment) {
        for (int i = 0; i < bindingValue.length; i++) {
            environment.put(bindingKey + "_" + i, (bindingValue[i] == null ? "" : toEmpty(bindingValue[i].toString())));
        }
    }

    private File commandAsTemporaryFile(String command, String fileExtension, String commandCharset) {
        PrintWriter pw = null;
        try {
            File commandAsFile = File.createTempFile(SHELL, fileExtension);
            pw = commandCharset == null ? new PrintWriter(commandAsFile) :
                    new PrintWriter(new OutputStreamWriter(new FileOutputStream(commandAsFile), commandCharset));
            pw.print(command);
            pw.flush();
            return commandAsFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private String toEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
