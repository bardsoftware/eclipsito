package org.bardsoftware.impl.eclipsito;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bardsoftware.eclipsito.Boot;

public class DependencyResolver {
    
    private final MarkedDescriptor[] myMarkedDescriptors;
    
    public DependencyResolver(PluginDescriptor[] descriptors) {
        if (descriptors == null) {
            throw new IllegalArgumentException("Cannot resolve null descriptors");
        }
        myMarkedDescriptors = new MarkedDescriptor[descriptors.length];
        for (int i=0; i<descriptors.length; i++) {
            myMarkedDescriptors[i] = new MarkedDescriptor(descriptors[i]);
        }
    }
    
    public PluginDescriptor[] resolveAll() {
        ArrayList result = new ArrayList();
        for (int i=0; i<myMarkedDescriptors.length; i++) {
            markAndSweep(myMarkedDescriptors[i]);
        }
        for (int i=0; i<myMarkedDescriptors.length; i++) {
            if (myMarkedDescriptors[i].isResolved()) {
                result.add(myMarkedDescriptors[i].myDescriptor);
            }
        }
        return (PluginDescriptor[]) result.toArray(new PluginDescriptor[result.size()]);
    }
    
    private boolean markAndSweep(MarkedDescriptor marked) {
        boolean result = false;
        if (marked.isMarked()){
            result = false;
        } else if (marked.isResolved()) {
            result = true;
        } else {
            // descriptor has not been explored yet
            marked.mark();
            String[] children = marked.myDescriptor.getRequiredPluginIds();
            boolean allChildrenResolved = true;
            for (int i=0; children != null && i<children.length; i++) {
                MarkedDescriptor child = getMarkedById(children[i]);
                if (child == null) {
                    Boot.LOG.log(Level.WARNING, "Found unknown required plugin "+children[i]+
                            ",\n    please, correct "+marked.myDescriptor.myLocationUrl.getPath()+
                            ",\n    plugin "+marked.myDescriptor.getId()+" is ignored!");
                    allChildrenResolved = false;
                    break;
                } else if (!markAndSweep(child)) {
                    Boot.LOG.log(Level.WARNING, "Found dependency cycle plugin "+children[i]+
                            ",\n    please, correct "+marked.myDescriptor.myLocationUrl.getPath()+
                    		",\n    plugin "+marked.myDescriptor.getId()+" is ignored!");
                    allChildrenResolved = false;
                    break;
                }
            }
            if (allChildrenResolved) {
                marked.resolve();
                result = true;
            }
        }
        return result;
    }
    
    private MarkedDescriptor getMarkedById(String id) {
        for (int i=0; i<myMarkedDescriptors.length; i++) {
            if (myMarkedDescriptors[i].myDescriptor.getId().equals(id)) {
                return myMarkedDescriptors[i];
            }
        }
        return null;
    }

    public PluginDescriptor[] getUsingBundles(PluginDescriptor descriptor) {
        ArrayList result = new ArrayList();
        for (int i=0; i<myMarkedDescriptors.length; i++) {
            MarkedDescriptor marked = myMarkedDescriptors[i];
            if (marked.myDescriptor != descriptor && marked.isResolved()) {
                String[] childrendIds = marked.myDescriptor.getRequiredPluginIds();
                for (int j=0; childrendIds != null && j<childrendIds.length; j++) {
                    if (marked.myDescriptor.getId().equals(childrendIds[j])) {
                        result.add(marked.myDescriptor);
                        break; // out of the nearest loop only
                    }
                }
            }
        }
        return (PluginDescriptor[]) result.toArray(new PluginDescriptor[result.size()]);
    }
    
    protected static class MarkedDescriptor {
        private static byte INIT = 0;
        private static byte MARKED = 1;
        private static byte RESOLVED = 2;
        
        public final PluginDescriptor myDescriptor;
        private byte myState;

        public MarkedDescriptor(PluginDescriptor descriptor) {
            myDescriptor = descriptor;
            myState = INIT;
        }
        
        public void mark() {
            myState = MARKED;
        }
        
        public void resolve() {
            myState = RESOLVED;
        }
        
        public boolean isMarked() {
            return myState == MARKED;
        }
        
        public boolean isResolved() {
            return myState == RESOLVED;
        }
        
    }

}
