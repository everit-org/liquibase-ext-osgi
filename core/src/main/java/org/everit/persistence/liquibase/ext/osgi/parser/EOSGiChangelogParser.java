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
package org.everit.persistence.liquibase.ext.osgi.parser;

import org.everit.persistence.liquibase.ext.osgi.EOSGiResourceAccessor;
import org.everit.persistence.liquibase.ext.osgi.LiquibaseEOSGiConstants;
import org.everit.persistence.liquibase.ext.osgi.util.BundleResource;
import org.everit.persistence.liquibase.ext.osgi.util.LiquibaseOSGiUtil;
import org.osgi.framework.Bundle;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.osgi.OSGiResourceAccessor;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.PrioritizedService;

/**
 * Liquibase Parser implementation that can handle files with the eosgi extension. Such files are
 * looked up as bundle capabilities.
 */
public class EOSGiChangelogParser implements ChangeLogParser {

  @Override
  public int getPriority() {
    return PrioritizedService.PRIORITY_DEFAULT;
  }

  @Override
  public DatabaseChangeLog parse(final String physicalChangeLogLocation,
      final ChangeLogParameters changeLogParameters, final ResourceAccessor resourceAccessor)
      throws ChangeLogParseException {

    String schemaExpression = physicalChangeLogLocation.substring(0,
        physicalChangeLogLocation.length()
            - LiquibaseEOSGiConstants.INCLUDE_OSGI_EXTENSION.length());

    if (!(resourceAccessor instanceof OSGiResourceAccessor)) {
      throw new IllegalArgumentException(
          "type of resourceAccessor must be " + OSGiResourceAccessor.class.getName());
    }
    OSGiResourceAccessor osgiResourceAccessor = (OSGiResourceAccessor) resourceAccessor;
    Bundle currentBundle = osgiResourceAccessor.getBundle();

    BundleResource bundleResource =
        LiquibaseOSGiUtil.findMatchingWireBySchemaExpression(currentBundle, schemaExpression);

    if (bundleResource == null) {
      throw new ChangeLogParseException("Could not find resource starting from bundle '"
          + currentBundle + "' with schema expression '" + schemaExpression + "'");
    }

    OSGiResourceAccessor newOSGiResourceAccessor = osgiResourceAccessor;
    if (!currentBundle.equals(bundleResource.bundle)) {
      newOSGiResourceAccessor =
          new EOSGiResourceAccessor(bundleResource.bundle, bundleResource.attributes);
    }

    try {
      return ChangeLogParserFactory.getInstance()
          .getParser(bundleResource.resourceName, newOSGiResourceAccessor)
          .parse(bundleResource.resourceName, changeLogParameters, newOSGiResourceAccessor);
    } catch (LiquibaseException e) {
      throw new ChangeLogParseException(e);
    }
  }

  @Override
  public boolean supports(final String changeLogFile, final ResourceAccessor resourceAccessor) {
    return (resourceAccessor instanceof OSGiResourceAccessor)
        && (changeLogFile.endsWith(LiquibaseEOSGiConstants.INCLUDE_OSGI_EXTENSION));
  }

}
