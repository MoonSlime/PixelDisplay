package com.pixeldp.launcher.screen;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.pixeldp.model.AppModel;
import com.pixeldp.launcher.R;
import com.pixeldp.util.PreferenceUtil;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class AppGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<AppModel>>, AdapterView.OnItemClickListener {
    private GridView gridView;
    private AppListAdapter mAdapter;
    private int columnSize[] = {5, 4, 3, 3, 2, 2};
    private int prevPosition;

    private int getColumnWidth(int position) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) dpWidth / columnSize[position];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_homescreen, null);
        int position = PreferenceUtil.instance(getActivity()).get("FONT_SIZE", 0) + 1;
        prevPosition = position;
        int columnWidth = getColumnWidth(position);
        int numColumn = columnSize[position];
        mAdapter = new AppListAdapter(getActivity(), position);

        gridView = ButterKnife.findById(view, R.id.fragment_homescreen_gridview);
        gridView.setColumnWidth(columnWidth);
        gridView.setNumColumns(numColumn);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ArrayList<AppModel>> onCreateLoader(int id, Bundle bundle) {
        return new AppsLoader(getActivity());
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<AppModel>> loader, ArrayList<AppModel> apps) {
        mAdapter.setData(apps);
    }

    @Override
    public void onResume() {
        super.onResume();
        int position = PreferenceUtil.instance(getActivity()).get("FONT_SIZE", 0) + 1;
        if (prevPosition != position) {
            refresh();
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppModel>> loader) {
        mAdapter.setData(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        AppModel app = mAdapter.getItem(position);
        if (app != null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName(app.getPackageName(), app.getUrl()));
            startActivity(intent);
        }
    }

    private void refresh() {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag("APPGRID");
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(fragment);
        fragmentTransaction.attach(fragment);
        fragmentTransaction.commit();
    }

}
