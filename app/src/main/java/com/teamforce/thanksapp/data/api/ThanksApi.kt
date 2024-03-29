package com.teamforce.thanksapp.data.api

import com.teamforce.thanksapp.data.entities.StatusResponse
import com.teamforce.thanksapp.data.entities.Stickers.StickerEntity
import com.teamforce.thanksapp.data.entities.auth.AuthVkRequest
import com.teamforce.thanksapp.data.entities.auth.AuthVkResponse
import com.teamforce.thanksapp.data.entities.auth.ChooseOrgThroughAuthRequest
import com.teamforce.thanksapp.data.entities.benefit.*
import com.teamforce.thanksapp.data.entities.branding.OrganizationBrandingEntity
import com.teamforce.thanksapp.data.entities.challenges.*
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ChainEntity
import com.teamforce.thanksapp.data.entities.employees.DepartmentFilterEntity
import com.teamforce.thanksapp.data.entities.employees.EmployeeEntity
import com.teamforce.thanksapp.data.entities.events.EventEntity
import com.teamforce.thanksapp.data.entities.feed.FeedItemByIdEntity
import com.teamforce.thanksapp.data.entities.feed.FeedItemEntity
import com.teamforce.thanksapp.data.entities.notifications.*
import com.teamforce.thanksapp.data.entities.profile.*
import com.teamforce.thanksapp.data.entities.templates.TemplateEntity
import com.teamforce.thanksapp.data.entities.transaction.SendCoinsSettingsEntity
import com.teamforce.thanksapp.data.repository.FeedRepositoryImpl
import com.teamforce.thanksapp.data.request.*
import com.teamforce.thanksapp.data.request.awards.GainAwardRequest
import com.teamforce.thanksapp.data.request.awards.SetInStatusAwardRequest
import com.teamforce.thanksapp.data.request.onboarding.CreateCommunityRequest
import com.teamforce.thanksapp.data.request.onboarding.LaunchCommunityPeriodRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodResponse
import com.teamforce.thanksapp.data.response.*
import com.teamforce.thanksapp.data.response.admin.GetCurrentPeriodResponse
import com.teamforce.thanksapp.data.response.awards.GainAwardResponse
import com.teamforce.thanksapp.data.response.awards.GetAwardsResponse
import com.teamforce.thanksapp.data.response.benefitCafe.GetReviewResponse
import com.teamforce.thanksapp.data.response.challengeChains.ChallengeChainsResponse
import com.teamforce.thanksapp.data.response.challengeChains.ParticipantsChainResponse
import com.teamforce.thanksapp.data.response.challenges.ChallengesResponse
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.data.response.history.UserTransactionsGroupResponse
import com.teamforce.thanksapp.data.response.onboarding.CreateCommunityResponse
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.data.response.onboarding.InviteLinkResponse
import com.teamforce.thanksapp.data.response.onboarding.LaunchCommunityPeriodResponse
import com.teamforce.thanksapp.data.response.profile.ProfileSettingsResponse
import com.teamforce.thanksapp.data.response.recommendations.RecommendationsResponse
import com.teamforce.thanksapp.model.domain.ChallengeModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ThanksApi {

    /**
     * Начало авторизации в приложении, отправка tg or email, получение хеш кода
     * для дальнейшей отправки вместе с кодом
     */
    @POST("/auth/")
    fun authorization(
        @Body authorizationRequest: AuthorizationRequest,
    ): Call<AuthResponse>

    /**
     * Авторизация через VK, через токен полученный у VK
     */
    @POST("/api/auth/vkauth/")
    suspend fun authThroughVk(
        @Body authorizationRequest: AuthVkRequest,
    ): AuthVkResponse

    /**
     * Выбор организации для авторизации через вк
     */
    @POST("/api/auth/vkchooseorg/")
    suspend fun chooseOrgThroughVk(
        @Body authorizationRequest: ChooseOrgThroughAuthRequest,
    ): AuthVkResponse
    /**
     * Верификация после авторизации через Telegram
     */
    @POST("/verify/")
    fun verificationWithTelegram(
        @Header("X-Telegram") xId: String?,
        @Header("X-Code") xCode: String?,
        @Body verificationRequest: VerificationRequest,
    ): Call<VerificationResponse>

    /**
     * Верификация после авторизации через Email
     */
    @POST("/verify/")
    fun verificationWithEmail(
        @Header("X-Email") xEmail: String?,
        @Header("X-Code") xCode: String?,
        @Body verificationRequest: VerificationRequest,
    ): Call<VerificationResponse>

    /**
     * Выбор организации после стандартной авторизации(не через vk)
     */
    @POST("/choose-organization/")
    fun chooseOrganization(
        @Header("login") login: String?,
        @Body chooseOrgRequest: ChooseOrgRequest,
    ): Call<AuthResponse>

    /**
     * Выбор организации после стандартной авторизации(не через vk)
     */
    @POST("/user/change-organization/")
    suspend fun changeOrganization(
        @Body data: ChangeOrgRequest,
    ): ChangeOrgResponse

    /**
     * Верификация после запроса смены организации и переход будет осуществлятся через Телеграм
     * Логика перехода решается на беке, все зависит от того, какой параметр общий между профилями двух органзиаций
     */
    @POST("/user/change-organization/verify/")
    suspend fun changeOrganizationVerifyWithTelegram(
        @Body verificationRequest: VerificationRequestForChangeOrg,
    ): VerificationResponse

    /**
     * Верификация после запроса смены организации и переход будет осуществлятся через Email
     * Логика перехода решается на беке, все зависит от того, какой параметр общий между профилями двух органзиаций
     */
    @POST("/user/change-organization/verify/")
    suspend fun changeOrganizationVerifyWithEmail(
        @Body verificationRequest: VerificationRequestForChangeOrg,
    ): VerificationResponse

    /**
     * Получение настроек организации
     */
    @GET("/organization/settings/")
    suspend fun getOrgSettings(): GetOrgSettings

    /**
     * Получение данных своего профиля
     */
    @GET("/user/profile/")
    suspend fun getProfile(): ProfileEntity

    /**
     * Получение списка доступных организаций для текущего профиля
     */
    @GET("/user/organizations/")
    suspend fun getOrganizations(): List<OrganizationModel>

    /**
     * Получение данных о балансе профиля
     */
    @GET("/user/balance/")
    fun getBalance(): Call<BalanceResponse>

    /**
     * Отправка внутр валюты пользователю
     * @param recipient Id получателя
     * @param amount Кол во валюты
     * @param reason Описание благодарности
     * @param is_anonymous Настройка анонимности перевода(увидит ли получатель, кто перевел)
     * @param isPublic Настройка публичности(попадет ли в ленту)
     * @param tags Список тегов(предопределенные причины перевода)
     * @param stickerId Id стикера в переводе
     */
    @Multipart
    @POST("/send-coins/")
    suspend fun sendCoinsWithImage(
        @Part photo: List<MultipartBody.Part?>?,
        @Part("recipient") recipient: Int,
        @Part("amount") amount: Int,
        @Part("reason") reason: String,
        @Part("is_anonymous") is_anonymous: Boolean,
        @Part("is_public") isPublic: Boolean,
        @Part("tags") tags: String?,
        @Part("sticker_id") stickerId: Int?,
    ): SendCoinsResponse

    /**
     * Получение настроек перевода текущей организации.
     * Содержит предвыбранные параметры анонимности и публичности.
     */
    @GET("/send-coins-settings/")
    suspend fun getSendCoinsSettings(
    ): SendCoinsSettingsEntity

    /**
     * Получение списка переводов(История)
     */
    @GET("/user/transactions/")
    suspend fun getUserTransactions(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sent_only") sentOnly: Int?,
        @Query("received_only") receivedOnly: Int?,
        @Query("from") fromDate: String?,
        @Query("to") upDate: String?,
    ): List<HistoryItem.UserTransactionsResponse>

    /**
     * Группировка по транзакциям(не актуально, скрыл группировку на экране истории)
     */
    @GET("/user/transactions/group/")
    suspend fun getUserTransactionsGroup(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): UserTransactionsGroupResponse

    /**
     * Детали перевода по id в ленте
     */
    @GET("/events/transactions/{id}/")
    suspend fun getTransactionById(
        @Path("id") transactionId: String,
    ): FeedItemByIdEntity

    /**
     * Детали перевода по id на экране истории
     */
    @GET("/user/transactions/{id}/")
    suspend fun getTransactionByIdInHistory(
        @Path("id") transactionId: Int,
    ): HistoryItem.UserTransactionsResponse

    /**
     * Получение списка юзеров для выбора, кому перевести
     * в UsersListRequest в data записывается input поля ввода
     */
    @POST("/search-user/")
    suspend fun getUsersList(
        @Body usersWithInputRequest: UsersListRequest,
    ): List<UserListItem.UserBean>

    /**
     * Получение списка юзеров для выбора, кому перевести
     * без поля ввода
     */
    @POST("/users-list/")
    suspend fun getUsersWithoutInput(
        @Body get_users: UserListWithoutInputRequest,
    ): List<UserListItem.UserBean>

    /**
     * Обновление аватара пользователя
     */
    @Multipart
    @POST("/update-profile-image/{id}/")
    suspend fun putUserAvatar(
        @Path("id") userId: String,
        @Part photo: MultipartBody.Part,
        @Part cropped_photo: MultipartBody.Part,
    ): PutUserAvatarResponse

    /**
     * Отмена перевода(если такое можно сделать в организации)
     * Пока перевод находится в обработке(время обработки устанавливается на беке для каждой организации отдельно)
     * его можно отменить, он так же может быть отменен модератором
     */
    @PUT("/cancel-transaction/{id}/")
    suspend fun cancelTransaction(
        @Path("id") transactionId: String,
        @Body status: CancelTransactionRequest,
    ): CancelTransactionResponse

    /**
     * Обновление профиля(кроме аватара)
     */
    @PUT("/update-profile-by-user/{id}/")
    suspend fun updateProfile(
        @Path("id") userId: String,
        @Body data: UpdateProfileRequest,
    ): UpdateProfileResponse

    /**
     * Обновление контактов профиля(номер телефона и почта)
     */
    @POST("/create-few-contacts/")
    fun updateFewContact(
        @Body data: List<ContactEntity>?,
    ): Call<UpdateFewContactsResponse>

    /**
     * Получение деталей профиля другого пользователя(не своего)
     */
    @GET("/profile/{user_id}/")
    suspend fun getAnotherProfile(
        @Path("user_id") user_Id: Int,
    ): ProfileEntity

    /**
     * Установка лайка
     */
    @POST("/press-like/")
    suspend fun pressLike(
        @Body data: Map<String, Int>,
    ): LikeResponse

    /**
     * Получение списка комментариев у объекта
     */
    @POST("/get-comments/")
    suspend fun getComments(
        @Body body: GetCommentsRequest,
    ): GetCommentsResponse

    /**
     * Создание комментария к объекту
     */
    @Multipart
    @POST("/create-comment/")
    suspend fun createComment(
        @Part picture: MultipartBody.Part?,
        @Part("challenge_id") challengeId: Int?,
        @Part("transaction_id") transactionId: Int?,
        @Part("challenge_report_id") challengeReportId: Int?,
        @Part("offer_id") offerId: Int?,
        @Part("text") text: String?,
        @Part("gif") gif: String?,
    ): CancelTransactionResponse

    /**
     * Удаление комментария
     */
    @DELETE("/delete-comment/{comment_id}/")
    suspend fun deleteComment(
        @Path("comment_id") commentId: Int,
    ): CancelTransactionResponse

    /**
     * Создания челленджа
     * @param parameter_id Если 2 то parameter_value это призовой фонд
     * @param parameter_value это призовой фонд
     */
    @Multipart
    @POST("/api/challenges/")
    suspend fun createChallenge(
        @Part("name") name: String,
        @Part("description") description: String,
        @Part("end_at") endAt: String?,
        @Part("start_at") startAt: String?,
        @Part("start_balance") amountFund: Int,
        @Part("parameter_id") parameter_id: Int,
        @Part("parameter_value") parameter_value: Int,
        @Part("multiple_reports") severalReports: String,
        @Part("challenge_type") challengeType: String,
        @Part("show_contenders") showContenders: String,
        @Part("account_id") debitAccountId: Int?,
        @Part("template_id") templateId: Int?,
        @Part photos: List<MultipartBody.Part>,
    ): ChallengeModel

    /**
     * Обнволение челленджа
     * @param fileList это список старых фото, которые остаеются при обновлении
     * @param photos список новых фото
     */
    @Multipart
    @JvmSuppressWildcards
    @PATCH("/api/challenges/{id}/")
    suspend fun updateChallenge(
        @Path("id") challengeId: Int,
        @Part("end_at") endAt: String?,
        @Part("start_at") startAt: String?,
        @Part("start_balance") startBalance: Int?,
        @Part("winners_count") winnersCount: Int?,
        @Part("multiple_reports") multipleReports: Boolean?,
        @Part("account_id") debitAccountId: Int?,
        @Part("show_contenders") showContenders: Boolean?,
        @Part("challenge_type") challengeType: String?,
        @Part("name") name: String?,
        @Part("description") description: String?,
        // @Part("fileList") fileList: List<ImageFileData>? = null,
        @PartMap fileList: Map<String, List<ImageFileData>>? = null,
        @Part photos: List<MultipartBody.Part>? = null,
    ): CommonStatusResponse

    /**
     * Получение предвыбранных настроек при создании чалика
     */
    @GET("/create-challenge/")
    suspend fun getCreateChallengeSettings(
    ): CreateChallengeSettingsEntity

    /**
     * Получение списка категорий в определенном скоупе
     */
    @GET("/get-sections/")
    suspend fun getSections(@Query("scope") scope: Int): GetSectionsEntity

    /**
     * Обновление категории(изменение названия)
     */
    @PATCH("/template-section/{id}/")
    suspend fun updateSections(
        @Path("id") sectionId: Int,
        @Body data: SectionUpdateRequest,
    ): Any

    /**
     * Создание новой категории
     */
    @POST("/create-challenge-template-section/")
    suspend fun createSections(
        @Body data: SectionUpdateRequest,
    ): Any

    /**
     * Удаление категории
     */
    @DELETE("/template-section/{id}/")
    suspend fun removeSections(@Path("id") sectionId: Int)

    /**
     * Получение списка чаликов
     * @param chainId - при передаче id цепочки будет список чаликов только данной цепочки
     */
    @GET("/api/challenges/")
    suspend fun getChallenges(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("active_only") activeOnly: Int = 0,
        @Query("show_deferred") showDelayedChallenges: Int = 0,
        @Query("group_id") chainId: Long? = null,
        @Query("dependent") challengeId: Int? = null,
    ): ChallengesResponse

    /**
     * Получение списка цепочке чаликов
     */
    @GET("/api/{org_id}/challenges/groups/")
    suspend fun getChallengeChains(
        @Path("org_id") organizationId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("current_state") state: Char?,
    ): ChallengeChainsResponse

    /**
     * Получение деталей цеопчки чаликов
     */
    @GET("/api/{org_id}/challenges/groups/{group_id}")
    suspend fun getChain(
        @Path("org_id") organizationId: String,
        @Path("group_id") chainId: String,
    ): ChainEntity

    /**
     * Получение участников цепочки чаликов
     */
    @GET("/api/{org_id}/challenges/groups/{group_id}/participants/")
    suspend fun getChainParticipants(
        @Path("org_id") organizationId: String,
        @Path("group_id") chainId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ParticipantsChainResponse

    /**
     * Удаление челленджа
     */
    @DELETE("api/challenges/{challenge_id}/")
    suspend fun deleteChallenge(
        @Path("challenge_id") challengeId: Int,
    ): CommonStatusResponse

    /**
     * Получение деталей челленджа
     */
    @GET("/challenges/{challenge_id}/")
    suspend fun getChallenge(
        @Path("challenge_id") challengeId: Int,
    ): ChallengeEntityById

    /**
     * Создания отчета к челленджу
     */
    @Multipart
    @POST("/create-challenge-report/")
    suspend fun createChallengeReport(
        @Part photo: MultipartBody.Part?,
        @Part("challenge") challengeId: RequestBody,
        @Part("text") comment: RequestBody,
    ): CreateReportResponse

    /**
     * Получение списка претендентов у чалика(те кто отправили отчеты)
     */
    @GET("/challenge-contenders/{challenge_id}/")
    suspend fun getChallengeContenders(
        @Path("challenge_id") challengeId: Int,
    ): List<GetChallengeContendersResponse.Contender>

    /**
     * Принятие или отклонение отчета челленджа
     */
    @PUT("/check-challenge-report/{challenge_id}/")
    suspend fun checkChallengeReport(
        @Path("challenge_id") challengeId: Int,
        @Body data: CheckChallengeReportRequest?,
    ): CheckReportResponse

    /**
     * Получение списка победителей челленджа
     */
    @GET("/challenge-winners-reports/{challenge_id}/")
    suspend fun getChallengeWinners(
        @Path("challenge_id") challengeId: Int,
    ): List<GetChallengeWinnersResponse.Winner>

    /**
     * Получение твоего результата у конуретного челленджа
     */
    @GET("/challenge-result/{challenge_id}/")
    suspend fun getChallengeResult(
        @Path("challenge_id") challengeId: Int,
    ): List<GetChallengeResultResponse>

    /**
     * Получение деталей отчета к челленджу
     */
    @GET("/challenge-report/{challenge_report_id}/")
    suspend fun getChallengeReportDetails(
        @Path("challenge_report_id") challengeReportId: Int,
    ): GetChallengeReportDetailsResponse


    /**
     * Получение списка событий(ленты)
     */
    @GET("/api/events/")
    suspend fun getEventsNew(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("filters") filters: List<Int>?,
    ): EventEntity

    /**
     * Сохранение фильтров для раздела ВСЕ в ленте, да раздел ВСЕ может показывать не все, а его настрйоки можно сохранить
     */
    @POST("/api/events/post_eventtypes_filter/")
    suspend fun saveFilters(
        @Body data: FeedRepositoryImpl.SaveFilterRequest,
    ): StatusResponse

    /**
     * Получение списка реакций у объекта
     */
    @POST("/get-likes/")
    suspend fun getReactions(
        @Body data: GetReactionsRequest,
    ): GetReactionsResponse

    /**
     * Установка push token
     */
    @POST("/set-fcm-token/")
    suspend fun setPushToken(@Body token: PushTokenEntity): PushTokenEntity

    /**
     * Удаление push token
     */
    @POST("/remove-fcm-token/")
    suspend fun removePushToken(@Body remove: RemovePushTokenEntity): RemovePushTokenResultEntity

    /**
     * Получение списка уведомлений
     */
    @GET("/notifications/")
    suspend fun getNotifications(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): List<NotificationEntity>

    /**
     * Получение кол во непрочитанных уведомлений
     */
    @GET("/notifications/unread/amount/")
    suspend fun getUnreadNotificationAmount(): UnreadNotificationsAmountEntity

    /**
     * получение статистики полученных спасибок по категориям
     */
    @GET("/user/profile/stats/")
    suspend fun getProfileStats(
        @Query("sender_only") senderOnly: Int?,
        @Query("recipient_only") recipientOnly: Int?,
    ): List<ProfileStatsEntity>


    // Reviews
    // 27.03.2024 На данный момент функционал скрыт(
    // view для перехода к отзывам скрыта, но сам функционал есть и даже работает, но он не был протестирован тестировщиками)

    /**
     * Получение списка отзывов на бенефит
     */
    @GET("/market/{market_id}/offers/{offer_id}/reviews")
    suspend fun getReviews(
        @Path("market_id") marketId: Int,
        @Path("offer_id") offerId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("order_by") orderBy: String?,
        @Query("desc") reverseOutput: Int = 0,
    ): GetReviewResponse

    /**
     * Создание отзыва
     */
    @Multipart
    @POST("/market/{market_id}/offers/{offer_id}/reviews")
    suspend fun createReview(
        @Path("market_id") marketId: Int,
        @Path("offer_id") offerId: Long,
        @Part photos: List<MultipartBody.Part?>?,
        @Part("text") text: String,
        @Part("rate") rate: Int,
    ): CommonStatusResponse

    /**
     * Удаление отзыва
     */
    @DELETE("/market/{market_id}/reviews/{review_id}")
    suspend fun deleteReview(
        @Path("market_id") marketId: Int,
        @Path("review_id") reviewId: Long,
    ): CommonStatusResponse?

    /**
     * Обновление отзыва
     */
    @Multipart
    @JvmSuppressWildcards
    @PATCH("/market/{market_id}/reviews/{review_id}")
    suspend fun updateReview(
        @Path("market_id") marketId: Int,
        @Path("review_id") reviewId: Long,
        @Part photos: List<MultipartBody.Part?>?,
        @PartMap fileList: Map<String, List<ImageFileData>>? = null,
        @Part("text") text: String,
        @Part("rate") rate: Int,
    ): CommonStatusResponse

    /**
     * Получение списка бнефитов
     */
    @GET("/market/{market_id}/offers/")
    suspend fun getOffers(
        @Path("market_id") marketId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
//        @Query("order_by") orderBy: Int,
        @Query("category") category: Int?,
        @Query("contain") contain: String? = null,
        @Query("min_price") minPrice: String? = null,
        @Query("max_price") maxPrice: String? = null,
    ): List<BenefitItemEntity>

    /**
     * Получение деталей бенефита
     */
    @GET("/market/{market_id}/offers/{offer_id}/")
    suspend fun getOfferById(
        @Path("market_id") marketId: Int,
        @Path("offer_id") offerId: Int,
    ): BenefitItemByIdEntity

    /**
     * Поулчение деталей бенефита
     */
    @GET("/market/{market_id}/categories/")
    suspend fun getBenefitCategories(
        @Path("market_id") marketId: Int,
    ): List<Category>

    /**
     * Добавление бенефита в корзину покупок
     */
    @POST("/market/{market_id}/add-to-cart/")
    suspend fun addBenefitToCart(
        @Path("market_id") marketId: Int,
        @Body data: AddBenefitToCartRequest,
    ): AddBenefitToCartResponse

    /**
     * Получение списка бенефитов в корзине
     */
    @GET("/market/{market_id}/cart/")
    suspend fun getOffersInCart(
        @Path("market_id") marketId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): List<OfferInCart>

    /**
     * Получение списка бенефитов в корзине
     */
    @DELETE("/market/{market_id}/cart/{order_id}/")
    suspend fun deleteOffersInCart(
        @Path("market_id") marketId: Int,
        @Path("order_id") orderId: Int,
    ): CancelTransactionResponse

    /**
     * Покупка бенефитов(все отмеченные бенефиты в корзине будут отправлены на рассмотрение модератору для покупки)
     */
    @POST("/market/{market_id}/orders/")
    suspend fun transactionOffersFromCartToOrder(
        @Path("market_id") marketId: Int,
        @Body data: TransactionOffersFromCartToOrderRequest,
    ): TransactionOffersFromCartToOrderResponse

    /**
     * Получение списка истории заказов(покупок)
     */
    @GET("/market/{market_id}/orders/")
    suspend fun getOrders(
        @Path("market_id") marketId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("order_status") orderStatus: List<String>? = null,
    ): List<Order>

    /**
     * Получение списка доступных маркетов в данной организации(их может быть несколько по идее, но
     * на данный момент переключения никакого нет, выбирается первый доступный)
     */
    @GET("/markets/available/")
    suspend fun getAvailableMarkets(): List<AvailableMarketEntity>

    /**
     * Получение списка стикеров
     */
    @GET("/stickers/get/")
    suspend fun getAllStickers(): List<StickerEntity>

    /**
     * Получение списка участников текущей организации
     */
    @POST("/colleagues/")
    suspend fun getColleagues(
        @Body body: GetEmployeesRequest,
    ): List<EmployeeEntity>

    /**
     * Получение списка департаментов текущей организации
     */
    @GET("/departments/tree/")
    suspend fun getDepartmentsTree(): List<DepartmentFilterEntity>

    /**
     * Получение списка шаблонов челленджей(по ним можно быстрее создавать челленджы)
     */
    @GET("/get-challenges-templates/")
    suspend fun getTemplates(
        @Query("section") selectedSections: List<Int>? = emptyList(),
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("scope") scope: String = "0",
        @Query("template_type") templateType: String = "challenge"
    ): List<TemplateEntity>

    /**
     * Создание шаблона челленджа
     */
    @Multipart
    @POST("/create-challenge-template/")
    suspend fun createTemplate(
        @Part photos: List<MultipartBody.Part?>,
        @Part("name") name: String,
        @Part("description") description: String,
        @Part("sections") sections: String = "",
        @Part("scope") scope: String,
        @Part("multiple_reports") severalReports: String,
        @Part("challenge_type") challengeType: String,
        @Part("show_contenders") showContenders: String,
    ): Any

    /**
     * Редактирование шаблона челленджа
     */
    @Multipart
    @JvmSuppressWildcards
    @POST("/update-challenge-template/")
    suspend fun updateTemplate(
        @PartMap fileList: Map<String, List<ImageFileData>>? = null,
        @Part photos: List<MultipartBody.Part>? = null,
        @Part("name") name: String,
        @Part("challenge_template") templateId: Int,
        @Part("description") description: String,
        @Part("sections") sections: String = "",
        @Part("scope") scope: String,
        @Part("multiple_reports") severalReports: String,
        @Part("challenge_type") challengeType: String,
        @Part("show_contenders") showContenders: String,
    ): Any

    /**
     * Создание организации
     */
    @POST("/community/")
    suspend fun createCommunity(
        @Body body: CreateCommunityRequest,
    ): CreateCommunityResponse

    /**
     * Запуск периода в текущей организации
     */
    @POST("/community/period/")
    suspend fun launchCommunityPeriod(
        @Body body: LaunchCommunityPeriodRequest,
    ): LaunchCommunityPeriodResponse

    /**
     * Получение ссылки-приглашения в текущую организацию
     */
    @GET("/invitelink/")
    suspend fun getInviteLink(
        @Query("organization_id") organizationId: Int?,
    ): InviteLinkResponse

    /**
     * Получение данных об организации по ссылки-приглашения
     */
    @GET("/community/invite/")
    suspend fun getOrgByInvitation(
        @Query("invite") invite: String,
    ): InvitationOrganizationResponse

    /**
     * Получение данных об текущем периоде
     */
    @GET("/get-current-period/")
    suspend fun getCurrentPeriod(): GetCurrentPeriodResponse

    /**
     * Получение настроек брендирования организации
     */
    @GET("/organizations/{id}/brand/")
    suspend fun getOrganizationBranding(
        @Path("id") organizationId: Int,
    ): OrganizationBrandingEntity

    /**
     * Создание организации с стандартными настрйоками периода
     */
    @POST("/community/create/")
    suspend fun createCommunity(
        @Body data: CreateCommunityWithPeriodRequest,
    ): CreateCommunityWithPeriodResponse

    /**
     * Получение списка наград
     */
    @GET("/api/{organization_id}/awards/")
    suspend fun getAwards(
        @Path("organization_id") orgId: Int? = 0,
        @Query("my_awards") myAwards: Boolean? = null
    ): GetAwardsResponse

    /**
     * Забрать награду(доступно, если выполнено условие)
     */
    @POST("/api/{organization_id}/awards/")
    suspend fun gainAward(
        @Path("organization_id") orgId: Int? = 0,
        @Body data: GainAwardRequest
    ): GainAwardResponse

    /**
     * Установка награды в профиль
     */
    @POST("/api/{organization_id}/awards/status/")
    suspend fun setInStatusAward(
        @Path("organization_id") orgId: Int? = 0,
        @Body data: SetInStatusAwardRequest
    ): Any

    /**
     * Обновление настроек профиля(используется для обновления языка профиля)
     */
    @PATCH("/api/profile/settings/")
    suspend fun profileSettingsUpdate(
        @Body profileSettings: ProfileSettingsForRequestEntity
    ): ProfileSettingsResponse

    /**
     * Получение настроек профиля
     */
    @GET("/api/profile/settings/")
    suspend fun getProfileSettings(): ProfileSettingsEntity

    /**
     * Получение списка рекомендаций(см главный экран)
     * Там могут быть, челленджы,цепочки челленджей, опросы
     */
    @GET("/api/recommendations")
    suspend fun getRecommendations(): RecommendationsResponse


}
