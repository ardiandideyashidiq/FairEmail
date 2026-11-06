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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentDialogAddImage extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        int title = getArguments().getInt("title");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean add_inline = prefs.getBoolean("add_inline", true);
        boolean resize_images = prefs.getBoolean("resize_images", true);
        int resize = prefs.getInt("resize", FragmentCompose.REDUCED_IMAGE_SIZE);
        boolean resize_width_only = prefs.getBoolean("resize_width_only", false);
        boolean privacy_images = prefs.getBoolean("privacy_images", false);
        boolean image_dialog = prefs.getBoolean("image_dialog", true);

        int typeIconRes = (title == R.string.title_attachment_photo
                ? R.drawable.twotone_photo_camera_24 : R.drawable.twotone_image_24);

        // https://developer.android.com/reference/android/provider/MediaStore#ACTION_PICK_IMAGES_SETTINGS
        PackageManager pm = context.getPackageManager();
        final Intent settings = new Intent(MediaStore.ACTION_PICK_IMAGES_SETTINGS);
        boolean hasSettings = settings.resolveActivity(pm) != null;

        String[] resizeNamesArray = getResources().getStringArray(R.array.resizeNames);
        List<String> resizeNames = Arrays.asList(resizeNamesArray);
        final int[] resizeValuesArray = getResources().getIntArray(R.array.resizeValues);
        List<Integer> resizeValues = new ArrayList<>();
        int selectedIndex = 0;
        for (int pos = 0; pos < resizeValuesArray.length; pos++) {
            resizeValues.add(resizeValuesArray[pos]);
            if (resizeValuesArray[pos] == resize)
                selectedIndex = pos;
        }

        final DialogAddImageView dialogView = new DialogAddImageView(
                context,
                title,
                typeIconRes,
                add_inline,
                hasSettings,
                new Runnable() {
                    @Override
                    public void run() {
                        context.startActivity(settings);
                    }
                },
                resize_images,
                new Runnable() {
                    @Override
                    public void run() {
                        Helper.viewFAQ(context, 63);
                    }
                },
                resizeNames,
                resizeValues,
                selectedIndex,
                resize_width_only,
                privacy_images,
                !image_dialog
        );

        return new AlertDialog.Builder(context)
                .setView(dialogView.getComposeView())
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(title,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit()
                                        .putBoolean("add_inline", dialogView.isAddInline())
                                        .putBoolean("resize_images", dialogView.isResizeImages())
                                        .putInt("resize", dialogView.getSelectedResize())
                                        .putBoolean("resize_width_only", dialogView.isResizeWidthOnly())
                                        .putBoolean("privacy_images", dialogView.isPrivacyImages())
                                        .putBoolean("image_dialog", !dialogView.isNotAgain())
                                        .apply();
                                sendResult(RESULT_OK);
                            }
                        })
                .create();
    }
}
