package org.smartregister.reveal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.widgets.EditTextFactory;

import org.json.JSONObject;
import org.smartregister.reveal.R;

import java.util.List;

import static org.smartregister.reveal.util.Constants.JsonForm.NO_PADDING;

/**
 * Created by samuelgithengi on 12/17/18.
 */
public class RevealEditTextFactory extends EditTextFactory {

    private boolean hasNoPadding;

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        hasNoPadding = new JSONObject(formFragment.getCurrentJsonState()).getJSONObject(stepName).optBoolean(NO_PADDING);
        return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    protected void attachLayout(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText, ImageView editButton) throws Exception {
        super.attachLayout(stepName, context, formFragment, jsonObject, editText, editButton);
        Configuration config = context.getResources().getConfiguration();
        int textSize = FormUtils.getValueFromSpOrDpOrPx(jsonObject.optString("text_size", String.valueOf(context.getResources().getDimension(com.vijay.jsonwizard.R
                .dimen.default_text_size))), context);
        if (config.smallestScreenWidthDp < 600) {
            editText.setFloatingLabelTextSize((int) (textSize * 1.6));
            editText.setTextSize(textSize);
        } else {
            editText.setFloatingLabelTextSize(textSize * 3);
            editText.setTextSize(textSize * 2);
        }
        editText.setGravity(Gravity.START);
        editText.setSingleLine(false);
    }

    @Override
    protected int getLayout() {
        if (hasNoPadding) {
            return R.layout.padded_item_edit_text;
        } else {
            return super.getLayout();
        }
    }
}
