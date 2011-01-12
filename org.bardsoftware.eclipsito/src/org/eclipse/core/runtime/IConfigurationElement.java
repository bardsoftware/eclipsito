package org.eclipse.core.runtime;

public interface IConfigurationElement {
    
	public Object createExecutableExtension(String propertyName) throws CoreException;

	public String getAttribute(String name);
	public String[] getAttributeNames();

	public IConfigurationElement[] getChildren();
	public IConfigurationElement[] getChildren(String name);

	public IExtension getDeclaringExtension();

	public Object getParent();

	public String getName();
	public String getValue();

}