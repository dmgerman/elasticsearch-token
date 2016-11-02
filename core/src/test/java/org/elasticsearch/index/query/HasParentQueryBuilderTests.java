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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|MatchNoDocsQuery
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
name|ScoreMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingRequest
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
name|compress
operator|.
name|CompressedXContent
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
name|search
operator|.
name|fetch
operator|.
name|subphase
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
name|sort
operator|.
name|FieldSortBuilder
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
name|sort
operator|.
name|SortOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|AbstractQueryTestCase
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|notNullValue
import|;
end_import

begin_class
DECL|class|HasParentQueryBuilderTests
specifier|public
class|class
name|HasParentQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|HasParentQueryBuilder
argument_list|>
block|{
DECL|field|PARENT_TYPE
specifier|protected
specifier|static
specifier|final
name|String
name|PARENT_TYPE
init|=
literal|"parent"
decl_stmt|;
DECL|field|CHILD_TYPE
specifier|protected
specifier|static
specifier|final
name|String
name|CHILD_TYPE
init|=
literal|"child"
decl_stmt|;
DECL|field|requiresRewrite
name|boolean
name|requiresRewrite
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
DECL|method|initializeAdditionalMappings
specifier|protected
name|void
name|initializeAdditionalMappings
parameter_list|(
name|MapperService
name|mapperService
parameter_list|)
throws|throws
name|IOException
block|{
name|mapperService
operator|.
name|merge
argument_list|(
name|PARENT_TYPE
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
name|PARENT_TYPE
argument_list|,
name|STRING_FIELD_NAME
argument_list|,
literal|"type=text"
argument_list|,
name|STRING_FIELD_NAME_2
argument_list|,
literal|"type=keyword"
argument_list|,
name|INT_FIELD_NAME
argument_list|,
literal|"type=integer"
argument_list|,
name|DOUBLE_FIELD_NAME
argument_list|,
literal|"type=double"
argument_list|,
name|BOOLEAN_FIELD_NAME
argument_list|,
literal|"type=boolean"
argument_list|,
name|DATE_FIELD_NAME
argument_list|,
literal|"type=date"
argument_list|,
name|OBJECT_FIELD_NAME
argument_list|,
literal|"type=object"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|CHILD_TYPE
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
name|CHILD_TYPE
argument_list|,
literal|"_parent"
argument_list|,
literal|"type="
operator|+
name|PARENT_TYPE
argument_list|,
name|STRING_FIELD_NAME
argument_list|,
literal|"type=text"
argument_list|,
name|STRING_FIELD_NAME_2
argument_list|,
literal|"type=keyword"
argument_list|,
name|INT_FIELD_NAME
argument_list|,
literal|"type=integer"
argument_list|,
name|DOUBLE_FIELD_NAME
argument_list|,
literal|"type=double"
argument_list|,
name|BOOLEAN_FIELD_NAME
argument_list|,
literal|"type=boolean"
argument_list|,
name|DATE_FIELD_NAME
argument_list|,
literal|"type=date"
argument_list|,
name|OBJECT_FIELD_NAME
argument_list|,
literal|"type=object"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"just_a_type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
literal|"just_a_type"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return a {@link HasChildQueryBuilder} with random values all over the place      */
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|HasParentQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|QueryBuilder
name|innerQueryBuilder
init|=
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|requiresRewrite
operator|=
literal|true
expr_stmt|;
name|innerQueryBuilder
operator|=
operator|new
name|WrapperQueryBuilder
argument_list|(
name|innerQueryBuilder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HasParentQueryBuilder
name|hqb
init|=
operator|new
name|HasParentQueryBuilder
argument_list|(
name|PARENT_TYPE
argument_list|,
name|innerQueryBuilder
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|hqb
operator|.
name|innerHit
argument_list|(
operator|new
name|InnerHitBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
operator|new
name|FieldSortBuilder
argument_list|(
name|STRING_FIELD_NAME_2
argument_list|)
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|ASC
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|hqb
operator|.
name|ignoreUnmapped
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|hqb
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|HasParentQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
throws|throws
name|IOException
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|HasChildQueryBuilder
operator|.
name|LateParsingQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|HasChildQueryBuilder
operator|.
name|LateParsingQuery
name|lpq
init|=
operator|(
name|HasChildQueryBuilder
operator|.
name|LateParsingQuery
operator|)
name|query
decl_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
operator|.
name|score
argument_list|()
condition|?
name|ScoreMode
operator|.
name|Max
else|:
name|ScoreMode
operator|.
name|None
argument_list|,
name|lpq
operator|.
name|getScoreMode
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|innerHit
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// have to rewrite again because the provided queryBuilder hasn't been rewritten (directly returned from
comment|// doCreateTestQueryBuilder)
name|queryBuilder
operator|=
operator|(
name|HasParentQueryBuilder
operator|)
name|queryBuilder
operator|.
name|rewrite
argument_list|(
name|searchContext
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitBuilder
argument_list|>
name|innerHitBuilders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|InnerHitBuilder
operator|.
name|extractInnerHits
argument_list|(
name|queryBuilder
argument_list|,
name|innerHitBuilders
argument_list|)
expr_stmt|;
for|for
control|(
name|InnerHitBuilder
name|builder
range|:
name|innerHitBuilders
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|build
argument_list|(
name|searchContext
argument_list|,
name|searchContext
operator|.
name|innerHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|searchContext
operator|.
name|innerHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|searchContext
operator|.
name|innerHits
argument_list|()
operator|.
name|getInnerHits
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|searchContext
operator|.
name|innerHits
argument_list|()
operator|.
name|getInnerHits
argument_list|()
operator|.
name|containsKey
argument_list|(
name|queryBuilder
operator|.
name|innerHit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|InnerHitsContext
operator|.
name|BaseInnerHits
name|innerHits
init|=
name|searchContext
operator|.
name|innerHits
argument_list|()
operator|.
name|getInnerHits
argument_list|()
operator|.
name|get
argument_list|(
name|queryBuilder
operator|.
name|innerHit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|innerHits
operator|.
name|size
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|innerHit
argument_list|()
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|innerHits
operator|.
name|sort
argument_list|()
operator|.
name|sort
operator|.
name|getSort
argument_list|()
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|innerHits
operator|.
name|sort
argument_list|()
operator|.
name|sort
operator|.
name|getSort
argument_list|()
index|[
literal|0
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|STRING_FIELD_NAME_2
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIllegalValues
specifier|public
name|void
name|testIllegalValues
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryBuilder
name|query
init|=
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|QueryBuilders
operator|.
name|hasParentQuery
argument_list|(
literal|null
argument_list|,
name|query
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[has_parent] requires 'type' field"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|QueryBuilders
operator|.
name|hasParentQuery
argument_list|(
literal|"foo"
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[has_parent] requires 'query' field"
argument_list|)
argument_list|)
expr_stmt|;
name|QueryShardContext
name|context
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|HasParentQueryBuilder
name|qb
init|=
name|QueryBuilders
operator|.
name|hasParentQuery
argument_list|(
literal|"just_a_type"
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|QueryShardException
name|qse
init|=
name|expectThrows
argument_list|(
name|QueryShardException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|qb
operator|.
name|doToQuery
argument_list|(
name|context
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|qse
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[has_parent] no child types found for type [just_a_type]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeprecatedXContent
specifier|public
name|void
name|testDeprecatedXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"has_parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|)
expr_stmt|;
operator|new
name|TermQueryBuilder
argument_list|(
literal|"a"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
comment|// deprecated
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Deprecated field [type] used, expected [parent_type] instead"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|HasParentQueryBuilder
name|queryBuilder
init|=
operator|(
name|HasParentQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|queryBuilder
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|checkWarningHeaders
argument_list|(
literal|"Deprecated field [type] used, expected [parent_type] instead"
argument_list|)
expr_stmt|;
block|}
DECL|method|testToQueryInnerQueryType
specifier|public
name|void
name|testToQueryInnerQueryType
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|searchTypes
init|=
operator|new
name|String
index|[]
block|{
name|CHILD_TYPE
block|}
decl_stmt|;
name|QueryShardContext
name|shardContext
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|shardContext
operator|.
name|setTypes
argument_list|(
name|searchTypes
argument_list|)
expr_stmt|;
name|HasParentQueryBuilder
name|hasParentQueryBuilder
init|=
operator|new
name|HasParentQueryBuilder
argument_list|(
name|PARENT_TYPE
argument_list|,
operator|new
name|IdsQueryBuilder
argument_list|()
operator|.
name|addIds
argument_list|(
literal|"id"
argument_list|)
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|hasParentQueryBuilder
operator|.
name|toQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
comment|//verify that the context types are still the same as the ones we previously set
name|assertThat
argument_list|(
name|shardContext
operator|.
name|getTypes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|searchTypes
argument_list|)
argument_list|)
expr_stmt|;
name|HasChildQueryBuilderTests
operator|.
name|assertLateParsingQuery
argument_list|(
name|query
argument_list|,
name|PARENT_TYPE
argument_list|,
literal|"id"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testMustRewrite
specifier|public
name|void
name|testMustRewrite
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|super
operator|.
name|testMustRewrite
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
if|if
condition|(
name|requiresRewrite
operator|==
literal|false
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
DECL|method|testFromJson
specifier|public
name|void
name|testFromJson
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"has_parent\" : {\n"
operator|+
literal|"    \"query\" : {\n"
operator|+
literal|"      \"term\" : {\n"
operator|+
literal|"        \"tag\" : {\n"
operator|+
literal|"          \"value\" : \"something\",\n"
operator|+
literal|"          \"boost\" : 1.0\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"parent_type\" : \"blog\",\n"
operator|+
literal|"    \"score\" : true,\n"
operator|+
literal|"    \"ignore_unmapped\" : false,\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|HasParentQueryBuilder
name|parsed
init|=
operator|(
name|HasParentQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|json
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"blog"
argument_list|,
name|parsed
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"something"
argument_list|,
operator|(
operator|(
name|TermQueryBuilder
operator|)
name|parsed
operator|.
name|query
argument_list|()
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * we resolve empty inner clauses by representing this whole query as empty optional upstream      */
DECL|method|testFromJsonEmptyQueryBody
specifier|public
name|void
name|testFromJsonEmptyQueryBody
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"  \"has_parent\" : {\n"
operator|+
literal|"    \"query\" : { },\n"
operator|+
literal|"    \"parent_type\" : \"blog\""
operator|+
literal|"   }"
operator|+
literal|"}"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|query
argument_list|)
operator|.
name|createParser
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|QueryParseContext
name|context
init|=
name|createParseContext
argument_list|(
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|Optional
argument_list|<
name|QueryBuilder
argument_list|>
name|innerQueryBuilder
init|=
name|context
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|innerQueryBuilder
operator|.
name|isPresent
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
name|checkWarningHeaders
argument_list|(
literal|"query malformed, empty clause found at [3:17]"
argument_list|)
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|query
argument_list|)
operator|.
name|createParser
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|QueryParseContext
name|otherContext
init|=
name|createParseContext
argument_list|(
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|IllegalArgumentException
name|ex
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|otherContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"query malformed, empty clause found at [3:17]"
argument_list|)
argument_list|)
expr_stmt|;
name|checkWarningHeaders
argument_list|(
literal|"query malformed, empty clause found at [3:17]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testIgnoreUnmapped
specifier|public
name|void
name|testIgnoreUnmapped
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|HasParentQueryBuilder
name|queryBuilder
init|=
operator|new
name|HasParentQueryBuilder
argument_list|(
literal|"unmapped"
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|queryBuilder
operator|.
name|ignoreUnmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|queryBuilder
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchNoDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|HasParentQueryBuilder
name|failingQueryBuilder
init|=
operator|new
name|HasParentQueryBuilder
argument_list|(
literal|"unmapped"
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|failingQueryBuilder
operator|.
name|ignoreUnmapped
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|QueryShardException
name|e
init|=
name|expectThrows
argument_list|(
name|QueryShardException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|failingQueryBuilder
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"["
operator|+
name|HasParentQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query configured 'parent_type' [unmapped] is not a valid type"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

