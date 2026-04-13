package com.swipeout.data.di;

import com.swipeout.data.db.AppDatabase;
import com.swipeout.data.db.dao.MonthlyMenuDao;
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
public final class DatabaseModule_ProvideMonthlyMenuDaoFactory implements Factory<MonthlyMenuDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideMonthlyMenuDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MonthlyMenuDao get() {
    return provideMonthlyMenuDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMonthlyMenuDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideMonthlyMenuDaoFactory(dbProvider);
  }

  public static MonthlyMenuDao provideMonthlyMenuDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMonthlyMenuDao(db));
  }
}
