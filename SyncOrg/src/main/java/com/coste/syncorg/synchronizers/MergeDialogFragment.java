package com.coste.syncorg.synchronizers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.coste.syncorg.R;

/**
 * Created by bcoste on 19/06/16.
 */
public class MergeDialogFragment extends DialogFragment {


    public MergeDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] conflictedFiles = getActivity().getIntent().getCharSequenceArrayExtra(JGitWrapper.CONFLICT_FILES);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sync_git_merge_files)
                .setItems(conflictedFiles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        return builder.create();
    }
}
