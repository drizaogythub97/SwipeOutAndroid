package com.swipeout.ui.settings;

import com.swipeout.data.db.dao.DeletionEventDao;
import com.swipeout.data.preferences.UserPreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferencesRepository> userPrefsProvider;

  private final Provider<DeletionEventDao> deletionEventDaoProvider;

  public SettingsViewModel_Factory(Provider<UserPreferencesRepository> userPrefsProvider,
      Provider<DeletionEventDao> deletionEventDaoProvider) {
    this.userPrefsProvider = userPrefsProvider;
    this.deletionEventDaoProvider = deletionEventDaoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userPrefsProvider.get(), deletionEventDaoProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<UserPreferencesRepository> userPrefsProvider,
      Provider<DeletionEventDao> deletionEventDaoProvider) {
    return new SettingsViewModel_Factory(userPrefsProvider, deletionEventDaoProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesRepository userPrefs,
      DeletionEventDao deletionEventDao) {
    return new SettingsViewModel(userPrefs, deletionEventDao);
  }
}
