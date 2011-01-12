package org.bardsoftware.impl.eclipsito;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

// this class is never used for now, sorry
public class ServiceReferenceImpl implements ServiceReference {
    private final Bundle myDeclaringBundle;
    private final Bundle[] myUsingBundles;
    private final Map myProperties;
    
    ServiceReferenceImpl(Bundle declaringBundle, Bundle[] usingBundles, Map properties) {
        myDeclaringBundle = declaringBundle;
        myUsingBundles = usingBundles;
        myProperties = properties;
    }

    public Bundle getBundle() {
        return myDeclaringBundle;
    }

    public Bundle[] getUsingBundles() {
        return myUsingBundles;
    }

    public Object getProperty(String key) {
        return myProperties.get(key);
    }

    public String[] getPropertyKeys() {
        return (String[]) myProperties.keySet().toArray(new String[0]);
    }

}
