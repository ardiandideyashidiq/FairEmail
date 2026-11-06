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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FragmentDialogEditImage extends FragmentDialogBase {
    private int resize = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (savedInstanceState != null)
            resize = savedInstanceState.getInt("fair:resize", 0);

        final int[] resizeValues = getResources().getIntArray(R.array.resizeValues);

        Context context = getContext();

        int p12 = Helper.dp2pixels(context, 12);
        int p6 = Helper.dp2pixels(context, 6);
        int p36 = Helper.dp2pixels(context, 36);
        int p24 = Helper.dp2pixels(context, 24);
        int minHeightCiv = Helper.dp2pixels(context, 60);
        int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        int selectableBgRes = outValue.resourceId;

        FrameLayout dview = new FrameLayout(context);
        dview.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        dview.setPadding(p12, p12, p12, p12);

        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        dview.addView(contentLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        CropImageView civ = new CropImageView(context);
        civ.setMinimumHeight(minHeightCiv);
        LinearLayout.LayoutParams lpCiv = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLayout.addView(civ, lpCiv);

        LinearLayout buttonBar = new LinearLayout(context);
        buttonBar.setOrientation(LinearLayout.HORIZONTAL);
        buttonBar.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpButtonBar = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpButtonBar.topMargin = p12;
        contentLayout.addView(buttonBar, lpButtonBar);

        LinearLayout leftActions = new LinearLayout(context);
        leftActions.setOrientation(LinearLayout.HORIZONTAL);
        leftActions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f);
        buttonBar.addView(leftActions, lpLeft);

        FixedImageButton ibRotate = new FixedImageButton(context);
        ibRotate.setBackgroundResource(selectableBgRes);
        ibRotate.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibRotate.setImageResource(R.drawable.twotone_rotate_right_24);
        ibRotate.setColorFilter(colorAccent);
        ibRotate.setContentDescription(getString(com.canhub.cropper.R.string.ic_rotate_right_24));
        leftActions.addView(ibRotate, new LinearLayout.LayoutParams(p36, p36));

        FixedImageButton ibFlipHorizontally = new FixedImageButton(context);
        ibFlipHorizontally.setBackgroundResource(selectableBgRes);
        ibFlipHorizontally.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibFlipHorizontally.setImageResource(R.drawable.twotone_swap_horiz_24);
        ibFlipHorizontally.setColorFilter(colorAccent);
        ibFlipHorizontally.setContentDescription(getString(com.canhub.cropper.R.string.ic_flip_24_horizontally));
        LinearLayout.LayoutParams lpFlipH = new LinearLayout.LayoutParams(p36, p36);
        lpFlipH.setMarginStart(p6);
        leftActions.addView(ibFlipHorizontally, lpFlipH);

        FixedImageButton ibFlipVertically = new FixedImageButton(context);
        ibFlipVertically.setBackgroundResource(selectableBgRes);
        ibFlipVertically.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibFlipVertically.setImageResource(R.drawable.twotone_swap_vert_24);
        ibFlipVertically.setColorFilter(colorAccent);
        ibFlipVertically.setContentDescription(getString(com.canhub.cropper.R.string.ic_flip_24_vertically));
        LinearLayout.LayoutParams lpFlipV = new LinearLayout.LayoutParams(p36, p36);
        lpFlipV.setMarginStart(p6);
        leftActions.addView(ibFlipVertically, lpFlipV);

        FixedImageButton ibResize = new FixedImageButton(context);
        ibResize.setBackgroundResource(selectableBgRes);
        ibResize.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibResize.setImageResource(R.drawable.twotone_open_in_full_24);
        ibResize.setColorFilter(colorAccent);
        ibResize.setContentDescription(getString(R.string.title_add_image_resize));
        LinearLayout.LayoutParams lpResize = new LinearLayout.LayoutParams(p36, p36);
        lpResize.setMarginStart(p6);
        leftActions.addView(ibResize, lpResize);

        FixedImageButton ibCancel = new FixedImageButton(context);
        ibCancel.setBackgroundResource(selectableBgRes);
        ibCancel.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibCancel.setImageResource(R.drawable.twotone_close_24);
        ibCancel.setContentDescription(getString(android.R.string.cancel));
        LinearLayout.LayoutParams lpCancel = new LinearLayout.LayoutParams(p36, p36);
        lpCancel.setMarginStart(p6);
        leftActions.addView(ibCancel, lpCancel);

        FixedImageButton ibSave = new FixedImageButton(context);
        ibSave.setBackgroundResource(selectableBgRes);
        ibSave.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibSave.setImageResource(R.drawable.twotone_save_alt_24);
        ibSave.setColorFilter(colorAccent);
        ibSave.setContentDescription(getString(R.string.title_save));
        buttonBar.addView(ibSave, new LinearLayout.LayoutParams(p36, p36));

        ContentLoadingProgressBar pbWait = new ContentLoadingProgressBar(context);
        pbWait.setIndeterminate(true);
        pbWait.setPadding(p24, p24, p24, p24);
        FrameLayout.LayoutParams lpPb = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lpPb.gravity = Gravity.CENTER;
        dview.addView(pbWait, lpPb);
        Dialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .create();

        ibRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                civ.rotateImage(90);
            }
        });

        ibFlipHorizontally.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                civ.flipImageHorizontally();
            }
        });

        ibFlipVertically.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                civ.flipImageVertically();
            }
        });

        ibResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, FragmentDialogEditImage.this, v);

                Size expected = civ.expectedImageSize();
                int s = (expected == null
                        ? Integer.MAX_VALUE
                        : Math.max(expected.getWidth(), expected.getHeight()));

                SpannableStringBuilder ssb = new SpannableStringBuilderEx("---");
                if (resize == 0)
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                popupMenu.getMenu().add(Menu.NONE, 1, 1, ssb)
                        .setIntent(new Intent().putExtra("size", 0));

                int order = 1;
                for (int size : resizeValues) {
                    order++;
                    ssb = new SpannableStringBuilderEx(getString(R.string.title_add_resize_pixels, size));
                    if (size == resize)
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                    popupMenu.getMenu().add(Menu.NONE, order, order, ssb)
                            .setEnabled(size < s)
                            .setIntent(new Intent().putExtra("size", size));
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = item.getIntent();
                        if (intent == null)
                            return false;

                        resize = intent.getIntExtra("size", 0);
                        return true;
                    }
                });

                popupMenu.show();

            }
        });

        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<Void>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ibSave.setEnabled(true);
                        pbWait.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        ibSave.setEnabled(true);
                        pbWait.setVisibility(View.GONE);
                    }

                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");

                        DB db = DB.getInstance(context);
                        EntityAttachment attachment = db.attachment().getAttachment(id);
                        if (attachment == null)
                            return null;

                        Bitmap bm = civ.getCroppedImage(resize, resize, CropImageView.RequestSizeOptions.RESIZE_INSIDE);
                        if (bm == null)
                            return null;

                        File file = attachment.getFile(context);

                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            bm.compress(Bitmap.CompressFormat.PNG, ImageHelper.DEFAULT_PNG_COMPRESSION, os);
                        }

                        long len = file.length();

                        if (attachment.name != null) {
                            int index = attachment.name.lastIndexOf(".");
                            if (index > 0) {
                                String ext = attachment.name.substring(index + 1);
                                if (!TextUtils.isEmpty(ext) && !ext.equalsIgnoreCase("png")) {
                                    File old = attachment.getFile(context);
                                    attachment.name = attachment.name.substring(0, index) + ".png";
                                    if (!old.renameTo(attachment.getFile(context)))
                                        throw new IOException("Error renaming " + file.getAbsolutePath());
                                }
                            }
                        }

                        db.attachment().setName(id, attachment.name, "image/png", len);

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        sendResult(RESULT_OK);
                        dismiss();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragment(), ex);
                    }
                }.execute(FragmentDialogEditImage.this, args, "save:image");
            }
        });

        CropImageOptions options = new CropImageOptions();
        options.borderCornerThickness = Helper.dp2pixels(context, 5);
        civ.setImageCropOptions(options);

        new SimpleTask<EntityAttachment>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
                ibSave.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
                ibSave.setEnabled(true);
            }

            @Override
            protected EntityAttachment onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.attachment().getAttachment(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityAttachment attachment) {
                if (attachment == null)
                    return;

                civ.setImageUriAsync(attachment.getUri(context));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "edit:image");

        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("fair:resize", resize);
        super.onSaveInstanceState(outState);
    }
}
