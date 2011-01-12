package org.eclipse.core.runtime;

public interface IExtensionRegistry {
    
	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId);
	public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName);
	public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName, String extensionId);

	public IExtension getExtension(String extensionId);
	public IExtension getExtension(String extensionPointId, String extensionId);
	public IExtension getExtension(String namespace, String extensionPointName, String extensionId);
	public IExtension[] getExtensions(String namespace);

	public IExtensionPoint getExtensionPoint(String extensionPointId);
	public IExtensionPoint getExtensionPoint(String namespace, String extensionPointName);
	public IExtensionPoint[] getExtensionPoints(String namespace);
	public IExtensionPoint[] getExtensionPoints();

	public String[] getNamespaces();

}