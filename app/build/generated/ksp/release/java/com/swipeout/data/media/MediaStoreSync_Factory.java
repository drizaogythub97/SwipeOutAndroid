package com.swipeout.data.media;

import android.content.Context;
import com.swipeout.data.db.dao.ImageDao;
import com.swipeout.data.db.dao.MonthlyMenuDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MediaStoreSync_Factory implements Factory<MediaStoreSync> {
  private final Provider<Context> contextProvider;

  private final Provider<ImageDao> imageDaoProvider;

  private final Provider<MonthlyMenuDao> monthlyMenuDaoProvider;

  public MediaStoreSync_Factory(Provider<Context> contextProvider,
      Provider<ImageDao> imageDaoProvider, Provider<MonthlyMenuDao> monthlyMenuDaoProvider) {
    this.contextProvider = contextProvider;
    this.imageDaoProvider = imageDaoProvider;
    this.monthlyMenuDaoProvider = monthlyMenuDaoProvider;
  }

  @Override
  public MediaStoreSync get() {
    return newInstance(contextProvider.get(), imageDaoProvider.get(), monthlyMenuDaoProvider.get());
  }

  public static MediaStoreSync_Factory create(Provider<Context> contextProvider,
      Provider<ImageDao> imageDaoProvider, Provider<MonthlyMenuDao> monthlyMenuDaoProvider) {
    return new MediaStoreSync_Factory(contextProvider, imageDaoProvider, monthlyMenuDaoProvider);
  }

  public static MediaStoreSync newInstance(Context context, ImageDao imageDao,
      MonthlyMenuDao monthlyMenuDao) {
    return new MediaStoreSync(context, imageDao, monthlyMenuDao);
  }
}
