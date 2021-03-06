begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
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
name|StringField
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
name|CompositeReaderContext
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
name|IndexWriter
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
name|LeafReaderContext
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
name|BaseDirectoryWrapper
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
name|util
operator|.
name|IOUtils
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
name|index
operator|.
name|ElasticsearchDirectoryReader
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
name|engine
operator|.
name|Engine
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
name|ESTestCase
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

begin_class
DECL|class|ShardUtilsTests
specifier|public
class|class
name|ShardUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testExtractShardId
specifier|public
name|void
name|testExtractShardId
parameter_list|()
throws|throws
name|IOException
block|{
name|BaseDirectoryWrapper
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|ShardId
name|id
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|"_na_"
argument_list|,
name|random
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|)
init|)
block|{
name|ElasticsearchDirectoryReader
name|wrap
init|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|reader
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|id
argument_list|,
name|ShardUtils
operator|.
name|extractShardId
argument_list|(
name|wrap
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|numDocs
init|=
literal|1
operator|+
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|5
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
name|newField
argument_list|(
literal|"name"
argument_list|,
literal|"foobar"
argument_list|,
name|StringField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
block|}
try|try
init|(
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|)
init|)
block|{
name|ElasticsearchDirectoryReader
name|wrap
init|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|reader
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|id
argument_list|,
name|ShardUtils
operator|.
name|extractShardId
argument_list|(
name|wrap
argument_list|)
argument_list|)
expr_stmt|;
name|CompositeReaderContext
name|context
init|=
name|wrap
operator|.
name|getContext
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|leaf
range|:
name|context
operator|.
name|leaves
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|id
argument_list|,
name|ShardUtils
operator|.
name|extractShardId
argument_list|(
name|leaf
operator|.
name|reader
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|IOUtils
operator|.
name|close
argument_list|(
name|writer
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
DECL|method|getShardEngine
specifier|public
specifier|static
name|Engine
name|getShardEngine
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
return|return
name|shard
operator|.
name|getEngine
argument_list|()
return|;
block|}
block|}
end_class

end_unit

