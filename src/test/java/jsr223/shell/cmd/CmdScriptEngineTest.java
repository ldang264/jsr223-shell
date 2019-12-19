package jsr223.shell.cmd;

import jsr223.shell.ShellEngine;
import jsr223.shell.ShellHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class CmdScriptEngineTest {

    private ShellEngine scriptEngine;
    private StringWriter scriptOutput;
    private StringWriter scriptError;

    @Before
    public void runOnlyOnWindows() {
        assumeTrue(System.getProperty("os.name").contains("Windows"));
    }

    @Before
    public void setup() {
        scriptEngine = new ShellEngine(new Cmd());
        scriptOutput = new StringWriter();
        scriptEngine.getContext().setWriter(scriptOutput);
        scriptError = new StringWriter();
        scriptEngine.getContext().setErrorWriter(scriptError);
    }

    @Test
    public void evaluate_echo_command() throws Exception {
        Integer returnCode = (Integer) scriptEngine.eval("echo hello");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("hello", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_echo_unicode_command() throws Exception {
        Integer returnCode = (Integer) scriptEngine.eval("echo 中国");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("中国", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_echo_command_array() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;
        bashScriptEngine.put("array", new String[]{"hello", "world"});
        Integer returnCode = (Integer) scriptEngine.eval("echo %array_0% %array_1%");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("hello world", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_echo_command_list() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;
        bashScriptEngine.put("list", Arrays.asList(new String[]{"hello", "world"}));
        Integer returnCode = (Integer) scriptEngine.eval("echo %list_0% %list_1%");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("hello world", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_echo_command_map() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tip", "hello world");
        bashScriptEngine.put("map", map);
        Integer returnCode = (Integer) scriptEngine.eval("echo %map_tip%");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("hello world", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_use_bindings() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;

        bashScriptEngine.put("string", "aString");
        bashScriptEngine.put("integer", 42);
        bashScriptEngine.put("float", 42.0);

        Integer returnCode = (Integer) bashScriptEngine.eval("echo %string% %integer% %float%");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("aString 42 42.0", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_ping() throws Exception {
        Integer returnCode = (Integer) scriptEngine.eval("ping -n 3 127.0.0.1");
        assertEquals(Integer.valueOf(0), returnCode);
        System.out.println(scriptOutput);
    }

    @Test
    public void evaluate_different_calls() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;

        assertEquals(0, bashScriptEngine.eval("echo %string%"));
        assertEquals(0, bashScriptEngine.eval(new StringReader("echo %string%")));
    }

    @Test
    public void evaluate_different_calls_with_bindings() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("string", "aString");

        assertEquals(0, bashScriptEngine.eval("echo %string%", bindings));
        assertEquals(0, bashScriptEngine.eval(new StringReader("echo %string%"), bindings));
        assertEquals("aString\naString", scriptOutput.toString().trim());
    }

    @Ignore("slow")
    @Test
    public void evaluate_script_with_large_output() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;

        assertEquals(0, bashScriptEngine.eval("FOR /L %%G IN (1,1,10000) DO echo %%G"));
        assertTrue(scriptOutput.toString().contains("10000"));
    }

    @Ignore("slow")
    @Test
    public void evaluate_large_script() throws Exception {
        ScriptEngine bashScriptEngine = scriptEngine;

        String largeScript = "";
        for (int i = 0; i < 5000; i++) {
            largeScript += "echo aString" + i + "\n";
        }

        assertEquals(0, bashScriptEngine.eval(largeScript));
        assertTrue(scriptOutput.toString().contains("aString4999"));
    }
}
