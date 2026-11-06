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
import android.os.Bundle;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class FragmentDialogAnswerButton extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String[] values = getResources().getStringArray(R.array.answerValues);

        String[] names = getResources().getStringArray(R.array.answerNames);

        String answer_default = prefs.getString("answer_single", "menu");
        int initialSingle = 0;
        for (int pos = 0; pos < values.length; pos++)
            if (values[pos].equals(answer_default)) {
                initialSingle = pos;
                break;
            }

        boolean reply_all = prefs.getBoolean("reply_all", false);
        String answer_action = prefs.getString("answer_action", reply_all ? "reply_all" : "reply");
        int initialLong = 0;
        for (int pos = 0; pos < values.length; pos++)
            if (values[pos].equals(answer_action)) {
                initialLong = pos;
                break;
            }

        final DialogAnswerButtonView answerButtonView = new DialogAnswerButtonView(
                context,
                Arrays.asList(names),
                initialSingle,
                initialLong
        );

        return new AlertDialog.Builder(getContext())
                .setView(answerButtonView.getComposeView())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("answer_single", values[answerButtonView.getSingleSelectedPosition()]).apply();
                        editor.putString("answer_action", values[answerButtonView.getLongSelectedPosition()]).apply();
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                })
                .create();
    }
}
