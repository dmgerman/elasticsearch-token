begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
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
name|analysis
operator|.
name|CannedTokenStream
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
name|Token
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
name|TokenStream
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
name|mapper
operator|.
name|DocumentMapper
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
name|DocumentMapperParser
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
name|MergeResult
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
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
comment|/**  * Test for {@link TokenCountFieldMapper}.  */
end_comment

begin_class
DECL|class|TokenCountFieldMapperTests
specifier|public
class|class
name|TokenCountFieldMapperTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|Test
DECL|method|testMerge
specifier|public
name|void
name|testMerge
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|stage1Mapping
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
literal|"person"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"tc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"token_count"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"keyword"
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
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|DocumentMapper
name|stage1
init|=
name|parser
operator|.
name|parse
argument_list|(
name|stage1Mapping
argument_list|)
decl_stmt|;
name|String
name|stage2Mapping
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
literal|"person"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"tc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"token_count"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"standard"
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
name|DocumentMapper
name|stage2
init|=
name|parser
operator|.
name|parse
argument_list|(
name|stage2Mapping
argument_list|)
decl_stmt|;
name|MergeResult
name|mergeResult
init|=
name|stage1
operator|.
name|merge
argument_list|(
name|stage2
operator|.
name|mapping
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Just simulated so merge hasn't happened yet
name|assertThat
argument_list|(
operator|(
operator|(
name|TokenCountFieldMapper
operator|)
name|stage1
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"tc"
argument_list|)
operator|)
operator|.
name|analyzer
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|mergeResult
operator|=
name|stage1
operator|.
name|merge
argument_list|(
name|stage2
operator|.
name|mapping
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Just simulated so merge hasn't happened yet
name|assertThat
argument_list|(
operator|(
operator|(
name|TokenCountFieldMapper
operator|)
name|stage1
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"tc"
argument_list|)
operator|)
operator|.
name|analyzer
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"standard"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCountPositions
specifier|public
name|void
name|testCountPositions
parameter_list|()
throws|throws
name|IOException
block|{
comment|// We're looking to make sure that we:
name|Token
name|t1
init|=
operator|new
name|Token
argument_list|()
decl_stmt|;
comment|// Don't count tokens without an increment
name|t1
operator|.
name|setPositionIncrement
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Token
name|t2
init|=
operator|new
name|Token
argument_list|()
decl_stmt|;
name|t2
operator|.
name|setPositionIncrement
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Count normal tokens with one increment
name|Token
name|t3
init|=
operator|new
name|Token
argument_list|()
decl_stmt|;
name|t2
operator|.
name|setPositionIncrement
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|// Count funny tokens with more than one increment
name|int
name|finalTokenIncrement
init|=
literal|4
decl_stmt|;
comment|// Count the final token increment on the rare token streams that have them
name|Token
index|[]
name|tokens
init|=
operator|new
name|Token
index|[]
block|{
name|t1
block|,
name|t2
block|,
name|t3
block|}
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|tokens
argument_list|)
argument_list|,
name|getRandom
argument_list|()
argument_list|)
expr_stmt|;
name|TokenStream
name|tokenStream
init|=
operator|new
name|CannedTokenStream
argument_list|(
name|finalTokenIncrement
argument_list|,
literal|0
argument_list|,
name|tokens
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|TokenCountFieldMapper
operator|.
name|countPositions
argument_list|(
name|tokenStream
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

