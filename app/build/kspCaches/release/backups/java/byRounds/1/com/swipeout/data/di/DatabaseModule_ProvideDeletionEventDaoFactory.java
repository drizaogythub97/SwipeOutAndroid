package com.swipeout.data.di;

import com.swipeout.data.db.AppDatabase;
import com.swipeout.data.db.dao.DeletionEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDeletionEventDaoFactory implements Factory<DeletionEventDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideDeletionEventDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DeletionEventDao get() {
    return provideDeletionEventDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDeletionEventDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideDeletionEventDaoFactory(dbProvider);
  }

  public static DeletionEventDao provideDeletionEventDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDeletionEventDao(db));
  }
}
