/* Copyright (c) 2019 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.ntp;

import static org.chromium.ui.base.ViewUtils.dpToPx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.base.ThreadUtils;
import org.chromium.base.supplier.Supplier;
import org.chromium.base.task.AsyncTask;
import org.chromium.brave_news.mojom.Article;
import org.chromium.brave_news.mojom.BraveNewsController;
import org.chromium.brave_news.mojom.CardType;
import org.chromium.brave_news.mojom.DisplayAd;
import org.chromium.brave_news.mojom.FeedItem;
import org.chromium.brave_news.mojom.FeedItemMetadata;
import org.chromium.brave_news.mojom.FeedPage;
import org.chromium.brave_news.mojom.FeedPageItem;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.BraveAdsNativeHelper;
import org.chromium.chrome.browser.BraveFeatureList;
import org.chromium.chrome.browser.BraveRewardsHelper;
import org.chromium.chrome.browser.InternetConnection;
import org.chromium.chrome.browser.QRCodeShareDialogFragment;
import org.chromium.chrome.browser.app.BraveActivity;
import org.chromium.chrome.browser.brave_news.BraveNewsAdapterFeedCard;
import org.chromium.chrome.browser.brave_news.BraveNewsControllerFactory;
import org.chromium.chrome.browser.brave_news.BraveNewsUtils;
import org.chromium.chrome.browser.brave_news.models.FeedItemCard;
import org.chromium.chrome.browser.brave_news.models.FeedItemsCard;
import org.chromium.chrome.browser.brave_stats.BraveStatsUtil;
import org.chromium.chrome.browser.compositor.CompositorViewHolder;
import org.chromium.chrome.browser.custom_layout.VerticalViewPager;
import org.chromium.chrome.browser.explore_sites.ExploreSitesBridge;
import org.chromium.chrome.browser.flags.ChromeFeatureList;
import org.chromium.chrome.browser.lifecycle.ActivityLifecycleDispatcher;
import org.chromium.chrome.browser.local_database.DatabaseHelper;
import org.chromium.chrome.browser.local_database.TopSiteTable;
import org.chromium.chrome.browser.native_page.ContextMenuManager;
import org.chromium.chrome.browser.night_mode.GlobalNightModeStateProviderHolder;
import org.chromium.chrome.browser.ntp.NewTabPageLayout;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetAdapter;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetItem;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetManager;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetStackActivity;
import org.chromium.chrome.browser.ntp_background_images.NTPBackgroundImagesBridge;
import org.chromium.chrome.browser.ntp_background_images.model.BackgroundImage;
import org.chromium.chrome.browser.ntp_background_images.model.NTPImage;
import org.chromium.chrome.browser.ntp_background_images.model.SponsoredTab;
import org.chromium.chrome.browser.ntp_background_images.model.TopSite;
import org.chromium.chrome.browser.ntp_background_images.model.Wallpaper;
import org.chromium.chrome.browser.ntp_background_images.util.FetchWallpaperWorkerTask;
import org.chromium.chrome.browser.ntp_background_images.util.NTPUtil;
import org.chromium.chrome.browser.ntp_background_images.util.NewTabPageListener;
import org.chromium.chrome.browser.ntp_background_images.util.SponsoredImageUtil;
import org.chromium.chrome.browser.offlinepages.DownloadUiActionFlags;
import org.chromium.chrome.browser.offlinepages.OfflinePageBridge;
import org.chromium.chrome.browser.offlinepages.RequestCoordinatorBridge;
import org.chromium.chrome.browser.onboarding.OnboardingPrefManager;
import org.chromium.chrome.browser.preferences.BravePref;
import org.chromium.chrome.browser.preferences.BravePrefServiceBridge;
import org.chromium.chrome.browser.preferences.BravePreferenceKeys;
import org.chromium.chrome.browser.preferences.SharedPreferencesManager;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.settings.BraveNewsPreferences;
import org.chromium.chrome.browser.settings.SettingsLauncherImpl;
import org.chromium.chrome.browser.suggestions.tile.TileGroup;
import org.chromium.chrome.browser.sync.settings.BraveManageSyncSettings;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.chrome.browser.tab.TabAttributes;
import org.chromium.chrome.browser.tab.TabImpl;
import org.chromium.chrome.browser.util.ConfigurationUtils;
import org.chromium.chrome.browser.util.PackageUtils;
import org.chromium.chrome.browser.util.TabUtils;
import org.chromium.components.browser_ui.settings.SettingsLauncher;
import org.chromium.components.browser_ui.widget.displaystyle.UiConfig;
import org.chromium.components.embedder_support.util.UrlUtilities;
import org.chromium.components.user_prefs.UserPrefs;
import org.chromium.mojo.bindings.ConnectionErrorHandler;
import org.chromium.mojo.system.MojoException;
import org.chromium.ui.base.WindowAndroid;
import org.chromium.ui.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BraveNewTabPageLayout extends NewTabPageLayout implements ConnectionErrorHandler {
    private static final String TAG = "BraveNewTabPageView";
    private static final String BRAVE_REF_URL = "https://brave.com/r/";
    private static final int ITEMS_PER_PAGE = 18;
    private static final int MINIMUM_VISIBLE_HEIGHT_THRESHOLD = 50;

    private View mBraveStatsViewFallBackLayout;

    private ImageView mBgImageView;
    private Profile mProfile;
    private BraveNewTabPageLayout mNtpContent;
    private LinearLayout mParentLayout;

    private SponsoredTab sponsoredTab;

    private BitmapDrawable imageDrawable;

    private FetchWallpaperWorkerTask mWorkerTask;
    private boolean isFromBottomSheet;
    private NTPBackgroundImagesBridge mNTPBackgroundImagesBridge;
    private ViewGroup mainLayout;
    private DatabaseHelper mDatabaseHelper;

    private ViewGroup mSiteSectionView;
    private TileGroup mTileGroup;
    private LottieAnimationView mBadgeAnimationView;
    private VerticalViewPager ntpWidgetViewPager;
    private NTPWidgetAdapter ntpWidgetAdapter;

    private Tab mTab;
    private Activity mActivity;
    private LinearLayout indicatorLayout;
    private LinearLayout superReferralSitesLayout;
    private LinearLayout ntpWidgetLayout;
    private LinearLayout bianceDisconnectLayout;
    private TextView mTopsiteErrorMessage;

    private List<NTPWidgetItem> widgetList = new ArrayList<NTPWidgetItem>();
    public static final int NTP_WIDGET_STACK_CODE = 3333;

    // Brave news
    private BraveNewsAdapterFeedCard mAdapterFeedCard;
    private FrameLayout mOptinButton;
    private TextView mOptinText;
    private LinearLayout mOptinLayout;
    private TextView mOptinLearnMore;
    private ImageView mOptinClose;
    private CopyOnWriteArrayList<FeedItemsCard> mNewsItemsFeedCard =
            new CopyOnWriteArrayList<FeedItemsCard>();
    private LinearLayout mContainer;
    private RecyclerView mRecyclerView;
    private TextView mLoading;
    private View mLoadingView;
    private View mFeedSpinner;
    private ScrollView mParentScrollView;
    private ViewGroup imageCreditLayout;
    private ViewGroup mSettingsBar;
    private ViewGroup mNewContentButton;
    private boolean isScrolled;
    private NTPImage mNtpImageGlobal;
    private boolean mSettingsBarIsClickable;
    private BraveNewsController mBraveNewsController;

    private CompositorViewHolder mCompositorView;

    private long mStartCardViewTime;
    private long mEndCardViewTime;
    private String mCreativeInstanceId;
    private String mUuid;
    //@TODO alex make an enum
    private String mCardType;
    private int mItemPosition;
    private FeedItemsCard mVisibleCard;
    private boolean mIsNewsOn;
    private boolean mIsShowOptin;
    private boolean mIsShowNewsOn;
    private int mmViewedNewsCardsCount;

    private boolean mIsFeedLoaded;
    private CopyOnWriteArrayList<FeedItemsCard> mExistingNewsFeedObject;
    private int mPrevScrollPosition;
    private static int mFirstVisibleCard;
    private String mFeedHash;
    private SharedPreferencesManager.Observer mPreferenceObserver;

    public BraveNewTabPageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProfile = Profile.getLastUsedRegularProfile();
        mNTPBackgroundImagesBridge = NTPBackgroundImagesBridge.getInstance(mProfile);
        mNTPBackgroundImagesBridge.setNewTabPageListener(newTabPageListener);
        mDatabaseHelper = DatabaseHelper.getInstance();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ntpWidgetLayout = findViewById(R.id.ntp_widget_layout);
        indicatorLayout = findViewById(R.id.indicator_layout);
        ntpWidgetViewPager = findViewById(R.id.ntp_widget_view_pager);
        ntpWidgetAdapter = new NTPWidgetAdapter();
        ntpWidgetAdapter.setNTPWidgetListener(ntpWidgetListener);
        ntpWidgetViewPager.setAdapter(ntpWidgetAdapter);

        ntpWidgetViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                updateAndShowIndicators(position);
                NTPWidgetManager.getInstance().setNTPWidgetOrder(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        showWidgetBasedOnOrder();
        NTPUtil.showBREBottomBanner(this);

        if (ChromeFeatureList.isEnabled(BraveFeatureList.BRAVE_NEWS)) {
            SharedPreferences sharedPreferences = ContextUtils.getAppSharedPreferences();

            mIsNewsOn = BravePrefServiceBridge.getInstance().getNewsOptIn();
            mIsShowOptin =
                    sharedPreferences.getBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, false);
            mIsShowNewsOn = BravePrefServiceBridge.getInstance().getShowNews();

            mFeedHash = "";

            InitBraveNewsController();

            mIsFeedLoaded = BraveActivity.getBraveActivity().isLoadedFeed();
            mExistingNewsFeedObject = BraveActivity.getBraveActivity().getNewsItemsFeedCards();
            mPrevScrollPosition = 2;

            if (mIsNewsOn && mIsShowNewsOn && mIsFeedLoaded && mExistingNewsFeedObject != null) {
                mNewsItemsFeedCard = mExistingNewsFeedObject;
            }

            if (BraveActivity.getBraveActivity() != null && mIsNewsOn) {
                Tab tab = BraveActivity.getBraveActivity().getActivityTab();
                if (tab != null && tab.getUrl().getSpec() != null
                        && UrlUtilities.isNTPUrl(tab.getUrl().getSpec())) {
                    BraveActivity.getBraveActivity().inflateNewsSettingsBar();
                } else {
                    //  remove settings bar
                    BraveActivity.getBraveActivity().removeSettingsBar();
                }
            }
        }
    }

    private void showFallBackNTPLayout() {
        if (mBraveStatsViewFallBackLayout != null
                && mBraveStatsViewFallBackLayout.getParent() != null) {
            ((ViewGroup) mBraveStatsViewFallBackLayout.getParent())
                    .removeView(mBraveStatsViewFallBackLayout);
        }
        LayoutInflater inflater =
                (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBraveStatsViewFallBackLayout = inflater.inflate(R.layout.brave_stats_layout, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutParams.setMargins(0, dpToPx(mActivity, 16), 0, dpToPx(mActivity, 16));
        mBraveStatsViewFallBackLayout.setLayoutParams(layoutParams);
        mBraveStatsViewFallBackLayout.requestLayout();

        mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_title_layout)
                .setVisibility(View.GONE);
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_text_ads))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_data_saved_text))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_text_time))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_text_time_count))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(
                 R.id.brave_stats_text_time_count_text))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        mBraveStatsViewFallBackLayout.setBackgroundColor(
                mActivity.getResources().getColor(android.R.color.transparent));
        mBraveStatsViewFallBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("SourceLockedOrientationActivity")
            public void onClick(View v) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                checkForBraveStats();
            }
        });
        BraveStatsUtil.updateBraveStatsLayout(mBraveStatsViewFallBackLayout);
        mainLayout.addView(mBraveStatsViewFallBackLayout, 0);

        int insertionPoint = mainLayout.indexOfChild(findViewById(R.id.ntp_middle_spacer)) + 1;
        if (mSiteSectionView.getParent() != null) {
            ((ViewGroup) mSiteSectionView.getParent()).removeView(mSiteSectionView);
        }
        mainLayout.addView(mSiteSectionView, insertionPoint);
    }

    protected void updateTileGridPlaceholderVisibility() {
        // This function is kept empty to avoid placeholder implementation
    }

    private List<NTPWidgetItem> setWidgetList() {
        NTPWidgetManager ntpWidgetManager = NTPWidgetManager.getInstance();
        LayoutInflater inflater =
                (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Map<Integer, NTPWidgetItem> ntpWidgetMap = new TreeMap<>();
        if (mSiteSectionView != null && mSiteSectionView.getParent() != null) {
            ((ViewGroup) mSiteSectionView.getParent()).removeView(mSiteSectionView);
        }

        for (String widget : ntpWidgetManager.getUsedWidgets()) {
            NTPWidgetItem ntpWidgetItem = NTPWidgetManager.mWidgetsMap.get(widget);
            if (widget.equals(NTPWidgetManager.PREF_PRIVATE_STATS)) {
                View mBraveStatsView = inflater.inflate(R.layout.brave_stats_layout, null);
                mBraveStatsView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @SuppressLint("SourceLockedOrientationActivity")
                    public void onClick(View v) {
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        checkForBraveStats();
                    }
                });
                ntpWidgetItem.setWidgetView(mBraveStatsView);
                ntpWidgetMap.put(ntpWidgetManager.getPrivateStatsWidget(), ntpWidgetItem);
            } else if (widget.equals(NTPWidgetManager.PREF_FAVORITES)) {
                View mTopSitesLayout = inflater.inflate(R.layout.top_sites_layout, null);
                FrameLayout mTopSitesGridLayout =
                        mTopSitesLayout.findViewById(R.id.top_sites_grid_layout);
                mTopsiteErrorMessage =
                        mTopSitesLayout.findViewById(R.id.widget_error_title);

                if (shouldShowSuperReferral() && superReferralSitesLayout != null) {
                    if (superReferralSitesLayout.getParent() != null) {
                        ((ViewGroup) superReferralSitesLayout.getParent())
                                .removeView(superReferralSitesLayout);
                    }
                    mTopSitesGridLayout.addView(superReferralSitesLayout);
                    ntpWidgetItem.setWidgetView(mTopSitesLayout);
                    ntpWidgetMap.put(ntpWidgetManager.getFavoritesWidget(), ntpWidgetItem);
                } else if (!mNTPBackgroundImagesBridge.isSuperReferral()
                        || !NTPBackgroundImagesBridge.enableSponsoredImages()
                        || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    boolean showPlaceholder = mTileGroup != null && mTileGroup.hasReceivedData()
                            && mTileGroup.isEmpty();
                    if (mSiteSectionView != null && !showPlaceholder) {
                        mTopsiteErrorMessage.setVisibility(View.GONE);
                        if (mSiteSectionView.getLayoutParams()
                                        instanceof ViewGroup.MarginLayoutParams) {
                            mSiteSectionView.setPadding(0, dpToPx(mActivity, 8), 0, 0);
                            mSiteSectionView.requestLayout();
                        }
                        mTopSitesGridLayout.addView(mSiteSectionView);
                    } else {
                        mTopsiteErrorMessage.setVisibility(View.VISIBLE);
                    }
                    ntpWidgetItem.setWidgetView(mTopSitesLayout);
                    ntpWidgetMap.put(ntpWidgetManager.getFavoritesWidget(), ntpWidgetItem);
                }
            }
        }

        return new ArrayList<NTPWidgetItem>(ntpWidgetMap.values());
    }

    private boolean shouldShowSuperReferral() {
        return mNTPBackgroundImagesBridge.isSuperReferral()
                && NTPBackgroundImagesBridge.enableSponsoredImages()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void showWidgetBasedOnOrder() {
        if (ntpWidgetViewPager != null) {
            int selectedOrder = NTPWidgetManager.getInstance().getNTPWidgetOrder();
            ntpWidgetViewPager.setCurrentItem(selectedOrder, true);
            updateAndShowIndicators(selectedOrder);
        }
    }

    private void showWidgets() {
        List<NTPWidgetItem> tempList = setWidgetList();
        if (tempList.size() > 0) {
            ntpWidgetLayout.setVisibility(View.VISIBLE);
            if (mBraveStatsViewFallBackLayout != null
                    && mBraveStatsViewFallBackLayout.getParent() != null) {
                ((ViewGroup) mBraveStatsViewFallBackLayout.getParent())
                        .removeView(mBraveStatsViewFallBackLayout);
            }
        } else {
            ntpWidgetLayout.setVisibility(View.GONE);
            if (!UserPrefs.get(Profile.getLastUsedRegularProfile())
                            .getBoolean(BravePref.NEW_TAB_PAGE_SHOW_BACKGROUND_IMAGE)) {
                showFallBackNTPLayout();
            }
        }

        if (ntpWidgetAdapter != null) {
            ntpWidgetAdapter.setWidgetList(tempList);
            ntpWidgetAdapter.notifyDataSetChanged();
            showWidgetBasedOnOrder();
        }
    }

    private void checkForBraveStats() {
        if (OnboardingPrefManager.getInstance().isBraveStatsEnabled()) {
            BraveStatsUtil.showBraveStats();
        } else {
            ((BraveActivity)mActivity).showOnboardingV2(true);
        }
    }

    @SuppressLint("VisibleForTests")
    protected void insertSiteSectionView() {
        mainLayout = findViewById(R.id.ntp_main_layout);

        mSiteSectionView = NewTabPageLayout.inflateSiteSection(mainLayout);
        ViewGroup.LayoutParams layoutParams = mSiteSectionView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        // If the explore sites section exists as its own section, then space it more closely.
        int variation = ExploreSitesBridge.getVariation();
        if (ExploreSitesBridge.isEnabled(variation)) {
            ((MarginLayoutParams) layoutParams).bottomMargin =
                getResources().getDimensionPixelOffset(
                    R.dimen.tile_grid_layout_vertical_spacing);
        }
        mSiteSectionView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sponsoredTab == null) {
            initilizeSponsoredTab();
        }
        checkAndShowNTPImage(false);
        mNTPBackgroundImagesBridge.addObserver(mNTPBackgroundImageServiceObserver);
        if (PackageUtils.isFirstInstall(mActivity)
                && !OnboardingPrefManager.getInstance().isNewOnboardingShown()
                && OnboardingPrefManager.getInstance().isP3aOnboardingShown()) {
            ((BraveActivity)mActivity).showOnboardingV2(false);
        }
        if (OnboardingPrefManager.getInstance().isFromNotification() ) {
            ((BraveActivity)mActivity).showOnboardingV2(false);
            OnboardingPrefManager.getInstance().setFromNotification(false);
        }
        if (mBadgeAnimationView != null
                && !OnboardingPrefManager.getInstance().shouldShowBadgeAnimation()) {
            mBadgeAnimationView.setVisibility(View.INVISIBLE);
        }
        showWidgets();

        if (ChromeFeatureList.isEnabled(BraveFeatureList.BRAVE_NEWS)) {
            if (mSettingsBar != null) {
                mSettingsBar.setVisibility(View.VISIBLE);
                mSettingsBar.setAlpha(0f);
            }
            initNews();
            if (BraveActivity.getBraveActivity() != null && mIsNewsOn) {
                Tab tab = BraveActivity.getBraveActivity().getActivityTab();
                if (tab != null && tab.getUrl().getSpec() != null
                        && UrlUtilities.isNTPUrl(tab.getUrl().getSpec())) {
                    BraveActivity.getBraveActivity().inflateNewsSettingsBar();
                } else {
                    BraveActivity.getBraveActivity().removeSettingsBar();
                }
            }
            mPreferenceObserver = (key) -> {
                if (TextUtils.equals(key, BravePreferenceKeys.BRAVE_NEWS_CHANGE_SOURCE)
                        || TextUtils.equals(key, BravePreferenceKeys.BRAVE_NEWS_PREF_SHOW_NEWS)) {
                    if (mNewContentButton != null) {
                        mNewContentButton.setVisibility(View.VISIBLE);
                    }
                }
            };
            SharedPreferencesManager.getInstance().addObserver(mPreferenceObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWorkerTask != null && mWorkerTask.getStatus() == AsyncTask.Status.RUNNING) {
            mWorkerTask.cancel(true);
            mWorkerTask = null;
        }

        if (!isFromBottomSheet) {
            setBackgroundResource(0);
            if (imageDrawable != null && imageDrawable.getBitmap() != null && !imageDrawable.getBitmap().isRecycled()) {
                imageDrawable.getBitmap().recycle();
            }
        }
        mNTPBackgroundImagesBridge.removeObserver(mNTPBackgroundImageServiceObserver);

        if (ChromeFeatureList.isEnabled(BraveFeatureList.BRAVE_NEWS)) {
            if (mNewsItemsFeedCard != null && mNewsItemsFeedCard.size() > 0) {
                BraveActivity.getBraveActivity().setNewsItemsFeedCards(mNewsItemsFeedCard);
            }

            if (mSettingsBar != null && mCompositorView != null) {
                if (mSettingsBar.getId() == R.id.news_settings_bar) {
                    mSettingsBar.setVisibility(View.GONE);
                    mSettingsBar.setAlpha(0f);
                }
            }

            if (mBraveNewsController != null) {
                mBraveNewsController.close();
            }

            // removes preference observer
            SharedPreferencesManager.getInstance().removeObserver(mPreferenceObserver);
            mPreferenceObserver = null;
        }

        super.onDetachedFromWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (sponsoredTab != null && NTPUtil.shouldEnableNTPFeature()) {
            if (mBgImageView != null && !ChromeFeatureList.isEnabled(BraveFeatureList.BRAVE_NEWS)) {
                // We need to redraw image to fit parent properly
                mBgImageView.setImageResource(android.R.color.transparent);
            }
            NTPImage ntpImage = sponsoredTab.getTabNTPImage(false);
            if (ntpImage == null) {
                sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
            } else if (ntpImage instanceof Wallpaper) {
                Wallpaper mWallpaper = (Wallpaper) ntpImage;
                if (mWallpaper == null) {
                    sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
                }
            }
            checkForNonDisruptiveBanner(ntpImage);
            super.onConfigurationChanged(newConfig);
            showNTPImage(ntpImage);
        } else {
            super.onConfigurationChanged(newConfig);
        }
    }

    // corrects position of image credit and for the loading spinner. Used when News is active
    private void correctPosition(boolean toTop) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        int pxHeight = dpToPx(mActivity, dpHeight);

        boolean isTablet = ConfigurationUtils.isTablet(mActivity);
        boolean isLandscape = ConfigurationUtils.isLandscape(mActivity);

        if (imageCreditLayout == null || mFeedSpinner == null) {
            imageCreditLayout = findViewById(R.id.image_credit_layout);
            mFeedSpinner = findViewById(R.id.feed_spinner);
        }

        if (imageCreditLayout != null) {
            LinearLayout.LayoutParams linearLayoutParams =
                    (LinearLayout.LayoutParams) imageCreditLayout.getLayoutParams();

            int imageCreditCorrection =
                    NTPUtil.correctImageCreditLayoutTopPosition(mNtpImageGlobal) - 140;
            if (toTop) {
                imageCreditCorrection = 0;
            }
            linearLayoutParams.setMargins(0, imageCreditCorrection, 0, 0);
            imageCreditLayout.setLayoutParams(linearLayoutParams);
            int imageCreditLayoutBottom = imageCreditLayout.getBottom();
        }

        if (mFeedSpinner != null) {
            FrameLayout.LayoutParams feedSpinnerParams =
                    (FrameLayout.LayoutParams) mFeedSpinner.getLayoutParams();
            FrameLayout bottomToolbar = (FrameLayout) findViewById(R.id.bottom_toolbar);
            feedSpinnerParams.topMargin = (int) (dpHeight - 30);
            mFeedSpinner.setLayoutParams(feedSpinnerParams);
        }
    }

    private void getFeed() {
        ExecutorService executors = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mBraveNewsController.getFeed((feed) -> {
                    if (feed == null) {
                        processFeed();
                        return;
                    }
                    try {
                        mFeedHash = feed.hash;
                        SharedPreferencesManager.getInstance().writeString(
                                BravePreferenceKeys.BRAVE_NEWS_FEED_HASH, feed.hash);
                        if (feed.featuredItem != null) {
                            // process Featured item
                            FeedItem featuredItem = feed.featuredItem;
                            FeedItemsCard featuredItemsCard = new FeedItemsCard();

                            FeedItemMetadata featuredItemMetaData = new FeedItemMetadata();
                            Article featuredArticle = featuredItem.getArticle();
                            FeedItemMetadata featuredArticleData = featuredArticle.data;

                            FeedItemCard featuredItemCard = new FeedItemCard();
                            List<FeedItemCard> featuredCardItems = new ArrayList<>();

                            featuredItemsCard.setCardType(CardType.HEADLINE);
                            featuredItemsCard.setUuid(UUID.randomUUID().toString());

                            featuredItemCard.setFeedItem(featuredItem);
                            featuredCardItems.add(featuredItemCard);

                            featuredItemsCard.setFeedItems(featuredCardItems);
                            mNewsItemsFeedCard.add(featuredItemsCard);
                        }

                        // adds empty card to trigger Display ad call for the second card, when the
                        // user starts scrolling
                        FeedItemsCard displayAdCard = new FeedItemsCard();
                        DisplayAd displayAd = new DisplayAd();
                        displayAdCard.setCardType(CardType.DISPLAY_AD);
                        displayAdCard.setDisplayAd(displayAd);
                        displayAdCard.setUuid(UUID.randomUUID().toString());
                        mNewsItemsFeedCard.add(displayAdCard);

                        // start page loop
                        int noPages = 0;
                        int itemIndex = 0;
                        int totalPages = feed.pages.length;
                        for (FeedPage page : feed.pages) {
                            for (FeedPageItem cardData : page.items) {
                                // if for any reason we get an empty object, unless it's a
                                // DISPLAY_AD we skip it
                                if (cardData.cardType != CardType.DISPLAY_AD) {
                                    if (cardData.items.length == 0) {
                                        continue;
                                    }
                                }
                                FeedItemsCard feedItemsCard = new FeedItemsCard();

                                feedItemsCard.setCardType(cardData.cardType);
                                feedItemsCard.setUuid(UUID.randomUUID().toString());
                                List<FeedItemCard> cardItems = new ArrayList<>();
                                for (FeedItem item : cardData.items) {
                                    FeedItemMetadata itemMetaData = new FeedItemMetadata();
                                    FeedItemCard feedItemCard = new FeedItemCard();
                                    feedItemCard.setFeedItem(item);

                                    cardItems.add(feedItemCard);

                                    feedItemsCard.setFeedItems(cardItems);
                                }
                                mNewsItemsFeedCard.add(feedItemsCard);
                            }
                        } // end page loop
                        processFeed();
                        BraveActivity.getBraveActivity().setNewsItemsFeedCards(mNewsItemsFeedCard);
                        BraveActivity.getBraveActivity().setLoadedFeed(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        };
        executors.submit(runnable);
    }

    private void refreshFeed() {
        mNewsItemsFeedCard.clear();
        mAdapterFeedCard =
                new BraveNewsAdapterFeedCard(mActivity, mNewsItemsFeedCard, mBraveNewsController);
        mRecyclerView.setAdapter(mAdapterFeedCard);

        if (!mIsShowNewsOn) {
            correctPosition(false);
            mRecyclerView.setVisibility(View.GONE);
            return;
        }
        getFeed();
        mParentScrollView.fullScroll(ScrollView.FOCUS_UP);
        mRecyclerView.scrollToPosition(0);
    }

    private void initNews() {
        mSettingsBarIsClickable = false;
        mRecyclerView = findViewById(R.id.newsRecycler);
        mContainer = (LinearLayout) findViewById(R.id.ntp_main_layout);
        mOptinButton = findViewById(R.id.optin_button);
        mOptinClose = findViewById(R.id.close_optin);
        mOptinLearnMore = findViewById(R.id.optin_learnmore);
        mOptinLayout = findViewById(R.id.optin_layout_id);
        mOptinText = findViewById(R.id.optin_button_text);
        mLoading = findViewById(R.id.loading);
        mLoadingView = findViewById(R.id.optin_loading_spinner);
        mFeedSpinner = findViewById(R.id.feed_spinner);
        mParentLayout = (LinearLayout) findViewById(R.id.parent_layout);
        mNtpContent = (BraveNewTabPageLayout) findViewById(R.id.ntp_content);

        SharedPreferencesManager.getInstance().writeBoolean(
                BravePreferenceKeys.BRAVE_NEWS_CHANGE_SOURCE, false);
        // init Brave news parameters
        mStartCardViewTime = 0;
        mEndCardViewTime = 0;
        mCreativeInstanceId = "";
        mUuid = "";
        //@TODO alex make an enum
        mCardType = "";
        mItemPosition = 0;
        mVisibleCard = null;

        mRecyclerView.setItemViewCacheSize(250);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mRecyclerView.setVisibility(View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mAdapterFeedCard =
                new BraveNewsAdapterFeedCard(mActivity, mNewsItemsFeedCard, mBraveNewsController);
        mRecyclerView.setAdapter(mAdapterFeedCard);

        mLoadingView.setVisibility(View.GONE);

        mParentScrollView = (ScrollView) mNtpContent.getParent();
        ViewGroup rootView = (ViewGroup) mParentScrollView.getParent();
        mCompositorView = (CompositorViewHolder) rootView.getParent();

        imageCreditLayout = findViewById(R.id.image_credit_layout);

        mOptinLayout.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = ContextUtils.getAppSharedPreferences();

        mIsNewsOn = BravePrefServiceBridge.getInstance().getNewsOptIn();
        mIsShowOptin = sharedPreferences.getBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, false);
        mIsShowNewsOn = BravePrefServiceBridge.getInstance().getShowNews();

        if (!mIsNewsOn || (!mIsNewsOn && mIsShowOptin)) {
            // correctPosition(false);
            mOptinLayout.setVisibility(View.VISIBLE);
        } else if (mIsShowNewsOn) {
            mOptinLayout.setVisibility(View.GONE);
            mParentLayout.removeView(mOptinLayout);
            mFeedSpinner.setVisibility(View.VISIBLE);

            boolean isFeedLoaded = BraveActivity.getBraveActivity().isLoadedFeed();
            CopyOnWriteArrayList<FeedItemsCard> existingNewsFeedObject =
                    BraveActivity.getBraveActivity().getNewsItemsFeedCards();
            int prevScrollPosition = SharedPreferencesManager.getInstance().readInt(
                    Integer.toString(BraveActivity.getBraveActivity().getActivityTab().getId()));

            mmViewedNewsCardsCount =
                    SharedPreferencesManager.getInstance().readInt("mViewedNewsCardsCount_"
                            + BraveActivity.getBraveActivity().getActivityTab().getId());
            if (prevScrollPosition == 0) {
                isFeedLoaded = false;
                existingNewsFeedObject = null;
                mmViewedNewsCardsCount = 0;
            }

            if (!isFeedLoaded) {
                getFeed();
                SharedPreferencesManager.getInstance().writeInt(
                        Integer.toString(BraveActivity.getBraveActivity().getActivityTab().getId()),
                        1);
                // Brave News interaction started
                if (mBraveNewsController != null) {
                    mBraveNewsController.onInteractionSessionStarted();
                }
            }

            else {
                if (mActivity == null) {
                    mActivity = BraveActivity.getBraveActivity();
                }
                setFeed();

                if (mRecyclerView != null) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            int scrollPosition = prevScrollPosition + 1;
                            if (prevScrollPosition <= 1) {
                                scrollPosition = 2;
                            }
                            final int scrollPositionFinal = scrollPosition;
                            if (mParentScrollView != null) {
                                mParentScrollView.fullScroll(ScrollView.FOCUS_UP);
                                mRecyclerView.scrollToPosition(scrollPositionFinal);
                                imageCreditLayout.scrollTo(0, 0);
                            }
                        }
                    });
                }
            }
        } else {
            // correctPosition(false);
            mOptinLayout.setVisibility(View.GONE);
        }

        ViewTreeObserver parentScrollViewObserver = mParentScrollView.getViewTreeObserver();
        mParentScrollView.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        try {
                            int scrollY = mParentScrollView.getScrollY();
                            if (mRecyclerView.getLayoutManager().findViewByPosition(0) != null) {
                                if (isScrolled) {
                                    correctPosition(false);
                                }
                            }
                            isScrolled = false;

                            float value = (float) scrollY / mParentScrollView.getMaxScrollAmount();
                            if (value >= 1) {
                                value = 1;
                            }
                            float alpha = (float) (1 - value);
                            if (alpha < 1f) {
                                imageCreditLayout.setAlpha(alpha);
                            }

                            if (mSettingsBar != null
                                    && mSettingsBar.getVisibility() == View.VISIBLE) {
                                if (value > 0.4 && mSettingsBar.getAlpha() <= 1f) {
                                    mSettingsBar.setAlpha((float) (value + 0.5));
                                } else if (value < 0.4 && mSettingsBar.getAlpha() > 0f) {
                                    mSettingsBar.setAlpha((float) (value - 0.2));
                                }
                                if (mSettingsBar.getAlpha() >= 1) {
                                    mSettingsBarIsClickable = true;
                                    LinearLayout.LayoutParams imageCreditLayoutParams =
                                            (LinearLayout.LayoutParams)
                                                    imageCreditLayout.getLayoutParams();
                                } else {
                                    mSettingsBarIsClickable = false;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("bn", "Exception parentScrollViewObserver e:" + e);
                        }
                    }
                });

        parentScrollViewObserver.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        try {
                            if (mSettingsBar != null) {
                                if (!isScrolled) {
                                    mSettingsBar.setAlpha(0f);
                                }

                                ImageView newsSettingsButton =
                                        (ImageView) mSettingsBar.findViewById(
                                                R.id.news_settings_button);
                                ViewTreeObserver.OnGlobalLayoutListener listener = this;

                                newsSettingsButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mSettingsBarIsClickable) {
                                            SettingsLauncher settingsLauncher =
                                                    new SettingsLauncherImpl();
                                            settingsLauncher.launchSettingsActivity(
                                                    getContext(), BraveNewsPreferences.class);
                                            mParentScrollView.getViewTreeObserver()
                                                    .removeOnGlobalLayoutListener(listener);
                                        }
                                    }
                                });
                            }

                            if (mNewContentButton != null) {
                                ProgressBar loadingSpinner =
                                        (ProgressBar) mNewContentButton.findViewById(
                                                R.id.new_content_loading_spinner);
                                TextView newContentButtonText =
                                        (TextView) mNewContentButton.findViewById(
                                                R.id.new_content_button_text);
                                mNewContentButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //@TODO alex check why visibility change doesn't work
                                        newContentButtonText.setVisibility(View.INVISIBLE);
                                        loadingSpinner.setVisibility(View.VISIBLE);

                                        mNewContentButton.invalidate();

                                        mNewContentButton.setClickable(false);
                                        SharedPreferencesManager.getInstance().writeBoolean(
                                                BravePreferenceKeys.BRAVE_NEWS_CHANGE_SOURCE,
                                                false);
                                        if (!mIsShowNewsOn) {
                                            mIsShowNewsOn = true;
                                        }
                                        refreshFeed();

                                        newContentButtonText.setVisibility(View.VISIBLE);
                                        loadingSpinner.setVisibility(View.GONE);
                                        mNewContentButton.setClickable(true);
                                        mNewContentButton.setVisibility(View.INVISIBLE);

                                        mNewContentButton.invalidate();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            Log.e("bn", "Exception  addOnGlobalLayoutListener e: " + e);
                        }
                    }
                });

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            mFirstVisibleCard = linearLayoutManager.findFirstVisibleItemPosition();

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                private int lastFirstVisibleItem;

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    int firstCompletelyVisibleItemPosition =
                            linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        mEndCardViewTime = System.currentTimeMillis();
                        long timeDiff = mEndCardViewTime - mStartCardViewTime;
                        // if viewed for more than 100 ms send the event
                        if (timeDiff > BraveNewsUtils.BRAVE_NEWS_VIEWD_CARD_TIME) {
                            if (firstCompletelyVisibleItemPosition > 0) {
                                SharedPreferencesManager.getInstance().writeInt(
                                        Integer.toString(BraveActivity.getBraveActivity()
                                                                 .getActivityTab()
                                                                 .getId()),
                                        firstCompletelyVisibleItemPosition);
                            }

                            if (mVisibleCard != null) {
                                if (!mVisibleCard.isViewStatSent()) {
                                    // send viewed cards events
                                    if (mCardType.equals("promo")) {
                                        if (!mUuid.equals("") && !mCreativeInstanceId.equals("")) {
                                            mBraveNewsController.onPromotedItemView(
                                                    mUuid, mCreativeInstanceId);
                                        }
                                    } else if (mCardType.equals("displayad")) {
                                        if (mUuid != null && mCreativeInstanceId != null) {
                                            if (!mUuid.equals("")
                                                    && !mCreativeInstanceId.equals("")) {
                                                mBraveNewsController.onDisplayAdView(
                                                        mUuid, mCreativeInstanceId);
                                            }
                                        }
                                    } else {
                                        mmViewedNewsCardsCount++;
                                        SharedPreferencesManager.getInstance().writeInt(
                                                "mViewedNewsCardsCount_"
                                                        + BraveActivity.getBraveActivity()
                                                                  .getActivityTab()
                                                                  .getId(),
                                                mmViewedNewsCardsCount);

                                        if (mmViewedNewsCardsCount > 0) {
                                            mBraveNewsController.onSessionCardViewsCountChanged(
                                                    (short) mmViewedNewsCardsCount);
                                        }
                                    }
                                    mVisibleCard.setViewStatSent(true);
                                }
                            }
                        }
                    }

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        mStartCardViewTime = System.currentTimeMillis();
                        int firstVisibleItemPosition =
                                linearLayoutManager.findFirstVisibleItemPosition();

                        int lastVisibleItemPosition =
                                linearLayoutManager.findLastVisibleItemPosition();

                        mFeedHash = SharedPreferencesManager.getInstance().readString(
                                BravePreferenceKeys.BRAVE_NEWS_FEED_HASH, "");

                        //@TODO alex optimize feed availability check
                        mBraveNewsController.isFeedUpdateAvailable(
                                mFeedHash, isNewsFeedAvailable -> {
                                    if (isNewsFeedAvailable) {
                                        if (mNewContentButton != null) {
                                            mNewContentButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });

                        for (int viewPosition = firstVisibleItemPosition;
                                viewPosition <= lastVisibleItemPosition; viewPosition++) {
                            View itemView = manager.findViewByPosition(viewPosition);
                            int visiblePercentage = (int) getVisibleHeightPercentage(itemView);
                            if (visiblePercentage >= MINIMUM_VISIBLE_HEIGHT_THRESHOLD) {
                                mVisibleCard = mNewsItemsFeedCard.get(viewPosition);
                                // get params for view PROMOTED_ARTICLE
                                if (mVisibleCard.getCardType() == CardType.PROMOTED_ARTICLE) {
                                    mItemPosition = viewPosition;
                                    mCreativeInstanceId =
                                            BraveNewsUtils.getPromotionIdItem(mVisibleCard);
                                    mUuid = mVisibleCard.getUuid();
                                    mCardType = "promo";
                                }
                                // get params for view DISPLAY_AD
                                if (mVisibleCard.getCardType() == CardType.DISPLAY_AD) {
                                    mItemPosition = viewPosition;
                                    DisplayAd currentDisplayAd = NTPUtil.getsCurrentDisplayAd();
                                    if (currentDisplayAd != null) {
                                        mCreativeInstanceId = currentDisplayAd != null
                                                ? currentDisplayAd.creativeInstanceId
                                                : "";
                                        mUuid = currentDisplayAd != null ? currentDisplayAd.uuid
                                                                         : "";
                                        mCardType = "displayad";
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    try {
                        int offset = recyclerView.computeVerticalScrollOffset();
                        mFirstVisibleCard = linearLayoutManager.findFirstVisibleItemPosition();

                        mParentScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mParentScrollView.scrollBy(0, offset + 20);
                                } catch (Exception e) {
                                    Log.e("bn", "Exception crashinvestigation e:" + e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("bn", "Exception onScrolled:" + e);
                    }
                }
            });
        }

        mOptinClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, false);
                sharedPreferencesEditor.apply();

                if (mOptinLayout != null) {
                    mOptinLayout.setVisibility(View.GONE);
                }
                correctPosition(false);
                mParentScrollView.scrollTo(0, 0);
                imageCreditLayout.setAlpha(1.0f);
            }
        });

        mOptinLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.brave.com"));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(browserIntent);
            }
        });

        mOptinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOptinButton.setClickable(false);
                mOptinText.setVisibility(View.INVISIBLE);
                mLoadingView.setVisibility(View.VISIBLE);

                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, false);
                sharedPreferencesEditor.apply();
                BravePrefServiceBridge.getInstance().setNewsOptIn(true);
                BravePrefServiceBridge.getInstance().setShowNews(true);
                if (BraveActivity.getBraveActivity() != null) {
                    BraveActivity.getBraveActivity().inflateNewsSettingsBar();
                    mSettingsBar =
                            (LinearLayout) mCompositorView.findViewById(R.id.news_settings_bar);
                }

                getFeed();
            }
        });
    }

    private double getVisibleHeightPercentage(View view) {
        Rect itemRect = new Rect();
        double viewVisibleHeightPercentage = 0;
        if (view != null) {
            view.getLocalVisibleRect(itemRect);

            // Find the height of the item.
            double visibleHeight = itemRect.height();
            double height = view.getMeasuredHeight();

            viewVisibleHeightPercentage = ((visibleHeight / height) * 100);
        }

        return viewVisibleHeightPercentage;
    }

    private void setFeed() {
        mFeedSpinner.setVisibility(View.GONE);
        mOptinLayout.setVisibility(View.GONE);

        mContainer.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        try {
            mParentScrollView.scrollTo(0, 0);
        } catch (Exception e) {
            Log.e("bn", "Exception processfeed e:" + e);
        }

        isScrolled = true;
    }

    private void processFeed() {
        mFeedSpinner.setVisibility(View.GONE);
        mOptinLayout.setVisibility(View.GONE);

        mContainer.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        mAdapterFeedCard.notifyItemRangeChanged(0, mAdapterFeedCard.getItemCount());

        try {
            mParentScrollView.scrollTo(0, 0);
        } catch (Exception e) {
            Log.e("bn", "Exception processfeed e:" + e);
        }

        isScrolled = true;
    }

    @Override
    public void initialize(NewTabPageManager manager, Activity activity,
            TileGroup.Delegate tileGroupDelegate, boolean searchProviderHasLogo,
            boolean searchProviderIsGoogle, ScrollDelegate scrollDelegate,
            ContextMenuManager contextMenuManager, UiConfig uiConfig, Supplier<Tab> tabProvider,
            ActivityLifecycleDispatcher lifecycleDispatcher, NewTabPageUma uma, boolean isIncognito,
            WindowAndroid windowAndroid) {
        super.initialize(manager, activity, tileGroupDelegate, searchProviderHasLogo,
                searchProviderIsGoogle, scrollDelegate, contextMenuManager, uiConfig, tabProvider,
                lifecycleDispatcher, uma, isIncognito, windowAndroid);

        assert (activity instanceof BraveActivity);
        mActivity = activity;
        ((BraveActivity) mActivity).dismissShieldsTooltip();
    }

    private void showNTPImage(NTPImage ntpImage) {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        NTPUtil.updateOrientedUI(mActivity, this, size, ntpImage);
        ImageView mSponsoredLogo = (ImageView) findViewById(R.id.sponsored_logo);
        FloatingActionButton mSuperReferralLogo = (FloatingActionButton) findViewById(R.id.super_referral_logo);
        TextView mCreditText = (TextView) findViewById(R.id.credit_text);
        mNtpImageGlobal = ntpImage;
        if (ntpImage instanceof Wallpaper
                && NTPUtil.isReferralEnabled()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setBackgroundImage(ntpImage);
            mSuperReferralLogo.setVisibility(View.VISIBLE);
            mCreditText.setVisibility(View.GONE);
            int floatingButtonIcon = R.drawable.ic_qr_code;
            mSuperReferralLogo.setImageResource(floatingButtonIcon);
            int floatingButtonIconColor =
                    GlobalNightModeStateProviderHolder.getInstance().isInNightMode()
                    ? android.R.color.white
                    : android.R.color.black;
            ImageViewCompat.setImageTintList(
                    mSuperReferralLogo, ColorStateList.valueOf(floatingButtonIconColor));
            mSuperReferralLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    QRCodeShareDialogFragment mQRCodeShareDialogFragment =
                            new QRCodeShareDialogFragment();
                    mQRCodeShareDialogFragment.setQRCodeText(
                            BRAVE_REF_URL + mNTPBackgroundImagesBridge.getSuperReferralCode());
                    mQRCodeShareDialogFragment.show(
                            ((BraveActivity) mActivity).getSupportFragmentManager(),
                            "QRCodeShareDialogFragment");
                }
            });
        } else if (UserPrefs.get(Profile.getLastUsedRegularProfile()).getBoolean(
                       BravePref.NEW_TAB_PAGE_SHOW_BACKGROUND_IMAGE)
                   && sponsoredTab != null
                   && NTPUtil.shouldEnableNTPFeature()) {
            setBackgroundImage(ntpImage);
            if (ntpImage instanceof BackgroundImage) {
                BackgroundImage backgroundImage = (BackgroundImage) ntpImage;
                mSponsoredLogo.setVisibility(View.GONE);
                mSuperReferralLogo.setVisibility(View.GONE);

                if (backgroundImage.getImageCredit() != null) {
                    String imageCreditStr = String.format(getResources().getString(R.string.photo_by, backgroundImage.getImageCredit().getName()));

                    SpannableStringBuilder spannableString = new SpannableStringBuilder(imageCreditStr);
                    spannableString.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        ((imageCreditStr.length() - 1)
                         - (backgroundImage.getImageCredit().getName().length() - 1)),
                        imageCreditStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    mCreditText.setText(spannableString);
                    mCreditText.setVisibility(View.VISIBLE);

                    mCreditText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (backgroundImage.getImageCredit() != null) {
                                TabUtils.openUrlInSameTab(
                                        backgroundImage.getImageCredit().getUrl());
                            }
                        }
                    });
                }
            }
        }
    }

    private void setBackgroundImage(NTPImage ntpImage) {
        mBgImageView = (ImageView) findViewById(R.id.bg_image_view);
        mBgImageView.setScaleType(ImageView.ScaleType.MATRIX);

        ViewTreeObserver observer = mBgImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mWorkerTask =
                        new FetchWallpaperWorkerTask(ntpImage, mBgImageView.getMeasuredWidth(),
                                mBgImageView.getMeasuredHeight(), wallpaperRetrievedCallback);
                mWorkerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBgImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void checkForNonDisruptiveBanner(NTPImage ntpImage) {
        int brOption = NTPUtil.checkForNonDisruptiveBanner(ntpImage, sponsoredTab);
        if (SponsoredImageUtil.BR_INVALID_OPTION != brOption && !NTPUtil.isReferralEnabled()
                && ((!BraveAdsNativeHelper.nativeIsBraveAdsEnabled(
                             Profile.getLastUsedRegularProfile())
                            && BraveRewardsHelper.shouldShowBraveRewardsOnboardingModal())
                        || BraveAdsNativeHelper.nativeIsBraveAdsEnabled(
                                Profile.getLastUsedRegularProfile()))) {
            NTPUtil.showNonDisruptiveBanner((BraveActivity) mActivity, this, brOption,
                                             sponsoredTab, newTabPageListener);
        }
    }

    private void checkAndShowNTPImage(boolean isReset) {
        NTPImage ntpImage = sponsoredTab.getTabNTPImage(isReset);
        if (ntpImage == null) {
            sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
        } else if (ntpImage instanceof Wallpaper) {
            Wallpaper mWallpaper = (Wallpaper) ntpImage;
            if (mWallpaper == null) {
                sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
            }
        }
        checkForNonDisruptiveBanner(ntpImage);
        showNTPImage(ntpImage);
    }

    private void initilizeSponsoredTab() {
        if (TabAttributes.from(getTab()).get(String.valueOf(getTabImpl().getId())) == null) {
            SponsoredTab mSponsoredTab = new SponsoredTab(mNTPBackgroundImagesBridge);
            TabAttributes.from(getTab()).set(String.valueOf(getTabImpl().getId()), mSponsoredTab);
        }
        sponsoredTab = TabAttributes.from(getTab()).get(String.valueOf((getTabImpl()).getId()));
        if (shouldShowSuperReferral()) mNTPBackgroundImagesBridge.getTopSites();
    }

    private NewTabPageListener newTabPageListener = new NewTabPageListener() {
        @Override
        public void updateInteractableFlag(boolean isBottomSheet) {
            isFromBottomSheet = isBottomSheet;
        }

        @Override
        public void updateNTPImage() {
            if (sponsoredTab == null) {
                initilizeSponsoredTab();
            }
            checkAndShowNTPImage(false);
            mParentScrollView = (ScrollView) mNtpContent.getParent();
            ViewGroup rootView = (ViewGroup) mParentScrollView.getParent();
            CompositorViewHolder compositorView = (CompositorViewHolder) rootView.getParent();
            final int childCount = compositorView.getChildCount();
        }

        @Override
        public void updateTopSites(List<TopSite> topSites) {
            new AsyncTask<List<TopSiteTable>>() {
                @Override
                protected List<TopSiteTable> doInBackground() {
                    for (TopSite topSite : topSites) {
                        mDatabaseHelper.insertTopSite(topSite);
                    }
                    return mDatabaseHelper.getAllTopSites();
                }

                @Override
                protected void onPostExecute(List<TopSiteTable> topSites) {
                    assert ThreadUtils.runningOnUiThread();
                    if (isCancelled()) return;

                    loadTopSites(topSites);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    };

    private NTPBackgroundImagesBridge.NTPBackgroundImageServiceObserver mNTPBackgroundImageServiceObserver = new NTPBackgroundImagesBridge.NTPBackgroundImageServiceObserver() {
        @Override
        public void onUpdated() {
            if (NTPUtil.isReferralEnabled()) {
                checkAndShowNTPImage(true);
                if (shouldShowSuperReferral()) {
                    mNTPBackgroundImagesBridge.getTopSites();
                }
            }
        } 
    };

    private FetchWallpaperWorkerTask.WallpaperRetrievedCallback wallpaperRetrievedCallback = new FetchWallpaperWorkerTask.WallpaperRetrievedCallback() {
        @Override
        public void bgWallpaperRetrieved(Bitmap bgWallpaper) {
            if (ChromeFeatureList.isEnabled(BraveFeatureList.BRAVE_NEWS)) {
                if (BraveActivity.getBraveActivity() != null) {
                    BitmapDrawable bgWallpaperDrawable = new BitmapDrawable(bgWallpaper);
                    BraveActivity.getBraveActivity().setBackground(bgWallpaperDrawable);
                }

                mParentScrollView = (ScrollView) mNtpContent.getParent();
                ViewGroup rootView = (ViewGroup) mParentScrollView.getParent();
                ViewGroup coordinator = (ViewGroup) rootView.getParent();

                CompositorViewHolder compositorView = (CompositorViewHolder) rootView.getParent();

                try {
                    if (BraveActivity.getBraveActivity() != null
                            && BraveActivity.getBraveActivity().getActivityTab() != null) {
                        if (UrlUtilities.isNTPUrl(BraveActivity.getBraveActivity()
                                                          .getActivityTab()
                                                          .getUrl()
                                                          .getSpec())) {
                            if (compositorView.getChildAt(2).getId() == R.id.news_settings_bar) {
                                mSettingsBar = (LinearLayout) compositorView.getChildAt(2);
                                mSettingsBar.setVisibility(View.VISIBLE);
                                mSettingsBar.setAlpha(0f);
                            }

                            if (compositorView.getChildAt(3).getId()
                                    == R.id.new_content_layout_id) {
                                mNewContentButton = (RelativeLayout) compositorView.getChildAt(3);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("bn", "crashinvestigation exception: " + e.getMessage());
                }

            } else {
                mBgImageView.setImageBitmap(bgWallpaper);
            }
        }

        @Override
        public void logoRetrieved(Wallpaper mWallpaper, Bitmap logoWallpaper) {
            if (!NTPUtil.isReferralEnabled()) {
                FloatingActionButton mSuperReferralLogo = (FloatingActionButton) findViewById(R.id.super_referral_logo);
                mSuperReferralLogo.setVisibility(View.GONE);

                ImageView sponsoredLogo = (ImageView) findViewById(R.id.sponsored_logo);
                sponsoredLogo.setVisibility(View.VISIBLE);
                sponsoredLogo.setImageBitmap(logoWallpaper);
                sponsoredLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mWallpaper.getLogoDestinationUrl() != null) {
                            TabUtils.openUrlInSameTab(mWallpaper.getLogoDestinationUrl());
                            mNTPBackgroundImagesBridge.wallpaperLogoClicked(mWallpaper);
                        }
                    }
                });
            }
        }
    };

    private void loadTopSites(List<TopSiteTable> topSites) {
        superReferralSitesLayout = new LinearLayout(mActivity);
        superReferralSitesLayout.setWeightSum(1f);
        superReferralSitesLayout.setOrientation(LinearLayout.HORIZONTAL);
        superReferralSitesLayout.setBackgroundColor(
                mActivity.getResources().getColor(R.color.topsite_bg_color));

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (TopSiteTable topSite : topSites) {
            final View view = inflater.inflate(R.layout.suggestions_tile_view, null);

            TextView tileViewTitleTv = view.findViewById(R.id.tile_view_title);
            tileViewTitleTv.setText(topSite.getName());
            tileViewTitleTv.setTextColor(getResources().getColor(android.R.color.black));

            ImageView iconIv = view.findViewById(R.id.tile_view_icon);
            if (NTPUtil.imageCache.get(topSite.getDestinationUrl()) == null) {
                NTPUtil.imageCache.put(topSite.getDestinationUrl(), new java.lang.ref.SoftReference(NTPUtil.getTopSiteBitmap(topSite.getImagePath())));
            }
            iconIv.setImageBitmap(NTPUtil.imageCache.get(topSite.getDestinationUrl()).get());
            iconIv.setBackgroundColor(mActivity.getResources().getColor(android.R.color.white));
            iconIv.setClickable(false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TabUtils.openUrlInSameTab(topSite.getDestinationUrl());
                }
            });

            view.setPadding(0, dpToPx(mActivity, 12), 0, 0);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 0.25f;
            layoutParams.gravity = Gravity.CENTER;
            view.setLayoutParams(layoutParams);
            view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add(R.string.contextmenu_open_in_new_tab).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            TabUtils.openUrlInNewTab(false, topSite.getDestinationUrl());
                            return true;
                        }
                    });
                    menu.add(R.string.contextmenu_open_in_incognito_tab).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            TabUtils.openUrlInNewTab(true, topSite.getDestinationUrl());
                            return true;
                        }
                    });
                    menu.add(R.string.contextmenu_save_link).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (getTab() != null) {
                                OfflinePageBridge.getForProfile(mProfile).scheduleDownload(getTab().getWebContents(),
                                        OfflinePageBridge.NTP_SUGGESTIONS_NAMESPACE, topSite.getDestinationUrl(), DownloadUiActionFlags.ALL);
                            } else {
                                RequestCoordinatorBridge.getForProfile(mProfile).savePageLater(
                                    topSite.getDestinationUrl(), OfflinePageBridge.NTP_SUGGESTIONS_NAMESPACE, true /* userRequested */);
                            }
                            return true;
                        }
                    });
                    menu.add(R.string.remove).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            NTPUtil.imageCache.remove(topSite.getDestinationUrl());
                            mDatabaseHelper.deleteTopSite(topSite.getDestinationUrl());
                            NTPUtil.addToRemovedTopSite(topSite.getDestinationUrl());
                            superReferralSitesLayout.removeView(view);
                            return true;
                        }
                    });
                }
            });
            superReferralSitesLayout.addView(view);
        }
        showWidgets();
    }

    public void setTab(Tab tab) {
        mTab = tab;
    }

    private Tab getTab() {
        assert mTab != null;
        return mTab;
    }

    private TabImpl getTabImpl() {
        return (TabImpl) getTab();
    }

    private void updateAndShowIndicators(int position) {
        indicatorLayout.removeAllViews();
        for (int i = 0; i < ntpWidgetAdapter.getCount(); i++) {
            TextView dotTextView = new TextView(mActivity);
            dotTextView.setText(Html.fromHtml("&#9679;"));
            dotTextView.setTextColor(getResources().getColor(android.R.color.white));
            dotTextView.setTextSize(8);
            if (position == i) {
                dotTextView.setAlpha(1.0f);
            } else {
                dotTextView.setAlpha(0.4f);
            }
            indicatorLayout.addView(dotTextView);
        }
    }

    // NTP related methods
    private NTPWidgetAdapter.NTPWidgetListener ntpWidgetListener =
            new NTPWidgetAdapter.NTPWidgetListener() {
                @Override
                public void onMenuEdit() {
                    openWidgetStack();
                }

                @Override
                public void onMenuRemove(int position) {
                    if (BraveActivity.getBraveActivity() != null 
                        && BraveActivity.getBraveActivity().getActivityTab() != null 
                        && !UserPrefs.get(Profile.getLastUsedRegularProfile())
                            .getBoolean(BravePref.NEW_TAB_PAGE_SHOW_BACKGROUND_IMAGE)
                        && NTPWidgetManager.getInstance().getUsedWidgets().size() <= 0) {
                        BraveActivity.getBraveActivity().getActivityTab().reloadIgnoringCache();
                    } else {
                        showWidgets();
                    }
                }
            };

    public void openWidgetStack() {
        final FragmentManager fm = ((BraveActivity) mActivity).getSupportFragmentManager();
        Fragment auxiliary = new Fragment() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                fm.beginTransaction().remove(this).commit();
                if (requestCode == NTP_WIDGET_STACK_CODE) {
                    showWidgets();
                }
            }
        };
        fm.beginTransaction().add(auxiliary, "FRAGMENT_TAG").commit();
        fm.executePendingTransactions();

        Intent ntpWidgetStackActivityIntent = new Intent(mActivity, NTPWidgetStackActivity.class);
        ntpWidgetStackActivityIntent.putExtra(NTPWidgetStackActivity.FROM_SETTINGS, false);
        auxiliary.startActivityForResult(ntpWidgetStackActivityIntent, NTP_WIDGET_STACK_CODE);
    }

    @Override
    public void onTileCountChanged() {
        if (mTopsiteErrorMessage == null) {
            return;
        }

        boolean showPlaceholder =
                mTileGroup != null && mTileGroup.hasReceivedData() && mTileGroup.isEmpty();
        if (!showPlaceholder) {
            mTopsiteErrorMessage.setVisibility(View.GONE);
        } else {
            mTopsiteErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnectionError(MojoException e) {
        if (mBraveNewsController != null) {
            mBraveNewsController.close();
        }
        mBraveNewsController = null;
        InitBraveNewsController();
    }

    private void InitBraveNewsController() {
        if (mBraveNewsController != null) {
            return;
        }

        mBraveNewsController =
                BraveNewsControllerFactory.getInstance().getBraveNewsController(this);
    }
}
