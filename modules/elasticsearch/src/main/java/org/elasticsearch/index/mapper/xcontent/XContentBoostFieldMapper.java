begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|xcontent
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
name|*
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
name|builder
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
name|NumericFloatAnalyzer
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
name|FieldData
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
name|BoostFieldMapper
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|XContentBoostFieldMapper
specifier|public
class|class
name|XContentBoostFieldMapper
extends|extends
name|XContentNumberFieldMapper
argument_list|<
name|Float
argument_list|>
implements|implements
name|BoostFieldMapper
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_boost"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|XContentNumberFieldMapper
operator|.
name|Defaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"_boost"
decl_stmt|;
DECL|field|NULL_VALUE
specifier|public
specifier|static
specifier|final
name|Float
name|NULL_VALUE
init|=
literal|null
decl_stmt|;
DECL|field|INDEX
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Index
name|INDEX
init|=
name|Field
operator|.
name|Index
operator|.
name|NO
decl_stmt|;
DECL|field|STORE
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Store
name|STORE
init|=
name|Field
operator|.
name|Store
operator|.
name|NO
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|XContentNumberFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|XContentBoostFieldMapper
argument_list|>
block|{
DECL|field|nullValue
specifier|protected
name|Float
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
name|index
operator|=
name|Defaults
operator|.
name|INDEX
expr_stmt|;
name|store
operator|=
name|Defaults
operator|.
name|STORE
expr_stmt|;
block|}
DECL|method|nullValue
specifier|public
name|Builder
name|nullValue
parameter_list|(
name|float
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
name|XContentBoostFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|XContentBoostFieldMapper
argument_list|(
name|name
argument_list|,
name|buildIndexName
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
return|;
block|}
block|}
DECL|field|nullValue
specifier|private
specifier|final
name|Float
name|nullValue
decl_stmt|;
DECL|method|XContentBoostFieldMapper
specifier|protected
name|XContentBoostFieldMapper
parameter_list|()
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
DECL|method|XContentBoostFieldMapper
specifier|protected
name|XContentBoostFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|Defaults
operator|.
name|PRECISION_STEP
argument_list|,
name|Defaults
operator|.
name|INDEX
argument_list|,
name|Defaults
operator|.
name|STORE
argument_list|,
name|Defaults
operator|.
name|BOOST
argument_list|,
name|Defaults
operator|.
name|OMIT_NORMS
argument_list|,
name|Defaults
operator|.
name|OMIT_TERM_FREQ_AND_POSITIONS
argument_list|,
name|Defaults
operator|.
name|NULL_VALUE
argument_list|)
expr_stmt|;
block|}
DECL|method|XContentBoostFieldMapper
specifier|protected
name|XContentBoostFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
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
name|Float
name|nullValue
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|Names
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|indexName
argument_list|,
name|name
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
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_float/"
operator|+
name|precisionStep
argument_list|,
operator|new
name|NumericFloatAnalyzer
argument_list|(
name|precisionStep
argument_list|)
argument_list|)
argument_list|,
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_float/max"
argument_list|,
operator|new
name|NumericFloatAnalyzer
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
literal|32
return|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|Float
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
name|bytesToFloat
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
name|indexedValue
argument_list|(
name|Float
operator|.
name|parseFloat
argument_list|(
name|value
argument_list|)
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
name|Float
name|value
parameter_list|)
block|{
return|return
name|NumericUtils
operator|.
name|floatToPrefixCoded
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|method|valueFromTerm
annotation|@
name|Override
specifier|public
name|Object
name|valueFromTerm
parameter_list|(
name|String
name|term
parameter_list|)
block|{
specifier|final
name|int
name|shift
init|=
name|term
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|-
name|NumericUtils
operator|.
name|SHIFT_START_INT
decl_stmt|;
if|if
condition|(
name|shift
operator|>
literal|0
operator|&&
name|shift
operator|<=
literal|31
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|NumericUtils
operator|.
name|prefixCodedToFloat
argument_list|(
name|term
argument_list|)
return|;
block|}
DECL|method|valueFromString
annotation|@
name|Override
specifier|public
name|Object
name|valueFromString
parameter_list|(
name|String
name|text
parameter_list|)
block|{
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|text
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
name|newFloatRange
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
name|Float
operator|.
name|parseFloat
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
name|Float
operator|.
name|parseFloat
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
name|newFloatRange
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
name|Float
operator|.
name|parseFloat
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
name|Float
operator|.
name|parseFloat
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
DECL|method|parse
annotation|@
name|Override
specifier|public
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we override parse since we want to handle cases where it is not indexed and not stored (the default)
name|float
name|value
init|=
name|parseFloatValue
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Float
operator|.
name|isNaN
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|context
operator|.
name|doc
argument_list|()
operator|.
name|setBoost
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
DECL|method|parseCreateField
annotation|@
name|Override
specifier|protected
name|Field
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|float
name|value
init|=
name|parseFloatValue
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|Float
operator|.
name|isNaN
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|context
operator|.
name|doc
argument_list|()
operator|.
name|setBoost
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|Field
name|field
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|stored
argument_list|()
condition|)
block|{
name|field
operator|=
operator|new
name|Field
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|Numbers
operator|.
name|floatToBytes
argument_list|(
name|value
argument_list|)
argument_list|,
name|store
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexed
argument_list|()
condition|)
block|{
name|field
operator|.
name|setTokenStream
argument_list|(
name|popCachedStream
argument_list|(
name|precisionStep
argument_list|)
operator|.
name|setFloatValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|indexed
argument_list|()
condition|)
block|{
name|field
operator|=
operator|new
name|Field
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|popCachedStream
argument_list|(
name|precisionStep
argument_list|)
operator|.
name|setFloatValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|field
return|;
block|}
DECL|method|parseFloatValue
specifier|private
name|float
name|parseFloatValue
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|float
name|value
decl_stmt|;
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
if|if
condition|(
name|nullValue
operator|==
literal|null
condition|)
block|{
return|return
name|Float
operator|.
name|NaN
return|;
block|}
name|value
operator|=
name|nullValue
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
DECL|method|sortType
annotation|@
name|Override
specifier|public
name|int
name|sortType
parameter_list|()
block|{
return|return
name|SortField
operator|.
name|FLOAT
return|;
block|}
DECL|method|fieldDataType
annotation|@
name|Override
specifier|public
name|FieldData
operator|.
name|Type
name|fieldDataType
parameter_list|()
block|{
return|return
name|FieldData
operator|.
name|Type
operator|.
name|FLOAT
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
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
argument_list|(
name|contentType
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|name
argument_list|()
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|XContentMapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
comment|// do nothing here, no merging, but also no exception
block|}
block|}
end_class

end_unit

