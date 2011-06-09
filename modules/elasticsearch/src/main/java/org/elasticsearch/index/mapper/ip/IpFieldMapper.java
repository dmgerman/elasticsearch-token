begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.ip
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|analysis
operator|.
name|NumericTokenStream
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
name|document
operator|.
name|Field
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
name|document
operator|.
name|Fieldable
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
name|search
operator|.
name|Filter
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
name|search
operator|.
name|FuzzyQuery
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
name|search
operator|.
name|NumericRangeFilter
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
name|search
operator|.
name|NumericRangeQuery
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
name|search
operator|.
name|Query
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
name|NumericUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|Numbers
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
name|Strings
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
name|index
operator|.
name|analysis
operator|.
name|NamedAnalyzer
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
name|analysis
operator|.
name|NumericAnalyzer
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
name|analysis
operator|.
name|NumericTokenizer
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
name|cache
operator|.
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|field
operator|.
name|data
operator|.
name|FieldDataType
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
name|Mapper
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
name|MapperParsingException
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
name|MergeContext
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
name|MergeMappingException
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
name|ParseContext
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
name|LongFieldMapper
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
name|search
operator|.
name|NumericRangeFieldDataFilter
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
name|io
operator|.
name|Reader
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperBuilders
operator|.
name|*
import|;
end_import

