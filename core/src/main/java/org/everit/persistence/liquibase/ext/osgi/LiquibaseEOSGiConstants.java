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

/**
 * Constants of Liquibase OSGi extension.
 */
public final class LiquibaseEOSGiConstants {

  /**
   * Capability attribute that points to the place of the changelog file within the bundle.
   */
  public static final String CAPABILITY_ATTR_RESOURCE = "resource";

  /**
   * The name of the capability that makes it possible to find liquibase changelogs. When an import
   * is used within a liquibase changelog file with the ".eosgi" extension, liquibase will browse
   * the wires of the bundle and looks for this capability to find the exact changelog file of the
   * inclusion.
   */
  public static final String CAPABILITY_NS_LIQUIBASE_CHANGELOG = "liquibase.changelog";

  /**
   * The extension that can be used within changelogs to include changelogs from other bundles via
   * capabilities.
   */
  public static final String INCLUDE_OSGI_EXTENSION = ".osgi";

  private LiquibaseEOSGiConstants() {
  }
}
