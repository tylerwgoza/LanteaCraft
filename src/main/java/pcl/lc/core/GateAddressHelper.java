package pcl.lc.core;

import java.util.logging.Level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import pcl.lc.LanteaCraft;
import pcl.lc.tileentity.TileEntityStargateBase;
import pcl.lc.util.AddressingError;
import pcl.lc.util.AddressingError.CoordRangeError;
import pcl.lc.util.AddressingError.DimensionRangeError;

public class GateAddressHelper {

	private static GateAddressHelper singleton = new GateAddressHelper();

	public static GateAddressHelper singleton() {
		return singleton;
	}

	private static long maximumShortSize;
	private static long maximumLongSize;

	public static String addressForLocation(WorldLocation position) throws AddressingError {
		return addressForLocation(position.toChunkLocation());
	}

	public static String addressForLocation(ChunkLocation location) throws AddressingError {
		try {
			int dcx = Math.abs(location.cx), dcz = Math.abs(location.cz);
			int dd = Math.abs(location.dimension);

			if (dcx > maximumLongSize)
				throw new CoordRangeError("X-coordinate out of range");
			if (dcz > maximumLongSize)
				throw new CoordRangeError("Z-coordinate out of range");
			if (dd > maximumShortSize)
				throw new DimensionRangeError("Dimension out of range");

			StringBuilder output = new StringBuilder();
			output.append(singleton.btos(new boolean[] { // metadata
					(dcx != location.cx), // dcx != cx => neg dx
							(dcz != location.cz), // dcz != cz => neg dz
							(dd != location.dimension), // dd != d => neg dim
							false // unused
					}));
			output.append(singleton.itos(dcx, 3)).append(singleton.itos(dcz, 3));
			output.append(singleton.itos(dd, 2));
			singleton.legal(output.toString());
			return output.toString();
		} catch (NumberFormatException format) {
			throw new AddressingError("Unexpected exception.", format);
		}
	}

	public static ChunkLocation locationForAddress(String address) throws AddressingError {
		try {
			singleton.legal(address);
		} catch (NumberFormatException format) {
			throw new AddressingError("Illegal address character!");
		}

		try {
			boolean[] flags = singleton.stob(address.substring(0, 1));
			int dcx = singleton.stoi(address.substring(1, 4)), dcz = singleton.stoi(address.substring(4, 7));
			if (flags[0])
				dcx = -dcx;
			if (flags[1])
				dcz = -dcz;
			if (address.length() > 7) {
				int ddimension = singleton.stoi(address.substring(7));
				if (flags[2])
					ddimension = -ddimension;
				return new ChunkLocation(ddimension, dcx, dcz);
			} else
				return new ChunkLocation(dcx, dcz);
		} catch (NumberFormatException format) {
			throw new AddressingError("");
		}
	}

	public static World getWorld(int dimension) {
		MinecraftServer s = MinecraftServer.getServer();
		return s.worldServerForDimension(dimension);
	}

	public static TileEntityStargateBase findStargate(ChunkLocation hostLocation, String address)
			throws AddressingError {
		TileEntityStargateBase dte = null;
		ChunkLocation location = GateAddressHelper.locationForAddress(address);
		if (!location.isStrongLocation)
			if (hostLocation.isStrongLocation)
				location.setDimension(hostLocation.dimension);
			else
				throw new AddressingError("Cannot guess effective dimension; location and host location are both weak!");
		WorldLocation localize = location.toWorldLocation();
		World world = GateAddressHelper.getWorld(localize.dimension);
		if (world != null) {
			Chunk chunk = world.getChunkFromBlockCoords(localize.x, localize.z);
			if (chunk != null)
				for (Object o : chunk.chunkTileEntityMap.values())
					if (o instanceof TileEntityStargateBase) {
						dte = (TileEntityStargateBase) o;
						break;
					}
		}
		return dte;
	}

	public GateAddressHelper() {
		this.radixSize = radix.length;
		GateAddressHelper.maximumLongSize = stoi(build(radix[radixSize - 1], 3));
		GateAddressHelper.maximumShortSize = stoi(build(radix[radixSize - 1], 2));
	}

	/**
	 * Radix declaration; order sensitive.
	 */
	private final char[] radix = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-+").toCharArray();

	/**
	 * Radix size reference.
	 */
	public final int radixSize;

	/**
	 * Builds a string of specified length out of character c.
	 * 
	 * @param c
	 *            The character.
	 * @param l
	 *            The number of times to repeat the character.
	 * @return The string.
	 */
	private String build(char c, int l) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < l; i++)
			result.append(c);
		return result.toString();
	}

	/**
	 * test: (each char in s) => (char in radix)
	 */
	public void legal(String s) {
		for (char c : s.toCharArray())
			index(c);
	}

	/**
	 * test: (each char in s) => (char in radix); throws no unchecked
	 * exceptions.
	 */
	public boolean isLegal(String s) {
		try {
			legal(s);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	/**
	 * Test: char c => char in radix; throws no unchecked exceptions.
	 */
	public boolean isLegal(char c) {
		try {
			return (index(c) > -1);
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * index of c in radix; exception if not radix-char
	 */
	public int index(char c) {
		for (int i = 0; i < radix.length; i++)
			if (radix[i] == c)
				return i;
		throw new NumberFormatException("Illegal radix value.");
	}

	/**
	 * value of index i in radix; exception if out of radix bounds
	 */
	public char index(int i) {
		if (0 > i || i > radix.length)
			throw new NumberFormatException("Illegal radix value.");
		return radix[i];
	}

	/**
	 * boolean[4] to String(1)
	 */
	private String btos(boolean[] flags) {
		int accum = 0;
		if (flags.length != 4)
			throw new NumberFormatException("Illegal btos dimension.");
		for (int i = 0; i < 4; i++)
			if (flags[i])
				accum |= 1 << i;
		return itos(accum, 1);
	}

	/**
	 * String(1) to boolean[4]
	 */
	private boolean[] stob(String value) {
		boolean[] result = new boolean[4];
		int accum = stoi(value);
		for (int i = 0; i < 4; i++)
			if ((accum & (1 << i)) != 0)
				result[i] = true;
		return result;
	}

	/**
	 * int to String(width)
	 */
	private String itos(int value, int width) {
		final char[] buf = new char[width];
		while (width > 0) {
			buf[--width] = index(value % radix.length);
			value /= radix.length;
		}
		if (value != 0)
			throw new NumberFormatException("Number too large.");
		return new String(buf);
	}

	/**
	 * String(?) to int
	 */
	private int stoi(String value) {
		int result = 0, multmin, digit;
		int i = 0, len = value.length(), limit = -Integer.MAX_VALUE;
		if (len > 0) {
			multmin = limit / radix.length;
			while (i < len) {
				digit = index(value.charAt(i++));
				if (digit < 0)
					throw new NumberFormatException("Not a legal radix-38 symbol.");
				if (result < multmin)
					throw new NumberFormatException("Out of legal radix-multiplication range.");
				result *= radix.length;
				if (result < limit + digit)
					throw new NumberFormatException("Out of legal radix range.");
				result += digit;
			}
		} else
			throw new NumberFormatException("Not a legal radix-38 number.");
		return result;
	}

}
