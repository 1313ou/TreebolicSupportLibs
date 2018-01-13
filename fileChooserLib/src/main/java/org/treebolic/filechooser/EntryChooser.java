package org.treebolic.filechooser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Zip entry chooser
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class EntryChooser
{
	/**
	 * Select callback
	 */
	public interface Callback
	{
		void onSelect(String selected);
	}

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * List of entries
	 */
	private final List<String> list;

	/**
	 * Select click listener
	 */
	private final DialogInterface.OnClickListener listener;

	/**
	 * Constructor
	 *
	 * @param context0  context
	 * @param list0     list of entries
	 * @param listener0 click listener
	 */
	@SuppressWarnings("WeakerAccess")
	public EntryChooser(final Context context0, final List<String> list0, final OnClickListener listener0)
	{
		super();
		this.context = context0;
		this.list = list0;
		this.listener = listener0;
	}

	/**
	 * Show dialog
	 */
	@SuppressWarnings("WeakerAccess")
	public void show()
	{
		final ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context, R.layout.filechooser_entries_zip, this.list);

		final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle(R.string.chooseEntry);
		builder.setAdapter(adapter, this.listener);
		final AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Get archive entries
	 *
	 * @param archive        zip archive
	 * @param negativeFilter negative filter
	 * @param positiveFilter positive filter
	 * @return list of entries
	 * @throws IOException io exception
	 */
	static private List<String> getZipEntries(final File archive, @SuppressWarnings("SameParameterValue") final String negativeFilter, @SuppressWarnings("SameParameterValue") final String positiveFilter) throws IOException
	{
		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile(archive);
			final List<String> result = new ArrayList<>();
			final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements())
			{
				final ZipEntry zipEntry = zipEntries.nextElement();
				final String name = zipEntry.getName();
				if (negativeFilter != null && name.matches(negativeFilter))
				{
					continue;
				}
				if (positiveFilter == null || name.matches(positiveFilter))
				{
					result.add(name);
				}
			}
			return result;
		}
		finally
		{
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}
	}

	/**
	 * Choose entry convenience method
	 *
	 * @param context  context
	 * @param archive  zip archive
	 * @param callback select callback
	 * @throws IOException io exception
	 */
	static public void choose(final Context context, final File archive, final Callback callback) throws IOException
	{
		final List<String> list = EntryChooser.getZipEntries(archive, "(.*gif|.*png|.*jpg|.*properties|.*MF|.*/)", ".*");
		final DialogInterface.OnClickListener listener = (dialog, which) ->
		{
			// The 'which' argument contains the index position of the selected item
			callback.onSelect(list.get(which));
		};
		new EntryChooser(context, list, listener).show();
	}
}
