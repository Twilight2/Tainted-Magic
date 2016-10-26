package taintedmagic.common.items.wand;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import taintedmagic.common.TaintedMagic;
import taintedmagic.common.entities.EntityEldritchOrbAttack;
import taintedmagic.common.helper.Vector3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXWisp;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityPrimalOrb;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXSonic;

public class ItemFocusTaintedBlast extends ItemFocusBasic
{
	private static final AspectList costBase = new AspectList().add(Aspect.ENTROPY, 500).add(Aspect.EARTH, 500).add(Aspect.WATER, 500);

	public static IIcon depthIcon;
	public static IIcon ornIcon;

	public ItemFocusTaintedBlast ()
	{
		this.setCreativeTab(TaintedMagic.tabTaintedMagic);
		this.setUnlocalizedName("ItemFocusTaintedBlast");
	}

	@SideOnly (Side.CLIENT)
	public void registerIcons (IIconRegister ir)
	{
		this.icon = ir.registerIcon("taintedmagic:ItemFocusTaintedBlast");
		this.depthIcon = ir.registerIcon("taintedmagic:ItemFocusTaint_depth");
		this.ornIcon = ir.registerIcon("taintedmagic:ItemFocusTaintedBlast_orn");
	}

	public IIcon getFocusDepthLayerIcon (ItemStack s)
	{
		return this.depthIcon;
	}

	public IIcon getOrnament (ItemStack s)
	{
		return this.ornIcon;
	}

	public String getSortingHelper (ItemStack s)
	{
		return "TB" + super.getSortingHelper(s);
	}

	public int getFocusColor (ItemStack s)
	{
		return 13107455;
	}

	public AspectList getVisCost (ItemStack s)
	{
		return costBase;
	}

	public int getActivationCooldown (ItemStack s)
	{
		return 15000;
	}

	public boolean isVisCostPerTick (ItemStack s)
	{
		return false;
	}

	public ItemFocusBasic.WandFocusAnimation getAnimation (ItemStack s)
	{
		return ItemFocusBasic.WandFocusAnimation.WAVE;
	}

	@Override
	public void addInformation (ItemStack s, EntityPlayer p, List l, boolean b)
	{
		super.addInformation(s, p, l, b);
		l.add(" ");
		l.add(EnumChatFormatting.BLUE + "+" + new String(this.isUpgradedWith(s, FocusUpgradeType.enlarge) ? Integer.toString(15 + this.getUpgradeLevel(s, FocusUpgradeType.enlarge)) : "15") + " " + StatCollector.translateToLocal("text.radius"));
	}

	public ItemStack onFocusRightClick (ItemStack s, World w, EntityPlayer p, MovingObjectPosition mop)
	{
		Iterator i$;
		ItemWandCasting wand = (ItemWandCasting) s.getItem();

		int potency = wand.getFocusPotency(s);
		if (wand.consumeAllVis(s, p, getVisCost(s), true, false))
		{
			List entities = p.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(p.posX, p.posY, p.posZ, p.posX + 1, p.posY + 1, p.posZ + 1).expand(15.0D + this.getUpgradeLevel(s, FocusUpgradeType.enlarge), 15.0D + this.getUpgradeLevel(s, FocusUpgradeType.enlarge), 15.0D + this.getUpgradeLevel(s, FocusUpgradeType.enlarge)));
			if ( (entities != null) && (entities.size() > 0)) for (i$ = entities.iterator(); i$.hasNext();)
			{
				Object ent = i$.next();
				Entity eo = (Entity) ent;

				if (eo != p)
				{
					if (eo.isEntityAlive() && !eo.isEntityInvulnerable())
					{
						double d = getDistanceTo(eo.posX, eo.posY, eo.posZ, p);
						if (d < 7.0D)
						{
							eo.attackEntityFrom(DamageSource.magic, 2.0F);
						}
					}
					Vector3 movement = getDistanceBetween(eo, p);
					eo.addVelocity(movement.x * 3, 0.8, movement.z * 3);
				}
			}
			w.playSoundAtEntity(p, "taintedmagic:shockwave", 5.0F, 1.0F * (float) Math.random());
			TaintedMagic.proxy.spawnShockwaveParticles(p.worldObj);
			return s;
		}
		return null;
	}

	public static Vector3 getDistanceBetween (Entity e, Entity e2)
	{
		Vector3 fromPosition = new Vector3(e.posX, e.posY, e.posZ);
		Vector3 toPosition = new Vector3(e2.posX, e2.posY, e2.posZ);
		Vector3 dist = fromPosition.sub(toPosition);
		dist.normalize();
		return dist;

	}

	public static double getDistanceTo (double x, double y, double z, EntityPlayer p)
	{
		double var7 = p.posX + 0.5D - x;
		double var9 = p.posY + 0.5D - y;
		double var11 = p.posZ + 0.5D - z;
		return var7 * var7 + var9 * var9 + var11 * var11;
	}

	public FocusUpgradeType[] getPossibleUpgradesByRank (ItemStack s, int rank)
	{
		switch (rank)
		{
		case 1 :
			return new FocusUpgradeType[]{
					FocusUpgradeType.frugal,
					FocusUpgradeType.enlarge };
		case 2 :
			return new FocusUpgradeType[]{
					FocusUpgradeType.frugal,
					FocusUpgradeType.enlarge };
		case 3 :
			return new FocusUpgradeType[]{
					FocusUpgradeType.frugal,
					FocusUpgradeType.enlarge };
		case 4 :
			return new FocusUpgradeType[]{
					FocusUpgradeType.frugal,
					FocusUpgradeType.enlarge };
		case 5 :
			return new FocusUpgradeType[]{
					FocusUpgradeType.frugal,
					FocusUpgradeType.enlarge };
		}
		return null;
	}
}
