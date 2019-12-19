package jsr223.shell.bash;

import jsr223.shell.ShellEngine;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.script.*;
import java.io.StringReader;
import java.io.StringWriter;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class BashScriptEngineTest {

    private ShellEngine scriptEngine;
    private StringWriter scriptOutput;
    private StringWriter scriptError;

    @Before
    public void runOnlyOnLinux() {
        assumeTrue(System.getProperty("os.name").contains("Linux"));
    }

    @Before
    public void setup() {
        scriptEngine = new ShellEngine(new Bash());
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
    public void evaluate_echo_command_no_new_line() throws Exception {
        Integer returnCode = (Integer) scriptEngine.eval("echo -n hello");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("hello", scriptOutput.toString().trim());
    }

    @Test(expected = ScriptException.class)
    public void evaluate_failing_command() throws Exception {
        scriptEngine.eval("nonexistingcommandwhatsoever");
    }

    @Test
    public void evaluate_use_bindings() throws Exception {
        scriptEngine.put("string", "aString");
        scriptEngine.put("integer", 42);
        scriptEngine.put("float", 42.0);

        Integer returnCode = (Integer) scriptEngine.eval("echo $string $integer $float");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("aString 42 42.0", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_use_bindings_arrays() throws Exception {
        scriptEngine.put("array", new String[]{"oneString", "anotherString", "thenAString"});
        scriptEngine.put("array_empty", new String[0]);
        scriptEngine.put("array_nulls", new String[]{null, null});

        Integer returnCode = (Integer) scriptEngine.eval("echo $array_0 $array_1 $array_2 $array_empty_0 $array_nulls_0");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("oneString anotherString thenAString", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_use_bindings_lists() throws Exception {
        scriptEngine.put("list", asList("oneString", "anotherString", "thenAString"));
        scriptEngine.put("list_empty", emptyList());
        scriptEngine.put("list_nulls", asList(null, null));

        Integer returnCode = (Integer) scriptEngine.eval("echo $list_0 $list_1 $list_2 $list_empty_0 $list_nulls_0");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("oneString anotherString thenAString", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_use_bindings_maps() throws Exception {
        scriptEngine.put("map", singletonMap("key", "value"));
        scriptEngine.put("map_empty", emptyMap());
        scriptEngine.put("map_nulls", singletonMap("key", null));

        Integer returnCode = (Integer) scriptEngine.eval("echo $map_key $map_empty_key $map_nulls_key");

        assertEquals(Integer.valueOf(0), returnCode);
        assertEquals("value", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_different_calls() throws Exception {
        assertEquals(Integer.valueOf(0), scriptEngine.eval("echo $string"));
        assertEquals(Integer.valueOf(0), scriptEngine.eval(new StringReader("echo $string")));
    }

    @Test
    public void null_binding() throws Exception {
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("var", null);

        scriptEngine.eval("echo $var", bindings);

        assertEquals("", scriptOutput.toString().trim());
    }

    @Test
    public void reading_input() throws Exception {
        StringReader stringInput = new StringReader("hello\n");
        scriptEngine.getContext().setReader(stringInput);
        stringInput.close();
        assertEquals(Integer.valueOf(0), scriptEngine.eval("cat"));
    }

    @Test
    public void evaluate_different_calls_with_bindings() throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("string", "aString");

        assertEquals(Integer.valueOf(0), scriptEngine.eval("echo $string", bindings));
        assertEquals(Integer.valueOf(0), scriptEngine.eval(new StringReader("echo $string"), bindings));
        assertEquals("aString\naString", scriptOutput.toString().trim());
    }

    @Test
    public void evaluate_different_calls_with_context() throws Exception {
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("string", "aString", ScriptContext.ENGINE_SCOPE);
        StringWriter contextOutput = new StringWriter();
        context.setWriter(contextOutput);

        assertEquals(Integer.valueOf(0), scriptEngine.eval("echo $string", context));
        assertEquals(Integer.valueOf(0), scriptEngine.eval(new StringReader("echo $string"), context));
        assertEquals("aString\naString", contextOutput.toString().trim());
    }

    @Ignore("slow")
    @Test
    public void evaluate_script_with_large_output() throws Exception {
        assertEquals(Integer.valueOf(0), scriptEngine.eval("for i in $(seq 10000); do echo $i; env; done"));
        assertTrue(scriptOutput.toString().contains("10000"));
    }

    @Test
    public void evaluate_large_script() throws Exception {
        String largeScript = "";
        for (int i = 0; i < 10000; i++) {
            largeScript += "echo aString" + i + "\n";
        }

        assertEquals(Integer.valueOf(0), scriptEngine.eval(largeScript));
        assertTrue(scriptOutput.toString().contains("aString4999"));
    }
}
