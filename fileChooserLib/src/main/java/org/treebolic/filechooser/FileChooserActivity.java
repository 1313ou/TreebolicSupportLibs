/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.filechooser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.treebolic.AppCompatCommonActivity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * File chooser
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
public class FileChooserActivity extends AppCompatCommonActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener
{
	// keys

	static public final String ARG_FILECHOOSER_EXTENSION_FILTER = "filechooser.extension_filter";

	static public final String ARG_FILECHOOSER_INITIAL_DIR = "filechooser.initial_dir";

	static public final String ARG_FILECHOOSER_CHOOSE_DIR = "filechooser.choose_dir";

	/**
	 * File entry
	 */
	public static class Entry implements Comparable<Entry>
	{
		/**
		 * Entry name
		 */
		@Nullable
		private final String name;

		/**
		 * Entry data
		 */
		@Nullable
		private final String data;

		/**
		 * Entry path
		 */
		@Nullable
		private final String path;

		/**
		 * Entry folder
		 */
		private final boolean folder;

		/**
		 * Entry parent
		 */
		private final boolean parent;

		/**
		 * Entry none
		 */
		private final boolean none;

		/**
		 * Constructor
		 *
		 * @param name0   name
		 * @param data0   data
		 * @param path0   path
		 * @param folder0 folder
		 * @param parent0 parent
		 */
		@SuppressWarnings("WeakerAccess")
		public Entry(@Nullable final String name0, @Nullable final String data0, @Nullable final String path0, final boolean folder0, final boolean parent0)
		{
			this.name = name0;
			this.data = data0;
			this.path = path0;
			this.folder = folder0;
			this.parent = parent0;
			this.none = false;
		}

		/**
		 * Null entry
		 */
		@SuppressWarnings("WeakerAccess")
		public Entry()
		{
			this.none = true;
			this.name = null;
			this.data = null;
			this.path = null;
			this.folder = false;
			this.parent = false;
		}

		/**
		 * Get name
		 *
		 * @return name
		 */
		@Nullable
		public String getName()
		{
			return this.name;
		}

		/**
		 * Get data
		 *
		 * @return data
		 */
		@SuppressWarnings("WeakerAccess")
		@Nullable
		public String getData()
		{
			return this.data;
		}

		/**
		 * Get path
		 *
		 * @return path
		 */
		@SuppressWarnings("WeakerAccess")
		@Nullable
		public String getPath()
		{
			return this.path;
		}

		/**
		 * Is folder
		 *
		 * @return true if entry is folder
		 */
		@SuppressWarnings("WeakerAccess")
		public boolean isFolder()
		{
			return this.folder;
		}

		/**
		 * Is parent
		 *
		 * @return true if entry is parent
		 */
		@SuppressWarnings("WeakerAccess")
		public boolean isParent()
		{
			return this.parent;
		}

		/**
		 * Is none
		 *
		 * @return true if entry is parent
		 */
		@SuppressWarnings("WeakerAccess")
		public boolean isNone()
		{
			return this.none;
		}

		@Override
		public int compareTo(@NonNull final Entry o)
		{
			if (this.name != null)
			{
				final String name2 = o.getName();
				if (name2 == null)
				{
					return 1;
				}
				return this.name.compareToIgnoreCase(name2);
			}
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Entry to list adapter
	 */
	@SuppressWarnings("WeakerAccess")
	public class FileArrayAdapter extends ArrayAdapter<Entry>
	{
		/**
		 * Context
		 */
		@NonNull
		private final Context context;

		/**
		 * Entry id
		 */
		private final int id;

		/**
		 * List of entries
		 */
		@NonNull
		private final List<Entry> items;

		/**
		 * Constructor
		 *
		 * @param context   context
		 * @param layoutRes text view resource id
		 * @param items     items
		 */
		@SuppressWarnings("WeakerAccess")
		public FileArrayAdapter(@NonNull final Context context, @LayoutRes final int layoutRes, @NonNull final List<Entry> items)
		{
			super(context, layoutRes, items);
			this.context = context;
			this.id = layoutRes;
			this.items = items;
		}

		@Override
		public Entry getItem(final int i)
		{
			return this.items.get(i);
		}

		@NonNull
		@SuppressWarnings("NullableProblems")
		@Override
		public View getView(final int position, final View convertView, @SuppressWarnings("NullableProblems") final ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				final LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				assert inflater != null;
				view = inflater.inflate(this.id, null);
			}
			final Entry entry = this.items.get(position);
			if (entry != null)
			{
				final ImageView image = view.findViewById(R.id.typeImage);
				final TextView containerLabel = view.findViewById(R.id.containerLabel);
				final TextView containerName = view.findViewById(R.id.containerName);

				if (entry.isNone())
				{
					image.setImageResource(R.drawable.filechooser_cancel);
				}
				else if (entry.isParent())
				{
					image.setImageResource(R.drawable.filechooser_back);
				}
				else if (entry.isFolder())
				{
					image.setImageResource(R.drawable.filechooser_folder);
				}
				else
				{
					String name = entry.getName();
					if (name != null)
					{
						name = name.toLowerCase(Locale.getDefault());
						if (name.endsWith(".zip"))
						{
							image.setImageResource(R.drawable.filechooser_zip);
						}
						else
						{
							image.setImageResource(R.drawable.filechooser_file);
						}
					}
				}

				if (containerLabel != null)
				{
					if (entry.isNone())
					{
						containerLabel.setText(R.string.cancel);
					}
					else
					{
						containerLabel.setText(entry.getName());
					}
				}
				if (containerName != null)
				{
					if (entry.isNone())
					{
						containerName.setText(R.string.none);
					}
					else
					{
						containerName.setText(entry.getData());
					}
				}
			}
			return view;
		}
	}

