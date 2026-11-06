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
import android.view.HapticFeedbackConstants;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FragmentMoveAsk extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String notagain = getArguments().getString("notagain");
        ArrayList<FragmentMessages.MessageTarget> result = getArguments().getParcelableArrayList("result");

        final Context context = getContext();

        String question = context.getResources()
                .getQuantityString(R.plurals.title_moving_messages,
                        result.size(), result.size());

        List<String> sources = new ArrayList<>();
        List<String> targets = new ArrayList<>();
        Integer sourceColor = null;
        Integer targetColor = null;
        boolean junk = false;
        for (FragmentMessages.MessageTarget t : result) {
            if (!sources.contains(t.sourceFolder.type))
                sources.add(t.sourceFolder.type);
            if (!targets.contains(t.targetFolder.type))
                targets.add(t.targetFolder.type);
            if (sourceColor == null)
                sourceColor = t.sourceFolder.color;
            if (targetColor == null)
                targetColor = t.targetFolder.color;
            if (!junk &&
                    (EntityFolder.JUNK.equals(t.sourceFolder.type) ||
                            EntityFolder.JUNK.equals(t.targetFolder.type)))
                junk = true;
        }

        int sourceIcon = sources.size() == 1 ? EntityFolder.getIcon(sources.get(0)) : R.drawable.twotone_folders_24;
        if (sources.size() == 1 && sourceColor == null)
            sourceColor = EntityFolder.getDefaultColor(sources.get(0), context);
        else if (sources.size() != 1)
            sourceColor = null;

        int targetIcon = targets.size() == 1 ? EntityFolder.getIcon(targets.get(0)) : R.drawable.twotone_folders_24;
        if (targets.size() == 1 && targetColor == null)
            targetColor = EntityFolder.getDefaultColor(targets.get(0), context);
        else if (targets.size() != 1)
            targetColor = null;

        DialogAskMoveView dialogView = new DialogAskMoveView(
                context,
                question,
                getNames(result, false),
                getNames(result, true),
                sourceIcon,
                targetIcon,
                sourceColor,
                targetColor,
                notagain != null,
                junk,
                isChecked -> {
                    if (notagain != null) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        prefs.edit().putBoolean(notagain, isChecked).apply();
                    }
                    return null;
                }
        );

        View dview = dialogView.getComposeView();
        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helper.performHapticFeedback(dview, HapticFeedbackConstants.CONFIRM);
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

    static String getNames(ArrayList<FragmentMessages.MessageTarget> result, boolean dest) {
        boolean across = false;
        for (FragmentMessages.MessageTarget target : result)
            if (target.isAcross())
                across = true;

        Map<String, Integer> nameCount = new HashMap<>();
        for (FragmentMessages.MessageTarget target : result) {
            String name = "";
            if (across)
                name += (dest ? target.targetAccount.name : target.sourceAccount.name) + "/";
            name += (dest ? target.targetFolder.display : target.sourceFolder.display);
            if (!nameCount.containsKey(name))
                nameCount.put(name, 0);
            nameCount.put(name, nameCount.get(name) + 1);
        }

        List<String> keys = new ArrayList(nameCount.keySet());

        Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
        Collections.sort(keys, collator);

        NumberFormat NF = NumberFormat.getNumberInstance();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(keys.get(i));
            if (!dest && keys.size() > 1) {
                int count = nameCount.get(keys.get(i));
                sb.append('(').append(NF.format(count)).append(')');
            }
        }

        return sb.toString();
    }
}
