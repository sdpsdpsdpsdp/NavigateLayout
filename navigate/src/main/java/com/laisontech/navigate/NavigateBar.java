package com.laisontech.navigate;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SDP
 * on 2019/6/4
 * Des：导航栏
 */
public class NavigateBar extends LinearLayout implements View.OnClickListener {
    private static final String NAV_TAG = NavigateBar.class.getSimpleName();
    private List<ViewHolder> mViewHolderList;
    private OnTabSelectedListener mTabSelectListener;
    private FragmentActivity mFragmentActivity;
    private String mCurrentTag;
    private String mRestoreTag;

    /**
     * 主内容显示区域View的id
     */
    private int mMainContentLayoutId;

    /**
     * 选中的Tab文字颜色
     */
    private ColorStateList mSelectedTextColor;

    /**
     * 正常的Tab文字颜色
     */
    private ColorStateList mNormalTextColor;

    /**
     * 默认选中的tab index
     */
    private int mDefaultSelectedTab = 0;

    /**
     * 当前选中的tab
     */
    private int mCurrentSelectedTab;

    public NavigateBar(Context context) {
        this(context, null);
    }

    public NavigateBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigateBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigateBar);
        this.mMainContentLayoutId = typedArray.getResourceId(R.styleable.NavigateBar_containerId, 0);
        typedArray.recycle();
        this.mViewHolderList = new ArrayList<>();
    }

    /**
     * 添加tab
     */
    public void addTab(Class frameLayoutClass, TabParam tabParam) {
        int defaultLayout = R.layout.view_navigate_tabbar;
        View view = LayoutInflater.from(getContext()).inflate(defaultLayout, null);
        view.setFocusable(true);

        ViewHolder holder = new ViewHolder();
        holder.tabIndex = this.mViewHolderList.size();
        holder.fragmentClass = frameLayoutClass;
        holder.tag = tabParam.title;
        holder.pageParam = tabParam;
        holder.tabIcon = view.findViewById(R.id.tab_icon);
        holder.tabTitle = view.findViewById(R.id.tab_title);

        if (TextUtils.isEmpty(tabParam.title)) {
            holder.tabTitle.setVisibility(View.INVISIBLE);
        } else {
            holder.tabTitle.setText(tabParam.title);
        }

        if (tabParam.titleSize > 0) {
            holder.tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabParam.titleSize);
        }

        if (this.mNormalTextColor != null) {
            holder.tabTitle.setTextColor(this.mNormalTextColor);
        }

        if (tabParam.backgroundColor > 0) {
            view.setBackgroundResource(tabParam.backgroundColor);
        }

        if (tabParam.iconResId > 0) {
            holder.tabIcon.setImageResource(tabParam.iconResId);
        } else {
            holder.tabIcon.setVisibility(View.INVISIBLE);
        }

        if (tabParam.iconResId > 0 && tabParam.iconSelectedResId > 0) {
            view.setTag(holder);
            view.setOnClickListener(this);
            this.mViewHolderList.add(holder);
        }

        super.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mMainContentLayoutId == 0) {
            throw new RuntimeException("mFrameLayoutId Cannot be 0");
        }
        if (this.mViewHolderList.size() == 0) {
            throw new RuntimeException("mViewHolderList.size Cannot be 0, Please call addTab()");
        }
        if (!(getContext() instanceof FragmentActivity)) {
            throw new RuntimeException("parent activity must is extends FragmentActivity");
        }
        this.mFragmentActivity = (FragmentActivity) getContext();

        ViewHolder defaultHolder = null;

        hideAllFragment();
        if (!TextUtils.isEmpty(this.mRestoreTag)) {
            for (ViewHolder holder : this.mViewHolderList) {
                if (TextUtils.equals(this.mRestoreTag, holder.tag)) {
                    defaultHolder = holder;
                    this.mRestoreTag = null;
                    break;
                }
            }
        } else {
            defaultHolder = this.mViewHolderList.get(this.mDefaultSelectedTab);
        }
        if (defaultHolder != null) {
            this.showFragment(defaultHolder);
        }
    }

    @Override
    public void onClick(View v) {
        Object object = v.getTag();
        if (object != null && object instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) v.getTag();
            showFragment(holder);
            if (this.mTabSelectListener != null) {
                this.mTabSelectListener.onTabSelected(holder);
            }
        }
    }

    /**
     * 显示holder对应的fragment
     */
    private void showFragment(ViewHolder holder) {
        if (holder == null) return;
        FragmentTransaction transaction = this.mFragmentActivity.getSupportFragmentManager().beginTransaction();
        if (isFragmentShown(transaction, holder.tag)) {
            return;
        }
        setCurrSelectedTabByTag(holder.tag);

        Fragment fragment = this.mFragmentActivity.getSupportFragmentManager().findFragmentByTag(holder.tag);
        if (fragment == null) {
            fragment = getFragmentInstance(holder.tag);
            transaction.add(this.mMainContentLayoutId, fragment, holder.tag);
        } else {
            transaction.show(fragment);
        }
        transaction.commit();
        this.mCurrentSelectedTab = holder.tabIndex;
    }

    private boolean isFragmentShown(FragmentTransaction transaction, String newTag) {
        if (TextUtils.equals(newTag, this.mCurrentTag)) {
            return true;
        }

        if (TextUtils.isEmpty(this.mCurrentTag)) {
            return false;
        }

        Fragment fragment = this.mFragmentActivity.getSupportFragmentManager().findFragmentByTag(this.mCurrentTag);
        if (fragment != null && !fragment.isHidden()) {
            transaction.hide(fragment);
        }

        return false;
    }

    /**
     * 设置当前选中tab的图片和文字颜色
     */
    private void setCurrSelectedTabByTag(String tag) {
        if (TextUtils.equals(this.mCurrentTag, tag)) {
            return;
        }
        for (ViewHolder holder : this.mViewHolderList) {
            if (TextUtils.equals(this.mCurrentTag, holder.tag)) {
                holder.tabIcon.setImageResource(holder.pageParam.iconResId);
                holder.tabTitle.setTextColor(this.mNormalTextColor);
            } else if (TextUtils.equals(tag, holder.tag)) {
                holder.tabIcon.setImageResource(holder.pageParam.iconSelectedResId);
                holder.tabTitle.setTextColor(this.mSelectedTextColor);
            }
        }
        this.mCurrentTag = tag;
    }

    /**
     * 获取fragment实例
     */
    private Fragment getFragmentInstance(String tag) {
        Fragment fragment = null;
        for (ViewHolder holder : this.mViewHolderList) {
            if (TextUtils.equals(tag, holder.tag)) {
                try {
                    fragment = (Fragment) Class.forName(holder.fragmentClass.getName()).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return fragment;
    }

    /**
     * 隐藏所有的fragment
     */
    private void hideAllFragment() {
        if (this.mViewHolderList == null || this.mViewHolderList.size() == 0) {
            return;
        }
        FragmentTransaction transaction = this.mFragmentActivity.getSupportFragmentManager().beginTransaction();
        for (ViewHolder holder : this.mViewHolderList) {
            Fragment fragment = this.mFragmentActivity.getSupportFragmentManager().findFragmentByTag(holder.tag);
            if (fragment != null && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    /**
     * 设置选中的tab文字颜色
     */
    public void setSelectedTabTextColor(ColorStateList selectedTextColor) {
        this.mSelectedTextColor = selectedTextColor;
    }

    /**
     * 设置选中的tab文字颜色
     */
    public void setSelectedTabTextColor(int color) {
        this.mSelectedTextColor = ColorStateList.valueOf(color);
    }

    /**
     * 设置tab文字颜色
     */
    public void setTabTextColor(ColorStateList color) {
        this.mNormalTextColor = color;
    }

    /**
     * 设置tab文字颜色
     */
    public void setTabTextColor(int color) {
        this.mNormalTextColor = ColorStateList.valueOf(color);
    }

    /**
     * 设置fragment布局文件
     */
    public void setFrameLayoutId(int frameLayoutId) {
        this.mMainContentLayoutId = frameLayoutId;
    }

    /**
     * 恢复状态
     *
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mRestoreTag = savedInstanceState.getString(NAV_TAG);
        }
    }

    /**
     * 保存状态
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(NAV_TAG, this.mCurrentTag);
    }

    /**
     * ViewHolder
     */
    private static class ViewHolder {
        String tag;
        TabParam pageParam;
        ImageView tabIcon;
        TextView tabTitle;
        Class fragmentClass;
        int tabIndex;
    }

    /**
     * tab参数类
     */
    static class TabParam {
        int backgroundColor = android.R.color.white;
        int iconResId;
        int iconSelectedResId;
        int titleResId;
        String title;
        float titleSize;

        TabParam(String title) {
            this.title = title;
        }

        TabParam(@StringRes int titleResId) {
            this.titleResId = titleResId;
        }

        TabParam(String title, float titleSize) {
            this.title = title;
            this.titleSize = titleSize;
        }

        TabParam(@StringRes int titleResId, float titleSize) {
            this.titleResId = titleResId;
            this.titleSize = titleSize;
        }

        /**
         * 构造方法
         */
        TabParam(@DrawableRes int iconSelectedResId, @DrawableRes int iconResId, String title) {
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.title = title;
        }

        /**
         * 构造方法
         */
        TabParam(@DrawableRes int iconSelectedResId, @DrawableRes int iconResId, @StringRes int titleResId) {
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
        }

        /**
         * 构造方法
         */
        TabParam(@DrawableRes int iconSelectedResId, @DrawableRes int iconResId, String title, float titleSize) {
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.title = title;
            this.titleSize = titleSize;
        }

        /**
         * 构造方法
         */
        TabParam(@DrawableRes int iconSelectedResId, @DrawableRes int iconResId, @StringRes int titleResId, float titleSize) {
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.titleSize = titleSize;
        }

        /**
         * 构造方法
         */
        TabParam(@ColorRes int backgroundColor, @DrawableRes int iconSelectedResId, @DrawableRes int iconResId, @StringRes int titleResId) {
            this.backgroundColor = backgroundColor;
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
        }

        /**
         * 构造方法
         */
        TabParam(@ColorRes int backgroundColor, @DrawableRes int iconSelectedResId, @DrawableRes int iconResId, String title) {
            this.backgroundColor = backgroundColor;
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.title = title;
        }

        /**
         * 构造方法
         */
        TabParam(@ColorRes int backgroundColor, @DrawableRes int iconSelectedResId, @DrawableRes int iconResId, @StringRes int titleResId, float titleSize) {
            this.backgroundColor = backgroundColor;
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.titleSize = titleSize;
        }

        /**
         * 构造方法
         */
        TabParam(@ColorRes int backgroundColor, @DrawableRes int iconSelectedResId, @DrawableRes int iconResId, String title, float titleSize) {
            this.backgroundColor = backgroundColor;
            this.iconSelectedResId = iconSelectedResId;
            this.iconResId = iconResId;
            this.title = title;
            this.titleSize = titleSize;
        }
    }

    /**
     * tab选中监听器
     */
    public interface OnTabSelectedListener {
        void onTabSelected(ViewHolder holder);
    }

    /**
     * 设置tab选中监听器
     */
    public void setTabSelectListener(OnTabSelectedListener tabSelectListener) {
        this.mTabSelectListener = tabSelectListener;
    }

    /**
     * 设置默认选中的tab
     */
    public void setDefaultSelectedTab(int index) {
        if (index >= 0 && index < this.mViewHolderList.size()) {
            this.mDefaultSelectedTab = index;
        }
    }

    /**
     * 设置当前选中的tab
     */
    public void setCurrentSelectedTab(int index) {
        if (index >= 0 && index < this.mViewHolderList.size()) {
            ViewHolder holder = this.mViewHolderList.get(index);
            this.showFragment(holder);
        }
    }

    /**
     * 获取当前选中的tab
     */
    public int getCurrentSelectedTab() {
        return this.mCurrentSelectedTab;
    }

    private void checkAppCompatTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.colorPrimary});
        boolean failed = !a.hasValue(0);
        a.recycle();
        if (failed) {
            throw new IllegalArgumentException("You need to use a Theme.AppCompat theme " + "(or descendant) with the design library.");
        }
    }

}
