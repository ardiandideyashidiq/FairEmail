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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogAlias extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        Bundle args = getArguments();
        String initialName = (args != null ? args.getString("name") : null);
        String initialEmail = (args != null ? args.getString("email") : null);

        final DialogAliasView aliasView = new DialogAliasView(
                context,
                initialName,
                initialEmail,
                new Runnable() {
                    @Override
                    public void run() {
                        Helper.viewFAQ(context, 9);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dialog = (AlertDialog) getDialog();
                        if (dialog != null) {
                            Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            if (btnOk != null)
                                btnOk.performClick();
                        }
                    }
                }
        );

        return new AlertDialog.Builder(context)
                .setView(aliasView.getComposeView())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        args.putString("name", aliasView.getName());
                        args.putString("email", aliasView.getEmail());

                        new SimpleTask<EntityIdentity>() {
                            @Override
                            protected EntityIdentity onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");
                                String name = args.getString("name");
                                String email = args.getString("email");

                                DB db = DB.getInstance(context);

                                EntityIdentity identity = db.identity().getIdentity(id);
                                if (identity == null)
                                    return null;

                                identity.id = null;
                                identity.name = name;
                                identity.email = email;
                                identity.primary = false;
                                identity.sign_key = null;
                                identity.sign_key_alias = null;
                                identity.error = null;
                                identity.last_connected = null;
                                identity.id = db.identity().insertIdentity(identity);

                                return identity;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentDialogAlias.this, args, "create:alias");

                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
