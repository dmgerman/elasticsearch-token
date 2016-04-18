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
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|join
operator|.
name|ToParentBlockJoinQuery
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
name|query
operator|.
name|support
operator|.
name|InnerHitBuilder
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
name|java
operator|.
name|io
operator|.
name|IOException
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
DECL|class|NestedQueryBuilderTests
specifier|public
class|class
name|NestedQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|NestedQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|MapperService
name|mapperService
init|=
name|createShardContext
argument_list|()
operator|.
name|getMapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"nested_doc"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
literal|"nested_doc"
argument_list|,
name|STRING_FIELD_NAME
argument_list|,
literal|"type=text"
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
argument_list|,
name|GEO_POINT_FIELD_NAME
argument_list|,
name|GEO_POINT_FIELD_MAPPING
argument_list|,
literal|"nested1"
argument_list|,
literal|"type=nested"
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
name|NestedQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|NestedQueryBuilder
name|nqb
init|=
operator|new
name|NestedQueryBuilder
argument_list|(
literal|"nested1"
argument_list|,
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|()
argument_list|,
name|ScoreMode
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|SearchContext
operator|.
name|current
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nqb
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
name|STRING_FIELD_NAME
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
name|nqb
operator|.
name|ignoreUnmapped
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|nqb
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|NestedQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryBuilder
name|innerQueryBuilder
init|=
name|queryBuilder
operator|.
name|query
argument_list|()
decl_stmt|;
if|if
condition|(
name|innerQueryBuilder
operator|instanceof
name|EmptyQueryBuilder
condition|)
block|{
name|assertNull
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|ToParentBlockJoinQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ToParentBlockJoinQuery
name|parentBlockJoinQuery
init|=
operator|(
name|ToParentBlockJoinQuery
operator|)
name|query
decl_stmt|;
comment|//TODO how to assert this?
block|}
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
name|assertNotNull
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|assertNotNull
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|innerHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|SearchContext
operator|.
name|current
argument_list|()
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
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|innerHits
argument_list|()
operator|.
name|getInnerHits
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"inner_hits_name"
argument_list|)
argument_list|)
expr_stmt|;
name|InnerHitsContext
operator|.
name|BaseInnerHits
name|innerHits
init|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|innerHits
argument_list|()
operator|.
name|getInnerHits
argument_list|()
operator|.
name|get
argument_list|(
literal|"inner_hits_name"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|innerHits
operator|.
name|size
argument_list|()
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|innerHits
operator|.
name|sort
argument_list|()
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
name|getSort
argument_list|()
index|[
literal|0
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|STRING_FIELD_NAME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|innerHits
argument_list|()
operator|.
name|getInnerHits
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testValidate
specifier|public
name|void
name|testValidate
parameter_list|()
block|{
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|innerQuery
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
name|nestedQuery
argument_list|(
literal|null
argument_list|,
name|innerQuery
argument_list|,
name|ScoreMode
operator|.
name|Avg
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
literal|"[nested] requires 'path' field"
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
name|nestedQuery
argument_list|(
literal|"foo"
argument_list|,
literal|null
argument_list|,
name|ScoreMode
operator|.
name|Avg
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
literal|"[nested] requires 'query' field"
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
name|nestedQuery
argument_list|(
literal|"foo"
argument_list|,
name|innerQuery
argument_list|,
literal|null
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
literal|"[nested] requires 'score_mode' field"
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"  \"nested\" : {\n"
operator|+
literal|"    \"query\" : {\n"
operator|+
literal|"      \"bool\" : {\n"
operator|+
literal|"        \"must\" : [ {\n"
operator|+
literal|"          \"match\" : {\n"
operator|+
literal|"            \"obj1.name\" : {\n"
operator|+
literal|"              \"query\" : \"blue\",\n"
operator|+
literal|"              \"operator\" : \"OR\",\n"
operator|+
literal|"              \"prefix_length\" : 0,\n"
operator|+
literal|"              \"max_expansions\" : 50,\n"
operator|+
literal|"              \"fuzzy_transpositions\" : true,\n"
operator|+
literal|"              \"lenient\" : false,\n"
operator|+
literal|"              \"zero_terms_query\" : \"NONE\",\n"
operator|+
literal|"              \"boost\" : 1.0\n"
operator|+
literal|"            }\n"
operator|+
literal|"          }\n"
operator|+
literal|"        }, {\n"
operator|+
literal|"          \"range\" : {\n"
operator|+
literal|"            \"obj1.count\" : {\n"
operator|+
literal|"              \"from\" : 5,\n"
operator|+
literal|"              \"to\" : null,\n"
operator|+
literal|"              \"include_lower\" : false,\n"
operator|+
literal|"              \"include_upper\" : true,\n"
operator|+
literal|"              \"boost\" : 1.0\n"
operator|+
literal|"            }\n"
operator|+
literal|"          }\n"
operator|+
literal|"        } ],\n"
operator|+
literal|"        \"disable_coord\" : false,\n"
operator|+
literal|"        \"adjust_pure_negative\" : true,\n"
operator|+
literal|"        \"boost\" : 1.0\n"
operator|+
literal|"      }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"path\" : \"obj1\",\n"
operator|+
literal|"    \"ignore_unmapped\" : false,\n"
operator|+
literal|"    \"score_mode\" : \"avg\",\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|NestedQueryBuilder
name|parsed
init|=
operator|(
name|NestedQueryBuilder
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
name|ScoreMode
operator|.
name|Avg
argument_list|,
name|parsed
operator|.
name|scoreMode
argument_list|()
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
name|NestedQueryBuilder
name|queryBuilder
init|=
operator|new
name|NestedQueryBuilder
argument_list|(
literal|"unmapped"
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|ScoreMode
operator|.
name|None
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
name|NestedQueryBuilder
name|failingQueryBuilder
init|=
operator|new
name|NestedQueryBuilder
argument_list|(
literal|"unmapped"
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|ScoreMode
operator|.
name|None
argument_list|)
decl_stmt|;
name|failingQueryBuilder
operator|.
name|ignoreUnmapped
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|IllegalStateException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
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
name|NestedQueryBuilder
operator|.
name|NAME
operator|+
literal|"] failed to find nested object under path [unmapped]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

