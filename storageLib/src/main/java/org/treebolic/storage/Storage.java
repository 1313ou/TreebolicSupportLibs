/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.storage;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class Storage
{
	static private final String TAG = "Storage";

	// T R E E B O L I C   S T O R A G E

	/**
	 * Treebolic storage preference name
	 */
	@SuppressWarnings("WeakerAccess")
	public static final String PREF_TREEBOLIC_STORAGE = "pref_storage";

	/**
	 * Cached treebolic storage
	 */
	@Nullable
	static private File treebolicStorage = null;

	/**
	 * Get treebolic storage
	 *
	 * @return treebolic storage directory
	 */
	@SuppressWarnings("WeakerAccess")
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	@NonNull
	static public File getTreebolicStorage(@NonNull final Context context)
	{
		// if cached return cache
		if (Storage.treebolicStorage != null)
		{
			return Storage.treebolicStorage;
		}

		// test if already discovered in this context
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final String pref = sharedPref.getString(Storage.PREF_TREEBOLIC_STORAGE, null);
		if (pref != null)
		{
			Storage.treebolicStorage = new File(pref);
			if (Storage.qualifies(Storage.treebolicStorage))
			{
				return Storage.treebolicStorage;
			}
		}

		// discover
		Storage.treebolicStorage = Storage.discoverTreebolicStorage(context);
		assert Storage.treebolicStorage != null;
		String path = Storage.treebolicStorage.getAbsolutePath();

		// flag as discovered
		sharedPref.edit().putString(Storage.PREF_TREEBOLIC_STORAGE, path).commit();

		return Storage.treebolicStorage;
	}

	/**
	 * Discover Treebolic storage
	 *
	 * @param context context
	 * @return Treebolic storage
	 */
	@Nullable
	static private File discoverTreebolicStorage(@NonNull final Context context)
	{
		// application-specific secondary or primary storage
		try
		{
			final File[] dirs = ContextCompat.getExternalFilesDirs(context, null);

			// preferably secondary storage
			for (int i = 1; i < dirs.length; i++)
			{
				if (Storage.qualifies(dirs[i]))
				{
					return dirs[i];
				}
			}
			// fall back on primary storage
			if (Storage.qualifies(dirs[0]))
			{
				return dirs[0];
			}
		}
		catch (@NonNull final Throwable ignored)
		{
			//
		}

		// top-level public external storage directory (KITKAT for DIRECTORY_DOCUMENTS)

		//		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
		//		{
		//			File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		//			if (Storage.qualifies(dir))
		//			{
		//				return dir;
		//			}
		//		}

		// top-level public in external
		//		try
		//		{
		//			final String storage = Storage.getExternalStorage();
		//			if (storage != null)
		//			{
		//				File dir = new File(storage, Storage.TREEBOLICDIR);
		//				if (Storage.qualifies(dir))
		//				{
		//					return dir;
		//				}
		//			}
		//		}
		//		catch (@NonNull final Throwable ignored)
		//		{
		//			//
		//		}

		// internal private storage
		return context.getFilesDir();
	}

	// T R E E B O L I C   S T O R A G E   Q U A L I F I C A T I O N

	/**
	 * Whether the dir qualifies as treebolic storage
	 *
	 * @param dir candidate dir
	 * @return true if it qualifies
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	static private boolean qualifies(@Nullable final File dir)
	{
		if (dir == null)
		{
			return false;
		}

		// log
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			Log.d(Storage.TAG, "storage state of " + dir + ": " + Environment.getExternalStorageState(dir));
		}

		// either mkdirs() creates dir or it is already a dir
		return dir.mkdirs() || dir.isDirectory(); // || dir.canWrite())
	}
}
