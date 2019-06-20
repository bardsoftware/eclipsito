package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Launch;
import com.bardsoftware.eclipsito.PluginDescriptor;

import java.util.ArrayList;
import java.util.logging.Level;

public class DependencyResolver {

    private final MarkedDescriptor[] myMarkedDescriptors;

    DependencyResolver(PluginDescriptor[] descriptors) {
        if (descriptors == null) {
            throw new IllegalArgumentException("Cannot resolve null descriptors");
        }
        myMarkedDescriptors = new MarkedDescriptor[descriptors.length];
        for (int i=0; i<descriptors.length; i++) {
            myMarkedDescriptors[i] = new MarkedDescriptor(descriptors[i]);
        }
    }

    PluginDescriptor[] resolveAll() {
        ArrayList result = new ArrayList();
        for (int i=0; i<myMarkedDescriptors.length; i++) {
            markAndSweep(myMarkedDescriptors[i]);
        }
        for (int i=0; i<myMarkedDescriptors.length; i++) {
            if (myMarkedDescriptors[i].isResolved()) {
                result.add(myMarkedDescriptors[i].myDescriptor);
            }
        }
        return (PluginDescriptor[]) result.toArray(new PluginDescriptor[0]);
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
            for (String requiredPluginId : children) {
                MarkedDescriptor child = getMarkedById(requiredPluginId);
                if (child == null) {
                    Launch.LOG.log(Level.SEVERE,
                        String.format("Plugin %s which is required by %s not found. Plugin %s will be ignored",
                            requiredPluginId,
                            marked.myDescriptor.getId(),
                            marked.myDescriptor.getLocation()
                        ));
                    allChildrenResolved = false;
                    break;
                } else if (!markAndSweep(child)) {
                    Launch.LOG.log(Level.SEVERE,
                        String.format("It seems that dependency on plugin %s from plugin %s makes a cycle. Plugin %s will be ignored",
                            requiredPluginId,
                            marked.myDescriptor.getId(),
                            marked.myDescriptor.getLocation()
                        ));
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
        return (PluginDescriptor[]) result.toArray(new PluginDescriptor[0]);
    }

    protected static class MarkedDescriptor {
        private static byte INIT = 0;
        private static byte MARKED = 1;
        private static byte RESOLVED = 2;

        final PluginDescriptor myDescriptor;
        private byte myState;

        MarkedDescriptor(PluginDescriptor descriptor) {
            myDescriptor = descriptor;
            myState = INIT;
        }

        void mark() {
            myState = MARKED;
        }

        void resolve() {
            myState = RESOLVED;
        }

        boolean isMarked() {
            return myState == MARKED;
        }

        boolean isResolved() {
            return myState == RESOLVED;
        }

    }

}
