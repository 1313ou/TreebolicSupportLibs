package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class Tip extends AppCompatDialogFragment
{
	@SuppressWarnings("WeakerAccess")
	public static Tip newInstance()
	{
		return new Tip();
	}

	@NonNull
	@SuppressLint("InflateParams")
	@Override
	public AppCompatDialog onCreateDialog(@SuppressWarnings("UnusedParameters") final Bundle savedInstanceState)
	{
		final AppCompatDialog dialog = new AppCompatDialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_tips);

		final Window window = dialog.getWindow();
		if (window != null)
		{
			window.setBackgroundDrawableResource(R.drawable.bg_semitransparent_rounded);
		}

		final ImageButton button = dialog.findViewById(R.id.tip_dismiss);
		assert button != null;
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
		final AppCompatDialogFragment newFragment = Tip.newInstance();
		newFragment.show(fragmentManager, "dialog");
	}
}
