/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package androidx.preference.legacy.contrib;

import android.annotation.SuppressLint;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;

import org.treebolic.preference.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("Registered")
public class PreferenceActivityCompat extends AppCompatActivity implements PreferenceActivityCompatDelegate.Connector, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
{
	private PreferenceActivityCompatDelegate mDelegate;

	@SuppressLint("RestrictedApi")
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mDelegate = new PreferenceActivityCompatDelegate(this, this);
		mDelegate.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy()
	{
		mDelegate.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		mDelegate.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle state)
	{
		super.onRestoreInstanceState(state);
		mDelegate.onRestoreInstanceState(state);
	}

	@Override
	public void onBackPressed()
	{
		if (mDelegate.onBackPressed())
		{
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onIsMultiPane()
	{
		return getResources().getBoolean(R.bool.dual_pane);
	}

	@SuppressWarnings("EmptyMethod")
	@Override
	public void onBuildHeaders(@NonNull final List<Header> target)
	{
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean isValidFragment(@Nullable final String fragmentName)
	{
		if (getApplicationInfo().targetSdkVersion >= VERSION_CODES.KITKAT)
		{
			throw new RuntimeException("Subclasses of PreferenceActivity must override isValidFragment(String)" + " to verify that the Fragment class is valid! " + getClass().getName() + " has not checked if fragment " + fragmentName + " is valid.");
		}
		else
		{
			return true;
		}
	}

	public int getSelectedItemPosition()
	{
		return mDelegate.getSelectedItemPosition();
	}

	public boolean hasHeaders()
	{
		return mDelegate.hasHeaders();
	}

	@NonNull
	public List<Header> getHeaders()
	{
		return mDelegate.getHeaders();
	}

	public boolean isMultiPane()
	{
		return mDelegate.isMultiPane();
	}

	public void invalidateHeaders()
	{
		mDelegate.invalidateHeaders();
	}

	@SuppressWarnings("WeakerAccess")
	public void loadHeadersFromResource(@XmlRes final int resId, @NonNull final List<Header> target)
	{
		mDelegate.loadHeadersFromResource(resId, target);
	}

	public void setListFooter(@NonNull final View view)
	{
		mDelegate.setListFooter(view);
	}

	public void switchToHeader(@NonNull final Header header)
	{
		mDelegate.switchToHeader(header);
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onPreferenceStartFragment(@NonNull final PreferenceFragmentCompat caller, @NonNull final Preference pref)
	{
		mDelegate.startPreferenceFragment(pref);
		return true;
	}
}