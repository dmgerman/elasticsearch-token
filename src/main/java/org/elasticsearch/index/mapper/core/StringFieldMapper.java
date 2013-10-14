begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
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
name|Analyzer
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
name|analysis
operator|.
name|TokenStream
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|OffsetAttribute
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
name|FieldType
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
name|SortedSetDocValuesField
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
name|index
operator|.
name|FieldInfo
operator|.
name|IndexOptions
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
name|settings
operator|.
name|Settings
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
name|support
operator|.
name|XContentMapValues
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
name|codec
operator|.
name|docvaluesformat
operator|.
name|DocValuesFormatProvider
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatProvider
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
name|*
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
name|internal
operator|.
name|AllFieldMapper
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
name|similarity
operator|.
name|SimilarityProvider
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
name|List
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
name|stringField
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
name|parseField
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|StringFieldMapper
specifier|public
class|class
name|StringFieldMapper
extends|extends
name|AbstractFieldMapper
argument_list|<
name|String
argument_list|>
implements|implements
name|AllFieldMapper
operator|.
name|IncludeInAll
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"string"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|AbstractFieldMapper
operator|.
name|Defaults
block|{
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|FIELD_TYPE
init|=
operator|new
name|FieldType
argument_list|(
name|AbstractFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
comment|// NOTE, when adding defaults here, make sure you add them in the builder
DECL|field|NULL_VALUE
specifier|public
specifier|static
specifier|final
name|String
name|NULL_VALUE
init|=
literal|null
decl_stmt|;
DECL|field|POSITION_OFFSET_GAP
specifier|public
specifier|static
specifier|final
name|int
name|POSITION_OFFSET_GAP
init|=
literal|0
decl_stmt|;
DECL|field|IGNORE_ABOVE
specifier|public
specifier|static
specifier|final
name|int
name|IGNORE_ABOVE
init|=
operator|-
literal|1
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|AbstractFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|StringFieldMapper
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
DECL|field|positionOffsetGap
specifier|protected
name|int
name|positionOffsetGap
init|=
name|Defaults
operator|.
name|POSITION_OFFSET_GAP
decl_stmt|;
DECL|field|searchQuotedAnalyzer
specifier|protected
name|NamedAnalyzer
name|searchQuotedAnalyzer
decl_stmt|;
DECL|field|ignoreAbove
specifier|protected
name|int
name|ignoreAbove
init|=
name|Defaults
operator|.
name|IGNORE_ABOVE
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
argument_list|,
operator|new
name|FieldType
argument_list|(
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
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
annotation|@
name|Override
DECL|method|searchAnalyzer
specifier|public
name|Builder
name|searchAnalyzer
parameter_list|(
name|NamedAnalyzer
name|searchAnalyzer
parameter_list|)
block|{
name|super
operator|.
name|searchAnalyzer
argument_list|(
name|searchAnalyzer
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchQuotedAnalyzer
operator|==
literal|null
condition|)
block|{
name|searchQuotedAnalyzer
operator|=
name|searchAnalyzer
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|positionOffsetGap
specifier|public
name|Builder
name|positionOffsetGap
parameter_list|(
name|int
name|positionOffsetGap
parameter_list|)
block|{
name|this
operator|.
name|positionOffsetGap
operator|=
name|positionOffsetGap
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|searchQuotedAnalyzer
specifier|public
name|Builder
name|searchQuotedAnalyzer
parameter_list|(
name|NamedAnalyzer
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|searchQuotedAnalyzer
operator|=
name|analyzer
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|ignoreAbove
specifier|public
name|Builder
name|ignoreAbove
parameter_list|(
name|int
name|ignoreAbove
parameter_list|)
block|{
name|this
operator|.
name|ignoreAbove
operator|=
name|ignoreAbove
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|StringFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|positionOffsetGap
operator|>
literal|0
condition|)
block|{
name|indexAnalyzer
operator|=
operator|new
name|NamedAnalyzer
argument_list|(
name|indexAnalyzer
argument_list|,
name|positionOffsetGap
argument_list|)
expr_stmt|;
name|searchAnalyzer
operator|=
operator|new
name|NamedAnalyzer
argument_list|(
name|searchAnalyzer
argument_list|,
name|positionOffsetGap
argument_list|)
expr_stmt|;
name|searchQuotedAnalyzer
operator|=
operator|new
name|NamedAnalyzer
argument_list|(
name|searchQuotedAnalyzer
argument_list|,
name|positionOffsetGap
argument_list|)
expr_stmt|;
block|}
comment|// if the field is not analyzed, then by default, we should omit norms and have docs only
comment|// index options, as probably what the user really wants
comment|// if they are set explicitly, we will use those values
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|&&
operator|!
name|fieldType
operator|.
name|tokenized
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|omitNormsSet
operator|&&
name|boost
operator|==
name|Defaults
operator|.
name|BOOST
condition|)
block|{
name|fieldType
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|indexOptionsSet
condition|)
block|{
name|fieldType
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS_ONLY
argument_list|)
expr_stmt|;
block|}
block|}
name|StringFieldMapper
name|fieldMapper
init|=
operator|new
name|StringFieldMapper
argument_list|(
name|buildNames
argument_list|(
name|context
argument_list|)
argument_list|,
name|boost
argument_list|,
name|fieldType
argument_list|,
name|nullValue
argument_list|,
name|indexAnalyzer
argument_list|,
name|searchAnalyzer
argument_list|,
name|searchQuotedAnalyzer
argument_list|,
name|positionOffsetGap
argument_list|,
name|ignoreAbove
argument_list|,
name|postingsProvider
argument_list|,
name|docValuesProvider
argument_list|,
name|similarity
argument_list|,
name|fieldDataSettings
argument_list|,
name|context
operator|.
name|indexSettings
argument_list|()
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
annotation|@
name|Override
DECL|method|parse
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
name|StringFieldMapper
operator|.
name|Builder
name|builder
init|=
name|stringField
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|parseField
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
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"search_quote_analyzer"
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|propNode
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Analyzer ["
operator|+
name|propNode
operator|.
name|toString
argument_list|()
operator|+
literal|"] not found for field ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|searchQuotedAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"position_offset_gap"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|positionOffsetGap
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|propNode
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// we need to update to actual analyzers if they are not set in this case...
comment|// so we can inject the position offset gap...
if|if
condition|(
name|builder
operator|.
name|indexAnalyzer
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|indexAnalyzer
operator|=
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultIndexAnalyzer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|searchAnalyzer
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|searchAnalyzer
operator|=
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultSearchAnalyzer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|searchQuotedAnalyzer
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|searchQuotedAnalyzer
operator|=
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"ignore_above"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|ignoreAbove
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|propNode
argument_list|,
operator|-
literal|1
argument_list|)
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
DECL|field|includeInAll
specifier|private
name|Boolean
name|includeInAll
decl_stmt|;
DECL|field|positionOffsetGap
specifier|private
name|int
name|positionOffsetGap
decl_stmt|;
DECL|field|searchQuotedAnalyzer
specifier|private
name|NamedAnalyzer
name|searchQuotedAnalyzer
decl_stmt|;
DECL|field|ignoreAbove
specifier|private
name|int
name|ignoreAbove
decl_stmt|;
DECL|method|StringFieldMapper
specifier|protected
name|StringFieldMapper
parameter_list|(
name|Names
name|names
parameter_list|,
name|float
name|boost
parameter_list|,
name|FieldType
name|fieldType
parameter_list|,
name|String
name|nullValue
parameter_list|,
name|NamedAnalyzer
name|indexAnalyzer
parameter_list|,
name|NamedAnalyzer
name|searchAnalyzer
parameter_list|,
name|NamedAnalyzer
name|searchQuotedAnalyzer
parameter_list|,
name|int
name|positionOffsetGap
parameter_list|,
name|int
name|ignoreAbove
parameter_list|,
name|PostingsFormatProvider
name|postingsFormat
parameter_list|,
name|DocValuesFormatProvider
name|docValuesFormat
parameter_list|,
name|SimilarityProvider
name|similarity
parameter_list|,
annotation|@
name|Nullable
name|Settings
name|fieldDataSettings
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|names
argument_list|,
name|boost
argument_list|,
name|fieldType
argument_list|,
name|indexAnalyzer
argument_list|,
name|searchAnalyzer
argument_list|,
name|postingsFormat
argument_list|,
name|docValuesFormat
argument_list|,
name|similarity
argument_list|,
name|fieldDataSettings
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldType
operator|.
name|tokenized
argument_list|()
operator|&&
name|fieldType
operator|.
name|indexed
argument_list|()
operator|&&
name|hasDocValues
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Field ["
operator|+
name|names
operator|.
name|fullName
argument_list|()
operator|+
literal|"] cannot be analyzed and have doc values"
argument_list|)
throw|;
block|}
name|this
operator|.
name|nullValue
operator|=
name|nullValue
expr_stmt|;
name|this
operator|.
name|positionOffsetGap
operator|=
name|positionOffsetGap
expr_stmt|;
name|this
operator|.
name|searchQuotedAnalyzer
operator|=
name|searchQuotedAnalyzer
operator|!=
literal|null
condition|?
name|searchQuotedAnalyzer
else|:
name|this
operator|.
name|searchAnalyzer
expr_stmt|;
name|this
operator|.
name|ignoreAbove
operator|=
name|ignoreAbove
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldType
specifier|public
name|FieldType
name|defaultFieldType
parameter_list|()
block|{
return|return
name|Defaults
operator|.
name|FIELD_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldDataType
specifier|public
name|FieldDataType
name|defaultFieldDataType
parameter_list|()
block|{
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"string"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|includeInAll
specifier|public
name|void
name|includeInAll
parameter_list|(
name|Boolean
name|includeInAll
parameter_list|)
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|includeInAllIfNotSet
specifier|public
name|void
name|includeInAllIfNotSet
parameter_list|(
name|Boolean
name|includeInAll
parameter_list|)
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
operator|&&
name|this
operator|.
name|includeInAll
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|String
name|value
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
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
name|value
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|customBoost
specifier|protected
name|boolean
name|customBoost
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|getPositionOffsetGap
specifier|public
name|int
name|getPositionOffsetGap
parameter_list|()
block|{
return|return
name|this
operator|.
name|positionOffsetGap
return|;
block|}
annotation|@
name|Override
DECL|method|searchQuoteAnalyzer
specifier|public
name|Analyzer
name|searchQuoteAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchQuotedAnalyzer
return|;
block|}
annotation|@
name|Override
DECL|method|nullValueFilter
specifier|public
name|Filter
name|nullValueFilter
parameter_list|()
block|{
if|if
condition|(
name|nullValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|termFilter
argument_list|(
name|nullValue
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|value
init|=
name|nullValue
decl_stmt|;
name|float
name|boost
init|=
name|this
operator|.
name|boost
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|externalValueSet
argument_list|()
condition|)
block|{
name|value
operator|=
operator|(
name|String
operator|)
name|context
operator|.
name|externalValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
if|if
condition|(
name|parser
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
name|value
operator|=
name|nullValue
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
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
else|else
block|{
if|if
condition|(
literal|"value"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"_value"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|value
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
literal|"boost"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"_boost"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"unknown property ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
else|else
block|{
name|value
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|ignoreAbove
operator|>
literal|0
operator|&&
name|value
operator|.
name|length
argument_list|()
operator|>
name|ignoreAbove
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|includeInAll
argument_list|(
name|includeInAll
argument_list|,
name|this
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
name|value
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldType
operator|.
name|indexed
argument_list|()
operator|||
name|fieldType
operator|.
name|stored
argument_list|()
condition|)
block|{
name|Field
name|field
init|=
operator|new
name|StringField
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|value
argument_list|,
name|fieldType
argument_list|)
decl_stmt|;
name|field
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasDocValues
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|SortedSetDocValuesField
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|context
operator|.
name|ignoredValue
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|merge
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
name|includeInAll
operator|=
operator|(
operator|(
name|StringFieldMapper
operator|)
name|mergeWith
operator|)
operator|.
name|includeInAll
expr_stmt|;
name|this
operator|.
name|nullValue
operator|=
operator|(
operator|(
name|StringFieldMapper
operator|)
name|mergeWith
operator|)
operator|.
name|nullValue
expr_stmt|;
name|this
operator|.
name|ignoreAbove
operator|=
operator|(
operator|(
name|StringFieldMapper
operator|)
name|mergeWith
operator|)
operator|.
name|ignoreAbove
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|void
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|boolean
name|includeDefaults
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doXContentBody
argument_list|(
name|builder
argument_list|,
name|includeDefaults
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeDefaults
operator|||
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
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"include_in_all"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
name|positionOffsetGap
operator|!=
name|Defaults
operator|.
name|POSITION_OFFSET_GAP
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"position_offset_gap"
argument_list|,
name|positionOffsetGap
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchQuotedAnalyzer
operator|!=
literal|null
operator|&&
name|searchAnalyzer
operator|!=
name|searchQuotedAnalyzer
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"search_quote_analyzer"
argument_list|,
name|searchQuotedAnalyzer
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
if|if
condition|(
name|searchQuotedAnalyzer
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"search_quote_analyzer"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"search_quote_analyzer"
argument_list|,
name|searchQuotedAnalyzer
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|includeDefaults
operator|||
name|ignoreAbove
operator|!=
name|Defaults
operator|.
name|IGNORE_ABOVE
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_above"
argument_list|,
name|ignoreAbove
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Extension of {@link Field} supporting reuse of a cached TokenStream for not-tokenized values. */
DECL|class|StringField
specifier|static
class|class
name|StringField
extends|extends
name|Field
block|{
DECL|method|StringField
specifier|public
name|StringField
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|,
name|FieldType
name|fieldType
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|fieldsData
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|tokenStream
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fieldType
argument_list|()
operator|.
name|indexed
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Only use the cached TokenStream if the value is indexed and not-tokenized
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|tokenized
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|tokenStream
argument_list|(
name|analyzer
argument_list|)
return|;
block|}
return|return
name|NOT_ANALYZED_TOKENSTREAM
operator|.
name|get
argument_list|()
operator|.
name|setValue
argument_list|(
operator|(
name|String
operator|)
name|fieldsData
argument_list|)
return|;
block|}
block|}
DECL|field|NOT_ANALYZED_TOKENSTREAM
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|StringTokenStream
argument_list|>
name|NOT_ANALYZED_TOKENSTREAM
init|=
operator|new
name|ThreadLocal
argument_list|<
name|StringTokenStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|StringTokenStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|StringTokenStream
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|// Copied from Field.java
DECL|class|StringTokenStream
specifier|static
specifier|final
class|class
name|StringTokenStream
extends|extends
name|TokenStream
block|{
DECL|field|termAttribute
specifier|private
specifier|final
name|CharTermAttribute
name|termAttribute
init|=
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|offsetAttribute
specifier|private
specifier|final
name|OffsetAttribute
name|offsetAttribute
init|=
name|addAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|used
specifier|private
name|boolean
name|used
init|=
literal|false
decl_stmt|;
DECL|field|value
specifier|private
name|String
name|value
init|=
literal|null
decl_stmt|;
comment|/**          * Creates a new TokenStream that returns a String as single token.          *<p>Warning: Does not initialize the value, you must call          * {@link #setValue(String)} afterwards!          */
DECL|method|StringTokenStream
name|StringTokenStream
parameter_list|()
block|{         }
comment|/** Sets the string value. */
DECL|method|setValue
name|StringTokenStream
name|setValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
name|boolean
name|incrementToken
parameter_list|()
block|{
if|if
condition|(
name|used
condition|)
block|{
return|return
literal|false
return|;
block|}
name|clearAttributes
argument_list|()
expr_stmt|;
name|termAttribute
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|offsetAttribute
operator|.
name|setOffset
argument_list|(
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|used
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|end
specifier|public
name|void
name|end
parameter_list|()
block|{
specifier|final
name|int
name|finalOffset
init|=
name|value
operator|.
name|length
argument_list|()
decl_stmt|;
name|offsetAttribute
operator|.
name|setOffset
argument_list|(
name|finalOffset
argument_list|,
name|finalOffset
argument_list|)
expr_stmt|;
name|value
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|used
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|value
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

