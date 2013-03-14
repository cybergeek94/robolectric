package org.robolectric.shadows;

import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = ListView.class, callThroughByDefault = true)
public class ShadowListView extends ShadowAbsListView {
    @RealObject private ListView realListView;

//    private boolean itemsCanFocus;
//    private List<View> headerViews = new ArrayList<View>();
//    private List<View> footerViews = new ArrayList<View>();
//
//    private int choiceMode;
//    private SparseBooleanArray checkedItemPositions = new SparseBooleanArray();
//
//    @Implementation
//    public View findViewTraversal(int id) {
//        View child = method("findViewTraversal").withReturnType(View.class).withParameterTypes(int.class)
//                .in(directlyOn(realListView, ListView.class)).invoke(id);
//        if (child == null) {
//            child = findView(headerViews, id);
//
//            if (child == null) {
//                child = findView(footerViews, id);
//            }
//        }
//        return child;
//    }
//
//    private View findView(List<View> views, int viewId) {
//        View child = null;
//        for (View v : views) {
//            child = v.findViewById(viewId);
//            if (child != null) {
//                break;
//            }
//        }
//        return child;
//    }
//
//
//    @Implementation
//    public void setItemsCanFocus(boolean itemsCanFocus) {
//        this.itemsCanFocus = itemsCanFocus;
//    }
//
//    @Override @Implementation public Adapter getAdapter() {
//        return super.getAdapter();
//    }
//
//    @Override @Implementation
//    public void setSelection(int position) {
//        super.setSelection(position);
//    }
//
//    @Implementation
//    @Override
//    public boolean performItemClick(View view, int position, long id) {
//        boolean handled = false;
//        if (choiceMode != ListView.CHOICE_MODE_NONE) {
//            handled = true;
//
//            if (choiceMode == ListView.CHOICE_MODE_MULTIPLE) {
//                boolean newValue = !checkedItemPositions.get(position, false);
//                checkedItemPositions.put(position, newValue);
//            } else {
//                boolean newValue = !checkedItemPositions.get(position, false);
//                if (newValue) {
//                	checkedItemPositions.clear();
//                	checkedItemPositions.put(position, true);
//                }
//            }
//        }
//
//        handled |= super.performItemClick(view, position, id);
//        return handled;
//    }
//
//    @Implementation
//    public void setAdapter(ListAdapter adapter) {
//        super.setAdapter(adapter);
//    }
//
//    @Implementation
//    public void addHeaderView(View headerView) {
//        addHeaderView(headerView, null, true);
//    }
//
//    @Implementation
//    public void addHeaderView(View headerView, Object data, boolean isSelectable) {
//        ensureAdapterNotSet("header");
//        headerViews.add(headerView);
//        realListView.addView(headerView);
//    }
//
//    @Implementation
//    public int getHeaderViewsCount() {
//        return headerViews.size();
//    }
//
//    @Implementation
//    public int getFooterViewsCount() {
//        return footerViews.size();
//    }
//
//    @Implementation
//    public void addFooterView(View footerView, Object data, boolean isSelectable) {
//        ensureAdapterNotSet("footer");
//        footerViews.add(footerView);
//        realListView.addView(footerView);
//    }
//
//    @Implementation
//    public void addFooterView(View footerView) {
//        addFooterView(footerView, null, false);
//    }
//
//    private void ensureAdapterNotSet(String view) {
//        if (getAdapter() != null) {
//            throw new IllegalStateException("Cannot add " + view + " view to list -- setAdapter has already been called");
//        }
//    }
//
//    public boolean isItemsCanFocus() {
//        return itemsCanFocus;
//    }

    public List<View> getHeaderViews() {
        HeaderViewListAdapter adapter = (HeaderViewListAdapter) realListView.getAdapter();
        ArrayList<View> headerViews = new ArrayList<View>();
        int headersCount = adapter.getHeadersCount();
        for (int i = 0; i < headersCount; i++) {
            headerViews.add(adapter.getView(i, null, realListView));
        }
        return headerViews;
    }

//    public void setHeaderViews(List<View> headerViews) {
//        this.headerViews = headerViews;
//    }

    public List<View> getFooterViews() {
        HeaderViewListAdapter adapter = (HeaderViewListAdapter) realListView.getAdapter();
        ArrayList<View> footerViews = new ArrayList<View>();
        int offset = adapter.getHeadersCount() + adapter.getCount() - adapter.getFootersCount();
        int itemCount = adapter.getCount();
        for (int i = offset; i < itemCount; i++) {
            footerViews.add(adapter.getView(i, null, realListView));
        }
        return footerViews;
    }

    //    public void setFooterViews(List<View> footerViews) {
//        this.footerViews = footerViews;
//    }
//
//    @Override
//    protected void addViews() {
//        for (View headerView : headerViews) {
//            realListView.addView(headerView);
//        }
//
//        super.addViews();
//
//        for (View footerView : footerViews) {
//            realListView.addView(footerView);
//        }
//    }
//
//    @Implementation
//    public int getChoiceMode() {
//        return choiceMode;
//    }
//
//    @Implementation
//    public void setChoiceMode(int choiceMode) {
//        this.choiceMode = choiceMode;
//    }
//
//    @Implementation
//    public void setItemChecked(int position, boolean value) {
//        if (choiceMode == ListView.CHOICE_MODE_SINGLE) {
//            checkedItemPositions.clear();
//            checkedItemPositions.put(position, value);
//        } else if (choiceMode == ListView.CHOICE_MODE_MULTIPLE) {
//            checkedItemPositions.put(position, value);
//        }
//    }
//
//    @Implementation
//    public int getCheckedItemPosition() {
//        if (choiceMode != ListView.CHOICE_MODE_SINGLE || checkedItemPositions.size() != 1)
//            return ListView.INVALID_POSITION;
//
//        return checkedItemPositions.keyAt(0);
//    }
//
//    @Implementation
//    public SparseBooleanArray getCheckedItemPositions() {
//        if (choiceMode == ListView.CHOICE_MODE_NONE)
//            return null;
//
//        return checkedItemPositions;
//    }
}
