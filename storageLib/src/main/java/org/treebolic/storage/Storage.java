package org.treebolic.storage;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Storage
{
	private static final String TAG = "Storage";

	/**
	 * Treebolic storage preference name
	 */
	public static final String PREF_TREEBOLIC_STORAGE = "pref_storage";

	/**
	 * Treebolic sub directory
	 */
	private static final String TREEBOLICDIR = "treebolic" + '/';

	/**
	 * Cached treebolic storage
	 */
	private static File treebolicStorage = null;

	/**
	 * Cached external storage
	 */
	private static String extStorage = null;

	/**
	 * Storage types
	 */
	enum StorageType
	{
		PRIMARY_EMULATED, PRIMARY_PHYSICAL, SECONDARY
	}

	/**
	 * Directory type
	 *
	 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
	 */
	public enum DirType
	{
		AUTO, APP_EXTERNAL_SECONDARY, APP_EXTERNAL_PRIMARY, PUBLIC_EXTERNAL_SECONDARY, PUBLIC_EXTERNAL_PRIMARY, APP_INTERNAL;

		/**
		 * Compare (sort by preference)
		 *
		 * @param type1 type 1
		 * @param type2 type 2
		 * @return order
		 */
		static public int compare(final DirType type1, final DirType type2)
		{
			int i1 = type1.ordinal();
			int i2 = type2.ordinal();
			return i1 < i2 ? -1 : (i1 == i2 ? 0 : 1);
		}

		public String toDisplay()
		{
			switch (this)
			{
				case AUTO:
					return "auto (internal or adopted)";
				case APP_EXTERNAL_SECONDARY:
					return "secondary";
				case APP_EXTERNAL_PRIMARY:
					return "primary";
				case PUBLIC_EXTERNAL_PRIMARY:
					return "public primary";
				case PUBLIC_EXTERNAL_SECONDARY:
					return "public secondary";
				case APP_INTERNAL:
					return "internal";
			}
			return null;
		}
	}

	/**
	 * Directory with type
	 *
	 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
	 */
	static public class Directory
	{
		private final File file;

		private final DirType type;

		Directory(final File file, final DirType type)
		{
			this.file = file;
			this.type = type;
		}

		DirType getType()
		{
			return this.type;
		}

		CharSequence getValue()
		{
			if (DirType.AUTO == this.type)
			{
				return DirType.AUTO.toString();
			}
			return this.file.getAbsolutePath();
		}

		public File getFile()
		{
			return this.file;
		}
	}

	/**
	 * Get data cache
	 *
	 * @param context context
	 * @return data cache
	 */
	static public File getCacheDir(final Context context)
	{
		// external is first choice
		File cache = context.getExternalCacheDir();

		// internal is second choice
		if (cache == null)
		{
			cache = context.getCacheDir();
		}
		return new File(cache.getAbsolutePath());
	}

	/**
	 * Get external storage
	 *
	 * @return external storage directory
	 */
	static public String getExternalStorage()
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
				final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
				final String[] folders = path.split(File.separator);
				final String lastFolder = folders[folders.length - 1];
				boolean isDigit = false;
				try
				{
					//noinspection ResultOfMethodCallIgnored
					Integer.valueOf(lastFolder);
					isDigit = true;
				}
				catch (final NumberFormatException ignored)
				{
					//
				}
				userId = isDigit ? lastFolder : "";
			}

			// /extStorage/emulated/0[1,2,...]
			if (userId != null && !userId.isEmpty())
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
	 * Get external storage directories
	 *
	 * @return map per type of of external storage directories
	 */
	public static Map<StorageType, String[]> getStorageDirectories()
	{
		// result set of paths
		final Map<StorageType, String[]> dirs = new EnumMap<>(StorageType.class);

		// P R I M A R Y

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
				final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
				final String[] folders = path.split(File.separator);
				final String lastFolder = folders[folders.length - 1];
				boolean isDigit = false;
				try
				{
					//noinspection ResultOfMethodCallIgnored
					Integer.valueOf(lastFolder);
					isDigit = true;
				}
				catch (final NumberFormatException ignored)
				{
					//
				}
				userId = isDigit ? lastFolder : "";
			}
			// /extStorage/emulated/0[1,2,...]
			if (userId == null || userId.isEmpty())
			{
				dirs.put(StorageType.PRIMARY_PHYSICAL, new String[]{emulatedStorageTarget});
			}
			else
			{
				dirs.put(StorageType.PRIMARY_PHYSICAL, new String[]{emulatedStorageTarget + File.separatorChar + userId});
			}
		}
		else
		{
			// primary physical sdcard (not emulated)
			final String externalStorage = System.getenv("EXTERNAL_STORAGE");

			// device has physical external extStorage; use plain paths
			if (externalStorage != null && !externalStorage.isEmpty())
			{
				dirs.put(StorageType.PRIMARY_EMULATED, new String[]{externalStorage});
			}
			else
			{
				// EXTERNAL_STORAGE undefined; falling back to default.
				dirs.put(StorageType.PRIMARY_EMULATED, new String[]{"/extStorage/sdcard0"});
			}
		}

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
				dirs.put(StorageType.SECONDARY, secondaryStorages);
			}
		}

		return dirs;
	}

	/**
	 * Get treebolic storage
	 *
	 * @return treebolic storage directory
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public File getTreebolicStorage(final Context context)
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

		// flag as discovered
		sharedPref.edit().putString(Storage.PREF_TREEBOLIC_STORAGE, Storage.treebolicStorage.getAbsolutePath()).commit();

		return Storage.treebolicStorage;
	}

	/**
	 * Discover Treebolic storage
	 *
	 * @param context context
	 * @return Treebolic storage
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	static public File discoverTreebolicStorage(final Context context)
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
		catch (final Throwable e)
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

		try
		{
			dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
			if (Storage.qualifies(dir))
			{
				return dir;
			}
		}
		catch (final NoSuchFieldError e)
		{
			//
		}

		dir = context.getExternalFilesDir("Documents");
		if (Storage.qualifies(dir))
		{
			return dir;
		}

		// top-level public external storage directory (KITKAT for DIRECTORY_DOCUMENTS)

		try
		{
			dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
			if (Storage.qualifies(dir))
			{
				return dir;
			}
		}
		catch (final Throwable e)
		{
			//
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
		catch (final Throwable e)
		{
			//
		}

		// internal private storage
		return context.getFilesDir();
	}

	/**
	 * Whether the dir qualifies as treebolic storage
	 *
	 * @param dir candidate dir
	 * @return true if it qualifies
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static boolean qualifies(final File dir)
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

	// C O P Y A S S E T

	/**
	 * Copy asset file
	 *
	 * @param context  context
	 * @param fileName file in assets
	 * @return uri of copied file
	 */
	@SuppressWarnings("resource")
	public static Uri copyAssetFile(final Context context, final String fileName)
	{
		final AssetManager assetManager = context.getAssets();
		final File dir = Storage.getTreebolicStorage(context);
		dir.mkdirs();
		final File file = new File(dir, fileName);
		if (Storage.copyAsset(assetManager, fileName, file.getAbsolutePath()))
		{
			return Uri.fromFile(file);
		}
		return null;
	}

	/**
	 * Copy asset file to path
	 *
	 * @param assetManager asset manager
	 * @param assetPath    asset path
	 * @param toPath       destination path
	 * @return true if successful
	 */
	private static boolean copyAsset(final AssetManager assetManager, final String assetPath, final String toPath)
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = assetManager.open(assetPath);
			new File(toPath).createNewFile();
			out = new FileOutputStream(toPath);
			Storage.copyFile(in, out);
			return true;
		}
		catch (final Exception e)
		{
			return false;
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (final IOException e)
				{
					//
				}
			}
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (final IOException e)
				{
					//
				}
			}
		}
	}

	/**
	 * Copy file to path
	 *
	 * @param fromPath source path
	 * @param toPath   destination path
	 * @return true if successful
	 */
	public static boolean copyFile(final String fromPath, final String toPath)
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream(fromPath);
			new File(toPath).createNewFile();
			out = new FileOutputStream(toPath);
			Storage.copyFile(in, out);
			return true;
		}
		catch (final Exception e)
		{
			return false;
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (final IOException e)
				{
					//
				}
			}
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (final IOException e)
				{
					//
				}
			}
		}
	}

	/**
	 * Copy instream to outstream
	 *
	 * @param in  instream
	 * @param out outstream
	 * @throws IOException io exception
	 */
	public static void copyFile(final InputStream in, final OutputStream out) throws IOException
	{
		final byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, read);
		}
	}

	// E X P A N D A S S E T

	/**
	 * Cleanup data storage
	 *
	 * @param context context
	 */
	public static void cleanup(final Context context)
	{
		final File dir = Storage.getTreebolicStorage(context);
		for (final File file : dir.listFiles())
		{
			file.delete();
		}
	}

	/**
	 * Expand asset file
	 *
	 * @param context  context
	 * @param fileName zip file in assets
	 * @return uri of dest dir
	 */
	@SuppressWarnings("resource")
	public static Uri expandZipAssetFile(final Context context, final String fileName)
	{
		final AssetManager assetManager = context.getAssets();
		final File dir = Storage.getTreebolicStorage(context);
		dir.mkdirs();
		if (Storage.expandZipAsset(assetManager, fileName, dir.getAbsolutePath()))
		{
			return Uri.fromFile(dir);
		}
		return null;
	}

	/**
	 * Expand asset file to path
	 *
	 * @param assetManager asset manager
	 * @param assetPath    asset path
	 * @param toPath       destination path
	 * @return true if successful
	 */
	private static boolean expandZipAsset(final AssetManager assetManager, final String assetPath, final String toPath)
	{
		InputStream in = null;
		try
		{
			in = assetManager.open(assetPath);
			Storage.expandZip(in, null, new File(toPath));
			return true;
		}
		catch (final Exception e)
		{
			return false;
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (final IOException e)
				{
					//
				}
			}
		}
	}

	/**
	 * Expand zip stream to dir
	 *
	 * @param in                zip file input stream
	 * @param pathPrefixFilter0 path prefix filter on entries
	 * @param destDir           destination dir
	 * @return dest dir
	 */
	static private File expandZip(final InputStream in, final String pathPrefixFilter0, final File destDir) throws IOException
	{
		// prefix
		String pathPrefixFilter = pathPrefixFilter0;
		if (pathPrefixFilter != null && !pathPrefixFilter.isEmpty() && pathPrefixFilter.charAt(0) == File.separatorChar)
		{
			pathPrefixFilter = pathPrefixFilter.substring(1);
		}

		// create output directory if not exists
		destDir.mkdir();

		// read and expand entries
		final ZipInputStream zis = new ZipInputStream(in);
		try
		{
			// get the zipped file list entry
			final byte[] buffer = new byte[1024];
			ZipEntry entry = zis.getNextEntry();
			while (entry != null)
			{
				if (!entry.isDirectory())
				{
					final String entryName = entry.getName();
					if (!entryName.endsWith("MANIFEST.MF"))
					{
						if (pathPrefixFilter == null || pathPrefixFilter.isEmpty() || entryName.startsWith(pathPrefixFilter))
						{
							// flatten zip hierarchy
							final File outFile = new File(destDir + File.separator + new File(entryName).getName());

							// create all non exists folders else you will hit FileNotFoundException for compressed folder
							new File(outFile.getParent()).mkdirs();

							// output
							final FileOutputStream os = new FileOutputStream(outFile);

							// copy
							try
							{
								int len;
								while ((len = zis.read(buffer)) > 0)
								{
									os.write(buffer, 0, len);
								}
							}
							finally
							{
								try
								{
									os.close();
								}
								catch (IOException ignored)
								{
								}
							}
						}
					}
				}
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
		}
		finally
		{
			try
			{
				zis.close();
			}
			catch (IOException ignored)
			{
			}

			try
			{
				in.close();
			}
			catch (IOException ignored)
			{
			}
		}

		return destDir;
	}

	/**
	 * Get directories as types and values
	 *
	 * @param context context
	 * @return pair of types and values
	 */
	static public Pair<CharSequence[], CharSequence[]> getDirectoriesTypesValues(final Context context)
	{
		final List<CharSequence> types = new ArrayList<>();
		final List<CharSequence> values = new ArrayList<>();
		final List<Directory> dirs = Storage.getDirectories(context);
		for (Directory dir : dirs)
		{
			// types
			types.add(dir.getType().toDisplay());

			// value
			values.add(dir.getFile().getAbsolutePath());
		}
		return new Pair<>(types.toArray(new CharSequence[0]), values.toArray(new CharSequence[0]));
	}

	/**
	 * Get list of directories
	 *
	 * @param context context
	 * @return list of storage directories
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	static private List<Directory> getDirectories(final Context context)
	{
		final String[] tags = {Environment.DIRECTORY_PODCASTS, Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_DCIM, Environment.DIRECTORY_DOCUMENTS};

		final List<Directory> result = new ArrayList<>();
		File dir;

		// P U B L I C

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final File[] externalMediaDirs = context.getExternalMediaDirs();
			if (externalMediaDirs != null)
			{
				for (File mediaStorage : externalMediaDirs)
				{
					//result.add(new Directory(mediaStorage, DirType.PUBLIC_EXTERNAL_SECONDARY));
				}
			}
		}

		// top-level public external storage directory
		for (String tag : tags)
		{
			dir = Environment.getExternalStoragePublicDirectory(tag);
			if (dir.exists())
			{
				result.add(new Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY));
			}
		}

		// top-level public in external
		dir = Environment.getExternalStorageDirectory();
		if (dir != null)
		{
			if (dir.exists())
			{
				result.add(new Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY));
			}
		}

		// S E C O N D A R Y

		// all secondary sdcards split into array
		final File[] secondaries = discoverSecondaryExternalStorage();
		if (secondaries != null)
		{
			for (File secondary : secondaries)
			{
				dir = secondary;
				if (dir.exists())
				{
					result.add(new Directory(dir, DirType.PUBLIC_EXTERNAL_SECONDARY));
				}
			}
		}

		// P R I M A R Y

		// primary emulated sdcard
		dir = discoverPrimaryEmulatedExternalStorage();
		if (dir != null)
		{
			if (dir.exists())
			{
				result.add(new Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY));
			}
		}

		dir = discoverPrimaryPhysicalExternalStorage();
		if (dir != null)
		{
			if (dir.exists())
			{
				result.add(new Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY));
			}
		}

		result.add(new Directory(new File("/storage"), DirType.PUBLIC_EXTERNAL_PRIMARY));
		return result;
	}

	/**
	 * Discover primary emulated external storage directory
	 *
	 * @return primary emulated external storage directory
	 */
	static private File discoverPrimaryEmulatedExternalStorage()
	{
		// primary emulated sdcard
		final String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
		if (emulatedStorageTarget != null && !emulatedStorageTarget.isEmpty())
		{
			// device has emulated extStorage
			// external extStorage paths should have userId burned into them
			final String userId = getUserId();

			// /extStorage/emulated/0[1,2,...]
			if (userId == null || userId.isEmpty())
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
	static private File discoverPrimaryPhysicalExternalStorage()
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
	static private File[] discoverSecondaryExternalStorage()
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
	static private String getUserId()
	{
		final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		final String[] folders = path.split(File.separator);
		final String lastFolder = folders[folders.length - 1];
		boolean isDigit = false;
		try
		{
			//noinspection ResultOfMethodCallIgnored
			Integer.valueOf(lastFolder);
			isDigit = true;
		}
		catch (final NumberFormatException ignored)
		{
			//
		}
		return isDigit ? lastFolder : "";
	}
}
