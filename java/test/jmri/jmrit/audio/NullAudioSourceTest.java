package jmri.jmrit.audio;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NullAudioSource
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class NullAudioSourceTest {

    @Test
    public void testCtor() {
        NullAudioSource l = new NullAudioSource("IAS1");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        NullAudioSource l = new NullAudioSource("IAS1","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.AudioManager am = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        jmri.InstanceManager.setDefault(jmri.AudioManager.class,am);
        am.init();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitAppender.suppressWarnMessage("Initialised Null audio system - no sounds will be available.");
        JUnitUtil.tearDown();
    }
}
