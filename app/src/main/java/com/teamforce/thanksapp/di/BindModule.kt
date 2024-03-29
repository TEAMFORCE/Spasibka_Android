package com.teamforce.thanksapp.di

import com.teamforce.thanksapp.data.repository.*
import com.teamforce.thanksapp.domain.repositories.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BindModule {

    @Binds
    fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    fun bindChallengeRepository(challengeRepositoryImpl: ChallengeRepositoryImpl): ChallengeRepository

    @Binds
    fun bindHistoryRepository(bindHistoryRepositoryImpl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    fun bindFeedRepository(feedRepositoryImpl: FeedRepositoryImpl): FeedRepository

    @Binds
    fun bindReactionRepository(reactionRepositoryImpl: ReactionRepositoryImpl): ReactionRepository

    @Binds
    fun bindLocationRepository(locationRepositoryImpl: LocationRepositoryImpl): LocationRepository

    @Binds
    fun bindBenefitRepository(benefitRepositoryImpl: BenefitRepositoryImpl): BenefitRepository

    @Binds
    fun bindEmployeesRepository(employeeRepositoryImpl: EmployeesRepositoryImpl): EmployeeRepository

    @Binds
    fun bindUsersRepository(usersRepositoryImpl: UsersRepositoryImpl): UsersRepository

    @Binds
    fun bindTemplatesRepository(templatesRepositoryImpl: TemplatesRepositoryImpl): TemplatesRepository

    @Binds
    fun bindAdministrationRepository(administrationRepositoryImpl: AdministrationRepositoryImpl): AdministrationRepository

    @Binds
    fun bindBrandingRepository(brandingRepositoryImpl: BrandingRepositoryImpl): BrandingRepository

    @Binds
    fun bindAuthRepository(brandingAuthImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl): TransactionsRepository

    @Binds
    fun bindAwardsRepository(awardsRepositoryImpl: AwardsRepositoryImpl): AwardsRepository

    @Binds
    fun bindRecommendationsRepository(recommendationsRepositoryImpl: RecommendationsRepositoryImpl): RecommendationsRepository

}