begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.nested
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
operator|.
name|nested
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
name|Document
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
name|index
operator|.
name|DirectoryReader
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
name|IndexWriterConfig
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
name|NoMergePolicy
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
name|RandomIndexWriter
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
name|Term
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
name|BooleanClause
operator|.
name|Occur
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
name|BooleanQuery
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
name|ConstantScoreQuery
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
name|IndexSearcher
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
name|QueryCachingPolicy
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
name|TermQuery
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
name|store
operator|.
name|Directory
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
name|CompressedString
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
name|lucene
operator|.
name|search
operator|.
name|Queries
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
name|IndexService
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
name|TypeFieldMapper
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
name|UidFieldMapper
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
name|Aggregator
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
name|AggregatorFactories
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
name|BucketCollector
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
name|SearchContextAggregations
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
name|support
operator|.
name|AggregationContext
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
name|test
operator|.
name|ElasticsearchSingleNodeTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|Arrays
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|NestedAggregatorTest
specifier|public
class|class
name|NestedAggregatorTest
extends|extends
name|ElasticsearchSingleNodeTest
block|{
annotation|@
name|Test
DECL|method|testResetRootDocId
specifier|public
name|void
name|testResetRootDocId
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|iwc
init|=
operator|new
name|IndexWriterConfig
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|iwc
operator|.
name|setMergePolicy
argument_list|(
name|NoMergePolicy
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|RandomIndexWriter
name|indexWriter
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|directory
argument_list|,
name|iwc
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Document
argument_list|>
name|documents
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// 1 segment with, 1 root document, with 3 nested sub docs
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"__nested_field"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"__nested_field"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"__nested_field"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"test"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocuments
argument_list|(
name|documents
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|documents
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// 1 segment with:
comment|// 1 document, with 1 nested subdoc
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#2"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"__nested_field"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#2"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"test"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocuments
argument_list|(
name|documents
argument_list|)
expr_stmt|;
name|documents
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// and 1 document, with 1 nested subdoc
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#3"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"__nested_field"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#3"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"test"
argument_list|,
name|TypeFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocuments
argument_list|(
name|documents
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|DirectoryReader
name|directoryReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|directoryReader
argument_list|)
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"test"
argument_list|,
operator|new
name|CompressedString
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
literal|"test"
argument_list|,
literal|"nested_field"
argument_list|,
literal|"type=nested"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|SearchContext
name|searchContext
init|=
name|createSearchContext
argument_list|(
name|indexService
argument_list|)
decl_stmt|;
name|AggregationContext
name|context
init|=
operator|new
name|AggregationContext
argument_list|(
name|searchContext
argument_list|)
decl_stmt|;
name|AggregatorFactories
operator|.
name|Builder
name|builder
init|=
name|AggregatorFactories
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
operator|new
name|NestedAggregator
operator|.
name|Factory
argument_list|(
literal|"test"
argument_list|,
literal|"nested_field"
argument_list|,
name|QueryCachingPolicy
operator|.
name|ALWAYS_CACHE
argument_list|)
argument_list|)
expr_stmt|;
name|AggregatorFactories
name|factories
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|searchContext
operator|.
name|aggregations
argument_list|(
operator|new
name|SearchContextAggregations
argument_list|(
name|factories
argument_list|)
argument_list|)
expr_stmt|;
name|Aggregator
index|[]
name|aggs
init|=
name|factories
operator|.
name|createTopLevelAggregators
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|BucketCollector
name|collector
init|=
name|BucketCollector
operator|.
name|wrap
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|aggs
argument_list|)
argument_list|)
decl_stmt|;
name|collector
operator|.
name|preCollection
argument_list|()
expr_stmt|;
comment|// A regular search always exclude nested docs, so we use NonNestedDocsFilter.INSTANCE here (otherwise MatchAllDocsQuery would be sufficient)
comment|// We exclude root doc with uid type#2, this will trigger the bug if we don't reset the root doc when we process a new segment, because
comment|// root doc type#3 and root doc type#1 have the same segment docid
name|BooleanQuery
name|bq
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
name|bq
operator|.
name|add
argument_list|(
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|,
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|bq
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type#2"
argument_list|)
argument_list|)
argument_list|,
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|ConstantScoreQuery
argument_list|(
name|bq
argument_list|)
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|collector
operator|.
name|postCollection
argument_list|()
expr_stmt|;
name|Nested
name|nested
init|=
operator|(
name|Nested
operator|)
name|aggs
index|[
literal|0
index|]
operator|.
name|buildAggregation
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// The bug manifests if 6 docs are returned, because currentRootDoc isn't reset the previous child docs from the first segment are emitted as hits.
name|assertThat
argument_list|(
name|nested
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|directoryReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|directory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

