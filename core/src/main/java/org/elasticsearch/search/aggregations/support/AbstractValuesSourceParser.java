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
name|ParseFieldMatcher
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
name|ParsingException
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
name|query
operator|.
name|QueryParseContext
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
DECL|class|AbstractValuesSourceParser
specifier|public
specifier|abstract
class|class
name|AbstractValuesSourceParser
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|>
implements|implements
name|Aggregator
operator|.
name|Parser
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
DECL|class|AnyValuesSourceParser
specifier|public
specifier|abstract
specifier|static
class|class
name|AnyValuesSourceParser
extends|extends
name|AbstractValuesSourceParser
argument_list|<
name|ValuesSource
argument_list|>
block|{
DECL|method|AnyValuesSourceParser
specifier|protected
name|AnyValuesSourceParser
parameter_list|(
name|boolean
name|scriptable
parameter_list|,
name|boolean
name|formattable
parameter_list|)
block|{
name|super
argument_list|(
name|scriptable
argument_list|,
name|formattable
argument_list|,
literal|false
argument_list|,
name|ValuesSourceType
operator|.
name|ANY
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NumericValuesSourceParser
specifier|public
specifier|abstract
specifier|static
class|class
name|NumericValuesSourceParser
extends|extends
name|AbstractValuesSourceParser
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
block|{
DECL|method|NumericValuesSourceParser
specifier|protected
name|NumericValuesSourceParser
parameter_list|(
name|boolean
name|scriptable
parameter_list|,
name|boolean
name|formattable
parameter_list|,
name|boolean
name|timezoneAware
parameter_list|)
block|{
name|super
argument_list|(
name|scriptable
argument_list|,
name|formattable
argument_list|,
name|timezoneAware
argument_list|,
name|ValuesSourceType
operator|.
name|NUMERIC
argument_list|,
name|ValueType
operator|.
name|NUMERIC
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|BytesValuesSourceParser
specifier|public
specifier|abstract
specifier|static
class|class
name|BytesValuesSourceParser
extends|extends
name|AbstractValuesSourceParser
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|>
block|{
DECL|method|BytesValuesSourceParser
specifier|protected
name|BytesValuesSourceParser
parameter_list|(
name|boolean
name|scriptable
parameter_list|,
name|boolean
name|formattable
parameter_list|)
block|{
name|super
argument_list|(
name|scriptable
argument_list|,
name|formattable
argument_list|,
literal|false
argument_list|,
name|ValuesSourceType
operator|.
name|BYTES
argument_list|,
name|ValueType
operator|.
name|STRING
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|GeoPointValuesSourceParser
specifier|public
specifier|abstract
specifier|static
class|class
name|GeoPointValuesSourceParser
extends|extends
name|AbstractValuesSourceParser
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|>
block|{
DECL|method|GeoPointValuesSourceParser
specifier|protected
name|GeoPointValuesSourceParser
parameter_list|(
name|boolean
name|scriptable
parameter_list|,
name|boolean
name|formattable
parameter_list|)
block|{
name|super
argument_list|(
name|scriptable
argument_list|,
name|formattable
argument_list|,
literal|false
argument_list|,
name|ValuesSourceType
operator|.
name|GEOPOINT
argument_list|,
name|ValueType
operator|.
name|GEOPOINT
argument_list|)
expr_stmt|;
block|}
block|}
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
DECL|field|valuesSourceType
specifier|private
name|ValuesSourceType
name|valuesSourceType
init|=
literal|null
decl_stmt|;
DECL|field|targetValueType
specifier|private
name|ValueType
name|targetValueType
init|=
literal|null
decl_stmt|;
DECL|method|AbstractValuesSourceParser
specifier|private
name|AbstractValuesSourceParser
parameter_list|(
name|boolean
name|scriptable
parameter_list|,
name|boolean
name|formattable
parameter_list|,
name|boolean
name|timezoneAware
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|)
block|{
name|this
operator|.
name|timezoneAware
operator|=
name|timezoneAware
expr_stmt|;
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
name|this
operator|.
name|scriptable
operator|=
name|scriptable
expr_stmt|;
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
specifier|final
name|ValuesSourceAggregatorBuilder
argument_list|<
name|VS
argument_list|,
name|?
argument_list|>
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
name|String
name|field
init|=
literal|null
decl_stmt|;
name|Script
name|script
init|=
literal|null
decl_stmt|;
name|ValueType
name|valueType
init|=
literal|null
decl_stmt|;
name|String
name|format
init|=
literal|null
decl_stmt|;
name|Object
name|missing
init|=
literal|null
decl_stmt|;
name|DateTimeZone
name|timezone
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
name|missing
operator|=
name|parser
operator|.
name|objectText
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
name|getParseFieldMatcher
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
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
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
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
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
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Aggregation ["
operator|+
name|aggregationName
operator|+
literal|"] was configured with an incompatible value type ["
operator|+
name|valueType
operator|+
literal|"]. It can only work on value of type ["
operator|+
name|targetValueType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|token
argument_list|(
name|aggregationName
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|,
name|otherOptions
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|token
argument_list|(
name|aggregationName
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|,
name|otherOptions
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
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
name|getParseFieldMatcher
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
name|getParseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|token
argument_list|(
name|aggregationName
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|,
name|otherOptions
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|token
argument_list|(
name|aggregationName
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|,
name|otherOptions
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
name|ValuesSourceAggregatorBuilder
argument_list|<
name|VS
argument_list|,
name|?
argument_list|>
name|factory
init|=
name|createFactory
argument_list|(
name|aggregationName
argument_list|,
name|this
operator|.
name|valuesSourceType
argument_list|,
name|this
operator|.
name|targetValueType
argument_list|,
name|otherOptions
argument_list|)
decl_stmt|;
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|field
argument_list|(
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
name|factory
operator|.
name|script
argument_list|(
name|script
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
name|factory
operator|.
name|valueType
argument_list|(
name|valueType
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
name|factory
operator|.
name|format
argument_list|(
name|format
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
name|factory
operator|.
name|missing
argument_list|(
name|missing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timezone
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|timeZone
argument_list|(
name|timezone
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
comment|/**      * Creates a {@link ValuesSourceAggregatorBuilder} from the information      * gathered by the subclass. Options parsed in      * {@link AbstractValuesSourceParser} itself will be added to the factory      * after it has been returned by this method.      *      * @param aggregationName      *            the name of the aggregation      * @param valuesSourceType      *            the type of the {@link ValuesSource}      * @param targetValueType      *            the target type of the final value output by the aggregation      * @param otherOptions      *            a {@link Map} containing the extra options parsed by the      *            {@link #token(String, String, org.elasticsearch.common.xcontent.XContentParser.Token,      *             XContentParser, ParseFieldMatcher, Map)}      *            method      * @return the created factory      */
DECL|method|createFactory
specifier|protected
specifier|abstract
name|ValuesSourceAggregatorBuilder
argument_list|<
name|VS
argument_list|,
name|?
argument_list|>
name|createFactory
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
function_decl|;
comment|/**      * Allows subclasses of {@link AbstractValuesSourceParser} to parse extra      * parameters and store them in a {@link Map} which will later be passed to      * {@link #createFactory(String, ValuesSourceType, ValueType, Map)}.      *      * @param aggregationName      *            the name of the aggregation      * @param currentFieldName      *            the name of the current field being parsed      * @param token      *            the current token for the parser      * @param parser      *            the parser      * @param parseFieldMatcher      *            the {@link ParseFieldMatcher} to use to match field names      * @param otherOptions      *            a {@link Map} of options to be populated by successive calls      *            to this method which will then be passed to the      *            {@link #createFactory(String, ValuesSourceType, ValueType, Map)}      *            method      * @return<code>true</code> if the current token was correctly parsed,      *<code>false</code> otherwise      * @throws IOException      *             if an error occurs whilst parsing      */
DECL|method|token
specifier|protected
specifier|abstract
name|boolean
name|token
parameter_list|(
name|String
name|aggregationName
parameter_list|,
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
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

