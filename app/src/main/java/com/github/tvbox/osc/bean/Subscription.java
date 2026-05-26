package com.github.tvbox.osc.bean;

import java.util.List;

public class Subscription {

    public Subscription() {}

    public Subscription(String name, String url) {
        this.name = name;
        this.url = url;
    }

    private String name;
    private String url;
    private boolean isChecked;
    private boolean top;
    private String multiUrl;
    private List<Line> lines;
    private int selectedIndex = -1;

    public boolean isTop() { return top; }
    public void setTop(boolean top) { this.top = top; }
    public boolean isChecked() { return isChecked; }
    public Subscription setChecked(boolean checked) { isChecked = checked; return this; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMultiUrl() { return multiUrl; }
    public void setMultiUrl(String multiUrl) { this.multiUrl = multiUrl; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
    public int getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(int selectedIndex) { this.selectedIndex = selectedIndex; }

    public Line getSelectedLine() {
        if (lines != null && selectedIndex >= 0 && selectedIndex < lines.size()) {
            return lines.get(selectedIndex);
        }
        return null;
    }

    public String getEffectiveUrl() {
        Line line = getSelectedLine();
        return line != null ? line.getUrl() : url;
    }

    public int getLineCount() {
        return lines != null ? lines.size() : 0;
    }

    public static class Line {
        private String name = "";
        private String url = "";

        public Line() {}
        public Line(String name, String url) {
            this.name = name != null ? name : "";
            this.url = url != null ? url : "";
        }
        public String getName() { return name != null ? name : ""; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url != null ? url : ""; }
        public void setUrl(String url) { this.url = url; }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        if (multiUrl != null && that.multiUrl != null) {
            return multiUrl.equals(that.multiUrl);
        }
        return url != null && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return multiUrl != null ? multiUrl.hashCode() : (url != null ? url.hashCode() : 0);
    }
}
