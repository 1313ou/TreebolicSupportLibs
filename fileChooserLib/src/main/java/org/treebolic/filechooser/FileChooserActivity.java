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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * File chooser
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
public class FileChooserActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener
{
	// keys

	static public final String ARG_FILECHOOSER_EXTENSION_FILTER = "filechooser.extension_filter";

	static public final String ARG_FILECHOOSER_INITIAL_DIR = "filechooser.initial_dir";

	static public final String ARG_FILECHOOSER_CHOOSE_DIR = "filechooser.choose_dir";

	/**
	 * File entry
	 */
	public class Entry implements Comparable<Entry>
	{
		/**
		 * Entry name
		 */
		private final String name;

		/**
		 * Entry data
		 */
		private final String data;

		/**
		 * Entry path
		 */
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
		public Entry(final String name0, final String data0, final String path0, final boolean folder0, final boolean parent0)
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
		public String getName()
		{
			return this.name;
		}

		/**
		 * Get data
		 *
		 * @return data
		 */
		public String getData()
		{
			return this.data;
		}

		/**
		 * Get path
		 *
		 * @return path
		 */
		public String getPath()
		{
			return this.path;
		}

		/**
		 * Is folder
		 *
		 * @return true if entry is folder
		 */
		public boolean isFolder()
		{
			return this.folder;
		}

		/**
		 * Is parent
		 *
		 * @return true if entry is parent
		 */
		public boolean isParent()
		{
			return this.parent;
		}

		/**
		 * Is none
		 *
		 * @return true if entry is parent
		 */
		public boolean isNone()
		{
			return this.none;
		}

		@Override
		public int compareTo(@SuppressWarnings("NullableProblems") final Entry o)
		{
			if (this.name != null)
			{
				return this.name.compareToIgnoreCase(o.getName());
			}
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Entry to list adapter
	 */
	public class FileArrayAdapter extends ArrayAdapter<Entry>
	{
		/**
		 * Context
		 */
		private final Context context;

		/**
		 * Entry id
		 */
		private final int id;

		/**
		 * List of entries
		 */
		private final List<Entry> items;

		/**
		 * Constructor
		 *
		 * @param context0 context
		 * @param id0      text view resource id
		 * @param items0   items
		 */
		public FileArrayAdapter(final Context context0, final int id0, final List<Entry> items0)
		{
			super(context0, id0, items0);
			this.context = context0;
			this.id = id0;
			this.items = items0;
		}

		@Override
		public Entry getItem(final int i)
		{
			return this.items.get(i);
		}

		@SuppressWarnings("NullableProblems")
		@Override
		public View getView(final int position, final View convertView, @SuppressWarnings("NullableProblems") final ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(this.id, null);
			}
			final Entry entry = this.items.get(position);
			if (entry != null)
			{
				final ImageView image = (ImageView) view.findViewById(R.id.typeImage);
				final TextView containerLabel = (TextView) view.findViewById(R.id.containerLabel);
				final TextView containerName = (TextView) view.findViewById(R.id.containerName);

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
					final String name = entry.getName().toLowerCase(Locale.getDefault());
					if (name.endsWith(".zip"))
					{
						image.setImageResource(R.drawable.filechooser_zip);
					}
					else
					{
						image.setImageResource(R.drawable.filechooser_file);
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
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// list view
		this.listView = (ListView) findViewById(android.R.id.list);

		// click listeners
		this.listView.setOnItemClickListener(this);
		this.listView.setOnItemLongClickListener(this);

		// default
		this.currentDir = Environment.getExternalStorageDirectory();

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
				this.fileFilter = new FileFilter()
				{
					@SuppressWarnings("synthetic-access")
					@Override
					public boolean accept(final File file)
					{
						final String name = file.getName();
						final int dot = name.lastIndexOf('.');
						return file.isDirectory() //
								|| FileChooserActivity.this.extensions == null //
								|| dot == -1 //
								|| FileChooserActivity.this.extensions.contains(name.substring(dot + 1));
					}
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
				this.currentDir = new File(entry.getPath());
				fill(this.currentDir);
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
	private void select(final Entry entry)
	{
		// select
		// Toast.makeText(this, getResources().getText(R.string.selected) + " " + entry.getName(), Toast.LENGTH_SHORT).show();
		final Intent resultIntent = new Intent();
		if (!entry.isNone())
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
	private void fill(final File dirFile)
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
		catch (final Exception e)
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