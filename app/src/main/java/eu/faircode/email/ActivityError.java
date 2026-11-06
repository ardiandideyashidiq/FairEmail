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

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.text.method.LinkMovementMethodCompat;
import androidx.core.widget.TextViewCompat;
import java.util.List;

public class ActivityError extends ActivityBase {
    static final int PI_ERROR = 1;
    static final int PI_ALERT = 2;

    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvCertificate;
    private Button btnPassword;
    private ImageButton ibSetting;
    private ImageButton ibInfo;
    private Button btnReload;
    private Button btnSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        int p6 = Helper.dp2pixels(this, 6);
        int p12 = Helper.dp2pixels(this, 12);
        int p24 = Helper.dp2pixels(this, 24);
        scrollView.setPadding(p6, p12, p6, p12);
        scrollView.setClipToPadding(false);

        CardView cardMessage = new CardView(this);
        FrameLayout.LayoutParams lpCard = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpCard.setMargins(Helper.dp2pixels(this, 3), 0, Helper.dp2pixels(this, 3), 0);
        cardMessage.setLayoutParams(lpCard);
        cardMessage.setCardBackgroundColor(Helper.resolveColor(this, R.attr.colorCardBackground));
        cardMessage.setRadius(Helper.dp2pixels(this, 6));
        cardMessage.setCardElevation(0);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(p6, p12, p6, p12);

        int colorWarning = Helper.resolveColor(this, R.attr.colorWarning);
        int textColorPrimary = Helper.resolveColor(this, android.R.attr.textColorPrimary);
        int colorError = Helper.resolveColor(this, androidx.appcompat.R.attr.colorError);

        tvTitle = new AppCompatTextView(this);
        tvTitle.setText("Error");
        TextViewCompat.setTextAppearance(tvTitle, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
        tvTitle.setTextColor(textColorPrimary);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.twotone_warning_24, 0, 0, 0);
        tvTitle.setCompoundDrawablePadding(p6);
        TextViewCompat.setCompoundDrawableTintList(tvTitle, ColorStateList.valueOf(colorWarning));
        cardContent.addView(tvTitle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        tvMessage = new AppCompatTextView(this);
        tvMessage.setText("Error");
        TextViewCompat.setTextAppearance(tvMessage, androidx.appcompat.R.style.TextAppearance_AppCompat_Small);
        tvMessage.setTextColor(colorWarning);
        tvMessage.setTextIsSelectable(true);
        tvMessage.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        tvMessage.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lpMessage = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpMessage.topMargin = p24;
        cardContent.addView(tvMessage, lpMessage);

        tvCertificate = new AppCompatTextView(this);
        tvCertificate.setText(R.string.title_certificate_error);
        TextViewCompat.setTextAppearance(tvCertificate, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
        tvCertificate.setTextColor(colorError);
        tvCertificate.setTypeface(null, Typeface.BOLD_ITALIC);
        LinearLayout.LayoutParams lpCert = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpCert.topMargin = p24;
        cardContent.addView(tvCertificate, lpCert);

        btnPassword = new AppCompatButton(this);
        btnPassword.setText(R.string.title_password);
        btnPassword.setCompoundDrawablePadding(p6);
        LinearLayout.LayoutParams lpPassword = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpPassword.topMargin = p24;
        cardContent.addView(btnPassword, lpPassword);

        RelativeLayout layoutButtons = new RelativeLayout(this);
        LinearLayout.LayoutParams lpButtons = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpButtons.topMargin = p24;

        ibSetting = new AppCompatImageButton(this);
        ibSetting.setImageResource(R.drawable.twotone_settings_24);
        ibSetting.setContentDescription(getString(R.string.title_setup));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ibSetting.setTooltipText(getString(R.string.title_setup));
        RelativeLayout.LayoutParams lpSetting = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpSetting.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutButtons.addView(ibSetting, lpSetting);

        ibInfo = new AppCompatImageButton(this);
        ibInfo.setImageResource(R.drawable.twotone_info_24);
        ibInfo.setContentDescription(getString(R.string.title_info));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ibInfo.setTooltipText(getString(R.string.title_info));
        RelativeLayout.LayoutParams lpInfo = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpInfo.addRule(RelativeLayout.ALIGN_PARENT_END);
        layoutButtons.addView(ibInfo, lpInfo);

        cardContent.addView(layoutButtons, lpButtons);

        btnReload = new AppCompatButton(this);
        btnReload.setText(R.string.title_boundary_retry);
        btnReload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.twotone_sync_24, 0);
        btnReload.setCompoundDrawablePadding(p6);
        LinearLayout.LayoutParams lpReload = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpReload.topMargin = p24;
        cardContent.addView(btnReload, lpReload);

        btnSupport = new AppCompatButton(this);
        btnSupport.setText(R.string.menu_faq);
        btnSupport.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.twotone_support_24, 0);
        btnSupport.setCompoundDrawablePadding(p6);
        LinearLayout.LayoutParams lpSupport = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpSupport.topMargin = p24;
        cardContent.addView(btnSupport, lpSupport);

        TextView tvSupport = new AppCompatTextView(this);
        tvSupport.setText(R.string.app_error);
        TextViewCompat.setTextAppearance(tvSupport, androidx.appcompat.R.style.TextAppearance_AppCompat_Small);
        tvSupport.setTypeface(null, Typeface.BOLD_ITALIC);
        LinearLayout.LayoutParams lpTvSupport = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpTvSupport.topMargin = p12;
        cardContent.addView(tvSupport, lpTvSupport);

