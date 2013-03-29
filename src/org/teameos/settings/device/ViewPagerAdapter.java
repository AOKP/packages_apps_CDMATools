
package org.teameos.settings.device;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Pair<Fragment, String>> mFragments;
    private Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFragments = new ArrayList<Pair<Fragment, String>>();
        if(InitHelper.isToroplus(context)) {
        mFragments.add(Pair.create(
                (Fragment) UpdatesPreferences.newInstance(R.xml.updates_and_menus),
                mContext.getString(R.string.eos_ota_menus)));
        }
        if(InitHelper.isTuna(context) || InitHelper.isCrespo(context)) {
        mFragments.add(Pair.create((Fragment) DiagPreferences.newInstance(R.xml.diagnostics),
                mContext.getString(R.string.eos_diag)));
        }
        if(InitHelper.isTuna(context)) {
        mFragments.add(Pair.create((Fragment) PrlPreferences.newInstance(R.xml.prl_management),
                mContext.getString(R.string.prl_management)));
        }
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return mFragments.get(position).second;
    }

    @Override
    public int getCount()
    {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        // TODO Auto-generated method stub
        return mFragments.get(position).first;
    }

    @Override
    public Object instantiateItem(View pager, int position)
    {
        return null;
    }

    @Override
    public void destroyItem(View pager, int position, Object view)
    {
    }

    @Override
    public void finishUpdate(View view) {
    }

    @Override
    public void restoreState(Parcelable p, ClassLoader c) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View view) {
    }
}
