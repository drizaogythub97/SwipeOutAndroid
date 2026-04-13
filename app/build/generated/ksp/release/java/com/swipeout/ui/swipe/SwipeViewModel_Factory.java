package com.swipeout.ui.swipe;

import androidx.lifecycle.SavedStateHandle;
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
public final class SwipeViewModel_Factory implements Factory<SwipeViewModel> {
  private final Provider<MediaRepository> repoProvider;

  private final Provider<SavedStateHandle> savedStateProvider;

  public SwipeViewModel_Factory(Provider<MediaRepository> repoProvider,
      Provider<SavedStateHandle> savedStateProvider) {
    this.repoProvider = repoProvider;
    this.savedStateProvider = savedStateProvider;
  }

  @Override
  public SwipeViewModel get() {
    return newInstance(repoProvider.get(), savedStateProvider.get());
  }

  public static SwipeViewModel_Factory create(Provider<MediaRepository> repoProvider,
      Provider<SavedStateHandle> savedStateProvider) {
    return new SwipeViewModel_Factory(repoProvider, savedStateProvider);
  }

  public static SwipeViewModel newInstance(MediaRepository repo, SavedStateHandle savedState) {
    return new SwipeViewModel(repo, savedState);
  }
}
