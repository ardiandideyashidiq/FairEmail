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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;


public class FragmentDialogExport extends FragmentDialogBase {
    private DialogExportView exportView;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fair:password1", exportView == null ? null : exportView.getPassword1());
        outState.putString("fair:password2", exportView == null ? null : exportView.getPassword2());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        String initialPassword1 = savedInstanceState != null ? savedInstanceState.getString("fair:password1") : null;
        String initialPassword2 = savedInstanceState != null ? savedInstanceState.getString("fair:password2") : null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = (BuildConfig.DEBUG || prefs.getBoolean("debug", false));

        exportView = new DialogExportView(
                context,
                initialPassword1,
                initialPassword2,
                debug,
                valid -> {
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if (dialog != null) {
                        Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (btnOk != null)
                            btnOk.setEnabled(valid);
                    }
                },
                () -> {
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if (dialog != null) {
                        Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (btnOk != null && btnOk.isEnabled())
                            btnOk.performClick();
                    }
                }
        );

        Dialog dialog = new AlertDialog.Builder(context)
                .setView(exportView.getComposeView())
                .setPositiveButton(R.string.title_save_file, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ViewModelExport vme = new ViewModelProvider(getActivity()).get(ViewModelExport.class);
                        vme.setPassword(exportView.getPassword1());
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btnOk != null && exportView != null)
                btnOk.setEnabled(exportView.isValid());
        }
    }
}
