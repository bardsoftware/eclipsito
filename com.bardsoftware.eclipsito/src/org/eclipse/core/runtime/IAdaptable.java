/*
 * Created on 26.05.2005
 */
package org.eclipse.core.runtime;

/**
 * @author bard
 */
public interface IAdaptable {
    Object getAdapter(Class adapter);
}
