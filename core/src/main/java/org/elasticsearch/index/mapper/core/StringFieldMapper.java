begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|mapper
operator|.
name|FieldMapper
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
name|internal
operator|.
name|AllFieldMapper
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
name|Iterator
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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexOptions
operator|.
name|NONE
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
name|parseMultiField
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
name|parseTextField
import|;
end_import

begin_class
DECL|class|StringFieldMapper
specifier|public
class|class
name|StringFieldMapper
extends|extends
name|FieldMapper
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
DECL|field|POSITION_INCREMENT_GAP_USE_ANALYZER
specifier|private
specifier|static
specifier|final
name|int
name|POSITION_INCREMENT_GAP_USE_ANALYZER
init|=
operator|-
literal|1
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|MappedFieldType
name|FIELD_TYPE
init|=
operator|new
name|StringFieldType
argument_list|()
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
name|FieldMapper
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
comment|/**          * The distance between tokens from different values in the same field.          * POSITION_INCREMENT_GAP_USE_ANALYZER means default to the analyzer's          * setting which in turn defaults to Defaults.POSITION_INCREMENT_GAP.          */
DECL|field|positionIncrementGap
specifier|protected
name|int
name|positionIncrementGap
init|=
name|POSITION_INCREMENT_GAP_USE_ANALYZER
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
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
expr_stmt|;
name|builder
operator|=
name|this
expr_stmt|;
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
return|return
name|this
return|;
block|}
DECL|method|positionIncrementGap
specifier|public
name|Builder
name|positionIncrementGap
parameter_list|(
name|int
name|positionIncrementGap
parameter_list|)
block|{
name|this
operator|.
name|positionIncrementGap
operator|=
name|positionIncrementGap
expr_stmt|;
return|return
name|this
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
name|positionIncrementGap
operator|!=
name|POSITION_INCREMENT_GAP_USE_ANALYZER
condition|)
block|{
name|fieldType
operator|.
name|setIndexAnalyzer
argument_list|(
operator|new
name|NamedAnalyzer
argument_list|(
name|fieldType
operator|.
name|indexAnalyzer
argument_list|()
argument_list|,
name|positionIncrementGap
argument_list|)
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setSearchAnalyzer
argument_list|(
operator|new
name|NamedAnalyzer
argument_list|(
name|fieldType
operator|.
name|searchAnalyzer
argument_list|()
argument_list|,
name|positionIncrementGap
argument_list|)
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setSearchQuoteAnalyzer
argument_list|(
operator|new
name|NamedAnalyzer
argument_list|(
name|fieldType
operator|.
name|searchQuoteAnalyzer
argument_list|()
argument_list|,
name|positionIncrementGap
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// if the field is not analyzed, then by default, we should omit norms and have docs only
comment|// index options, as probably what the user really wants
comment|// if they are set explicitly, we will use those values
comment|// we also change the values on the default field type so that toXContent emits what
comment|// differs from the defaults
if|if
condition|(
name|fieldType
operator|.
name|indexOptions
argument_list|()
operator|!=
name|IndexOptions
operator|.
name|NONE
operator|&&
operator|!
name|fieldType
operator|.
name|tokenized
argument_list|()
condition|)
block|{
name|defaultFieldType
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|defaultFieldType
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|omitNormsSet
operator|&&
name|fieldType
operator|.
name|boost
argument_list|()
operator|==
literal|1.0f
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
name|DOCS
argument_list|)
expr_stmt|;
block|}
block|}
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|StringFieldMapper
name|fieldMapper
init|=
operator|new
name|StringFieldMapper
argument_list|(
name|name
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|positionIncrementGap
argument_list|,
name|ignoreAbove
argument_list|,
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|,
name|multiFieldsBuilder
operator|.
name|build
argument_list|(
name|this
argument_list|,
name|context
argument_list|)
argument_list|,
name|copyTo
argument_list|)
decl_stmt|;
return|return
name|fieldMapper
operator|.
name|includeInAll
argument_list|(
name|includeInAll
argument_list|)
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
name|fieldName
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
comment|// TODO: temporarily disabled to give Kibana time to upgrade to text/keyword mappings
comment|/*if (parserContext.indexVersionCreated().onOrAfter(Version.V_5_0_0)) {                 throw new IllegalArgumentException("The [string] type is removed in 5.0. You should now use either a [text] "                         + "or [keyword] field instead for field [" + fieldName + "]");             }*/
name|StringFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|StringFieldMapper
operator|.
name|Builder
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
comment|// hack for the fact that string can't just accept true/false for
comment|// the index property and still accepts no/not_analyzed/analyzed
specifier|final
name|Object
name|index
init|=
name|node
operator|.
name|remove
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
literal|null
condition|)
block|{
specifier|final
name|String
name|normalizedIndex
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|index
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|normalizedIndex
condition|)
block|{
case|case
literal|"analyzed"
case|:
name|builder
operator|.
name|tokenized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|node
operator|.
name|put
argument_list|(
literal|"index"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"not_analyzed"
case|:
name|builder
operator|.
name|tokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|node
operator|.
name|put
argument_list|(
literal|"index"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"no"
case|:
name|node
operator|.
name|put
argument_list|(
literal|"index"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse [index] value ["
operator|+
name|index
operator|+
literal|"] for field ["
operator|+
name|fieldName
operator|+
literal|"], expected [true], [false], [no], [not_analyzed] or [analyzed]"
argument_list|)
throw|;
block|}
block|}
name|parseTextField
argument_list|(
name|builder
argument_list|,
name|fieldName
argument_list|,
name|node
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
init|=
name|node
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
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
if|if
condition|(
name|propNode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Property [null_value] cannot be null."
argument_list|)
throw|;
block|}
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
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"position_increment_gap"
argument_list|)
condition|)
block|{
name|int
name|newPositionIncrementGap
init|=
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|propNode
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|newPositionIncrementGap
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"positions_increment_gap less than 0 aren't allowed."
argument_list|)
throw|;
block|}
name|builder
operator|.
name|positionIncrementGap
argument_list|(
name|newPositionIncrementGap
argument_list|)
expr_stmt|;
comment|// we need to update to actual analyzers if they are not set in this case...
comment|// so we can inject the position increment gap...
if|if
condition|(
name|builder
operator|.
name|fieldType
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|fieldType
argument_list|()
operator|.
name|setIndexAnalyzer
argument_list|(
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultIndexAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|fieldType
argument_list|()
operator|.
name|setSearchAnalyzer
argument_list|(
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultSearchAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|fieldType
argument_list|()
operator|.
name|searchQuoteAnalyzer
argument_list|()
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|fieldType
argument_list|()
operator|.
name|setSearchQuoteAnalyzer
argument_list|(
name|parserContext
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
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
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseMultiField
argument_list|(
name|builder
argument_list|,
name|fieldName
argument_list|,
name|parserContext
argument_list|,
name|propName
argument_list|,
name|propNode
argument_list|)
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|class|StringFieldType
specifier|public
specifier|static
specifier|final
class|class
name|StringFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|StringFieldType
specifier|public
name|StringFieldType
parameter_list|()
block|{}
DECL|method|StringFieldType
specifier|protected
name|StringFieldType
parameter_list|(
name|StringFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
DECL|method|clone
specifier|public
name|StringFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|StringFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
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
DECL|method|nullValueQuery
specifier|public
name|Query
name|nullValueQuery
parameter_list|()
block|{
if|if
condition|(
name|nullValue
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|termQuery
argument_list|(
name|nullValue
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
DECL|field|includeInAll
specifier|private
name|Boolean
name|includeInAll
decl_stmt|;
DECL|field|positionIncrementGap
specifier|private
name|int
name|positionIncrementGap
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
name|String
name|simpleName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|MappedFieldType
name|defaultFieldType
parameter_list|,
name|int
name|positionIncrementGap
parameter_list|,
name|int
name|ignoreAbove
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|MultiFields
name|multiFields
parameter_list|,
name|CopyTo
name|copyTo
parameter_list|)
block|{
name|super
argument_list|(
name|simpleName
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|indexSettings
argument_list|,
name|multiFields
argument_list|,
name|copyTo
argument_list|)
expr_stmt|;
comment|// TODO: temporarily disabled to give Kibana time to upgrade to text/keyword mappings
comment|/*if (Version.indexCreated(indexSettings).onOrAfter(Version.V_5_0_0)) {             throw new IllegalArgumentException("The [string] type is removed in 5.0. You should now use either a [text] "                     + "or [keyword] field instead for field [" + fieldType.name() + "]");         }*/
if|if
condition|(
name|fieldType
operator|.
name|tokenized
argument_list|()
operator|&&
name|fieldType
operator|.
name|indexOptions
argument_list|()
operator|!=
name|NONE
operator|&&
name|fieldType
argument_list|()
operator|.
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
name|fieldType
operator|.
name|name
argument_list|()
operator|+
literal|"] cannot be analyzed and have doc values"
argument_list|)
throw|;
block|}
name|this
operator|.
name|positionIncrementGap
operator|=
name|positionIncrementGap
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
DECL|method|clone
specifier|protected
name|StringFieldMapper
name|clone
parameter_list|()
block|{
return|return
operator|(
name|StringFieldMapper
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|includeInAll
specifier|public
name|StringFieldMapper
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
name|StringFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|includeInAllIfNotSet
specifier|public
name|StringFieldMapper
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
name|StringFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|unsetIncludeInAll
specifier|public
name|StringFieldMapper
name|unsetIncludeInAll
parameter_list|()
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|StringFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
literal|null
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
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
DECL|method|getPositionIncrementGap
specifier|public
name|int
name|getPositionIncrementGap
parameter_list|()
block|{
return|return
name|this
operator|.
name|positionIncrementGap
return|;
block|}
DECL|method|getIgnoreAbove
specifier|public
name|int
name|getIgnoreAbove
parameter_list|()
block|{
return|return
name|ignoreAbove
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
name|ValueAndBoost
name|valueAndBoost
init|=
name|parseCreateFieldForString
argument_list|(
name|context
argument_list|,
name|fieldType
argument_list|()
operator|.
name|nullValueAsString
argument_list|()
argument_list|,
name|fieldType
argument_list|()
operator|.
name|boost
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueAndBoost
operator|.
name|value
argument_list|()
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
name|valueAndBoost
operator|.
name|value
argument_list|()
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
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|valueAndBoost
operator|.
name|value
argument_list|()
argument_list|,
name|valueAndBoost
operator|.
name|boost
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
operator|!=
name|IndexOptions
operator|.
name|NONE
operator|||
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|Field
name|field
init|=
operator|new
name|Field
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|valueAndBoost
operator|.
name|value
argument_list|()
argument_list|,
name|fieldType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueAndBoost
operator|.
name|boost
argument_list|()
operator|!=
literal|1f
operator|&&
name|Version
operator|.
name|indexCreated
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
condition|)
block|{
name|field
operator|.
name|setBoost
argument_list|(
name|valueAndBoost
operator|.
name|boost
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|fieldType
argument_list|()
operator|.
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
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|valueAndBoost
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Parse a field as though it were a string.      * @param context parse context used during parsing      * @param nullValue value to use for null      * @param defaultBoost default boost value returned unless overwritten in the field      * @return the parsed field and the boost either parsed or defaulted      * @throws IOException if thrown while parsing      */
DECL|method|parseCreateFieldForString
specifier|public
specifier|static
name|ValueAndBoost
name|parseCreateFieldForString
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|String
name|nullValue
parameter_list|,
name|float
name|defaultBoost
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|externalValueSet
argument_list|()
condition|)
block|{
return|return
operator|new
name|ValueAndBoost
argument_list|(
name|context
operator|.
name|externalValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|defaultBoost
argument_list|)
return|;
block|}
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
return|return
operator|new
name|ValueAndBoost
argument_list|(
name|nullValue
argument_list|,
name|defaultBoost
argument_list|)
return|;
block|}
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
operator|&&
name|Version
operator|.
name|indexCreated
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
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
name|String
name|value
init|=
name|nullValue
decl_stmt|;
name|float
name|boost
init|=
name|defaultBoost
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
name|IllegalArgumentException
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
return|return
operator|new
name|ValueAndBoost
argument_list|(
name|value
argument_list|,
name|boost
argument_list|)
return|;
block|}
return|return
operator|new
name|ValueAndBoost
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|,
name|defaultBoost
argument_list|)
return|;
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
DECL|method|doMerge
specifier|protected
name|void
name|doMerge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|super
operator|.
name|doMerge
argument_list|(
name|mergeWith
argument_list|,
name|updateAllTypes
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
DECL|method|indexTokenizeOption
specifier|protected
name|String
name|indexTokenizeOption
parameter_list|(
name|boolean
name|indexed
parameter_list|,
name|boolean
name|tokenized
parameter_list|)
block|{
if|if
condition|(
operator|!
name|indexed
condition|)
block|{
return|return
literal|"no"
return|;
block|}
elseif|else
if|if
condition|(
name|tokenized
condition|)
block|{
return|return
literal|"analyzed"
return|;
block|}
else|else
block|{
return|return
literal|"not_analyzed"
return|;
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
name|doXContentAnalyzers
argument_list|(
name|builder
argument_list|,
name|includeDefaults
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeDefaults
operator|||
name|fieldType
argument_list|()
operator|.
name|nullValue
argument_list|()
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
name|fieldType
argument_list|()
operator|.
name|nullValue
argument_list|()
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
name|positionIncrementGap
operator|!=
name|POSITION_INCREMENT_GAP_USE_ANALYZER
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"position_increment_gap"
argument_list|,
name|positionIncrementGap
argument_list|)
expr_stmt|;
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
comment|/**      * Parsed value and boost to be returned from {@link #parseCreateFieldForString}.      */
DECL|class|ValueAndBoost
specifier|public
specifier|static
class|class
name|ValueAndBoost
block|{
DECL|field|value
specifier|private
specifier|final
name|String
name|value
decl_stmt|;
DECL|field|boost
specifier|private
specifier|final
name|float
name|boost
decl_stmt|;
DECL|method|ValueAndBoost
specifier|public
name|ValueAndBoost
parameter_list|(
name|String
name|value
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
block|}
comment|/**          * Value of string field.          * @return value of string field          */
DECL|method|value
specifier|public
name|String
name|value
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**          * Boost either parsed from the document or defaulted.          * @return boost either parsed from the document or defaulted          */
DECL|method|boost
specifier|public
name|float
name|boost
parameter_list|()
block|{
return|return
name|boost
return|;
block|}
block|}
block|}
end_class

end_unit

