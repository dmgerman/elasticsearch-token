begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|percentiles
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|DoubleArrayList
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
name|Aggregator
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
name|metrics
operator|.
name|percentiles
operator|.
name|tdigest
operator|.
name|InternalTDigestPercentiles
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
name|ValuesSource
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
name|ValuesSource
operator|.
name|Numeric
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
name|ValuesSourceConfig
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
name|ValuesSourceParser
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
name|Arrays
import|;
end_import

begin_class
DECL|class|AbstractPercentilesParser
specifier|public
specifier|abstract
class|class
name|AbstractPercentilesParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
DECL|field|KEYED_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|KEYED_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"keyed"
argument_list|)
decl_stmt|;
DECL|field|METHOD_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|METHOD_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"method"
argument_list|)
decl_stmt|;
DECL|field|COMPRESSION_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|COMPRESSION_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"compression"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_SIGNIFICANT_DIGITS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|NUMBER_SIGNIFICANT_DIGITS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"number_of_significant_value_digits"
argument_list|)
decl_stmt|;
DECL|field|formattable
specifier|private
name|boolean
name|formattable
decl_stmt|;
DECL|method|AbstractPercentilesParser
specifier|public
name|AbstractPercentilesParser
parameter_list|(
name|boolean
name|formattable
parameter_list|)
block|{
name|this
operator|.
name|formattable
operator|=
name|formattable
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|ValuesSourceParser
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|vsParser
init|=
name|ValuesSourceParser
operator|.
name|numeric
argument_list|(
name|aggregationName
argument_list|,
name|InternalTDigestPercentiles
operator|.
name|TYPE
argument_list|,
name|context
argument_list|)
operator|.
name|formattable
argument_list|(
name|formattable
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|double
index|[]
name|keys
init|=
literal|null
decl_stmt|;
name|boolean
name|keyed
init|=
literal|true
decl_stmt|;
name|Double
name|compression
init|=
literal|null
decl_stmt|;
name|Integer
name|numberOfSignificantValueDigits
init|=
literal|null
decl_stmt|;
name|PercentilesMethod
name|method
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|vsParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|)
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
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
name|keysField
argument_list|()
argument_list|)
condition|)
block|{
name|DoubleArrayList
name|values
init|=
operator|new
name|DoubleArrayList
argument_list|(
literal|10
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|double
name|value
init|=
name|parser
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|keys
operator|=
name|values
operator|.
name|toArray
argument_list|()
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|keys
argument_list|)
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
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
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_BOOLEAN
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
name|KEYED_FIELD
argument_list|)
condition|)
block|{
name|keyed
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
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
name|method
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Found multiple methods in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]. only one of ["
operator|+
name|PercentilesMethod
operator|.
name|TDIGEST
operator|.
name|getName
argument_list|()
operator|+
literal|"] and ["
operator|+
name|PercentilesMethod
operator|.
name|HDR
operator|.
name|getName
argument_list|()
operator|+
literal|"] may be used."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
name|method
operator|=
name|PercentilesMethod
operator|.
name|resolveFromName
argument_list|(
name|currentFieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|method
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
switch|switch
condition|(
name|method
condition|)
block|{
case|case
name|TDIGEST
case|:
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
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
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|COMPRESSION_FIELD
argument_list|)
condition|)
block|{
name|compression
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
break|break;
case|case
name|HDR
case|:
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
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
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|NUMBER_SIGNIFICANT_DIGITS_FIELD
argument_list|)
condition|)
block|{
name|numberOfSignificantValueDigits
operator|=
name|parser
operator|.
name|intValue
argument_list|()
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
break|break;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|method
operator|==
literal|null
condition|)
block|{
name|method
operator|=
name|PercentilesMethod
operator|.
name|TDIGEST
expr_stmt|;
block|}
switch|switch
condition|(
name|method
condition|)
block|{
case|case
name|TDIGEST
case|:
if|if
condition|(
name|numberOfSignificantValueDigits
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"[number_of_significant_value_digits] cannot be used with method [tdigest] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|compression
operator|==
literal|null
condition|)
block|{
name|compression
operator|=
literal|100.0
expr_stmt|;
block|}
break|break;
case|case
name|HDR
case|:
if|if
condition|(
name|compression
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"[compression] cannot be used with method [hdr] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|numberOfSignificantValueDigits
operator|==
literal|null
condition|)
block|{
name|numberOfSignificantValueDigits
operator|=
literal|3
expr_stmt|;
block|}
break|break;
default|default:
comment|// Shouldn't get here but if we do, throw a parse exception for
comment|// invalid method
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown value for ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|method
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|buildFactory
argument_list|(
name|context
argument_list|,
name|aggregationName
argument_list|,
name|vsParser
operator|.
name|config
argument_list|()
argument_list|,
name|keys
argument_list|,
name|method
argument_list|,
name|compression
argument_list|,
name|numberOfSignificantValueDigits
argument_list|,
name|keyed
argument_list|)
return|;
block|}
DECL|method|buildFactory
specifier|protected
specifier|abstract
name|AggregatorFactory
name|buildFactory
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|String
name|aggregationName
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|Numeric
argument_list|>
name|config
parameter_list|,
name|double
index|[]
name|cdfValues
parameter_list|,
name|PercentilesMethod
name|method
parameter_list|,
name|Double
name|compression
parameter_list|,
name|Integer
name|numberOfSignificantValueDigits
parameter_list|,
name|boolean
name|keyed
parameter_list|)
function_decl|;
DECL|method|keysField
specifier|protected
specifier|abstract
name|ParseField
name|keysField
parameter_list|()
function_decl|;
block|}
end_class

end_unit

