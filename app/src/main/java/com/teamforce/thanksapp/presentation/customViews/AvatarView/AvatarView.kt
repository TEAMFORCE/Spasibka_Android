package com.teamforce.thanksapp.presentation.customViews.AvatarView

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.internal.*
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import java.lang.Float.min
import kotlin.math.min

/**
 * AvatarView supports segmented style images, borders, indicators, and initials.
 */

class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr), Themable {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val indicatorOutlinePaint = Paint().apply { style = Paint.Style.FILL }
    private val indicatorPaint = Paint().apply { style = Paint.Style.FILL }
    private val backgroundPaint = Paint().apply { style = Paint.Style.FILL }
    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    private val indicatorTextPaint = Paint().apply {
        color =
            Color.parseColor(Branding.brand.colorsJson.minorSuccessColor)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    /** The border width of AvatarView. */
    @get:Px
    public var avatarBorderWidth: Int by viewProperty(3.dp)

    /** The border color of AvatarView. */
    @get:ColorInt
    public var avatarBorderColor: Int by viewProperty(Color.TRANSPARENT)

    /** The border color array of AvatarView. */
    public var avatarBorderColorArray: IntArray by viewProperty(intArrayOf())

    /** The border radius of AvatarView. */
    @get:Px
    public var avatarBorderRadius: Float by viewProperty(6.dp.toFloat())

    /** The shape of the AvatarView. */
    public var avatarShape: AvatarShape by viewProperty(AvatarShape.CIRCLE)

    /** The initials to be drawn instead of an image. */
    private var _avatarInitials: String? by viewProperty(null)
    public var avatarInitials: String? = null //by viewProperty(null)
        set(value) {
            setImageDrawable(null)
            if (value.isNullOrEmpty()) {
                _avatarInitials = "??"
                field = "??"
            } else {
                _avatarInitials = value
                field = value
            }
        }

    /** The text size of the initials. */
    @get:Px
    public var avatarInitialsTextSize: Int by viewProperty(-1)

    /** The text size ratio of the initials. */
    @get:FloatRange(from = 0.0, to = 1.0)
    public var avatarInitialsTextSizeRatio: Float by viewProperty(0.33f)

    /** The text color of the initials. */
    @get:ColorInt
    public var avatarInitialsTextColor: Int by viewProperty(Color.WHITE)

    /** The text styles color of the initials. */
    public var avatarInitialsStyle: Int by viewProperty(Typeface.NORMAL)

    /** The background color of the initials. */
    @get:ColorInt
    public var avatarInitialsBackgroundColor: Int by viewProperty(internalBlue)

    public var useGradient: Boolean by viewProperty(false)

    /** The background gradient color start of the initials. */
    @get:ColorInt
    var avatarInitialsBackgroundGradientColorStart: Int by viewProperty(internalBlue)

    /** The background gradient color end of the initials. */
    @get:ColorInt
    var avatarInitialsBackgroundGradientColorEnd: Int by viewProperty(internalBlue)

    /** Sets the visibility of the indicator. */
    public var indicatorEnabled: Boolean by viewProperty(false)

    /** The position of the indicator. */
    public var indicatorPosition: IndicatorPosition by viewProperty(IndicatorPosition.TOP_RIGHT)

    /** The color of the indicator. */
    @get:ColorInt
    public var indicatorColor: Int by viewProperty(internalGreen)


    var indicatorText: String = "??"
        set(value) {
            field = if (value.isNullOrEmpty()) {
                "??"
            } else {
                value
            }
        }

    /** The border color of the indicator. */
    @get:ColorInt
    public var indicatorBorderColor: Int by viewProperty(Color.WHITE)

    /** The border color array of the indicator. */
    public var indicatorBorderColorArray: IntArray by viewProperty(intArrayOf())

    /** The size criteria of the indicator. */
    public var indicatorSizeCriteria: Float by viewProperty(8f)

    /** The border size criteria of the indicator. This must be bigger than the [indicatorSizeCriteria]. */
    public var indicatorBorderSizeCriteria: Float by viewProperty(10f)

    /** A custom indicator view. */
    public var indicatorDrawable: Drawable? by viewProperty(null)

    /** Supports RTL layout is enabled or not. */
    public var supportRtlEnabled: Boolean by viewProperty(true)

    /** A placeholder that should be shown when loading an image. */
    public var placeholder: Drawable? by viewProperty(null)

    /** An error placeholder that should be shown when request failed. */
    public var errorPlaceholder: Drawable? by viewProperty(null)

    /**
     * The maximum section size of the avatar when loading multiple images.
     * This size must between 1 and 4.
     */
    public var maxSectionSize: Int = 4
        set(value) {
            field = value.coerceIn(1..4)
        }

    init {
        initAttributes(attrs, defStyleAttr)
    }

    private fun initAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        attrs ?: return
        context.obtainStyledAttributes(attrs, R.styleable.AvatarView, defStyleAttr, 0)
            .use { typedArray ->
                avatarBorderWidth = typedArray.getDimensionPixelSize(
                    R.styleable.AvatarView_avatarViewBorderWidth, avatarBorderWidth
                )
                avatarBorderColor = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewBorderColor, avatarBorderColor
                )
                avatarBorderColorArray = typedArray.getIntArray(
                    R.styleable.AvatarView_avatarViewBorderColorArray, intArrayOf()
                )
                avatarBorderRadius = typedArray.getDimension(
                    R.styleable.AvatarView_avatarViewBorderRadius,
                    avatarBorderRadius
                )
                _avatarInitials = typedArray.getString(
                    R.styleable.AvatarView_avatarViewInitials
                )
                avatarInitialsTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.AvatarView_avatarViewInitialsTextSize,
                    avatarInitialsTextSize
                )
                avatarInitialsTextSizeRatio = typedArray.getFloat(
                    R.styleable.AvatarView_avatarViewInitialsTextSizeRatio,
                    avatarInitialsTextSizeRatio
                )
                avatarInitialsTextColor = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewInitialsTextColor,
                    avatarInitialsTextColor
                )
                avatarInitialsBackgroundColor = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewInitialsBackgroundColor,
                    avatarInitialsBackgroundColor
                )
                useGradient = typedArray.getBoolean(
                    R.styleable.AvatarView_useGradient,
                    useGradient
                )
                avatarInitialsBackgroundGradientColorStart = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewInitialsBackgroundGradientColorStart,
                    avatarInitialsBackgroundGradientColorStart
                )
                avatarInitialsBackgroundGradientColorEnd = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewInitialsBackgroundGradientColorEnd,
                    avatarInitialsBackgroundGradientColorEnd
                )
                avatarInitialsStyle = typedArray.getInt(
                    R.styleable.AvatarView_avatarViewInitialsTextStyle,
                    avatarInitialsStyle
                )
                avatarShape = typedArray.getEnum(
                    R.styleable.AvatarView_avatarViewShape,
                    avatarShape
                )
                indicatorEnabled = typedArray.getBoolean(
                    R.styleable.AvatarView_avatarViewIndicatorEnabled,
                    indicatorEnabled
                )
                indicatorPosition = typedArray.getEnum(
                    R.styleable.AvatarView_avatarViewIndicatorPosition,
                    indicatorPosition
                )
                indicatorColor = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewIndicatorColor, indicatorColor
                )
                indicatorText = typedArray.getString(
                    R.styleable.AvatarView_avatarViewIndicatorText
                ) ?: "??"
                indicatorBorderColor = typedArray.getColor(
                    R.styleable.AvatarView_avatarViewIndicatorBorderColor,
                    indicatorBorderColor
                )
                indicatorBorderColorArray = typedArray.getIntArray(
                    R.styleable.AvatarView_avatarViewIndicatorBorderColorArray,
                    indicatorBorderColorArray
                )
                indicatorSizeCriteria = typedArray.getFloat(
                    R.styleable.AvatarView_avatarViewIndicatorSizeCriteria,
                    indicatorSizeCriteria
                )
                indicatorBorderSizeCriteria = typedArray.getFloat(
                    R.styleable.AvatarView_avatarViewIndicatorBorderSizeCriteria,
                    indicatorBorderSizeCriteria
                )
                indicatorDrawable = typedArray.getDrawable(
                    R.styleable.AvatarView_avatarViewIndicatorDrawable
                )
                supportRtlEnabled = typedArray.getBoolean(
                    R.styleable.AvatarView_avatarViewSupportRtlEnabled,
                    supportRtlEnabled
                )
                maxSectionSize = typedArray.getInt(
                    R.styleable.AvatarView_avatarViewMaxSectionSize,
                    maxSectionSize
                )
                placeholder = typedArray.getDrawable(
                    R.styleable.AvatarView_avatarViewPlaceholder
                )
                errorPlaceholder = typedArray.getDrawable(
                    R.styleable.AvatarView_avatarViewErrorPlaceholder
                )
            }
        setThemeColor(Branding.appTheme)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(0, widthMeasureSpec)
        val height = resolveSize(0, heightMeasureSpec)
        val avatarViewSize = width.coerceAtMost(height)
        setMeasuredDimension(avatarViewSize, avatarViewSize)
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable != null) {
            // Если есть изображение, рисуем его
            super.onDraw(canvas)
            applyPaintStyles()
            drawBorder(canvas)
            drawIndicator(canvas)
        } else if (!avatarInitials.isNullOrEmpty()) {
            // Если нет изображения, но есть инициалы, рисуем их
            applyPaintStyles()
            drawColor(canvas)
            drawInitials(canvas)
            drawBorder(canvas)
            drawIndicator(canvas)
        }
    }

    /** Applies custom attributes to [AvatarView]. */
    private fun applyPaintStyles() {
        borderPaint.color = avatarBorderColor
        borderPaint.strokeWidth = avatarBorderWidth.toFloat()
        // Убрал установку padding, тк идет отличие размер view с инициалами и с фото
        // val padding = (avatarBorderWidth - AVATAR_SIZE_EXTRA).coerceAtLeast(0)
        val padding = (AVATAR_SIZE_EXTRA).coerceAtLeast(0)
        setPadding(padding, padding, padding, padding)
        indicatorOutlinePaint.color = indicatorBorderColor
        indicatorPaint.color = indicatorColor
        if (!useGradient) backgroundPaint.color = avatarInitialsBackgroundColor
        else {
            backgroundPaint.shader = LinearGradient(
                0f,
                0f,
                measuredWidth.toFloat(),
                measuredHeight.toFloat(),
                avatarInitialsBackgroundGradientColorStart,
                avatarInitialsBackgroundGradientColorEnd,
                Shader.TileMode.MIRROR
            )
        }

        textPaint.color = avatarInitialsTextColor
        textPaint.typeface = Typeface.defaultFromStyle(avatarInitialsStyle)
        textPaint.textSize = avatarInitialsTextSize.takeIf { it != -1 }?.toFloat()
            ?: (avatarInitialsTextSizeRatio * width)
    }

    /** Draws a border to [AvatarView]. */
    private fun drawBorder(canvas: Canvas) {
        if (avatarBorderWidth == 0) return

        if (avatarShape == AvatarShape.ROUNDED_RECT) {
            canvas.drawRoundRect(
                BORDER_OFFSET,
                BORDER_OFFSET,
                width.toFloat() - BORDER_OFFSET,
                height.toFloat() - BORDER_OFFSET,
                avatarBorderRadius,
                avatarBorderRadius,
                borderPaint.applyGradientShader(avatarBorderColorArray, width / 2f, height / 2f)
            )
        } else {
            canvas.drawCircle(
                width / 2f,
                height / 2f,
                width / 2f - avatarBorderWidth / 2,
                borderPaint.applyGradientShader(avatarBorderColorArray, width / 2f, height / 2f)
            )
        }
    }

    /** Draws initials to [AvatarView]. */
    private fun drawInitials(canvas: Canvas) {
        val initials = this.avatarInitials ?: return

        // Calculate the available space inside the circle
        val availableWidth = measuredWidth - avatarBorderWidth * 2
        val availableHeight = measuredHeight - avatarBorderWidth * 2

        // Calculate the maximum allowed text size to fit inside the circle
        val maxTextSize = min(availableWidth, availableHeight) * 0.5

        // Set the text size to be a fraction of the maximum allowed size
        textPaint.textSize = maxTextSize.toFloat()

        // Calculate the position to center the text inside the circle
        val textX = measuredWidth.toFloat() / 2
        val textY =
            (measuredHeight.toFloat() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)

        canvas.drawText(
            initials.parseInitials,
            textX,
            textY,
            textPaint
        )
    }

    /** Draws color of the initials to [AvatarView]. */
    private fun drawColor(canvas: Canvas) {
        if (avatarShape == AvatarShape.ROUNDED_RECT) {
            canvas.drawRoundRect(
                0F,
                0F,
                width.toFloat(),
                height.toFloat(),
                avatarBorderRadius,
                avatarBorderRadius,
                backgroundPaint
            )
        } else {
            canvas.drawCircle(
                width / 2f,
                height / 2f,
                width / 2f,
                backgroundPaint
            )
        }
    }

    /** Draws an indicator to [AvatarView]. */
    private fun drawIndicator(canvas: Canvas) {
        if (indicatorEnabled) {
            val isRtlEnabled = supportRtlEnabled && isRtlLayout

            val customIndicator = indicatorDrawable
            if (customIndicator != null) with(customIndicator) {
                val cx: Float = when (indicatorPosition) {
                    IndicatorPosition.TOP_LEFT,
                    IndicatorPosition.BOTTOM_LEFT,
                    -> if (isRtlEnabled) width - (width / indicatorSizeCriteria)
                    else 0f

                    IndicatorPosition.TOP_RIGHT,
                    IndicatorPosition.BOTTOM_RIGHT,
                    -> if (isRtlEnabled) width / indicatorSizeCriteria
                    else width - (width / indicatorSizeCriteria)
                }
                val cy: Float = when (indicatorPosition) {
                    IndicatorPosition.TOP_LEFT,
                    IndicatorPosition.TOP_RIGHT,
                    -> 0f

                    IndicatorPosition.BOTTOM_LEFT,
                    IndicatorPosition.BOTTOM_RIGHT,
                    -> height - height / indicatorSizeCriteria
                }
                setBounds(
                    cx.toInt(),
                    cy.toInt(),
                    (cx + width / indicatorSizeCriteria).toInt(),
                    (cy + height / indicatorSizeCriteria).toInt()
                )
                draw(canvas)
            } else {
                val cx: Float = when (indicatorPosition) {
                    IndicatorPosition.TOP_LEFT,
                    IndicatorPosition.BOTTOM_LEFT,
                    -> if (isRtlEnabled) width - (width / indicatorSizeCriteria)
                    else width / indicatorSizeCriteria

                    IndicatorPosition.TOP_RIGHT,
                    IndicatorPosition.BOTTOM_RIGHT,
                    -> if (isRtlEnabled) width / indicatorSizeCriteria
                    else width - (width / indicatorSizeCriteria)
                }

                val cy: Float = when (indicatorPosition) {
                    IndicatorPosition.TOP_LEFT,
                    IndicatorPosition.TOP_RIGHT,
                    -> height / indicatorSizeCriteria

                    IndicatorPosition.BOTTOM_LEFT,
                    IndicatorPosition.BOTTOM_RIGHT,
                    -> height - height / indicatorSizeCriteria
                }

                // Рисование круглого индикатора
                canvas.drawCircle(
                    cx,
                    cy,
                    width / indicatorSizeCriteria,
                    indicatorOutlinePaint.applyGradientShader(indicatorBorderColorArray, cx, cy)
                )
                canvas.drawCircle(cx, cy, width / indicatorBorderSizeCriteria, indicatorPaint)


                val textSize = min(
                    (width / indicatorSizeCriteria) * 0.8,
                    (height / indicatorSizeCriteria) * 0.8
                )
                indicatorTextPaint.textSize = textSize.toFloat()

                val textBaseline: Float =
                    cy - (indicatorTextPaint.fontMetrics.descent + indicatorTextPaint.fontMetrics.ascent) / 2
                canvas.drawText(indicatorText, cx, textBaseline, indicatorTextPaint)
            }
        }
    }


    /** Apply gradient shader to a [Paint]. */
    private fun Paint.applyGradientShader(colorArray: IntArray, cx: Float, cy: Float): Paint =
        apply {
            if (colorArray.isNotEmpty()) {
                shader = SweepGradient(
                    cx,
                    cy,
                    colorArray,
                    colorArray.arrayPositions
                )
            }
        }

    fun setIndicatorRes(@DrawableRes drawableRes: Int) {
        indicatorDrawable = ResourcesCompat.getDrawable(resources, drawableRes, null)
    }

    fun updatePicture(toUri: Uri, onComplete: ((ImageView, Uri) -> Unit)? = null) {
        this.avatarInitials = null
        onComplete?.invoke(this, toUri)
    }

    fun setAvatarImageOrInitials(imageUri: String?, initials: String?) {
        // Очищаем изображение, чтобы убедиться, что предыдущие изображения не остаются
        setImageDrawable(null)
        avatarInitials = null

        val image = if (imageUri?.trim().isNullOrEmpty() && initials?.trim().isNullOrEmpty()) {
            R.drawable.ic_anon_avatar
        } else {
            imageUri?.addBaseUrl()
        }

        if (image != null && image != "".addBaseUrl()) {
            Glide.with(context)
                .load(image)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Загрузка изображения не удалась, устанавливаем инициалы
                        avatarInitials = initials
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Изображение успешно загружено, обнуляем инициалы
                        avatarInitials = null
                        return false
                    }
                })
                .into(this)
        } else {
            // Нет изображения, устанавливаем инициалы
            avatarInitials = initials
        }

    }

    fun setDrawableImage(@DrawableRes drawable: Int) {
        Glide.with(context)
            .load(drawable)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }


    internal companion object {

        internal const val AVATAR_SIZE_EXTRA = 1

        private const val BORDER_OFFSET = 4F
    }

    override fun setThemeColor(theme: ColorsModel) {
        avatarInitialsBackgroundGradientColorStart = Color.parseColor(theme.mainBrandColor)
        avatarInitialsBackgroundGradientColorEnd = Color.parseColor(theme.secondaryBrandColor)
        avatarInitialsBackgroundColor = Color.parseColor(theme.mainBrandColor)
    }
}