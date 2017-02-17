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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Storage
{
	private static final String TAG = "Storage"; //$NON-NLS-1$

	/**
	 * Treebolic storage preference name
	 */
	public static final String PREF_TREEBOLIC_STORAGE = "pref_storage"; //$NON-NLS-1$

	/**
	 * Treebolic sub directory
	 */
	private static final String TREEBOLICDIR = "treebolic" + '/'; //$NON-NLS-1$

	/**
	 * Cached treebolic storage
	 */
	private static File treebolicStorage = null;

	/**
	 * Cached external storage
	 */
	private static String extStorage = null;

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
			Storage.extStorage = Storage.discoverStorage();
		}
		return Storage.extStorage;
	}

	/**
	 * Discover external storage
	 *
	 * @return (cached) external storage directory
	 */
	static private String discoverStorage()
	{
		// S E C O N D A R Y

		// all secondary sdcards (all exclude primary) separated by ":"
		final String secondaryStoragesStr = System.getenv("SECONDARY_STORAGE"); //$NON-NLS-1$

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
		final String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET"); //$NON-NLS-1$
		if (emulatedStorageTarget != null && !emulatedStorageTarget.isEmpty())
		{
			// device has emulated extStorage; external extStorage paths should have userId burned into them.
			final String userId;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				userId = ""; //$NON-NLS-1$
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
				userId = isDigit ? lastFolder : ""; //$NON-NLS-1$
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
		final String externalStorage = System.getenv("EXTERNAL_STORAGE"); //$NON-NLS-1$

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
	 * Storage types
	 */
	public enum StorageType
	{
		PRIMARY_EMULATED, PRIMARY_PHYSICAL, SECONDARY
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
		final String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET"); //$NON-NLS-1$
		if (emulatedStorageTarget != null && !emulatedStorageTarget.isEmpty())
		{
			// device has emulated extStorage; external extStorage paths should have userId burned into them.
			final String userId;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				userId = ""; //$NON-NLS-1$
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
				userId = isDigit ? lastFolder : ""; //$NON-NLS-1$
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
			final String externalStorage = System.getenv("EXTERNAL_STORAGE"); //$NON-NLS-1$

			// device has physical external extStorage; use plain paths
			if (externalStorage != null && !externalStorage.isEmpty())
			{
				dirs.put(StorageType.PRIMARY_EMULATED, new String[]{externalStorage});
			}
			else
			{
				// EXTERNAL_STORAGE undefined; falling back to default.
				dirs.put(StorageType.PRIMARY_EMULATED, new String[]{"/extStorage/sdcard0"}); //$NON-NLS-1$
			}
		}

		// S E C O N D A R Y

		// all secondary sdcards (all exclude primary) separated by ":"
		final String secondaryStoragesStr = System.getenv("SECONDARY_STORAGE"); //$NON-NLS-1$

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
	@SuppressLint("CommitPrefEdits")
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

		dir = context.getExternalFilesDir("Documents"); //$NON-NLS-1$
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
			Log.d(Storage.TAG, "storage state of " + dir + ": " + Environment.getExternalStorageState(dir)); //$NON-NLS-1$ //$NON-NLS-2$
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
	 * @throws IOException
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
					if (!entryName.endsWith("MANIFEST.MF")) //$NON-NLS-1$
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
}
