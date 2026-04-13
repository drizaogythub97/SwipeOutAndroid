package com.swipeout.ui.onboarding;

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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<UserPreferencesRepository> prefsProvider;

  private final Provider<MediaRepository> repoProvider;

  public OnboardingViewModel_Factory(Provider<UserPreferencesRepository> prefsProvider,
      Provider<MediaRepository> repoProvider) {
    this.prefsProvider = prefsProvider;
    this.repoProvider = repoProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(prefsProvider.get(), repoProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<UserPreferencesRepository> prefsProvider, Provider<MediaRepository> repoProvider) {
    return new OnboardingViewModel_Factory(prefsProvider, repoProvider);
  }

  public static OnboardingViewModel newInstance(UserPreferencesRepository prefs,
      MediaRepository repo) {
    return new OnboardingViewModel(prefs, repo);
  }
}
