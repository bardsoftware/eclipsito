package org.bardsoftware.test.eclipsito;

import org.bardsoftware.impl.eclipsito.ModulesDirectoryProcessor;
import org.bardsoftware.impl.eclipsito.PluginDescriptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

// please, add eclipsito home directory to the classpath before running this test!
public class TestModulesDirectoryProcessor extends TestsEclipsitoBase {

    public void testEmptyContent() {
        File tempDirectory = getTempDirectory();
        File[] contents = tempDirectory.listFiles();
        assertEquals(0, contents.length);
        PluginDescriptor[] descriptors = ModulesDirectoryProcessor.process(tempDirectory, "");
        assertEquals(0, descriptors.length);
    }

    public void testNonExistingModulesDirectory() {
        File ghostFile = new File(getTempDirectory(), "ghost");
        assertFalse(ghostFile.exists());
        PluginDescriptor[] descriptors = ModulesDirectoryProcessor.process(ghostFile, "*");
        assertEquals(0, descriptors.length);
    }

    public void descriptorInRoot() throws IOException {
        String descrPattern = "test.descriptor";
        File descriptor = new File(getTempDirectory(), descrPattern);
        descriptor.createNewFile();
        fillFileWithTestContent(descriptor);
        assertTrue(descriptor.exists());
        PluginDescriptor[] descriptors = ModulesDirectoryProcessor.process(getTempDirectory(), descrPattern);
        assertEquals(0, descriptors.length);
    }

    public void testOneExistingModulesDescriptor() throws IOException {
        String descrPattern = "test.descriptor";
        File module = new File(getTempDirectory(), "test");
        module.mkdir();
        File descriptor = new File(module, descrPattern);
        descriptor.createNewFile();
        fillFileWithTestContent(descriptor);
        assertTrue(descriptor.exists());
        PluginDescriptor[] descriptors = ModulesDirectoryProcessor.process(getTempDirectory(), descrPattern);
        assertNotNull(descriptors);
        assertEquals(1, descriptors.length);
        descriptor.delete();
        module.delete();
    }

    public void testNExistingModulesDescriptors() throws IOException {
        int n = 3;
        String descrPattern = "test.descriptor";
        File[] moduleDirectories = new File[n];
        File[] moduleDescriptors = new File[n];
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            moduleDirectories[i] = new File(getTempDirectory(), "test"+rnd.nextLong());
            moduleDirectories[i].mkdir();
            moduleDescriptors[i] = new File(moduleDirectories[i], descrPattern);
            moduleDescriptors[i].createNewFile();
            fillFileWithTestContent(moduleDescriptors[i]);
        }
        PluginDescriptor[] descriptors = ModulesDirectoryProcessor.process(getTempDirectory(), descrPattern);
        assertEquals(n, descriptors.length);
        for (int i = 0; i < n; i++) {
            moduleDescriptors[i].delete();
            moduleDirectories[i].delete();
        }
    }

    private void fillFileWithTestContent(File descriptor) throws IOException {
        FileWriter writer = new FileWriter(descriptor);
        writer.write(TEST_CONTENT_0);
        writer.close();
    }

    private File getTempDirectory() {
        File result = new File(getClass().getClassLoader().getResource("modules").getPath(), "test");
        result.mkdir();
        return result;
    }

    private static final String TEST_CONTENT_0 = "<plugin id='id' name='name' version='version'/>";
}
