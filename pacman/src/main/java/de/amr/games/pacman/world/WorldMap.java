package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.amr.games.pacman.lib.V2i;

public class WorldMap {

	public static final byte SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	private byte[][] data;
	private String path;

	public WorldMap(String path) {
		this.path = path;
		load();
		log("");
	}

	public int sizeX() {
		return data[0].length;
	}

	public int sizeY() {
		return data.length;
	}

	public byte data(V2i tile) {
		return data(tile.x, tile.y);
	}

	public byte data(int x, int y) {
		return data[y][x];
	}

	private void load() {
		dimMap();
		int lineNumber = 0;
		int y = 0;
		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
			for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
				++lineNumber;
				if (line.startsWith("!") || line.isBlank()) {
					continue; // skip comments and blank lines
				}
				for (int x = 0; x < line.length(); ++x) {
					char c = line.charAt(x);
					try {
						data[y][x] = decode(c);
					} catch (Exception cause) {
						data[y][x] = SPACE;
						log("*** Error in map '%s': Illegal char '%c' at line %d, column %d", path, c, lineNumber, x + 1);
					}
				}
				++y;
			}
		} catch (IOException e) {
			throw new RuntimeException(String.format("Error reading map '%s'", path, e));
		}
	}

	private void dimMap() {
		int sizeX = -1;
		int y = 0;
		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
			for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
				if (line.startsWith("!") || line.isBlank()) {
					continue; // skip comments and blank lines
				}
				if (sizeX == -1) {
					sizeX = line.length();
				} else if (sizeX != line.length()) {
					throw new RuntimeException(String.format("Error reading map '%s', inconsistent line length", path));
				}
				++y;
			}
		} catch (IOException e) {
			throw new RuntimeException(String.format("Error reading map '%s'", path, e));
		}
		data = new byte[y][sizeX];
	}

	private byte decode(char c) {
		switch (c) {
		case ' ':
			return SPACE;
		case '#':
			return WALL;
		case 'T':
			return TUNNEL;
		case '-':
			return DOOR;
		case '.':
			return PILL;
		case '*':
			return ENERGIZER;
		default:
			throw new RuntimeException();
		}
	}
}