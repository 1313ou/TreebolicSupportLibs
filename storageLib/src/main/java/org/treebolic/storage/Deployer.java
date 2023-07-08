/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.storage;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Deployer
{
	private static final String TAG = "StorageUtils";

	// C O P Y A S S E T

	/**
	 * Copy asset file
	 *
	 * @param context  context
	 * @param fileName file in assets
	 * @return uri of copied file
	 */
	@Nullable
	public static Uri copyAssetFile(@NonNull final Context context, @NonNull final String fileName)
	{
		final AssetManager assetManager = context.getAssets();
		final File dir = Storage.getTreebolicStorage(context);

		//noinspection ResultOfMethodCallIgnored
		dir.mkdirs();
		final File file = new File(dir, fileName);
		if (copyAsset(assetManager, fileName, file.getAbsolutePath()))
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
	private static boolean copyAsset(@NonNull final AssetManager assetManager, @NonNull final String assetPath, @NonNull final String toPath)
	{
		try
		{
			//noinspection ResultOfMethodCallIgnored
			new File(toPath).createNewFile();
		}
		catch (IOException e)
		{
			return false;
		}
		try (InputStream in = assetManager.open(assetPath); @SuppressWarnings("IOStreamConstructor") OutputStream out = new FileOutputStream(toPath))
		{
			copyFile(in, out);
			return true;
		}
		catch (@NonNull final Exception ignored)
		{
			return false;
		}
	}

	/**
	 * Copy file to path
	 *
	 * @param fromPath source path
	 * @param toPath   destination path
	 * @return true if successful
	 */
	public static boolean copyFile(@NonNull final String fromPath, @NonNull final String toPath)
	{
		try
		{
			//noinspection ResultOfMethodCallIgnored
			new File(toPath).createNewFile();
		}
		catch (IOException e)
		{
			return false;
		}

		try (@SuppressWarnings("IOStreamConstructor") InputStream in = new FileInputStream(fromPath); @SuppressWarnings("IOStreamConstructor") OutputStream out = new FileOutputStream(toPath))
		{
			copyFile(in, out);
			return true;
		}
		catch (@NonNull final Exception ignored)
		{
			return false;
		}
	}

	/**
	 * Copy in stream to out stream
	 *
	 * @param in  in stream
	 * @param out out stream
	 * @throws IOException io exception
	 */
	@SuppressWarnings("WeakerAccess")
	public static void copyFile(@NonNull final InputStream in, @NonNull final OutputStream out) throws IOException
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
	 * Expand asset file
	 *
	 * @param context  context
	 * @param fileName zip file in assets
	 * @return uri of dest dir
	 */
	@Nullable
	@SuppressWarnings({"UnusedReturnValue"})
	public static Uri expandZipAssetFile(@NonNull final Context context, @NonNull final String fileName)
	{
		final AssetManager assetManager = context.getAssets();
		final File dir = Storage.getTreebolicStorage(context);
		//noinspection ResultOfMethodCallIgnored
		dir.mkdirs();
		if (expandZipAsset(assetManager, fileName, dir.getAbsolutePath()))
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
	private static boolean expandZipAsset(@NonNull final AssetManager assetManager, @NonNull final String assetPath, @NonNull final String toPath)
	{
		try (InputStream in = assetManager.open(assetPath))
		{
			expandZip(in, null, new File(toPath));
			return true;
		}
		catch (@NonNull final Exception ignored)
		{
			return false;
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
	@NonNull
	@SuppressWarnings("UnusedReturnValue")
	static private File expandZip(@NonNull final InputStream in, @SuppressWarnings("SameParameterValue") final String pathPrefixFilter0, @NonNull final File destDir) throws IOException
	{
		// prefix
		String pathPrefixFilter = pathPrefixFilter0;
		if (pathPrefixFilter != null && !pathPrefixFilter.isEmpty() && pathPrefixFilter.charAt(0) == File.separatorChar)
		{
			pathPrefixFilter = pathPrefixFilter.substring(1);
		}

		// create output directory if not exists
		//noinspection ResultOfMethodCallIgnored
		destDir.mkdir();

		// read and expand entries
		try (ZipInputStream zis = new ZipInputStream(in))
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
							final String parent = outFile.getParent();
							if (parent != null)
							{
								File dir = new File(parent);
								boolean created = dir.mkdirs();
								Log.d(TAG, dir + " created=" + created + " exists=" + dir.exists());
							}

							// output

							// copy
							try (FileOutputStream os = new FileOutputStream(outFile))
							{
								int len;
								while ((len = zis.read(buffer)) > 0)
								{
									os.write(buffer, 0, len);
								}
							}
						}
					}
				}
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
		}
		return destDir;
	}

	/**
	 * Cleanup data storage
	 *
	 * @param context context
	 */
	public static void cleanup(@NonNull final Context context)
	{
		final File dir = Storage.getTreebolicStorage(context);
		File[] dirContent = dir.listFiles();
		if (dirContent != null)
		{
			for (final File file : dirContent)
			{
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}
}
