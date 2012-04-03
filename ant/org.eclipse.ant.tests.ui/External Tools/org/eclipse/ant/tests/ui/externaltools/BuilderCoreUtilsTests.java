/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.externaltools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.internal.launching.AntLaunchingUtil;
import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.model.BuilderCoreUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * Tests for {@link BuilderCoreUtils}
 * 
 * @since 3.5.100 org.eclipse.ant.tests.ui
 */
public class BuilderCoreUtilsTests extends AbstractExternalToolTest {
	
	/**
	 * Constructor
	 */
	public BuilderCoreUtilsTests() {
		super("BuilderCoreUtils Tests");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//create the external tool builder dir
		BuilderCoreUtils.getBuilderFolder(getProject(), true);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map missing the {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute and all other config arguments
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs1() throws Exception {
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), new HashMap(), new String[] {BuilderCoreUtils.VERSION_1_0});
		assertNull("There should be no configuration returned without the config handle and arguments", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map missing the {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute only
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs2() throws Exception {
		Map args = get20AntArgumentMap();
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), args, new String[] {BuilderCoreUtils.VERSION_2_1});
		assertNotNull("There should be a migrated configuration returned", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map with an invalid {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs3() throws Exception {
		Map args = new HashMap();
		args.put(BuilderCoreUtils.LAUNCH_CONFIG_HANDLE, "foo");
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), args, new String[] {BuilderCoreUtils.VERSION_2_1});
		assertNull("There should be no configuration returned", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map with a valid {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute with no project prefix, but does
	 * include the .externalToolBuilders dir name - causes a lookup in the launch manager which fails because of the extra path prefix
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs4() throws Exception {
		createExternalToolBuilder(getProject(), "testConfigFromBuildCommandArgs4", null);
		Map args = new HashMap();
		args.put(BuilderCoreUtils.LAUNCH_CONFIG_HANDLE, "/.externalToolBuilders/testConfigFromBuildCommandArgs4.launch");
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), args, new String[] {BuilderCoreUtils.VERSION_2_1});
		assertNull("There should be no configuration returned", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map with a valid {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute with no project prefix - 
	 * causes a lookup in the launch manager returns the config
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs5() throws Exception {
		createExternalToolBuilder(getProject(), "testConfigFromBuildCommandArgs5", null);
		Map args = new HashMap();
		args.put(BuilderCoreUtils.LAUNCH_CONFIG_HANDLE, "testConfigFromBuildCommandArgs5.launch");
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), args, new String[] {BuilderCoreUtils.VERSION_2_1});
		assertNotNull("There should be a configuration returned", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map with a valid {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute with the project prefix but
	 * not including the .externalToolBuilder path segment
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs6() throws Exception {
		createExternalToolBuilder(getProject(), "testConfigFromBuildCommandArgs6", null);
		Map args = new HashMap();
		args.put(BuilderCoreUtils.LAUNCH_CONFIG_HANDLE, "<project>/testConfigFromBuildCommandArgs6.launch");
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), args, new String[] {BuilderCoreUtils.VERSION_2_1});
		assertNull("There should be no configuration returned", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configFromBuildCommandArgs(org.eclipse.core.resources.IProject, java.util.Map, String[])}
	 * method.
	 * <br><br>
	 * Tests the argument map with a valid {@link BuilderCoreUtils#LAUNCH_CONFIG_HANDLE} attribute with the project prefix and 
	 * a valid config path
	 * @throws Exception
	 */
	public void testConfigFromBuildCommandArgs7() throws Exception {
		createExternalToolBuilder(getProject(), "testConfigFromBuildCommandArgs7", null);
		Map args = new HashMap();
		args.put(BuilderCoreUtils.LAUNCH_CONFIG_HANDLE, "<project>/.externalToolBuilders/testConfigFromBuildCommandArgs7.launch");
		ILaunchConfiguration config = BuilderCoreUtils.configFromBuildCommandArgs(getProject(), args, new String[] {BuilderCoreUtils.VERSION_2_1});
		assertNotNull("There should be a configuration returned", config);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for a full build of the default target after a clean
	 * @throws Exception
	 */
	public void testConfigureTriggers1() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_AFTER_CLEAN_TARGETS, null);
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_FULL);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers1", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building FULL builds", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - null given for target names", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for a full build of a specific targets 'def' and 'clean'
	 * @throws Exception
	 */
	public void testConfigureTriggers2() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_AFTER_CLEAN_TARGETS, "def,clean");
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_FULL);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers2", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building FULL builds", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - only available during a build", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for an incremental AND full build with default targets
	 * <br><br>
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=114563
	 * @throws Exception
	 */
	public void testConfigureTriggers3() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_MANUAL_TARGETS, null);
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers3", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building INCREMENTAL builds", command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		assertTrue("the command must be building FULL builds", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - null given for target names", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for an incremental AND full build with the targets 'def' and 'inc'
	 * <br><br>
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=114563
	 * @throws Exception
	 */
	public void testConfigureTriggers4() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_MANUAL_TARGETS, "def,inc");
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers4", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building INCREMENTAL builds", command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		assertTrue("the command must be building FULL builds", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - only available during a build", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for an auto build
	 * @throws Exception
	 */
	public void testConfigureTriggers5() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_AUTO_TARGETS, null);
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_AUTO);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers5", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building AUTO builds", command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - null given for target names", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for an auto build with the targets 'def' and 'auto'
	 * @throws Exception
	 */
	public void testConfigureTriggers6() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_AUTO_TARGETS, "def,auto");
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_AUTO);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers6", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building AUTO builds", command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - only available during a build", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for a clean build
	 * @throws Exception
	 */
	public void testConfigureTriggers7() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_CLEAN_TARGETS, null);
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_CLEAN);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers7", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - null given for target names", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for a clean build with the targets 'def' and 'clean'
	 * @throws Exception
	 */
	public void testConfigureTriggers8() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_CLEAN_TARGETS, "def,clean");
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_CLEAN);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers6", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - only available during a build", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for a full + incremental build with the targets 'def' and 'inc' specified 
	 * for after clean targets and manual targets respectively
	 * @throws Exception
	 */
	public void testConfigureTriggers9() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_AFTER_CLEAN_TARGETS, "def");
		args.put(IAntLaunchConstants.ATTR_ANT_MANUAL_TARGETS, "inc");
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, IExternalToolConstants.BUILD_TYPE_CLEAN+","+IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers9", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - only available during a build", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#configureTriggers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests that the triggers are configured for a full + incremental build with the targets 'def' and 'inc' specified 
	 * for after clean targets and manual targets respectively
	 * @throws Exception
	 */
	public void testConfigureTriggers10() throws Exception {
		Map args = new HashMap();
		args.put(IAntLaunchConstants.ATTR_ANT_AFTER_CLEAN_TARGETS, "def");
		args.put(IAntLaunchConstants.ATTR_ANT_MANUAL_TARGETS, "inc");
		args.put(IAntLaunchConstants.ATTR_ANT_AUTO_TARGETS, "auto");
		args.put(IAntLaunchConstants.ATTR_ANT_CLEAN_TARGETS, "clean");
		args.put(IExternalToolConstants.ATTR_LOCATION, getBuildFile(EXT_BUILD_FILE_NAME).getAbsolutePath());
		String kinds = IExternalToolConstants.BUILD_TYPE_CLEAN+","+
				IExternalToolConstants.BUILD_TYPE_INCREMENTAL+","+
				IExternalToolConstants.BUILD_TYPE_AUTO+","+
				IExternalToolConstants.BUILD_TYPE_FULL;
		args.put(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, kinds);
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testConfigureTriggers10", args);
		assertNotNull("the test builder must not be null", config);
		ICommand command = createBuildCommand(config);
		assertNotNull("the test build command must not be null", command);
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		assertTrue("the command must be building CLEAN builds", command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));
		String[] names = AntLaunchingUtil.getTargetNames(config);
		assertNull("should be no target names resolved from the config - only available during a build", names);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#isUnmigratedConfig(org.eclipse.debug.core.ILaunchConfiguration)} method
	 * @throws Exception
	 */
	public void testIsUnmigratedConfig1() throws Exception {
		ILaunchConfigurationType type = AbstractAntUITest.getLaunchManager().getLaunchConfigurationType(IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE);
		if(type != null) {
			ILaunchConfigurationWorkingCopy config = type.newInstance(BuilderCoreUtils.getBuilderFolder(getProject(), true), "testIsUnmigratedConfig1");
			assertTrue("should be considered 'unmigrated'", BuilderCoreUtils.isUnmigratedConfig(config));
		}
		else {
			fail("could not find the Ant builder launch configuration type");
		}
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#isUnmigratedConfig(org.eclipse.debug.core.ILaunchConfiguration)} method
	 * @throws Exception
	 */
	public void testIsUnmigratedConfig2() throws Exception {
		ILaunchConfiguration config = createExternalToolBuilder(getProject(), "testIsUnmigratedConfig2", null);
		assertFalse("Shoudl not be considered 'unmigrated'", BuilderCoreUtils.isUnmigratedConfig(config));
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#toBuildCommand(org.eclipse.core.resources.IProject, org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests the case of a new un-saved {@link ILaunchConfiguration}
	 * @throws Exception
	 */
	public void testToBuildCommand1() throws Exception {
		ILaunchConfigurationWorkingCopy copy = createExternalToolBuilderWorkingCopy(getProject(), "testToBuildCommand1", null);
		ICommand command = BuilderCoreUtils.toBuildCommand(getProject(), copy, getProject().getDescription().newCommand());
		assertNotNull("There should have been a new build command created", command);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#toBuildCommand(org.eclipse.core.resources.IProject, org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests the case of an existing configuration
	 * @throws Exception
	 */
	public void testToBuildCommand2() throws Exception {
		Map args = new HashMap();
		ILaunchConfiguration copy = createExternalToolBuilder(getProject(), "testToBuildCommand2", args);
		ICommand command = BuilderCoreUtils.toBuildCommand(getProject(), copy, getProject().getDescription().newCommand());
		assertNotNull("There should have been a new build command created", command);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#toBuildCommand(org.eclipse.core.resources.IProject, org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.ICommand)}
	 * method
	 * <br><br>
	 * Tests the case of the working copy of an existing configuration
	 * @throws Exception
	 */
	public void testToBuildCommand3() throws Exception {
		Map args = new HashMap();
		ILaunchConfiguration copy = createExternalToolBuilder(getProject(), "testToBuildCommand3", args);
		ICommand command = BuilderCoreUtils.toBuildCommand(getProject(), copy.getWorkingCopy(), getProject().getDescription().newCommand());
		assertNotNull("There should have been a new build command created", command);
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#migrateBuilderConfiguration(org.eclipse.core.resources.IProject, org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)}
	 * method
	 * @throws Exception
	 */
	public void testMigrateBuilderConfiguration1() throws Exception {
		ILaunchConfigurationWorkingCopy copy = createExternalToolBuilderWorkingCopy(getProject(), "testMigrateBuilderConfiguration1", null);
		ILaunchConfiguration config = BuilderCoreUtils.migrateBuilderConfiguration(getProject(), copy);
		assertNotNull("The un-saved working copy should have been migrated", config);
		assertTrue("The name of the migrated configuration should be testMigrateBuilderConfiguration1", config.getName().equals("testMigrateBuilderConfiguration1"));
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#migrateBuilderConfiguration(org.eclipse.core.resources.IProject, org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)}
	 * method
	 * @throws Exception
	 */
	public void testMigrateBuilderConfiguration2() throws Exception {
		ILaunchConfigurationWorkingCopy copy = createExternalToolBuilderWorkingCopy(getProject(), "testMigra/teBuilderConfi/guration2", null);
		ILaunchConfiguration config = BuilderCoreUtils.migrateBuilderConfiguration(getProject(), copy);
		assertNotNull("The un-saved working copy should have been migrated", config);
		assertTrue("The name of the migrated configuration should be testMigra.teBuilderConfi.guration2", config.getName().equals("testMigra.teBuilderConfi.guration2"));
	}
	
	/**
	 * Tests the {@link BuilderCoreUtils#buildTypesToArray(String)} method
	 * @throws Exception
	 */
	public void testBuildTypesToArray1() throws Exception {
		String kinds = IExternalToolConstants.BUILD_TYPE_CLEAN+","+
				IExternalToolConstants.BUILD_TYPE_INCREMENTAL+","+
				IExternalToolConstants.BUILD_TYPE_AUTO+","+
				IExternalToolConstants.BUILD_TYPE_FULL;
		int[] array = BuilderCoreUtils.buildTypesToArray(kinds);
		assertNotNull("The build kinds array cannot be null", array);
		boolean contains = true;
		for (int i = 0; i < array.length; i++) {
			contains &= (array[i] == IncrementalProjectBuilder.AUTO_BUILD) |
					(array[i] == IncrementalProjectBuilder.CLEAN_BUILD) |
					(array[i] == IncrementalProjectBuilder.FULL_BUILD) |
					(array[i] == IncrementalProjectBuilder.INCREMENTAL_BUILD);
			if(!contains) {
				break;
			}
		}
		assertTrue("All of the build kinds should have been found", contains);
	}
}
