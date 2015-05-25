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
name|cache
operator|.
name|bitset
operator|.
name|BitsetFilterCache
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
name|standard
operator|.
name|StandardAnalyzer
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
name|search
operator|.
name|Filter
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
name|FieldMapper
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
name|MapperService
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
name|ElasticsearchSingleNodeTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|fielddata
operator|.
name|IndexFieldData
operator|.
name|XFieldComparatorSource
operator|.
name|Nested
import|;
end_import

begin_class
DECL|class|AbstractFieldDataTests
specifier|public
specifier|abstract
class|class
name|AbstractFieldDataTests
extends|extends
name|ElasticsearchSingleNodeTest
block|{
DECL|field|indexService
specifier|protected
name|IndexService
name|indexService
decl_stmt|;
DECL|field|ifdService
specifier|protected
name|IndexFieldDataService
name|ifdService
decl_stmt|;
DECL|field|mapperService
specifier|protected
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|writer
specifier|protected
name|IndexWriter
name|writer
decl_stmt|;
DECL|field|readerContext
specifier|protected
name|LeafReaderContext
name|readerContext
decl_stmt|;
DECL|field|topLevelReader
specifier|protected
name|IndexReader
name|topLevelReader
decl_stmt|;
DECL|field|indicesFieldDataCache
specifier|protected
name|IndicesFieldDataCache
name|indicesFieldDataCache
decl_stmt|;
DECL|method|getFieldDataType
specifier|protected
specifier|abstract
name|FieldDataType
name|getFieldDataType
parameter_list|()
function_decl|;
DECL|method|hasDocValues
specifier|protected
name|boolean
name|hasDocValues
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|getForField
specifier|public
parameter_list|<
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|?
argument_list|>
parameter_list|>
name|IFD
name|getForField
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|getForField
argument_list|(
name|getFieldDataType
argument_list|()
argument_list|,
name|fieldName
argument_list|,
name|hasDocValues
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getForField
specifier|public
parameter_list|<
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|?
argument_list|>
parameter_list|>
name|IFD
name|getForField
parameter_list|(
name|FieldDataType
name|type
parameter_list|,
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|getForField
argument_list|(
name|type
argument_list|,
name|fieldName
argument_list|,
name|hasDocValues
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getForField
specifier|public
parameter_list|<
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|?
argument_list|>
parameter_list|>
name|IFD
name|getForField
parameter_list|(
name|FieldDataType
name|type
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|boolean
name|docValues
parameter_list|)
block|{
specifier|final
name|FieldMapper
name|mapper
decl_stmt|;
specifier|final
name|BuilderContext
name|context
init|=
operator|new
name|BuilderContext
argument_list|(
name|indexService
operator|.
name|settingsService
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
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|stringField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|tokenized
argument_list|(
literal|false
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|floatField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"double"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|doubleField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"long"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|longField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|integerField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"short"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|shortField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"byte"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|byteField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"geo_point"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|geoPointField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"_parent"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|parent
argument_list|()
operator|.
name|type
argument_list|(
name|fieldName
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"binary"
argument_list|)
condition|)
block|{
name|mapper
operator|=
name|MapperBuilders
operator|.
name|binaryField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|docValues
argument_list|(
name|docValues
argument_list|)
operator|.
name|fieldDataSettings
argument_list|(
name|type
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|type
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|ifdService
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
return|;
block|}
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.fielddata.cache"
argument_list|,
literal|"none"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|mapperService
operator|=
name|indexService
operator|.
name|mapperService
argument_list|()
expr_stmt|;
name|indicesFieldDataCache
operator|=
name|indexService
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesFieldDataCache
operator|.
name|class
argument_list|)
expr_stmt|;
name|ifdService
operator|=
name|indexService
operator|.
name|fieldData
argument_list|()
expr_stmt|;
comment|// LogByteSizeMP to preserve doc ID order
name|writer
operator|=
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
name|StandardAnalyzer
argument_list|()
argument_list|)
operator|.
name|setMergePolicy
argument_list|(
operator|new
name|LogByteSizeMergePolicy
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|refreshReader
specifier|protected
name|LeafReaderContext
name|refreshReader
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|readerContext
operator|!=
literal|null
condition|)
block|{
name|readerContext
operator|.
name|reader
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LeafReader
name|reader
init|=
name|SlowCompositeReaderWrapper
operator|.
name|wrap
argument_list|(
name|topLevelReader
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|readerContext
operator|=
name|reader
operator|.
name|getContext
argument_list|()
expr_stmt|;
return|return
name|readerContext
return|;
block|}
annotation|@
name|Override
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|readerContext
operator|!=
literal|null
condition|)
block|{
name|readerContext
operator|.
name|reader
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|createNested
specifier|protected
name|Nested
name|createNested
parameter_list|(
name|Filter
name|parentFilter
parameter_list|,
name|Filter
name|childFilter
parameter_list|)
block|{
name|BitsetFilterCache
name|s
init|=
name|indexService
operator|.
name|bitsetFilterCache
argument_list|()
decl_stmt|;
return|return
operator|new
name|Nested
argument_list|(
name|s
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|parentFilter
argument_list|)
argument_list|,
name|s
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|childFilter
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

