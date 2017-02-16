package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class Tip extends DialogFragment
{
	public static Tip newInstance()
	{
		return new Tip();
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_tips);

		final Window window = dialog.getWindow();
		if (window != null)
		{
			window.setBackgroundDrawableResource(R.drawable.bg_semitransparent_rounded);
		}

		final ImageButton button = (ImageButton) dialog.findViewById(R.id.tip_dismiss);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});
		return dialog;
	}

	/**
	 * Show tips
	 */
	static public void show(final FragmentManager fragmentManager)
	{
		final DialogFragment newFragment = Tip.newInstance();
		newFragment.show(fragmentManager, "dialog"); //$NON-NLS-1$
	}
}
