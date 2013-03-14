package org.robolectric.shadows;

import android.widget.BaseAdapter;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = BaseAdapter.class, callThroughByDefault = true)
public class ShadowBaseAdapter {
	@RealObject private BaseAdapter realBaseAdapter;
//    private final List<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();
    private boolean wasNotifyDataSetChangedCalled;

//    @Implementation
//    public boolean isEmpty() {
//    	return realBaseAdapter.getCount() == 0;
//    }
//
//    /**
//     * Just returns true
//     *
//     * @return true
//     */
//    @Implementation
//    public boolean areAllItemsEnabled() {
//        return true;
//    }
//
//    /**
//     * Registers the observer.
//     *
//     * @param observer observer
//     */
//    @Implementation
//    public void registerDataSetObserver(DataSetObserver observer) {
//        dataSetObservers.add(observer);
//    }
//
//    /**
//     * Unregisters the observer if it can be found. Nothing otherwise.
//     *
//     * @param observer observer
//     */
//    @Implementation
//    public void unregisterDataSetObserver(DataSetObserver observer) {
//        dataSetObservers.remove(observer);
//    }
//
//    /**
//     * Notifies the registered observers
//     */
    @Implementation
    public void notifyDataSetChanged() {
        wasNotifyDataSetChangedCalled = true;
        directlyOn(realBaseAdapter, BaseAdapter.class, "notifyDataSetChanged").invoke();
    }

//
//    /**
//     * Notifies the registered observers
//     */
//    @Implementation
//    public void notifyDataSetInvalidated() {
//        for (DataSetObserver dataSetObserver : dataSetObservers) {
//            dataSetObserver.onInvalidated();
//        }
//    }

    public void clearWasDataSetChangedCalledFlag() {
        wasNotifyDataSetChangedCalled = false;
    }

    public boolean wasNotifyDataSetChangedCalled() {
        return wasNotifyDataSetChangedCalled;
    }
}
