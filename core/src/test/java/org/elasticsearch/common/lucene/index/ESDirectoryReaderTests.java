begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|index
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
name|store
operator|.
name|Directory
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_comment
comment|/** Simple tests for this filterreader */
end_comment

begin_class
DECL|class|ESDirectoryReaderTests
specifier|public
class|class
name|ESDirectoryReaderTests
extends|extends
name|ESTestCase
block|{
comment|/** Test that core cache key (needed for NRT) is working */
DECL|method|testCoreCacheKey
specifier|public
name|void
name|testCoreCacheKey
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
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
name|setMaxBufferedDocs
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|iwc
operator|.
name|setMergePolicy
argument_list|(
name|NoMergePolicy
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|IndexWriter
name|iw
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|iwc
argument_list|)
decl_stmt|;
comment|// add two docs, id:0 and id:1
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|Field
name|idField
init|=
operator|new
name|StringField
argument_list|(
literal|"id"
argument_list|,
literal|""
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|idField
argument_list|)
expr_stmt|;
name|idField
operator|.
name|setStringValue
argument_list|(
literal|"0"
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|idField
operator|.
name|setStringValue
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
comment|// open reader
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"fake"
argument_list|,
literal|"_na_"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|DirectoryReader
name|ir
init|=
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|iw
argument_list|)
argument_list|,
name|shardId
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|ir
operator|.
name|numDocs
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ir
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// delete id:0 and reopen
name|iw
operator|.
name|deleteDocuments
argument_list|(
operator|new
name|Term
argument_list|(
literal|"id"
argument_list|,
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|DirectoryReader
name|ir2
init|=
name|DirectoryReader
operator|.
name|openIfChanged
argument_list|(
name|ir
argument_list|)
decl_stmt|;
comment|// we should have the same cache key as before
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ir2
operator|.
name|numDocs
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ir2
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ir
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheHelper
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|,
name|ir2
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheHelper
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|ir
argument_list|,
name|ir2
argument_list|,
name|iw
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

