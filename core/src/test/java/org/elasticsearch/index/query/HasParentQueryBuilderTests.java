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
name|Version
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
name|fielddata
operator|.
name|IndexFieldDataService
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
name|QueryInnerHits
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
name|child
operator|.
name|ChildrenQuery
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
name|child
operator|.
name|ParentConstantScoreQuery
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
name|child
operator|.
name|ParentQuery
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
name|child
operator|.
name|ScoreType
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
name|InnerHitsBuilder
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
name|TestSearchContext
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
name|Arrays
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
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

begin_class
DECL|class|HasParentQueryBuilderTests
specifier|public
class|class
name|HasParentQueryBuilderTests
extends|extends
name|BaseQueryTestCase
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
name|queryParserService
argument_list|()
operator|.
name|mapperService
decl_stmt|;
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
literal|"type=string"
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
literal|false
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
literal|"type=string"
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
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|setSearchContext
specifier|protected
name|void
name|setSearchContext
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
specifier|final
name|MapperService
name|mapperService
init|=
name|queryParserService
argument_list|()
operator|.
name|mapperService
decl_stmt|;
specifier|final
name|IndexFieldDataService
name|fieldData
init|=
name|queryParserService
argument_list|()
operator|.
name|fieldDataService
decl_stmt|;
name|TestSearchContext
name|testSearchContext
init|=
operator|new
name|TestSearchContext
argument_list|()
block|{
specifier|private
name|InnerHitsContext
name|context
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|innerHits
parameter_list|(
name|InnerHitsContext
name|innerHitsContext
parameter_list|)
block|{
name|context
operator|=
name|innerHitsContext
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|InnerHitsContext
name|innerHits
parameter_list|()
block|{
return|return
name|context
return|;
block|}
annotation|@
name|Override
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|mapperService
return|;
comment|// need to build / parse inner hits sort fields
block|}
annotation|@
name|Override
specifier|public
name|IndexFieldDataService
name|fieldData
parameter_list|()
block|{
return|return
name|fieldData
return|;
comment|// need to build / parse inner hits sort fields
block|}
block|}
decl_stmt|;
name|testSearchContext
operator|.
name|setTypes
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|SearchContext
operator|.
name|setCurrent
argument_list|(
name|testSearchContext
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
name|InnerHitsBuilder
operator|.
name|InnerHit
name|innerHit
init|=
operator|new
name|InnerHitsBuilder
operator|.
name|InnerHit
argument_list|()
operator|.
name|setSize
argument_list|(
literal|100
argument_list|)
operator|.
name|addSort
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
decl_stmt|;
return|return
operator|new
name|HasParentQueryBuilder
argument_list|(
name|PARENT_TYPE
argument_list|,
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|SearchContext
operator|.
name|current
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|QueryInnerHits
argument_list|(
literal|"inner_hits_name"
argument_list|,
name|innerHit
argument_list|)
argument_list|)
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
elseif|else
if|if
condition|(
name|context
operator|.
name|indexVersionCreated
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_0_beta1
argument_list|)
condition|)
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
block|}
else|else
block|{
if|if
condition|(
name|queryBuilder
operator|.
name|score
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|ParentQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ParentQuery
name|pq
init|=
operator|(
name|ParentQuery
operator|)
name|query
decl_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
operator|.
name|boost
argument_list|()
argument_list|,
name|pq
operator|.
name|getBoost
argument_list|()
argument_list|,
literal|0f
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
name|ParentConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ParentConstantScoreQuery
name|csq
init|=
operator|(
name|ParentConstantScoreQuery
operator|)
name|query
decl_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
operator|.
name|boost
argument_list|()
argument_list|,
name|csq
operator|.
name|getBoost
argument_list|()
argument_list|,
literal|0f
argument_list|)
expr_stmt|;
block|}
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
name|assertNull
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
block|}
block|}
block|}
DECL|method|testIllegalValues
specifier|public
name|void
name|testIllegalValues
parameter_list|()
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
try|try
block|{
operator|new
name|HasParentQueryBuilder
argument_list|(
literal|null
argument_list|,
name|query
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{          }
try|try
block|{
operator|new
name|HasParentQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{          }
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
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
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
name|String
name|queryAsString
init|=
name|builder
operator|.
name|string
argument_list|()
decl_stmt|;
name|QueryShardContext
name|shardContext
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|QueryParseContext
name|context
init|=
name|shardContext
operator|.
name|parseContext
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|queryAsString
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|context
operator|.
name|parseFieldMatcher
argument_list|(
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
try|try
block|{
name|context
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"type is deprecated"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Deprecated field [type] used, expected [parent_type] instead"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|key
init|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"score_mode"
argument_list|,
literal|"scoreMode"
argument_list|,
literal|"score_type"
argument_list|,
literal|"scoreType"
argument_list|)
argument_list|)
decl_stmt|;
name|builder
operator|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
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
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
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
name|key
argument_list|,
literal|"score"
argument_list|)
expr_stmt|;
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
name|queryAsString
operator|=
name|builder
operator|.
name|string
argument_list|()
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|queryAsString
argument_list|)
expr_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|context
operator|.
name|parseFieldMatcher
argument_list|(
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
try|try
block|{
name|context
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
name|fail
argument_list|(
name|key
operator|+
literal|" is deprecated"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Deprecated field ["
operator|+
name|key
operator|+
literal|"] used, replaced by [score]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

