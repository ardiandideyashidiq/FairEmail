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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogEditName extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final eu.faircode.email.ui.SingleInputDialogComposeView view =
                eu.faircode.email.ui.ComposeContentHelper.createEditNameDialogView(context);

        Bundle args = getArguments();
        view.setText(args.getString("name"));
        view.setChecked(args.getBoolean("primary"));
        view.setShowCheckbox(args.containsKey("primary"));
        view.requestInputFocus();

        view.setOnDoneListener(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = (AlertDialog) getDialog();
                if (dialog == null)
                    return;
                Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (btnOk == null)
                    return;
                btnOk.performClick();
            }
        });

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        args.putString("name", view.getText());
                        args.putBoolean("primary", view.isChecked());
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
