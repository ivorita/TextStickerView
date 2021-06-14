package com.ifrit.zoomtextview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.ifrit.zoomtextview.R;
import com.ifrit.zoomtextview.utils.RectUtil;

/**
 * 文本贴图处理控件
 */
public class TextStickerView extends View {

    protected static final String TAG = TextStickerView.class.getSimpleName();

    public final int TEXT_SIZE_DEFAULT = getResources().getDimensionPixelSize(R.dimen.fontsize_default);
    //    public final int PADDING = getResources().getDimensionPixelSize(R.dimen.font_padding);
    public final int PADDING = 32;
    //    public final int STICKER_BTN_HALF_SIZE = getResources().getDimensionPixelSize(R.dimen.sticker_btn_half_size);
    public final int STICKER_BTN_HALF_SIZE = 30;
    private String mText = getResources().getString(R.string.input_hint); //给贴图文本赋个初始值
    private TextPaint mPaint = new TextPaint();
    private Paint mHelpPaint = new Paint();

    private Rect mTextRect = new Rect();// warp text rect record
    private RectF mHelpBoxRect = new RectF();
    private Rect mDeleteRect = new Rect();//删除按钮位置
    private Rect mRotateRect = new Rect();//旋转按钮位置

    private RectF mDeleteDstRect = new RectF();
    private RectF mRotateDstRect = new RectF();

    private Bitmap mDeleteBitmap;
    private Bitmap mRotateBitmap;

    private int mCurrentMode = IDLE_MODE;
    //控件的几种模式
    private static final int IDLE_MODE = 2;//正常
    private static final int MOVE_MODE = 3;//移动模式
    private static final int ROTATE_MODE = 4;//旋转模式
    private static final int DELETE_MODE = 5;//删除模式

    private EditText mEditText;//输入控件

    public int layout_x = 0;
    public int layout_y = 0;

    private float last_x = 0;
    private float last_y = 0;

    public float mRotateAngle = 0;
    public float mScale = 1;
    private boolean isInitLayout = true;

    private boolean isShowHelpBox = true;
    private float dx;
    private float dy;

    //系统时间
    private long currentTime;

    //设置字体
    private Typeface type;

    //设置画笔的透明度
    private int mAlpha = 255;

    public TextStickerView(Context context, Typeface type) {
        super(context);
        initView(context, type);
    }

    public TextStickerView(Context context, AttributeSet attrs, Typeface type) {
        super(context, attrs);
        initView(context, type);
    }

    public TextStickerView(Context context, AttributeSet attrs, int defStyleAttr, Typeface type) {
        super(context, attrs, defStyleAttr);
        initView(context, type);
    }

    public void setEditText(EditText textView) {
        this.mEditText = textView;

//        //设置特殊字体
//        mEditText.setTypeface(type);
    }

