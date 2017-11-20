package io.github.archemedes.betterbooks.io;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import org.bukkit.Location;

public class DelBookRow extends SingleStatementRow {

    private final Location loc;

    public DelBookRow(Location loc) {
        this.loc = loc;
    }

    @Override
    protected String getStatement() {
        return "DELETE FROM books WHERE world=? AND x=? AND y=? AND z=?";
    }

    @Override
    protected Object getValueFor(int i) {
        switch (i) {
            case 1:
                return loc.getWorld().getUID().toString();
            case 2:
                return loc.getBlockX();
            case 3:
                return loc.getBlockY();
            case 4:
                return loc.getBlockZ();
            default:
                throw new IllegalArgumentException("There are only 4 arguments in this statement.");
        }
    }
}
