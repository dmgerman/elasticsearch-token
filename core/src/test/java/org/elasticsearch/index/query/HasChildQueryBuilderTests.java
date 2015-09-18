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
DECL|class|HasChildQueryBuilderTests
specifier|public
class|class
name|HasChildQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|HasChildQueryBuilder
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
name|HasChildQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|int
name|min
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|/
literal|2
argument_list|)
decl_stmt|;
name|int
name|max
init|=
name|randomIntBetween
argument_list|(
name|min
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
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
name|HasChildQueryBuilder
argument_list|(
name|CHILD_TYPE
argument_list|,
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|max
argument_list|,
name|min
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
argument_list|,
name|randomBoolean
argument_list|()
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
name|HasChildQueryBuilder
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
name|minChildren
argument_list|()
argument_list|,
name|lpq
operator|.
name|getMinChildren
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
operator|.
name|maxChildren
argument_list|()
argument_list|,
name|lpq
operator|.
name|getMaxChildren
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
operator|.
name|scoreMode
argument_list|()
argument_list|,
name|lpq
operator|.
name|getScoreMode
argument_list|()
argument_list|)
expr_stmt|;
comment|// WTF is this why do we have two?
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
name|HasChildQueryBuilder
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
name|HasChildQueryBuilder
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
name|HasChildQueryBuilder
name|foo
init|=
operator|new
name|HasChildQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
name|query
argument_list|)
decl_stmt|;
comment|// all good
try|try
block|{
name|foo
operator|.
name|scoreMode
argument_list|(
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
specifier|final
name|int
name|positiveValue
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
try|try
block|{
name|foo
operator|.
name|minChildren
argument_list|(
name|randomIntBetween
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be negative"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{          }
name|foo
operator|.
name|minChildren
argument_list|(
name|positiveValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|positiveValue
argument_list|,
name|foo
operator|.
name|minChildren
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|foo
operator|.
name|maxChildren
argument_list|(
name|randomIntBetween
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be negative"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{          }
name|foo
operator|.
name|maxChildren
argument_list|(
name|positiveValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|positiveValue
argument_list|,
name|foo
operator|.
name|maxChildren
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseFromJSON
specifier|public
name|void
name|testParseFromJSON
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/has-child-with-inner-hits.json"
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|HasChildQueryBuilder
name|queryBuilder
init|=
operator|(
name|HasChildQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|maxChildren
argument_list|()
argument_list|,
literal|1217235442
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|minChildren
argument_list|()
argument_list|,
literal|883170873
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|boost
argument_list|()
argument_list|,
literal|2.0f
argument_list|,
literal|0.0f
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|queryName
argument_list|()
argument_list|,
literal|"WNzYMJKRwePuRBh"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|childType
argument_list|()
argument_list|,
literal|"child"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|scoreMode
argument_list|()
argument_list|,
name|ScoreMode
operator|.
name|Avg
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|innerHit
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|queryBuilder
operator|.
name|innerHit
argument_list|()
argument_list|,
operator|new
name|QueryInnerHits
argument_list|(
literal|"inner_hits_name"
argument_list|,
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
literal|"mapped_string"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// now assert that we actually generate the same JSON
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
name|queryBuilder
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
name|assertEquals
argument_list|(
name|query
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
