package org.singsurf.jns3.generic_protocol;

/**
 * A port on the upper side of a protocols handling byte arrays.
 */
public interface UpperPhysicalPort {
	/**
	 * Register a {@link LowerAdapter}
	 * @param adapter
	 */
	public void register(LowerPhysicalAdapter adapter);
}
