package io.github.archemedes.betterbooks;

import org.bukkit.Location;

import java.util.List;

class OpenBook {
    private final String title;
    private final String author;
    private final List<String> pages;
    private final Location location;
    private int pageMarker = -1;
    private int task = 0;

    OpenBook(String title, String author, List<String> pages, Location location) {
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.location = location;
    }

    String getTitle() {
        return this.title;
    }

    String getAuthor() {
        return this.author;
    }

    String readNext(boolean back) {
        this.pageMarker += (back ? -1 : 1);
        if (this.pageMarker < 0) this.pageMarker = (this.pages.size() - 1);
        if (this.pageMarker >= this.pages.size()) this.pageMarker = 0;

        return this.pages.get(this.pageMarker);
    }

    int getPage() {
        return this.pageMarker;
    }

    int getPages() {
        return this.pages.size();
    }

    int getTask() {
        return this.task;
    }

    void setTask(int task) {
        if (task > 0) this.task = task;
    }

    Location getLocation() {
        return this.location.clone();
    }
}