    private void initView(Context context, Typeface type) {

        // 获取资源 Bitmap
        mDeleteBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.text_delete);
        mRotateBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.text_edit);

        // 对 Bitmap 进行裁剪，裁剪出想要绘制的部分，以下裁剪结果为原图大小
        // If the source rectangle is not null, it specifies the subset of the bitmap to draw.
        // 传入到 Canvas.drawBitmp() 方法的第二个参数 Rect src 中，如果设置为原 Bitmap 大小，则直接传入 null 即可
        mDeleteRect.set(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mRotateRect.set(0, 0, mRotateBitmap.getWidth(), mRotateBitmap.getHeight());

        Log.e(TAG, mDeleteBitmap.getWidth() + " " + (STICKER_BTN_HALF_SIZE << 1));

        // Bitmap 绘制的位置，若 dstBitmap 比 srcBitmap 大，则等比拉伸，反之等比缩小
        mDeleteDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
        mRotateDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);

        mPaint.setColor(Color.RED);

        //设置字体
        if (null != type) {
            mPaint.setTypeface(type);
        }

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(TEXT_SIZE_DEFAULT);
        mPaint.setAntiAlias(true);

        //设置画笔的透明度
        mPaint.setAlpha(mAlpha);

        mHelpPaint.setColor(Color.RED);
        mHelpPaint.setStyle(Paint.Style.STROKE);
        mHelpPaint.setAntiAlias(true);
        mHelpPaint.setStrokeWidth(4);

    }

    /**
     * 设置框内文本
     *
     * @param text 框内文本
     */
    public void setText(String text) {
        this.mText = text;
        invalidate();
    }

    /**
     * 获取框内文本
     *
     * @return 框内文本
     */
    public String getmText() {
        return mText;
    }

    /**
     * 设置文本颜色
     *
     * @param newColor文本颜色
     */
    public void setTextColor(int newColor) {
        mPaint.setColor(newColor);
        invalidate();
    }

    /**
     * 获取字体样式
     *
     * @return 字体样式
     */
    public Typeface getType() {
        return type;
    }

    /**
     * 设置字体样式
     *
     * @param type 字体样式
     */
    public void setType(Typeface type) {
        this.type = type;
        mPaint.setTypeface(type);
        invalidate();
    }

    /**
     * 获取画笔的透明度
     *
     * @return 画笔的透明度
     */
    public int getmAlpha() {
        return mAlpha;
    }

    /**
     * 设置画笔透明度
     *
     * @param mAlpha 画笔透明度
     */
    public void setmAlpha(int mAlpha) {
        this.mAlpha = mAlpha;
        mPaint.setAlpha(mAlpha);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isInitLayout) {
            isInitLayout = false;
            resetView();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (TextUtils.isEmpty(mText))
            return;

        drawContent(canvas);
    }

    private void drawContent(Canvas canvas) {
        drawText(canvas);

        //draw x and rotate button
        int offsetValue = ((int) mDeleteDstRect.width()) >> 1;
        Log.e(TAG, "offsetValue: " + offsetValue);

        //将矩形偏移到新的 left 坐标和 top 坐标
        mDeleteDstRect.offsetTo(mHelpBoxRect.left - offsetValue, mHelpBoxRect.top - offsetValue);
        mRotateDstRect.offsetTo(mHelpBoxRect.right - offsetValue, mHelpBoxRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);

        if (!isShowHelpBox) {
            return;
        }

        canvas.save(); // 保存了旋转前的画布状态
        canvas.rotate(mRotateAngle, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.drawRoundRect(mHelpBoxRect, 10, 10, mHelpPaint);
        canvas.restore(); // 取出之前保存的画布状态，这样做是为了保证之后的绘制内容不受到旋转的影响

        canvas.drawBitmap(mDeleteBitmap, mDeleteRect, mDeleteDstRect, null);
        canvas.drawBitmap(mRotateBitmap, mRotateRect, mRotateDstRect, null);
    }

    private void drawText(Canvas canvas) {
        drawText(canvas, layout_x, layout_y, mScale, mRotateAngle);
    }

    public void drawText(Canvas canvas, int _x, int _y, float scale, float rotate) {
        //通过 setText(String text) 方法，每次更新文本显示
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        int x = _x;
        int y = _y;

        //判断一下是否有换行
        if (!mText.contains("\n")) {
            //如果没有换行

            //获取文字所占区域最小矩形，返回值存在 mTextRect
            mPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
            //返回的文本边界坐标 left, top, right, bottom
            Log.e(TAG, mTextRect.toShortString());

            //offset 通过添加 dx 和 dy 来偏移矩形
            mTextRect.offset(x - (mTextRect.width() >> 1), y);//右移一位相当于除以2

            //根据文本矩形参数设置外边框，与文本边距为 PADDING
            mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                    , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
            RectUtil.scaleRect(mHelpBoxRect, scale);

            canvas.save();
            canvas.scale(scale, scale, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
            canvas.rotate(rotate, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
            canvas.drawText(mText, x, y, mPaint);
            canvas.restore();

        } else {
            Log.e(TAG, mText.contains("\n") + "");
            //有换行
            String[] textSubs = mText.split("\n");
            //textSubs[]中字符串长度最长的
            int indexOfMax = 0;
            for (int i = 1; i < textSubs.length; i++) {
                if (textSubs[indexOfMax].length() < textSubs[i].length()) {
                    indexOfMax = i;
                }
            }
            //测量
            mPaint.getTextBounds(mText, 0, textSubs[indexOfMax].length(), mTextRect);

            Log.e(TAG, "mTextRect.toShortString(): " + mTextRect.toShortString());

            Log.e(TAG, "mTextRect.bottom: " + (TEXT_SIZE_DEFAULT + mTextRect.bottom - 10) * textSubs.length);

            //设置底部新一行的底部坐标
            mTextRect.bottom = (TEXT_SIZE_DEFAULT + mTextRect.bottom) * textSubs.length;

            //获取文字所占区域最小矩形
            Log.e(TAG, mTextRect.toShortString());

            //矩形的中心点
            mTextRect.offset(x - (mTextRect.width() >> 1), y);

            mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                    , mTextRect.right + PADDING, mTextRect.bottom);
            RectUtil.scaleRect(mHelpBoxRect, scale);

            canvas.save();
            canvas.scale(scale, scale, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
            canvas.rotate(rotate, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());

            //字符串所占的高度
            for (int i = 0; i < textSubs.length; i++) {
                //一行一行画
                canvas.drawText(textSubs[i], x, y + (i * (mTextRect.bottom - mTextRect.top) / (float) textSubs.length), mPaint);
            }
            canvas.restore();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 是否向下传递事件标志 true为消耗
        boolean ret = super.onTouchEvent(event);

        int action = event.getAction();
        float x = event.getX();// 获取自定义 View 内部点击事件点击坐标距离自定义 View 左边边界的距离
        float y = event.getY();// 获取自定义 View 内部点击事件点击坐标距离自定义 View 顶边边界的距离

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // RectF.contains(x,y): 如果 (x,y) 在矩形内，则返回 true
                // 即 left <= x < right 和 top <= y < bottom，左闭右开区间
                if (mDeleteDstRect.contains(x, y)) {// 删除模式
                    isShowHelpBox = true;
                    mCurrentMode = DELETE_MODE;
                } else if (mRotateDstRect.contains(x, y)) {// 旋转按钮
                    isShowHelpBox = true;
                    mCurrentMode = ROTATE_MODE;
                    last_x = mRotateDstRect.centerX();
                    last_y = mRotateDstRect.centerY();
                    ret = true;
                } else if (mHelpBoxRect.contains(x, y)) {// 移动模式或者编辑模式
                    isShowHelpBox = true;
                    mCurrentMode = MOVE_MODE;
                    last_x = x;
                    last_y = y;
                    ret = true;

                    currentTime = System.currentTimeMillis();

                } else {
                    isShowHelpBox = false;
                    invalidate();
                }// end if

                if (mCurrentMode == DELETE_MODE) {// 删除选定贴图
                    mCurrentMode = IDLE_MODE;// 返回空闲状态
                    clearTextContent();
                    invalidate();
                }// end if
                break;
            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (mCurrentMode == MOVE_MODE) {// 移动贴图
                    mCurrentMode = MOVE_MODE;
                    dx = x - last_x;
                    dy = y - last_y;

                    layout_x += dx;
                    layout_y += dy;

                    invalidate();

                    last_x = x;
                    last_y = y;
                } else if (mCurrentMode == ROTATE_MODE) {// 旋转 缩放文字操作
                    mCurrentMode = ROTATE_MODE;
                    dx = x - last_x;
                    dy = y - last_y;

                    updateRotateAndScale(dx, dy);

                    invalidate();
                    last_x = x;
                    last_y = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //判断是否单击编辑框
                long moveTime = System.currentTimeMillis() - currentTime;//移动时间

                //判断是否继续传递信号
                if (moveTime <= 200 && (dx <= 20 || dy <= 20)) {
                    //点击事件，自己消费
//                    Toast.makeText(getContext(), "aaaaa",Toast.LENGTH_SHORT).show();
//                    isShowHelpBox = false;
//                    invalidate();

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onEditClick(this);
                    }

                    return true;
                }
                ret = false;
                mCurrentMode = IDLE_MODE;
                break;
        }// end switch

        return ret;
    }

    public void clearTextContent() {
        if (mEditText != null) {
            mEditText.setText(null);
        }
        //setText(null);
    }

    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mHelpBoxRect.centerX();
        float c_y = mHelpBoxRect.centerY();

        float x = mRotateDstRect.centerX();
        float y = mRotateDstRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;// 计算缩放比

        mScale *= scale;
        float newWidth = mHelpBoxRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }

    public void resetView() {
        layout_x = getMeasuredWidth() / 2; //getMeasuredWidth() 此视图的原始测量宽度
        layout_y = getMeasuredHeight() / 2; //getMeasuredHeight() 此视图的原始测量高度
        mRotateAngle = 0;
        mScale = 1;
    }

    public boolean isShowHelpBox() {
        return isShowHelpBox;
    }

    public void setShowHelpBox(boolean showHelpBox) {
        isShowHelpBox = showHelpBox;
        invalidate();
    }

    public float getScale() {
        return mScale;
    }

    public float getRotateAngle() {
        return mRotateAngle;
    }

    /**
     * 向外部提供监听事件
     */
    private OnEditClickListener mOnItemClickListener;

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnEditClickListener {
        void onEditClick(View v);
    }
}//end class