begin_import
import|import static
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
name|TypeParsers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|IpFieldMapper
specifier|public
class|class
name|IpFieldMapper
extends|extends
name|NumberFieldMapper
argument_list|<
name|Long
argument_list|>
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"ip"
decl_stmt|;
DECL|method|longToIp
specifier|public
specifier|static
name|String
name|longToIp
parameter_list|(
name|long
name|longIp
parameter_list|)
block|{
name|int
name|octet3
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|longIp
operator|>>
literal|24
operator|)
operator|%
literal|256
argument_list|)
decl_stmt|;
name|int
name|octet2
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|longIp
operator|>>
literal|16
operator|)
operator|%
literal|256
argument_list|)
decl_stmt|;
name|int
name|octet1
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|longIp
operator|>>
literal|8
operator|)
operator|%
literal|256
argument_list|)
decl_stmt|;
name|int
name|octet0
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|longIp
operator|)
operator|%
literal|256
argument_list|)
decl_stmt|;
return|return
name|octet3
operator|+
literal|"."
operator|+
name|octet2
operator|+
literal|"."
operator|+
name|octet1
operator|+
literal|"."
operator|+
name|octet0
return|;
block|}
DECL|field|pattern
specifier|private
specifier|static
specifier|final
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
DECL|method|ipToLong
specifier|public
specifier|static
name|long
name|ipToLong
parameter_list|(
name|String
name|ip
parameter_list|)
throws|throws
name|ElasticSearchIllegalArgumentException
block|{
try|try
block|{
name|String
index|[]
name|octets
init|=
name|pattern
operator|.
name|split
argument_list|(
name|ip
argument_list|)
decl_stmt|;
if|if
condition|(
name|octets
operator|.
name|length
operator|!=
literal|4
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to parse ip ["
operator|+
name|ip
operator|+
literal|"], not full ip address (4 dots)"
argument_list|)
throw|;
block|}
return|return
operator|(
name|Long
operator|.
name|parseLong
argument_list|(
name|octets
index|[
literal|0
index|]
argument_list|)
operator|<<
literal|24
operator|)
operator|+
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|octets
index|[
literal|1
index|]
argument_list|)
operator|<<
literal|16
operator|)
operator|+
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|octets
index|[
literal|2
index|]
argument_list|)
operator|<<
literal|8
operator|)
operator|+
name|Integer
operator|.
name|parseInt
argument_list|(
name|octets
index|[
literal|3
index|]
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ElasticSearchIllegalArgumentException
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchIllegalArgumentException
operator|)
name|e
throw|;
block|}
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to parse ip ["
operator|+
name|ip
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|NumberFieldMapper
operator|.
name|Defaults
block|{
DECL|field|NULL_VALUE
specifier|public
specifier|static
specifier|final
name|String
name|NULL_VALUE
init|=
literal|null
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|NumberFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|IpFieldMapper
argument_list|>
block|{
DECL|field|nullValue
specifier|protected
name|String
name|nullValue
init|=
name|Defaults
operator|.
name|NULL_VALUE
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|=
name|this
expr_stmt|;
block|}
DECL|method|nullValue
specifier|public
name|Builder
name|nullValue
parameter_list|(
name|String
name|nullValue
parameter_list|)
block|{
name|this
operator|.
name|nullValue
operator|=
name|nullValue
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
annotation|@
name|Override
specifier|public
name|IpFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|IpFieldMapper
name|fieldMapper
init|=
operator|new
name|IpFieldMapper
argument_list|(
name|buildNames
argument_list|(
name|context
argument_list|)
argument_list|,
name|precisionStep
argument_list|,
name|index
argument_list|,
name|store
argument_list|,
name|boost
argument_list|,
name|omitNorms
argument_list|,
name|omitTermFreqAndPositions
argument_list|,
name|nullValue
argument_list|)
decl_stmt|;
name|fieldMapper
operator|.
name|includeInAll
argument_list|(
name|includeInAll
argument_list|)
expr_stmt|;
return|return
name|fieldMapper
return|;
block|}
block|}
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
implements|implements
name|Mapper
operator|.
name|TypeParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|Mapper
operator|.
name|Builder
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|IpFieldMapper
operator|.
name|Builder
name|builder
init|=
name|ipField
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|parseNumberField
argument_list|(
name|builder
argument_list|,
name|name
argument_list|,
name|node
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|node
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|propName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|propNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"null_value"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|nullValue
argument_list|(
name|propNode
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|nullValue
specifier|private
name|String
name|nullValue
decl_stmt|;
DECL|method|IpFieldMapper
specifier|protected
name|IpFieldMapper
parameter_list|(
name|Names
name|names
parameter_list|,
name|int
name|precisionStep
parameter_list|,
name|Field
operator|.
name|Index
name|index
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|,
name|float
name|boost
parameter_list|,
name|boolean
name|omitNorms
parameter_list|,
name|boolean
name|omitTermFreqAndPositions
parameter_list|,
name|String
name|nullValue
parameter_list|)
block|{
name|super
argument_list|(
name|names
argument_list|,
name|precisionStep
argument_list|,
literal|null
argument_list|,
name|index
argument_list|,
name|store
argument_list|,
name|boost
argument_list|,
name|omitNorms
argument_list|,
name|omitTermFreqAndPositions
argument_list|,
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_ip/"
operator|+
name|precisionStep
argument_list|,
operator|new
name|NumericIpAnalyzer
argument_list|(
name|precisionStep
argument_list|)
argument_list|)
argument_list|,
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_ip/max"
argument_list|,
operator|new
name|NumericIpAnalyzer
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|nullValue
operator|=
name|nullValue
expr_stmt|;
block|}
DECL|method|maxPrecisionStep
annotation|@
name|Override
specifier|protected
name|int
name|maxPrecisionStep
parameter_list|()
block|{
return|return
literal|64
return|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|Long
name|value
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
name|byte
index|[]
name|value
init|=
name|field
operator|.
name|getBinaryValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Numbers
operator|.
name|bytesToLong
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|method|valueFromString
annotation|@
name|Override
specifier|public
name|Long
name|valueFromString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|ipToLong
argument_list|(
name|value
argument_list|)
return|;
block|}
comment|/**      * IPs should return as a string, delegates to {@link #valueAsString(org.apache.lucene.document.Fieldable)}.      */
DECL|method|valueForSearch
annotation|@
name|Override
specifier|public
name|Object
name|valueForSearch
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
return|return
name|valueAsString
argument_list|(
name|field
argument_list|)
return|;
block|}
DECL|method|valueAsString
annotation|@
name|Override
specifier|public
name|String
name|valueAsString
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
name|Long
name|value
init|=
name|value
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|longToIp
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|method|indexedValue
annotation|@
name|Override
specifier|public
name|String
name|indexedValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|NumericUtils
operator|.
name|longToPrefixCoded
argument_list|(
name|ipToLong
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
DECL|method|fuzzyQuery
annotation|@
name|Override
specifier|public
name|Query
name|fuzzyQuery
parameter_list|(
name|String
name|value
parameter_list|,
name|String
name|minSim
parameter_list|,
name|int
name|prefixLength
parameter_list|,
name|int
name|maxExpansions
parameter_list|)
block|{
name|long
name|iValue
init|=
name|ipToLong
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|long
name|iSim
decl_stmt|;
try|try
block|{
name|iSim
operator|=
name|ipToLong
argument_list|(
name|minSim
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticSearchIllegalArgumentException
name|e
parameter_list|)
block|{
try|try
block|{
name|iSim
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|minSim
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e1
parameter_list|)
block|{
name|iSim
operator|=
operator|(
name|long
operator|)
name|Double
operator|.
name|parseDouble
argument_list|(
name|minSim
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|NumericRangeQuery
operator|.
name|newLongRange
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|precisionStep
argument_list|,
name|iValue
operator|-
name|iSim
argument_list|,
name|iValue
operator|+
name|iSim
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|fuzzyQuery
annotation|@
name|Override
specifier|public
name|Query
name|fuzzyQuery
parameter_list|(
name|String
name|value
parameter_list|,
name|double
name|minSim
parameter_list|,
name|int
name|prefixLength
parameter_list|,
name|int
name|maxExpansions
parameter_list|)
block|{
return|return
operator|new
name|FuzzyQuery
argument_list|(
name|termFactory
operator|.
name|createTerm
argument_list|(
name|value
argument_list|)
argument_list|,
operator|(
name|float
operator|)
name|minSim
argument_list|,
name|prefixLength
argument_list|,
name|maxExpansions
argument_list|)
return|;
block|}
DECL|method|rangeQuery
annotation|@
name|Override
specifier|public
name|Query
name|rangeQuery
parameter_list|(
name|String
name|lowerTerm
parameter_list|,
name|String
name|upperTerm
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|)
block|{
return|return
name|NumericRangeQuery
operator|.
name|newLongRange
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|precisionStep
argument_list|,
name|lowerTerm
operator|==
literal|null
condition|?
literal|null
else|:
name|ipToLong
argument_list|(
name|lowerTerm
argument_list|)
argument_list|,
name|upperTerm
operator|==
literal|null
condition|?
literal|null
else|:
name|ipToLong
argument_list|(
name|upperTerm
argument_list|)
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|)
return|;
block|}
DECL|method|rangeFilter
annotation|@
name|Override
specifier|public
name|Filter
name|rangeFilter
parameter_list|(
name|String
name|lowerTerm
parameter_list|,
name|String
name|upperTerm
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|)
block|{
return|return
name|NumericRangeFilter
operator|.
name|newLongRange
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|precisionStep
argument_list|,
name|lowerTerm
operator|==
literal|null
condition|?
literal|null
else|:
name|ipToLong
argument_list|(
name|lowerTerm
argument_list|)
argument_list|,
name|upperTerm
operator|==
literal|null
condition|?
literal|null
else|:
name|ipToLong
argument_list|(
name|upperTerm
argument_list|)
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|)
return|;
block|}
DECL|method|rangeFilter
annotation|@
name|Override
specifier|public
name|Filter
name|rangeFilter
parameter_list|(
name|FieldDataCache
name|fieldDataCache
parameter_list|,
name|String
name|lowerTerm
parameter_list|,
name|String
name|upperTerm
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|)
block|{
return|return
name|NumericRangeFieldDataFilter
operator|.
name|newLongRange
argument_list|(
name|fieldDataCache
argument_list|,
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|lowerTerm
operator|==
literal|null
condition|?
literal|null
else|:
name|ipToLong
argument_list|(
name|lowerTerm
argument_list|)
argument_list|,
name|upperTerm
operator|==
literal|null
condition|?
literal|null
else|:
name|ipToLong
argument_list|(
name|upperTerm
argument_list|)
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|)
return|;
block|}
DECL|method|parseCreateField
annotation|@
name|Override
specifier|protected
name|Fieldable
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|ipAsString
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|externalValueSet
argument_list|()
condition|)
block|{
name|ipAsString
operator|=
operator|(
name|String
operator|)
name|context
operator|.
name|externalValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|ipAsString
operator|==
literal|null
condition|)
block|{
name|ipAsString
operator|=
name|nullValue
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
name|ipAsString
operator|=
name|nullValue
expr_stmt|;
block|}
else|else
block|{
name|ipAsString
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ipAsString
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|context
operator|.
name|includeInAll
argument_list|(
name|includeInAll
argument_list|)
condition|)
block|{
name|context
operator|.
name|allEntries
argument_list|()
operator|.
name|addText
argument_list|(
name|names
operator|.
name|fullName
argument_list|()
argument_list|,
name|ipAsString
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|value
init|=
name|ipToLong
argument_list|(
name|ipAsString
argument_list|)
decl_stmt|;
return|return
operator|new
name|LongFieldMapper
operator|.
name|CustomLongNumericField
argument_list|(
name|this
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|fieldDataType
annotation|@
name|Override
specifier|public
name|FieldDataType
name|fieldDataType
parameter_list|()
block|{
return|return
name|FieldDataType
operator|.
name|DefaultTypes
operator|.
name|LONG
return|;
block|}
DECL|method|contentType
annotation|@
name|Override
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
name|super
operator|.
name|merge
argument_list|(
name|mergeWith
argument_list|,
name|mergeContext
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|mergeWith
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|mergeContext
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|()
condition|)
block|{
name|this
operator|.
name|nullValue
operator|=
operator|(
operator|(
name|IpFieldMapper
operator|)
name|mergeWith
operator|)
operator|.
name|nullValue
expr_stmt|;
block|}
block|}
DECL|method|doXContentBody
annotation|@
name|Override
specifier|protected
name|void
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doXContentBody
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|index
operator|!=
name|Defaults
operator|.
name|INDEX
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|index
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|store
operator|!=
name|Defaults
operator|.
name|STORE
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
name|store
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|termVector
operator|!=
name|Defaults
operator|.
name|TERM_VECTOR
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"term_vector"
argument_list|,
name|termVector
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|omitNorms
operator|!=
name|Defaults
operator|.
name|OMIT_NORMS
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"omit_norms"
argument_list|,
name|omitNorms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|omitTermFreqAndPositions
operator|!=
name|Defaults
operator|.
name|OMIT_TERM_FREQ_AND_POSITIONS
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"omit_term_freq_and_positions"
argument_list|,
name|omitTermFreqAndPositions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|precisionStep
operator|!=
name|Defaults
operator|.
name|PRECISION_STEP
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"precision_step"
argument_list|,
name|precisionStep
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nullValue
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"null_value"
argument_list|,
name|nullValue
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"include_in_all"
argument_list|,
name|includeInAll
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NumericIpAnalyzer
specifier|public
specifier|static
class|class
name|NumericIpAnalyzer
extends|extends
name|NumericAnalyzer
argument_list|<
name|NumericIpTokenizer
argument_list|>
block|{
DECL|field|precisionStep
specifier|private
specifier|final
name|int
name|precisionStep
decl_stmt|;
DECL|method|NumericIpAnalyzer
specifier|public
name|NumericIpAnalyzer
parameter_list|()
block|{
name|this
argument_list|(
name|NumericUtils
operator|.
name|PRECISION_STEP_DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|NumericIpAnalyzer
specifier|public
name|NumericIpAnalyzer
parameter_list|(
name|int
name|precisionStep
parameter_list|)
block|{
name|this
operator|.
name|precisionStep
operator|=
name|precisionStep
expr_stmt|;
block|}
DECL|method|createNumericTokenizer
annotation|@
name|Override
specifier|protected
name|NumericIpTokenizer
name|createNumericTokenizer
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|char
index|[]
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|NumericIpTokenizer
argument_list|(
name|reader
argument_list|,
name|precisionStep
argument_list|,
name|buffer
argument_list|)
return|;
block|}
block|}
DECL|class|NumericIpTokenizer
specifier|public
specifier|static
class|class
name|NumericIpTokenizer
extends|extends
name|NumericTokenizer
block|{
DECL|method|NumericIpTokenizer
specifier|public
name|NumericIpTokenizer
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|int
name|precisionStep
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|reader
argument_list|,
operator|new
name|NumericTokenStream
argument_list|(
name|precisionStep
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|NumericIpTokenizer
specifier|public
name|NumericIpTokenizer
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|int
name|precisionStep
parameter_list|,
name|char
index|[]
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|reader
argument_list|,
operator|new
name|NumericTokenStream
argument_list|(
name|precisionStep
argument_list|)
argument_list|,
name|buffer
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|setValue
annotation|@
name|Override
specifier|protected
name|void
name|setValue
parameter_list|(
name|NumericTokenStream
name|tokenStream
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|tokenStream
operator|.
name|setLongValue
argument_list|(
name|ipToLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

