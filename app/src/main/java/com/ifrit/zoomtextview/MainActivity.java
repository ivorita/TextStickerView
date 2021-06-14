package com.ifrit.zoomtextview;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ifrit.zoomtextview.view.TextInputDialog;
import com.ifrit.zoomtextview.view.TextStickerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private Button btn2;
    private Button btn3;

    private Context mContext;

    private RelativeLayout mRlContainer;

    //文字集合
    private List<TextStickerView> mTextList = new ArrayList<>();

    //文本贴图输入框
    private EditText mInputText;

    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mRlContainer = findViewById(R.id.rl_container);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTextStickView();
            }
        });
    }

    private void createTextStickView() {
        TextStickerView mTextStickerView = new TextStickerView(this, null);
        mTextList.add(mTextStickerView);
        RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutparams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mTextStickerView.setLayoutParams(layoutparams);
        mRlContainer.addView(mTextStickerView);

        //从底部弹出文本输入编辑器
        showInputDialog(mTextStickerView);
    }

    /**
     * 贴图文本输入框dialog
     *
     * @param mTextStickerView
     */
    private void showInputDialog(final TextStickerView mTextStickerView) {
        final TextInputDialog dialog = new TextInputDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //获取dialog中的eittext
        mInputText = dialog.getEditInput();

        //文本贴图
        mTextStickerView.setEditText(mInputText);
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                //mTextStickerView change
                String text = editable.toString().trim();
                mTextStickerView.setText(text);

                //给一个特殊标识，防止textwatcher侦听不到
                String flagt = text + "$";

                //避免出现text为空或者删除掉该textstick点击空白处弹出输入框的尴尬
                //StringUtils.isEquals(flagt)说明text为空或者被删除
                if (!isEquals(flagt, "$")) {
                    mTextStickerView.setOnEditClickListener(new TextStickerView.OnEditClickListener() {
                        @Override
                        public void onEditClick(View v) {
                            //点中编辑框
                            showInputDialog(mTextStickerView);
                            mInputText.setText(mTextStickerView.getmText());
                        }
                    });
                } else {
                    mTextStickerView.setOnEditClickListener(new TextStickerView.OnEditClickListener() {
                        @Override
                        public void onEditClick(View v) {
                            return;
                        }
                    });
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });

        //如果用户未输入任何字符，则textwatch监听不到，防止点击无反应
        if (mTextStickerView.getmText().equals(getResources().getString(R.string.input_hint))) {
            mTextStickerView.setOnEditClickListener(new TextStickerView.OnEditClickListener() {
                @Override
                public void onEditClick(View v) {
                    //点中编辑框
                    showInputDialog(mTextStickerView);
                }
            });
        }

        dialog.show();

        Window window = dialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    /**
     * 判断多个字符串是否相等，如果其中有一个为空字符串或者null，则返回false，只有全相等才返回true
     */
    public static boolean isEquals(String... agrs) {
        String last = null;
        for (int i = 0; i < agrs.length; i++) {
            String str = agrs[i];
            if (str.equals("")) {
                return false;
            }
            if (last != null && !str.equalsIgnoreCase(last)) {
                return false;
            }
            last = str;
        }
        return true;
    }
}