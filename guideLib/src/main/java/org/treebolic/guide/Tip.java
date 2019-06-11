package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
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
	public AppCompatDialog onCreateDialog(final Bundle savedInstanceState)
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
		button.setOnClickListener(v -> dialog.cancel());
		return dialog;
	}

	/**
	 * Show tips
	 */
	static public void show(@NonNull final FragmentManager fragmentManager)
	{
		final AppCompatDialogFragment newFragment = Tip.newInstance();
		newFragment.show(fragmentManager, "dialog");
	}
}
