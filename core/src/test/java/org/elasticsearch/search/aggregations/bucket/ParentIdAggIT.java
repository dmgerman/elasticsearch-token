begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
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
name|search
operator|.
name|SearchResponse
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
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
name|aggregations
operator|.
name|bucket
operator|.
name|terms
operator|.
name|Terms
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
name|ESIntegTestCase
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|matchAllQuery
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
name|assertAcked
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
name|assertSearchResponse
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

begin_class
DECL|class|ParentIdAggIT
specifier|public
class|class
name|ParentIdAggIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testParentIdAggregation
specifier|public
name|void
name|testParentIdAggregation
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|mapping
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"childtype"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_parent"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"parenttype"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"testidx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"childtype"
argument_list|,
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"testidx"
argument_list|,
literal|"childtype"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"num"
argument_list|,
literal|1
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"testidx"
argument_list|,
literal|"childtype"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"num"
argument_list|,
literal|2
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"testidx"
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"testidx"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"childtype"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|terms
argument_list|(
literal|"children"
argument_list|)
operator|.
name|field
argument_list|(
literal|"_parent#parenttype"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|assertSearchResponse
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"children"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Terms
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Terms
name|terms
init|=
operator|(
name|Terms
operator|)
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"children"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

