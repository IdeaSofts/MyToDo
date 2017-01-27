package com.idea.todo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EllipsizeTextView extends TextView {
    private static final String ELLIPSIS = "...";
    private  List<EllipsizeListener> mEllipsizeListeners;
    private String mFullText;
    private boolean mIsEllipsized;
    private boolean mIsStale;
    private float mLineAdditionalVerticalPadding;
    private float mLineSpacingMultiplier;
    private int mMaxLines;
    private boolean mProgrammaticChange;

    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean z);
    }

    public EllipsizeTextView(Context context) {
        super(context);
        init();
    }

    public EllipsizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EllipsizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.mEllipsizeListeners = new ArrayList<>();
        this.mMaxLines = -1;
        this.mLineSpacingMultiplier = 1.0f;
        this.mLineAdditionalVerticalPadding = 0.0f;
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        this.mEllipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        this.mEllipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return this.mIsEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.mMaxLines = maxLines;
        this.mIsStale = true;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.mLineAdditionalVerticalPadding = add;
        this.mLineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        if (!this.mProgrammaticChange) {
            this.mFullText = text.toString().trim();
            this.mIsStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (this.mIsStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        int maxLines = this.mMaxLines;
        String workingText = this.mFullText;
        boolean ellipsized = false;
        if (maxLines != -1) {
            Layout layout = createWorkingLayout(workingText);
            if (layout.getLineCount() > maxLines) {
                workingText = this.mFullText.substring(0, layout.getLineEnd(maxLines - 1)).trim();
                while (createWorkingLayout(new StringBuilder(String.valueOf(workingText)).append(ELLIPSIS).toString()).getLineCount() > maxLines) {
                    int lastSpace = workingText.lastIndexOf(32);
                    if (lastSpace == -1) {
                        break;
                    }
                    workingText = workingText.substring(0, lastSpace);
                }
                workingText = new StringBuilder(String.valueOf(workingText)).append(ELLIPSIS).toString();
                ellipsized = true;
            }
        }
        if (!workingText.equals(getText())) {
            this.mProgrammaticChange = true;
            try {
                setText(workingText);
            } finally {
                this.mProgrammaticChange = false;
            }
        }
        this.mIsStale = false;
        if (ellipsized != this.mIsEllipsized) {
            this.mIsEllipsized = ellipsized;
            for (EllipsizeListener listener : this.mEllipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }
        }
    }

    private Layout createWorkingLayout(String workingText) {
        return new StaticLayout(workingText, getPaint(), (getWidth() - getPaddingLeft()) - getPaddingRight(), Alignment.ALIGN_NORMAL, this.mLineSpacingMultiplier, this.mLineAdditionalVerticalPadding, false);
    }


}