        cardMessage.addView(cardContent);
        scrollView.addView(cardMessage, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        ContentLoadingProgressBar pbWait = new ContentLoadingProgressBar(this);
        FrameLayout.LayoutParams lpPb = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpPb.gravity = Gravity.CENTER;
        pbWait.setVisibility(View.GONE);
        root.addView(pbWait, lpPb);

        setContentView(root);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.title_setup_error));
        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void load() {
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String provider = intent.getStringExtra("provider");
        long account = intent.getLongExtra("account", -1L);
        long identity = intent.getLongExtra("identity", -1L);
        int protocol = intent.getIntExtra("protocol", -1);
        int auth_type = intent.getIntExtra("auth_type", -1);
        String host = intent.getStringExtra("host");
        int faq = intent.getIntExtra("faq", -1);

        boolean isCertificateException = (message != null && message.contains("CertificateException"));

        tvTitle.setText(title);
        tvMessage.setMovementMethod(LinkMovementMethodCompat.getInstance());
        tvMessage.setText(message);

        tvCertificate.setVisibility(
                isCertificateException && !SSLHelper.customTrustManager()
                        ? View.VISIBLE : View.GONE);

        boolean password = (auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD);
        boolean outlook = ("outlook.office365.com".equalsIgnoreCase(host) ||
                "smtp.office365.com".equalsIgnoreCase(host) ||
                "imap-mail.outlook.com".equalsIgnoreCase(host) ||
                "smtp-mail.outlook.com".equalsIgnoreCase(host));

        btnPassword.setText(password && !outlook ? R.string.title_password : R.string.title_setup_oauth_authorize);
        btnPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0,
                password ? R.drawable.twotone_edit_24 : R.drawable.twotone_check_24, 0);

        btnPassword.setVisibility(account < 0 ? View.GONE : View.VISIBLE);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth_type == ServiceAuthenticator.AUTH_TYPE_GMAIL)
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "gmail")
                            .putExtra("personal", intent.getStringExtra("personal"))
                            .putExtra("address", intent.getStringExtra("address")));
                else if (auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH) {
                    try {
                        EmailProvider eprovider = EmailProvider.getProvider(ActivityError.this, provider);
                        startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("target", "oauth")
                                .putExtra("id", eprovider.id)
                                .putExtra("name", eprovider.description)
                                .putExtra("privacy", eprovider.oauth.privacy)
                                .putExtra("askAccount", eprovider.oauth.askAccount)
                                .putExtra("askTenant", eprovider.oauth.askTenant())
                                .putExtra("personal", intent.getStringExtra("personal"))
                                .putExtra("address", intent.getStringExtra("address")));
                    } catch (Throwable ex) {
                        Log.e(ex);
                        startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                } else if (auth_type == ServiceAuthenticator.AUTH_TYPE_GRAPH ||
                        (auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD && outlook))
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "oauth")
                            .putExtra("id", "outlookgraph")
                            .putExtra("name", "Outlook")
                            .putExtra("askAccount", true)
                            .putExtra("askTenant", true)
                            .putExtra("personal", intent.getStringExtra("personal"))
                            .putExtra("address", intent.getStringExtra("address")));
                else
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "accounts")
                            .putExtra("id", account)
                            .putExtra("protocol", protocol));
                finish();
            }
        });

        ibSetting.setVisibility(account < 0 ? View.GONE : View.VISIBLE);
        ibSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("target", "accounts")
                        .putExtra("id", account)
                        .putExtra("protocol", protocol));
            }
        });

        ibInfo.setVisibility(faq > 0 ? View.VISIBLE : View.GONE);
        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(view.getContext(), isCertificateException ? 4 : faq);
            }
        });

        btnReload.setVisibility(account > 0 ? View.VISIBLE : View.GONE);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceSynchronize.reload(v.getContext(), account, true, "retry");
                finish();
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();

                sb.append("auth_type=")
                        .append(ServiceAuthenticator.getAuthTypeName(auth_type))
                        .append("\n");

                if (!TextUtils.isEmpty(host))
                    sb.append("host=")
                            .append(host)
                            .append("\n");

                if (account > 0)
                    sb.append("protocol=")
                            .append(protocol == EntityAccount.TYPE_IMAP ? "imap" : "pop3")
                            .append("\n");

                if (!TextUtils.isEmpty(provider))
                    sb.append("provider=")
                            .append(provider)
                            .append("\n");

                if (!TextUtils.isEmpty(message))
                    sb.append(Helper.limit(message, 384));

                Uri uri = Helper.getSupportUri(v.getContext(), "Sync:error")
                        .buildUpon()
                        .appendQueryParameter("message", sb.toString())
                        .build();
                Helper.view(v.getContext(), uri, true);
            }
        });

        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<EntityIdentity>() {
            @Override
            protected EntityIdentity onExecute(Context context, Bundle args) throws Throwable {
                long account = args.getLong("account");

                DB db = DB.getInstance(context);
                List<EntityIdentity> identities = db.identity().getIdentities(account);
                if (identities == null)
                    return null;
                if (identities.size() == 1)
                    return identities.get(0);
                for (EntityIdentity identity : identities)
                    if (identity.primary)
                        return identity;
                return null;
            }

            @Override
            protected void onExecuted(Bundle args, EntityIdentity identity) {
                if (identity == null)
                    return;
                intent.putExtra("personal", identity.name);
                intent.putExtra("address", identity.email);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // Ignored
            }
        }.execute(this, args, "error:details");
    }
}
