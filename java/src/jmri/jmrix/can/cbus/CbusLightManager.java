package jmri.jmrix.can.cbus;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Light;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAN CBUS implementation of a LightManager.
 * <p>
 * Lights must be manually created.
 *
 * @author Matthew Harris Copyright (C) 2015
 * @since 3.11.7
 */
public class CbusLightManager extends AbstractLightManager {

    /**
     * Ctor using a given system connection memo
     */
    public CbusLightManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    private CanSystemConnectionMemo memo;
    private String prefix = "M";

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * {@inheritDoc}
     * Override to normalize System Name
     */
    @Override
    @Nonnull
    public Light provideLight(@Nonnull String key) {
        String name = normalizeSystemName(key);
        Light t = getLight(name);
        if (t == null) {
            if (name.startsWith(prefix + typeLetter())) {
                return newLight(name, null);
            } else if (name.length() > 0) {
                return newLight(makeSystemName(name), null);
            } else {
                throw new IllegalArgumentException("\"" + name + "\" is invalid");
            }
        }
        return t;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return never null
     */
    @Override
    protected Light createNewLight(String systemName, String userName) {
        String addr = systemName.substring(prefix.length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        // validate (will add "+" to unsigned int)
        String newAddress = CbusAddress.validateSysName(addr);
        // OK, make
        Light l = new CbusLight(prefix, newAddress, memo.getTrafficController());
        l.setUserName(userName);
        return l;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /* createSystemName() and getNextValidAddress() are not used in code, or any other LightManager
    * to be removed. Tests for these in jmri.jmrix.can.cbus.CbusLightManagerTest also commented out.
    *
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        // first, check validity
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // prefix + as service to user
        String newAddress = CbusAddress.validateSysName(curAddress);
        return prefix + typeLetter() + newAddress;
    }

    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        String testAddr = curAddress;
        // make sure starting name is valid
        try {
            validateSystemNameFormat(testAddr);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Light s = getBySystemName(prefix + typeLetter() + testAddr);
        if (s == null) {
            return testAddr;
        }
        // build local addresses
        String newaddr = CbusAddress.getIncrement(testAddr);
        if (newaddr==null) {
            return null;
        }
        //If the new hardware address does not already exist then this can
        //be considered the next valid address.
        Light snew = getBySystemName(prefix + typeLetter() + newaddr);
        if (snew == null) {
            return newaddr;
        }
        return null;
    }
    */

    /**
     * {@inheritDoc} 
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        String addr;
        try {
            addr = systemName.substring(prefix.length() + 1); // get only the address part
        } catch (StringIndexOutOfBoundsException e){
            return NameValidity.INVALID;
        }
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation.
     * Logging of handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        String newAddress = CbusAddress.validateSysName(address);
        log.debug("validated system name {}", newAddress);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        String addr = systemName.substring(prefix.length() + 1);
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            log.debug("Warning: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Light getBySystemName(@Nonnull String key ) {
        String name = normalizeSystemName(key);
        return _tsys.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace, adding +/- if not present.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        String address = inputName.toUpperCase().trim();
        // check Cbus hardware address parts
        if ((!address.startsWith(prefix + typeLetter()) || (address.length() < prefix.length() + 2))) {
            return address;
        }
        try {
            log.debug("Normalize address = {}", address);
            address = CbusAddress.validateSysName(address.substring(prefix.length() + 1));
        } catch (IllegalArgumentException e){
            return address;
        } catch (StringIndexOutOfBoundsException e){
            return address;
        }
        return prefix + typeLetter() + address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusLightManager.class);

}
