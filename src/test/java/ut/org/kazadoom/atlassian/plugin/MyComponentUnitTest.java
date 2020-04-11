package ut.org.kazadoom.atlassian.plugin;

import org.junit.Test;
import org.kazadoom.atlassian.plugin.api.MyPluginComponent;
import org.kazadoom.atlassian.plugin.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}