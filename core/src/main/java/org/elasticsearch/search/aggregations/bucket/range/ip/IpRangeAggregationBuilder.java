begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.ip
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|range
operator|.
name|ip
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
name|document
operator|.
name|InetAddressPoint
import|;
end_import

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
name|network
operator|.
name|InetAddresses
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
name|ObjectParser
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
name|ToXContent
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
operator|.
name|Token
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilder
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
name|bucket
operator|.
name|range
operator|.
name|BinaryRangeAggregator
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
name|bucket
operator|.
name|range
operator|.
name|BinaryRangeAggregatorFactory
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
name|bucket
operator|.
name|range
operator|.
name|RangeAggregator
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
name|ValueType
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
name|ValuesSourceAggregationBuilder
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
name|ValuesSourceAggregatorFactory
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
name|ValuesSourceParserHelper
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
name|ValuesSourceType
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
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

begin_class
DECL|class|IpRangeAggregationBuilder
specifier|public
specifier|final
class|class
name|IpRangeAggregationBuilder
extends|extends
name|ValuesSourceAggregationBuilder
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|,
name|IpRangeAggregationBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"ip_range"
decl_stmt|;
DECL|field|MASK_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|MASK_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"mask"
argument_list|)
decl_stmt|;
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|IpRangeAggregationBuilder
argument_list|,
name|QueryParseContext
argument_list|>
name|PARSER
decl_stmt|;
static|static
block|{
name|PARSER
operator|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|IpRangeAggregationBuilder
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|ValuesSourceParserHelper
operator|.
name|declareBytesFields
argument_list|(
name|PARSER
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareBoolean
argument_list|(
name|IpRangeAggregationBuilder
operator|::
name|keyed
argument_list|,
name|RangeAggregator
operator|.
name|KEYED_FIELD
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareObjectArray
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|ranges
parameter_list|)
lambda|->
block|{
for|for
control|(
name|Range
name|range
range|:
name|ranges
control|)
name|agg
operator|.
name|addRange
argument_list|(
name|range
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|IpRangeAggregationBuilder
operator|::
name|parseRange
argument_list|,
name|RangeAggregator
operator|.
name|RANGES_FIELD
argument_list|)
expr_stmt|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|AggregationBuilder
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
return|return
name|PARSER
operator|.
name|parse
argument_list|(
name|context
operator|.
name|parser
argument_list|()
argument_list|,
operator|new
name|IpRangeAggregationBuilder
argument_list|(
name|aggregationName
argument_list|)
argument_list|,
name|context
argument_list|)
return|;
block|}
DECL|method|parseRange
specifier|private
specifier|static
name|Range
name|parseRange
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
literal|null
decl_stmt|;
name|String
name|from
init|=
literal|null
decl_stmt|;
name|String
name|to
init|=
literal|null
decl_stmt|;
name|String
name|mask
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|!=
name|Token
operator|.
name|START_OBJECT
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
literal|"[ranges] must contain objects, but hit a "
operator|+
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
throw|;
block|}
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|RangeAggregator
operator|.
name|Range
operator|.
name|KEY_FIELD
operator|.
name|match
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|key
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
name|RangeAggregator
operator|.
name|Range
operator|.
name|FROM_FIELD
operator|.
name|match
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|from
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|RangeAggregator
operator|.
name|Range
operator|.
name|TO_FIELD
operator|.
name|match
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|to
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|MASK_FIELD
operator|.
name|match
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|mask
operator|=
name|parser
operator|.
name|text
argument_list|()
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
literal|"Unexpected ip range parameter: ["
operator|+
name|parser
operator|.
name|currentName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|mask
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
name|key
operator|=
name|mask
expr_stmt|;
block|}
return|return
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|mask
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
block|}
DECL|class|Range
specifier|public
specifier|static
class|class
name|Range
implements|implements
name|ToXContent
block|{
DECL|field|key
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
DECL|field|from
specifier|private
specifier|final
name|String
name|from
decl_stmt|;
DECL|field|to
specifier|private
specifier|final
name|String
name|to
decl_stmt|;
DECL|method|Range
name|Range
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
if|if
condition|(
name|from
operator|!=
literal|null
condition|)
block|{
name|InetAddresses
operator|.
name|forString
argument_list|(
name|from
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|to
operator|!=
literal|null
condition|)
block|{
name|InetAddresses
operator|.
name|forString
argument_list|(
name|to
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|to
expr_stmt|;
block|}
DECL|method|Range
name|Range
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|mask
parameter_list|)
block|{
name|String
index|[]
name|splits
init|=
name|mask
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|splits
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected [ip/prefix_length] but got ["
operator|+
name|mask
operator|+
literal|"], which contains zero or more than one [/]"
argument_list|)
throw|;
block|}
name|InetAddress
name|value
init|=
name|InetAddresses
operator|.
name|forString
argument_list|(
name|splits
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|int
name|prefixLength
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|splits
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
comment|// copied from InetAddressPoint.newPrefixQuery
if|if
condition|(
name|prefixLength
argument_list|<
literal|0
operator|||
name|prefixLength
argument_list|>
literal|8
operator|*
name|value
operator|.
name|getAddress
argument_list|()
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"illegal prefixLength ["
operator|+
name|prefixLength
operator|+
literal|"] in ["
operator|+
name|mask
operator|+
literal|"]. Must be 0-32 for IPv4 ranges, 0-128 for IPv6 ranges"
argument_list|)
throw|;
block|}
comment|// create the lower value by zeroing out the host portion, upper value by filling it with all ones.
name|byte
name|lower
index|[]
init|=
name|value
operator|.
name|getAddress
argument_list|()
decl_stmt|;
name|byte
name|upper
index|[]
init|=
name|value
operator|.
name|getAddress
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|prefixLength
init|;
name|i
operator|<
literal|8
operator|*
name|lower
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|m
init|=
literal|1
operator|<<
operator|(
literal|7
operator|-
operator|(
name|i
operator|&
literal|7
operator|)
operator|)
decl_stmt|;
name|lower
index|[
name|i
operator|>>
literal|3
index|]
operator|&=
operator|~
name|m
expr_stmt|;
name|upper
index|[
name|i
operator|>>
literal|3
index|]
operator||=
name|m
expr_stmt|;
block|}
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
try|try
block|{
name|InetAddress
name|fromAddress
init|=
name|InetAddress
operator|.
name|getByAddress
argument_list|(
name|lower
argument_list|)
decl_stmt|;
if|if
condition|(
name|fromAddress
operator|.
name|equals
argument_list|(
name|InetAddressPoint
operator|.
name|MIN_VALUE
argument_list|)
condition|)
block|{
name|this
operator|.
name|from
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|from
operator|=
name|InetAddresses
operator|.
name|toAddrString
argument_list|(
name|fromAddress
argument_list|)
expr_stmt|;
block|}
name|InetAddress
name|inclusiveToAddress
init|=
name|InetAddress
operator|.
name|getByAddress
argument_list|(
name|upper
argument_list|)
decl_stmt|;
if|if
condition|(
name|inclusiveToAddress
operator|.
name|equals
argument_list|(
name|InetAddressPoint
operator|.
name|MAX_VALUE
argument_list|)
condition|)
block|{
name|this
operator|.
name|to
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|to
operator|=
name|InetAddresses
operator|.
name|toAddrString
argument_list|(
name|InetAddressPoint
operator|.
name|nextUp
argument_list|(
name|inclusiveToAddress
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|bogus
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|bogus
argument_list|)
throw|;
block|}
block|}
DECL|method|Range
specifier|private
name|Range
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|key
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
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
name|writeOptionalString
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|from
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|to
argument_list|)
expr_stmt|;
block|}
DECL|method|getKey
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
DECL|method|getFrom
specifier|public
name|String
name|getFrom
parameter_list|()
block|{
return|return
name|from
return|;
block|}
DECL|method|getTo
specifier|public
name|String
name|getTo
parameter_list|()
block|{
return|return
name|to
return|;
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
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Range
name|that
init|=
operator|(
name|Range
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|key
argument_list|,
name|that
operator|.
name|key
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|from
argument_list|,
name|that
operator|.
name|from
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|to
argument_list|,
name|that
operator|.
name|to
argument_list|)
return|;
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
name|Objects
operator|.
name|hash
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|key
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|Range
operator|.
name|KEY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|from
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|Range
operator|.
name|FROM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|from
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|to
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|Range
operator|.
name|TO_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|to
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
DECL|field|keyed
specifier|private
name|boolean
name|keyed
init|=
literal|false
decl_stmt|;
DECL|field|ranges
specifier|private
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|IpRangeAggregationBuilder
specifier|public
name|IpRangeAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|ValuesSourceType
operator|.
name|BYTES
argument_list|,
name|ValueType
operator|.
name|IP
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
DECL|method|keyed
specifier|public
name|IpRangeAggregationBuilder
name|keyed
parameter_list|(
name|boolean
name|keyed
parameter_list|)
block|{
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|keyed
specifier|public
name|boolean
name|keyed
parameter_list|()
block|{
return|return
name|keyed
return|;
block|}
comment|/** Get the current list or ranges that are configured on this aggregation. */
DECL|method|getRanges
specifier|public
name|List
argument_list|<
name|Range
argument_list|>
name|getRanges
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|ranges
argument_list|)
return|;
block|}
comment|/** Add a new {@link Range} to this aggregation. */
DECL|method|addRange
specifier|public
name|IpRangeAggregationBuilder
name|addRange
parameter_list|(
name|Range
name|range
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
name|range
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a new range to this aggregation.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the distances, inclusive      * @param to      *            the upper bound on the distances, exclusive      */
DECL|method|addRange
specifier|public
name|IpRangeAggregationBuilder
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a new range to this aggregation using the CIDR notation.      */
DECL|method|addMaskRange
specifier|public
name|IpRangeAggregationBuilder
name|addMaskRange
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|mask
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|mask
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #addMaskRange(String, String)} but uses the mask itself as      * a key.      */
DECL|method|addMaskRange
specifier|public
name|IpRangeAggregationBuilder
name|addMaskRange
parameter_list|(
name|String
name|mask
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|mask
argument_list|,
name|mask
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but the key will be      * automatically generated.      */
DECL|method|addRange
specifier|public
name|IpRangeAggregationBuilder
name|addRange
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but there will be no      * lower bound.      */
DECL|method|addUnboundedTo
specifier|public
name|IpRangeAggregationBuilder
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedTo(String, String)} but the key will be      * generated automatically.      */
DECL|method|addUnboundedTo
specifier|public
name|IpRangeAggregationBuilder
name|addUnboundedTo
parameter_list|(
name|String
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but there will be no      * upper bound.      */
DECL|method|addUnboundedFrom
specifier|public
name|IpRangeAggregationBuilder
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|script
specifier|public
name|IpRangeAggregationBuilder
name|script
parameter_list|(
name|Script
name|script
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[ip_range] does not support scripts"
argument_list|)
throw|;
block|}
comment|/**      * Same as {@link #addUnboundedFrom(String, String)} but the key will be      * generated automatically.      */
DECL|method|addUnboundedFrom
specifier|public
name|IpRangeAggregationBuilder
name|addUnboundedFrom
parameter_list|(
name|String
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
argument_list|)
return|;
block|}
DECL|method|IpRangeAggregationBuilder
specifier|public
name|IpRangeAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|ValuesSourceType
operator|.
name|BYTES
argument_list|,
name|ValueType
operator|.
name|IP
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numRanges
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRanges
condition|;
operator|++
name|i
control|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|keyed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerWriteTo
specifier|protected
name|void
name|innerWriteTo
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
name|ranges
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Range
name|range
range|:
name|ranges
control|)
block|{
name|range
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
block|}
DECL|method|toBytesRef
specifier|private
specifier|static
name|BytesRef
name|toBytesRef
parameter_list|(
name|String
name|ip
parameter_list|)
block|{
if|if
condition|(
name|ip
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|InetAddress
name|address
init|=
name|InetAddresses
operator|.
name|forString
argument_list|(
name|ip
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|InetAddressPoint
operator|.
name|encode
argument_list|(
name|address
argument_list|)
decl_stmt|;
return|return
operator|new
name|BytesRef
argument_list|(
name|bytes
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerBuild
specifier|protected
name|ValuesSourceAggregatorFactory
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|,
name|?
argument_list|>
name|innerBuild
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|>
name|config
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|BinaryRangeAggregator
operator|.
name|Range
argument_list|>
name|ranges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|ranges
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No [ranges] specified for the ["
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|"] aggregation"
argument_list|)
throw|;
block|}
for|for
control|(
name|Range
name|range
range|:
name|this
operator|.
name|ranges
control|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|BinaryRangeAggregator
operator|.
name|Range
argument_list|(
name|range
operator|.
name|key
argument_list|,
name|toBytesRef
argument_list|(
name|range
operator|.
name|from
argument_list|)
argument_list|,
name|toBytesRef
argument_list|(
name|range
operator|.
name|to
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|BinaryRangeAggregatorFactory
argument_list|(
name|name
argument_list|,
name|config
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
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
block|{
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|RANGES_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ranges
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|KEYED_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|keyed
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|innerHashCode
specifier|protected
name|int
name|innerHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|keyed
argument_list|,
name|ranges
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerEquals
specifier|protected
name|boolean
name|innerEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|IpRangeAggregationBuilder
name|that
init|=
operator|(
name|IpRangeAggregationBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|keyed
operator|==
name|that
operator|.
name|keyed
operator|&&
name|ranges
operator|.
name|equals
argument_list|(
name|that
operator|.
name|ranges
argument_list|)
return|;
block|}
block|}
end_class

end_unit

