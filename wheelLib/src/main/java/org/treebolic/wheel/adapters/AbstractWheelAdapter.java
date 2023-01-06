/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.wheel.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Abstract Wheel adapter.
 */
public abstract class AbstractWheelAdapter implements WheelViewAdapter
{
	// Observers
	private List<DataSetObserver> datasetObservers;

	@SuppressWarnings("SameReturnValue")
	@Nullable
	@Override
	public View getEmptyItem(View convertView, ViewGroup parent)
	{
		return null;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer)
	{
		if (this.datasetObservers == null)
		{
			this.datasetObservers = new LinkedList<>();
		}
		this.datasetObservers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer)
	{
		if (this.datasetObservers != null)
		{
			this.datasetObservers.remove(observer);
		}
	}

	/**
	 * Notifies observers about data changing
	 */
	protected void notifyDataChangedEvent()
	{
		if (this.datasetObservers != null)
		{
			for (DataSetObserver observer : this.datasetObservers)
			{
				observer.onChanged();
			}
		}
	}

	/**
	 * Notifies observers about invalidating data
	 */
	@SuppressWarnings("WeakerAccess")
	protected void notifyDataInvalidatedEvent()
	{
		if (this.datasetObservers != null)
		{
			for (DataSetObserver observer : this.datasetObservers)
			{
				observer.onInvalidated();
			}
		}
	}
}
