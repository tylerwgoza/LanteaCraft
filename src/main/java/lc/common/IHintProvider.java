package lc.common;

import lc.api.audio.ISoundController;
import lc.api.components.IConfigurationProvider;
import lc.api.defs.IContainerDefinition;
import lc.api.defs.IRecipeDefinition;
import lc.api.rendering.IParticleMachine;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

/**
 * Hint provider contract interface.
 *
 * @author AfterLifeLochie
 *
 */
public interface IHintProvider {

	/** Called during pre-initialization */
	public void preInit();

	/** Called during initialization */
	public void init();

	/** Called during post-initialization */
	public void postInit();

	/**
	 * Provide hints on a definition
	 *
	 * @param definition
	 *            A definition element
	 */
	public void provideHints(IContainerDefinition definition);

	/**
	 * Provide hints on a recipe
	 *
	 * @param definition
	 *            A recipe definition element
	 */
	public void provideHints(IRecipeDefinition definition);

	/**
	 * Called by the system when a server instance is starting
	 * 
	 * @param event
	 *            The start event
	 */
	public void serverStarting(FMLServerStartingEvent event);

	/**
	 * Called by the system when a server instance is stopping
	 * 
	 * @param event
	 *            The stop event
	 */
	public void serverStopping(FMLServerStoppingEvent event);

	/**
	 * Return the current rendering configuration node
	 * 
	 * @return The current rendering configuration node
	 */
	public IConfigurationProvider config();

	/**
	 * Return the current audio provider in use
	 * 
	 * @return The audio provider in use
	 */
	public ISoundController audio();

	public IParticleMachine particles();

	/**
	 * Called by the system when a signature violation is detected
	 * 
	 * @param event
	 *            The violation event
	 */
	public void signatureViolation(FMLFingerprintViolationEvent event);

	/**
	 * Called by the system when an IMC is received for this mod
	 * 
	 * @param event
	 *            The IMC event
	 */
	public void receiveIMC(IMCEvent event);

	/**
	 * Called by the system when a server instance is stopped
	 * 
	 * @param event
	 *            The stop event
	 */
	public void serverStopped(FMLServerStoppedEvent event);

	/**
	 * Called by the system when a server instance is started
	 * 
	 * @param event
	 *            The started event
	 */
	public void serverStarted(FMLServerStartedEvent event);

	/**
	 * Called by the system when a server instance is about to start
	 * 
	 * @param event
	 *            The start event
	 */
	public void beforeServerStarting(FMLServerAboutToStartEvent event);
}
