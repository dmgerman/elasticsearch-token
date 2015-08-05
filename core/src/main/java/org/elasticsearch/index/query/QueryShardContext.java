begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|queryparser
operator|.
name|classic
operator|.
name|MapperQueryParser
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
name|queryparser
operator|.
name|classic
operator|.
name|QueryParserSettings
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
name|search
operator|.
name|join
operator|.
name|BitDocIdSetFilter
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
name|similarities
operator|.
name|Similarity
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|mapper
operator|.
name|ContentPath
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
name|MapperBuilders
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
name|MapperService
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
name|StringFieldMapper
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
name|query
operator|.
name|support
operator|.
name|NestedScope
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
name|SimilarityService
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
name|ScriptService
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
name|fetch
operator|.
name|innerhits
operator|.
name|InnerHitsContext
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
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SearchLookup
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
comment|/**  * Context object used to create lucene queries on the shard level.  */
end_comment

begin_class
DECL|class|QueryShardContext
specifier|public
class|class
name|QueryShardContext
block|{
DECL|field|typesContext
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|String
index|[]
argument_list|>
name|typesContext
init|=
operator|new
name|ThreadLocal
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|setTypes
specifier|public
specifier|static
name|void
name|setTypes
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
name|typesContext
operator|.
name|set
argument_list|(
name|types
argument_list|)
expr_stmt|;
block|}
DECL|method|getTypes
specifier|public
specifier|static
name|String
index|[]
name|getTypes
parameter_list|()
block|{
return|return
name|typesContext
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|setTypesWithPrevious
specifier|public
specifier|static
name|String
index|[]
name|setTypesWithPrevious
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
name|String
index|[]
name|old
init|=
name|typesContext
operator|.
name|get
argument_list|()
decl_stmt|;
name|setTypes
argument_list|(
name|types
argument_list|)
expr_stmt|;
return|return
name|old
return|;
block|}
DECL|method|removeTypes
specifier|public
specifier|static
name|void
name|removeTypes
parameter_list|()
block|{
name|typesContext
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
DECL|field|index
specifier|private
specifier|final
name|Index
name|index
decl_stmt|;
DECL|field|indexVersionCreated
specifier|private
specifier|final
name|Version
name|indexVersionCreated
decl_stmt|;
DECL|field|indexQueryParser
specifier|private
specifier|final
name|IndexQueryParserService
name|indexQueryParser
decl_stmt|;
DECL|field|namedQueries
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|namedQueries
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|queryParser
specifier|private
specifier|final
name|MapperQueryParser
name|queryParser
init|=
operator|new
name|MapperQueryParser
argument_list|(
name|this
argument_list|)
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|private
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
DECL|field|allowUnmappedFields
specifier|private
name|boolean
name|allowUnmappedFields
decl_stmt|;
DECL|field|mapUnmappedFieldAsString
specifier|private
name|boolean
name|mapUnmappedFieldAsString
decl_stmt|;
DECL|field|nestedScope
specifier|private
name|NestedScope
name|nestedScope
decl_stmt|;
comment|//norelease this should be possible to remove once query context are completely separated
DECL|field|parseContext
specifier|private
name|QueryParseContext
name|parseContext
decl_stmt|;
DECL|field|isFilter
name|boolean
name|isFilter
decl_stmt|;
DECL|method|QueryShardContext
specifier|public
name|QueryShardContext
parameter_list|(
name|Index
name|index
parameter_list|,
name|IndexQueryParserService
name|indexQueryParser
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|indexVersionCreated
operator|=
name|Version
operator|.
name|indexCreated
argument_list|(
name|indexQueryParser
operator|.
name|indexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexQueryParser
operator|=
name|indexQueryParser
expr_stmt|;
name|this
operator|.
name|parseContext
operator|=
operator|new
name|QueryParseContext
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|parseFieldMatcher
specifier|public
name|void
name|parseFieldMatcher
parameter_list|(
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|this
operator|.
name|parseFieldMatcher
operator|=
name|parseFieldMatcher
expr_stmt|;
block|}
DECL|method|parseFieldMatcher
specifier|public
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|()
block|{
return|return
name|parseFieldMatcher
return|;
block|}
DECL|method|reset
specifier|private
name|void
name|reset
parameter_list|()
block|{
name|allowUnmappedFields
operator|=
name|indexQueryParser
operator|.
name|defaultAllowUnmappedFields
argument_list|()
expr_stmt|;
name|this
operator|.
name|parseFieldMatcher
operator|=
name|ParseFieldMatcher
operator|.
name|EMPTY
expr_stmt|;
name|this
operator|.
name|lookup
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|namedQueries
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|nestedScope
operator|=
operator|new
name|NestedScope
argument_list|()
expr_stmt|;
block|}
comment|//norelease remove parser argument once query contexts are separated
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|(
name|XContentParser
name|jp
parameter_list|)
block|{
name|this
operator|.
name|reset
argument_list|()
expr_stmt|;
name|this
operator|.
name|parseContext
operator|.
name|reset
argument_list|(
name|jp
argument_list|)
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|Index
name|index
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
DECL|method|indexQueryParserService
specifier|public
name|IndexQueryParserService
name|indexQueryParserService
parameter_list|()
block|{
return|return
name|indexQueryParser
return|;
block|}
DECL|method|analysisService
specifier|public
name|AnalysisService
name|analysisService
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|analysisService
return|;
block|}
DECL|method|scriptService
specifier|public
name|ScriptService
name|scriptService
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|scriptService
return|;
block|}
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|mapperService
return|;
block|}
annotation|@
name|Nullable
DECL|method|similarityService
specifier|public
name|SimilarityService
name|similarityService
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|similarityService
return|;
block|}
DECL|method|searchSimilarity
specifier|public
name|Similarity
name|searchSimilarity
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|similarityService
operator|!=
literal|null
condition|?
name|indexQueryParser
operator|.
name|similarityService
operator|.
name|similarity
argument_list|()
else|:
literal|null
return|;
block|}
DECL|method|defaultField
specifier|public
name|String
name|defaultField
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|defaultField
argument_list|()
return|;
block|}
DECL|method|queryStringLenient
specifier|public
name|boolean
name|queryStringLenient
parameter_list|()
block|{
return|return
name|indexQueryParser
operator|.
name|queryStringLenient
argument_list|()
return|;
block|}
DECL|method|queryParser
specifier|public
name|MapperQueryParser
name|queryParser
parameter_list|(
name|QueryParserSettings
name|settings
parameter_list|)
block|{
name|queryParser
operator|.
name|reset
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|queryParser
return|;
block|}
DECL|method|bitsetFilter
specifier|public
name|BitDocIdSetFilter
name|bitsetFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
return|return
name|indexQueryParser
operator|.
name|bitsetFilterCache
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|filter
argument_list|)
return|;
block|}
DECL|method|getForField
specifier|public
parameter_list|<
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|?
argument_list|>
parameter_list|>
name|IFD
name|getForField
parameter_list|(
name|MappedFieldType
name|mapper
parameter_list|)
block|{
return|return
name|indexQueryParser
operator|.
name|fieldDataService
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
return|;
block|}
DECL|method|addNamedQuery
specifier|public
name|void
name|addNamedQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|Query
name|query
parameter_list|)
block|{
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|namedQueries
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|copyNamedQueries
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|copyNamedQueries
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|namedQueries
argument_list|)
return|;
block|}
DECL|method|combineNamedQueries
specifier|public
name|void
name|combineNamedQueries
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
block|{
name|namedQueries
operator|.
name|putAll
argument_list|(
name|context
operator|.
name|namedQueries
argument_list|)
expr_stmt|;
block|}
comment|/**      * Return whether we are currently parsing a filter or a query.      */
DECL|method|isFilter
specifier|public
name|boolean
name|isFilter
parameter_list|()
block|{
return|return
name|isFilter
return|;
block|}
DECL|method|addInnerHits
specifier|public
name|void
name|addInnerHits
parameter_list|(
name|String
name|name
parameter_list|,
name|InnerHitsContext
operator|.
name|BaseInnerHits
name|context
parameter_list|)
block|{
name|SearchContext
name|sc
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|sc
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|this
argument_list|,
literal|"inner_hits unsupported"
argument_list|)
throw|;
block|}
name|InnerHitsContext
name|innerHitsContext
decl_stmt|;
if|if
condition|(
name|sc
operator|.
name|innerHits
argument_list|()
operator|==
literal|null
condition|)
block|{
name|innerHitsContext
operator|=
operator|new
name|InnerHitsContext
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|InnerHitsContext
operator|.
name|BaseInnerHits
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sc
operator|.
name|innerHits
argument_list|(
name|innerHitsContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|innerHitsContext
operator|=
name|sc
operator|.
name|innerHits
argument_list|()
expr_stmt|;
block|}
name|innerHitsContext
operator|.
name|addInnerHitDefinition
argument_list|(
name|name
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
DECL|method|simpleMatchToIndexNames
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|simpleMatchToIndexNames
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
return|return
name|indexQueryParser
operator|.
name|mapperService
operator|.
name|simpleMatchToIndexNames
argument_list|(
name|pattern
argument_list|,
name|getTypes
argument_list|()
argument_list|)
return|;
block|}
DECL|method|fieldMapper
specifier|public
name|MappedFieldType
name|fieldMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|failIfFieldMappingNotFound
argument_list|(
name|name
argument_list|,
name|indexQueryParser
operator|.
name|mapperService
operator|.
name|smartNameFieldType
argument_list|(
name|name
argument_list|,
name|getTypes
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getObjectMapper
specifier|public
name|ObjectMapper
name|getObjectMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|indexQueryParser
operator|.
name|mapperService
operator|.
name|getObjectMapper
argument_list|(
name|name
argument_list|,
name|getTypes
argument_list|()
argument_list|)
return|;
block|}
comment|/** Gets the search analyzer for the given field, or the default if there is none present for the field      * TODO: remove this by moving defaults into mappers themselves      */
DECL|method|getSearchAnalyzer
specifier|public
name|Analyzer
name|getSearchAnalyzer
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|searchAnalyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|fieldType
operator|.
name|searchAnalyzer
argument_list|()
return|;
block|}
return|return
name|mapperService
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
return|;
block|}
comment|/** Gets the search quote nalyzer for the given field, or the default if there is none present for the field      * TODO: remove this by moving defaults into mappers themselves      */
DECL|method|getSearchQuoteAnalyzer
specifier|public
name|Analyzer
name|getSearchQuoteAnalyzer
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|searchQuoteAnalyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|fieldType
operator|.
name|searchQuoteAnalyzer
argument_list|()
return|;
block|}
return|return
name|mapperService
argument_list|()
operator|.
name|searchQuoteAnalyzer
argument_list|()
return|;
block|}
DECL|method|setAllowUnmappedFields
specifier|public
name|void
name|setAllowUnmappedFields
parameter_list|(
name|boolean
name|allowUnmappedFields
parameter_list|)
block|{
name|this
operator|.
name|allowUnmappedFields
operator|=
name|allowUnmappedFields
expr_stmt|;
block|}
DECL|method|setMapUnmappedFieldAsString
specifier|public
name|void
name|setMapUnmappedFieldAsString
parameter_list|(
name|boolean
name|mapUnmappedFieldAsString
parameter_list|)
block|{
name|this
operator|.
name|mapUnmappedFieldAsString
operator|=
name|mapUnmappedFieldAsString
expr_stmt|;
block|}
DECL|method|failIfFieldMappingNotFound
specifier|private
name|MappedFieldType
name|failIfFieldMappingNotFound
parameter_list|(
name|String
name|name
parameter_list|,
name|MappedFieldType
name|fieldMapping
parameter_list|)
block|{
if|if
condition|(
name|allowUnmappedFields
condition|)
block|{
return|return
name|fieldMapping
return|;
block|}
elseif|else
if|if
condition|(
name|mapUnmappedFieldAsString
condition|)
block|{
name|StringFieldMapper
operator|.
name|Builder
name|builder
init|=
name|MapperBuilders
operator|.
name|stringField
argument_list|(
name|name
argument_list|)
decl_stmt|;
comment|// it would be better to pass the real index settings, but they are not easily accessible from here...
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|indexQueryParser
operator|.
name|getIndexCreatedVersion
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|settings
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
operator|.
name|fieldType
argument_list|()
return|;
block|}
else|else
block|{
name|Version
name|indexCreatedVersion
init|=
name|indexQueryParser
operator|.
name|getIndexCreatedVersion
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldMapping
operator|==
literal|null
operator|&&
name|indexCreatedVersion
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|this
argument_list|,
literal|"Strict field resolution and no field mapping can be found for the field with name ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
return|return
name|fieldMapping
return|;
block|}
block|}
block|}
comment|/**      * Returns the narrowed down explicit types, or, if not set, all types.      */
DECL|method|queryTypes
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|queryTypes
parameter_list|()
block|{
name|String
index|[]
name|types
init|=
name|getTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|types
operator|==
literal|null
operator|||
name|types
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|mapperService
argument_list|()
operator|.
name|types
argument_list|()
return|;
block|}
if|if
condition|(
name|types
operator|.
name|length
operator|==
literal|1
operator|&&
name|types
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
return|return
name|mapperService
argument_list|()
operator|.
name|types
argument_list|()
return|;
block|}
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|types
argument_list|)
return|;
block|}
DECL|field|lookup
specifier|private
name|SearchLookup
name|lookup
init|=
literal|null
decl_stmt|;
DECL|method|lookup
specifier|public
name|SearchLookup
name|lookup
parameter_list|()
block|{
name|SearchContext
name|current
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
return|return
name|current
operator|.
name|lookup
argument_list|()
return|;
block|}
if|if
condition|(
name|lookup
operator|==
literal|null
condition|)
block|{
name|lookup
operator|=
operator|new
name|SearchLookup
argument_list|(
name|mapperService
argument_list|()
argument_list|,
name|indexQueryParser
operator|.
name|fieldDataService
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|lookup
return|;
block|}
DECL|method|nowInMillis
specifier|public
name|long
name|nowInMillis
parameter_list|()
block|{
name|SearchContext
name|current
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
return|return
name|current
operator|.
name|nowInMillis
argument_list|()
return|;
block|}
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
return|;
block|}
DECL|method|nestedScope
specifier|public
name|NestedScope
name|nestedScope
parameter_list|()
block|{
return|return
name|nestedScope
return|;
block|}
DECL|method|indexVersionCreated
specifier|public
name|Version
name|indexVersionCreated
parameter_list|()
block|{
return|return
name|indexVersionCreated
return|;
block|}
DECL|method|parseContext
specifier|public
name|QueryParseContext
name|parseContext
parameter_list|()
block|{
return|return
name|this
operator|.
name|parseContext
return|;
block|}
block|}
end_class

end_unit

