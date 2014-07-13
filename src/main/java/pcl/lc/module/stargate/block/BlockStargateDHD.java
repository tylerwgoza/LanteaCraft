package pcl.lc.module.stargate.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import pcl.lc.LanteaCraft;
import pcl.lc.base.RotationOrientedBlock;
import pcl.lc.module.ModuleStargates;
import pcl.lc.module.stargate.gui.ScreenStargateDHD;
import pcl.lc.module.stargate.tile.TileStargateDHD;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockStargateDHD extends RotationOrientedBlock {

	IIcon topTexture, bottomTexture, sideTexture;

	public BlockStargateDHD() {
		super(Material.rock);
		setHardness(1.5F);
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getTextureName() {
		return LanteaCraft.getAssetKey() + ":" + getUnlocalizedName() + "_" + LanteaCraft.getProxy().getRenderMode();
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		topTexture = register.registerIcon(LanteaCraft.getAssetKey() + ":" + "controller_top_"
				+ LanteaCraft.getProxy().getRenderMode());
		bottomTexture = register.registerIcon(LanteaCraft.getAssetKey() + ":" + "controller_bottom_"
				+ LanteaCraft.getProxy().getRenderMode());
		sideTexture = register.registerIcon(LanteaCraft.getAssetKey() + ":" + "controller_side_"
				+ LanteaCraft.getProxy().getRenderMode());
	}

	@Override
	public IIcon getIcon(int side, int data) {
		switch (side) {
		case 0:
			return bottomTexture;
		case 1:
			return topTexture;
		default:
			return sideTexture;
		}
	}

	@Override
	public int getRenderType() {
		if (ModuleStargates.Render.blockStargateBaseRenderer != null)
			return ModuleStargates.Render.blockControllerRenderer.renderID;
		return 0;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, player, stack);
		checkForLink(world, x, y, z);
		int dir = MathHelper.floor_double(player.rotationYaw * 4F / 360F + 0.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, dir, 0);
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return true;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float cx,
			float cy, float cz) {
		if (side != ForgeDirection.UP.ordinal())
			player.openGui(LanteaCraft.getInstance(), LanteaCraft.EnumGUIs.StargateDHDEnergy.ordinal(), world,
					x, y, z);
		else {
			TileStargateDHD tile = (TileStargateDHD) world.getTileEntity(x, y, z);
			Minecraft.getMinecraft().displayGuiScreen(new ScreenStargateDHD(tile, player));
		}
		return true;
	}

	public void checkForLink(World world, int x, int y, int z) {
		((TileStargateDHD) getTileEntity(world, x, y, z)).checkForLink();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileStargateDHD();
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		// TODO Auto-generated method stub

	}

}