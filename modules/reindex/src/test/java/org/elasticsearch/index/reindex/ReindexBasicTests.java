begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexRequestBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
import|;
end_import

begin_class
DECL|class|ReindexBasicTests
specifier|public
class|class
name|ReindexBasicTests
extends|extends
name|ReindexTestCase
block|{
DECL|method|testFiltering
specifier|public
name|void
name|testFiltering
parameter_list|()
throws|throws
name|Exception
block|{
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"source"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"source"
argument_list|,
literal|"test"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"source"
argument_list|,
literal|"test"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"b"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"source"
argument_list|,
literal|"test"
argument_list|,
literal|"4"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"source"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// Copy all the docs
name|ReindexRequestBuilder
name|copy
init|=
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
literal|"all"
argument_list|)
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|copy
operator|.
name|get
argument_list|()
argument_list|,
name|reindexResponseMatcher
argument_list|()
operator|.
name|created
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"all"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// Now none of them
name|copy
operator|=
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"all"
argument_list|,
literal|"none"
argument_list|)
operator|.
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"no_match"
argument_list|)
argument_list|)
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|copy
operator|.
name|get
argument_list|()
argument_list|,
name|reindexResponseMatcher
argument_list|()
operator|.
name|created
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"none"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Now half of them
name|copy
operator|=
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
literal|"half"
argument_list|)
operator|.
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|copy
operator|.
name|get
argument_list|()
argument_list|,
name|reindexResponseMatcher
argument_list|()
operator|.
name|created
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"half"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// Limit with size
name|copy
operator|=
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
literal|"size_one"
argument_list|)
operator|.
name|size
argument_list|(
literal|1
argument_list|)
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|copy
operator|.
name|get
argument_list|()
argument_list|,
name|reindexResponseMatcher
argument_list|()
operator|.
name|created
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"size_one"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|testCopyMany
specifier|public
name|void
name|testCopyMany
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|max
init|=
name|between
argument_list|(
literal|150
argument_list|,
literal|500
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|max
condition|;
name|i
operator|++
control|)
block|{
name|docs
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"source"
argument_list|,
literal|"test"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"source"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|max
argument_list|)
expr_stmt|;
comment|// Copy all the docs
name|ReindexRequestBuilder
name|copy
init|=
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
literal|"all"
argument_list|)
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|// Use a small batch size so we have to use more than one batch
name|copy
operator|.
name|source
argument_list|()
operator|.
name|setSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|copy
operator|.
name|get
argument_list|()
argument_list|,
name|reindexResponseMatcher
argument_list|()
operator|.
name|created
argument_list|(
name|max
argument_list|)
operator|.
name|batches
argument_list|(
name|max
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"all"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|max
argument_list|)
expr_stmt|;
comment|// Copy some of the docs
name|int
name|half
init|=
name|max
operator|/
literal|2
decl_stmt|;
name|copy
operator|=
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
literal|"half"
argument_list|)
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Use a small batch size so we have to use more than one batch
name|copy
operator|.
name|source
argument_list|()
operator|.
name|setSize
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|copy
operator|.
name|size
argument_list|(
name|half
argument_list|)
expr_stmt|;
comment|// The real "size" of the request.
name|assertThat
argument_list|(
name|copy
operator|.
name|get
argument_list|()
argument_list|,
name|reindexResponseMatcher
argument_list|()
operator|.
name|created
argument_list|(
name|half
argument_list|)
operator|.
name|batches
argument_list|(
name|half
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"half"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|half
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

