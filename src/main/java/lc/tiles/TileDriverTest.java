package lc.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import lc.api.components.IntegrationType;
import lc.common.base.LCTile;
import lc.common.network.LCPacket;
import lc.api.drivers.DeviceDrivers;

@DeviceDrivers.DriverCandidate(types = { IntegrationType.POWER, IntegrationType.POWER })
public class TileDriverTest extends LCTile {

	@Override
	public void handlePacket(LCPacket packetOf, EntityPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public IInventory getInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void thinkClient() {
		// TODO Auto-generated method stub

	}

	@Override
	public void thinkServer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(NBTTagCompound compound) {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(NBTTagCompound compound) {
		// TODO Auto-generated method stub

	}

}
