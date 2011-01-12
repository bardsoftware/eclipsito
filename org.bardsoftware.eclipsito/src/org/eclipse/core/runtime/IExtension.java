package org.eclipse.core.runtime;

public interface IExtension {
    
	public IConfigurationElement[] getConfigurationElements();
	
	public String getExtensionPointUniqueIdentifier();

	public String getLabel();
	public String getUniqueIdentifier();

	public String getNamespace();
	public String getSimpleIdentifier();
	
}