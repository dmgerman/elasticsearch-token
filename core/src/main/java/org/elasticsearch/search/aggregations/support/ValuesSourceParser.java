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
name|ParseField
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
name|XContentParser
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
name|index
operator|.
name|mapper
operator|.
name|core
operator|.
name|BooleanFieldMapper
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
name|core
operator|.
name|DateFieldMapper
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
name|core
operator|.
name|NumberFieldMapper
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
name|ip
operator|.
name|IpFieldMapper
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
name|Script
operator|.
name|ScriptField
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
name|ScriptParameterParser
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
name|ScriptParameterParser
operator|.
name|ScriptParameterValue
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
name|InternalAggregation
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
name|support
operator|.
name|format
operator|.
name|ValueFormat
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ValuesSourceParser
specifier|public
class|class
name|ValuesSourceParser
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|>
block|{
DECL|field|TIME_ZONE
specifier|static
specifier|final
name|ParseField
name|TIME_ZONE
init|=
operator|new
name|ParseField
argument_list|(
literal|"time_zone"
argument_list|)
decl_stmt|;
DECL|method|any
specifier|public
specifier|static
name|Builder
name|any
parameter_list|(
name|String
name|aggName
parameter_list|,
name|InternalAggregation
operator|.
name|Type
name|aggType
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|(
name|aggName
argument_list|,
name|aggType
argument_list|,
name|context
argument_list|,
name|ValuesSource
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|numeric
specifier|public
specifier|static
name|Builder
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|numeric
parameter_list|(
name|String
name|aggName
parameter_list|,
name|InternalAggregation
operator|.
name|Type
name|aggType
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|(
name|aggName
argument_list|,
name|aggType
argument_list|,
name|context
argument_list|,
name|ValuesSource
operator|.
name|Numeric
operator|.
name|class
argument_list|)
operator|.
name|targetValueType
argument_list|(
name|ValueType
operator|.
name|NUMERIC
argument_list|)
return|;
block|}
DECL|method|bytes
specifier|public
specifier|static
name|Builder
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|>
name|bytes
parameter_list|(
name|String
name|aggName
parameter_list|,
name|InternalAggregation
operator|.
name|Type
name|aggType
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|(
name|aggName
argument_list|,
name|aggType
argument_list|,
name|context
argument_list|,
name|ValuesSource
operator|.
name|Bytes
operator|.
name|class
argument_list|)
operator|.
name|targetValueType
argument_list|(
name|ValueType
operator|.
name|STRING
argument_list|)
return|;
block|}
DECL|method|geoPoint
specifier|public
specifier|static
name|Builder
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|>
name|geoPoint
parameter_list|(
name|String
name|aggName
parameter_list|,
name|InternalAggregation
operator|.
name|Type
name|aggType
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|(
name|aggName
argument_list|,
name|aggType
argument_list|,
name|context
argument_list|,
name|ValuesSource
operator|.
name|GeoPoint
operator|.
name|class
argument_list|)
operator|.
name|targetValueType
argument_list|(
name|ValueType
operator|.
name|GEOPOINT
argument_list|)
operator|.
name|scriptable
argument_list|(
literal|false
argument_list|)
return|;
block|}
DECL|class|Input
specifier|public
specifier|static
class|class
name|Input
block|{
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
annotation|@
name|Deprecated
DECL|field|params
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
literal|null
decl_stmt|;
comment|// TODO Remove in 3.0
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
DECL|field|timezone
specifier|private
name|DateTimeZone
name|timezone
init|=
name|DateTimeZone
operator|.
name|UTC
decl_stmt|;
DECL|method|timezone
specifier|public
name|DateTimeZone
name|timezone
parameter_list|()
block|{
return|return
name|this
operator|.
name|timezone
return|;
block|}
block|}
DECL|field|aggName
specifier|private
specifier|final
name|String
name|aggName
decl_stmt|;
DECL|field|aggType
specifier|private
specifier|final
name|InternalAggregation
operator|.
name|Type
name|aggType
decl_stmt|;
DECL|field|context
specifier|private
specifier|final
name|SearchContext
name|context
decl_stmt|;
DECL|field|valuesSourceType
specifier|private
specifier|final
name|Class
argument_list|<
name|VS
argument_list|>
name|valuesSourceType
decl_stmt|;
DECL|field|scriptable
specifier|private
name|boolean
name|scriptable
init|=
literal|true
decl_stmt|;
DECL|field|formattable
specifier|private
name|boolean
name|formattable
init|=
literal|false
decl_stmt|;
DECL|field|timezoneAware
specifier|private
name|boolean
name|timezoneAware
init|=
literal|false
decl_stmt|;
DECL|field|targetValueType
specifier|private
name|ValueType
name|targetValueType
init|=
literal|null
decl_stmt|;
DECL|field|scriptParameterParser
specifier|private
name|ScriptParameterParser
name|scriptParameterParser
init|=
operator|new
name|ScriptParameterParser
argument_list|()
decl_stmt|;
DECL|field|input
specifier|private
name|Input
name|input
init|=
operator|new
name|Input
argument_list|()
decl_stmt|;
DECL|method|ValuesSourceParser
specifier|private
name|ValuesSourceParser
parameter_list|(
name|String
name|aggName
parameter_list|,
name|InternalAggregation
operator|.
name|Type
name|aggType
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|Class
argument_list|<
name|VS
argument_list|>
name|valuesSourceType
parameter_list|)
block|{
name|this
operator|.
name|aggName
operator|=
name|aggName
expr_stmt|;
name|this
operator|.
name|aggType
operator|=
name|aggType
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|valuesSourceType
operator|=
name|valuesSourceType
expr_stmt|;
block|}
DECL|method|token
specifier|public
name|boolean
name|token
parameter_list|(
name|String
name|currentFieldName
parameter_list|,
name|XContentParser
operator|.
name|Token
name|token
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|"missing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|&&
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
name|input
operator|.
name|missing
operator|=
name|parser
operator|.
name|objectText
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|input
operator|.
name|field
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|formattable
operator|&&
literal|"format"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|input
operator|.
name|format
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|timezoneAware
operator|&&
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|TIME_ZONE
argument_list|)
condition|)
block|{
name|input
operator|.
name|timezone
operator|=
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scriptable
condition|)
block|{
if|if
condition|(
literal|"value_type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"valueType"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|input
operator|.
name|valueType
operator|=
name|ValueType
operator|.
name|resolveForScript
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|targetValueType
operator|!=
literal|null
operator|&&
name|input
operator|.
name|valueType
operator|.
name|isNotA
argument_list|(
name|targetValueType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
name|aggType
operator|.
name|name
argument_list|()
operator|+
literal|" aggregation ["
operator|+
name|aggName
operator|+
literal|"] was configured with an incompatible value type ["
operator|+
name|input
operator|.
name|valueType
operator|+
literal|"]. ["
operator|+
name|aggType
operator|+
literal|"] aggregation can only work on value of type ["
operator|+
name|targetValueType
operator|+
literal|"]"
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|scriptParameterParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
name|timezoneAware
operator|&&
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|TIME_ZONE
argument_list|)
condition|)
block|{
name|input
operator|.
name|timezone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
if|if
condition|(
name|scriptable
operator|&&
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptField
operator|.
name|SCRIPT
argument_list|)
condition|)
block|{
name|input
operator|.
name|script
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
literal|"params"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|input
operator|.
name|params
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|config
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
parameter_list|()
block|{
if|if
condition|(
name|input
operator|.
name|script
operator|==
literal|null
condition|)
block|{
comment|// Didn't find anything using the new API so try using the old one instead
name|ScriptParameterValue
name|scriptValue
init|=
name|scriptParameterParser
operator|.
name|getDefaultScriptParameterValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|input
operator|.
name|params
operator|==
literal|null
condition|)
block|{
name|input
operator|.
name|params
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|input
operator|.
name|script
operator|=
operator|new
name|Script
argument_list|(
name|scriptValue
operator|.
name|script
argument_list|()
argument_list|,
name|scriptValue
operator|.
name|scriptType
argument_list|()
argument_list|,
name|scriptParameterParser
operator|.
name|lang
argument_list|()
argument_list|,
name|input
operator|.
name|params
argument_list|)
expr_stmt|;
block|}
block|}
name|ValueType
name|valueType
init|=
name|input
operator|.
name|valueType
operator|!=
literal|null
condition|?
name|input
operator|.
name|valueType
else|:
name|targetValueType
decl_stmt|;
if|if
condition|(
name|input
operator|.
name|field
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|input
operator|.
name|script
operator|==
literal|null
condition|)
block|{
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|(
name|ValuesSource
operator|.
name|class
argument_list|)
decl_stmt|;
name|config
operator|.
name|format
operator|=
name|resolveFormat
argument_list|(
literal|null
argument_list|,
name|valueType
argument_list|)
expr_stmt|;
return|return
name|config
return|;
block|}
name|Class
name|valuesSourceType
init|=
name|valueType
operator|!=
literal|null
condition|?
operator|(
name|Class
argument_list|<
name|VS
argument_list|>
operator|)
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
name|ValuesSource
operator|.
name|class
condition|)
block|{
comment|// the specific value source type is undefined, but for scripts, we need to have a specific value source
comment|// type to know how to handle the script values, so we fallback on Bytes
name|valuesSourceType
operator|=
name|ValuesSource
operator|.
name|Bytes
operator|.
name|class
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
operator|=
name|input
operator|.
name|missing
expr_stmt|;
name|config
operator|.
name|format
operator|=
name|resolveFormat
argument_list|(
name|input
operator|.
name|format
argument_list|,
name|valueType
argument_list|)
expr_stmt|;
name|config
operator|.
name|script
operator|=
name|createScript
argument_list|()
expr_stmt|;
name|config
operator|.
name|scriptValueType
operator|=
name|valueType
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
name|smartNameFieldTypeFromAnyType
argument_list|(
name|input
operator|.
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
name|Class
argument_list|<
name|VS
argument_list|>
name|valuesSourceType
init|=
name|valueType
operator|!=
literal|null
condition|?
operator|(
name|Class
argument_list|<
name|VS
argument_list|>
operator|)
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
operator|=
name|input
operator|.
name|missing
expr_stmt|;
name|config
operator|.
name|format
operator|=
name|resolveFormat
argument_list|(
name|input
operator|.
name|format
argument_list|,
name|valueType
argument_list|)
expr_stmt|;
name|config
operator|.
name|unmapped
operator|=
literal|true
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
operator|=
name|valueType
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
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
name|config
decl_stmt|;
if|if
condition|(
name|valuesSourceType
operator|==
name|ValuesSource
operator|.
name|class
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
name|ValuesSource
operator|.
name|Numeric
operator|.
name|class
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
name|ValuesSource
operator|.
name|GeoPoint
operator|.
name|class
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
name|ValuesSource
operator|.
name|Bytes
operator|.
name|class
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
operator|=
operator|new
name|FieldContext
argument_list|(
name|input
operator|.
name|field
argument_list|,
name|indexFieldData
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|config
operator|.
name|missing
operator|=
name|input
operator|.
name|missing
expr_stmt|;
name|config
operator|.
name|script
operator|=
name|createScript
argument_list|()
expr_stmt|;
name|config
operator|.
name|format
operator|=
name|resolveFormat
argument_list|(
name|input
operator|.
name|format
argument_list|,
name|input
operator|.
name|timezone
argument_list|,
name|fieldType
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
parameter_list|()
block|{
return|return
name|input
operator|.
name|script
operator|==
literal|null
condition|?
literal|null
else|:
name|context
operator|.
name|scriptService
argument_list|()
operator|.
name|search
argument_list|(
name|context
operator|.
name|lookup
argument_list|()
argument_list|,
name|input
operator|.
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
DECL|method|resolveFormat
specifier|private
specifier|static
name|ValueFormat
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
name|ValueFormat
operator|.
name|RAW
return|;
comment|// we can't figure it out
block|}
name|ValueFormat
name|valueFormat
init|=
name|valueType
operator|.
name|defaultFormat
decl_stmt|;
if|if
condition|(
name|valueFormat
operator|!=
literal|null
operator|&&
name|valueFormat
operator|instanceof
name|ValueFormat
operator|.
name|Patternable
operator|&&
name|format
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
operator|(
name|ValueFormat
operator|.
name|Patternable
operator|)
name|valueFormat
operator|)
operator|.
name|create
argument_list|(
name|format
argument_list|)
return|;
block|}
return|return
name|valueFormat
return|;
block|}
DECL|method|resolveFormat
specifier|private
specifier|static
name|ValueFormat
name|resolveFormat
parameter_list|(
annotation|@
name|Nullable
name|String
name|format
parameter_list|,
annotation|@
name|Nullable
name|DateTimeZone
name|timezone
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|instanceof
name|DateFieldMapper
operator|.
name|DateFieldType
condition|)
block|{
return|return
name|format
operator|!=
literal|null
condition|?
name|ValueFormat
operator|.
name|DateTime
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|timezone
argument_list|)
else|:
name|ValueFormat
operator|.
name|DateTime
operator|.
name|mapper
argument_list|(
operator|(
name|DateFieldMapper
operator|.
name|DateFieldType
operator|)
name|fieldType
argument_list|,
name|timezone
argument_list|)
return|;
block|}
if|if
condition|(
name|fieldType
operator|instanceof
name|IpFieldMapper
operator|.
name|IpFieldType
condition|)
block|{
return|return
name|ValueFormat
operator|.
name|IPv4
return|;
block|}
if|if
condition|(
name|fieldType
operator|instanceof
name|BooleanFieldMapper
operator|.
name|BooleanFieldType
condition|)
block|{
return|return
name|ValueFormat
operator|.
name|BOOLEAN
return|;
block|}
if|if
condition|(
name|fieldType
operator|instanceof
name|NumberFieldMapper
operator|.
name|NumberFieldType
condition|)
block|{
return|return
name|format
operator|!=
literal|null
condition|?
name|ValueFormat
operator|.
name|Number
operator|.
name|format
argument_list|(
name|format
argument_list|)
else|:
name|ValueFormat
operator|.
name|RAW
return|;
block|}
return|return
name|ValueFormat
operator|.
name|RAW
return|;
block|}
DECL|method|input
specifier|public
name|Input
name|input
parameter_list|()
block|{
return|return
name|this
operator|.
name|input
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|>
block|{
DECL|field|parser
specifier|private
specifier|final
name|ValuesSourceParser
argument_list|<
name|VS
argument_list|>
name|parser
decl_stmt|;
DECL|method|Builder
specifier|private
name|Builder
parameter_list|(
name|String
name|aggName
parameter_list|,
name|InternalAggregation
operator|.
name|Type
name|aggType
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|Class
argument_list|<
name|VS
argument_list|>
name|valuesSourceType
parameter_list|)
block|{
name|parser
operator|=
operator|new
name|ValuesSourceParser
argument_list|<>
argument_list|(
name|aggName
argument_list|,
name|aggType
argument_list|,
name|context
argument_list|,
name|valuesSourceType
argument_list|)
expr_stmt|;
block|}
DECL|method|scriptable
specifier|public
name|Builder
argument_list|<
name|VS
argument_list|>
name|scriptable
parameter_list|(
name|boolean
name|scriptable
parameter_list|)
block|{
name|parser
operator|.
name|scriptable
operator|=
name|scriptable
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|formattable
specifier|public
name|Builder
argument_list|<
name|VS
argument_list|>
name|formattable
parameter_list|(
name|boolean
name|formattable
parameter_list|)
block|{
name|parser
operator|.
name|formattable
operator|=
name|formattable
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timezoneAware
specifier|public
name|Builder
argument_list|<
name|VS
argument_list|>
name|timezoneAware
parameter_list|(
name|boolean
name|timezoneAware
parameter_list|)
block|{
name|parser
operator|.
name|timezoneAware
operator|=
name|timezoneAware
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|targetValueType
specifier|public
name|Builder
argument_list|<
name|VS
argument_list|>
name|targetValueType
parameter_list|(
name|ValueType
name|valueType
parameter_list|)
block|{
name|parser
operator|.
name|targetValueType
operator|=
name|valueType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|ValuesSourceParser
argument_list|<
name|VS
argument_list|>
name|build
parameter_list|()
block|{
return|return
name|parser
return|;
block|}
block|}
block|}
end_class

end_unit

