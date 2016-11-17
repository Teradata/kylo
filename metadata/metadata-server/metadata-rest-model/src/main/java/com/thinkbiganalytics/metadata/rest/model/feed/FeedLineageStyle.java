package com.thinkbiganalytics.metadata.rest.model.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 11/16/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedLineageStyle {

    private String shape;
    private String color;
    private Integer size;

    private FontOptions font;
    private IconOptions icon;

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public FontOptions getFont() {
        return font;
    }

    public void setFont(FontOptions font) {
        this.font = font;
    }

    public IconOptions getIcon() {
        return icon;
    }

    public void setIcon(IconOptions icon) {
        this.icon = icon;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class IconOptions {

        private String code;
        private Integer size = 50;
        private String color = "#2B7CE9";

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class FontOptions {

        private String color = "#343434";
        private Integer size = 14;
        private String face = "arial";
        private String background = "none";
        private Integer strokeWidth = 0;
        private String strokeColor = "#ffffff";
        private String align = "center";

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getFace() {
            return face;
        }

        public void setFace(String face) {
            this.face = face;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public Integer getStrokeWidth() {
            return strokeWidth;
        }

        public void setStrokeWidth(Integer strokeWidth) {
            this.strokeWidth = strokeWidth;
        }

        public String getStrokeColor() {
            return strokeColor;
        }

        public void setStrokeColor(String strokeColor) {
            this.strokeColor = strokeColor;
        }

        public String getAlign() {
            return align;
        }

        public void setAlign(String align) {
            this.align = align;
        }
    }
}
