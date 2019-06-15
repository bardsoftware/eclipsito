package org.eclipse.core.runtime;

public interface IExtensionPoint {

	public IExtension getExtension(String extensionId);
	public IExtension[] getExtensions();

	public String getSimpleIdentifier();
	public String getUniqueIdentifier();

	public String getNamespace();
	public String getLabel();

	public String getSchemaReference();

    // this method should never be used - extension point has no nested config elements!
	public IConfigurationElement[] getConfigurationElements();

}
