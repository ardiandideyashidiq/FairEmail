package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2026 by Marcel Bokhorst (M66B)
*/

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class FragmentDialogAsk extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String question = args.getString("question");
        String remark = args.getString("remark");
        String notagain = args.getString("notagain");
        String accept = args.getString("accept");
        boolean warning = args.getBoolean("warning");
        int faq = args.getInt("faq");

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final DialogAskAgainView askAgainView = new DialogAskAgainView(
                context,
                question,
                remark,
                accept,
                notagain != null,
                warning,
                faq != 0 ? () -> Helper.viewFAQ(context, faq) : null
        );

        EntityLog.log(context, "Ask " + TextUtils.join(" ", Log.getExtras(args)));

        return new AlertDialog.Builder(context)
                .setView(askAgainView.getComposeView())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EntityLog.log(context, "Ask confirmed");
                        if (notagain != null)
                            prefs.edit().putBoolean(notagain, askAgainView.isNotAgain()).apply();
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EntityLog.log(context, "Ask canceled");
                        sendResult(Activity.RESULT_CANCELED);
                    }
                })
                .create();
    }
}
