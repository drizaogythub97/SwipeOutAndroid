package com.swipeout.data.repository;

import com.swipeout.data.db.dao.DeletionEventDao;
import com.swipeout.data.db.dao.ImageDao;
import com.swipeout.data.db.dao.MonthlyMenuDao;
import com.swipeout.data.media.MediaStoreSync;
import com.swipeout.data.preferences.UserPreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class MediaRepository_Factory implements Factory<MediaRepository> {
  private final Provider<ImageDao> imageDaoProvider;

  private final Provider<MonthlyMenuDao> monthlyMenuDaoProvider;

  private final Provider<DeletionEventDao> deletionEventDaoProvider;

  private final Provider<MediaStoreSync> mediaStoreSyncProvider;

  private final Provider<UserPreferencesRepository> userPrefsProvider;

  public MediaRepository_Factory(Provider<ImageDao> imageDaoProvider,
      Provider<MonthlyMenuDao> monthlyMenuDaoProvider,
      Provider<DeletionEventDao> deletionEventDaoProvider,
      Provider<MediaStoreSync> mediaStoreSyncProvider,
      Provider<UserPreferencesRepository> userPrefsProvider) {
    this.imageDaoProvider = imageDaoProvider;
    this.monthlyMenuDaoProvider = monthlyMenuDaoProvider;
    this.deletionEventDaoProvider = deletionEventDaoProvider;
    this.mediaStoreSyncProvider = mediaStoreSyncProvider;
    this.userPrefsProvider = userPrefsProvider;
  }

  @Override
  public MediaRepository get() {
    return newInstance(imageDaoProvider.get(), monthlyMenuDaoProvider.get(), deletionEventDaoProvider.get(), mediaStoreSyncProvider.get(), userPrefsProvider.get());
  }

  public static MediaRepository_Factory create(Provider<ImageDao> imageDaoProvider,
      Provider<MonthlyMenuDao> monthlyMenuDaoProvider,
      Provider<DeletionEventDao> deletionEventDaoProvider,
      Provider<MediaStoreSync> mediaStoreSyncProvider,
      Provider<UserPreferencesRepository> userPrefsProvider) {
    return new MediaRepository_Factory(imageDaoProvider, monthlyMenuDaoProvider, deletionEventDaoProvider, mediaStoreSyncProvider, userPrefsProvider);
  }

  public static MediaRepository newInstance(ImageDao imageDao, MonthlyMenuDao monthlyMenuDao,
      DeletionEventDao deletionEventDao, MediaStoreSync mediaStoreSync,
      UserPreferencesRepository userPrefs) {
    return new MediaRepository(imageDao, monthlyMenuDao, deletionEventDao, mediaStoreSync, userPrefs);
  }
}
