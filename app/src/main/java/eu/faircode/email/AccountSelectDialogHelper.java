package eu.faircode.email;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AccountSelectDialogHelper {
    public final View root;
    public final RecyclerView rvSelect;
    public final ContentLoadingProgressBar pbWait;

    public AccountSelectDialogHelper(Context context) {
        FrameLayout dview = new FrameLayout(context);
        int p12 = Helper.dp2pixels(context, 12);
        dview.setPadding(p12, p12, p12, p12);

        FixedRecyclerView rv = new FixedRecyclerView(context);
        rv.setHasFixedSize(false);
        rv.setLayoutManager(new LinearLayoutManager(context));
        dview.addView(rv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ContentLoadingProgressBar pb = new ContentLoadingProgressBar(context);
        FrameLayout.LayoutParams lpPb = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpPb.gravity = Gravity.CENTER;
        dview.addView(pb, lpPb);

        this.root = dview;
        this.rvSelect = rv;
        this.pbWait = pb;
    }

    public void setLoading(boolean loading) {
        pbWait.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvSelect.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
