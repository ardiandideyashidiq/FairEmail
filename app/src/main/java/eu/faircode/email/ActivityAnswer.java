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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.Group;

import java.util.Date;
import java.util.List;

public class ActivityAnswer extends ActivityBase {

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        final CharSequence query = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        final boolean readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);

        final ActivityAnswerView answerView = new ActivityAnswerView(this, answer -> {
            Helper.getParallelExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        DB db = DB.getInstance(ActivityAnswer.this);
                        db.answer().applyAnswer(answer.id, new Date().getTime());
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });

            String html = answer.getData(ActivityAnswer.this, null).getHtml();
            String text = HtmlHelper.getText(ActivityAnswer.this, html);

            ClipboardManager cbm = Helper.getSystemService(ActivityAnswer.this, ClipboardManager.class);
            cbm.setPrimaryClip(ClipData.newHtmlText(getString(R.string.app_name), text, html));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                ToastEx.makeText(ActivityAnswer.this, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();

            if (!readonly) {
                Intent result = new Intent();
                result.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
                setResult(RESULT_OK, result);
            }

            finish();
        });

        setContentView(answerView.getComposeView());
        getSupportActionBar().setSubtitle(query == null ? null : query.toString());

        new SimpleTask<List<EntityAnswer>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                answerView.setLoading(true);
            }

            @Override
            protected List<EntityAnswer> onExecute(Context context, Bundle args) throws Throwable {
                DB db = DB.getInstance(context);
                return db.answer().getAnswersExternal();
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                answerView.setAnswers(answers);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "answers");
    }

    static boolean canAnswer(Context context) {
        return BuildConfig.DEBUG;
    }
}
