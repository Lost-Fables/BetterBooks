package io.github.archemedes.betterbooks;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClearShelfTask extends ArcheTask {
    private static Connection connection = null;
    private static PreparedStatement ST_DEL = null;
    private static PreparedStatement ST_INS = null;
    private final ItemStack[] contents;
    private final Location location;

    ClearShelfTask(ItemStack[] contents, Location l) {
        this.contents = contents;
        this.location = l;
    }

    public static void close() {
        try {
            if (ST_DEL != null) ST_DEL.close();
            if (ST_INS != null) ST_INS.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        connection = ArcheCore.getControls().getSQLHandler().getConnection();
        try {
            synchronized (handle) {
                ST_DEL = connection.prepareStatement("DELETE FROM books WHERE WORLD = ? AND x = ? AND y = ? AND z = ?");
                ST_INS = connection.prepareStatement("INSERT INTO books VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        if (connection == null)
            init();
        try {
            ST_DEL.setString(1, this.location.getWorld().getName());
            ST_DEL.setInt(2, this.location.getBlockX());
            ST_DEL.setInt(3, this.location.getBlockY());
            ST_DEL.setInt(4, this.location.getBlockZ());

            synchronized (handle) {
                ST_DEL.execute();

                if ((this.contents != null) && (this.contents.length == 9)) {
                    ST_INS.setString(1, this.location.getWorld().getName());
                    ST_INS.setInt(2, this.location.getBlockX());
                    ST_INS.setInt(3, this.location.getBlockY());
                    ST_INS.setInt(4, this.location.getBlockZ());

                    for (int i = 0; i < 9; i++) {
                        ItemStack item = this.contents[i];
                        if (item != null) {
                            ST_INS.setInt(5, i);
                            SQLSerializer.serialize(ST_INS, item);
                            ST_INS.execute();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}