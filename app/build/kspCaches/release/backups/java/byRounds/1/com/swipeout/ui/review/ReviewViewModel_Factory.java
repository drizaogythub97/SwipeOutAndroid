package com.swipeout.ui.review;

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
public final class ReviewViewModel_Factory implements Factory<ReviewViewModel> {
  private final Provider<MediaRepository> repoProvider;

  private final Provider<SavedStateHandle> savedStateProvider;

  public ReviewViewModel_Factory(Provider<MediaRepository> repoProvider,
      Provider<SavedStateHandle> savedStateProvider) {
    this.repoProvider = repoProvider;
    this.savedStateProvider = savedStateProvider;
  }

  @Override
  public ReviewViewModel get() {
    return newInstance(repoProvider.get(), savedStateProvider.get());
  }

  public static ReviewViewModel_Factory create(Provider<MediaRepository> repoProvider,
      Provider<SavedStateHandle> savedStateProvider) {
    return new ReviewViewModel_Factory(repoProvider, savedStateProvider);
  }

  public static ReviewViewModel newInstance(MediaRepository repo, SavedStateHandle savedState) {
    return new ReviewViewModel(repo, savedState);
  }
}
