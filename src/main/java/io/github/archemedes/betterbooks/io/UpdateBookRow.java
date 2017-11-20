package io.github.archemedes.betterbooks.io;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import org.bukkit.Location;

public class UpdateBookRow extends SingleStatementRow {

    private final Location loc;
    private final String inv;

    public UpdateBookRow(Location loc, String inv) {
        this.loc = loc;
        this.inv = inv;
    }

    @Override
    protected String getStatement() {
        return "UPDATE books SET inv=? WHERE world=? AND x=? AND y=? AND z=?";
    }

    @Override
    protected Object getValueFor(int i) {
        switch (i) {
            case 1:
                return inv;
            case 2:
                return loc.getWorld().getUID().toString();
            case 3:
                return loc.getBlockX();
            case 4:
                return loc.getBlockY();
            case 5:
                return loc.getBlockZ();
            default:
                throw new IllegalArgumentException("There are only 5 possible arguments for this statement");
        }
    }
}
