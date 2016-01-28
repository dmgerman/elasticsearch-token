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
name|SortedSetDocValuesField
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
name|BytesRef
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
name|PagedBytesIndexFieldData
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
name|SortedSetDVOrdinalsIndexFieldData
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
name|core
operator|.
name|StringFieldMapper
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
name|NoneCircuitBreakerService
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|FieldMaskingReader
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

begin_class
DECL|class|FieldDataCacheTests
specifier|public
class|class
name|FieldDataCacheTests
extends|extends
name|ESTestCase
block|{
DECL|method|testLoadGlobal_neverCacheIfFieldIsMissing
specifier|public
name|void
name|testLoadGlobal_neverCacheIfFieldIsMissing
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
name|long
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|32
argument_list|,
literal|128
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
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
name|SortedSetDocValuesField
argument_list|(
literal|"field1"
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"field2"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|24
operator|==
literal|0
condition|)
block|{
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
block|}
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|dir
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
literal|"_index"
argument_list|,
literal|"_na_"
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|DummyAccountingFieldDataCache
name|fieldDataCache
init|=
operator|new
name|DummyAccountingFieldDataCache
argument_list|()
decl_stmt|;
comment|// Testing SortedSetDVOrdinalsIndexFieldData:
name|SortedSetDVOrdinalsIndexFieldData
name|sortedSetDVOrdinalsIndexFieldData
init|=
name|createSortedDV
argument_list|(
literal|"field1"
argument_list|,
name|fieldDataCache
argument_list|)
decl_stmt|;
name|sortedSetDVOrdinalsIndexFieldData
operator|.
name|loadGlobal
argument_list|(
name|ir
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldDataCache
operator|.
name|cachedGlobally
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|sortedSetDVOrdinalsIndexFieldData
operator|.
name|loadGlobal
argument_list|(
operator|new
name|FieldMaskingReader
argument_list|(
literal|"field1"
argument_list|,
name|ir
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldDataCache
operator|.
name|cachedGlobally
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Testing PagedBytesIndexFieldData
name|PagedBytesIndexFieldData
name|pagedBytesIndexFieldData
init|=
name|createPagedBytes
argument_list|(
literal|"field2"
argument_list|,
name|fieldDataCache
argument_list|)
decl_stmt|;
name|pagedBytesIndexFieldData
operator|.
name|loadGlobal
argument_list|(
name|ir
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldDataCache
operator|.
name|cachedGlobally
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|pagedBytesIndexFieldData
operator|.
name|loadGlobal
argument_list|(
operator|new
name|FieldMaskingReader
argument_list|(
literal|"field2"
argument_list|,
name|ir
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldDataCache
operator|.
name|cachedGlobally
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|ir
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|createSortedDV
specifier|private
name|SortedSetDVOrdinalsIndexFieldData
name|createSortedDV
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|IndexFieldDataCache
name|indexFieldDataCache
parameter_list|)
block|{
name|FieldDataType
name|fieldDataType
init|=
operator|new
name|StringFieldMapper
operator|.
name|StringFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
decl_stmt|;
return|return
operator|new
name|SortedSetDVOrdinalsIndexFieldData
argument_list|(
name|createIndexSettings
argument_list|()
argument_list|,
name|indexFieldDataCache
argument_list|,
name|fieldName
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|,
name|fieldDataType
argument_list|)
return|;
block|}
DECL|method|createPagedBytes
specifier|private
name|PagedBytesIndexFieldData
name|createPagedBytes
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|IndexFieldDataCache
name|indexFieldDataCache
parameter_list|)
block|{
name|FieldDataType
name|fieldDataType
init|=
operator|new
name|StringFieldMapper
operator|.
name|StringFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
decl_stmt|;
return|return
operator|new
name|PagedBytesIndexFieldData
argument_list|(
name|createIndexSettings
argument_list|()
argument_list|,
name|fieldName
argument_list|,
name|fieldDataType
argument_list|,
name|indexFieldDataCache
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|)
return|;
block|}
DECL|method|createIndexSettings
specifier|private
name|IndexSettings
name|createIndexSettings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|EMPTY
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"_name"
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
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|creationDate
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
operator|new
name|IndexSettings
argument_list|(
name|indexMetaData
argument_list|,
name|settings
argument_list|)
return|;
block|}
DECL|class|DummyAccountingFieldDataCache
specifier|private
class|class
name|DummyAccountingFieldDataCache
implements|implements
name|IndexFieldDataCache
block|{
DECL|field|cachedGlobally
specifier|private
name|int
name|cachedGlobally
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
DECL|method|load
specifier|public
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|FD
name|load
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|indexFieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
operator|.
name|Global
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|IFD
name|load
parameter_list|(
name|DirectoryReader
name|indexReader
parameter_list|,
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
block|{
name|cachedGlobally
operator|++
expr_stmt|;
return|return
operator|(
name|IFD
operator|)
name|indexFieldData
operator|.
name|localGlobalDirect
argument_list|(
name|indexReader
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{         }
block|}
block|}
end_class

end_unit

