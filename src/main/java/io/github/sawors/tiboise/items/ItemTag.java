package io.github.sawors.tiboise.items;

import java.util.Locale;

public enum ItemTag {
    PREVENT_USE_IN_CRAFTING,
    INGREDIENT,
    HIDE_FROM_CLIENT,
    HIDE_GLINT,
    PREVENT_BREAKING,
    HAT,
    UNMOVABLE,
    PREVENT_PACKING,
    POST_SENDABLE
    ;
    
    
    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }
}
