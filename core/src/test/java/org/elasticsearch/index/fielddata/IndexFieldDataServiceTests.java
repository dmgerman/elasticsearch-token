begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|core
operator|.
name|KeywordAnalyzer
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
operator|.
name|Store
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
name|*
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
name|RAMDirectory
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
name|Accountable
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
name|fielddata
operator|.
name|plain
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
name|mapper
operator|.
name|ContentPath
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
name|Names
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
name|Mapper
operator|.
name|BuilderContext
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
name|MapperBuilders
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
name|core
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
name|Index
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
name|breaker
operator|.
name|CircuitBreakerService
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
name|fielddata
operator|.
name|cache
operator|.
name|IndicesFieldDataCache
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
name|elasticsearch
operator|.
name|test
operator|.
name|IndexSettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|instanceOf
import|;
end_import

begin_class
DECL|class|IndexFieldDataServiceTests
specifier|public
class|class
name|IndexFieldDataServiceTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testGetForFieldDefaults
specifier|public
name|void
name|testGetForFieldDefaults
parameter_list|()
block|{
specifier|final
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|final
name|IndexFieldDataService
name|ifdService
init|=
name|indexService
operator|.
name|fieldData
argument_list|()
decl_stmt|;
specifier|final
name|BuilderContext
name|ctx
init|=
operator|new
name|BuilderContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|MappedFieldType
name|stringMapper
init|=
operator|new
name|StringFieldMapper
operator|.
name|Builder
argument_list|(
literal|"string"
argument_list|)
operator|.
name|tokenized
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|fd
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|stringMapper
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fd
operator|instanceof
name|SortedSetDVOrdinalsIndexFieldData
argument_list|)
expr_stmt|;
for|for
control|(
name|MappedFieldType
name|mapper
range|:
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|ByteFieldMapper
operator|.
name|Builder
argument_list|(
literal|"int"
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
argument_list|,
operator|new
name|ShortFieldMapper
operator|.
name|Builder
argument_list|(
literal|"int"
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
argument_list|,
operator|new
name|IntegerFieldMapper
operator|.
name|Builder
argument_list|(
literal|"int"
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
argument_list|,
operator|new
name|LongFieldMapper
operator|.
name|Builder
argument_list|(
literal|"long"
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
argument_list|)
control|)
block|{
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fd
operator|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fd
operator|instanceof
name|SortedNumericDVIndexFieldData
argument_list|)
expr_stmt|;
block|}
specifier|final
name|MappedFieldType
name|floatMapper
init|=
operator|new
name|FloatFieldMapper
operator|.
name|Builder
argument_list|(
literal|"float"
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fd
operator|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|floatMapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fd
operator|instanceof
name|SortedNumericDVIndexFieldData
argument_list|)
expr_stmt|;
specifier|final
name|MappedFieldType
name|doubleMapper
init|=
operator|new
name|DoubleFieldMapper
operator|.
name|Builder
argument_list|(
literal|"double"
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fd
operator|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|doubleMapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fd
operator|instanceof
name|SortedNumericDVIndexFieldData
argument_list|)
expr_stmt|;
block|}
DECL|method|testChangeFieldDataFormat
specifier|public
name|void
name|testChangeFieldDataFormat
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|final
name|IndexFieldDataService
name|ifdService
init|=
name|indexService
operator|.
name|fieldData
argument_list|()
decl_stmt|;
specifier|final
name|BuilderContext
name|ctx
init|=
operator|new
name|BuilderContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|MappedFieldType
name|mapper1
init|=
name|MapperBuilders
operator|.
name|stringField
argument_list|(
literal|"s"
argument_list|)
operator|.
name|tokenized
argument_list|(
literal|false
argument_list|)
operator|.
name|docValues
argument_list|(
literal|true
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|FieldDataType
operator|.
name|FORMAT_KEY
argument_list|,
literal|"paged_bytes"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
specifier|final
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
operator|new
name|RAMDirectory
argument_list|()
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
operator|new
name|KeywordAnalyzer
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"s"
argument_list|,
literal|"thisisastring"
argument_list|,
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|IndexReader
name|reader1
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|ifd
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|mapper1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ifd
argument_list|,
name|instanceOf
argument_list|(
name|PagedBytesIndexFieldData
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|LeafReader
argument_list|>
name|oldSegments
init|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|IdentityHashMap
argument_list|<
name|LeafReader
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|arc
range|:
name|reader1
operator|.
name|leaves
argument_list|()
control|)
block|{
name|oldSegments
operator|.
name|add
argument_list|(
name|arc
operator|.
name|reader
argument_list|()
argument_list|)
expr_stmt|;
name|AtomicFieldData
name|afd
init|=
name|ifd
operator|.
name|load
argument_list|(
name|arc
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|afd
argument_list|,
name|instanceOf
argument_list|(
name|PagedBytesAtomicFieldData
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// write new segment
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|IndexReader
name|reader2
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|MappedFieldType
name|mapper2
init|=
name|MapperBuilders
operator|.
name|stringField
argument_list|(
literal|"s"
argument_list|)
operator|.
name|tokenized
argument_list|(
literal|false
argument_list|)
operator|.
name|docValues
argument_list|(
literal|true
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|FieldDataType
operator|.
name|FORMAT_KEY
argument_list|,
literal|"doc_values"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ifd
operator|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|mapper2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ifd
argument_list|,
name|instanceOf
argument_list|(
name|SortedSetDVOrdinalsIndexFieldData
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|reader1
operator|.
name|close
argument_list|()
expr_stmt|;
name|reader2
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
operator|.
name|getDirectory
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testFieldDataCacheListener
specifier|public
name|void
name|testFieldDataCacheListener
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexFieldDataService
name|shardPrivateService
init|=
name|indexService
operator|.
name|fieldData
argument_list|()
decl_stmt|;
comment|// copy the ifdService since we can set the listener only once.
specifier|final
name|IndexFieldDataService
name|ifdService
init|=
operator|new
name|IndexFieldDataService
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|getInstanceFromNode
argument_list|(
name|IndicesFieldDataCache
operator|.
name|class
argument_list|)
argument_list|,
name|getInstanceFromNode
argument_list|(
name|CircuitBreakerService
operator|.
name|class
argument_list|)
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|BuilderContext
name|ctx
init|=
operator|new
name|BuilderContext
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|MappedFieldType
name|mapper1
init|=
name|MapperBuilders
operator|.
name|stringField
argument_list|(
literal|"s"
argument_list|)
operator|.
name|tokenized
argument_list|(
literal|false
argument_list|)
operator|.
name|docValues
argument_list|(
literal|true
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|FieldDataType
operator|.
name|FORMAT_KEY
argument_list|,
literal|"paged_bytes"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|ctx
argument_list|)
operator|.
name|fieldType
argument_list|()
decl_stmt|;
specifier|final
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
operator|new
name|RAMDirectory
argument_list|()
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
operator|new
name|KeywordAnalyzer
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"s"
argument_list|,
literal|"thisisastring"
argument_list|,
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|DirectoryReader
name|open
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|wrap
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|IndexReader
name|reader
init|=
name|wrap
condition|?
name|ElasticsearchDirectoryReader
operator|.
name|wrap
argument_list|(
name|open
argument_list|,
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
argument_list|)
else|:
name|open
decl_stmt|;
specifier|final
name|AtomicInteger
name|onCacheCalled
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|onRemovalCalled
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|ifdService
operator|.
name|setListener
argument_list|(
operator|new
name|IndexFieldDataCache
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onCache
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|MappedFieldType
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|Accountable
name|ramUsage
parameter_list|)
block|{
if|if
condition|(
name|wrap
condition|)
block|{
name|assertEquals
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
name|onCacheCalled
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|MappedFieldType
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|boolean
name|wasEvicted
parameter_list|,
name|long
name|sizeInBytes
parameter_list|)
block|{
if|if
condition|(
name|wrap
condition|)
block|{
name|assertEquals
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
name|onRemovalCalled
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|ifd
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
name|mapper1
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|leafReaderContext
init|=
name|reader
operator|.
name|getContext
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicFieldData
name|load
init|=
name|ifd
operator|.
name|load
argument_list|(
name|leafReaderContext
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|onCacheCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|onRemovalCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|load
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|onCacheCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|onRemovalCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ifdService
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|testSetCacheListenerTwice
specifier|public
name|void
name|testSetCacheListenerTwice
parameter_list|()
block|{
specifier|final
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexFieldDataService
name|shardPrivateService
init|=
name|indexService
operator|.
name|fieldData
argument_list|()
decl_stmt|;
try|try
block|{
name|shardPrivateService
operator|.
name|setListener
argument_list|(
operator|new
name|IndexFieldDataCache
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onCache
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|MappedFieldType
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|Accountable
name|ramUsage
parameter_list|)
block|{                  }
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|MappedFieldType
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|boolean
name|wasEvicted
parameter_list|,
name|long
name|sizeInBytes
parameter_list|)
block|{                  }
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"listener already set"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
comment|// all well
block|}
block|}
DECL|method|doTestRequireDocValues
specifier|private
name|void
name|doTestRequireDocValues
parameter_list|(
name|MappedFieldType
name|ft
parameter_list|)
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"random_threadpool_name"
argument_list|)
decl_stmt|;
try|try
block|{
name|IndicesFieldDataCache
name|cache
init|=
operator|new
name|IndicesFieldDataCache
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|null
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|IndexFieldDataService
name|ifds
init|=
operator|new
name|IndexFieldDataService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
argument_list|,
name|cache
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ft
operator|.
name|setNames
argument_list|(
operator|new
name|Names
argument_list|(
literal|"some_long"
argument_list|)
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ifds
operator|.
name|getForField
argument_list|(
name|ft
argument_list|)
expr_stmt|;
comment|// no exception
name|ft
operator|.
name|setHasDocValues
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|ifds
operator|.
name|getForField
argument_list|(
name|ft
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
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
literal|"doc values"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|testRequireDocValuesOnLongs
specifier|public
name|void
name|testRequireDocValuesOnLongs
parameter_list|()
block|{
name|doTestRequireDocValues
argument_list|(
operator|new
name|LongFieldMapper
operator|.
name|LongFieldType
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRequireDocValuesOnDoubles
specifier|public
name|void
name|testRequireDocValuesOnDoubles
parameter_list|()
block|{
name|doTestRequireDocValues
argument_list|(
operator|new
name|DoubleFieldMapper
operator|.
name|DoubleFieldType
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRequireDocValuesOnBools
specifier|public
name|void
name|testRequireDocValuesOnBools
parameter_list|()
block|{
name|doTestRequireDocValues
argument_list|(
operator|new
name|BooleanFieldMapper
operator|.
name|BooleanFieldType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

