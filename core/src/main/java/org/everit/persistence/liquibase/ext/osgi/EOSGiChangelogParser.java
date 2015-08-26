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
package org.everit.persistence.liquibase.ext.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWire;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.osgi.OSGiResourceAccessor;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;

/**
 * Liquibase Parser implementation that can handle files with the eosgi extension. Such files are
 * looked up as bundle capabilities.
 */
public class EOSGiChangelogParser implements ChangeLogParser {

  public static final String EOSGI_EXTENSION = ".eosgi";

  @Override
  public int getPriority() {
    return ChangeLogParser.PRIORITY_DEFAULT;
  }

  @Override
  public DatabaseChangeLog parse(final String physicalChangeLogLocation,
      final ChangeLogParameters changeLogParameters, final ResourceAccessor resourceAccessor)
          throws ChangeLogParseException {

    String schemaExpression = physicalChangeLogLocation.substring(0,
        physicalChangeLogLocation.length() - EOSGI_EXTENSION.length());

    OSGiResourceAccessor osgiResourceAccessor = (OSGiResourceAccessor) resourceAccessor;
    Bundle currentBundle = osgiResourceAccessor.getBundle();

    BundleWire wire =
        LiquibaseOSGiUtil.findMatchingWireBySchemaExpression(currentBundle, schemaExpression);

    ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor).parse(fileName,
        changeLogParameters, resourceAccessor);
    return null;
  }

  @Override
  public boolean supports(final String changeLogFile, final ResourceAccessor resourceAccessor) {
    return (resourceAccessor instanceof OSGiResourceAccessor)
        && (changeLogFile.endsWith(EOSGI_EXTENSION));
  }

}
