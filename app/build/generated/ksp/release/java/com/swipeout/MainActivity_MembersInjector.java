package com.swipeout;

import com.swipeout.data.preferences.UserPreferencesRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<UserPreferencesRepository> userPrefsProvider;

  public MainActivity_MembersInjector(Provider<UserPreferencesRepository> userPrefsProvider) {
    this.userPrefsProvider = userPrefsProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<UserPreferencesRepository> userPrefsProvider) {
    return new MainActivity_MembersInjector(userPrefsProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectUserPrefs(instance, userPrefsProvider.get());
  }

  @InjectedFieldSignature("com.swipeout.MainActivity.userPrefs")
  public static void injectUserPrefs(MainActivity instance, UserPreferencesRepository userPrefs) {
    instance.userPrefs = userPrefs;
  }
}
