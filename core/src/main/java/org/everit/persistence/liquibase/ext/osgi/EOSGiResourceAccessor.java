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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

import liquibase.osgi.OSGiResourceAccessor;

/**
 * The resource accessor that should be used in OSGi environments.
 */
public class EOSGiResourceAccessor extends OSGiResourceAccessor {

  private final Map<String, Object> attributes;

  /**
   * Creating a new resource accessor for the specified bundle without any attributes.
   *
   * @param bundle
   *          The bundle.
   */
  public EOSGiResourceAccessor(final Bundle bundle) {
    this(bundle, null);
  }

  /**
   * Creating a new {@link EOSGiResourceAccessor} for the specified bundle with the specified
   * attributes.
   *
   * @param bundle
   *          The bundle.
   * @param attributes
   *          See {@link #getAttributes()}.
   */
  public EOSGiResourceAccessor(final Bundle bundle, final Map<String, Object> attributes) {
    super(bundle);
    if (attributes == null) {
      this.attributes = Collections.emptyMap();
    } else {
      this.attributes = Collections.unmodifiableMap(new HashMap<String, Object>(attributes));
    }
  }

  /**
   * Attributes are normally coming from the liquibase.schema capability definition.
   *
   * @return The attributes of the resource accessor.
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

}
