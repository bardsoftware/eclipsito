package org.bardsoftware.test.eclipsito;

import org.bardsoftware.impl.eclipsito.DescriptorParser;
import org.bardsoftware.impl.eclipsito.PluginDescriptor;
import org.eclipse.core.runtime.IModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.InvalidParameterException;


// sorry, this test is only a stub for now, i'm not sure it is wise to implement it now
public class TestDescriptorParser extends TestsEclipsitoBase {

    public void testSimpiestDescriptor() throws Exception, FactoryConfigurationError {
        String testAttr = "test";
        Element root = createPluginDocumentElement();
		root.setAttribute(IModel.PLUGIN_ID, testAttr);
		root.setAttribute(IModel.PLUGIN_NAME, testAttr);
		root.setAttribute(IModel.PLUGIN_VERSION, testAttr);
		PluginDescriptor descriptor = DescriptorParserMock.test(root);
		assertNotNull(descriptor);
		assertEquals(testAttr, descriptor.getId());
		assertEquals(testAttr, descriptor.getName());
		assertEquals(testAttr, descriptor.getVersion());
		assertEquals(0, descriptor.getClassName().length());
		assertEquals(0, descriptor.getProviderName().length());
		assertEquals(0, descriptor.getRuntimeLibraries().length);
		assertEquals(0, descriptor.getRequiredPluginIds().length);
		assertEquals(0, descriptor.getExtensionPointDescriptors().length);
		assertEquals(0, descriptor.getExtensions().length);
    }

    public void testMissingRequiredPluginAttributes() throws Exception, FactoryConfigurationError {
        Element root = createPluginDocumentElement();
        try {
            DescriptorParserMock.test(root);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    private Element createPluginDocumentElement() throws ParserConfigurationException, FactoryConfigurationError {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		return (Element) doc.appendChild(doc.createElement(IModel.PLUGIN));
    }

    private static class DescriptorParserMock extends DescriptorParser {
        public static PluginDescriptor test(Element pluginElement) throws MalformedURLException {
            URI pluginDescriptorUri = URI.create("file:///tmp");
            return constructPluginDescriptor(pluginElement, pluginDescriptorUri.toURL());
        }
    }
}
