package com.idea.todo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.idea.todo.R;
import com.idea.todo.adapter.viewpager.ViewPagerAdapter;
import com.idea.todo.constants.C;
import com.idea.todo.db.Database;
import com.idea.todo.frag.dialog.GroupsDialog;
import com.idea.todo.model.GroupsDialogArgs;
import com.idea.todo.wrapper.SharedData;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements
        C,
        ViewPager.OnPageChangeListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener
{

    private ViewPagerAdapter mViewPagerAdapter;
    public static int currentSection;
    ActionBarDrawerToggle toggle;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fabBase)
    FloatingActionMenu fabBase;

    @BindView(R.id.fabAddToDo)
    FloatingActionButton fabAddToDo;

    @BindView(R.id.fabGroupSelect)
    FloatingActionButton fabGroupSelect;

    @BindView(R.id.fabGroupOptions)
    FloatingActionButton fabGroupOptions;

    @BindView(R.id.fabSettings)
    FloatingActionButton fabSettings;

    @BindView(R.id.fabAbout)
    FloatingActionButton fabAbout;

    @BindView(R.id.fabPrefs)
    FloatingActionButton fabPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        initDrawerLayout();
        initViewPager();
        initTabLayout();
        initSharedData();
    }

    private void init() {
        fabBase.setClosedOnTouchOutside(true);
        fabAddToDo.setOnClickListener(this);
        fabGroupSelect.setOnClickListener(this);
        fabGroupOptions.setOnClickListener(this);
        fabSettings.setOnClickListener(this);
        fabAbout.setOnClickListener(this);
        fabPrefs.setOnClickListener(this);
    }

    private void initSharedData() {
        if (SharedData.INSTANCE.getCurrentGroup() == 0) {
            try {
                Database database = Database.getInstance(this);
                SharedData.INSTANCE.setCurrentGroup(database.readControlLongValue(KEY_CURRENT_GROUP, 1));
                database.close();
            } catch (Exception e) {
                e.printStackTrace();
                displayToast(R.string.databaseError);
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == fabAddToDo){
            addToDo();
        }
        else if (view == fabGroupSelect){
            selectToDoGroup();
        }
        else if (view == fabGroupOptions){
            startMyActivity(GroupOptionsActivity.class);
        }
        else if (view == fabSettings){
            startMyActivity(SettingsActivity.class);
        }
        else if (view == fabAbout){
            startMyActivity(AboutActivity.class);
        }
        else if (view == fabPrefs){
            startMyActivity(PrefsActivity.class);
        }

        fabBase.close(true);
    }

    private void addToDo() {
        Intent intent = new Intent(this, ToDoActivity.class);
        intent.putExtra(INTENT_KEY_TODO_ID, (long) -1);
        intent.putExtra(INTENT_KEY_TODO_STATUS, currentSection);
        startActivity(intent);
    }

    private void startMyActivity(Class myClass) {
        startActivity(new Intent(this, myClass));
    }

    private void selectToDoGroup() {
        Fragment currentFrag = mViewPagerAdapter.getRegisteredFragment(currentSection);
        GroupsDialogArgs groupsDialogArgs = new GroupsDialogArgs(
                currentFrag,
                REQUEST_DIALOG_FRAG_GROUP_SELECT);

        GroupsDialog groupsDialog = GroupsDialog.newInstance(groupsDialogArgs);
        groupsDialog.show(getSupportFragmentManager(), "groupsDialog");
    }

    private void initDrawerLayout() {
        if (navigationView != null) {
            navigationView.setItemIconTintList(null);
            navigationView.setNavigationItemSelectedListener(this);
        }
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name);
        mDrawerLayout.addDrawerListener(toggle);
    }

    private void initViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        /**
         * Prevent caching fragments to allow
         * calling onResume every time the fragment
         * is displayed to update data
         */
        viewPager.setOffscreenPageLimit(0);
    }

    private void initTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    /**
     * ViewPager.OnPageChangeListener
     **/
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentSection = position;
        updateFragmentData(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateFragmentData(int position) {
        mViewPagerAdapter.getRegisteredFragment(position).onResume();
    }
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            switch (item.getItemId()) {


                case R.id.drawerItemNewToDo:
                    addToDo();
                    break;

                case R.id.drawerItemSelectGroup:
                    selectToDoGroup();
                    break;

                case R.id.drawerItemGroupOptions:
                    startMyActivity(GroupOptionsActivity.class);
                    break;

//                ********************

                case R.id.drawerItemNow:
                    if (currentSection != 0) viewPager.setCurrentItem(0);
                    break;

                case R.id.drawerItemLate:
                    if (currentSection != 1) viewPager.setCurrentItem(1);
                    break;

                case R.id.drawerItemDone:
                    if (currentSection != 2) viewPager.setCurrentItem(2);
                    break;

//                ********************

                case R.id.drawerItemSettings:
                    startMyActivity(PrefsActivity.class);
                    break;

                case R.id.drawerItemAbout:
                    startMyActivity(AboutActivity.class);
                    break;

                  case R.id.drawerItemMore:
                      startMyActivity(SettingsActivity.class);
                    break;
            }
            return true;
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Database database = Database.getInstance(this);
            database.writeControlLongValue(KEY_CURRENT_GROUP, SharedData.INSTANCE.getCurrentGroup());
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
