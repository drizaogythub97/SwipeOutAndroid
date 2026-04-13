package com.swipeout.ui.home;

import com.swipeout.data.preferences.UserPreferencesRepository;
import com.swipeout.data.repository.MediaRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<MediaRepository> repoProvider;

  private final Provider<UserPreferencesRepository> userPrefsProvider;

  public HomeViewModel_Factory(Provider<MediaRepository> repoProvider,
      Provider<UserPreferencesRepository> userPrefsProvider) {
    this.repoProvider = repoProvider;
    this.userPrefsProvider = userPrefsProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repoProvider.get(), userPrefsProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MediaRepository> repoProvider,
      Provider<UserPreferencesRepository> userPrefsProvider) {
    return new HomeViewModel_Factory(repoProvider, userPrefsProvider);
  }

  public static HomeViewModel newInstance(MediaRepository repo,
      UserPreferencesRepository userPrefs) {
    return new HomeViewModel(repo, userPrefs);
  }
}
