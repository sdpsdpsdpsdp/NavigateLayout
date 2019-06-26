package com.laisontech.navigate;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by SDP
 * on 2019/6/4
 * Des：
 */
public class NavigateLayout extends RelativeLayout {
    private int mNavLayoutHeight = 100;
    private int mNavLayoutBgColor;
    private int mAddViewResId;
    private int mAddViewWidthAndHeight = 70;
    private int mNavBarHeight = 50;
    private int mNavBarSelectedColor;
    private int mNavBarUnselectedColor;
    private static float mNavBarTextSize = 10;
    private NavigateBar mNavigateBar;
    private ImageView mAddView;
    private RelativeLayout mNavLayout;
    private View mView;
    private OnAddViewClickListener mAddViewClickListener;

    public NavigateLayout(Context context) {
        this(context, null);
    }

    public NavigateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigateLayout);

        this.mNavLayoutHeight = (int) typedArray.getDimension(R.styleable.NavigateLayout_navigateLayoutHeight, mNavLayoutHeight);
        this.mNavLayoutHeight = dp2px(context, mNavLayoutHeight);

        this.mNavLayoutBgColor = android.R.color.transparent;
        this.mNavLayoutBgColor = typedArray.getColor(R.styleable.NavigateLayout_navigateLayoutBgColor, color(context, mNavLayoutBgColor));

        this.mAddViewResId = R.drawable.default_drawable_add;
        this.mAddViewResId = typedArray.getResourceId(R.styleable.NavigateLayout_navigateAddViewResId, mAddViewResId);

        this.mAddViewWidthAndHeight = (int) typedArray.getDimension(R.styleable.NavigateLayout_navigateBarHeight, mAddViewWidthAndHeight);
        this.mAddViewWidthAndHeight = dp2px(context, mAddViewWidthAndHeight);

        this.mNavBarHeight = (int) typedArray.getDimension(R.styleable.NavigateLayout_navigateBarHeight, mNavBarHeight);
        this.mNavBarHeight = dp2px(context, mNavBarHeight);

        this.mNavBarSelectedColor = android.R.color.black;
        this.mNavBarSelectedColor = typedArray.getColor(R.styleable.NavigateLayout_navigateBarSelectedColor, color(context, mNavBarSelectedColor));

        this.mNavBarUnselectedColor = android.R.color.darker_gray;
        this.mNavBarUnselectedColor = typedArray.getColor(R.styleable.NavigateLayout_navigateBarUnselectedColor, color(context, mNavBarUnselectedColor));

        mNavBarTextSize = typedArray.getDimension(R.styleable.NavigateLayout_navigateBarTitleSize, mNavBarTextSize);
        mNavBarTextSize = sp2px(context, mNavBarTextSize);

        typedArray.recycle();

        initViews(context);
        setParams();
    }

    private void initViews(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.view_navigate_layout, null);
        mNavLayout = mView.findViewById(R.id.rl_add_view);
        mNavigateBar = mView.findViewById(R.id.main_tab_bar);
        mAddView = mView.findViewById(R.id.tab_add_view);
    }

    private void setParams() {
        LayoutParams navLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, mNavLayoutHeight);
        navLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
        mNavLayout.setLayoutParams(navLayoutParams);
        mNavLayout.setBackgroundColor(mNavLayoutBgColor);

        mAddView.setImageResource(mAddViewResId);
        LayoutParams addViewParams = new LayoutParams(mAddViewWidthAndHeight, mAddViewWidthAndHeight);
        addViewParams.addRule(CENTER_IN_PARENT);
        mAddView.setLayoutParams(addViewParams);

        mNavigateBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mNavBarHeight));
        mNavigateBar.setTabTextColor(mNavBarUnselectedColor);
        mNavigateBar.setSelectedTabTextColor(mNavBarSelectedColor);
    }

    /**
     * 恢复 bundle
     */

    public void restoreBundleInstance(Bundle bundle) {
        if (mNavigateBar != null) {
            mNavigateBar.onRestoreInstanceState(bundle);
        }
    }

    /**
     * 保存 bundle
     */
    public void saveBundleInstance(Bundle bundle) {
        if (mNavigateBar != null) {
            mNavigateBar.onSaveInstanceState(bundle);
        }
    }

    /**
     * 设置数据
     */
    public void setNavResource(List<TabResource> tabResources) {
        if (tabResources == null || tabResources.size() < 1) return;

        //设置数据
        for (TabResource resource : tabResources) {
            mNavigateBar.addTab(resource.getFragmentClass(), resource.getTabParam());
        }
        //判断是否需要显示添加View
        boolean showAddView = false;
        for (TabResource resource : tabResources) {
            if (resource.getFragmentClass() == null) {
                showAddView = true;
                break;
            }
        }
        mNavLayout.setVisibility(showAddView ? VISIBLE : GONE);
        if (mAddView != null && mAddView.getVisibility() == VISIBLE) {
            mAddView.setOnClickListener(v -> {
                if (mAddViewClickListener != null) {
                    mAddViewClickListener.onAddViewClick();
                }
            });
        }
        this.addView(mView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setAddViewClickListener(OnAddViewClickListener addViewClickListener) {
        this.mAddViewClickListener = addViewClickListener;
    }


    private int color(Context context, int resId) {
        return context.getResources().getColor(resId);
    }

    public static class TabResource {
        private Class fragmentClass;
        private NavigateBar.TabParam tabParam;

        public TabResource(String navTabTitle) {
            this.fragmentClass = null;
            this.tabParam = new NavigateBar.TabParam(navTabTitle, mNavBarTextSize);
        }

        public TabResource(@StringRes int tabResId) {
            this.fragmentClass = null;
            this.tabParam = new NavigateBar.TabParam(tabResId, mNavBarTextSize);
        }

        public TabResource(Class<?> fragmentClass, @DrawableRes int iconSelectedId, @DrawableRes int iconUnselectedId, String title) {
            this.fragmentClass = fragmentClass;
            this.tabParam = new NavigateBar.TabParam(iconSelectedId, iconUnselectedId, title, mNavBarTextSize);
        }

        public TabResource(Class fragmentClass, @DrawableRes int iconSelectedId, @DrawableRes int iconUnselectedId, @StringRes int titleResId) {
            this.fragmentClass = fragmentClass;
            this.tabParam = new NavigateBar.TabParam(iconSelectedId, iconUnselectedId, titleResId, mNavBarTextSize);
        }

        Class getFragmentClass() {
            return fragmentClass;
        }

        NavigateBar.TabParam getTabParam() {
            return tabParam;
        }
    }

    public interface OnAddViewClickListener {
        void onAddViewClick();
    }

    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    private int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
}
