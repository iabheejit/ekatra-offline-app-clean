package org.ekatra.alfred.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ekatra.alfred.InferenceEngine
import org.ekatra.alfred.LlamaEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideLlamaEngine(): LlamaEngine = LlamaEngine()

    /** All consumers should inject [InferenceEngine], not [LlamaEngine]. */
    @Provides
    @Singleton
    fun provideInferenceEngine(llama: LlamaEngine): InferenceEngine = llama
}
