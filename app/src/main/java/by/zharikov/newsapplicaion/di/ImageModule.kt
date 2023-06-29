package by.zharikov.newsapplicaion.di

import by.zharikov.newsapplicaion.domain.repository.ImageRepository
import by.zharikov.newsapplicaion.domain.usecase.imageusecases.DeleteImage
import by.zharikov.newsapplicaion.domain.usecase.imageusecases.GetImage
import by.zharikov.newsapplicaion.domain.usecase.imageusecases.ImagesUseCases
import by.zharikov.newsapplicaion.domain.usecase.imageusecases.SetImage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageUseCases(repository: ImageRepository): ImagesUseCases =
        ImagesUseCases(
            deleteImage = DeleteImage(repository),
            getImage = GetImage(repository),
            setImage = SetImage(repository)
        )
}