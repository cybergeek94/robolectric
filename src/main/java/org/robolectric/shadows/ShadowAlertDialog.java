package org.robolectric.shadows;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;
import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.type;
import static org.robolectric.Robolectric.*;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlertDialog.class)
public class ShadowAlertDialog extends ShadowDialog {
    @RealObject
    private AlertDialog realAlertDialog;

    private CharSequence[] items;
    private DialogInterface.OnClickListener clickListener;
    private boolean isMultiItem;
    private boolean isSingleItem;
    private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
//    private boolean[] checkedItems;
//    private int checkedItemIndex;
    private FrameLayout custom;

    /**
     * Non-Android accessor.
     *
     * @return the most recently created {@code AlertDialog}, or null if none has been created during this test run
     */
    public static AlertDialog getLatestAlertDialog() {
        ShadowAlertDialog dialog = Robolectric.getShadowApplication().getLatestAlertDialog();
        return dialog == null ? null : dialog.realAlertDialog;
    }

    public FrameLayout getCustomView() {
        if (custom == null) {
            custom = new FrameLayout(context);
        }
        return custom;
    }

//    @Implementation
//    public void setView(View view) {
//        this.view = view;
//    }

    /**
     * Resets the tracking of the most recently created {@code AlertDialog}
     */
    public static void reset() {
        getShadowApplication().setLatestAlertDialog(null);
    }

    /**
     * Simulates a click on the {@code Dialog} item indicated by {@code index}. Handles both multi- and single-choice dialogs, tracks which items are currently
     * checked and calls listeners appropriately.
     *
     * @param index the index of the item to click on
     */
    public void clickOnItem(int index) {
        shadowOf(realAlertDialog.getListView()).performItemClick(index);
    }

//    @Implementation
//    public Button getButton(int whichButton) {
//        switch (whichButton) {
//            case AlertDialog.BUTTON_POSITIVE:
//                return positiveButton;
//            case AlertDialog.BUTTON_NEGATIVE:
//                return negativeButton;
//            case AlertDialog.BUTTON_NEUTRAL:
//                return neutralButton;
//        }
//        throw new RuntimeException("Only positive, negative, or neutral button choices are recognized");
//    }
//
//    @Implementation
//    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener) {
//        switch (whichButton) {
//            case AlertDialog.BUTTON_POSITIVE:
//                positiveButton = createButton(context, realAlertDialog, whichButton, text, listener);
//                return;
//            case AlertDialog.BUTTON_NEGATIVE:
//                negativeButton = createButton(context, realAlertDialog, whichButton, text, listener);
//                return;
//            case AlertDialog.BUTTON_NEUTRAL:
//                neutralButton = createButton(context, realAlertDialog, whichButton, text, listener);
//                return;
//        }
//        throw new RuntimeException("Only positive, negative, or neutral button choices are recognized");
//    }


    @Override public CharSequence getTitle() {
        return getShadowAlertController().getTitle();
    }

//    private static Button createButton(final Context context, final DialogInterface dialog, final int which, CharSequence text, final DialogInterface.OnClickListener listener) {
//        if (text == null && listener == null) {
//            return null;
//        }
//        Button button = new Button(context);
//        button.setText(text);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (listener != null) {
//                    listener.onClick(dialog, which);
//                }
//                dialog.dismiss();
//            }
//        });
//        return button;
//    }

//    @Implementation
//    public ListView getListView() {
//        if (listView == null) {
//            listView = new ListView(context);
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    if (isMultiItem) {
//                        checkedItems[position] = !checkedItems[position];
//                        multiChoiceClickListener.onClick(realAlertDialog, position, checkedItems[position]);
//                    } else {
//                        if (isSingleItem) {
//                            checkedItemIndex = position;
//                        }
//                        clickListener.onClick(realAlertDialog, position);
//                    }
//                }
//            });
//        }
//        return listView;
//    }

    /**
     * Non-Android accessor.
     *
     * @return the items that are available to be clicked on
     */
    public CharSequence[] getItems() {
        Adapter adapter = getShadowAlertController().getAdapter();
        int count = adapter.getCount();
        CharSequence[] items = new CharSequence[count];
        for (int i = 0; i < items.length; i++) {
            items[i] = (CharSequence) adapter.getItem(i);
        }
        return items;
    }

    public Adapter getAdapter() {
        return getShadowAlertController().getAdapter();
    }

    /**
     * Non-Android accessor.
     *
     * @return the message displayed in the dialog
     */
    public CharSequence getMessage() {
        return getShadowAlertController().getMessage();
    }

//    @Implementation
//    public void setMessage(CharSequence message) {
//        this.message = (message == null ? null : message.toString());
//    }

//    /**
//     * Non-Android accessor.
//     *
//     * @return an array indicating which items are and are not clicked on a multi-choice dialog
//     */
//    public boolean[] getCheckedItems() {
//        return checkedItems;
//    }
//
//    /**
//     * Non-Android accessor.
//     *
//     * @return return the index of the checked item clicked on a single-choice dialog
//     */
//    public int getCheckedItemIndex() {
//        return checkedItemIndex;
//    }

    @Implementation
    public void show() {
        directlyOn(realAlertDialog, AlertDialog.class).show();
        super.show();
        getShadowApplication().setLatestAlertDialog(this);
    }

    /**
     * Non-Android accessor.
     *
     * @return return the view set with {@link AlertDialog.Builder#setView(View)}
     */
    public View getView() {
        return getShadowAlertController().getView();
    }

    /**
     * Non-Android accessor.
     *
     * @return return the view set with {@link AlertDialog.Builder#setCustomTitle(View)}
     */
    public View getCustomTitleView() {
        return getShadowAlertController().getCustomTitleView();
    }

    public ShadowAlertController getShadowAlertController() {
        return shadowOf_(
                field("mAlert")
                        .ofType(type(ShadowAlertController.ALERT_CONTROLLER_CLASS_NAME).load())
                        .in(realAlertDialog).get());
    }

    /**
     * Shadows the {@code android.app.AlertDialog.Builder} class.
     */
    @Implements(AlertDialog.Builder.class)
    public static class ShadowBuilder {
    }
}
