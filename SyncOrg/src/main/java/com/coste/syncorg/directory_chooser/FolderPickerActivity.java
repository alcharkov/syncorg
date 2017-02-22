package com.coste.syncorg.directory_chooser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coste.syncorg.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Activity that allows selecting a directory in the local file system.
 */
public class FolderPickerActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    public static final String EXTRA_INITIAL_DIRECTORY =
            "com.nutomic.syncthingandroid.activities.FolderPickerActivity.INITIAL_DIRECTORY";
    public static final String EXTRA_RESULT_DIRECTORY =
            "com.nutomic.syncthingandroid.activities.FolderPickerActivity.RESULT_DIRECTORY";
    final public static int MAKE_NULL_SYNC_DIR_PERMISSION = 0;
    private static final String TAG = "FolderPickerActivity";
    private ListView mListView;
    private FileAdapter mFilesAdapter;
    private RootsAdapter mRootsAdapter;
    /**
     * Location of null means that the list of roots is displayed.
     */
    private File mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_folder_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mFilesAdapter = new FileAdapter(this);
        mRootsAdapter = new RootsAdapter(this);
        mListView.setAdapter(mFilesAdapter);

        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MAKE_NULL_SYNC_DIR_PERMISSION);
            }
        }
        populateAndDisplay();
    }

    private void populateAndDisplay() {
        populateRoots();

        if (getIntent().hasExtra(EXTRA_INITIAL_DIRECTORY)) {
            displayFolder(new File(getIntent().getStringExtra(EXTRA_INITIAL_DIRECTORY)));
        } else {
            displayRoot();
        }
    }

    /**
     * Reads available storage devices/folders from various APIs and inserts them into
     * {@link #mRootsAdapter}.
     */
    @SuppressLint("NewApi")
    private void populateRoots() {
        ArrayList<File> roots = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            roots.addAll(Arrays.asList(getExternalFilesDirs(null)));
            roots.remove(getExternalFilesDir(null));
        }
        roots.add(Environment.getExternalStorageDirectory());
        roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));

        // Add paths that might not be accessible to Syncthing.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("advanced_folder_picker", false)) {
            Collections.addAll(roots, new File("/storage/").listFiles());
            roots.add(new File("/"));
        }

        // Remove any invalid directories.
        Iterator<File> it = roots.iterator();
        while (it.hasNext()) {
            File f = it.next();
            if (f == null || !f.exists() || !f.isDirectory()) {
                it.remove();
            }
        }
//        mRootsAdapter.addAll(Sets.newTreeSet(roots));
        mRootsAdapter.addAll(Environment.getExternalStorageDirectory());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mListView.getAdapter() == mRootsAdapter)
            return true;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.folder_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_folder:
                final EditText et = new EditText(this);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.create_folder_label)
                        .setView(et)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        createFolder(et.getText().toString());
                                    }
                                }
                        )
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
                dialog.show();
                return true;
            case R.id.select:
                Intent intent = new Intent()
                        .putExtra(EXTRA_RESULT_DIRECTORY, mLocation.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MAKE_NULL_SYNC_DIR_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    populateAndDisplay();
                } else {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage(R.string.write_permission_denied_warning_content)
                            .setTitle(R.string.write_permission_denied_warning_title);

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    /**
     * Creates a new folder with the given name and enters it.
     */
    private void createFolder(String name) {
        File newFolder = new File(mLocation, name);
        if (newFolder.mkdir()) {
            displayFolder(newFolder);
        } else {
            Toast.makeText(this, R.string.create_folder_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes the ListView to show the contents of the folder in {@code }mLocation.peek()}.
     */
    private void displayFolder(File folder) {
        mLocation = folder;
        mFilesAdapter.clear();
        File[] contents = mLocation.listFiles();
        // In case we don't have read access to the folder, just display nothing.
        if (contents == null)
            contents = new File[]{};


        Arrays.sort(contents, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && f2.isFile())
                    return -1;
                if (f1.isFile() && f2.isDirectory())
                    return 1;
                return f1.getName().compareTo(f2.getName());
            }
        });

        for (File f : contents) {
            mFilesAdapter.add(f);
        }
        mListView.setAdapter(mFilesAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<File> adapter = (ArrayAdapter<File>) mListView.getAdapter();
        File f = adapter.getItem(i);
        if (f.isDirectory()) {
            displayFolder(f);
            invalidateOptions();
        }
    }

    private void invalidateOptions() {
        invalidateOptionsMenu();
    }

    /**
     * Goes up a directory, up to the list of roots if there are multiple roots.
     * <p>
     * If we already are in the list of roots, or if we are directly in the only
     * root folder, we cancel.
     */
    @Override
    public void onBackPressed() {
        if (!mRootsAdapter.contains(mLocation) && mLocation != null) {
            displayFolder(mLocation.getParentFile());
        } else if (mRootsAdapter.contains(mLocation) && mRootsAdapter.getCount() > 1) {
            displayRoot();
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Displays a list of all available roots, or if there is only one root, the
     * contents of that folder.
     */
    private void displayRoot() {
        mFilesAdapter.clear();
        if (mRootsAdapter.getCount() == 1) {
            displayFolder(mRootsAdapter.getItem(0));
        } else {
            mListView.setAdapter(mRootsAdapter);
            mLocation = null;
        }
        invalidateOptions();
    }

    private class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context) {
            super(context, R.layout.item_folder_picker);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            TextView title = (TextView) convertView.findViewById(android.R.id.text1);
            File f = getItem(position);
            title.setText(f.getName());
            int textColor = (f.isDirectory())
                    ? android.R.color.primary_text_light
                    : android.R.color.tertiary_text_light;
            title.setTextColor(ContextCompat.getColor(getContext(), textColor));

            return convertView;
        }
    }

    private class RootsAdapter extends ArrayAdapter<File> {

        public RootsAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            TextView title = (TextView) convertView.findViewById(android.R.id.text1);
            title.setText(getItem(position).getAbsolutePath());
            return convertView;
        }

        public boolean contains(File file) {
            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).equals(file))
                    return true;
            }
            return false;
        }
    }

}
