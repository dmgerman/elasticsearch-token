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
name|index
operator|.
name|IndexReader
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
name|MultiReader
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
name|MappedFieldType
operator|.
name|Relation
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
operator|.
name|MergeReason
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
name|ESSingleNodeTestCase
import|;
end_import

begin_comment
comment|// The purpose of this test case is to test RangeQueryBuilder.getRelation()
end_comment

begin_comment
comment|// Whether it should return INTERSECT/DISJOINT/WITHIN is already tested in
end_comment

begin_comment
comment|// RangeQueryBuilderTests
end_comment

begin_class
DECL|class|RangeQueryRewriteTests
specifier|public
class|class
name|RangeQueryRewriteTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testRewriteMissingField
specifier|public
name|void
name|testRewriteMissingField
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexReader
name|reader
init|=
operator|new
name|MultiReader
argument_list|()
decl_stmt|;
name|QueryRewriteContext
name|context
init|=
operator|new
name|QueryRewriteContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|reader
argument_list|)
decl_stmt|;
name|RangeQueryBuilder
name|range
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Relation
operator|.
name|DISJOINT
argument_list|,
name|range
operator|.
name|getRelation
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRewriteMissingReader
specifier|public
name|void
name|testRewriteMissingReader
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"date"
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|,
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|QueryRewriteContext
name|context
init|=
operator|new
name|QueryRewriteContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RangeQueryBuilder
name|range
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
comment|// can't make assumptions on a missing reader, so it must return INTERSECT
name|assertEquals
argument_list|(
name|Relation
operator|.
name|INTERSECTS
argument_list|,
name|range
operator|.
name|getRelation
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRewriteEmptyReader
specifier|public
name|void
name|testRewriteEmptyReader
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"date"
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|,
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
operator|new
name|MultiReader
argument_list|()
decl_stmt|;
name|QueryRewriteContext
name|context
init|=
operator|new
name|QueryRewriteContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|reader
argument_list|)
decl_stmt|;
name|RangeQueryBuilder
name|range
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
comment|// no values -> DISJOINT
name|assertEquals
argument_list|(
name|Relation
operator|.
name|DISJOINT
argument_list|,
name|range
operator|.
name|getRelation
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

