package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;

/**
 * Map of the game world, created from a textual representation.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	private static void error(String message, Object... args) {
		log("Error parsing map: %s", String.format(message, args));
	}

	public static final byte UNDEFINED = -1, SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	private static byte decode(char c) {
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
			return UNDEFINED;
		}
	}

	public static WorldMap from(String resourcePath) {
		WorldMap map = new WorldMap();
		try (BufferedReader rdr = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(resourcePath)))) {
			map.parse(rdr.lines());
			return map;
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private final ValueDefinitionParser parser = new ValueDefinitionParser();
	private final Map<String, Object> definitions = new HashMap<>();
	private byte[][] content;

	private void parse(Stream<String> lines) {
		List<String> dataLines = new ArrayList<>();
		lines.forEach(line -> {
			if (line.startsWith("!")) {
				// skip comment lines
			} else {
				// value definition?
				Map.Entry<String, ?> definition = parser.parse(line);
				if (definition != null) {
					definitions.put(definition.getKey(), definition.getValue());
				} else {
					dataLines.add(line);
				}
			}
		});
		V2i size = vector("size");
		if (dataLines.size() != size.y) {
			error("Specified map height %d does not match number of data lines %d", size.y, dataLines.size());
		}
		content = new byte[size.y][size.x]; // stored row-wise!
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				char c = dataLines.get(row).charAt(col);
				byte value = decode(c);
				if (value == UNDEFINED) {
					error("Found undefined map character at row %d, col %d: '%s'", row, col, c);
					content[row][col] = SPACE;
				} else {
					content[row][col] = value;
				}
			}
		}
	}

	public byte data(V2i tile) {
		return data(tile.x, tile.y);
	}

	public byte data(int x, int y) {
		return content[y][x]; // row-wise order!
	}

	public V2i vector(String valueName) {
		Object value = definitions.get(valueName);
		if (value == null) {
			error("Value '%s' is not defined", valueName);
			return V2i.NULL;
		}
		if (!(value instanceof V2i)) {
			error("Value '%s' does not contain a vector", valueName);
			return V2i.NULL;
		}
		return (V2i) value;
	}

	public Optional<V2i> vectorOpt(String valueName) {
		Object value = definitions.get(valueName);
		if (value == null) {
			return Optional.empty();
		}
		if (!(value instanceof V2i)) {
			error("Value '%s' does not contain a vector", valueName);
			return Optional.empty();
		}
		return Optional.of((V2i) value);
	}

	/**
	 * @param listName the list name (prefix before the dot in list variable assignments), e.g.
	 *                 <code>level</code> for list entries like <code>level.42</code>
	 * @return list of all values for given list name
	 */
	public List<V2i> vector_list(String listName) {
		return definitions.keySet().stream()//
				.filter(varName -> varName.startsWith(listName + "."))//
				.sorted()//
				.map(this::vector)//
				.collect(Collectors.toList());
	}
}