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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.felix.utils.manifest.Attribute;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Directive;
import org.apache.felix.utils.manifest.Parser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

public final class LiquibaseOSGiUtil {

  /**
   * The name attribute in the liquibase.schema capability. The name should be specified in the
   * include tag of the changelog XML.
   */
  public static final String ATTR_SCHEMA_NAME = "name";

  /**
   * Capability attribute that points to the place of the changelog file within the bundle.
   */
  public static final String ATTR_SCHEMA_RESOURCE = "resource";

  /**
   * The name of the capability that makes it possible to find liquibase changelogs. When an import
   * is used within a liquibase changelog file with the "eosgi:" prefix, liquibase will browse the
   * wires of the bundle and looks for this capability to find the exact changelog file of the
   * inclusion.
   */
  public static final String LIQUIBASE_CAPABILITY_NS = "liquibase.schema";

  public static Filter createFilterForLiquibaseCapabilityAttributes(final String schemaExpression) {
    Clause[] clauses = Parser.parseClauses(new String[] { schemaExpression });
    if (clauses.length != 1) {
      throw new SchemaExpressionSyntaxException(
          "The number of Clauses in the Schema expression should be 1");
    }
    Clause clause = clauses[0];
    String schemaName = clause.getName();
    Attribute[] attributes = clause.getAttributes();
    if (attributes.length > 0) {
      throw new SchemaExpressionSyntaxException(
          "No Attributes in the schema expresson are supported.");
    }
    Directive[] directives = clause.getDirectives();
    String filterString = "(" + ATTR_SCHEMA_NAME + "=" + schemaName + ")";
    if (directives.length == 1) {
      if (!Constants.FILTER_DIRECTIVE.equals(directives[0].getName())) {
        throw new SchemaExpressionSyntaxException(
            "Only the 'filter' directive is supported in the schema expression");
      }
      String additionalFilterString = directives[0].getValue();
      filterString = "(&" + filterString + additionalFilterString + ")";

    }
    try {
      return FrameworkUtil.createFilter(filterString);
    } catch (InvalidSyntaxException e) {
      throw new SchemaExpressionSyntaxException("The filter contains an invalid filter string");
    }
  }

  public static List<BundleResource> findBundleResourcesBySchemaExpression(final Bundle bundle,
      final String schemaExpression) {
    // TODO
    return Collections.emptyList();
  }

  public static Map<Bundle, List<BundleCapability>> findBundlesBySchemaExpression(
      final String schemaExpression,
      final BundleContext bundleContext, final int necessaryBundleStates) {
    Filter filter =
        LiquibaseOSGiUtil.createFilterForLiquibaseCapabilityAttributes(schemaExpression);
    Map<Bundle, List<BundleCapability>> result =
        new TreeMap<Bundle, List<BundleCapability>>(new Comparator<Bundle>() {

          @Override
          public int compare(final Bundle o1, final Bundle o2) {
            long bundle1Id = o1.getBundleId();
            long bundle2Id = o2.getBundleId();
            if (bundle1Id == bundle2Id) {
              return 0;
            } else if (bundle1Id < bundle2Id) {
              return -1;
            } else {
              return 1;
            }
          }
        });
    Bundle[] bundles = bundleContext.getBundles();
    for (Bundle bundle : bundles) {
      int state = bundle.getState();
      if ((state & necessaryBundleStates) != 0) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        List<BundleCapability> capabilities = bundleWiring.getCapabilities(LIQUIBASE_CAPABILITY_NS);
        for (BundleCapability capability : capabilities) {
          Map<String, Object> attributes = capability.getAttributes();
          if (attributes.get(LiquibaseOSGiUtil.ATTR_SCHEMA_RESOURCE) != null) {
            if (filter.matches(attributes)) {
              List<BundleCapability> capabilityList = result.get(bundle);
              if (capabilityList == null) {
                capabilityList = new ArrayList<BundleCapability>();
                result.put(bundle, capabilityList);
              }
              capabilityList.add(capability);
            }
          } else {
            // TODO log
          }
        }
      }
    }
    return result;
  }

  public static final BundleWire findMatchingWireBySchemaExpression(final Bundle currentBundle,
      final String schemaExpression) {

    BundleWiring bundleWiring = currentBundle.adapt(BundleWiring.class);
    List<BundleWire> wires = bundleWiring.getRequiredWires(LIQUIBASE_CAPABILITY_NS);

    if (wires.size() == 0) {
      return null;
    }

    Filter capabilityFilter =
        LiquibaseOSGiUtil.createFilterForLiquibaseCapabilityAttributes(schemaExpression);

    Iterator<BundleWire> iterator = wires.iterator();
    BundleWire matchingWire = null;
    // Iterate through the wires to find the one that matches the schema expression
    while ((matchingWire == null) && iterator.hasNext()) {
      BundleWire wire = iterator.next();
      BundleCapability capability = wire.getCapability();
      Map<String, Object> capabilityAttributes = capability.getAttributes();
      if (capabilityFilter.matches(capabilityAttributes)) {
        Object schemaResourceAttr = capabilityAttributes.get(ATTR_SCHEMA_RESOURCE);
        if (schemaResourceAttr != null) {
          matchingWire = wire;
        } else {
          // TODO Write WARNING
        }
      }
    }
    return matchingWire;
  }

  private LiquibaseOSGiUtil() {
  }
}
