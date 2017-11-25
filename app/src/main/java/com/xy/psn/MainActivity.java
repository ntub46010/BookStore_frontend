package com.xy.psn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xy.psn.data.MyHelper;
import com.xy.psn.member.MemberHomeFrag;
import com.xy.psn.product.ProductHomeFrag;
import com.xy.psn.product.ProductSearchActivity;
import com.xy.psn.setting.SettingHomeFrag;
import com.xy.psn.type.DepartmentFrag;

import java.util.ArrayList;
import java.util.List;

import static com.xy.psn.data.MyHelper.fromNotification;
import static com.xy.psn.data.MyHelper.setBoardTitle;

public class MainActivity extends FragmentActivity {
    public static Context context;
    public static Toolbar toolbar;
    public static ViewPager mViewPager;
    private TabLayout mTabLayout;
    public static int lastPosition = 1;
    public static String board = "-1";
    private ViewPagerAdapter adapter;
    public static TextView txtBarTitle;
    public static ImageView btnSearchProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        fromNotification = false;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);

        //初次使用將開啟通知功能
        SharedPreferences sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);
        if (sp.getBoolean(getString(R.string.sp_isFirstUse), true)) {
            sp.edit()
                    .putBoolean(getString(R.string.sp_showNotification), true)
                    .putBoolean(getString(R.string.sp_isFirstUse), false)
                    .apply();
        }

        btnSearchProduct = (ImageView) toolbar.findViewById(R.id.btnSearchProduct);
        btnSearchProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ProductSearchActivity.class));
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(15);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (position == 0 || position == 1) {
                    setBoardTitle();
                    btnSearchProduct.setVisibility(View.VISIBLE);
                }else {
                    txtBarTitle.setText(getString(R.string.app_name));
                    btnSearchProduct.setVisibility(View.GONE);
                }

                MyHelper.canShowProduct = true; //被選一定要顯示
                if (lastPosition == 0 && position == 1) //從科系移動到商品後必重新刷新商品
                    adapter.getItem(position).onResume();
                lastPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        setupViewPager(mViewPager);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(fromNotification ? 2 : lastPosition); //開啟Activity第一個顯示的頁面
    }

    private void setupViewPager(ViewPager viewPager) {
        //加入頁面
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DepartmentFrag(), "科系");
        adapter.addFragment(new ProductHomeFrag(), "商品");
        adapter.addFragment(new MemberHomeFrag(), "會員專區");
        adapter.addFragment(new SettingHomeFrag(), "設定");
        viewPager.setAdapter(adapter);
    }

    //FragmentStatePagerAdapter(畫面滑出便會清除，返回需重新載入)
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
