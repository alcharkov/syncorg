package com.coste.syncorg.gui.wizard.wizards;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ListView;

import com.coste.syncorg.R;
import com.coste.syncorg.gui.wizard.FolderAdapter;
import com.coste.syncorg.gui.wizard.LocalDirectoryBrowser;

import static com.coste.syncorg.settings.SettingsActivity.KEY_SYNC_SOURCE;
import static com.coste.syncorg.synchronizers.Synchronizer.SD_CARD;

public class SDCardWizard extends AppCompatActivity {

    private FolderAdapter directoryAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_folder_pick_list);

        LocalDirectoryBrowser directory = new LocalDirectoryBrowser(this);
        directoryAdapter = new FolderAdapter(this, R.layout.folder_adapter_row,
                directory.listFiles());
        directoryAdapter
                .setDoneButton((Button) findViewById(R.id.wizard_done_button));
        directoryAdapter.setDirectoryBrowser(directory);

        ListView folderList = (ListView) findViewById(R.id.wizard_folder_list);
        folderList.setAdapter(directoryAdapter);
        directoryAdapter.notifyDataSetChanged();
    }

    public void saveSettings() {
        SharedPreferences appSettings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = appSettings.edit();

        editor.putString(KEY_SYNC_SOURCE, SD_CARD);
        editor.putString("indexFilePath",
                directoryAdapter.getCheckedDirectory());

        editor.apply();
        finish();
    }
}
