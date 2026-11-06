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
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.compose.ui.platform.ComposeView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FragmentDialogSelectIdentity extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final Bundle args = getArguments();

        final AccountSelectDialogHelper helper = new AccountSelectDialogHelper(context);
        RecyclerView rvSelect = helper.rvSelect;
        View dview = helper.root;

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_person_24)
                .setTitle(R.string.title_list_identities)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null);

        if (args != null && args.getBoolean("add"))
            builder.setNeutralButton(R.string.title_add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    args.putLong("id", -1L);
                    sendResult(RESULT_OK);
                }
            });

        Dialog dialog = builder.create();

        new SimpleTask<List<TupleIdentityEx>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                helper.setLoading(true);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                helper.setLoading(false);
            }

            @Override
            protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.identity().getComposableIdentities(null);
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                AdapterIdentity adapter = new AdapterIdentity(context, identities, new AdapterIdentity.IListener() {
                    @Override
                    public void onSelected(TupleIdentityEx identity) {
                        Bundle args = getArguments();
                        args.putLong("id", identity.id);
                        args.putString("html", identity.signature);
                        sendResult(RESULT_OK);
                        dialog.dismiss();
                    }
                });
                rvSelect.setAdapter(adapter);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "select:identity");

        return dialog;
    }

    public static class AdapterIdentity extends RecyclerView.Adapter<AdapterIdentity.ViewHolder> {
        private Context context;
        private LayoutInflater inflater;

        private int dp6;
        private int dp12;
        private List<TupleIdentityEx> items;
        private IListener listener;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ComposeView composeView;

            ViewHolder(ComposeView itemView) {
                super(itemView);
                composeView = itemView;
            }

            private void bindTo(TupleIdentityEx identity) {
                boolean isCompact = (getItemCount() > 10);
                Integer color = (identity.color == null ? identity.accountColor : identity.color);
                String name = identity.getDisplayName();

                IdentitySelectItemBinder.bind(
                    composeView,
                    TextUtils.isEmpty(name) ? "-" : name,
                    identity.email,
                    color,
                    isCompact,
                    () -> {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            TupleIdentityEx id = items.get(pos);
                            listener.onSelected(id);
                        }
                    }
                );
            }
        }

        AdapterIdentity(Context context, List<TupleIdentityEx> identities, IListener listener) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.dp6 = Helper.dp2pixels(context, 6);
            this.dp12 = Helper.dp2pixels(context, 12);

            setHasStableIds(true);
            this.items = identities;
            this.listener = listener;
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(IdentitySelectItemBinder.createComposeView(parent));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TupleIdentityEx identity = items.get(position);
            holder.bindTo(identity);
        }

        public interface IListener {
            void onSelected(TupleIdentityEx identity);
        }
    }
}
