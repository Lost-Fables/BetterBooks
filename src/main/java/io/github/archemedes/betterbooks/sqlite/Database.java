package io.github.archemedes.betterbooks.sqlite;

import co.lotc.core.bukkit.util.InventoryUtil;
import io.github.archemedes.betterbooks.BetterBooks;
import io.github.archemedes.betterbooks.BookShelf;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class Database {
	protected static BetterBooks plugin;
	public static Connection connection;

	private String dbname;
	private static final String SQLiteTableName = "shelves_table";
	private String SQLiteTokensTable;

	public Database(BetterBooks instance){
		plugin = instance;
		dbname = instance.getConfig().getString("SQLite.Filename", "shelves");
		SQLiteTokensTable = "CREATE TABLE IF NOT EXISTS " + SQLiteTableName + " (\n" +
							"    WORLD TEXT NOT NULL,\n" +
							"    X INT NOT NULL,\n" +
							"    Y INT NOT NULL,\n" +
							"    Z INT NOT NULL,\n" +
							"    INV TEXT NOT NULL,\n" +
							"    PRIMARY KEY (WORLD,X,Y,Z)\n" +
							");";
	}

	// SQL creation stuff, You can leave the below stuff untouched.
	public Connection getSQLConnection() {
		File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
		if (!dataFolder.exists()){
			try {
				dataFolder.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
			}
		}
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
			Class.forName("org.sqlite.JDBC");
			String locale = dataFolder.toString();
			if (BetterBooks.DEBUGGING) {
				plugin.getLogger().info("LOCALE: " + locale);
			}
			connection = DriverManager.getConnection("jdbc:sqlite:" + locale);
			return connection;
		} catch (SQLException ex) {
			if (BetterBooks.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
			}
		} catch (ClassNotFoundException ex) {
			if (BetterBooks.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
			}
		}
		return null;
	}

	public void load() {
		connection = getSQLConnection();
		try {
			Statement s = connection.createStatement();
			s.execute(SQLiteTokensTable);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
	}

	public void initialize(){
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQLiteTableName + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String world = rs.getString("WORLD");
				World w = Bukkit.getWorld(UUID.fromString(world));
				if (w != null) {
					int x = rs.getInt("X");
					int y = rs.getInt("Y");
					int z = rs.getInt("Z");

					Block b = w.getBlockAt(x, y, z);
					BookShelf s = BookShelf.getBookshelf(b);
					if (s != null) {
						s.loadFromString(rs.getString("INV"));
					}
				}
			}
			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public static void close(PreparedStatement ps,ResultSet rs){
		try {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			Errors.close(plugin, ex);
		}
	}

	public void addOrUpdate(BookShelf shelf) {
		Connection conn = null;
		PreparedStatement ps = null;
		String stmt;
		stmt = "INSERT OR REPLACE INTO " + SQLiteTableName + " (WORLD,X,Y,Z,INV) VALUES(?,?,?,?,?)";

		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement(stmt);

			Location loc = shelf.getLocation().clone();
			ps.setString(1, (loc.getWorld() != null) ? loc.getWorld().getName() : null);

			ps.setInt(2, loc.getBlockX());
			ps.setInt(3, loc.getBlockY());
			ps.setInt(4, loc.getBlockZ());

			ps.setString(5, InventoryUtil.serializeItems(shelf.getInventory()));
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}

	// Remove info
	public void deleteShelf(Location loc) {
		Connection conn = null;
		PreparedStatement ps = null;

		if (loc.getWorld() != null) {
			try {
				conn = getSQLConnection();
				String stmt;
				stmt = "DELETE FROM " + SQLiteTableName + " WHERE WORLD='" + loc.getWorld().getName() + "' AND X='" + loc.getBlockX() + "' AND Y='" + loc.getBlockY() + "' AND Z='" + loc.getBlockZ() + "';";
				ps = conn.prepareStatement(stmt);
				ps.executeUpdate();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (SQLException ex) {
					plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
				}
			}
		}
	}
}