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
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
	 * Treebolic sub directory
	 */
	static private final String TREEBOLICDIR = "treebolic" + '/';

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
	@TargetApi(Build.VERSION_CODES.KITKAT)
	static private File discoverTreebolicStorage(@NonNull final Context context)
	{
		// application-specific secondary storage or primary (KITKAT)
		try
		{
			final File[] dirs = context.getExternalFilesDirs(null);
			if (dirs != null && dirs.length > 0)
			{
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
		}
		catch (@NonNull final Throwable ignored)
		{
			//
		}

		File dir;

		// application-specific primary storage

		dir = context.getExternalFilesDir(null);
		if (Storage.qualifies(dir))
		{
			return dir;
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
		{
			dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
			if (Storage.qualifies(dir))
			{
				return dir;
			}
		}

		dir = context.getExternalFilesDir("Documents");
		if (Storage.qualifies(dir))
		{
			return dir;
		}

		// top-level public external storage directory (KITKAT for DIRECTORY_DOCUMENTS)

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
		{
			dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
			if (Storage.qualifies(dir))
			{
				return dir;
			}
		}

		// top-level public in external
		try
		{
			final String storage = Storage.getExternalStorage();
			if (storage != null)
			{
				dir = new File(storage, Storage.TREEBOLICDIR);
				if (Storage.qualifies(dir))
				{
					return dir;
				}
			}
		}
		catch (@NonNull final Throwable ignored)
		{
			//
		}

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

	// E X T E R N A L   S T O R A G E

	/**
	 * Cached external storage
	 */
	@Nullable
	static private String extStorage = null;

	/**
	 * Get external storage
	 *
	 * @return external storage directory
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public static String getExternalStorage()
	{
		if (Storage.extStorage == null)
		{
			Storage.extStorage = Storage.discoverExternalStorage();
		}
		return Storage.extStorage;
	}

	/**
	 * Discover external storage
	 *
	 * @return (cached) external storage directory
	 */
	@Nullable
	static private String discoverExternalStorage()
	{
		// S E C O N D A R Y

		// all secondary sdcards (all exclude primary) separated by ":"
		final String secondaryStoragesStr = System.getenv("SECONDARY_STORAGE");

		// add all secondary storages
		if (secondaryStoragesStr != null && !secondaryStoragesStr.isEmpty())
		{
			// all secondary sdcards split into array
			final String[] secondaryStorages = secondaryStoragesStr.split(File.pathSeparator);
			if (secondaryStorages.length > 0)
			{
				return secondaryStorages[0];
			}
		}

		// P R I M A R Y E M U L A T E D

		// primary emulated sdcard
		final String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
		if (emulatedStorageTarget != null && !emulatedStorageTarget.isEmpty())
		{
			// device has emulated extStorage; external extStorage paths should have userId burned into them.
			final String userId;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				userId = "";
			}
			else
			{
				userId = getUserId();
			}

			// /extStorage/emulated/0[1,2,...]
			if (!userId.isEmpty())
			{
				return emulatedStorageTarget + File.separatorChar + userId;
			}
			return emulatedStorageTarget;
		}

		// P R I M A R Y N O N E M U L A T E D

		// primary physical sdcard (not emulated)
		final String externalStorage = System.getenv("EXTERNAL_STORAGE");

		// device has physical external extStorage; use plain paths.
		if (externalStorage != null && !externalStorage.isEmpty())
		{
			return externalStorage;
		}

		// EXTERNAL_STORAGE undefined; falling back to default.
		// return "/extStorage/sdcard0";

		return null;
	}

	/**
	 * Discover primary emulated external storage directory
	 *
	 * @return primary emulated external storage directory
	 */
	@Nullable
	public static File discoverPrimaryEmulatedExternalStorage()
	{
		// primary emulated sdcard
		final String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
		if (emulatedStorageTarget != null && !emulatedStorageTarget.isEmpty())
		{
			// device has emulated extStorage
			// external extStorage paths should have userId burned into them
			final String userId = getUserId();

			// /extStorage/emulated/0[1,2,...]
			if (/*userId == null ||*/ userId.isEmpty())
			{
				return new File(emulatedStorageTarget);
			}
			else
			{
				return new File(emulatedStorageTarget + File.separatorChar + userId);
			}
		}
		return null;
	}

	/**
	 * Discover primary physical external storage directory
	 *
	 * @return primary physical external storage directory
	 */
	@Nullable
	public static File discoverPrimaryPhysicalExternalStorage()
	{
		final String externalStorage = System.getenv("EXTERNAL_STORAGE");
		// device has physical external extStorage; use plain paths.
		if (externalStorage != null && !externalStorage.isEmpty())
		{
			return new File(externalStorage);
		}

		return null;
	}

	/**
	 * Discover secondary external storage directories
	 *
	 * @return secondary external storage directories
	 */
	@Nullable
	public static File[] discoverSecondaryExternalStorage()
	{
		// all secondary sdcards (all except primary) separated by ":"
		String secondaryStoragesEnv = System.getenv("SECONDARY_STORAGE");
		if ((secondaryStoragesEnv == null) || secondaryStoragesEnv.isEmpty())
		{
			secondaryStoragesEnv = System.getenv("EXTERNAL_SDCARD_STORAGE");
		}

		// addItem all secondary storages
		if (secondaryStoragesEnv != null && !secondaryStoragesEnv.isEmpty())
		{
			// all secondary sdcards split into array
			final String[] paths = secondaryStoragesEnv.split(File.pathSeparator);
			final List<File> dirs = new ArrayList<>();
			for (String path : paths)
			{
				final File dir = new File(path);
				if (dir.exists())
				{
					dirs.add(dir);
				}
			}
			return dirs.toArray(new File[0]);
		}
		return null;
	}

	// U S E R I D

	/**
	 * User id
	 *
	 * @return user id
	 */
	@NonNull
	static private String getUserId()
	{
		final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		final String[] folders = path.split(File.separator);
		final String lastFolder = folders[folders.length - 1];
		boolean isDigit = false;
		try
		{
			Integer.valueOf(lastFolder);
			isDigit = true;
		}
		catch (@NonNull final NumberFormatException ignored)
		{
			//
		}
		return isDigit ? lastFolder : "";
	}
}