	/**
	 * List view
	 */
	private ListView listView;

	/**
	 * Current directory
	 */
	@Nullable
	private File currentDir;

	/**
	 * Type
	 */
	private boolean chooseDir;

	/**
	 * Adapter
	 */
	private FileArrayAdapter adapter;

	/**
	 * File filter
	 */
	private FileFilter fileFilter;

	/**
	 * Acceptable extensions
	 */
	private List<String> extensions;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_choose_file);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// list view
		this.listView = findViewById(android.R.id.list);

		// click listeners
		this.listView.setOnItemClickListener(this);
		this.listView.setOnItemLongClickListener(this);

		// default
		this.currentDir = Environment.getExternalStorageDirectory();
		assert this.currentDir != null;

		// extras
		final Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			// initial
			String initialDirExtra = extras.getString(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR);
			if (initialDirExtra != null)
			{
				final Uri uri = Uri.parse(initialDirExtra);
				if (uri != null && "file".equals(uri.getScheme()))
				{
					initialDirExtra = uri.getPath();
				}
				this.currentDir = new File(initialDirExtra);
			}

			// type
			this.chooseDir = extras.getBoolean(FileChooserActivity.ARG_FILECHOOSER_CHOOSE_DIR, false);

			// extensions
			final String[] extensionExtras = extras.getStringArray(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER);
			if (extensionExtras != null && extensionExtras.length > 0)
			{
				this.extensions = Arrays.asList(extensionExtras);
				this.fileFilter = file -> {
					final String name = file.getName();
					final int dot = name.lastIndexOf('.');
					return file.isDirectory() //
							|| FileChooserActivity.this.extensions == null //
							|| dot == -1 //
							|| FileChooserActivity.this.extensions.contains(name.substring(dot + 1));
				};
			}
		}

		// initialize list
		fill(this.currentDir);

		// initialize list
		if (this.chooseDir)
		{
			Toast.makeText(this, R.string.howToSelect, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			assert this.currentDir != null;
			if (// !this.currentDir.getName().equals(ROOT) &&
					this.currentDir.getParentFile() != null)
			{
				this.currentDir = this.currentDir.getParentFile();
				fill(this.currentDir);
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Entry entry = this.adapter.getItem(position);
		if (entry != null)
		{
			if (entry.isFolder() || entry.isParent())
			{
				// if folder we move into it
				final String path = entry.getPath();
				if (path != null)
				{
					this.currentDir = new File(path);
					fill(this.currentDir);
				}
			}
			else
			{
				// select
				if (!this.chooseDir || entry.isNone())
				{
					select(entry);
				}
			}
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Entry entry = this.adapter.getItem(position);
		if (entry != null)
		{
			if (this.chooseDir && (entry.isFolder() || entry.isParent()))
			{
				// select
				select(entry);
				return true;
			}
		}
		return false;
	}

	/**
	 * Select and return entry
	 *
	 * @param entry entry
	 */
	private void select(@NonNull final Entry entry)
	{
		// select
		// Toast.makeText(this, getResources().getText(R.string.selected) + " " + entry.getName(), Toast.LENGTH_SHORT).show();
		final Intent resultIntent = new Intent();
		if (!entry.isNone() && entry.getPath() != null)
		{
			final Uri fileUri = Uri.fromFile(new File(entry.getPath()));
			resultIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
		}
		else
		{
			resultIntent.setData(null);
		}
		setResult(AppCompatActivity.RESULT_OK, resultIntent);
		finish();
	}

	/**
	 * Fill with entries from dir
	 *
	 * @param dirFile dir
	 */
	private void fill(@NonNull final File dirFile)
	{
		File[] items;
		if (this.fileFilter != null)
		{
			items = dirFile.listFiles(this.fileFilter);
		}
		else
		{
			items = dirFile.listFiles();
		}

		this.setTitle(getString(R.string.currentDir) + ": " + dirFile.getName());
		final List<Entry> dirs = new ArrayList<>();
		final List<Entry> files = new ArrayList<>();
		try
		{
			if (items != null)
			{
				for (final File item : items)
				{
					if (item.isDirectory() && !item.isHidden())
					{
						dirs.add(new Entry(item.getName(), getString(R.string.folder), item.getAbsolutePath(), true, false));
					}
					else
					{
						if (!item.isHidden())
						{
							files.add(new Entry(item.getName(), getString(R.string.fileSize) + ": " + item.length(), item.getAbsolutePath(), false, false));
						}
					}
				}
			}
		}
		catch (@NonNull final Exception ignored)
		{
			//
		}

		// sort
		Collections.sort(dirs);
		Collections.sort(files);
		dirs.addAll(files);
		dirs.add(new Entry());

		// container
		// if (!dirFile.getName().equalsIgnoreCase(ROOT))
		{
			if (dirFile.getParentFile() != null)
			{
				dirs.add(0, new Entry(".. " + getString(R.string.parentDirectory), dirFile.getAbsolutePath(), dirFile.getParent(), false, true));
			}
		}

		// adapter
		this.adapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.filechooser_entries_file, dirs);
		this.listView.setAdapter(this.adapter);
	}

	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void setFolder(final Context context, final String key, final String folder)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor edit = prefs.edit();
		edit.putString(key, folder).commit();
	}

	@Nullable
	static public File getFolder(final Context context, final String key)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String path = prefs.getString(key, null);
		if (path == null)
		{
			return null;
		}
		final File dir = new File(path);
		if (!dir.exists() || !dir.isDirectory())
		{
			return null;
		}
		return dir;
	}
}
