/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.routines;

import io.harness.timescaledb.Public;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class TimeBucket12 extends AbstractRoutine<Long> {
  private static final long serialVersionUID = 1L;

  /**
   * The parameter <code>public.time_bucket.RETURN_VALUE</code>.
   */
  public static final Parameter<Long> RETURN_VALUE =
      Internal.createParameter("RETURN_VALUE", SQLDataType.BIGINT, false, false);

  /**
   * The parameter <code>public.time_bucket.bucket_width</code>.
   */
  public static final Parameter<Long> BUCKET_WIDTH =
      Internal.createParameter("bucket_width", SQLDataType.BIGINT, false, false);

  /**
   * The parameter <code>public.time_bucket.ts</code>.
   */
  public static final Parameter<Long> TS = Internal.createParameter("ts", SQLDataType.BIGINT, false, false);

  /**
   * The parameter <code>public.time_bucket.offset</code>.
   */
  public static final Parameter<Long> OFFSET = Internal.createParameter("offset", SQLDataType.BIGINT, false, false);

  /**
   * Create a new routine call instance
   */
  public TimeBucket12() {
    super("time_bucket", Public.PUBLIC, SQLDataType.BIGINT);

    setReturnParameter(RETURN_VALUE);
    addInParameter(BUCKET_WIDTH);
    addInParameter(TS);
    addInParameter(OFFSET);
    setOverloaded(true);
  }

  /**
   * Set the <code>bucket_width</code> parameter IN value to the routine
   */
  public void setBucketWidth(Long value) {
    setValue(BUCKET_WIDTH, value);
  }

  /**
   * Set the <code>bucket_width</code> parameter to the function to be used with a {@link org.jooq.Select} statement
   */
  public TimeBucket12 setBucketWidth(Field<Long> field) {
    setField(BUCKET_WIDTH, field);
    return this;
  }

  /**
   * Set the <code>ts</code> parameter IN value to the routine
   */
  public void setTs(Long value) {
    setValue(TS, value);
  }

  /**
   * Set the <code>ts</code> parameter to the function to be used with a {@link org.jooq.Select} statement
   */
  public TimeBucket12 setTs(Field<Long> field) {
    setField(TS, field);
    return this;
  }

  /**
   * Set the <code>offset</code> parameter IN value to the routine
   */
  public void setOffset(Long value) {
    setValue(OFFSET, value);
  }

  /**
   * Set the <code>offset</code> parameter to the function to be used with a {@link org.jooq.Select} statement
   */
  public TimeBucket12 setOffset(Field<Long> field) {
    setField(OFFSET, field);
    return this;
  }
}
