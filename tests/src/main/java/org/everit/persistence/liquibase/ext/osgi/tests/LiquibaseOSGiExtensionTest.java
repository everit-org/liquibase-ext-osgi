/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.persistence.liquibase.ext.osgi.tests;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;

import aQute.bnd.annotation.headers.ProvideCapability;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.osgi.OSGiResourceAccessor;

/**
 * Test for Liquibase OSGI Extension.
 */
@Component(configurationPolicy = ConfigurationPolicy.IGNORE)
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = "eosgi.testId", defaultValue = "LiquibaseOSGiExtension"),
    @StringAttribute(attributeId = "eosgi.testEngine", defaultValue = "junit4") })
@Service
public class LiquibaseOSGiExtensionTest {

  private BundleContext bundleContext;

  @Activate
  public void activate(final BundleContext bundleContext) {
    this.bundleContext = bundleContext;
  }

  private Bundle installBundleFromResource(final String resourceName) {
    BundleWiring bundleWiring = bundleContext.getBundle().adapt(BundleWiring.class);
    ClassLoader classLoader = bundleWiring.getClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
      return bundleContext.installBundle(resourceName, inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (BundleException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @TestDuringDevelopment
  public void testParserWithReferencedBundle() {
    Bundle testBundle = installBundleFromResource(
        "META-INF/jars/org.everit.persistence.liquibase.ext.osgi.test1.jar");
    try {
      testBundle.start();
    } catch (BundleException e) {
      try {
        testBundle.uninstall();
      } catch (BundleException e1) {
        e.addSuppressed(e1);
      }
      throw new RuntimeException(e);
    }

    JdbcDataSource h2DataSource = new JdbcDataSource();
    h2DataSource.setURL("jdbc:h2:mem:");

    try (Connection connection = h2DataSource.getConnection()) {
      JdbcConnection jdbcConnection = new JdbcConnection(connection);
      Liquibase liquibase = new Liquibase(
          "META-INF/liquibase/org.everit.persistence.liquibase.ext.osgi.test1.xml",
          new OSGiResourceAccessor(testBundle), jdbcConnection);
      liquibase.update((Contexts) null);

      try (CallableStatement statement =
          connection.prepareCall("select * from \"test0_included\"")) {
        try (ResultSet resultSet = statement.executeQuery()) {
          Assert.assertNotNull(resultSet);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (LiquibaseException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        testBundle.uninstall();
      } catch (BundleException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
