begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
package|;
end_package

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
name|search
operator|.
name|SearchRequest
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
name|AliasMetaData
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
name|bytes
operator|.
name|BytesArray
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableAwareStreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|query
operator|.
name|QueryBuilder
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
name|QueryBuilders
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
name|QueryParseContext
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
name|QueryShardContext
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
name|RandomQueryBuilder
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
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|InvalidAliasNameException
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
name|AbstractSearchTestCase
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
name|Base64
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|Matchers
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
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|ShardSearchTransportRequestTests
specifier|public
class|class
name|ShardSearchTransportRequestTests
extends|extends
name|AbstractSearchTestCase
block|{
DECL|field|baseMetaData
specifier|private
name|IndexMetaData
name|baseMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
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
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|ShardSearchTransportRequest
name|shardSearchTransportRequest
init|=
name|createShardSearchTransportRequest
argument_list|()
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|shardSearchTransportRequest
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|)
init|)
block|{
name|ShardSearchTransportRequest
name|deserializedRequest
init|=
operator|new
name|ShardSearchTransportRequest
argument_list|()
decl_stmt|;
name|deserializedRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|scroll
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|scroll
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|deserializedRequest
operator|.
name|indices
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|deserializedRequest
operator|.
name|types
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|isProfile
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|isProfile
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|nowInMillis
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|nowInMillis
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|source
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|searchType
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|searchType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|numberOfShards
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|numberOfShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|cacheKey
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|cacheKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|deserializedRequest
argument_list|,
name|shardSearchTransportRequest
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|indexBoost
argument_list|()
argument_list|,
name|shardSearchTransportRequest
operator|.
name|indexBoost
argument_list|()
argument_list|,
literal|0.0f
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|createShardSearchTransportRequest
specifier|private
name|ShardSearchTransportRequest
name|createShardSearchTransportRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|SearchRequest
name|searchRequest
init|=
name|createSearchRequest
argument_list|()
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomInt
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|AliasFilter
name|filteringAliases
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|String
index|[]
name|strings
init|=
name|generateRandomStringArray
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|filteringAliases
operator|=
operator|new
name|AliasFilter
argument_list|(
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|strings
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|filteringAliases
operator|=
operator|new
name|AliasFilter
argument_list|(
literal|null
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ShardSearchTransportRequest
argument_list|(
name|searchRequest
argument_list|,
name|shardId
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|,
name|filteringAliases
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|1.0f
else|:
name|randomFloat
argument_list|()
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|randomLong
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
DECL|method|testFilteringAliases
specifier|public
name|void
name|testFilteringAliases
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|baseMetaData
decl_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"all"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexMetaData
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"cats"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexMetaData
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"dogs"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexMetaData
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"turtles"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|)
argument_list|,
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|,
literal|"dogs"
argument_list|)
argument_list|,
name|QueryBuilders
operator|.
name|boolQuery
argument_list|()
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Non-filtering alias should turn off all filters because filters are ORed
name|assertThat
argument_list|(
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"all"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|,
literal|"all"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"all"
argument_list|,
literal|"cats"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"feline"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"canine"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"dogs"
argument_list|,
literal|"cats"
argument_list|)
argument_list|,
name|QueryBuilders
operator|.
name|boolQuery
argument_list|()
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"canine"
argument_list|)
argument_list|)
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"feline"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemovedAliasFilter
specifier|public
name|void
name|testRemovedAliasFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|baseMetaData
decl_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|remove
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|)
expr_stmt|;
try|try
block|{
name|aliasFilter
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected InvalidAliasNameException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidAliasNameException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Invalid alias name [cats]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnknownAliasFilter
specifier|public
name|void
name|testUnknownAliasFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|baseMetaData
decl_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexMetaData
operator|=
name|add
argument_list|(
name|indexMetaData
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IndexMetaData
name|finalIndexMetadata
init|=
name|indexMetaData
decl_stmt|;
name|expectThrows
argument_list|(
name|InvalidAliasNameException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|aliasFilter
argument_list|(
name|finalIndexMetadata
argument_list|,
literal|"unknown"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|filter
specifier|public
specifier|static
name|CompressedXContent
name|filter
parameter_list|(
name|QueryBuilder
name|filterBuilder
parameter_list|)
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
decl_stmt|;
name|filterBuilder
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
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|CompressedXContent
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
DECL|method|remove
specifier|private
name|IndexMetaData
name|remove
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|IndexMetaData
name|build
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|removeAlias
argument_list|(
name|alias
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|build
return|;
block|}
DECL|method|add
specifier|private
name|IndexMetaData
name|add
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|String
name|alias
parameter_list|,
annotation|@
name|Nullable
name|CompressedXContent
name|filter
parameter_list|)
block|{
return|return
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
name|alias
argument_list|)
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|aliasFilter
specifier|public
name|QueryBuilder
name|aliasFilter
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|String
modifier|...
name|aliasNames
parameter_list|)
block|{
name|Function
argument_list|<
name|XContentParser
argument_list|,
name|QueryParseContext
argument_list|>
name|contextFactory
init|=
parameter_list|(
name|p
parameter_list|)
lambda|->
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|p
argument_list|,
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|ShardSearchRequest
operator|.
name|parseAliasFilter
argument_list|(
name|contextFactory
argument_list|,
name|indexMetaData
argument_list|,
name|aliasNames
argument_list|)
return|;
block|}
comment|// BWC test for changes from #20916
DECL|method|testSerialize50Request
specifier|public
name|void
name|testSerialize50Request
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesArray
name|requestBytes
init|=
operator|new
name|BytesArray
argument_list|(
name|Base64
operator|.
name|getDecoder
argument_list|()
comment|// this is a base64 encoded request generated with the same input
operator|.
name|decode
argument_list|(
literal|"AAh4cXptdEhJcgdnT0d1ZldWyfL/sgQBJAHkDAMBAAIBAQ4TWlljWlZ5TkVmRU5xQnFQVHBjVBRZbUpod2pRV2dDSXVxRXpRaEdGVBRFZWFJY0plT2hn"
operator|+
literal|"UEpISFhmSXR6Qw5XZ1hQcmFidWhWalFSQghuUWNwZ2JjQxBtZldRREJPaGF3UnlQSE56EVhQSUtRa25Iekh3bU5kbGVECWlFT2NIeEh3RgZIYXpMTWgUeGJq"
operator|+
literal|"VU9Tdkdua3RORU5QZkNrb1EOalRyWGh5WXhvZ3plV2UUcWlXZFl2eUFUSXdPVGdMUUtYTHAJU3RKR3JxQkVJEkdEQ01xUHpnWWNaT3N3U3prSRIUeURlVFpM"
operator|+
literal|"Q1lBZERZcWpDb3NOVWIST1NyQlZtdUNrd0F1UXRvdVRjEGp6RlVMd1dqc3VtUVNaTk0JT3N2cnpLQ3ZLBmRpS1J6cgdYbmVhZnBxBUlTUU9pEEJMcm1ERXVs"
operator|+
literal|"eXhESlBoVkgTaWdUUmtVZGh4d0FFc2ZKRm9ZahNrb01XTnFFd2NWSVVDU3pWS2xBC3JVTWV3V2tUUWJUE3VGQU1Hd21CYUFMTmNQZkxobXUIZ3dxWHBxWXcF"
operator|+
literal|"bmNDZUEOTFBSTEpYZVF6Z3d2eE0PV1BucUFacll6WWRxa1hCDGxkbXNMaVRzcUZXbAtSY0NsY3FNdlJQcv8BAP////8PAQAAARQAAQp5THlIcHdQeGtMAAAB"
operator|+
literal|"AQAAAAEDbkVLAQMBCgACAAADAQABAAAAAQhIc25wRGxQbwEBQgABAAACAQMAAAEIAAAJMF9OSG9kSmh2HwABAwljRW5MVWxFbVQFemlxWG8KcXZQTkRUUGJk"
operator|+
literal|"bgECCkpMbXVMT1dtVnkISEdUUHhsd0cBAAEJAAABA2lkcz+rKsUAAAAAAAAAAAECAQYAAgwxX0ZlRWxSQkhzQ07/////DwABAAEDCnRyYXFHR1hjVHkKTERY"
operator|+
literal|"aE1HRWVySghuSWtzbEtXUwABCgEHSlRwQnhwdwAAAQECAgAAAAAAAQcyX3FlYmNDGQEEBklxZU9iUQdTc01Gek5YCWlMd2xuamNRQwNiVncAAUHt61kAAQR0"
operator|+
literal|"ZXJtP4AAAAANbUtDSnpHU3lidm5KUBUMaVpqeG9vcm5QSFlvAAEBLGdtcWxuRWpWTXdvTlhMSHh0RWlFdHBnbEF1cUNmVmhoUVlwRFZxVllnWWV1A2ZvbwEA"
operator|+
literal|"AQhwYWlubGVzc/8AALk4AAAAAAABAAAAAAAAAwpKU09PU0ZmWnhFClVqTGxMa2p3V2gKdUJwZ3R3dXFER5Hg97uT7MOmPgEADw"
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|requestBytes
operator|.
name|streamInput
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|)
init|)
block|{
name|in
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
expr_stmt|;
name|ShardSearchTransportRequest
name|readRequest
init|=
operator|new
name|ShardSearchTransportRequest
argument_list|()
decl_stmt|;
name|readRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|in
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|IllegalStateException
name|illegalStateException
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|readRequest
operator|.
name|filteringAliases
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"alias filter for aliases: [JSOOSFfZxE, UjLlLkjwWh, uBpgtwuqDG] must be rewritten first"
argument_list|,
name|illegalStateException
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|IndexMetaData
operator|.
name|Builder
name|indexMetadata
init|=
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
name|baseMetaData
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|newAliasMetaDataBuilder
argument_list|(
literal|"JSOOSFfZxE"
argument_list|)
operator|.
name|filter
argument_list|(
literal|"{\"term\" : {\"foo\" : \"bar\"}}"
argument_list|)
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|newAliasMetaDataBuilder
argument_list|(
literal|"UjLlLkjwWh"
argument_list|)
operator|.
name|filter
argument_list|(
literal|"{\"term\" : {\"foo\" : \"bar1\"}}"
argument_list|)
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|newAliasMetaDataBuilder
argument_list|(
literal|"uBpgtwuqDG"
argument_list|)
operator|.
name|filter
argument_list|(
literal|"{\"term\" : {\"foo\" : \"bar2\"}}"
argument_list|)
argument_list|)
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
operator|new
name|IndexSettings
argument_list|(
name|indexMetadata
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
specifier|final
name|long
name|nowInMillis
init|=
name|randomPositiveLong
argument_list|()
decl_stmt|;
name|QueryShardContext
name|context
init|=
operator|new
name|QueryShardContext
argument_list|(
literal|0
argument_list|,
name|indexSettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|queriesRegistry
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
name|nowInMillis
argument_list|)
decl_stmt|;
name|readRequest
operator|.
name|rewrite
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|QueryBuilder
name|queryBuilder
init|=
name|readRequest
operator|.
name|filteringAliases
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
argument_list|,
name|QueryBuilders
operator|.
name|boolQuery
argument_list|()
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"bar1"
argument_list|)
argument_list|)
operator|.
name|should
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"bar2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|output
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
expr_stmt|;
name|readRequest
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
operator|.
name|toBytesRef
argument_list|()
argument_list|,
name|requestBytes
operator|.
name|toBytesRef
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// BWC test for changes from #21393
DECL|method|testSerialize50RequestForIndexBoost
specifier|public
name|void
name|testSerialize50RequestForIndexBoost
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesArray
name|requestBytes
init|=
operator|new
name|BytesArray
argument_list|(
name|Base64
operator|.
name|getDecoder
argument_list|()
comment|// this is a base64 encoded request generated with the same input
operator|.
name|decode
argument_list|(
literal|"AAZpbmRleDEWTjEyM2trbHFUT21XZDY1Z2VDYlo5ZwABBAABAAIA/wD/////DwABBmluZGV4MUAAAAAAAAAAAP////8PAAAAAAAAAgAAAA"
operator|+
literal|"AAAPa/q8mOKwIAJg=="
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|requestBytes
operator|.
name|streamInput
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|)
init|)
block|{
name|in
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
expr_stmt|;
name|ShardSearchTransportRequest
name|readRequest
init|=
operator|new
name|ShardSearchTransportRequest
argument_list|()
decl_stmt|;
name|readRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|in
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2.0f
argument_list|,
name|readRequest
operator|.
name|indexBoost
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|output
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
expr_stmt|;
name|readRequest
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
operator|.
name|toBytesRef
argument_list|()
argument_list|,
name|requestBytes
operator|.
name|toBytesRef
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

