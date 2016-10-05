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
name|xcontent
operator|.
name|XContentBuilder
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
name|mapper
operator|.
name|MappedFieldType
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
name|Script
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
name|ScriptContext
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
name|aggregations
operator|.
name|AggregationInitializationException
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
name|AbstractAggregationBuilder
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
name|AggregatorFactories
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
name|AggregatorFactories
operator|.
name|Builder
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
name|AggregatorFactory
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
name|InternalAggregation
operator|.
name|Type
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ValuesSourceAggregationBuilder
specifier|public
specifier|abstract
class|class
name|ValuesSourceAggregationBuilder
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|,
name|AB
extends|extends
name|ValuesSourceAggregationBuilder
parameter_list|<
name|VS
parameter_list|,
name|AB
parameter_list|>
parameter_list|>
extends|extends
name|AbstractAggregationBuilder
argument_list|<
name|AB
argument_list|>
block|{
DECL|class|LeafOnly
specifier|public
specifier|abstract
specifier|static
class|class
name|LeafOnly
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|,
name|AB
extends|extends
name|ValuesSourceAggregationBuilder
parameter_list|<
name|VS
parameter_list|,
name|AB
parameter_list|>
parameter_list|>
extends|extends
name|ValuesSourceAggregationBuilder
argument_list|<
name|VS
argument_list|,
name|AB
argument_list|>
block|{
DECL|method|LeafOnly
specifier|protected
name|LeafOnly
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|valuesSourceType
argument_list|,
name|targetValueType
argument_list|)
expr_stmt|;
block|}
comment|/**          * Read an aggregation from a stream that does not serialize its targetValueType. This should be used by most subclasses.          */
DECL|method|LeafOnly
specifier|protected
name|LeafOnly
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|type
argument_list|,
name|valuesSourceType
argument_list|,
name|targetValueType
argument_list|)
expr_stmt|;
block|}
comment|/**          * Read an aggregation from a stream that serializes its targetValueType. This should only be used by subclasses that override          * {@link #serializeTargetValueType()} to return true.          */
DECL|method|LeafOnly
specifier|protected
name|LeafOnly
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|type
argument_list|,
name|valuesSourceType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|subAggregations
specifier|public
name|AB
name|subAggregations
parameter_list|(
name|Builder
name|subFactories
parameter_list|)
block|{
throw|throw
operator|new
name|AggregationInitializationException
argument_list|(
literal|"Aggregator ["
operator|+
name|name
operator|+
literal|"] of type ["
operator|+
name|type
operator|+
literal|"] cannot accept sub-aggregations"
argument_list|)
throw|;
block|}
block|}
DECL|field|valuesSourceType
specifier|private
specifier|final
name|ValuesSourceType
name|valuesSourceType
decl_stmt|;
DECL|field|targetValueType
specifier|private
specifier|final
name|ValueType
name|targetValueType
decl_stmt|;
DECL|field|field
specifier|private
name|String
name|field
init|=
literal|null
decl_stmt|;
DECL|field|script
specifier|private
name|Script
name|script
init|=
literal|null
decl_stmt|;
DECL|field|valueType
specifier|private
name|ValueType
name|valueType
init|=
literal|null
decl_stmt|;
DECL|field|format
specifier|private
name|String
name|format
init|=
literal|null
decl_stmt|;
DECL|field|missing
specifier|private
name|Object
name|missing
init|=
literal|null
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
init|=
literal|null
decl_stmt|;
DECL|field|config
specifier|protected
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
decl_stmt|;
DECL|method|ValuesSourceAggregationBuilder
specifier|protected
name|ValuesSourceAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|valuesSourceType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[valuesSourceType] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|valuesSourceType
operator|=
name|valuesSourceType
expr_stmt|;
name|this
operator|.
name|targetValueType
operator|=
name|targetValueType
expr_stmt|;
block|}
comment|/**      * Read an aggregation from a stream that does not serialize its targetValueType. This should be used by most subclasses.      */
DECL|method|ValuesSourceAggregationBuilder
specifier|protected
name|ValuesSourceAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|type
argument_list|)
expr_stmt|;
assert|assert
literal|false
operator|==
name|serializeTargetValueType
argument_list|()
operator|:
literal|"Wrong read constructor called for subclass that provides its targetValueType"
assert|;
name|this
operator|.
name|valuesSourceType
operator|=
name|valuesSourceType
expr_stmt|;
name|this
operator|.
name|targetValueType
operator|=
name|targetValueType
expr_stmt|;
name|read
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read an aggregation from a stream that serializes its targetValueType. This should only be used by subclasses that override      * {@link #serializeTargetValueType()} to return true.      */
DECL|method|ValuesSourceAggregationBuilder
specifier|protected
name|ValuesSourceAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|type
argument_list|)
expr_stmt|;
assert|assert
name|serializeTargetValueType
argument_list|()
operator|:
literal|"Wrong read constructor called for subclass that serializes its targetValueType"
assert|;
name|this
operator|.
name|valuesSourceType
operator|=
name|valuesSourceType
expr_stmt|;
name|this
operator|.
name|targetValueType
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|ValueType
operator|::
name|readFromStream
argument_list|)
expr_stmt|;
name|read
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|read
specifier|private
name|void
name|read
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|field
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|script
operator|=
operator|new
name|Script
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|valueType
operator|=
name|ValueType
operator|.
name|readFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|format
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|missing
operator|=
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
specifier|final
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|serializeTargetValueType
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|targetValueType
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|boolean
name|hasScript
init|=
name|script
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasScript
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasScript
condition|)
block|{
name|script
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|boolean
name|hasValueType
init|=
name|valueType
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasValueType
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasValueType
condition|)
block|{
name|valueType
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|format
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|boolean
name|hasTimeZone
init|=
name|timeZone
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasTimeZone
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasTimeZone
condition|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|timeZone
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|innerWriteTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
comment|/**      * Write subclass's state to the stream.      */
DECL|method|innerWriteTo
specifier|protected
specifier|abstract
name|void
name|innerWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Should this builder serialize its targetValueType? Defaults to false. All subclasses that override this to true should use the three      * argument read constructor rather than the four argument version.      */
DECL|method|serializeTargetValueType
specifier|protected
name|boolean
name|serializeTargetValueType
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**      * Sets the field to use for this aggregation.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|field
specifier|public
name|AB
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[field] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
return|return
operator|(
name|AB
operator|)
name|this
return|;
block|}
comment|/**      * Gets the field to use for this aggregation.      */
DECL|method|field
specifier|public
name|String
name|field
parameter_list|()
block|{
return|return
name|field
return|;
block|}
comment|/**      * Sets the script to use for this aggregation.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|script
specifier|public
name|AB
name|script
parameter_list|(
name|Script
name|script
parameter_list|)
block|{
if|if
condition|(
name|script
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[script] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
return|return
operator|(
name|AB
operator|)
name|this
return|;
block|}
comment|/**      * Gets the script to use for this aggregation.      */
DECL|method|script
specifier|public
name|Script
name|script
parameter_list|()
block|{
return|return
name|script
return|;
block|}
comment|/**      * Sets the {@link ValueType} for the value produced by this aggregation      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|valueType
specifier|public
name|AB
name|valueType
parameter_list|(
name|ValueType
name|valueType
parameter_list|)
block|{
if|if
condition|(
name|valueType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[valueType] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|valueType
operator|=
name|valueType
expr_stmt|;
return|return
operator|(
name|AB
operator|)
name|this
return|;
block|}
comment|/**      * Gets the {@link ValueType} for the value produced by this aggregation      */
DECL|method|valueType
specifier|public
name|ValueType
name|valueType
parameter_list|()
block|{
return|return
name|valueType
return|;
block|}
comment|/**      * Sets the format to use for the output of the aggregation.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|format
specifier|public
name|AB
name|format
parameter_list|(
name|String
name|format
parameter_list|)
block|{
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[format] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
return|return
operator|(
name|AB
operator|)
name|this
return|;
block|}
comment|/**      * Gets the format to use for the output of the aggregation.      */
DECL|method|format
specifier|public
name|String
name|format
parameter_list|()
block|{
return|return
name|format
return|;
block|}
comment|/**      * Sets the value to use when the aggregation finds a missing value in a      * document      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|missing
specifier|public
name|AB
name|missing
parameter_list|(
name|Object
name|missing
parameter_list|)
block|{
if|if
condition|(
name|missing
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[missing] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
return|return
operator|(
name|AB
operator|)
name|this
return|;
block|}
comment|/**      * Gets the value to use when the aggregation finds a missing value in a      * document      */
DECL|method|missing
specifier|public
name|Object
name|missing
parameter_list|()
block|{
return|return
name|missing
return|;
block|}
comment|/**      * Sets the time zone to use for this aggregation      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|timeZone
specifier|public
name|AB
name|timeZone
parameter_list|(
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
if|if
condition|(
name|timeZone
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[timeZone] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
return|return
operator|(
name|AB
operator|)
name|this
return|;
block|}
comment|/**      * Gets the time zone to use for this aggregation      */
DECL|method|timeZone
specifier|public
name|DateTimeZone
name|timeZone
parameter_list|()
block|{
return|return
name|timeZone
return|;
block|}
annotation|@
name|Override
DECL|method|doBuild
specifier|protected
specifier|final
name|ValuesSourceAggregatorFactory
argument_list|<
name|VS
argument_list|,
name|?
argument_list|>
name|doBuild
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactories
operator|.
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
init|=
name|resolveConfig
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|ValuesSourceAggregatorFactory
argument_list|<
name|VS
argument_list|,
name|?
argument_list|>
name|factory
init|=
name|innerBuild
argument_list|(
name|context
argument_list|,
name|config
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|)
decl_stmt|;
return|return
name|factory
return|;
block|}
DECL|method|resolveConfig
specifier|protected
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|resolveConfig
parameter_list|(
name|AggregationContext
name|context
parameter_list|)
block|{
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
init|=
name|config
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
name|config
return|;
block|}
DECL|method|innerBuild
specifier|protected
specifier|abstract
name|ValuesSourceAggregatorFactory
argument_list|<
name|VS
argument_list|,
name|?
argument_list|>
name|innerBuild
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactories
operator|.
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|config
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
parameter_list|(
name|AggregationContext
name|context
parameter_list|)
block|{
name|ValueType
name|valueType
init|=
name|this
operator|.
name|valueType
operator|!=
literal|null
condition|?
name|this
operator|.
name|valueType
else|:
name|targetValueType
decl_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|script
operator|==
literal|null
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|(
name|ValuesSourceType
operator|.
name|ANY
argument_list|)
decl_stmt|;
name|config
operator|.
name|format
argument_list|(
name|resolveFormat
argument_list|(
literal|null
argument_list|,
name|valueType
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|config
return|;
block|}
name|ValuesSourceType
name|valuesSourceType
init|=
name|valueType
operator|!=
literal|null
condition|?
name|valueType
operator|.
name|getValuesSourceType
argument_list|()
else|:
name|this
operator|.
name|valuesSourceType
decl_stmt|;
if|if
condition|(
name|valuesSourceType
operator|==
literal|null
operator|||
name|valuesSourceType
operator|==
name|ValuesSourceType
operator|.
name|ANY
condition|)
block|{
comment|// the specific value source type is undefined, but for scripts,
comment|// we need to have a specific value source
comment|// type to know how to handle the script values, so we fallback
comment|// on Bytes
name|valuesSourceType
operator|=
name|ValuesSourceType
operator|.
name|BYTES
expr_stmt|;
block|}
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
argument_list|(
name|valuesSourceType
argument_list|)
decl_stmt|;
name|config
operator|.
name|missing
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|config
operator|.
name|timezone
argument_list|(
name|timeZone
argument_list|)
expr_stmt|;
name|config
operator|.
name|format
argument_list|(
name|resolveFormat
argument_list|(
name|format
argument_list|,
name|valueType
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|script
argument_list|(
name|createScript
argument_list|(
name|script
argument_list|,
name|context
operator|.
name|searchContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|scriptValueType
argument_list|(
name|valueType
argument_list|)
expr_stmt|;
return|return
name|config
return|;
block|}
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|smartNameFieldType
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
name|ValuesSourceType
name|valuesSourceType
init|=
name|valueType
operator|!=
literal|null
condition|?
name|valueType
operator|.
name|getValuesSourceType
argument_list|()
else|:
name|this
operator|.
name|valuesSourceType
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|<>
argument_list|(
name|valuesSourceType
argument_list|)
decl_stmt|;
name|config
operator|.
name|missing
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|config
operator|.
name|timezone
argument_list|(
name|timeZone
argument_list|)
expr_stmt|;
name|config
operator|.
name|format
argument_list|(
name|resolveFormat
argument_list|(
name|format
argument_list|,
name|valueType
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|unmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueType
operator|!=
literal|null
condition|)
block|{
comment|// todo do we really need this for unmapped?
name|config
operator|.
name|scriptValueType
argument_list|(
name|valueType
argument_list|)
expr_stmt|;
block|}
return|return
name|config
return|;
block|}
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
decl_stmt|;
if|if
condition|(
name|valuesSourceType
operator|==
name|ValuesSourceType
operator|.
name|ANY
condition|)
block|{
if|if
condition|(
name|indexFieldData
operator|instanceof
name|IndexNumericFieldData
condition|)
block|{
name|config
operator|=
operator|new
name|ValuesSourceConfig
argument_list|<>
argument_list|(
name|ValuesSourceType
operator|.
name|NUMERIC
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexFieldData
operator|instanceof
name|IndexGeoPointFieldData
condition|)
block|{
name|config
operator|=
operator|new
name|ValuesSourceConfig
argument_list|<>
argument_list|(
name|ValuesSourceType
operator|.
name|GEOPOINT
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|config
operator|=
operator|new
name|ValuesSourceConfig
argument_list|<>
argument_list|(
name|ValuesSourceType
operator|.
name|BYTES
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|config
operator|=
operator|new
name|ValuesSourceConfig
argument_list|(
name|valuesSourceType
argument_list|)
expr_stmt|;
block|}
name|config
operator|.
name|fieldContext
argument_list|(
operator|new
name|FieldContext
argument_list|(
name|field
argument_list|,
name|indexFieldData
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|missing
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|config
operator|.
name|timezone
argument_list|(
name|timeZone
argument_list|)
expr_stmt|;
name|config
operator|.
name|script
argument_list|(
name|createScript
argument_list|(
name|script
argument_list|,
name|context
operator|.
name|searchContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|.
name|format
argument_list|(
name|fieldType
operator|.
name|docValueFormat
argument_list|(
name|format
argument_list|,
name|timeZone
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|config
return|;
block|}
DECL|method|createScript
specifier|private
name|SearchScript
name|createScript
parameter_list|(
name|Script
name|script
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|script
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|getSearchScript
argument_list|(
name|script
argument_list|,
name|ScriptContext
operator|.
name|Standard
operator|.
name|AGGS
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|resolveFormat
specifier|private
specifier|static
name|DocValueFormat
name|resolveFormat
parameter_list|(
annotation|@
name|Nullable
name|String
name|format
parameter_list|,
annotation|@
name|Nullable
name|ValueType
name|valueType
parameter_list|)
block|{
if|if
condition|(
name|valueType
operator|==
literal|null
condition|)
block|{
return|return
name|DocValueFormat
operator|.
name|RAW
return|;
comment|// we can't figure it out
block|}
name|DocValueFormat
name|valueFormat
init|=
name|valueType
operator|.
name|defaultFormat
decl_stmt|;
if|if
condition|(
name|valueFormat
operator|instanceof
name|DocValueFormat
operator|.
name|Decimal
operator|&&
name|format
operator|!=
literal|null
condition|)
block|{
name|valueFormat
operator|=
operator|new
name|DocValueFormat
operator|.
name|Decimal
argument_list|(
name|format
argument_list|)
expr_stmt|;
block|}
return|return
name|valueFormat
return|;
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|public
specifier|final
name|XContentBuilder
name|internalXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|missing
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"missing"
argument_list|,
name|missing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timeZone
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"time_zone"
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|valueType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"value_type"
argument_list|,
name|valueType
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|doXContentBody
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|doXContentBody
specifier|protected
specifier|abstract
name|XContentBuilder
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
specifier|final
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|field
argument_list|,
name|format
argument_list|,
name|missing
argument_list|,
name|script
argument_list|,
name|targetValueType
argument_list|,
name|timeZone
argument_list|,
name|valueType
argument_list|,
name|valuesSourceType
argument_list|,
name|innerHashCode
argument_list|()
argument_list|)
return|;
block|}
DECL|method|innerHashCode
specifier|protected
specifier|abstract
name|int
name|innerHashCode
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
specifier|final
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|ValuesSourceAggregationBuilder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|other
init|=
operator|(
name|ValuesSourceAggregationBuilder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|field
argument_list|,
name|other
operator|.
name|field
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|format
argument_list|,
name|other
operator|.
name|format
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|missing
argument_list|,
name|other
operator|.
name|missing
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|script
argument_list|,
name|other
operator|.
name|script
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|targetValueType
argument_list|,
name|other
operator|.
name|targetValueType
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|timeZone
argument_list|,
name|other
operator|.
name|timeZone
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|valueType
argument_list|,
name|other
operator|.
name|valueType
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|valuesSourceType
argument_list|,
name|other
operator|.
name|valuesSourceType
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
name|innerEquals
argument_list|(
name|obj
argument_list|)
return|;
block|}
DECL|method|innerEquals
specifier|protected
specifier|abstract
name|boolean
name|innerEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
function_decl|;
block|}
end_class

end_unit

