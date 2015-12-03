/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2015-11-16 19:10:01 UTC)
 * on 2015-12-02 at 12:46:24 UTC 
 * Modify at your own risk.
 */

package com.koleshop.api.commonEndpoint.model;

/**
 * Model definition for ProductVarietyAttributeMeasuringUnit.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the commonEndpoint. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class ProductVarietyAttributeMeasuringUnit extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean baseUnit;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Float conversionRate;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean isBaseUnit;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String unit;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String unitFullName;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String unitType;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getBaseUnit() {
    return baseUnit;
  }

  /**
   * @param baseUnit baseUnit or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setBaseUnit(java.lang.Boolean baseUnit) {
    this.baseUnit = baseUnit;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Float getConversionRate() {
    return conversionRate;
  }

  /**
   * @param conversionRate conversionRate or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setConversionRate(java.lang.Float conversionRate) {
    this.conversionRate = conversionRate;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setId(java.lang.Integer id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getIsBaseUnit() {
    return isBaseUnit;
  }

  /**
   * @param isBaseUnit isBaseUnit or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setIsBaseUnit(java.lang.Boolean isBaseUnit) {
    this.isBaseUnit = isBaseUnit;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUnit() {
    return unit;
  }

  /**
   * @param unit unit or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setUnit(java.lang.String unit) {
    this.unit = unit;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUnitFullName() {
    return unitFullName;
  }

  /**
   * @param unitFullName unitFullName or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setUnitFullName(java.lang.String unitFullName) {
    this.unitFullName = unitFullName;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUnitType() {
    return unitType;
  }

  /**
   * @param unitType unitType or {@code null} for none
   */
  public ProductVarietyAttributeMeasuringUnit setUnitType(java.lang.String unitType) {
    this.unitType = unitType;
    return this;
  }

  @Override
  public ProductVarietyAttributeMeasuringUnit set(String fieldName, Object value) {
    return (ProductVarietyAttributeMeasuringUnit) super.set(fieldName, value);
  }

  @Override
  public ProductVarietyAttributeMeasuringUnit clone() {
    return (ProductVarietyAttributeMeasuringUnit) super.clone();
  }

}
