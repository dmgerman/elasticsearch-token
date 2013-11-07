begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|collect
operator|.
name|MapBuilder
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
name|collect
operator|.
name|Tuple
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
name|ShapesAvailability
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
name|ImmutableSettings
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
name|XContentFactory
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
name|XContentHelper
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
name|AbstractIndexComponent
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
name|Index
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
name|AnalysisService
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
name|DocValuesFormatService
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
name|PostingsFormatService
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
name|geo
operator|.
name|GeoPointFieldMapper
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
name|geo
operator|.
name|GeoShapeFieldMapper
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
name|index
operator|.
name|mapper
operator|.
name|multifield
operator|.
name|MultiFieldMapper
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
name|object
operator|.
name|ObjectMapper
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
name|object
operator|.
name|RootObjectMapper
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
name|settings
operator|.
name|IndexSettings
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
name|SimilarityLookupService
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
name|doc
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DocumentMapperParser
specifier|public
class|class
name|DocumentMapperParser
extends|extends
name|AbstractIndexComponent
block|{
DECL|field|analysisService
specifier|final
name|AnalysisService
name|analysisService
decl_stmt|;
DECL|field|postingsFormatService
specifier|private
specifier|final
name|PostingsFormatService
name|postingsFormatService
decl_stmt|;
DECL|field|docValuesFormatService
specifier|private
specifier|final
name|DocValuesFormatService
name|docValuesFormatService
decl_stmt|;
DECL|field|similarityLookupService
specifier|private
specifier|final
name|SimilarityLookupService
name|similarityLookupService
decl_stmt|;
DECL|field|rootObjectTypeParser
specifier|private
specifier|final
name|RootObjectMapper
operator|.
name|TypeParser
name|rootObjectTypeParser
init|=
operator|new
name|RootObjectMapper
operator|.
name|TypeParser
argument_list|()
decl_stmt|;
DECL|field|typeParsersMutex
specifier|private
specifier|final
name|Object
name|typeParsersMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|typeParsers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|typeParsers
decl_stmt|;
DECL|field|rootTypeParsers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|rootTypeParsers
decl_stmt|;
DECL|method|DocumentMapperParser
specifier|public
name|DocumentMapperParser
parameter_list|(
name|Index
name|index
parameter_list|,
name|AnalysisService
name|analysisService
parameter_list|,
name|PostingsFormatService
name|postingsFormatService
parameter_list|,
name|DocValuesFormatService
name|docValuesFormatService
parameter_list|,
name|SimilarityLookupService
name|similarityLookupService
parameter_list|)
block|{
name|this
argument_list|(
name|index
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|analysisService
argument_list|,
name|postingsFormatService
argument_list|,
name|docValuesFormatService
argument_list|,
name|similarityLookupService
argument_list|)
expr_stmt|;
block|}
DECL|method|DocumentMapperParser
specifier|public
name|DocumentMapperParser
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|AnalysisService
name|analysisService
parameter_list|,
name|PostingsFormatService
name|postingsFormatService
parameter_list|,
name|DocValuesFormatService
name|docValuesFormatService
parameter_list|,
name|SimilarityLookupService
name|similarityLookupService
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|analysisService
operator|=
name|analysisService
expr_stmt|;
name|this
operator|.
name|postingsFormatService
operator|=
name|postingsFormatService
expr_stmt|;
name|this
operator|.
name|docValuesFormatService
operator|=
name|docValuesFormatService
expr_stmt|;
name|this
operator|.
name|similarityLookupService
operator|=
name|similarityLookupService
expr_stmt|;
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|typeParsersBuilder
init|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
argument_list|()
operator|.
name|put
argument_list|(
name|ByteFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|ByteFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|ShortFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|ShortFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IntegerFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|IntegerFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|LongFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|LongFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|FloatFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|FloatFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|DoubleFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|DoubleFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|BooleanFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|BooleanFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|BinaryFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|BinaryFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|DateFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|DateFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IpFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|IpFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|StringFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|StringFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|ObjectMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|ObjectMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|ObjectMapper
operator|.
name|NESTED_CONTENT_TYPE
argument_list|,
operator|new
name|ObjectMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|MultiFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|MultiFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|CompletionFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|CompletionFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|GeoPointFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|GeoPointFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ShapesAvailability
operator|.
name|JTS_AVAILABLE
condition|)
block|{
name|typeParsersBuilder
operator|.
name|put
argument_list|(
name|GeoShapeFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|GeoShapeFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|typeParsers
operator|=
name|typeParsersBuilder
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
name|rootTypeParsers
operator|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
argument_list|()
operator|.
name|put
argument_list|(
name|SizeFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|SizeFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|IndexFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|SourceFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|TypeFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|AllFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|AllFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|AnalyzerMapper
operator|.
name|NAME
argument_list|,
operator|new
name|AnalyzerMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|BoostFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|BoostFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|ParentFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|RoutingFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|RoutingFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|TimestampFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|TimestampFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|TTLFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|TTLFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|UidFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|VersionFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IdFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|IdFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
DECL|method|putTypeParser
specifier|public
name|void
name|putTypeParser
parameter_list|(
name|String
name|type
parameter_list|,
name|Mapper
operator|.
name|TypeParser
name|typeParser
parameter_list|)
block|{
synchronized|synchronized
init|(
name|typeParsersMutex
init|)
block|{
name|typeParsers
operator|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
argument_list|(
name|typeParsers
argument_list|)
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|typeParser
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|putRootTypeParser
specifier|public
name|void
name|putRootTypeParser
parameter_list|(
name|String
name|type
parameter_list|,
name|Mapper
operator|.
name|TypeParser
name|typeParser
parameter_list|)
block|{
synchronized|synchronized
init|(
name|typeParsersMutex
init|)
block|{
name|rootTypeParsers
operator|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
argument_list|(
name|rootTypeParsers
argument_list|)
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|typeParser
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|parserContext
specifier|public
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|()
block|{
return|return
operator|new
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
argument_list|(
name|postingsFormatService
argument_list|,
name|docValuesFormatService
argument_list|,
name|analysisService
argument_list|,
name|similarityLookupService
argument_list|,
name|typeParsers
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
name|String
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
return|return
name|parse
argument_list|(
literal|null
argument_list|,
name|source
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
return|return
name|parse
argument_list|(
name|type
argument_list|,
name|source
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|parse
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|,
name|String
name|defaultSource
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|t
init|=
name|extractMapping
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|type
operator|=
name|t
operator|.
name|v1
argument_list|()
expr_stmt|;
name|mapping
operator|=
name|t
operator|.
name|v2
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|mapping
operator|==
literal|null
condition|)
block|{
name|mapping
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Failed to derive type"
argument_list|)
throw|;
block|}
if|if
condition|(
name|defaultSource
operator|!=
literal|null
condition|)
block|{
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|t
init|=
name|extractMapping
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
name|defaultSource
argument_list|)
decl_stmt|;
if|if
condition|(
name|t
operator|.
name|v2
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|XContentHelper
operator|.
name|mergeDefaults
argument_list|(
name|mapping
argument_list|,
name|t
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
name|parserContext
init|=
operator|new
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
argument_list|(
name|postingsFormatService
argument_list|,
name|docValuesFormatService
argument_list|,
name|analysisService
argument_list|,
name|similarityLookupService
argument_list|,
name|typeParsers
argument_list|)
decl_stmt|;
name|DocumentMapper
operator|.
name|Builder
name|docBuilder
init|=
name|doc
argument_list|(
name|index
operator|.
name|name
argument_list|()
argument_list|,
name|indexSettings
argument_list|,
operator|(
name|RootObjectMapper
operator|.
name|Builder
operator|)
name|rootObjectTypeParser
operator|.
name|parse
argument_list|(
name|type
argument_list|,
name|mapping
argument_list|,
name|parserContext
argument_list|)
argument_list|)
decl_stmt|;
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
name|mapping
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
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
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"index_analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
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
name|fieldNode
operator|.
name|toString
argument_list|()
operator|+
literal|"] not found for index_analyzer setting on root type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|docBuilder
operator|.
name|indexAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"search_analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
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
name|fieldNode
operator|.
name|toString
argument_list|()
operator|+
literal|"] not found for search_analyzer setting on root type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"search_quote_analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
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
name|fieldNode
operator|.
name|toString
argument_list|()
operator|+
literal|"] not found for search_analyzer setting on root type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|docBuilder
operator|.
name|searchQuoteAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
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
name|fieldNode
operator|.
name|toString
argument_list|()
operator|+
literal|"] not found for analyzer setting on root type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|docBuilder
operator|.
name|indexAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Mapper
operator|.
name|TypeParser
name|typeParser
init|=
name|rootTypeParsers
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeParser
operator|!=
literal|null
condition|)
block|{
name|docBuilder
operator|.
name|put
argument_list|(
name|typeParser
operator|.
name|parse
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|docBuilder
operator|.
name|hasIndexAnalyzer
argument_list|()
condition|)
block|{
name|docBuilder
operator|.
name|indexAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|docBuilder
operator|.
name|hasSearchAnalyzer
argument_list|()
condition|)
block|{
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|docBuilder
operator|.
name|hasSearchQuoteAnalyzer
argument_list|()
condition|)
block|{
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|attributes
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapping
operator|.
name|containsKey
argument_list|(
literal|"_meta"
argument_list|)
condition|)
block|{
name|attributes
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|mapping
operator|.
name|get
argument_list|(
literal|"_meta"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|docBuilder
operator|.
name|meta
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|docBuilder
operator|.
name|build
argument_list|(
name|this
argument_list|)
decl_stmt|;
comment|// update the source with the generated one
name|documentMapper
operator|.
name|refreshSource
argument_list|()
expr_stmt|;
return|return
name|documentMapper
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractMapping
specifier|private
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|extractMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|root
decl_stmt|;
try|try
block|{
name|root
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
operator|.
name|mapOrderedAndClose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"failed to parse mapping definition"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|int
name|size
init|=
name|root
operator|.
name|size
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|size
condition|)
block|{
case|case
literal|0
case|:
comment|// if we don't have any keys throw an exception
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"malformed mapping no root object found"
argument_list|)
throw|;
case|case
literal|1
case|:
break|break;
default|default:
comment|// we always assume the first and single key is the mapping type root
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"mapping must have the `type` as the root object"
argument_list|)
throw|;
block|}
name|String
name|rootName
init|=
name|root
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|type
operator|=
name|rootName
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|type
operator|.
name|equals
argument_list|(
name|rootName
argument_list|)
condition|)
block|{
comment|// we always assume the first and single key is the mapping type root
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"mapping must have the `type` as the root object. Got ["
operator|+
name|rootName
operator|+
literal|"], expected ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|Tuple
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|(
name|type
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|root
operator|.
name|get
argument_list|(
name|rootName
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

