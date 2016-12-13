begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
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
name|Nullable
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
name|geo
operator|.
name|GeoPoint
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
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|IndexGeoPointFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|IndexNumericFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|IndexOrdinalsFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
operator|.
name|ParentChildIndexFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|DocValueFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationExecutionException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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

begin_class
DECL|class|ValuesSourceConfig
specifier|public
class|class
name|ValuesSourceConfig
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|>
block|{
DECL|field|valueSourceType
specifier|private
specifier|final
name|ValuesSourceType
name|valueSourceType
decl_stmt|;
DECL|field|fieldContext
specifier|private
name|FieldContext
name|fieldContext
decl_stmt|;
DECL|field|script
specifier|private
name|SearchScript
name|script
decl_stmt|;
DECL|field|scriptValueType
specifier|private
name|ValueType
name|scriptValueType
decl_stmt|;
DECL|field|unmapped
specifier|private
name|boolean
name|unmapped
init|=
literal|false
decl_stmt|;
DECL|field|format
specifier|private
name|DocValueFormat
name|format
init|=
name|DocValueFormat
operator|.
name|RAW
decl_stmt|;
DECL|field|missing
specifier|private
name|Object
name|missing
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
decl_stmt|;
DECL|method|ValuesSourceConfig
specifier|public
name|ValuesSourceConfig
parameter_list|(
name|ValuesSourceType
name|valueSourceType
parameter_list|)
block|{
name|this
operator|.
name|valueSourceType
operator|=
name|valueSourceType
expr_stmt|;
block|}
DECL|method|valueSourceType
specifier|public
name|ValuesSourceType
name|valueSourceType
parameter_list|()
block|{
return|return
name|valueSourceType
return|;
block|}
DECL|method|fieldContext
specifier|public
name|FieldContext
name|fieldContext
parameter_list|()
block|{
return|return
name|fieldContext
return|;
block|}
DECL|method|script
specifier|public
name|SearchScript
name|script
parameter_list|()
block|{
return|return
name|script
return|;
block|}
DECL|method|unmapped
specifier|public
name|boolean
name|unmapped
parameter_list|()
block|{
return|return
name|unmapped
return|;
block|}
DECL|method|valid
specifier|public
name|boolean
name|valid
parameter_list|()
block|{
return|return
name|fieldContext
operator|!=
literal|null
operator|||
name|script
operator|!=
literal|null
operator|||
name|unmapped
return|;
block|}
DECL|method|fieldContext
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|fieldContext
parameter_list|(
name|FieldContext
name|fieldContext
parameter_list|)
block|{
name|this
operator|.
name|fieldContext
operator|=
name|fieldContext
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|script
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|script
parameter_list|(
name|SearchScript
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scriptValueType
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|scriptValueType
parameter_list|(
name|ValueType
name|scriptValueType
parameter_list|)
block|{
name|this
operator|.
name|scriptValueType
operator|=
name|scriptValueType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scriptValueType
specifier|public
name|ValueType
name|scriptValueType
parameter_list|()
block|{
return|return
name|this
operator|.
name|scriptValueType
return|;
block|}
DECL|method|unmapped
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|unmapped
parameter_list|(
name|boolean
name|unmapped
parameter_list|)
block|{
name|this
operator|.
name|unmapped
operator|=
name|unmapped
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|format
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|format
parameter_list|(
specifier|final
name|DocValueFormat
name|format
parameter_list|)
block|{
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|missing
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|missing
parameter_list|(
specifier|final
name|Object
name|missing
parameter_list|)
block|{
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|missing
specifier|public
name|Object
name|missing
parameter_list|()
block|{
return|return
name|this
operator|.
name|missing
return|;
block|}
DECL|method|timezone
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|timezone
parameter_list|(
specifier|final
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timezone
specifier|public
name|DateTimeZone
name|timezone
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeZone
return|;
block|}
DECL|method|format
specifier|public
name|DocValueFormat
name|format
parameter_list|()
block|{
return|return
name|format
return|;
block|}
comment|/** Get a value source given its configuration. A return value of null indicates that      *  no value source could be built. */
annotation|@
name|Nullable
DECL|method|toValuesSource
specifier|public
name|VS
name|toValuesSource
parameter_list|(
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|valid
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"value source config is invalid; must have either a field context or a script or marked as unwrapped"
argument_list|)
throw|;
block|}
specifier|final
name|VS
name|vs
decl_stmt|;
if|if
condition|(
name|unmapped
argument_list|()
condition|)
block|{
if|if
condition|(
name|missing
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// otherwise we will have values because of the missing value
name|vs
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|NUMERIC
condition|)
block|{
name|vs
operator|=
operator|(
name|VS
operator|)
name|ValuesSource
operator|.
name|Numeric
operator|.
name|EMPTY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|GEOPOINT
condition|)
block|{
name|vs
operator|=
operator|(
name|VS
operator|)
name|ValuesSource
operator|.
name|GeoPoint
operator|.
name|EMPTY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|ANY
operator|||
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|BYTES
condition|)
block|{
name|vs
operator|=
operator|(
name|VS
operator|)
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Can't deal with unmapped ValuesSource type "
operator|+
name|valueSourceType
argument_list|()
argument_list|,
literal|null
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|vs
operator|=
name|originalValuesSource
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|missing
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|vs
return|;
block|}
if|if
condition|(
name|vs
operator|instanceof
name|ValuesSource
operator|.
name|Bytes
condition|)
block|{
specifier|final
name|BytesRef
name|missing
init|=
operator|new
name|BytesRef
argument_list|(
name|missing
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|vs
operator|instanceof
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|MissingValues
operator|.
name|replaceMissing
argument_list|(
operator|(
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|)
name|vs
argument_list|,
name|missing
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|(
name|VS
operator|)
name|MissingValues
operator|.
name|replaceMissing
argument_list|(
operator|(
name|ValuesSource
operator|.
name|Bytes
operator|)
name|vs
argument_list|,
name|missing
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|vs
operator|instanceof
name|ValuesSource
operator|.
name|Numeric
condition|)
block|{
name|Number
name|missing
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|missing
argument_list|()
operator|instanceof
name|Number
condition|)
block|{
name|missing
operator|=
operator|(
name|Number
operator|)
name|missing
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|fieldContext
argument_list|()
operator|!=
literal|null
operator|&&
name|fieldContext
argument_list|()
operator|.
name|fieldType
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|missing
operator|=
name|fieldContext
argument_list|()
operator|.
name|fieldType
argument_list|()
operator|.
name|docValueFormat
argument_list|(
literal|null
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|parseDouble
argument_list|(
name|missing
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|,
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|::
name|nowInMillis
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|missing
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|missing
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|VS
operator|)
name|MissingValues
operator|.
name|replaceMissing
argument_list|(
operator|(
name|ValuesSource
operator|.
name|Numeric
operator|)
name|vs
argument_list|,
name|missing
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|vs
operator|instanceof
name|ValuesSource
operator|.
name|GeoPoint
condition|)
block|{
comment|// TODO: also support the structured formats of geo points
specifier|final
name|GeoPoint
name|missing
init|=
name|GeoUtils
operator|.
name|parseGeoPoint
argument_list|(
name|missing
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|GeoPoint
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|(
name|VS
operator|)
name|MissingValues
operator|.
name|replaceMissing
argument_list|(
operator|(
name|ValuesSource
operator|.
name|GeoPoint
operator|)
name|vs
argument_list|,
name|missing
argument_list|)
return|;
block|}
else|else
block|{
comment|// Should not happen
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Can't apply missing values on a "
operator|+
name|vs
operator|.
name|getClass
argument_list|()
argument_list|,
literal|null
argument_list|)
throw|;
block|}
block|}
comment|/**      * Return the original values source, before we apply `missing`.      */
DECL|method|originalValuesSource
specifier|private
name|VS
name|originalValuesSource
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|fieldContext
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|NUMERIC
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|numericScript
argument_list|()
return|;
block|}
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|BYTES
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|bytesScript
argument_list|()
return|;
block|}
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"value source of type ["
operator|+
name|valueSourceType
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] is not supported by scripts"
argument_list|)
throw|;
block|}
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|NUMERIC
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|numericField
argument_list|()
return|;
block|}
if|if
condition|(
name|valueSourceType
argument_list|()
operator|==
name|ValuesSourceType
operator|.
name|GEOPOINT
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|geoPointField
argument_list|()
return|;
block|}
comment|// falling back to bytes values
return|return
operator|(
name|VS
operator|)
name|bytesField
argument_list|()
return|;
block|}
DECL|method|numericScript
specifier|private
name|ValuesSource
operator|.
name|Numeric
name|numericScript
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ValuesSource
operator|.
name|Numeric
operator|.
name|Script
argument_list|(
name|script
argument_list|()
argument_list|,
name|scriptValueType
argument_list|()
argument_list|)
return|;
block|}
DECL|method|numericField
specifier|private
name|ValuesSource
operator|.
name|Numeric
name|numericField
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|fieldContext
argument_list|()
operator|.
name|indexFieldData
argument_list|()
operator|instanceof
name|IndexNumericFieldData
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected numeric type on field ["
operator|+
name|fieldContext
argument_list|()
operator|.
name|field
argument_list|()
operator|+
literal|"], but got ["
operator|+
name|fieldContext
argument_list|()
operator|.
name|fieldType
argument_list|()
operator|.
name|typeName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|ValuesSource
operator|.
name|Numeric
name|dataSource
init|=
operator|new
name|ValuesSource
operator|.
name|Numeric
operator|.
name|FieldData
argument_list|(
operator|(
name|IndexNumericFieldData
operator|)
name|fieldContext
argument_list|()
operator|.
name|indexFieldData
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|script
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|dataSource
operator|=
operator|new
name|ValuesSource
operator|.
name|Numeric
operator|.
name|WithScript
argument_list|(
name|dataSource
argument_list|,
name|script
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|dataSource
return|;
block|}
DECL|method|bytesField
specifier|private
name|ValuesSource
name|bytesField
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|fieldContext
argument_list|()
operator|.
name|indexFieldData
argument_list|()
decl_stmt|;
name|ValuesSource
name|dataSource
decl_stmt|;
if|if
condition|(
name|indexFieldData
operator|instanceof
name|ParentChildIndexFieldData
condition|)
block|{
name|dataSource
operator|=
operator|new
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|ParentChild
argument_list|(
operator|(
name|ParentChildIndexFieldData
operator|)
name|indexFieldData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexFieldData
operator|instanceof
name|IndexOrdinalsFieldData
condition|)
block|{
name|dataSource
operator|=
operator|new
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|FieldData
argument_list|(
operator|(
name|IndexOrdinalsFieldData
operator|)
name|indexFieldData
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dataSource
operator|=
operator|new
name|ValuesSource
operator|.
name|Bytes
operator|.
name|FieldData
argument_list|(
name|indexFieldData
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|script
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|dataSource
operator|=
operator|new
name|ValuesSource
operator|.
name|WithScript
argument_list|(
name|dataSource
argument_list|,
name|script
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|dataSource
return|;
block|}
DECL|method|bytesScript
specifier|private
name|ValuesSource
operator|.
name|Bytes
name|bytesScript
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ValuesSource
operator|.
name|Bytes
operator|.
name|Script
argument_list|(
name|script
argument_list|()
argument_list|)
return|;
block|}
DECL|method|geoPointField
specifier|private
name|ValuesSource
operator|.
name|GeoPoint
name|geoPointField
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|fieldContext
argument_list|()
operator|.
name|indexFieldData
argument_list|()
operator|instanceof
name|IndexGeoPointFieldData
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected geo_point type on field ["
operator|+
name|fieldContext
argument_list|()
operator|.
name|field
argument_list|()
operator|+
literal|"], but got ["
operator|+
name|fieldContext
argument_list|()
operator|.
name|fieldType
argument_list|()
operator|.
name|typeName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ValuesSource
operator|.
name|GeoPoint
operator|.
name|Fielddata
argument_list|(
operator|(
name|IndexGeoPointFieldData
operator|)
name|fieldContext
argument_list|()
operator|.
name|indexFieldData
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

