package org.treebolic.download;

import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Deployer
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class Deploy
{
	/**
	 * Log tag
	 */
	private static final String TAG = "Deploy";

	/**
	 * Copy stream to file
	 *
	 * @param in     input stream
	 * @param toFile dest file
	 * @throws IOException io exception
	 */
	public static void copy(final InputStream in, final File toFile) throws IOException
	{
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(toFile);

			final byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);
			}
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}
	}

	/**
	 * Expand archive stream to dir
	 *
	 * @param in      input stream
	 * @param toDir   to directory
	 * @param asTarGz is tar gz type
	 * @throws IOException io exception
	 */
	public static void expand(final InputStream in, final File toDir, @SuppressWarnings("SameParameterValue") boolean asTarGz) throws IOException
	{
		if (asTarGz)
		{
			extractTarGz(in, toDir, true, ".*", null);
			return;
		}
		expandZip(in, toDir, true, ".*", "META-INF.*");
	}

	/**
	 * Expand zip stream to dir
	 *
	 * @param in      zip file input stream
	 * @param destDir destination dir
	 * @param include include regexp filter
	 * @param exclude exclude regexp filter
	 * @return dest dir
	 */
	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	static public File expandZip(final InputStream in, final File destDir, @SuppressWarnings("SameParameterValue") final boolean flat, @SuppressWarnings("SameParameterValue") final String include, @SuppressWarnings("SameParameterValue") final String exclude) throws IOException
	{
		// patterns
		final Pattern includePattern = include == null ? null : Pattern.compile(include);
		final Pattern excludePattern = exclude == null ? null : Pattern.compile(exclude);

		// create output directory is not exists
		//noinspection ResultOfMethodCallIgnored
		destDir.mkdir();

		// buffer
		final byte[] buffer = new byte[1024];

		// read and expand entries
		ZipInputStream zipIn = null;
		try
		{
			zipIn = new ZipInputStream(in);

			// loop through entries
			for (ZipEntry zipEntry = zipIn.getNextEntry(); zipEntry != null; zipEntry = zipIn.getNextEntry())
			{
				String entryName = zipEntry.getName();
				Log.d(Deploy.TAG, "Entry " + entryName);

				// include
				if (includePattern != null)
				{
					if (!includePattern.matcher(entryName).matches())
					{
						zipIn.closeEntry();
						continue;
					}
				}

				// exclude
				if (excludePattern != null)
				{
					if (excludePattern.matcher(entryName).matches())
					{
						zipIn.closeEntry();
						continue;
					}
				}

				// expand this entry
				if (zipEntry.isDirectory())
				{
					// create dir if we don't flatten
					if (!flat)
					{
						//noinspection ResultOfMethodCallIgnored
						new File(destDir, entryName).mkdirs();
					}
				}
				else
				{
					// flatten zip hierarchy
					if (flat)
					{
						final int index = entryName.lastIndexOf('/');
						if (index != -1)
						{
							entryName = entryName.substring(index + 1);
						}
					}

					// create destination
					final File destFile = new File(destDir, entryName);
					Log.d(Deploy.TAG, "Unzip to " + destFile.getCanonicalPath());
					//noinspection ResultOfMethodCallIgnored
					destFile.createNewFile();

					// copy
					BufferedOutputStream bout = null;
					try
					{
						bout = new BufferedOutputStream(new FileOutputStream(destFile));
						for (int len = zipIn.read(buffer); len != -1; len = zipIn.read(buffer))
						{
							bout.write(buffer, 0, len);
						}
					}
					finally
					{
						if (bout != null)
						{
							try
							{
								bout.close();
							}
							catch (IOException ignored)
							{
							}

						}
					}
				}
			}
			zipIn.closeEntry();
		}
		finally
		{
			if (zipIn != null)
			{
				try
				{
					zipIn.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}

		return destDir;
	}

	/**
	 * Extract tar.gz stream
	 *
	 * @param in      input stream
	 * @param destDir destination dir
	 * @param flat    flatten
	 * @param include include regexp filter
	 * @param exclude exclude regexp filter
	 * @return dest dir
	 * @throws IOException io exception
	 */
	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	static public File extractTarGz(final InputStream in, final File destDir, @SuppressWarnings("SameParameterValue") final boolean flat, @SuppressWarnings("SameParameterValue") final String include, @SuppressWarnings("SameParameterValue") final String exclude) throws IOException
	{
		final Pattern includePattern = include == null ? null : Pattern.compile(include);
		final Pattern excludePattern = exclude == null ? null : Pattern.compile(exclude);

		// create output directory is not exists
		//noinspection ResultOfMethodCallIgnored
		destDir.mkdirs();

		// buffer
		final byte[] buffer = new byte[1024];

		// input stream
		TarArchiveInputStream tarIn = null;
		try
		{
			tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(in)));

			// loop through entries
			for (TarArchiveEntry tarEntry = tarIn.getNextTarEntry(); tarEntry != null; tarEntry = tarIn.getNextTarEntry())
			{
				String entryName = tarEntry.getName();

				// include
				if (includePattern != null)
				{
					if (!includePattern.matcher(entryName).matches())
					{
						continue;
					}
				}

				// exclude
				if (excludePattern != null)
				{
					if (excludePattern.matcher(entryName).matches())
					{
						continue;
					}
				}

				// expand this entry
				if (tarEntry.isDirectory())
				{
					// create dir if we don't flatten
					if (!flat)
					{
						//noinspection ResultOfMethodCallIgnored
						new File(destDir, entryName).mkdirs();
					}
				}
				else
				{
					// flatten tar hierarchy
					if (flat)
					{
						final int index = entryName.lastIndexOf('/');
						if (index != -1)
						{
							entryName = entryName.substring(index + 1);
						}
					}

					// create destination file with same name as entry
					final File destFile = new File(destDir, entryName);
					Log.d(Deploy.TAG, "Untar to " + destFile.getCanonicalPath());
					//noinspection ResultOfMethodCallIgnored
					destFile.createNewFile();

					// copy
					BufferedOutputStream bout = null;
					try
					{
						bout = new BufferedOutputStream(new FileOutputStream(destFile));
						for (int len = tarIn.read(buffer); len != -1; len = tarIn.read(buffer))
						{
							bout.write(buffer, 0, len);
						}
					}
					finally
					{
						if (bout != null)
						{
							try
							{
								bout.close();
							}
							catch (IOException ignored)
							{
							}

						}
					}
				}
			}
		}
		finally
		{
			if (tarIn != null)
			{
				try
				{
					tarIn.close();
				}
				catch (IOException ignored)
				{
				}

			}
		}
		return destDir;
	}
}
