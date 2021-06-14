文本贴图处理控件：可以拖动，旋转，缩放，删除，选择文本颜色，支持多行文本编辑
# 使用
```java
TextStickerView mTextStickerView = new TextStickerView(this, null);
mTextList.add(mTextStickerView);
RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
layoutparams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
mTextStickerView.setLayoutParams(layoutparams);
mRlContainer.addView(mTextStickerView);
```
