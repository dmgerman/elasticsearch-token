begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.unit
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|Writeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * The DistanceUnit enumerates several units for measuring distances. These units   * provide methods for converting strings and methods to convert units among each  * others. Some methods like {@link DistanceUnit#getEarthCircumference} refer to  * the earth ellipsoid defined in {@link GeoUtils}. The default unit used within  * this project is<code>METERS</code> which is defined by<code>DEFAULT</code>  */
end_comment

begin_enum
DECL|enum|DistanceUnit
specifier|public
enum|enum
name|DistanceUnit
implements|implements
name|Writeable
argument_list|<
name|DistanceUnit
argument_list|>
block|{
DECL|enum constant|INCH
name|INCH
argument_list|(
literal|0.0254
argument_list|,
literal|"in"
argument_list|,
literal|"inch"
argument_list|)
block|,
DECL|enum constant|YARD
name|YARD
argument_list|(
literal|0.9144
argument_list|,
literal|"yd"
argument_list|,
literal|"yards"
argument_list|)
block|,
DECL|enum constant|FEET
name|FEET
argument_list|(
literal|0.3048
argument_list|,
literal|"ft"
argument_list|,
literal|"feet"
argument_list|)
block|,
DECL|enum constant|KILOMETERS
name|KILOMETERS
argument_list|(
literal|1000.0
argument_list|,
literal|"km"
argument_list|,
literal|"kilometers"
argument_list|)
block|,
DECL|enum constant|NAUTICALMILES
name|NAUTICALMILES
argument_list|(
literal|1852.0
argument_list|,
literal|"NM"
argument_list|,
literal|"nmi"
argument_list|,
literal|"nauticalmiles"
argument_list|)
block|,
DECL|enum constant|MILLIMETERS
name|MILLIMETERS
argument_list|(
literal|0.001
argument_list|,
literal|"mm"
argument_list|,
literal|"millimeters"
argument_list|)
block|,
DECL|enum constant|CENTIMETERS
name|CENTIMETERS
argument_list|(
literal|0.01
argument_list|,
literal|"cm"
argument_list|,
literal|"centimeters"
argument_list|)
block|,
comment|// 'm' is a suffix of 'nmi' so it must follow 'nmi'
DECL|enum constant|MILES
name|MILES
argument_list|(
literal|1609.344
argument_list|,
literal|"mi"
argument_list|,
literal|"miles"
argument_list|)
block|,
comment|// since 'm' is suffix of other unit
comment|// it must be the last entry of unit
comment|// names ending with 'm'. otherwise
comment|// parsing would fail
DECL|enum constant|METERS
name|METERS
argument_list|(
literal|1
argument_list|,
literal|"m"
argument_list|,
literal|"meters"
argument_list|)
block|;
DECL|field|DEFAULT
specifier|public
specifier|static
specifier|final
name|DistanceUnit
name|DEFAULT
init|=
name|METERS
decl_stmt|;
DECL|field|meters
specifier|private
name|double
name|meters
decl_stmt|;
DECL|field|names
specifier|private
specifier|final
name|String
index|[]
name|names
decl_stmt|;
DECL|method|DistanceUnit
name|DistanceUnit
parameter_list|(
name|double
name|meters
parameter_list|,
name|String
modifier|...
name|names
parameter_list|)
block|{
name|this
operator|.
name|meters
operator|=
name|meters
expr_stmt|;
name|this
operator|.
name|names
operator|=
name|names
expr_stmt|;
block|}
comment|/**      * Measures the circumference of earth in this unit      *       * @return length of earth circumference in this unit      */
DECL|method|getEarthCircumference
specifier|public
name|double
name|getEarthCircumference
parameter_list|()
block|{
return|return
name|GeoUtils
operator|.
name|EARTH_EQUATOR
operator|/
name|meters
return|;
block|}
comment|/**      * Measures the radius of earth in this unit      *       * @return length of earth radius in this unit      */
DECL|method|getEarthRadius
specifier|public
name|double
name|getEarthRadius
parameter_list|()
block|{
return|return
name|GeoUtils
operator|.
name|EARTH_SEMI_MAJOR_AXIS
operator|/
name|meters
return|;
block|}
comment|/**      * Measures a longitude in this unit      *       * @return length of a longitude degree in this unit      */
DECL|method|getDistancePerDegree
specifier|public
name|double
name|getDistancePerDegree
parameter_list|()
block|{
return|return
name|GeoUtils
operator|.
name|EARTH_EQUATOR
operator|/
operator|(
literal|360.0
operator|*
name|meters
operator|)
return|;
block|}
comment|/**      * Convert a value into meters      *       * @param distance distance in this unit      * @return value in meters      */
DECL|method|toMeters
specifier|public
name|double
name|toMeters
parameter_list|(
name|double
name|distance
parameter_list|)
block|{
return|return
name|convert
argument_list|(
name|distance
argument_list|,
name|this
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
return|;
block|}
comment|/**      * Convert a value given in meters to a value of this unit      *       * @param distance distance in meters      * @return value in this unit      */
DECL|method|fromMeters
specifier|public
name|double
name|fromMeters
parameter_list|(
name|double
name|distance
parameter_list|)
block|{
return|return
name|convert
argument_list|(
name|distance
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|,
name|this
argument_list|)
return|;
block|}
comment|/**       * Convert a given value into another unit      *       * @param distance value in this unit      * @param unit source unit      * @return value in this unit      */
DECL|method|convert
specifier|public
name|double
name|convert
parameter_list|(
name|double
name|distance
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|)
block|{
return|return
name|convert
argument_list|(
name|distance
argument_list|,
name|unit
argument_list|,
name|this
argument_list|)
return|;
block|}
comment|/**      * Convert a value to a distance string      *       * @param distance value to convert      * @return String representation of the distance       */
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|double
name|distance
parameter_list|)
block|{
return|return
name|distance
operator|+
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|names
index|[
literal|0
index|]
return|;
block|}
comment|/**      * Converts the given distance from the given DistanceUnit, to the given DistanceUnit      *      * @param distance Distance to convert      * @param from     Unit to convert the distance from      * @param to       Unit of distance to convert to      * @return Given distance converted to the distance in the given unit      */
DECL|method|convert
specifier|public
specifier|static
name|double
name|convert
parameter_list|(
name|double
name|distance
parameter_list|,
name|DistanceUnit
name|from
parameter_list|,
name|DistanceUnit
name|to
parameter_list|)
block|{
if|if
condition|(
name|from
operator|==
name|to
condition|)
block|{
return|return
name|distance
return|;
block|}
else|else
block|{
return|return
name|distance
operator|*
name|from
operator|.
name|meters
operator|/
name|to
operator|.
name|meters
return|;
block|}
block|}
comment|/**      * Parses a given distance and converts it to the specified unit.      *       * @param distance String defining a distance (value and unit)      * @param defaultUnit unit assumed if none is defined      * @param to unit of result      * @return parsed distance      */
DECL|method|parse
specifier|public
specifier|static
name|double
name|parse
parameter_list|(
name|String
name|distance
parameter_list|,
name|DistanceUnit
name|defaultUnit
parameter_list|,
name|DistanceUnit
name|to
parameter_list|)
block|{
name|Distance
name|dist
init|=
name|Distance
operator|.
name|parseDistance
argument_list|(
name|distance
argument_list|,
name|defaultUnit
argument_list|)
decl_stmt|;
return|return
name|convert
argument_list|(
name|dist
operator|.
name|value
argument_list|,
name|dist
operator|.
name|unit
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Parses a given distance and converts it to this unit.      *       * @param distance String defining a distance (value and unit)      * @param defaultUnit unit to expect if none if provided      * @return parsed distance      */
DECL|method|parse
specifier|public
name|double
name|parse
parameter_list|(
name|String
name|distance
parameter_list|,
name|DistanceUnit
name|defaultUnit
parameter_list|)
block|{
return|return
name|parse
argument_list|(
name|distance
argument_list|,
name|defaultUnit
argument_list|,
name|this
argument_list|)
return|;
block|}
comment|/**      * Convert a String to a {@link DistanceUnit}      *       * @param unit name of the unit      * @return unit matching the given name      * @throws IllegalArgumentException if no unit matches the given name      */
DECL|method|fromString
specifier|public
specifier|static
name|DistanceUnit
name|fromString
parameter_list|(
name|String
name|unit
parameter_list|)
block|{
for|for
control|(
name|DistanceUnit
name|dunit
range|:
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|name
range|:
name|dunit
operator|.
name|names
control|)
block|{
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|dunit
return|;
block|}
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No distance unit match ["
operator|+
name|unit
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|/**      * Parses the suffix of a given distance string and return the corresponding {@link DistanceUnit}      *       * @param distance string representing a distance      * @param defaultUnit default unit to use, if no unit is provided by the string      * @return unit of the given distance      */
DECL|method|parseUnit
specifier|public
specifier|static
name|DistanceUnit
name|parseUnit
parameter_list|(
name|String
name|distance
parameter_list|,
name|DistanceUnit
name|defaultUnit
parameter_list|)
block|{
for|for
control|(
name|DistanceUnit
name|unit
range|:
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|name
range|:
name|unit
operator|.
name|names
control|)
block|{
if|if
condition|(
name|distance
operator|.
name|endsWith
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|unit
return|;
block|}
block|}
block|}
return|return
name|defaultUnit
return|;
block|}
comment|/**      * Write a {@link DistanceUnit} to a {@link StreamOutput}      *       * @param out {@link StreamOutput} to write to      * @param unit {@link DistanceUnit} to write       */
DECL|method|writeDistanceUnit
specifier|public
specifier|static
name|void
name|writeDistanceUnit
parameter_list|(
name|StreamOutput
name|out
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|unit
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read a {@link DistanceUnit} from a {@link StreamInput}       *       * @param in {@link StreamInput} to read the {@link DistanceUnit} from      * @return {@link DistanceUnit} read from the {@link StreamInput}      * @throws IOException if no unit can be read from the {@link StreamInput}      * @throws IllegalArgumentException if no matching {@link DistanceUnit} can be found      */
DECL|method|readDistanceUnit
specifier|public
specifier|static
name|DistanceUnit
name|readDistanceUnit
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|b
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|b
operator|<
literal|0
operator|||
name|b
operator|>=
name|values
argument_list|()
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No type for distance unit matching ["
operator|+
name|b
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
return|return
name|values
argument_list|()
index|[
name|b
index|]
return|;
block|}
block|}
comment|/**      * This class implements a value+unit tuple.      */
DECL|class|Distance
specifier|public
specifier|static
class|class
name|Distance
implements|implements
name|Comparable
argument_list|<
name|Distance
argument_list|>
block|{
DECL|field|value
specifier|public
specifier|final
name|double
name|value
decl_stmt|;
DECL|field|unit
specifier|public
specifier|final
name|DistanceUnit
name|unit
decl_stmt|;
DECL|method|Distance
specifier|public
name|Distance
parameter_list|(
name|double
name|value
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
block|}
comment|/**          * Converts a {@link Distance} value given in a specific {@link DistanceUnit} into          * a value equal to the specified value but in a other {@link DistanceUnit}.          *           * @param unit unit of the result          * @return converted distance          */
DECL|method|convert
specifier|public
name|Distance
name|convert
parameter_list|(
name|DistanceUnit
name|unit
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|unit
operator|==
name|unit
condition|)
block|{
return|return
name|this
return|;
block|}
else|else
block|{
return|return
operator|new
name|Distance
argument_list|(
name|DistanceUnit
operator|.
name|convert
argument_list|(
name|value
argument_list|,
name|this
operator|.
name|unit
argument_list|,
name|unit
argument_list|)
argument_list|,
name|unit
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Distance
condition|)
block|{
name|Distance
name|other
init|=
operator|(
name|Distance
operator|)
name|obj
decl_stmt|;
return|return
name|DistanceUnit
operator|.
name|convert
argument_list|(
name|value
argument_list|,
name|unit
argument_list|,
name|other
operator|.
name|unit
argument_list|)
operator|==
name|other
operator|.
name|value
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Double
operator|.
name|valueOf
argument_list|(
name|value
operator|*
name|unit
operator|.
name|meters
argument_list|)
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|Distance
name|o
parameter_list|)
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|value
argument_list|,
name|DistanceUnit
operator|.
name|convert
argument_list|(
name|o
operator|.
name|value
argument_list|,
name|o
operator|.
name|unit
argument_list|,
name|unit
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|unit
operator|.
name|toString
argument_list|(
name|value
argument_list|)
return|;
block|}
comment|/**          * Parse a {@link Distance} from a given String. If no unit is given          *<code>DistanceUnit.DEFAULT</code> will be used           *           * @param distance String defining a {@link Distance}           * @return parsed {@link Distance}          */
DECL|method|parseDistance
specifier|public
specifier|static
name|Distance
name|parseDistance
parameter_list|(
name|String
name|distance
parameter_list|)
block|{
return|return
name|parseDistance
argument_list|(
name|distance
argument_list|,
name|DEFAULT
argument_list|)
return|;
block|}
comment|/**          * Parse a {@link Distance} from a given String          *           * @param distance String defining a {@link Distance}           * @param defaultUnit {@link DistanceUnit} to be assumed          *          if not unit is provided in the first argument            * @return parsed {@link Distance}          */
DECL|method|parseDistance
specifier|private
specifier|static
name|Distance
name|parseDistance
parameter_list|(
name|String
name|distance
parameter_list|,
name|DistanceUnit
name|defaultUnit
parameter_list|)
block|{
for|for
control|(
name|DistanceUnit
name|unit
range|:
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|name
range|:
name|unit
operator|.
name|names
control|)
block|{
if|if
condition|(
name|distance
operator|.
name|endsWith
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
operator|new
name|Distance
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|distance
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|distance
operator|.
name|length
argument_list|()
operator|-
name|name
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|unit
argument_list|)
return|;
block|}
block|}
block|}
return|return
operator|new
name|Distance
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|distance
argument_list|)
argument_list|,
name|defaultUnit
argument_list|)
return|;
block|}
block|}
DECL|field|PROTOTYPE
specifier|private
specifier|static
specifier|final
name|DistanceUnit
name|PROTOTYPE
init|=
name|DEFAULT
decl_stmt|;
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|DistanceUnit
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ordinal
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|ordinal
operator|<
literal|0
operator|||
name|ordinal
operator|>=
name|values
argument_list|()
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown DistanceUnit ordinal ["
operator|+
name|ordinal
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|values
argument_list|()
index|[
name|ordinal
index|]
return|;
block|}
DECL|method|readUnitFrom
specifier|public
specifier|static
name|DistanceUnit
name|readUnitFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|this
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_enum

end_unit

