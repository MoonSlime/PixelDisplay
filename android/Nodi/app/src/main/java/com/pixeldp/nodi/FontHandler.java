package com.pixeldp.nodi;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.List;

public class FontHandler {
    public enum Font {
        UI_REGULAR,
        UI_ITALIC,
        UI_BOLD_ITALIC,
        UI_BOLD,
        REGULAR,
        ITALIC,
        BOLDITALIC,
        BOLD
    }

    private static Typeface ui_regular;
    private static Typeface ui_italic;
    private static Typeface ui_bold_italic;
    private static Typeface ui_bold;
    private static Typeface regular;
    private static Typeface italic;
    private static Typeface bold_italic;
    private static Typeface bold;

    private static FontHandler instance;

    private FontHandler(Context context) {
        ui_regular = Typeface.createFromAsset(context.getAssets(), "NotoSansUI-Regular.ttf");
        ui_italic = Typeface.createFromAsset(context.getAssets(), "NotoSansUI-Italic.ttf");
        ui_bold_italic = Typeface.createFromAsset(context.getAssets(), "NotoSansUI-BoldItalic.ttf");
        ui_bold = Typeface.createFromAsset(context.getAssets(), "NotoSansUI-Bold.ttf");
        regular = Typeface.createFromAsset(context.getAssets(), "NotoSans-Regular.ttf");
        italic = Typeface.createFromAsset(context.getAssets(), "NotoSans-Italic.ttf");
        bold_italic = Typeface.createFromAsset(context.getAssets(), "NotoSans-BoldItalic.ttf");
        bold = Typeface.createFromAsset(context.getAssets(), "NotoSans-Bold.ttf");
    }

    public static FontHandler getInstance(Context context) {
        if (instance == null) {
            instance = new FontHandler(context);
        }

        return instance;
    }

    public void setFont(Font font, TextView... views) {
        Typeface selectedFont = findFont(font);

        for (TextView view : views) {
            view.setTypeface(selectedFont);
        }
    }

    public void setFont(Font font, List<TextView> views) {
        Typeface selectedFont = findFont(font);

        for (TextView view : views) {
            view.setTypeface(selectedFont);
        }
    }

    private static Typeface findFont(Font font) {
        switch(font) {
            case UI_REGULAR:
                return ui_regular;
            case UI_ITALIC:
                return ui_italic;
            case UI_BOLD_ITALIC:
                return ui_bold_italic;
            case UI_BOLD:
                return ui_bold;
            case REGULAR:
                return regular;
            case ITALIC:
                return italic;
            case BOLDITALIC:
                return bold_italic;
            case BOLD:
                return bold;
        }

        return null;
    }
}