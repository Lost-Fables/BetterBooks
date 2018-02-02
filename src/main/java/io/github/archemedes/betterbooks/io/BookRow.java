package io.github.archemedes.betterbooks.io;

import io.github.archemedes.betterbooks.BookShelf;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class BookRow extends SingleStatementRow {

    private final BookShelf shelf;

    public BookRow(BookShelf shelf) {
        this.shelf = shelf;
    }

    @Override
    protected String getStatement() {
        return "INSERT INTO books VALUES (?,?,?,?,?)";
    }

    @Override
    protected Object getValueFor(int i) {
        switch (i) {
            case 1:
                return shelf.getLocation().getWorld().getUID().toString();
            case 2:
                return shelf.getLocation().getBlockX();
            case 3:
                return shelf.getLocation().getBlockY();
            case 4:
                return shelf.getLocation().getBlockZ();
            case 5:
                return shelf.saveToString();
            default:
                return null;
            //throw new IllegalArgumentException("Only 5 values are present for this statement and "+i+" was passed in.");
        }
    }
}
